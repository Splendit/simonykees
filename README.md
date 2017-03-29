# simonykees #

## build ##

    mvn clean verify -fae

## release ##

    mvn org.eclipse.tycho:tycho-versions-plugin:0.26.0:set-version -DnewVersion=<newVersion>

## proguard build ##

		mvn -Pproguard clean verify

		This command produces a *.zip artifact in ./site/target that contains obfuscated eclipse-site artifact
		The profile is executed without tests by default, because they are not working with obfuscation.
