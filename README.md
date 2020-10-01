# simonykees #

## steps after importing the project in eclipse ##

Build the project with
 
	mvn clean validate

Execute
 
	Maven > Update Project...
	
## missing dependencies in eclipse ##

Read instructions at: [Eclipse](https://confluence.splendit.loc/display/Tutorials/Setup+Eclipse+for+jSparrow)

## build ##

    mvn clean verify -fae

## version update ##

    mvn org.eclipse.tycho:tycho-versions-plugin:1.0.0:set-version -DnewVersion=<newVersion>
    
Since not everything can be done by tycho yet (i.e., releng and the maven-plugin) search for any occurrences of the old version like that:

	# example when upgrading to 2.4.0 and searching for references to 2.3.0:
	egrep -ir --exclude-dir={.git,target} "2\.3\.0[-\.](SNAPSHOT|qualifier)" ./
    
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

Please read instructions at: [Package Drone and manual deployment](https://confluence.splendit.loc/display/SIM/Package+Drone+and+manual+deployment)

## faster builds ##

To avoid fetching indexes, the --offline (-o) flag can be used (avoids checking timestamps for cached p2 artifacts and metadata files):
	
	mvn clean verify -o
	
To just clean the project, tycho can be deactivated (this results in in an almost instantaneous clean):
	
	mvn clean -Dtycho.mode=maven
	
## Building versions with Test or Productive license attributes ##

Since Netlicensing distinguishes between test and productive versions of the plugin, based on the different Product Number and Product Module Numbers, a different version of the plugin has to be built for the test and productive environments respectively. This is done by using the Maven profiles **test** and **production**. E.g. for a test deployment we use
 
	mvn clean verify

as _test_ is the default profile. A productive version can be built with
	
	mvn -Dproduction clean verify
	
**Note:** We always want to activate profiles with a property ("-D") because that way we can use profile activation and deactivation (search for "!production" or "!proguard" to get the picture). 

**Note:** Internally, a productive deployment requires also a system property **production** to be set. This is done during the build when the **production** profile is used. If only the property is set and no profile is used, the Build will not be in a consistent state!

This usage of profiles would result in
	
1. The class containing the relevant information for the Test or Production environment, **LicenseProperties**, to be copied into the project. Previous information will be overwritten during this step.
2. A flag file, **prod-license.properties** or **test-license.properties** being copied into the _target/classes_ directory of the  _license.netlicensing_ module. This file will be subsequently packaged in the JAR file and will serve to help immediately identify the version of the build.
3. In the case of the profile production, a system property with the name **production** to be additionally set. 
	 
**Notes:**
- Copying of the class file is done in the POM of the **license.netlicensing module**.
- Default profile is **test**
- Creating a test or production version has no bearing on obfuscation! 

## Statistics

to save the statistics to a .json file, please specify the destination path as the following Java system property `-Deu.jsparrow.statistics.save.path`. This can be done in `eclipse.ini` by adding this property after the `-vmargs` argument. For testing (runtime-eclipse applicaton), the java system properties can be configured in the run/debug configurations.
