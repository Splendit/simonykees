#!/bin/bash

#command to tag the development build
# returns exit code 128 with error if tag already excists

#prefix definition for develop
if [ "$1" == "develop" ]; then prefix=dev/main/; fi

#prefix definition for master
if [ "$1" == "master" ]; then prefix=release/; fi

if [ -z ${prefix+x} ]; then echo "Exectued with wrong argument '$1' or argument is unset"; exit 1; fi


tag=`pcregrep -o1 "name='jSparrow\.feature\.feature\.group' range='\[(\d.\d.\d.\d{8}-\d{4})," site/target/p2content.xml`
suffix=`pcregrep -o1 "name='(\w+\.\w+)\.feature\.group' range='\[(\d.\d.\d.\d{8}-\d{4})," site/target/p2content.xml`

#echo $suffix

completeTag=$prefix$tag-$suffix

#echo $completeTag

#tag the build
git tag $completeTag

#push the tage to the remote
git push origin $completeTag 
