# Trust Policy Authoring Tool (TPAT)
This repository holds the source for the Trust Policy Authoring Tool. 

## How to Use the TPAT

If you are interested in trying out the TPAT, we recommmended that you deploy it using the [Trustmark Tools Deployer](https://github.com/Trustmark-Initiative/trustmark-tools-deploy). You can quickly setup a local version of the tool to experiment with, and you can use that project to deploy an Internet facing version as well. For more information about how to use the TPAT, please see the [TPAT User Guide](https://github.com/Trustmark-Initiative/tpat/wiki).

## How to Build

1. Download and Install Java8.  Make sure javac -version is `javac 1.8.0_*`

2. You must have a built copy of the https://github.com/Trustmark-Initiative/tmf-api available in your local Maven repository.

3. Install SDK Manager, see details at: sdkman.io

4. Once SDK Man is installed, install grails.  Done this way: `sdk install grails`.  The current version is 4.0.1

5. You can build the software with grails or gradlew.

6. To run the software you must have a database available.  You can run this DB via docker, locally, remotely, or however makes sense 
for your environment.  You must configure the DB within the `grails-app/conf/application.yml` file.  This file is currently configured
to use the docker database as seen configured within the https://github.com/Trustmark-Initiative/trustmark-tools-deploy project.

7. Execute the web application (grails run-app or run the war in docker)

