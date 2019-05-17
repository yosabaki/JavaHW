#!/usr/bin/env bash
MY_CLASS_PATH=src/ru/ifmo/rain/ustinov
GOSHA_CLASS_PATH=artifacts/info.kgeorgiy.java.advanced.mapper.jar:artifacts/info.kgeorgiy.java.advanced.crawler.jar
mkdir run
cp ./lib/* run
cp ./artifacts/*
javac -d ./run/ -cp ./run/:$GOSHA_CLASS_PATH:./artifacts/ -p ./run/:$GOSHA_CLASS_PATH:./artifacts/ $MY_CLASS_PATH/crawler/WebCrawler.java
#java -cp ./run/:$GOSHA_CLASS_PATH:./artifacts/ -p ./run/:$GOSHA_CLASS_PATH:./artifacts/ -m info.kgeorgiy.java.advanced.mapper $1 ru.ifmo.rain.ustinov.mapper.ParallelMapperImpl,ru.ifmo.rain.ustinov.concurrent.IterativeParallelism $2
java -cp ./run/:$GOSHA_CLASS_PATH:./artifacts/ -p ./run/:$GOSHA_CLASS_PATH:./artifacts/ -m info.kgeorgiy.java.advanced.crawler $1 ru.ifmo.rain.ustinov.crawler.WebCrawler $2
rm -rf run
