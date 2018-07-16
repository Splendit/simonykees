#!/bin/bash

# command to tag the builds
# returns exit code 128 with error if tag already exists
# first parameter is required and has to be develop or master and results in the directory of the tag
# second parameter is optional and allows to set a subdirector for the tag

# prefix definition for develop, master and master-jmp
if [ "$1" == "develop" ] || [ "$1" == "master" ] || [ "$1" == "master-jmp" ]; then dir=$1/; fi

if [ -z ${dir+x} ]; then echo "Executed with wrong argument '$1' or argument is unset"; exit 1; fi

# second parameter is the sub directory
if [ -n ${2+x} ]; then subdir="$2"/; fi

# building the tag
tag=`pcregrep -o1 "name='eu\.jsparrow\.feature\.feature\.group' range='\[.*,(.*-\d{4})" releng/site/target/p2content.xml`
echo tag=$tag
suffix=`pcregrep -o1 "name='(\w+\.\w+\.\w+)\.feature\.group' range='\[.*,(.*-\d{4})" releng/site/target/p2content.xml`
echo suffix=$suffix
completeTag=$dir$subdir$tag-$suffix
echo completeTag=$completeTag

# tag the build
git tag $completeTag

# push the tag to the remote
git push origin $completeTag 
