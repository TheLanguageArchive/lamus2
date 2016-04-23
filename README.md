# LAMUS 2


LAMUS 2 is a web-based application that allows users to organize and update the 
content in the extensive archive of the [CMDI (Component MetaData Infrastructure)]
(https://www.clarin.eu/content/component-metadata) corpus at the
[Max Planck Institute for Psycholinguistics] (http://www.mpi.nl/),
maintained by [TLA (The Language Archive)] (https://tla.mpi.nl/).


## Dependencies

LAMUS 2 works within the LAT (Language Archiving Technology) ecosystem, so it
depends on the existence of several MPI databases, as well as web applications
and JARs in order to be useful. The JARs are included in the POM dependencies,
but the applications and databases are separate and need to be running in the same server.
In order to get a virtual image containing most of these dependencies, please
try [here] (https://tla.mpi.nl/tools/tla-tools/vmwareimage/).
This might not be prepared for a CMDI archive, and therefore not include the necessary dependencies, though.
For a more up-to-date image or more information on how to setup your archive,
please [contact TLA] (https://tla.mpi.nl/contact/).

The essential components for LAMUS 2 to function are:
* A CMDI archive
* The Corpusstructure database, which stores the information about the archive
* The LAMUS 2 database
* AMS 2 (access management system), together with its database of users and rules


## Building and Deploying the application

Before building the application, an external jar needs to be installed in your local maven repository.
This is a dependency of the cmdi-validator jar, coming from the CLARIN repository.
At the moment it's not pulling a particular jar it depends on - xercesImpl-patched. This jar is provided under [jar/lib] (https://github.com/TheLanguageArchive/lamus2/tree/master/jar/lib).
You can install it in your repository using the following command:

```
mvn install:install-file -Dfile=jar/lib/xercesImpl-patched-2.11.0.jar -DgroupId=xerces -DartifactId=xercesImpl-patched -Dversion=2.11.0 -Dpackaging=jar
```

The application can then be packaged:

```
mvn clean package -DdeployTo={YOUR_SERVER}
```

The 'deployTo' property is used to specify the server for which the application is going to be deployed;
a default value can be set in the maven settings.xml file.
In this case it is just passed directly in the maven command.
The value of this property is ultimately used to populate the shibboleth URL values.
So a file named 'application.{YOUR_SERVER}.properties' must be created with the appropriate values.
You can find some examples [here] (https://github.com/TheLanguageArchive/lamus2/tree/master/wicket/src/main/filters).

Then the package should be deployed to the server (either using the war file created under the 'wicket' project or the tar.gz under the 'targz' project.

## Configuration

There is some necessary configuration to be applied to the tomcat running LAMUS 2.
An example of this is shown in [CONFIGURATION.txt] (https://github.com/TheLanguageArchive/lamus2/blob/master/CONFIGURATION.txt).