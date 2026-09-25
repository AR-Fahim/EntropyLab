package com.entropylab.ui;

import com.sun.jna.CallbackReference;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.*;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.platform.win32.WinUser.WindowProc;
import com.sun.jna.ptr.IntByReference;
import javafx.stage.Stage;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * WindowsSnapHelper bridges the JavaFX undecorated window with the native Windows
 * Desktop Window Manager (DWM) and Windows Shell.
 *
 * It removes WS_POPUP and adds WS_THICKFRAME, WS_CAPTION, WS_SYSMENU, WS_MINIMIZEBOX,
 * WS_MAXIMIZEBOX, and WS_EX_APPWINDOW so Windows treats the app as a first-class
 * arrangable application.
 *
 * It hooks WndProc to:
 * 1. Return 0 for WM_NCCALCSIZE so the custom title bar renders at (0,0) without standard OS chrome.
 * 2. Return ptMinTrackSize for WM_GETMINMAXINFO to natively enforce minimum boundaries (850x550).
 * 3. Return HTCAPTION / HTMAXBUTTON / HTCLIENT / resize border codes for WM_NCHITTEST so:
 *    - Win + Left / Win + Right natively snaps into split screen alongside other apps with Snap Assist.
 *    - Win + Up / Win + Down maximizes / restores.
 *    - Title bar drag natively snaps to screen edges.
 *    - Hovering over Maximize button displays Windows 11 Snap Layouts.
 *    - Windows draws the native DWM drop shadow.
 */
public class WindowsSnapHelper {

    public static class MARGINS extends Structure {
        public int cxLeftWidth;
        public int cxRightWidth;
        public int cyTopHeight;
        public int cyBottomHeight;

