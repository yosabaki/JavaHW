#!/usr/bin/env bash
mkdir run
cp -R src/ru/ifmo/rain/ustinov/implementor/* src/ru.ifmo.rain.ustinov.implementor/ru/ifmo/rain/ustinov/implementor
javac -d ./run/ -cp artifacts -p artifacts:lib --module-source-path src --module ru.ifmo.rain.ustinov.implementor
mkdir out-jar
jar --create --file=out-jar/ru.ifmo.rain.ustinov.implementor.jar --main-class=ru.ifmo.rain.ustinov.implementor.Implementor -C run/ru.ifmo.rain.ustinov.implementor .
java -cp artifacts:out-jar/ru.ifmo.rain.ustinov.implementor.jar/:lib -p artifacts:lib -m info.kgeorgiy.java.advanced.implementor $1 ru.ifmo.rain.ustinov.implementor.Implementor $2
rm -rf run
