if [[ $BUTTER_PLAY_CONFIG && ${BUTTER_PLAY_CONFIG} ]]
then
    echo "Play config detected - downloading..."
    curl -L -o play_publish.json ${BUTTER_PLAY_CONFIG}
    echo "playPublishFile=../play_publish.json" >> gradle.properties
else
    echo "Play config uri not set. APK will not be uploaded."
fi
