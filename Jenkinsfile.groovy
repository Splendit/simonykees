#!/usr/bin/groovy

// if this script is changed expand the documentation on confluence!
// url: https://confluence.splendit.loc/display/SIM/Jenkins+Pipeline+Description

// add timestaps to the "Console Output" of Jenkins

import groovy.transform.Field

/*
 * Should production steps be executed?
 * This changes whether or not deploy steps etc. are executed.
 * This only changes something on non-feature branches.
 *
 * Will be set to true if remote repository is our main repository
 */
@Field boolean isLiveEnvironment = false

void setLiveEnvironment(def url) {
    /*
     * FIXME: ITGlobal we should set the remote url to ssh. The credentials should be corrected accordingly. For now, setting the expected liveUrl to https to avoid MOCK steps. 
     * def liveUrl = "ssh://git@gitlab.splendit.at:10022/legacy-migration/simonykees.git"
     */
    def liveUrl = "https://gitlab.splendit.at/legacy-migration/simonykees.git"
    if (liveUrl == url) {
        println "Live environment is enabled"
        isLiveEnvironment = true
    } else {
        println "Live environment is disabled"
        isLiveEnvironment = false
    }
}

// gets the maven home
def mvnBin() { "${tool 'mvn system'}/bin/mvn" }

// jenkins git ssh credentials
@Field final static def sshCredentials = 'e85bf18f-89af-4890-ab74-042b6330fba9'

// defines the backup repository to push to
@Field final static def backupOrigin = 'git@github.com:Splendit/simonykees.git'

enum Profile {
    MASTER_PRODUCTION_PROGUARD("-Dproduction -Dproguard -Pmaster-production-proguard", ""),
    MASTER_PRODUCTION_noPROGUARD("-Dproduction -Pmaster-production-noProguard", "_noProguard"),
    MASTER_TEST_PROGUARD("-Dproguard -Pmaster-test-proguard", "_test"),
    MASTER_TEST_noPROGUARD("-Pmaster-test-noProguard", "_noProguard_test"),
    DEVELOP_TEST_PROGUARD("-Dproguard -Pdevelop-test-proguard", "_test"),
    DEVELOP_TEST_noPROGUARD("-Pdevelop-test-noProguard", "_noProguard_test"),
    RELEASE_TEST_PROGUARD("-Dproguard -PreleaseCandidate", "_test_rc");

    Profile(String options, String qualifier) {
        this.options = options
        this.qualifier = qualifier
    }

    private final String options
    private final String qualifier

    String mvnOptions(String timestamp) { "$options -DforceContextQualifier=${timestamp}${qualifier}" }

    String formattedName() { name().split("_").collect { it.toLowerCase().capitalize() }.join(" ") }

    // TODO remove this getter once a nicer solution for uploadMappingFile(Profile profile) is found
    String getQualifier() { qualifier }
}

timestamps {
    node {
        step([$class: 'StashNotifier']) // Notifies the Stash Instance of an INPROGRESS build

        try {

            String timestamp = new Date().format("yyyyMMdd-HHmm", TimeZone.getTimeZone('UTC'))

            checkout()

            // we split at the slash. this is only relevant for the release branch.
            switch (env.BRANCH_NAME.tokenize("/")[0]) {
                case "develop":

                    runStandardSteps()

                    runSonarQubeAnalysis()

                    // deploy test noProguard
                    Profile profile = Profile.DEVELOP_TEST_noPROGUARD
                    deployEclipsePlugin(profile, timestamp)
                    deployMavenPlugin(profile, timestamp)

                    // deploy test proguard
                    profile = Profile.DEVELOP_TEST_PROGUARD
                    deployEclipsePlugin(profile, timestamp)
                    uploadMappingFile(profile)

                    tagCommit(env.BRANCH_NAME, "main")
                    break
                case "master":

                    runStandardSteps()

                    // deploy production proguard
                    Profile profile = Profile.MASTER_PRODUCTION_PROGUARD
                    deployEclipsePlugin(profile, timestamp)
                    uploadMappingFile(profile)

                    // deploy production noProguard
                    profile = Profile.MASTER_PRODUCTION_noPROGUARD
                    deployEclipsePlugin(profile, timestamp)

                    // deploy test proguard
                    profile = Profile.MASTER_TEST_PROGUARD
                    deployEclipsePlugin(profile, timestamp)
                    uploadMappingFile(profile)

                    // deploy test noProguard
                    profile = Profile.MASTER_TEST_noPROGUARD
                    deployEclipsePlugin(profile, timestamp)

                    tagCommit(env.BRANCH_NAME, "main")
                    break
                case "master-jmp":

                    runStandardSteps()

                    // deploy production proguard
                    Profile profile = Profile.MASTER_PRODUCTION_PROGUARD
                    buildEclipsePlugin(profile, timestamp) // only build, no deploy
                    uploadMappingFile(profile)
                    deployMavenPlugin(profile, timestamp)

                    // deploy test noProguard
                    profile = Profile.MASTER_TEST_noPROGUARD
                    buildEclipsePlugin(profile, timestamp) // only build, no deploy
                    deployMavenPlugin(profile, timestamp)

                    tagCommit("master", "jmp")

                    break
                case "release":

                    runStandardSteps()

                    // deploy test proguard
                    Profile profile = Profile.RELEASE_TEST_PROGUARD
                    deployEclipsePlugin(profile, timestamp)
                    uploadMappingFile(profile)
                    deployMavenPlugin(profile, timestamp)

                    break
                default:

                    runStandardSteps()

                    break

            }

        } catch (e) {
            // If there was an exception thrown, the build failed
            currentBuild.result = "FAILURE"
            throw e
        } finally {
            // Success or failure, always send notifications
            notifyBuild(currentBuild.result)
        }

        step([$class: 'StashNotifier']) // Notifies the Stash Instance of the build result
    }
}

