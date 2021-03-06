#!/bin/bash
JAVA_PATH=$(/usr/libexec/java_home -v 13)/bin
#JPACKAGER_PATH=~/Downloads/jdk.packager-osx

#echo "Creating application jmod"
#mkdir -p build/modules
#"${JAVA_PATH}"/jmod create --class-path build/libs/MasterTools-1.0-SNAPSHOT.jar \
#  --main-class org.culpan.mastertools.MainApp \
#  build/modules/mastertools.jmod

./create_icon.sh

cp MasterTools.iconset/icon_64x64.png src/main/resources

./gradlew clean build

echo "Creating image"
rm -rf build/image
"${JAVA_PATH}"/jlink --no-header-files --no-man-pages \
   --add-modules java.datatransfer,java.desktop,java.logging,java.scripting,\
java.sql,java.xml,jdk.jsobject,jdk.unsupported,jdk.unsupported.desktop,jdk.xml.dom,\
javafx.base,javafx.controls,javafx.fxml,javafx.web \
   --module-path ~/javafx/current/javafx-jmods-11.0.2:build/modules \
   --output build/image

cp libs/sqlite-jdbc-3.28.0.jar build/image
cp build/libs/* build/image

mkdir -p build/MasterTools.app/Contents/Resources
mkdir -p build/MasterTools.app/Contents/MacOS

cp MasterTools.icns build/MasterTools.app/Contents/Resources
cp -r build/image/* build/MasterTools.app/Contents
cp start.command build/MasterTools.app/Contents/MacOS
chmod a+x build/MasterTools.app/Contents/MacOS/start.command
cp info.plist build/MasterTools.app/Contents

#${JPACKAGER_PATH}/jpackager \
#   create-image \
#   --module-path libs/javafx-jmods-11.0.2 \
#   --verbose \
#   --echo-mode \
#   --add-modules java.datatransfer,java.desktop,java.logging,java.scripting,\
#java.sql,java.xml,jdk.jsobject,jdk.unsupported,jdk.unsupported.desktop,\
#jdk.xml.dom,javafx.base,javafx.controls,javafx.fxml \
#   --input src \
#   --output build/final-image \
#   --name MasterTools \
#   --main-jar build/libs/MasterTools-1.0-SNAPSHOT.jar \
#   --version 1.0 \
#   --jvm-args '--add-opens javafx.base/com.sun.javafx.reflect=ALL-UNNAMED' \
#   --class org.culpan.mastertools.MainApp \
#   --verbose

echo "Done"
