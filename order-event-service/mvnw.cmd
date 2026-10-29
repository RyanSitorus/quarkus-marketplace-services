@ECHO OFF
SETLOCAL

SET "CACHED_MVN=%USERPROFILE%\.m2\wrapper\dists\apache-maven-3.9.5-bin\2adeog8mj13csp1uusqnc1f2mo\apache-maven-3.9.5\bin\mvn.cmd"

IF EXIST "%CACHED_MVN%" (
    CALL "%CACHED_MVN%" %*
    EXIT /B %ERRORLEVEL%
)

WHERE mvn >NUL 2>NUL
IF %ERRORLEVEL% EQU 0 (
    CALL mvn %*
    EXIT /B %ERRORLEVEL%
)

ECHO Maven not found in PATH or .m2 wrapper cache. Downloading Apache Maven 3.9.5...
powershell -NoProfile -ExecutionPolicy Bypass -Command ^
    "$destDir = Join-Path $env:USERPROFILE '.m2\wrapper\dists\apache-maven-3.9.5-bin\2adeog8mj13csp1uusqnc1f2mo'; ^
     New-Item -ItemType Directory -Force -Path $destDir | Out-Null; ^
     $zipPath = Join-Path $destDir 'apache-maven-3.9.5-bin.zip'; ^
     Invoke-WebRequest -Uri 'https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.5/apache-maven-3.9.5-bin.zip' -OutFile $zipPath; ^
     Expand-Archive -Path $zipPath -DestinationPath $destDir -Force"

CALL "%CACHED_MVN%" %*
EXIT /B %ERRORLEVEL%

