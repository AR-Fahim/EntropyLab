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

# 4. Determine package type (exe installer if WiX available, otherwise app-image)
$packageType = "exe"
if (!(Get-Command candle.exe -ErrorAction SilentlyContinue)) {
    Write-Warning "WiX not found on PATH. Falling back to app-image."
    $packageType = "app-image"
}

$destDir = "target/installer"
if (Test-Path $destDir) { Remove-Item -Recurse -Force $destDir }
New-Item -ItemType Directory -Force $destDir | Out-Null

Write-Host "Running jpackage (type=$packageType)..."
$jpackageArgs = @(
    "--type", $packageType,
    "--dest", $destDir,
    "--name", "EntropyLab",
    "--app-version", "1.0.0",
    "--vendor", "Abdur Rahman",
    "--input", $packageInput,
    "--main-jar", "entropylab-1.0.0-SNAPSHOT.jar",
    "--main-class", "com.entropylab.Main",
    "--module-path", $javafxMods,
    "--add-modules", "javafx.controls,jdk.httpserver,java.net.http,java.sql,java.desktop,java.naming,jdk.unsupported,java.xml"
)

if ($packageType -eq "exe" -or $packageType -eq "msi") {
    $jpackageArgs += @(
        "--win-menu",
        "--win-shortcut",
        "--win-dir-chooser"
    )
}

jpackage @jpackageArgs

Write-Host "Packaging complete! Output located at: $destDir"
Get-ChildItem $destDir