        public MARGINS(int l, int r, int t, int b) {
            cxLeftWidth = l;
            cxRightWidth = r;
            cyTopHeight = t;
            cyBottomHeight = b;
        }

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("cxLeftWidth", "cxRightWidth", "cyTopHeight", "cyBottomHeight");
        }
    }

    public static class POINT extends Structure {
        public int x;
        public int y;

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("x", "y");
        }
    }

    public static class MINMAXINFO extends Structure {
        public POINT ptReserved;
        public POINT ptMaxSize;
        public POINT ptMaxPosition;
        public POINT ptMinTrackSize;
        public POINT ptMaxTrackSize;

        public MINMAXINFO(Pointer p) {
            super(p);
            read();
        }

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("ptReserved", "ptMaxSize", "ptMaxPosition", "ptMinTrackSize", "ptMaxTrackSize");
        }
    }

    public interface DwmApi extends com.sun.jna.Library {
        DwmApi INSTANCE = Native.load("dwmapi", DwmApi.class);
        int DwmExtendFrameIntoClientArea(HWND hWnd, MARGINS pMarInset);
    }

    private static final int GWL_STYLE = -16;
    private static final int GWL_EXSTYLE = -20;
    private static final int GWLP_WNDPROC = -4;

    private static final int WS_POPUP = 0x80000000;
    private static final int WS_CAPTION = 0x00C00000;
    private static final int WS_THICKFRAME = 0x00040000;
    private static final int WS_MINIMIZEBOX = 0x00020000;
    private static final int WS_MAXIMIZEBOX = 0x00010000;
    private static final int WS_SYSMENU = 0x00080000;
    private static final int WS_EX_APPWINDOW = 0x00040000;

    private static final int SWP_NOSIZE = 0x0001;
    private static final int SWP_NOMOVE = 0x0002;
    private static final int SWP_NOZORDER = 0x0004;
    private static final int SWP_FRAMECHANGED = 0x0020;

    private static final int WM_NCCALCSIZE = 0x0083;
    private static final int WM_NCHITTEST = 0x0084;
    private static final int WM_GETMINMAXINFO = 0x0024;

    private static final int HTCLIENT = 1;
    private static final int HTCAPTION = 2;
    private static final int HTMAXBUTTON = 9;
    private static final int HTLEFT = 10;
    private static final int HTRIGHT = 11;
    private static final int HTTOP = 12;
    private static final int HTTOPLEFT = 13;
    private static final int HTTOPRIGHT = 14;
    private static final int HTBOTTOM = 15;
    private static final int HTBOTTOMLEFT = 16;
    private static final int HTBOTTOMRIGHT = 17;

    // Retain strong reference to prevent GC collection of native callback
    private static WindowProc wndProc;
    private static Pointer oldWndProc;

    public static void enableSnap(Stage stage) {
        enableSnap(stage, 850, 550);
    }

    public static void enableSnap(Stage stage, int minWidth, int minHeight) {
        if (!isWindows()) {
            return;
        }

        javafx.application.Platform.runLater(() -> {
            try {
                HWND hwnd = findHwnd(stage);
                if (hwnd != null) {
                    applySnapStylesAndHook(hwnd, minWidth, minHeight);
                }
            } catch (Throwable t) {
                System.err.println("Could not enable native Windows snap: " + t.getMessage());
            }
        });
    }

    private static void applySnapStylesAndHook(HWND hwnd, int minW, int minH) {
        int style = User32.INSTANCE.GetWindowLong(hwnd, GWL_STYLE);
        int exStyle = User32.INSTANCE.GetWindowLong(hwnd, GWL_EXSTYLE);

        // Remove WS_POPUP, add WS_THICKFRAME, WS_CAPTION, WS_SYSMENU, WS_MINIMIZEBOX, WS_MAXIMIZEBOX
        int newStyle = (style & ~WS_POPUP) | WS_THICKFRAME | WS_CAPTION | WS_SYSMENU | WS_MINIMIZEBOX | WS_MAXIMIZEBOX;
        int newExStyle = exStyle | WS_EX_APPWINDOW;

        User32.INSTANCE.SetWindowLong(hwnd, GWL_STYLE, newStyle);
        User32.INSTANCE.SetWindowLong(hwnd, GWL_EXSTYLE, newExStyle);

        // Extend DWM frame for native shadow
        try {
            MARGINS margins = new MARGINS(1, 1, 1, 1);
            DwmApi.INSTANCE.DwmExtendFrameIntoClientArea(hwnd, margins);
        } catch (Throwable ignored) {
        }

        wndProc = (hWnd, uMsg, wParam, lParam) -> {
            if (uMsg == WM_NCCALCSIZE) {
                if (wParam != null && wParam.intValue() != 0) {
                    // Tell Windows that the client area covers the entire window rectangle
                    return new LRESULT(0);
                }
            } else if (uMsg == WM_GETMINMAXINFO) {
                if (lParam != null) {
                    MINMAXINFO mmi = new MINMAXINFO(new Pointer(lParam.longValue()));
                    mmi.ptMinTrackSize.x = minW;
                    mmi.ptMinTrackSize.y = minH;
                    mmi.write();
                    return new LRESULT(0);
                }
            } else if (uMsg == WM_NCHITTEST) {
                long lp = lParam.longValue();
                short x = (short) (lp & 0xFFFF);
                short y = (short) ((lp >> 16) & 0xFFFF);

                RECT rect = new RECT();
                User32.INSTANCE.GetWindowRect(hWnd, rect);

                int border = 8;
                boolean isLeft = x >= rect.left && x < rect.left + border;
                boolean isRight = x <= rect.right && x > rect.right - border;
                boolean isTop = y >= rect.top && y < rect.top + border;
                boolean isBottom = y <= rect.bottom && y > rect.bottom - border;

                // Border hit tests for native resizing cursors & hardware drag
                if (isTop && isLeft) return new LRESULT(HTTOPLEFT);
                if (isTop && isRight) return new LRESULT(HTTOPRIGHT);
                if (isBottom && isLeft) return new LRESULT(HTBOTTOMLEFT);
                if (isBottom && isRight) return new LRESULT(HTBOTTOMRIGHT);
                if (isLeft) return new LRESULT(HTLEFT);
                if (isRight) return new LRESULT(HTRIGHT);
                if (isTop) return new LRESULT(HTTOP);
                if (isBottom) return new LRESULT(HTBOTTOM);

                // Title bar area (top 42px)
                if (y >= rect.top && y < rect.top + 42) {
                    // Check if mouse is over window controls area on right side (~180px)
                    if (x >= rect.right - 180) {
                        // Maximize button area (~46px between rect.right - 92 and rect.right - 46)
                        if (x >= rect.right - 92 && x <= rect.right - 46) {
                            return new LRESULT(HTMAXBUTTON);
                        }
                        return new LRESULT(HTCLIENT);
                    }
                    return new LRESULT(HTCAPTION);
                }

                return new LRESULT(HTCLIENT);
            }

            return User32.INSTANCE.CallWindowProc(oldWndProc, hWnd, uMsg, wParam, lParam);
        };

        if (Platform.is64Bit()) {
            oldWndProc = User32.INSTANCE.SetWindowLongPtr(
                    hwnd,
                    GWLP_WNDPROC,
                    CallbackReference.getFunctionPointer(wndProc)
            );
        } else {
            oldWndProc = new Pointer(User32.INSTANCE.SetWindowLong(
                    hwnd,
                    GWLP_WNDPROC,
                    (int) Pointer.nativeValue(CallbackReference.getFunctionPointer(wndProc))
            ));
        }

        User32.INSTANCE.SetWindowPos(hwnd, null, 0, 0, 0, 0,
                SWP_NOMOVE | SWP_NOSIZE | SWP_NOZORDER | SWP_FRAMECHANGED);
    }

    private static HWND findHwnd(Stage stage) {
        try {
            int currentPid = Kernel32.INSTANCE.GetCurrentProcessId();
            AtomicReference<HWND> found = new AtomicReference<>();
            User32.INSTANCE.EnumWindows((hwnd, data) -> {
                IntByReference pidRef = new IntByReference();
                User32.INSTANCE.GetWindowThreadProcessId(hwnd, pidRef);
                if (pidRef.getValue() == currentPid && User32.INSTANCE.IsWindowVisible(hwnd)) {
                    found.set(hwnd);
                    return false;
                }
                return true;
            }, null);

            if (found.get() != null) {
                return found.get();
            }
        } catch (Throwable ignored) {
        }

        if (stage != null && stage.getTitle() != null) {
            try {
                return User32.INSTANCE.FindWindow(null, stage.getTitle());
            } catch (Throwable ignored) {
            }
        }

        return null;
    }

    private static boolean isWindows() {
        String os = System.getProperty("os.name");
        return os != null && os.toLowerCase().contains("win");
    }
}
