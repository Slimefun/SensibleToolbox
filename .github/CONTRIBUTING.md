# Contributing to SensibleToolbox
This document outlines various ways how you can help contribute to SensibleToolbox and make this a bigger and better project.<br>
All contributions must be inline with our [Code of Conduct](https://github.com/Slimefun/SensibleToolbox/blob/master/.github/CODE_OF_CONDUCT.md) and [License](https://github.com/Slimefun/SensibleToolbox/blob/master/LICENSE).
Please also follow the templates for Issues and Pull Requests we provide.

## :beetle: 1. Issues: Bug Reports
One of the foundations for good software is reliability. To facilitate this reliability, our community must work together to crush bugs that arise. 
This of course requires good information and knowledge about ongoing bugs and issues though.

You can help this project by reporting a bug on our [Issues Tracker](https://github.com/Slimefun/SensibleToolbox/issues).<br>
Please adhere to the provided template and provide as much information as possible.
For more info on how to make good and helpful bug reports, check out our article on [How to report bugs](https://github.com/Slimefun/Slimefun4/wiki/How-to-report-bugs).

If you encounter an issue which has already been reported, please don't open a new one.<br>
It would be awesome though if you could post a comment on the existing issue which explains how you were able to reproduce this yourself.
The more context and information we get, the easier we can fix it.

## :hammer_and_wrench: 2. Pull Requests: Bug Fixes
Bugs that have been reported need to be fixed of course.<br>
Any open Issue on our [Issues Tracker](https://github.com/Slimefun/SensibleToolbox/issues) is waiting to be fixed.

This is an Open-Source project and we love Pull Requests. 
So if you have an idea on how to approach a known issue, feel free to make a [Pull Request](https://github.com/Slimefun/SensibleToolbox/pulls) which fixes this bug.
You can also comment on the existing Issue, proposing your idea or communicating that you wanna work on this.

## :wrench: 3. Pull Requests: Additions/Changes
SensibleToolbox is an Open-Source project and anyone is allowed to make changes or add content to this plugin!

Please visit our [Discord Server](https://github.com/Slimefun/Slimefun4#discord) and share your ideas first, we hate to reject changes because the community disagrees.<br>
So communicating your intended changes before-hand will ensure that you don't put too much work into something that might get rejected.

## :star: 4. Pull Requests: Code Quality
SensibleToolbox uses [sonarcloud.io](https://sonarcloud.io/dashboard?id=TheBusyBiscuit_SensibleToolbox) to monitor Code Quality.

We always welcome quality improvements to the code and the "Code Smells" section on [sonarcloud.io](https://sonarcloud.io/dashboard?id=TheBusyBiscuit_SensibleToolbox) is a great place to start.
But please keep in mind that some design patterns may not be changed too abruptly if an addon depends on them. 
To prevent any accidents from happening, please contact us on our [Discord Server](https://github.com/Slimefun/Slimefun4#discord) before-hand and state your intended changes.

#### Documentation
Code documentation is also a great way to improve the maintainability of the project.
1. Every class and every public method should have a Javadocs section assigned to it. 
2. Classes should also include an `@author` tag to indicate who worked on that class.
3. Methods and parameters should be annotated with `@Nullable` or `@Nonnull` to indicate whether or not null values are accepted.


#### Unit Tests
Unit Tests help us test the project to work as intended in an automated manner.<br>
More or better Unit Tests are always good to have, so feel free to submit a Test and place it in our [src/test/java](https://github.com/Slimefun/Slimefun4/tree/master/src/test/java/io/github/thebusybiscuit/slimefun4/testing) directory

We are using [Junit 5 - Jupiter](https://github.com/junit-team/junit5/) and [MockBukkit](https://github.com/seeseemelk/MockBukkit) as our testing environment.<br>
Every new Unit Test should have a `@DisplayName` annotation with a plain text description on what the Unit Test tests.

## :toolbox: How to compile SensibleToolbox
SensibleToolbox is written in Java and uses [Maven](https://maven.apache.org/) for compilation.<br>
To compile SensibleToolbox yourself, follow these steps:

1. Clone the project via git<br>
`$ git clone https://github.com/Slimefun/SensibleToolbox/`
2. Compile the project using Maven<br>
`$ mvn clean package`
3. Extract the compiled `SensibleToolbox-vX-UNOFFICIAL.jar` from your `/target/` directory.

If you are already using an IDE, make sure to import the project via git and set it up as a *Maven project*. 
Then you should be able build it via Maven using the goals `clean package`.

If you have any further questions, then please join our [Discord Support Server](#discord) and ask your questions in the `#programming-help` channel.<br>
**Note that we will not accept any bug reports from custom-compiled versions of SensibleToolbox**.
