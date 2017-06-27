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
    
## additional information how to trigger special profiles ##

Our profiles production and proguard are activated with a maven property (-Dproguard, -Dproduction), because we need other profiles to get deactivated on this step and this is solved with an profile activator that triggers on the absence of a property.  

	Example:
	<profile>
		<id>test</id>
		<activation>
			<property>
				<name>!production</name>
			</property>
		</activation>
		<!-- actions of the profile -->
	</profile>

## proguard build ##

	mvn -Dproguard clean verify

This command produces a *.zip artifact in ./site/target that contains obfuscated eclipse-site artifact
The profile is executed without tests by default, because they are not working with obfuscation.
	
**NOTE:** Don't use -Pproguard, because only with -D the tycho-source-plugin is deactivated (resulting in duplicated dependencies within the classpath)
	
**NOTE:** We use -D to define a system property (proguard) but we do not assign a value to that property. 
	The property is simply used because it is the easiest way to activate/deactivate the proguard and no-proguard profiles respectively. 
	
## manual packagedrone deployment ##

To manually deploy to our test deployment channel on packagedrone, execute the following command:
	
	mvn clean deploy -DaltDeploymentRepository=pdrone.0c59cce1-cb2c-4942-8e44-465609fe2fdb::default::http://packagedrone-vm-01.splendit.loc:8080/maven/0c59cce1-cb2c-4942-8e44-465609fe2fdb
	
**NOTE:** make sure the configuration is added to settings.xml (a configured version can be downloaded at https://confluence.splendit.loc/display/Tutorials/DEV+PC):
	
	<server>
		<id>pdrone.0c59cce1-cb2c-4942-8e44-465609fe2fdb</id>
		<username>deploy</username>
		<password>c8afacb01b3ce8171db24192e31b13eb60110a59b9b3f989cf9a6b3815aac496</password>
	</server>
	
The deployed artifact can be seen at: http://packagedrone-vm-01.splendit.loc:8080/channel/0c59cce1-cb2c-4942-8e44-465609fe2fdb/view
	
The channel can be used as update site in Eclipse with the following URL:
	
	http://packagedrone-vm-01.splendit.loc:8080/p2/jSparrow-test-channel/

## faster builds ##

To avoid fetching indexes, the --offline flag can be used (avoids checking timestamps for cached p2 artifacts and metadata files):
	
	mvn clean verify -o
	
To just clean the project, tycho can be deactivated (this results in in an almost instantaneous clean):
	
	mvn clean -Dtycho.mode=maven
	
## Building versions with Test or Productive license attributes ##

Since Netlicensing distinguishes between test and productive versions of the plugin, based on the different Product Number and Product Module Numbers, a different version of the plugin has to be built for the test and productive environments respectively. This is done by using the Maven profiles **test** and **production**. E.g. for a test deployment we use

	mvn -ptest clean verify
	
or simply
 
	mvn clean verify

as _test_ is the default profile. A productive version can be built with
	
	mvn -pproduction clean verify

**Note:** Internally, a productive deployment requires also a system property **production** to be set. This is done during the build when the **production** profile is used. If only the property is set and no profile is used, the Build will not be in a consistent state!

This usage of profiles would result in
	
1. The class containing the relevant information for the Test or Production environment, **LicenseProperties** , to be copied into the project. Previous information will be overwritten during this step.
2. A flag file, **prod-license.properties** or **test-license.properties** being copied into the _target/classes_ directory of the  _license.netlicensing_ module. This file will be subsequently packaged in the JAR file and will serve to help immediately identify the version of the build.
3. In the case of the profile production, a system property with the name **production** to be additionally set. 
	 
**Notes:**
- Copying of the class file is done in the POM of the **license.netlicensing module**.
- Default profile is **test**
- Creating a test or production version has no bearing on obfuscation! 