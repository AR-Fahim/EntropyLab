param(
    [string]$PackageType = "msi"
)

$ErrorActionPreference = "Stop"

# 1. Ensure WiX tools are on PATH
$wixDir = "C:\Users\Fahim\.wix311"
if (Test-Path "$wixDir\candle.exe") {
    $env:PATH = "$wixDir;" + $env:PATH
}

# 2. Package and gather dependencies
Write-Host "Building project JAR and dependencies..."
mvn package -DskipTests
mvn dependency:copy-dependencies -DoutputDirectory=target/libs

# 3. Prepare input directories
$packageInput = "target/package-input"
$javafxMods = "target/javafx-mods"

if (Test-Path $packageInput) { Remove-Item -Recurse -Force $packageInput }
if (Test-Path $javafxMods) { Remove-Item -Recurse -Force $javafxMods }

New-Item -ItemType Directory -Force $packageInput | Out-Null
New-Item -ItemType Directory -Force $javafxMods | Out-Null

Copy-Item "target/libs/*.jar" $packageInput
Copy-Item "target/entropylab-1.0.0-SNAPSHOT.jar" $packageInput
Copy-Item "target/libs/*-win.jar" $javafxMods

# 4. Check WiX availability
if (!(Get-Command candle.exe -ErrorAction SilentlyContinue)) {
    Write-Warning "WiX not found on PATH. Falling back to app-image."
    $PackageType = "app-image"
}

$destDir = "target/installer"
if (Test-Path $destDir) { Remove-Item -Recurse -Force $destDir }
New-Item -ItemType Directory -Force $destDir | Out-Null

$iconPath = "src/main/resources/com/entropylab/branding/EntropyLab.ico"

Write-Host "Running jpackage (type=$PackageType)..."
$jpackageArgs = @(
    "--type", $PackageType,
    "--dest", $destDir,
    "--name", "EntropyLab",
    "--app-version", "1.0.0",
    "--vendor", "Abdur Rahman",
    "--description", "EntropyLab - Reverse Proxy & Chaos Engineering Lab",
    "--input", $packageInput,
    "--main-jar", "entropylab-1.0.0-SNAPSHOT.jar",
    "--main-class", "com.entropylab.Main",
    "--module-path", $javafxMods,
    "--add-modules", "javafx.controls,jdk.httpserver,java.net.http,java.sql,java.desktop,java.naming,jdk.unsupported,java.xml,jdk.crypto.ec,jdk.crypto.mscapi"
)

if (Test-Path $iconPath) {
    $jpackageArgs += @("--icon", $iconPath)
}

if ($PackageType -eq "exe" -or $PackageType -eq "msi") {
    $jpackageArgs += @(
        "--win-menu",
        "--win-menu-group", "EntropyLab",
        "--win-shortcut",
        "--win-shortcut-prompt",
        "--win-dir-chooser",
        "--win-upgrade-uuid", "3f98c4c2-9b2e-4e89-a291-76a08696ecde"
    )
}

jpackage @jpackageArgs

Write-Host "Packaging complete! Output located at: $destDir"
Get-ChildItem $destDir
