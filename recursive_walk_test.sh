#!/usr/bin/env bash
javac -d ./run/ ./src/ru/ifmo/rain/ustinov/walk/RecursiveWalk.java
cp ./lib/* ./run/
cp ./artifacts/info.kgeorgiy.java.advanced.walk.jar ./run/
cp ./artifacts/info.kgeorgiy.java.advanced.base.jar ./run/
java -cp ./run/ -p ./run/ -m info.kgeorgiy.java.advanced.walk RecursiveWalk ru.ifmo.rain.ustinov.walk.RecursiveWalk $1
rm -rf ./run/
