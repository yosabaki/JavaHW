javadoc -html4 -p ./artifacts:./lib -author -version -private \
    -link https://docs.oracle.com/en/java/javase/11/docs/api/ \
    --module-source-path ./src/:./modules/ \
    --module ru.ifmo.rain.ustinov.implementor,info.kgeorgiy.java.advanced.implementor \
    -d javadoc
