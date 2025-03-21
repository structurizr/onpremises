#!/bin/zsh

# - this script merges the contents of the structurizr/ui repository into this directory,
# - this has only been tested on MacOS

export STRUCTURIZR_BUILD_NUMBER=$1
export STRUCTURIZR_UI_DIR=../structurizr-ui
export STRUCTURIZR_ONPREMISES_DIR=./structurizr-onpremises

rm -rf $STRUCTURIZR_ONPREMISES_DIR/src/main/webapp/static/bootstrap-icons
rm -rf $STRUCTURIZR_ONPREMISES_DIR/src/main/webapp/static/css
rm -rf $STRUCTURIZR_ONPREMISES_DIR/src/main/webapp/static/html
rm -rf $STRUCTURIZR_ONPREMISES_DIR/src/main/webapp/static/img
rm -rf $STRUCTURIZR_ONPREMISES_DIR/src/main/webapp/static/js

# JavaScript
mkdir -p $STRUCTURIZR_ONPREMISES_DIR/src/main/webapp/static/js
cp -a $STRUCTURIZR_UI_DIR/src/js/* $STRUCTURIZR_ONPREMISES_DIR/src/main/webapp/static/js/

if [[ $STRUCTURIZR_BUILD_NUMBER != "" ]]
then
  for file in $STRUCTURIZR_ONPREMISES_DIR/src/main/webapp/static/js/structurizr*.js
  do
    filename="${file%.*}"

    if [[ $file == *structurizr-embed.js ]]
    then
      echo "Skipping $filename"
    else
      echo "Renaming $filename-$STRUCTURIZR_BUILD_NUMBER.js"
      mv "$filename.js" "$filename-$STRUCTURIZR_BUILD_NUMBER.js"
    fi
  done
fi

# CSS
mkdir -p $STRUCTURIZR_ONPREMISES_DIR/src/main/webapp/static/css
cp -a $STRUCTURIZR_UI_DIR/src/css/* $STRUCTURIZR_ONPREMISES_DIR/src/main/webapp/static/css

# Images
mkdir -p $STRUCTURIZR_ONPREMISES_DIR/src/main/webapp/static/img
cp -a $STRUCTURIZR_UI_DIR/src/img/* $STRUCTURIZR_ONPREMISES_DIR/src/main/webapp/static/img

# Bootstrap icons
mkdir -p $STRUCTURIZR_ONPREMISES_DIR/src/main/webapp/static/bootstrap-icons
cp $STRUCTURIZR_UI_DIR/src/bootstrap-icons/* $STRUCTURIZR_ONPREMISES_DIR/src/main/webapp/static/bootstrap-icons

# HTML (offline exports)
mkdir -p $STRUCTURIZR_ONPREMISES_DIR/src/main/webapp/static/html
cp $STRUCTURIZR_UI_DIR/src/html/* $STRUCTURIZR_ONPREMISES_DIR/src/main/webapp/static/html

# JSP fragments
cp -a $STRUCTURIZR_UI_DIR/src/fragments/* $STRUCTURIZR_ONPREMISES_DIR/src/main/webapp/WEB-INF/fragments

# JSP
cp $STRUCTURIZR_UI_DIR/src/jsp/* $STRUCTURIZR_ONPREMISES_DIR/src/main/webapp/WEB-INF/views

# Java
mkdir -p $STRUCTURIZR_ONPREMISES_DIR/src/main/java/com/structurizr/util/
cp $STRUCTURIZR_UI_DIR/src/java/com/structurizr/util/DslTemplate.java $STRUCTURIZR_ONPREMISES_DIR/src/main/java/com/structurizr/util/