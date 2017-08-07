#!/bin/sh
#author: Matthias Webhofer

exitPrintUsage() {
	echo "Usage: $0 <buildNumber> <buildDirectory> <mappingFilesDirectory>"
	exit 1
}

exitDirectoryNotExisting() {
	echo "The Directory $2 doesn't exist!"
	exit 2
}

exitDirectoryCreationError() {
	echo "The directory named \"$1\" could not be created!"
	exit 3 
}

exitCopyError() {
	echo "Error copying mapping files!"
	exit 4
}

# check argument count
if [ "$#" -ne "3" ]; then
	exitPrintUsage
fi

build_directory=$2

# remove last '/' of the path, if there is any
build_number_dir=${3%/}

# check if the directory, in which all build numbers are stored
# (as directories themselves), exists. exit on failure
if [ -d "$build_number_dir" ] && [ -d "$build_directory" ]; then
	
	mapping_files_dir="${build_number_dir}/$1/"
	
	# create the mapping files directory, if it doesn't exist yet
	if [ ! -d "$mapping_files_dir" ]; then
		mkdir "$mapping_files_dir"
		if [ $? -gt 0 ]; then
			exitDirectoryCreationError
		fi
	fi

	# find: find all files ending with *.out recursively from the given directory
	# xargs: takes the stdout of find and applies cp to each entry of the list
	# {} is the placeholder for the list items
	find "$build_directory" -name "*.out" | xargs -i cp {} "$mapping_files_dir"

	if [ $? -gt 0 ]; then
		exitCopyError
	fi

	echo "mapping files copied successfully from \"$build_directory\" to \"$mapping_files_dir\""
else
	exitDirectoryNotExisting
fi

