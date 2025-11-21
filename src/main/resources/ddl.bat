@echo off

cd /d "D:\classProject\Mer-server\src\main\resources"


mysql.exe -h127.0.0.1 -P3306 -uroot -pJnh@9372 mer ^
  < "D:\classProject\Mer-server\src\main\resources\ddl.sql"
