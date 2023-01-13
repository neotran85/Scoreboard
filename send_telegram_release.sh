#!/bin/bash
NEXT_VERSION_NAME=$1
message="Build version STAGING is success: http://163.43.104.239/bida-scoreboard-staging-$NEXT_VERSION_NAME.apk"
FILE_LOCATION="app/build/outputs/apk/staging/release/bida-scoreboard-staging-$NEXT_VERSION_NAME.apk"
scp -P 54822 $FILE_LOCATION root@163.43.104.239:/var/www/html
TELEGRAM_BOT_TOKEN="1307304356:AAGFu3l9pJWL4vfE3a_34EPMsHLMKO1ZfiI"
TELEGRAM_BOT_GROUP="-887223456"
curl -s -X POST https://api.telegram.org/bot$TELEGRAM_BOT_TOKEN/sendMessage \
         -d chat_id=$TELEGRAM_BOT_GROUP \
         -d text="$message"