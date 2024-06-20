# - this is a temporary script that merges the contents of the structurizr/ui repository into this directory,
# - it will likely be migrated in the Gradle build file at some point in the future
# - this has only been tested on MacOS

export STRUCTURIZR_UI_DIR=../structurizr-ui
export STRUCTURIZR_ONPREMISES_DIR=./structurizr-onpremises

mkdir $STRUCTURIZR_ONPREMISES_DIR/src/main/webapp/static

cp -u -r $STRUCTURIZR_UI_DIR/src/js $STRUCTURIZR_ONPREMISES_DIR/src/main/webapp/static
cp -u -r $STRUCTURIZR_UI_DIR/src/css $STRUCTURIZR_ONPREMISES_DIR/src/main/webapp/static
cp -u -r $STRUCTURIZR_UI_DIR/src/img $STRUCTURIZR_ONPREMISES_DIR/src/main/webapp/static
cp -u -r $STRUCTURIZR_UI_DIR/src/bootstrap-icons $STRUCTURIZR_ONPREMISES_DIR/src/main/webapp/static
cp -u -r $STRUCTURIZR_UI_DIR/src/html $STRUCTURIZR_ONPREMISES_DIR/src/main/webapp/static

# JSP fragments
cp -u -r $STRUCTURIZR_UI_DIR/src/fragments/* $STRUCTURIZR_ONPREMISES_DIR/src/main/webapp/WEB-INF/fragments

# JSP
cp -u -r $STRUCTURIZR_UI_DIR/src/jsp/* $STRUCTURIZR_ONPREMISES_DIR/src/main/webapp/WEB-INF/views