void checkout() {
    stage('Preparation') { // for display purposes
        checkout scm
        // getting the remote url we are building from
        def url = sh(returnStdout: true, script: 'git config remote.origin.url').trim()

        setLiveEnvironment(url)
    }
}

void pushToGithub() {

    def stageName = "Push to Github"

    if (isLiveEnvironment) {
        stage(stageName) {
            println "Pushing to GitHub..."
            sshagent([sshCredentials]) { //key id of ssh-rsa key in remote repository within jenkins
                // pushing the repository to github
                // FIXME: ITGlobal temporary avoiding github push. Ssh credentials need to be updated 
                //sh("git push $backupOrigin HEAD:refs/heads/$env.BRANCH_NAME")
            }
        }
    } else {
        mockStage(stageName)
    }
}

void runStandardSteps() {

    // this has been added to the standard steps, see SIM-1737
    pushToGithub();

    compileEclipsePlugin()
    compileMavenPlugin()
    runIntegrationTests()
}

void compileEclipsePlugin() {
    stage('Compile Eclipse Plugin') {
        def mvnCommand = 'clean verify -B -DskipTests'
        sh "'${mvnBin()}' ${mvnCommand}"
    }
}

/**
 * this only tests whether or not the sources compile.
 * see deployMavenPluginWithDependencies for a full build that includes copying dependencies
 */
void compileMavenPlugin() {
    stage('Compile Maven Plugin') {
        def mvnCommand = 'clean install -DskipTests'

        dir('jsparrow-maven-plugin') {
            sh "'${mvnBin()}' ${mvnCommand}"
        }
    }
}

/**
 * Creates a virtual X windows display and runs integration tests.
 * Uses setTestStatus to set the result.
 */
void runIntegrationTests() {
    wrap([$class: 'Xvfb', additionalOptions: '', assignedLabels: '', autoDisplayName: true, debug: true, screen: '1366x768x24', shutdownWithBuild: true, timeout: 10]) {
        // X virtual framebuffer (virtual X window display) is needed for plugin tests
        // wrap([$class: 'Xvfb']) {
        stage('Integration-Tests') {
            // Run the maven build
            def mvnCommand = 'clean verify -B -fae -Dsurefire.rerunFailingTestsCount=2'

            // def mvnCommand = 'surefire:test -fae -Dsurefire.rerunFailingTestsCount=2'
            def statusCode = sh(returnStatus: true, script: "'${mvnBin()}' ${mvnCommand}")

            // in case of failing tests, there will be 'repeats' number of reruns
            int i = 0
            int repeats = 1
            while (statusCode != 0 && i < repeats) {
                def rerunTests = 'clean verify -B -fae'
                statusCode = sh(returnStatus: true, script: "'${mvnBin()}' ${rerunTests}")
                i = i + 1
            }

            setTestStatus(statusCode)

            // collects unit test results
            junit '**/target/surefire-reports/TEST-*.xml'
            archive 'target/*.jar'
        }
    }
}

/**
 * Run SonarQube analysis, server configuration takes place in the Jenkins config
 */
void runSonarQubeAnalysis() {

    def stageName = "SonarQube Analysis"

    if (isLiveEnvironment) {
        stage(stageName) {
            withSonarQubeEnv('SonarQube Server') {
                sh 'mvn sonar:sonar'
            }
        }
    } else {
        mockStage(stageName)
    }

}

void tagCommit(def branchName, String subdirectory) {

    stage('Tag Commit (Bitbucket)') {
        // tag build in repository
        sshagent([sshCredentials]) { //key id of ssh-rsa key in remote repository within jenkins
            // first parameter is the dir, second parameter is the subdirectory and optional
            sh("./tag-deployment.sh $branchName $subdirectory")
        }
    }

    def stageName = "Tag Commit (GitHub)"

    if (isLiveEnvironment) {
        stage(stageName) {
            // push tags to github
            sshagent([sshCredentials]) { //key id of ssh-rsa key in remote repository within jenkins
                // FIXME: ITGlobal - fix ssh credentials to push to github
                // sh("git push $backupOrigin --tags")
            }
        }
    } else {
        mockStage(stageName)
    }
}

void buildEclipsePlugin(Profile profile, String timestamp) {

    def mvnCommand = "clean verify -DskipTests -B ${profile.mvnOptions(timestamp)}"

    stage("Eclipse Build: ${profile.formattedName()}") {
        sh "'${mvnBin()}' $mvnCommand"
    }
}

