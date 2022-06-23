# jSparrow Language Server 

This is POC of the jSparrow Language Server. 
It provides a simple implementation of the Language Server Protocol which makes use of jSparrow Marker resolvers. 
Thus, provides jSparrow refactoring through the LSP.

## Requirements 

## How to Build

Simply run `mvn clean verify` in the parent project.

## How to Run

For the whole language server to get up and running it requires two parts: 

A) The Client Side (see [vscode-java](https://github.com/redhat-developer/vscode-java) as an example).
See also [VS Code Contributing](https://github.com/redhat-developer/vscode-java/blob/master/CONTRIBUTING.md).

B) The Server Side (this project). This POC is set up based on [Eclipse JDT Language Server](https://github.com/eclipse/eclipse.jdt.ls). 


See 

## Resources 
 
* [Language Server Protocol](https://microsoft.github.io/language-server-protocol/)
* [Language support for Java for VS Code](https://github.com/redhat-developer/vscode-java)
* [Eclipse JDT Language Server](https://github.com/eclipse/eclipse.jdt.ls). 