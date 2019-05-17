#!/usr/bin/env bash
javac -d ./run/ -cp ./artifacts/info.kgeorgiy.java.advanced.student.jar ./src/ru/ifmo/rain/ustinov/student/StudentDB.java
cp ./lib/* ./run/
cp ./artifacts/info.kgeorgiy.java.advanced.student.jar ./run/
cp ./artifacts/info.kgeorgiy.java.advanced.base.jar ./run/
java -cp ./run/ -p ./run -m info.kgeorgiy.java.advanced.student StudentGroupQuery ru.ifmo.rain.ustinov.student.StudentDB $1
rm -rf ./run/
