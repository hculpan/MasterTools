# MasterTools

This application is a tool to assist me in running
Dungeons & Dragons 5th Edition games.

## Requirements
* Java 11+
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

This will actually call Gradle to build the app, so the
first command is not necessary when building a runnable
image.  The resultant image will be in `build/image`, and
will have a `start` script to launch the application.
