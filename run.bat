@echo off
IF NOT EXIST %comspec% GOTO nocomspec

:comspec
rem %comspec% points to an existing command interpreter
%comspec% /E:4096 /C SERVER.bat
goto end

:nocomspec
rem %comspec% is not set, trying command.com
command /E:4096 /C SERVER.bat

goto end

:end