void deployEclipsePlugin(Profile profile, String timestamp) {

    def mvnCommand = "clean deploy -DskipTests -X -B ${profile.mvnOptions(timestamp)}"

    def stageName = "Eclipse Deploy: ${profile.formattedName()}"

    if (isLiveEnvironment) {
        stage(stageName) {
            sh "'${mvnBin()}' $mvnCommand"
        }
    } else {
        mockStage(stageName)
    }

}

/**
 * IMPORTANT: the Eclipse plugin has to be built first! This means that either buildEclipsePlugin or deployEclipsePlugin has to be executed with the same profile!
 *
 * @param profile the same profile that has been used to build the Eclipse plugin
 */
void deployMavenPlugin(Profile profile, String timestamp) {

    /*
     * for the JMP -DforceContextQualifier will do nothing on master because there is no qualifier
     * (1.0.0 vs 1.0.0-SNAPSHOT -> SNAPSHOT will be replaced with the qualifier for the jar name)
     */
    def mvnCommand = "clean deploy -DskipTests -B ${profile.mvnOptions(timestamp)}"

    def pluginResourcePath = 'jsparrow-maven-plugin/src/main/resources'
    def jSparrowTargetPath = 'releng/eu.jsparrow.product/target/repository/plugins'
    def manifest = 'manifest.standalone'
    def manifestContent = sh(script: "ls $jSparrowTargetPath", returnStdout: true)

    def stageName = "JMP Deploy: ${profile.formattedName()}"

    if (isLiveEnvironment) {
        stage(stageName) {
            dir('jsparrow-maven-plugin/src/main/resources') {
                writeFile file: "${manifest}", text: "${manifestContent}"
            }

            // Copy required dependencies into plugin resource folder
            sh("cp ${jSparrowTargetPath}/* ${pluginResourcePath}")

            dir('jsparrow-maven-plugin') {
                sh "'${mvnBin()}' ${mvnCommand}"
            }
        }
    } else {
        mockStage(stageName)
    }

}

void mockStage(String stageName) {
    stage("MOCK: $stageName") {
        println "Stage '$stageName' got called but will not be executed"
    }
}

void uploadMappingFile(Profile profile) {

    def stageName = "Upload Mapping Files"

    if (isLiveEnvironment) {
        stage(stageName) {
        	// TODO there has to be a nicer way to get the version (and then the directory), when we already have the qualifier
          // tag-deployment.sh uses the same mechanism
    		def buildNumber = sh(returnStdout: true, script: "pcregrep -o1 \"name='eu\\.jsparrow\\.photon\\.feature\\.feature\\.group' range='\\[.*,((\\d*\\.){3}\\d{8}-\\d{4})\" releng/eu.jsparrow.site.photon/target/p2content.xml").trim()
    		def directory = "${buildNumber}${profile.qualifier}"
            // FIXME: ITGlobal skip mapping file upload. we need the deobfuscation service up and running in ITGlobal env
            uploadMappingFiles(directory)
        }
    } else {
        mockStage(stageName)
    }
}

def setTestStatus(testStatus) {
    if (testStatus == 0) {
        currentBuild.result = 'SUCCESS'
    } else if (testStatus == 1) {
        currentBuild.result = 'UNSTABLE'
    }
}


def notifyBuild(def buildStatus) {
    // send to email only if buildStatus is UNSTABLE or FAILED
    if (buildStatus == 'FAILURE' || buildStatus == 'UNSTABLE') {
        jobName = env.JOB_NAME.replace("%2F", "/")

        //currently not supported by pipeline variable
        def gitCommit = sh(returnStdout: true, script: 'git rev-parse HEAD').trim()
        def commitURL = "https://bitbucket.splendit.loc/projects/LM/repos/simonykees/commits/${gitCommit}"
        def emailOfBuildInitiate = emailextrecipients([[$class: 'DevelopersRecipientProvider']])

        def subject = "${buildStatus}: Job '${jobName} [${env.BUILD_NUMBER}]'"
        def details = "<p>${buildStatus}: Job '${jobName} [${env.BUILD_NUMBER}]':</p>" +
                "\n<p>List of committers for this build: [${emailOfBuildInitiate}]</p>" +
                "\n<p>Last commit url: ${commitURL}" +
                "\n<p>Check console output at \"<a href='${env.BUILD_URL}'>${jobName} [${env.BUILD_NUMBER}]</a>\"</p>"



        emailext(
                subject: subject,
                body: details,
                recipientProviders: [[$class: 'CulpritsRecipientProvider']]
        )
    }
}

def uploadMappingFiles(String directory) {
    def statusCode = sh(returnStatus: true, script: "./uploadMappingFiles.sh ./ ${directory}")
    if (statusCode != 0) {
        println("Uploading mapping files FAILED! Error Code: ${statusCode}")
        currentBuild.result = "UNSTABLE"
    } else {
        println("Uploading mapping files SUCCEEDED!")
        currentBuild.result = "SUCCESS"
    }
}
