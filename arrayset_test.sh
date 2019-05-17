#!/usr/bin/env bash
javac -d ./run/ ./src/ru/ifmo/rain/ustinov/arrayset/ArraySet.java
cp ./lib/* ./run/
cp ./artifacts/info.kgeorgiy.java.advanced.arrayset.jar ./run/
cp ./artifacts/info.kgeorgiy.java.advanced.base.jar ./run/
java -cp ./run/ -p ./run/ -m info.kgeorgiy.java.advanced.arrayset NavigableSet ru.ifmo.rain.ustinov.arrayset.ArraySet $1
rm -rf ./run/
