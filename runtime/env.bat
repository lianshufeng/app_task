@echo off
REM 设置环境变量
set JAVA_HOME=%~dp0jdk
set ANDROID_HOME=%~dp0android-sdk
set NODE_HOME=%~dp0node


set Path=%JAVA_HOME%\bin;%Path%
set Path=%ANDROID_HOME%\platform-tools;%Path%
set Path=%NODE_HOME%;%Path%




REM echo %JAVA_HOME%
REM echo %ANDROID_HOME%
REM echo %NODE_HOME%
REM echo %Path%
