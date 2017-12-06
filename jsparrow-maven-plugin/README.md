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
* if some "required items could not be found", have a look [here](http://download.eclipse.org/eclipse/downloads/drops4/R-4.7.1a-201710090410/buildlogs/reporeports/reports/featureNames.html) and if the plugin is listed, install it in Eclipse from [this update site](http://download.eclipse.org/eclipse/updates/4.7/R-4.7.1a-201710090410/)

## configure jsparrow-maven-plugin
* create directory `resources` in `src/main`
* in `src/main/resources` create file named `manifest.standalone`
* change to the destination directory specified in eclipse product export wizard
* change to subdirectory `repository/plugins`
* copy all plugins to `jsparrow-maven-plugin/src/main/resources`
* copy all plugin names to `manifest.standalone`
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

# Use jSparrow Maven Plugin

See Confluence: <https://confluence.splendit.loc/display/SIM/jSparrow+Standalone>
