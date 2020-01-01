#!/bin/bash
JAVA_PATH=$(/usr/libexec/java_home -v 11)/bin
JPACKAGER_PATH=~/Downloads/jdk.packager-osx

#echo "Creating application jmod"
#mkdir -p build/modules
#"${JAVA_PATH}"/jmod create --class-path build/libs/MasterTools-1.0-SNAPSHOT.jar \
#  --main-class org.culpan.mastertools.MainApp \
#  build/modules/mastertools.jmod

./gradlew clean build

echo "Creating image"
#rm -rf build/image
#"${JAVA_PATH}"/jlink --no-header-files --no-man-pages \
#   --add-modules java.datatransfer,java.desktop,java.logging,java.scripting,\
#java.sql,java.xml,jdk.jsobject,jdk.unsupported,jdk.unsupported.desktop,jdk.xml.dom,javafx.base,javafx.controls,javafx.fxml \
#   --module-path ~/javafx/current/javafx-jmods-11.0.2:build/modules \
#   --output build/image

#cp libs/* build/image
#cp build/libs/* build/image
#cp start build/image
#chmod a+x build/image/start

${JPACKAGER_PATH}/jpackager \
   create-image \
   --module-path lib/javafx-jmods-11.0.2 \
   --verbose \
   --echo-mode \
   --add-modules java.datatransfer,java.desktop,java.logging,java.scripting,\
java.sql,java.xml,jdk.jsobject,jdk.unsupported,jdk.unsupported.desktop,\
jdk.xml.dom,javafx.base,javafx.controls,javafx.fxml \
   --input libs \
   --output build/final-image \
   --name MasterTools \
   --main-jar build/MasterTools-1.0-SNAPSHOT.jar \
   --version 1.0 \
   --jvm-args '--add-opens javafx.base/com.sun.javafx.reflect=ALL-UNNAMED' \
   --class org.culpan.mastertools.MainApp

echo "Done"
