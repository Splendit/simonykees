#!/usr/bin/groovy

// if this script is changed expand the documentation on confluence!
// url: https://confluence.splendit.loc/display/SIM/Jenkins+Pipeline+Description


node {
	try {
		notifyBuild('STARTED')
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
				
				// collects unit test results
				junit '**/target/surefire-reports/TEST-*.xml'
				archive 'target/*.jar'
			}
		}
		
		// master and develop builds get deployed to packagedrone (see pom.xml) and tagged (see tag-deployment.sh)
		if ( env.BRANCH_NAME == 'master' || env.BRANCH_NAME == 'develop' ) {
			if ( currentBuild.result == 'SUCCESS' ) {
				stage('Deploy and Tag') {
					// skipping tests, because integration tests have passed already
					// -B batch mode for clean output (otherwise upload status will spam the console)
					def mvnCommand = 'clean deploy -DskipTests -B'
					sh "'${mvnHome}/bin/mvn' ${mvnCommand}"	
					// tag build in repository
					sshagent([sshCredentials]) { //key id of ssh-rsa key in remote repository within jenkins
						// first parameter is the dir, second parameter is the subdirectory and optional
						sh("./tag-deployment.sh $env.BRANCH_NAME main")
						sh("git push $backupOrigin --tags")
					}
				}
			}
		}
	} catch (e) {
	  // If there was an exception thrown, the build failed
	  currentBuild.result = "FAILED"
	  throw e
	} finally {
	  // Success or failure, always send notifications
	  notifyBuild(currentBuild.result)
	}
}

def setTestStatus(testStatus) {
	if (testStatus == 0) {
		currentBuild.result = 'SUCCESS'
	} else if ( testStatus == 1 ) {
		currentBuild.result = 'UNSTABLE'
	}
}


def notifyBuild(String buildStatus) {
  // send to email

	jobName = env.JOB_NAME.replace("%2F", "/")

	def subject = "${buildStatus}: Job '${jobName} [${env.BUILD_NUMBER}]'"
  def details = "<p>${buildStatus}: Job '${jobName} [${env.BUILD_NUMBER}]':</p>\n<p>Check console output at \"<a href='${env.BUILD_URL}'>${jobName} [${env.BUILD_NUMBER}]</a>\"</p>"

	  emailext (
      subject: subject,
      body: details,
      recipientProviders: [[$class: 'CulpritsRecipientProvider']]
    )
}
