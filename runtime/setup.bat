@echo off

:: jdk : https://jdk.java.net/java-se-ri/11

:: node : https://nodejs.org/en/download/

:: android-cmdline-tools : https://developer.android.com/studio#downloads

:: 加载环境变量
call %~dp0env.bat


REM 创建目录
mkdir %ANDROID_HOME%
mkdir jdk
mkdir node
mkdir android-cmdline-tools


:: android-sdk
echo [update] - android-sdk
set SdkmanagerPath=%~dp0android-cmdline-tools\bin\sdkmanager
cmd /c %SdkmanagerPath% --sdk_root=%ANDROID_HOME% --licenses
cmd /c %SdkmanagerPath% --sdk_root=%ANDROID_HOME%  "platform-tools" "build-tools;29.0.2"

:: appium
cmd /c %~dp0appium\install.bat


:: simulator , 设置雷电模拟器的adb为运行环境配置的
taskkill /im adb.exe /f
del %~dp0.\simulator\adb.exe
mklink %~dp0.\simulator\adb.exe %ANDROID_HOME%\platform-tools\adb.exe


PAUSE