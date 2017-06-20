#!/usr/bin/groovy

// if this script is changed expand the documentation on confluence!
// url: https://confluence.splendit.loc/display/SIM/Jenkins+Pipeline+Description

// add timestaps to the "Console Output" of Jenkins
timestamps {
	node {
		try {
			// variable for maven home
			def mvnHome = tool 'mvn system'
			// defines the backup repository to push to
			def backupOrigin = 'git@github.com:Splendit/simonykees.git'
			// jenkins git ssh credentials
			def sshCredentials = '7f15bb8a-a1db-4cdf-978f-3ae5983400b6'
			
			
			stage('Preparation') { // for display purposes
				checkout scm
			}
			
			if ( env.BRANCH_NAME == 'master' || env.BRANCH_NAME == 'develop' ) {
				stage('Push to Github') {
					println "Pushing to GitHub..."
					sshagent([sshCredentials]) { //key id of ssh-rsa key in remote repository within jenkins
						// pushing the repository to github
						sh("git push $backupOrigin HEAD:$env.BRANCH_NAME")
					}
				}
			}
			
			stage('Maven Compile') {
				def mvnCommand = 'clean compile'
				sh "'${mvnHome}/bin/mvn' ${mvnCommand}"
			}
			
			wrap([$class: 'Xvfb', additionalOptions: '', assignedLabels: '', autoDisplayName: true, debug: true, screen: '1366x768x24', shutdownWithBuild: true, timeout: 10]) {
			// X virtual framebuffer (virtual X window display) is needed for plugin tests
			// wrap([$class: 'Xvfb']) {
				stage('Integration-Tests') {
					// Run the maven build
					def mvnCommand = 'clean install -fae -Dsurefire.rerunFailingTestsCount=2'
			
					// def mvnCommand = 'surefire:test -fae -Dsurefire.rerunFailingTestsCount=2'
					def statusCode = sh(returnStatus: true, script: "'${mvnHome}/bin/mvn' ${mvnCommand}")
			
					// in case of failing tests, there will be 'repeats' number of reruns 
					int i = 0
					int repeats = 1		
					while (statusCode != 0 && i < repeats) {
						def rerunTests = 'verify -fae'
						statusCode = sh(returnStatus: true, script: "'${mvnHome}/bin/mvn' ${rerunTests}")
						i = i + 1
					}
			
					setTestStatus(statusCode)
					
					// extract the qualifier from the build to generate the obfuscated build with the same buildnumber
					def qualifier = sh(returnStdout: true, script: "pcregrep -o1 \"name='jSparrow\\.feature\\.feature\\.group' range='\\[.*,(.*-\\d{4})\" site/target/p2content.xml")
					println qualifier
					
					// collects unit test results
					junit '**/target/surefire-reports/TEST-*.xml'
					archive 'target/*.jar'
				}
			}
			
			// master and develop builds get deployed to packagedrone (see pom.xml) and tagged (see tag-deployment.sh)
			if ( env.BRANCH_NAME == 'master' || env.BRANCH_NAME == 'develop' ) {
				if ( currentBuild.result == 'SUCCESS' ) {
					// skipping tests, because integration tests have passed already
					// -B batch mode for clean output (otherwise upload status will spam the console)
					def mvnCommand = 'clean deploy -DskipTests -B'
				
				
					stage('Deploy and Tag') {
						sh "'${mvnHome}/bin/mvn' ${mvnCommand} -P${env.BRANCH_NAME}-test-noProguard"	
						
						// tag build in repository
						sshagent([sshCredentials]) { //key id of ssh-rsa key in remote repository within jenkins
							// first parameter is the dir, second parameter is the subdirectory and optional
							sh("./tag-deployment.sh $env.BRANCH_NAME main")
							sh("git push $backupOrigin --tags")
						}
					}
					
					// extract the qualifier from the build to generate the obfuscated build with the same buildnumber
					def qualifier = sh(returnStdout: true, script: "pcregrep -o1 \"name='jSparrow\\.feature\\.feature\\.group' range='\\[.*,(.*-\\d{4})\" site/target/p2content.xml")
					println qualifier
					
					stage('Deploy obfuscation') {
						def mvnOptions = '-Dproguard -DforceContextQualifier=${qualifier}_test'
						sh "'${mvnHome}/bin/mvn' ${mvnCommand} ${mvnOptions} -P${env.BRANCH_NAME}-test-proguard"
					}
					if ( env.BRANCH_NAME == 'master') {
						stage('Deploy production') {
							def mvnOptions = '-Dproduction -DforceContextQualifier=${qualifier}_noProguard'
							sh "'${mvnHome}/bin/mvn' ${mvnCommand} ${mvnOptions} -P${env.BRANCH_NAME}-production-noProguard"
						}
						stage('Deploy production, obfuscation') {
							def mvnOptions = '-Dproduction -Dproguard -DforceContextQualifier=${qualifier}'
							sh "'${mvnHome}/bin/mvn' ${mvnCommand} ${mvnOptions} -P${env.BRANCH_NAME}-production-proguard"
						}
					}
				}
			}
		} catch (e) {
			// If there was an exception thrown, the build failed
			currentBuild.result = "FAILURE"
			throw e
		} finally {
			// Success or failure, always send notifications
			notifyBuild(currentBuild.result)
		}
	}
}
	
def setTestStatus(testStatus) {
	if ( testStatus == 0 ) {
		currentBuild.result = 'SUCCESS'
	} else if ( testStatus == 1 ) {
		currentBuild.result = 'UNSTABLE'
	}
}


def notifyBuild(String buildStatus) {
	// send to email only if buildStatus is UNSTABLE or FAILED
	if (buildStatus == 'FAILURE' || buildStatus == 'UNSTABLE') {
		jobName = env.JOB_NAME.replace("%2F", "/")

		//currently not supported by pipeline variable
		def gitCommit = sh(returnStdout: true, script: 'git rev-parse HEAD').trim()
		def commitURL = "https://bitbucket.splendit.loc/projects/LM/repos/simonykees/commits/${gitCommit}"
		def emailOfBuildInitiate = emailextrecipients( [[$class: 'DevelopersRecipientProvider']])

		def subject = "${buildStatus}: Job '${jobName} [${env.BUILD_NUMBER}]'"
		def details = "<p>${buildStatus}: Job '${jobName} [${env.BUILD_NUMBER}]':</p>" +
									"\n<p>List of committers for this build: [${emailOfBuildInitiate}]</p>" +
									"\n<p>Last commit url: ${commitURL}" +
									"\n<p>Check console output at \"<a href='${env.BUILD_URL}'>${jobName} [${env.BUILD_NUMBER}]</a>\"</p>"


	
		emailext (
			subject: subject,
			body: details,
			recipientProviders: [[$class: 'CulpritsRecipientProvider']]
		)
	}
}

