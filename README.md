

- simulator
````shell
md simulator
xcopy C:\LeiDian\LDPlayer4.0 .\simulator\ /y /e
````

- android-sdk
````shell
set ANDROID_HOME env
path=%ANDROID_HOME%\platform-tools

#Command line tools only
https://developer.android.com/studio#downloads 

#licenses
sdkmanager --sdk_root=%ANDROID_HOME% --licenses

#instal sdk
sdkmanager --sdk_root=%ANDROID_HOME%  "platform-tools" "build-tools;29.0.2"


#link adb.exe
cd ./simulator
mklink adb.exe %ANDROID_HOME%\platform-tools\adb.exe
````