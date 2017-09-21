#!/bin/bash

# command to tag the development build
# returns exit code 128 with error if tag already excists
# first paramter is required an has to be develop or master and results in the directory of the tag
# second parameter is optinal and allows to set a subdirector for the tag

# prefix definition for develop
if [ "$1" == "develop" ]; then dir=$1/; fi

# prefix definition for master
if [ "$1" == "master" ]; then dir=$1/; fi

if [ -z ${dir+x} ]; then echo "Exectued with wrong argument '$1' or argument is unset"; exit 1; fi

# second parameter is the sub dircetory
if [ -n ${2+x} ]; then subdir="$2"/; fi

# building the tag
tag=`pcregrep -o1 "name='eu\.jsparrow\.feature\.feature\.group' range='\[.*,(.*-\d{4})" site/target/p2content.xml`
echo tag=$tag
suffix=`pcregrep -o1 "name='(\w+\.\w+\.\w+)\.feature\.group' range='\[.*,(.*-\d{4})" site/target/p2content.xml`
echo suffix=$suffix
completeTag=$dir$subdir$tag-$suffix
echo completeTag=$completeTag

# tag the build
git tag $completeTag

# push the tage to the remote
git push origin $completeTag 
