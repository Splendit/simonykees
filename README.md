# simonykees #

# --tmp-test-deployment-- #

## momentary not working profiles with credentials need be added to pom! ##
    add to maven settings.xml the deployment credentials (/usr/share/maven/settings.xml)

	<server>
		<id>pdrone.b8eed447-5f46-42c2-bfc1-1b9b174069e5</id>
		<username>deploy</username>
		<password>c8afacb01b3ce8171db24192e31b13eb60110a59b9b3f989cf9a6b3815aac496</password>
	</server>

	deploy with: mvn clean deploy
	deploy with proguard: mvn clean deploy -Dproguard
	
	Don't use -Pproguard, because with -D the tycho-source-plugin is deactivated
	(resulted in double dependencies within the classpath)

	updated osgi bundle will be visible on:
	http://packagedrone-vm-01.splendit.loc:8080/p2/jSparrow-test-channel/

## build ##

    mvn clean verify -fae

## release ##

    mvn org.eclipse.tycho:tycho-versions-plugin:0.26.0:set-version -DnewVersion=<newVersion>

## proguard build ##

	mvn -Pproguard clean verify

	This command produces a *.zip artifact in ./site/target that contains obfuscated eclipse-site artifact
	The profile is executed without tests by default, because they are not working with obfuscation.

## steps after importing the project in eclipse ##

	Build the project with 
	mvn clean validate

	Execute 
	Maven > Update Project...

## faster builds ##

	To avoid fetching indexes, the --offline flag can be used (avoids checking timestamps for cached p2 artifacts and metadata files):
	
	mvn clean verify -o
	
	To just clean the project, tycho can be deactivated:
	
	mvn clean -Dtycho.mode=maven