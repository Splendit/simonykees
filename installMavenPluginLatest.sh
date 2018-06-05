#!/bin/bash

MAVEN_PLUGIN_VERSION="0.5.0-SNAPSHOT"

function usage() {
   cat <<EOF
Usage: $0 (-u|-j)
where:
    -j install the maven plugin that is already locally built
    -u install the latest develop version from packagedrone
EOF
   exit 0
}

function installUrl() {
  # get the latest url and use it (since wget creates tmp files, we switch directory)
  local url=`(cd /tmp \
            && wget --spider -r --no-parent "http://packagedrone-vm-01.splendit.loc:8080/maven/jenkins-jSparrow-maven-plugin-develop/eu/jsparrow/jsparrow-maven-plugin/$MAVEN_PLUGIN_VERSION/") 2>&1 \
            | egrep ".jar$" \
            | grep -v SNAPSHOT.jar \
            | sed 's/--.*--  //' \
            | sort \
            | tail -1`

  jsparrow-maven-plugin/install.sh -u $url
}

function installJar() {
  jsparrow-maven-plugin/install.sh -j "jsparrow-maven-plugin/target/jsparrow-maven-plugin-$MAVEN_PLUGIN_VERSION.jar"
}

# parse arguments
while getopts ":ujh" option
do
  case "${option}" in
    u) installUrl;;
    j) installJar;;
    h) usage;;
  esac
done

if [[ -z "$1" ]] 
  then 
  usage
fi
