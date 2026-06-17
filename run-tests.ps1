# ---------------------------------------------------------------------------
# Runs the rentDepositManagement test suite from the terminal.
#
# Normally you would just run:  .\mvnw.cmd test
# But in this offline/corporate-proxy setup the Surefire test runner cannot be
# downloaded, so this script compiles offline and runs the tests via the JUnit
# Platform launcher directly.
#
# Usage:
#   .\run-tests.ps1                 # uses MySQL password 'root'
#   $env:DB_PASSWORD="secret"; .\run-tests.ps1
# ---------------------------------------------------------------------------
$ErrorActionPreference = "Stop"
$root = $PSScriptRoot

# Locate the JDK (prefer JAVA_HOME, fall back to a common install path).
if ($env:JAVA_HOME -and (Test-Path "$env:JAVA_HOME\bin\java.exe")) {
    $javaHome = $env:JAVA_HOME
} elseif (Test-Path "C:\Program Files\Java\jdk-21\bin\java.exe") {
    $javaHome = "C:\Program Files\Java\jdk-21"
} else {
    throw "Could not find a JDK. Set JAVA_HOME."
}
$java  = "$javaHome\bin\java.exe"
$javac = "$javaHome\bin\javac.exe"

# 1. Compile main + test sources (works offline).
Write-Host "==> Compiling (offline)..." -ForegroundColor Cyan
& "$root\mvnw.cmd" -o test-compile
if ($LASTEXITCODE -ne 0) { throw "Compilation failed." }

# 2. Read the prebuilt test classpath.
$cpFile = "$root\test-tools\test-classpath.txt"
if (-not (Test-Path $cpFile)) { throw "Missing $cpFile (run the classpath capture step)." }
$cp = [IO.File]::ReadAllText($cpFile).Trim()
$env:CLASSPATH = $cp

# 3. Compile the launcher.
& $javac -proc:none -d "$root\target\runner" "$root\test-tools\RunTests.java"
if ($LASTEXITCODE -ne 0) { throw "Launcher compilation failed." }

# 4. Run the suite.
$dbPass = if ($env:DB_PASSWORD) { $env:DB_PASSWORD } else { "root" }
Write-Host "==> Running tests..." -ForegroundColor Cyan
& $java "-XX:+EnableDynamicAgentLoading" "-Dspring.datasource.password=$dbPass" RunTests
exit $LASTEXITCODE
