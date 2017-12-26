if [[ $BUTTER_SIGNING_KEY && ${BUTTER_SIGNING_KEY} ]]
then
    echo "Keystore detected - downloading..."
    curl -L -o release-keystore.jks ${BUTTER_SIGNING_KEY}
    echo "butterReleaseStoreFile=../release-keystore.jks" >> gradle.properties
    echo "butterReleaseStorePassword=${BUTTER_SIGNING_PASSWORD}" >> gradle.properties
    echo "butterReleaseKeyAlias=butter" >> gradle.properties
    echo "butterReleaseKeyPassword=${BUTTER_SIGNING_PASSWORD}" >> gradle.properties
else
    echo "Keystore uri not set.  .APK artifact will not be signed."
fi
