# jSparrow Maven Plugin

This repository contains the source for the jSparrow Maven Plugin. 

For installation, build instructions and usage see the [Confluence Page](https://confluence.splendit.loc/pages/viewpage.action?pageId=70844440).

## Statistics
The JMP offers the possibility to send statistics to our AWS DynamoDB Intance. For that, three metadata parameters and one parameter for enabling the statistics collection have to be specified when executing the JMP:

  - `-DstartTime="<start-timestamp>"`: the start time for the duration meassurement in the form defined by [`DateTimeFormatter.ISO_INSTANT`](https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html#ISO_INSTANT)
  - `-DrepoOwner="<repo-owner>"`: The company or repository owner of the project
  - `-DrepoName="<repo-name>"`: The name of the project or repository
  - `-DsendStatistics`: When set, the statistics will be send to AWS DynamoDB
