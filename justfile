# A justfile :3

build:
    ./gradlew build

clean:
    ./gradlew clean

mergeJars:
    ./gradlew mergeJars

ide:
    nohup idea ./ >/dev/null 2>&1 &
