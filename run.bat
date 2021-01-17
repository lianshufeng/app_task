@echo off

REM 加载环境变量
call %~dp0runtime\env.bat

#修改控制台输出为UTF-8
chcp 65001
REM 启动服务
cd /d %~dp0build
java -Dfile.encoding=utf-8 -jar apptask-0.0.1-SNAPSHOT.jar