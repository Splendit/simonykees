#!/bin/bash
## bash script for building the jsparrow-maven-plugins

function printUsage {
  echo -e "\tUsage:"
  echo -e "\t\t$0 [-t][-p]"
  echo -e "\n\tParameters:"
  echo -e "\t\t-t\t\trun tests while building"
  echo -e "\t\t-p\t\tenable proguard obfuscated build"
  echo -e "\t\t-p\t\tuse productive values for netlicensing"
}

TEST=false
OBFUSCATION=false
PRODUCTION=false 

# parse arguments
while getopts ":top" option
do
  case "${option}" in
    t) TEST=true;;
    o) OBFUSCATION=true;;
    p) PRODUCTION=true;;
    ?) printUsage;;
  esac
done

JSPARROW_TARGET_PATH="releng/eu.jsparrow.product/target/repository/plugins"
PLUGIN_RESOURCES_PATH="jsparrow-maven-plugin/src/main/resources"
MANIFEST_FILE_NAME="manifest.standalone"

echo "Building jSparrow"

PARAMETERS=

if [ $TEST = false ]; then
  PARAMETERS="$PARAMETERS -DskipTests"
fi

if [ $OBFUSCATION = true ]; then
  PARAMETERS="$PARAMETERS -Dproguard"
fi

if [ $PRODUCTION = true ]; then
  PARAMETERS="$PARAMETERS -Dproduction"
fi

# build jsparrow
mvn clean verify $PARAMETERS

# check maven result and exit if necessary
if [ $? -ne 0 ]; then
  echo "maven on jsparrow failed!"
  exit 1
fi

# create jsparrow maven plugin resource directory if it doesn't exist
if [ ! -d $PLUGIN_RESOURCES_PATH ]; then
    mkdir -p $PLUGIN_RESOURCES_PATH
    if [ $? -ne 0 ]; then
      echo "mkdir failed for $PLUGIN_RESOURCES_PATH!"
      exit 2
    fi
else
    rm -rf $PLUGIN_RESOURCES_PATH/*
    if [ $? -ne 0 ]; then
      echo "error removing files from $PLUGIN_RESOURCES_PATH"
      exit 6
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

mvn clean install $PARAMETERS

if [ $? -ne 0 ]; then
  echo "maven on jSparrow maven plugin failed!"
  exit 4
fi

echo
echo "Plugin dependency to copy:"

# get the artifact version of the jsparrow-maven-plugin
MVN_VERSION=$(mvn -q -Dexec.executable="echo" -Dexec.args='${project.version}' --non-recursive org.codehaus.mojo:exec-maven-plugin:1.3.1:exec)

cat <<EOF
<plugin>
    <groupId>eu.jsparrow</groupId>
    <artifactId>jsparrow-maven-plugin</artifactId>
    <version>${MVN_VERSION}</version>
</plugin>
EOF

echo
echo "Execution example:"
echo "mvn jsparrow:refactor -DdefaultConfiguration"
