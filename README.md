# simonykees #

## steps after importing the project in eclipse ##

	Build the project with 
	mvn clean validate

	Execute 
	Maven > Update Project...

## build ##

    mvn clean verify -fae

## release ##

    mvn org.eclipse.tycho:tycho-versions-plugin:0.26.0:set-version -DnewVersion=<newVersion>

## proguard build ##

	mvn -Dproguard clean verify

	This command produces a *.zip artifact in ./site/target that contains obfuscated eclipse-site artifact
	The profile is executed without tests by default, because they are not working with obfuscation.
	
	NOTE: Don't use -Pproguard, because only with -D the tycho-source-plugin is deactivated (resulting in duplicated dependencies within the classpath)
	
	NOTE: We use -D to define a system property (proguard) but we do not assign a value to that property. 
	The property is simply used because it is the easiest way to activate/deactivate the proguard and no-proguard profiles respectively. 
	
## manual packagedrone deployment ##

	To manually deploy to our test deployment channel on packagedrone, execute the following command:
	
	mvn clean deploy -DaltDeploymentRepository=pdrone.0c59cce1-cb2c-4942-8e44-465609fe2fdb::default::http://packagedrone-vm-01.splendit.loc:8080/maven/0c59cce1-cb2c-4942-8e44-465609fe2fdb
	
	NOTE: make sure the configuration is added to settings.xml (a configured version can be downloaded at https://confluence.splendit.loc/display/Tutorials/DEV+PC):
	
	<server>
		<id>pdrone.0c59cce1-cb2c-4942-8e44-465609fe2fdb</id>
		<username>deploy</username>
		<password>c8afacb01b3ce8171db24192e31b13eb60110a59b9b3f989cf9a6b3815aac496</password>
	</server>
	
	The deployed artifact can be seen at: http://packagedrone-vm-01.splendit.loc:8080/channel/0c59cce1-cb2c-4942-8e44-465609fe2fdb/view

## faster builds ##

	To avoid fetching indexes, the --offline flag can be used (avoids checking timestamps for cached p2 artifacts and metadata files):
	
	mvn clean verify -o
	
	To just clean the project, tycho can be deactivated (this results in in an almost instantaneous clean):
	
	mvn clean -Dtycho.mode=maven