# MasterTools

This application is a tool to assist me in running
Dungeons & Dragons 5th Edition games.

## Requirements
* Java 13+
* JavaFX
* SQLite 3.28.0+
* Gradle 6.0.1+

## Data Storage
As indicated above, this application uses SQLite.  The
database files will be stored in a subdirectory of
your home directory, `.mastertools`.

## Build
This is a Gradle-based project, so building the basic
jar is nothing more than:
```
./gradlew build
```

Building the runnable image requires running
```
./bundle.sh
```

This will create a mac OS-compliant (at least as of Catalina)
app bundle.  This will be created in the 'build' directory.

Executables are not currently created for either Window or
Linux, though the Java source code is not OS-specific.
