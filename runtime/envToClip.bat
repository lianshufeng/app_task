@echo off
REM 设置环境变量
call %~dp0env.bat


set SDK_HOME=JAVA_HOME=%JAVA_HOME%
set SDK_HOME=%SDK_HOME%;ANDROID_HOME=%ANDROID_HOME%
set SDK_HOME=%SDK_HOME%;NODE_HOME=%NODE_HOME%

:: 复制到剪贴板
set/p=%SDK_HOME%<nul | clip



PAUSE