#!/bin/bash

BASE_URL_DEVELOP="http://packagedrone-vm-01.splendit.loc:8080/maven/jenkins-jSparrow-maven-plugin-develop-test-noProguard"
BASE_URL_RELEASE_CANDIDATE="http://packagedrone-vm-01.splendit.loc:8080/maven/jSparrow-maven-plugin-release-candidate"
BASE_URL_MASTER="http://packagedrone-vm-01.splendit.loc:8080/maven/jenkins-jSparrow-maven-plugin-master-production-proguard"

BASE_DIRECTORY_JAR="jsparrow-maven-plugin/target"

function usage() {
   cat <<EOF
Usage: $0 (-j|-d|-r|-m)
where:
    -j install the maven plugin that is already locally built
    -d install the latest develop version from packagedrone
    -r install the latest release candidate version from packagedrone
    -m install the latest master version from packagedrone
EOF
   exit 0
}

function installUrl() {

  local baseUrl=$1

  echo "Searching for the latest version of the JMP at the following url: $baseUrl"

  # get the latest url and use it (since wget creates tmp files, we switch directory)
  local url=`(cd /tmp \
            && wget --spider -r --no-parent "$baseUrl/") 2>&1 \
            | egrep ".jar$" \
            | grep -v SNAPSHOT.jar \
            | sed 's/--.*--  //' \
            | sort \
            | tail -1`

  if [[ -z $url ]] 
    then
    echo "No artifact could be found on the base url. Please check the url."
  else
    printf  "Installing the jSparrow Maven Plugin using the following url: '%s'\n\n" $url

    jsparrow-maven-plugin/install.sh -u $url
  fi

}

function installJar() {
  
  if [[ -d $BASE_DIRECTORY_JAR ]]
    then
    # finds the path of the jar in target, independent of version or whether or not it is a SNAPSHOT build. Ignores proguard_base jars. 
    local jarPath=`find $BASE_DIRECTORY_JAR -regex "$BASE_DIRECTORY_JAR/jsparrow-maven-plugin-[0-9]+\.[0-9]+\.[0-9]+[-]*[A-Z]*\.jar"`
    printf "Installing the jSparrow Maven Plugin using the following local path: '%s'\n\n" $jarPath
    jsparrow-maven-plugin/install.sh -j $jarPath
  else
    echo "Directory does not exist: '$BASE_DIRECTORY_JAR'"
  fi

}

# parse arguments
while getopts ":drmjh" option
do
  case "${option}" in
    d) installUrl $BASE_URL_DEVELOP;;
    r) installUrl $BASE_URL_RELEASE_CANDIDATE;;
    m) installUrl $BASE_URL_MASTER;;
    j) installJar;;
    h) usage;;
  esac
done

if [[ -z "$1" ]] 
  then 
  usage
fi
