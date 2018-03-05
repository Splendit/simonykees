# Configure and Build jSparrow Maven Plugin

## use bash-script

Execute the `buildMavenPluginWithoutTests.sh` bash-script in the simonykees root directory.
This will build and install the maven plugin as described below. It won't execute tests on simonykees using the `-DskipTests` flag.

## build simonykees

```bash
$ mvn clean verify
```

## configure jsparrow-maven-plugin
* create directory `resources` in `src/main`
* in `src/main/resources` create file named `manifest.standalone`
* change to the directory: `eu.jsparrow.releng/eu.jsparrow.product/target/repository/plugins`
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
