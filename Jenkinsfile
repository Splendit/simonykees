#!/usr/bin/groovy

node {
  // variable for maven home
	def mvnHome
	stage('Preparation') { // for display purposes
		checkout scm
		mvnHome = tool 'mvn system'
	}

	stage('Maven Compile') {
			def mvnCommand = 'clean compile'
			if (isUnix()) {
				sh "'${mvnHome}/bin/mvn' ${mvnCommand}"
			} else {
				bat(/"${mvnHome}\bin\mvn ${mvnCommand}"/)
			}
  }

	wrap([$class: 'Xvfb']) {
		stage('Integration-Tests') {
			// Run the maven build
			def mvnCommand = 'clean verify -fae -Dsurefire.rerunFailingTestsCount=2'
			//def mvnCommand = 'surefire:test -fae -Dsurefire.rerunFailingTestsCount=2'
			if (isUnix()) {
				setTestStatus(sh(returnStatus: true, script: "'${mvnHome}/bin/mvn' ${mvnCommand}"))
			} else {
				setTestStatus(bat(/"${mvnHome}\bin\mvn ${mvnCommand}"/))
			}
		}
		stage('Results') {
				junit '**/target/surefire-reports/TEST-*.xml'
				archive 'target/*.jar'
		}
	}
	
	if ( env.BRANCH_NAME == 'master' || env.BRANCH_NAME == 'develop' ) {
		stage('Push to Github') {
			println "Is Develop or Master"
			println "Pushing to github..."
			sshagent(["jenkins-testjsparrow"]) { //key id of ssh-rsa key in remote repository within jenkins
				// pushing the repository to github
				sh("git checkout $env.BRANCH_NAME")
      	sh("git push ssh://git@bitbucket.splendit.loc:7999/tes/testsparrow2.git HEAD:$env.BRANCH_NAME")
				sh("git rev-parse HEAD | xargs git checkout")
				sh("git branch -d $env.BRANCH_NAME")
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
