# simonykees #

## build ##

    mvn clean verify -fae

## release ##

    mvn org.eclipse.tycho:tycho-versions-plugin:0.26.0:set-version -DnewVersion=<newVersion>
