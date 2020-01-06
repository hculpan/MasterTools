#!/bin/sh
# Assumes scripts is in Contents/MacOS, but everything else is pathed
# relative to Contents
cd -- "$(dirname "$BASH_SOURCE")/.."
CURR_DIR=`pwd`
${CURR_DIR}/bin/java \
  --add-modules java.sql,javafx.base,javafx.controls,javafx.fxml \
  -cp MasterTools-1.0-SNAPSHOT.jar:sqlite-jdbc-3.28.0.jar \
  org.culpan.mastertools.MainApp &
