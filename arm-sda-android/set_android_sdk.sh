
ANDROID_SDK_FILENAME=sdk-tools-linux-3859397.zip

#Get Android SDK from NGINX server
wget http://prov-docker.kfn.arm.com:6868/$ANDROID_SDK_FILENAME
#Unzip the sdk
unzip sdk-tools-linux-3859397.zip
#Accept to all licenses
yes | tools/bin/sdkmanager --licenses

#Note: you need to export ANDROID_HOME with the arm-sda-android location,
#the sdk extracted to this location and Android build need it.
#We export ANDROID_HOME in Jenkins JOB
