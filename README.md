## jPOS Template

Clone this project in order to create your own jPOS based application.

The template ships with a `.sdkmanrc` file that pins Java 25.0.1-amzn and Gradle 9.4.0 so every shell can run `sdk env install && sdk env` to reproduce the toolchain.
It also applies the published `org.jpos.jposapp` Gradle plugin (v0.0.16), which replaces the legacy `jpos-app.gradle` helper and provides the `installApp`, `dist`, `distnc`, `zip`, and `run` tasks out of the box.

We recommend that you install [Gradle](http://gradle.org/) in order to build your jPOS projects, but if you don't have it installed, you can use the Gradle wrapper scripts `gradlew` and `gradlew.bat`. In the following instructions, when we say `gradle` we really mean either your installed Gradle or one of the wrapper scripts (depending if you are on Unix or DOS based platforms).

### Build an eclipse project
````
gradle eclipse
````

### Build an IDEA project
````
gradle idea
````

### Build your own jar
````
gradle jar
````

### Check the jPOS version
````
gradle version
````

### Create a distribution of your application
````
gradle dist
````
This creates a tar gzipped file in the `build/distributions` directory.

### Install application in 'build/install' directory
````
gradle installApp
````
Installs application in `build/install` with everything you need to run jPOS. Once the directory is created, you can `cd build/install` and call `java -jar your-project-version.jar` or the `bin/q2` (or `q2.bat`) script available in the `bin` directory.

### Generate an install a Maven artifact
````
gradle install
````

### List available Gradle tasks
````
gradle tasks
````

