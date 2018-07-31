# Contributing to jSparrow

When contributing to this repository make sure that an issue outlining what you want to change exists. 

## Pull Request Process

### Review Checklist

When reviewing a pull request the reviewer should stick to the steps outlined below. When accepting a pull request make sure you update the related issue status to signify that you are working on this issue. 

- **Exploratory Tests**: Verify that not only the changes outlined in the issue related to the pull request work, but also that no related functionality was broken. The focus here is to try and find bugs and identify any unwanted side effects. 

  Be especially wary of large pull requests that change core functionality. Large changes will most likely take a long time to review, and it might even be necessary to run a full regression test. 

  If you are unsure how a specific change might be tested, ask your colleagues ;)
- **Code Review**: Have a look at the code and make sure that it adheres to the proper Code Style and [Coding Guidelines](https://confluence.splendit.loc/display/SIM/Coding+Guidelines). Make sure to execute a Sonar Analysis and check the pull request for any Sonar Issues. 
  
  Be careful to not only look for style, but also for maintainability issues. If you do not understand a specific piece of code, it is the authors job to make this code understandable. The reasoning behind why something was implemented in a certain way should always be clear, so check for comments explaining difficult code. 

- **Unit Tests**: Make sure that unit tests exists where applicable. In general, any new code should be covered by unit tests, but exceptions can be made. 

  Execute unit tests that already exist with [EclEmma](http://www.eclemma.org/) to check whether the content of the pull request is properly covered. If it is not additional tests need to be implemented. 

  Also make sure that the existing unit tests also adhere to the code style and follow unit testing best practices. 
- **Documentation**: Identify whether or not changes to the documentation are necessary. If a new feature or configuration option was added, this is most likely the case. 

  Verify that the existing documentation is clearly understandable. Assess if additional technical information (graphs, diagrams) are necessary to explain what was implemented.

- **Update the Issue & Pull Request**: Leave information for the pull request author relating to the points above in the pull request and the related ticket.
  
  Ideally, no verbal communication is required, and everything that needs to be changed or done should be clear from inspecting the pull request. 

