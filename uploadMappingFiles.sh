#!/bin/sh
#author: Matthias Webhofer, Hans-Jörg Schrödl

exitPrintUsage() {
	echo "Usage: $0 <originDirectory> <targetDirectory>"
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
	echo "Error uploading mapping files!"
	exit 4
}

# check argument count
if [ "$#" -ne "2" ]; then
	exitPrintUsage
fi

origin_dir=$1
target_dir=$2

# check if the directory, in which all build numbers are stored
# (as directories themselves), exists. exit on failure 
if [ -d "$origin_dir" ]; then
	
	# create a temporary directory, if it doesn't exist yet
	if [ ! -d "$target_dir" ]; then
		mkdir "$target_dir"
		if [ $? -gt 0 ]; then
			exitDirectoryCreationError
		fi
	fi

	# first: copy all files to temp directory
	# find: find all files ending with *.out recursively from the given directory
	# xargs: takes the stdout of find and applies cp to each entry of the list
	# {} is the placeholder for the list items
	find "$origin_dir" -name "*.out" | xargs -i cp {} "$target_dir"
	
	# then: zip and upload to deobfuscation service
	zip -r "$target_dir".zip "$target_dir"
	curl -F file=@"$target_dir".zip http://172.16.0.6:8080/upload

	if [ $? -gt 0 ]; then
		exitCopyError
	fi
	
	# remove tempdir and zipfile
	rm -rf "$target_dir" "$target_dir.zip"

	echo "Mapping files uploaded successfully"
else
	exitDirectoryNotExisting
fi

