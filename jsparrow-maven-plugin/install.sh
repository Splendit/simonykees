#!/bin/bash

readonly TMP_DIR="/tmp/jmpInstall"
readonly TMP_ZIP="${TMP_DIR}/jsparrow-maven-plugin.tar.gz"

# Parameters
VERBOSE=0
VERSION="0.5.0-SNAPSHOT"
URL=
ZIP_FILE=
WORK_DIR=$(pwd)


main(){
  # We are doing fancy things with verbose mode
  setup_output

  # getopt goes here, later maybe
  parse_arguments "$@"

  # Check if the neccessary tools are installed
  check_preconditions
  # clean up if the script fails for some reason
  #trap clean INT TERM EXIT
  
  printf "Starting jSparrow Maven Plugin installation...\n"

  # create temporary directory
  setup_tmp_dir
  
  # Download tar file
  if [[ -n ${ZIP_FILE} ]]; then
    move_zip
  else
    download
  fi

  # unzip archive
  unzip
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
  while getopts ":hlv:u:z:" o; do
    case "${o}" in
      l)
        VERBOSE=1
        ;;
      u)
        URL=${OPTARG}
        ;;
      z)
        ZIP_FILE=${OPTARG}
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
  if [[ -n ${URL} ]] && [[ -n ${ZIP_FILE} ]]; then
     printf "Invalid argument. You must not specify both download link and zip file.\nRun './install -h' for usage.\n"
    exit 1
  fi
  
  # Must specify at least one
  if [[ -z ${URL} ]] && [[ -z ${ZIP_FILE} ]]; then
    printf "Invalid argument. You must specify either a download link or a zip file.\nRun './install -h' for usage.\n"
    exit 1
  fi
  
}

function move_zip() {
  printf "\nMoving archive to temporary folder...\n"
  if [[ ! -f "${ZIP_FILE}" ]]; then
    printf "The specified file ${ZIP_FILE} does not exist. Aborting.\n"
    exit 1;
  else
    cp ${ZIP_FILE} ${TMP_ZIP}
  fi
}

function download() {
  # Download fresh maven plugin if it doesnt exist

  if  [[ ! -f "${zipPath}" ]]; then
    printf "\nDownloading jSparrow Maven Plugin archive to temporary directory...\n"
    wget $( (( VERBOSE == 0 )) && printf %s "-q" ) \
      --show-progress \
      "${URL}" \
      -O "${TMP_ZIP}"
    rc=$?; if [[ $rc != 0 ]]; then echo "Failed to download achive from ${URL}, aborting"; exit $rc; fi
  else
    printf "\nExisting archive file found, skipping download.\n"
  fi
}

function unzip(){
  printf "\nUnpacking jSparrow Maven Plugin from achive...\n"
  
  tar $( (( VERBOSE == 1 )) && printf %s "-vzxf" ) \
    $( (( VERBOSE == 0 )) && printf %s "-zxf" ) \
    ${TMP_ZIP} \
    -C ${TMP_DIR}
  rc=$?; if [[ $rc != 0 ]]; then echo "Failed to unpack archive, exiting";  exit $rc; fi
  
}

function install(){
  printf "\nInstalling jSparrow Maven Plugin...\n"
  local jarFile="${TMP_DIR}/jsparrow-maven-plugin-${VERSION}.jar"
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

Copy the following snippet into your projects pom.xml:

<plugin>
    <groupId>eu.jsparrow</groupId>
    <artifactId>jsparrow-maven-plugin</artifactId>
    <version>${VERSION}</version>
</plugin>

For more information visit <link here>
EOF
}

function clean(){
  rm -rf ${TMP_DIR}
}

function usage() {
  cat <<EOF

Usage: ./install [OPTIONS] 

Install the jSparrow Maven Plugin from the distribution archive.

Options: 
  -l        Enable verbose mode
  -z file   Specify a local file as distribution archive. Mutually exclusive with -u. 
  -u url    Specify an url to download the distribution archive from. Mutally exclusive with -z.
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
