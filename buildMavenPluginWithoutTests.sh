#!/bin/bash
##  bash script for building the jsparrow-maven-plugins

JSPARROW_TARGET_PATH="releng/eu.jsparrow.product/target/repository/plugins"
PLUGIN_RESOURCES_PATH="jsparrow-maven-plugin/src/main/resources"
MANIFEST_FILE_NAME="manifest.standalone"

echo "Building jSparrow"

# build jsparrow without tests
mvn clean verify -DskipTests

# check maven result and exit if necessary
if [ $? -ne 0 ]; then
  echo "maven on jsparrow failed!"
  exit 1
fi

# create jsparrow maven plugin resource driectory if it doesn't exist
if [ ! -d $PLUGIN_RESOURCES_PATH ]; then
    mkdir -p $PLUGIN_RESOURCES_PATH
    if [ $? -ne 0 ]; then
      echo "mkdir failed for $PLUGIN_RESOURCES_PATH!"
      exit 2
    fi
fi

echo "Copying Resource Files"

# copy all dependencies form jsparrow into the maven plugin's resources
cp $JSPARROW_TARGET_PATH/* $PLUGIN_RESOURCES_PATH

if [ $? -ne 0 ]; then
  echo "copying files from $JSPARROW_TARGET_PATH to $PLUGIN_RESOURCES_PATH failed!"
  exit 3
fi

echo "Creating $MANIFEST_FILE_NAME"

# list the contents of the build jsparrow plugins and redirect it to the necessary manifest.standalone in the maven plugins
ls $JSPARROW_TARGET_PATH > $PLUGIN_RESOURCES_PATH/$MANIFEST_FILE_NAME

# build and install the jsparrow-maven-plugin
cd jsparrow-maven-plugin

echo "Building jSparrow Maven Plugin"
mvn clean install

if [ $? -ne 0 ]; then
  echo "maven on jSparrow maven plugin failed!"
  exit 4
fi
