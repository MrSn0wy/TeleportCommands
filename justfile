# A justfile :3

build:
    ./gradlew build

clean:
    ./gradlew clean

mergeJars:
    ./gradlew mergeJars

ide:
    nohup idea ./ >/dev/null 2>&1 &

fabricClient:
    ./gradlew :fabric:runClient

fabricServer:
    ./gradlew :fabric:runServer

neoforgeClient:
    ./gradlew :neoforge:runClient

neoforgeServer:
    ./gradlew :neoforge:runServer
