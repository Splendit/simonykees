#!/usr/bin/groovy

node {
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
			println "Pushing to github..."
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

	wrap([$class: 'Xvfb']) {
		stage('Integration-Tests') {
			// Run the maven build
			def mvnCommand = 'clean verify -fae -Dsurefire.rerunFailingTestsCount=2'

			// def mvnCommand = 'surefire:test -fae -Dsurefire.rerunFailingTestsCount=2'
			setTestStatus(sh(returnStatus: true, script: "'${mvnHome}/bin/mvn' ${mvnCommand}"))
			// collects unit test results
			junit '**/target/surefire-reports/TEST-*.xml'
			archive 'target/*.jar'
		}
	}
	
	if ( env.BRANCH_NAME == 'master' || env.BRANCH_NAME == 'develop' ) {
		if ( currentBuild.result == 'SUCCESS' ) {
			stage('Deploy and Tag') {
				// skipping tests, because integration tests have passed already
				// -B batch mode for clean output
				def mvnCommand = 'clean deploy -DskipTests -B'
			  sh "'${mvnHome}/bin/mvn' ${mvnCommand}"	
				// tag build in reppsitory
				sshagent([sshCredentials]) { //key id of ssh-rsa key in remote repository within jenkins
					// first parameter is the dir, second parameter is the subdirectory and optional
					sh("./tag_deployment.sh $env.BRANCH_NAME main")
      	  sh("git push $backupOrigin --tags")
				}
			}
		}
	}
}

def setTestStatus(testStatus) {
	println testStatus
  if (testStatus == 0) {
    currentBuild.result = 'SUCCESS'
  } else if ( testStatus == 1 ) {
    currentBuild.result = 'UNSTABLE'
  }
}
