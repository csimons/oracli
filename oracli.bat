@echo off
REM Christopher L. Simons

setlocal enableDelayedExpansion

if not defined ORACLI_HOME (goto no_oracli_home)

set "CONFIG=%HOMEDRIVE%%HOMEPATH%\.config\oracli.conf"

set ORACLI_LIB=

for %%a in (%ORACLI_HOME%\lib\*.jar) do (
    set "ORACLI_LIB=!ORACLI_LIB!;%%a"
)

echo Using Windows, so external editing and command history unavailable.

"%JAVA_HOME%\bin\java" -cp %ORACLI_LIB% clojure.main "%ORACLI_HOME%\src\oracli.clj" %CONFIG% %1

goto success

:no_oracli_home

echo ORACLI_HOME environment variable must be set.
rem exit 1

:success

rem exit 0
