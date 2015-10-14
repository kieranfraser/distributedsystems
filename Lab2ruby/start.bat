
echo off

set arg1=%1
set PATHTORUBY=%cd%\Ruby21\bin
set RUBY=%PATHTORUBY%\ruby.exe

start cmd /k %RUBY% Server.rb %arg1%


