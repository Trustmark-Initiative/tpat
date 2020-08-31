# Trustmark Framework Artifact Manager (TFAM)
This repository holds the source for the AAPC's Trustmark Framework Artifact Manager, or TFAM.

## How to Build

1. Download and Install Mysql Server and Java8.  Make sure javac -version is `javac 1.8.0_*`

2. Install SDK Manager, see details at: sdkman.io

3. Once SDK Man is installed, install grails 3.1.x (3.1.11 at the time of this writing).  Done this way: `sdk install grails 3.1.11`

4a  Add an entry to your HOSTS file for 127.0.0.1 aliased to "aapc_mysql_host"
4b. Create your Database: Create a database based on the values in the `grails-app/conf/application.yml` file.  For production, that's `/WEB-INF/classes/application.yml`
    * The default values are: 
        * database = tfam_dev 
        * username = tfam 
        * password = tfam11!

```
CREATE DATABASE tfam_dev;
CREATE USER 'tfam'@'localhost' IDENTIFIED BY 'tfam11!';
GRANT ALL PRIVILEGES ON tfam_dev.* TO 'tfam'@'localhost';
FLUSH PRIVILEGES;
```

5. Execute the web application (grails run-app or run the war in tomcat8)

6. The default user accounts are configured in `grails-app/conf/defaultAccounts.properties`, but by default they are:
    * username = admin@tfam.trustmark.gtri.gatech.edu, password = admin11!
    * username = org.admin@tfam.trustmark.gtri.gatech.edu, password = admin11!
    * username = developer@tfam.trustmark.gtri.gatech.edu, password = dev11!
    * username = reviewer@tfam.trustmark.gtri.gatech.edu, password = review11!

7. To build the docker container execute (modify as needed for other versions):
   * docker image build . -t tfam-prod:latest -f docker/war-only/tfam.safecom.gtri.gatech.edu/Dockerfile

8. To run the specific docker image on safecom: 
   * docker run -d --add-host=aapc_mysql_host:172.17.0.1 --volume /opt/tfam/:/opt/tfam --network bridge --name tfam1 --publish 8009:8009 --restart always tfam-prod
   * docker run -p3306:3306/tcp -p33060:33060/tcp -e MYSQL_ROOT_PASSWORD=tfam11! -d mysql:latest

