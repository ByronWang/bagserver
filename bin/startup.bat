
set NEBULA_HOME=

rem Guess NEBULA_HOME if not defined
set "CURRENT_DIR=%cd%"
if not "%NEBULA_HOME%" == "" goto gotHome
echo NOT GOT HOME
set "NEBULA_HOME=%CURRENT_DIR%"
if exist "%NEBULA_HOME%\bin\startup.bat" goto okHome
cd ..
set "NEBULA_HOME=%cd%"
:gotHome
echo GOTHOME
if exist "%NEBULA_HOME%\bin\startup.bat" goto okHome
echo The NEBULA_HOME environment variable is not defined correctly
echo This environment variable is needed to run this program
goto end
:okHome

java -server -cp .;lib\*;conf\* http.startup.JettyServer

