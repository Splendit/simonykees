# simonykees #

# --tmp-test-deployment-- #

    add to maven settings.xml the deployment credentials (/usr/share/maven/settings.xml)

		<server>
			<id>pdrone.b8eed447-5f46-42c2-bfc1-1b9b174069e5</id>
			<username>deploy</username>
			<password>c8afacb01b3ce8171db24192e31b13eb60110a59b9b3f989cf9a6b3815aac496</password>
		</server>

		deploy with: mvn clean deploy

		updated osgi bundle will be visible on:
		http://packagedrone-vm-01.splendit.loc:8080/p2/jSparrow-test-channel/


## build ##

    mvn clean verify -fae

## release ##

    mvn org.eclipse.tycho:tycho-versions-plugin:0.26.0:set-version -DnewVersion=<newVersion>
