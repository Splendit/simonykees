#!/bin/bash

readonly TMP_DIR="/tmp/jmpInstall"
readonly TMP_JAR="${TMP_DIR}/jsparrow-maven-plugin.jar"

# Parameters
VERBOSE=0
VERSION="0.5.0-SNAPSHOT"
URL=
JAR_FILE=
WORK_DIR=$(pwd)


main(){
  parse_arguments "$@"

  printf "Starting jSparrow Maven Plugin installation...\n"
  
  # Enable logging 
  setup_output

  # Check if the neccessary tools are installed
  check_preconditions
  # Clean up if the script fails for some reason
  trap clean INT TERM EXIT

  # create temporary directory
  setup_tmp_dir

  if [[ -n ${JAR_FILE} ]]; then
    move_jar
  else
    download
  fi

  # unzip archive
  extract_pom
  # install from archive
  install 

  print_success 
}

function check_preconditions(){
  if ! [ -x "$(command -v mvn)" ]; then
    printf "Maven is not installed, exiting." 
    exit 1
  fi
}

function setup_tmp_dir(){
  if [[ ! -e ${TMP_DIR} ]]; then
    log "\nSetting up temporary working directory in ${TMP_DIR}.\n"
    mkdir -p ${TMP_DIR}
  else 
    log "\nDirectory '${TMP_DIR}' already exists.\n"
  fi
}

function parse_arguments(){
  while getopts ":hlv:j:u:" o; do
    case "${o}" in
      l)
        VERBOSE=1
        ;;
      u)
        URL=${OPTARG}
        ;;
      j)
        JAR_FILE=${OPTARG}
        ;;
      v)
        VERSION=${OPTARG}
        ;;
      h)
        usage; exit 1
        ;;
      \?)
        echo "Invalid option: -$OPTARG" >&2
        exit 1
        ;;
      :)
        echo "Option -$OPTARG requires an argument." >&2
        exit 1
        ;;
    esac
  done
  shift $((OPTIND-1))

  # Url and Zip are mutually exclusive
  if [[ -n ${URL} ]] && [[ -n ${JAR_FILE} ]]; then
     printf "Invalid argument. You must not specify both download link and jar file.\nRun './install -h' for usage.\n"
    exit 1
  fi
  
  # Must specify at least one
  if [[ -z ${URL} ]] && [[ -z ${JAR_FILE} ]]; then
    printf "Invalid argument. You must specify either a download link or a jar file.\nRun './install -h' for usage.\n"
    exit 1
  fi
  
}

function move_jar() {
  printf "\nMoving archive to temporary folder...\n"
  if [[ ! -f "${JAR_FILE}" ]]; then
    printf "The specified file ${JAR_FILE} does not exist. Aborting.\n"
    exit 1;
  else
    cp ${JAR_FILE} ${TMP_JAR}
  fi
}

function download() {
  # Download fresh maven plugin if it doesnt exist
  if  [[ ! -f "${zipPath}" ]]; then
    printf "\nDownloading jSparrow Maven Plugin archive to temporary directory...\n"
    wget $( (( VERBOSE == 0 )) && printf %s "-q" ) \
      --show-progress \
      "${URL}" \
      -O "${TMP_JAR}"
    rc=$?; if [[ $rc != 0 ]]; then echo "Failed to download achive from ${URL}, aborting"; exit $rc; fi
  else
    printf "\nExisting archive file found, skipping download.\n"
  fi
}

function extract_pom() {
  printf "\nExtracting pom.xml...\n"
  local pomFile="META-INF/maven/eu.jsparrow/jsparrow-maven-plugin/pom.xml"
  cd ${TMP_DIR}
  jar \
    $( (( VERBOSE == 1 )) && printf %s "-vxf" ) \
    $( (( VERBOSE == 0 )) && printf %s "-xf" ) \
    ${TMP_JAR}
  rc=$?; if [[ $rc != 0 ]]; then echo "Failed to extract ${TMP_JAR}, aborting"; exit $rc; fi
 
  mv "${pomFile}" "pom.xml"
  rc=$?; if [[ $rc != 0 ]]; then echo "Failed to find pom.xml, aborting"; exit $rc; fi

  cd "${HOME_DIR}"
}


function install(){
  printf "\nInstalling jSparrow Maven Plugin...\n"
  local jarFile="${TMP_DIR}/jsparrow-maven-plugin.jar"
  local pomFile="${TMP_DIR}/pom.xml"
  mvn install:install-file \
    $( (( VERBOSE == 0 )) && printf %s "-q" )  \
    -Dfile=${jarFile} \
    -DpomFile=${pomFile}
  rc=$?; if [[ $rc != 0 ]]; then echo "Maven failed with exit code ${rc}, aborting";  exit $rc; fi
}

function print_success() {
  cat <<EOF

The jSparrow Maven Plugin was installed successfully!

Copy the following snippet into your project's pom.xml:

<plugin>
    <groupId>eu.jsparrow</groupId>
    <artifactId>jsparrow-maven-plugin</artifactId>
    <version>${VERSION}</version>
</plugin>

For more information visit <link here>
EOF
}

function clean(){
  log "\nCleanup, removing temporary directory\n"
  rm -rf ${TMP_DIR}
}

function usage() {
  cat <<EOF

Usage: ./install [OPTIONS] 

Install the jSparrow Maven Plugin from the distribution archive.

Options: 
  -l        Enable verbose mode
  -j file   Specify a local jar file to install. Mutually exclusive with -u. 
  -u url    Specify an url to download the distribution archive from. Mutally exclusive with -j.
  -v ver    Specify the version of the jSparrow Maven Plugin that you want to install. 
  -h        Display usage information. 
EOF
}

function setup_output () {
  # Pretty much from here:
  # https://serverfault.com/questions/414810/sh-conditional-redirectio://serverfault.com/questions/414810/sh-conditional-redirection
  exec 6>/dev/null
  if [[ $VERBOSE -eq 1 ]]; then
    exec 6>&1
  fi
}

function log () {
  printf "$@" >&6 2>&1
}

main "$@"
