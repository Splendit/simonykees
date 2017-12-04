# Configure and Build jSparrow Maven Plugin

## build simonykees

```bash
$ mvn clean verify
```

## export product from eu.jsparrow.releng project
* open `tools/eclipse/jsparrow.product`
* run the Eclipse Product Export Wizard
* uncheck "Synchronize before exporting"
* select a destination directory
* click "Finish"

## configure jsparrow-maven-plugin
* create directory `resources` in `src/main`
* in `src/main/resources` create file named `manifest.standalone`
* change to the destination directory specified in eclipse product export wizard
* change to subdirectory `repository/plugins`
* copy all plugins to `jsparrow-maven-plugin/src/main/resources`
* copy all plguin names to `manifest.standalone`
* download apache maven binaries as zip-file from: <https://maven.apache.org/download.cgi>
* copy the downloaded zip file to `jsparrow-maven-plugin/src/main/resources`

## build jsparrow-maven-plugin
run the following command from the jsparrow-maven-plugin root directory:

```bash
$ mvn clean verify
```

# Install jSparrow Maven Plugin

## install jsparrow-maven-plugin from source
run the following command from the jsparrow-maven-plugin root directory:

```bash
$ mvn clean install
```

## install jsparrow-maven-plugin from jar file / deliverable

* Two files are needed for the installation:
    * `jsparrow-maven-plugin-<VERSION>.jar` (in jsparrow-maven-plugin/target)
    * `pom.xml` (in the root of jsparrow-maven-plugin)
* To install jsparrow-maven-plugin in the local maven repository use the following command:

```bash
$ mvn install:install-file -Dfile=jsparrow-maven-plugin-<VERSION>.jar -DpomFile=pom.xml
```

# Use jSparrow Maven Plugin

## configure project to use jsparrow-maven-plugin
To use jsparrow-maven-plugin on a project, add the following code snippet to the project's pom.xml (in `build/plugins`):

```xml
<plugin>
	<groupId>eu.jsparrow</groupId>
	<artifactId>jsparrow-maven-plugin</artifactId>
	<version><VERSION></version>
</plugin>
```

## jsparrow-maven-plugin configuration file (YAML)

The jsparrow maven configuration file is a YAML file with extensions `*.yml` or `*.yaml`.
The default location is a file in the project's root directory called `jsparrow.yml`.
It has the following form:

```
jsparrow.config

# specify one of the profiles declared below as the selected profile.
# if the selectedProfile is not specified the rules in the “rules:” section will be applied
selectedProfile: profile1

# define profiles here
profiles:
  - name: profile1
    rules:
      - TryWithResource
      - MultiCatch
      - ...
  - name: ...
    rules:
      - ...

# rules in this section will be executed, if no profile has been specified as selectedProfile or via maven.
# to deactivate rules, they could be commented with the #-sign
rules:
  - TryWithResource
  - MultiCatch
  - ...
```

The rule IDs can be taken from the `list-rules-short`-goal of the jsparrow-maven-plugin.

## execute jsparrow-maven-plugin
run jsparrow-maven-plugin with the following command and use goals described below:

```bash
$ mvn jsparrow:<goal>
```

## jsparrow-maven-plugin goals

### refactor
This goal starts the refactoring process. 

```bash
$ mvn jsparrow:refactor [[[-DconfigFile=<config-file-path>] [-Dprofile=<selected-profile-id>]] | -DdefaultConfiguration]
```

Parameters:
    
* `no parameters`: A configuration file with the name "jsparrow.yml" has to be present in the root directory of the project. This file will be used as the jsparrow configuration.
* `-DconfigFile=<config-file-path>`: The specified configuration file will be used.
* `-Dprofile=<selected-profile-id>`: The specified profile will be used. Make sure that either a jsparrow.yml is in the root directory or `-DconfigFile=<config-file-path>` is specified. The given profile ID will be compared to the declared profiles in the configuration file. If the given profile ID is not declared, an error will be thrown.
* `-DdefaultConfiguration`: The built-in default configuration will be used for refactoring. If this parameter is set, all others will be ignored.

### list-rules
This goal lists rules with their IDs, names and descriptions.

```bash
$ mvn jsparrow:list-rules [-Drules=<list-of-rule-ids>}
```

Parameters:

* `no parameters`: all rules will be listed.
* `-Drules=<list-of-rule-ids>`: The rules with the given IDs will be listed. `list-of-rule-ids` is a comma-separated (`,`) list of rule ids.

### list-rules-short
This goal lists all rules in a table with ID and name

```bash
$ mvn jsparrow:list-rules-short
```
