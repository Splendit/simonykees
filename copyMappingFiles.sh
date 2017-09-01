#!/bin/sh
#author: Matthias Webhofer, Hans-Jörg Schrödl

exitPrintUsage() {
	echo "Usage: $0 <originDirectory> <destinationRoot> <buildNumber>"
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

origin_dir=$1

# remove last '/' of the path, if there is any
destination_root_dir=${2%/}
build_nr_dir=$3

# check if the directory, in which all build numbers are stored
# (as directories themselves), exists. exit on failure 
if [ -d "$origin_dir" ]; then
	

	# create a temporary directory, if it doesn't exist yet
	if [ ! -d "$build_nr_dir" ]; then
		mkdir "$build_nr_dir"
		if [ $? -gt 0 ]; then
			exitDirectoryCreationError
		fi
	fi

	# first: copy all files to temp directory
	# find: find all files ending with *.out recursively from the given directory
	# xargs: takes the stdout of find and applies cp to each entry of the list
	# {} is the placeholder for the list items
	find "$origin_dir" -name "*.out" | xargs -i cp {} "$build_nr_dir"
	
	# then: copy all files from temp to remote
	scp -r -i ~/.ssh/slave_rsa "$build_nr_dir" "jenkins.splendit.loc:$destination_root_dir"

	if [ $? -gt 0 ]; then
		exitCopyError
	fi
	
	rm -rf "$mapping_files_dest_dir"

	echo "mapping files copied successfully from \"$origin_dir\" to remote \"$destination_root_dir\""
else
	exitDirectoryNotExisting
fi

