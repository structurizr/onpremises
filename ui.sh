# - this is a temporary script that merges the contents of the structurizr/ui repository into this directory,
# - it will likely be migrated in the Gradle build file at some point in the future
# - this has only been tested on MacOS

export STRUCTURIZR_UI_DIR=../structurizr-ui
export STRUCTURIZR_ONPREMISES_DIR=./structurizr-onpremises

mkdir $STRUCTURIZR_ONPREMISES_DIR/src/main/webapp/static

# JavaScript
mkdir $STRUCTURIZR_ONPREMISES_DIR/src/main/webapp/static/js
cp -u -r $STRUCTURIZR_UI_DIR/src/js/* $STRUCTURIZR_ONPREMISES_DIR/src/main/webapp/static/js
mkdir $STRUCTURIZR_ONPREMISES_DIR/src/main/webapp/static/js/ace
cp -u -r $STRUCTURIZR_UI_DIR/src/js/ace/* $STRUCTURIZR_ONPREMISES_DIR/src/main/webapp/static/js/ace

# CSS
mkdir $STRUCTURIZR_ONPREMISES_DIR/src/main/webapp/static/css
cp -u -r $STRUCTURIZR_UI_DIR/src/css/* $STRUCTURIZR_ONPREMISES_DIR/src/main/webapp/static/css

# CSS fonts
mkdir $STRUCTURIZR_ONPREMISES_DIR/src/main/webapp/static/css/fonts
cp -u -r $STRUCTURIZR_UI_DIR/src/css/fonts/* $STRUCTURIZR_ONPREMISES_DIR/src/main/webapp/static/css/fonts

# Images
mkdir $STRUCTURIZR_ONPREMISES_DIR/src/main/webapp/static/img
cp -u -r $STRUCTURIZR_UI_DIR/src/img/* $STRUCTURIZR_ONPREMISES_DIR/src/main/webapp/static/img
mkdir $STRUCTURIZR_ONPREMISES_DIR/src/main/webapp/static/img/review
cp -u -r $STRUCTURIZR_UI_DIR/src/img/review/* $STRUCTURIZR_ONPREMISES_DIR/src/main/webapp/static/img/review

# Bootstrap icons
mkdir $STRUCTURIZR_ONPREMISES_DIR/src/main/webapp/static/bootstrap-icons
cp -u -r $STRUCTURIZR_UI_DIR/src/bootstrap-icons/* $STRUCTURIZR_ONPREMISES_DIR/src/main/webapp/static/bootstrap-icons

# HTML (offline exports)
mkdir $STRUCTURIZR_ONPREMISES_DIR/src/main/webapp/static/html
cp -u -r $STRUCTURIZR_UI_DIR/src/html/* $STRUCTURIZR_ONPREMISES_DIR/src/main/webapp/static/html

# JSP fragments
cp -u -r $STRUCTURIZR_UI_DIR/src/fragments/* $STRUCTURIZR_ONPREMISES_DIR/src/main/webapp/WEB-INF/fragments
mkdir $STRUCTURIZR_ONPREMISES_DIR/src/main/webapp/WEB-INF/fragments/workspace
cp -u -r $STRUCTURIZR_UI_DIR/src/fragments/workspace/* $STRUCTURIZR_ONPREMISES_DIR/src/main/webapp/WEB-INF/fragments/workspace
mkdir $STRUCTURIZR_ONPREMISES_DIR/src/main/webapp/WEB-INF/fragments/diagrams
cp -u -r $STRUCTURIZR_UI_DIR/src/fragments/diagrams/* $STRUCTURIZR_ONPREMISES_DIR/src/main/webapp/WEB-INF/fragments/diagrams
mkdir $STRUCTURIZR_ONPREMISES_DIR/src/main/webapp/WEB-INF/fragments/decisions
cp -u -r $STRUCTURIZR_UI_DIR/src/fragments/decisions/* $STRUCTURIZR_ONPREMISES_DIR/src/main/webapp/WEB-INF/fragments/decisions
mkdir $STRUCTURIZR_ONPREMISES_DIR/src/main/webapp/WEB-INF/fragments/dsl
cp -u -r $STRUCTURIZR_UI_DIR/src/fragments/dsl/* $STRUCTURIZR_ONPREMISES_DIR/src/main/webapp/WEB-INF/fragments/dsl

# JSP
cp -u -r $STRUCTURIZR_UI_DIR/src/jsp/* $STRUCTURIZR_ONPREMISES_DIR/src/main/webapp/WEB-INF/views

