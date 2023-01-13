#!/bin/bash
NEXT_VERSION_NAME=$1
FILE_LOCATION="app/build/outputs/apk/dev/release/bida-scoreboard-dev-$NEXT_VERSION_NAME.apk"
scp -P 54822 $FILE_LOCATION root@163.43.104.239:/var/www/html