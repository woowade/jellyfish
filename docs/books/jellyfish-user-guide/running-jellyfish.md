---
restrictions: UNCLASSIFIED Copyright © 2018, Northrop Grumman Systems Corporation
title: Ch. 4 Running Jellyfish
book-title: Jellyfish User Guide
book-page: jellyfish-user-guide
next-title: Ch. 5 The Structure of a Service Project
next-page: the-structure-of-a-service-project
prev-title: Ch. 3 A Lightweight Micro-Service Architecture for Java
prev-page: a-lightweight-micro-service-architecture-for-java
---
{% include base.html %}

We'll cover actaully running Jellyfish in this chapter.  We'll use Jellyfish to intially create the micro-service
projects.  We'll then create a "system" project which is useful for testing.

# Updating the SD Project
Before we generate the project, we'll want to make a few updates to the model.  Since we've decided on using a
micro-service architecture for our system, we want to add metadata to our model to convey that decision.  This metadata
**does not impact Jellyfish in any way**.  It just helps communicate that the sub-components of the `SoftwareStore`
system our realized as micro-services and not some other type of architecture.

First, we'll include the `system` stereotype in the `SoftwareStore` model:

**SoftwareStore.sd**
```
model SoftwareStore {
  metadata {
    "stereotypes" : ["system"]
  }

  // ... Remaining items omitted.
}
```

Next, we'll add the `service` stereotype to each of the services.  For example,

**SoftwareRequestService.sd**
```
model SoftwareRequestService {
  metadata {
     "stereotypes" : ["service"]
  }

  // ... Remaining items omitted.
}
``` 

## Deployment Models
One of the most useful aspects of Jellyfish is its ability to generate the neccessary infrastructure to allow these 
micro-services to communicate with one another.  In order to do, Jellyfish must reference a __deployment model__.  A 
deployment refines an existing __logical model__.  In our example, the model of the `SoftwareStore` in the file
`SoftwareStore.sd` is that logical model.

Creating an actual deployment model is out of the scope of this book, but just know we have created such a deployment
model named `LocalSoftwareStore`.  This model resides in the same `com.helpco.helpdesk` package as the `SoftwareStore`
model.  The model configures a deployment of the `SoftwareStore` system so all of the services can run on a single host.
This is useful for development and for our example.

Jellyfish can generate a project __without__ a deployment model.  However, Jellyfish won't be able to generate some of
the infrastructure configuration.  This will be left to the development team.  As a result, the generated project won't
run out of the box.  We do include a deployment model, we can run our services out of the box.

## Configuring an SD Project for Gradle
Our final step is to configure this SD project to use Gradle.  We do this so we can upload the SD project to a remote
Maven repository.  This is useful because Jellyfish can then download the project when generating projects.  To do this,
we need to create two files in the SD project directory.  This is the directory that contains the `src` directory.

First, we'll create a **settings.gradle** file.  This file controls the name of the project.  We'll name our project
`helpdesk.descriptor`:

**settings.gradle**
```groovy
rootProject.name = 'helpdesk.descriptor'
```

Next, we need to create a file named `build.gradle`:

**build.gradle**
```groovy
buildscript {
    repositories {
        mavenLocal()

        maven {
            url nexusConsolidated
        }
    }
    
    dependencies {
        classpath 'com.ngc.seaside:jellyfish.cli.gradle.plugins:2.13.0'
    }
}

apply plugin: 'com.ngc.seaside.jellyfish.system-descriptor'

group = 'com.ngc.seaside'
version = '1.0.0-SNAPSHOT'
```

We're using a group ID of 'com.ngc.seaside'.  Group IDs are used to organize groups of projects.  Typically, a single
organization uses a single group ID.  We're also using version 1.0.0-SNAPSHOT which means this version of the model is
a __preview__ of the 1.0.0 release (which has not been published yet).

**Gradle wrapper**
```note-info
If you are in an envionrment where you can't run the Gradle wrapper, skip the next step.  Install Gradle manaully
instead.  Instead of running Gradle with the wrapper script, use the gradle command directly.  IE, replace ./gradlew 
with just gradle.
```

Finally, we need to setup our project to use the Gradle.  For convenience, you can find the neccessary files
[here](https://nexusrepomgr.ms.northgrum.com/repository/raw-ng-repo/ceacide%2Fgradle-wrapper-swcoe.zip).  Extract the 
contents of this ZIP directly to the project's directory.  If using Linux, run `chmod a+x gradlew` to make the `gradlew`
script executable.

It's now possible to run the command `./gradlew clean build`.  This command will install Gradle if Gradle is not already
installed.  This will also automatically run `jellyfish validate` to make sure the project is valid.  Modelers can now
use Gradle to completey control the project and build it.  Once the build is finished, inspect the `build/libs`
directory to find two ZIP files.  One ZIP contains the System Descriptor files and the other contains the feature files.
Users can run `./gradlew clean build` at any time to rebuild the project.

When we are finished, the directory structure should look like this:

**SD project structure**
```plaintext
 - src/
   - main/
     - sd/
   - test/
     - gherkin/
 - build.gradle
 - settings.gradle
 - gradle/
   - wrapper/
     - gradle-wrapper.jar
     - gradle-wrapper.properties
```

## Deploying an SD Project
**gradle.properties is required**
```note-warning
This setup requires a properly configured gradle.properties file.  Please see Ch. 1 for details.
```

Once Gradle is setup, we can upload our project to a remote repository.  Use the command `./gradlew upload` to do this.

# Generating a Micro-Service Project
We can now finally run Jellyfish.  Since the `SoftwareStore` system contains four sub-components which we are going to
implement as micro-services, we will run Jellyfish four times to generate four independent services.  First, we'll run
Jellyfish to generate the service project for the `SoftwareRequestService`.  

When we run Jellyfish we need to specify the ID of the System Descriptor project that contains the models.  Since we
have uploaded the project to a remote repository, _we don't actually need a local copy of the SD project_ to run 
Jellyfish.  We just need the _group ID_, _artifact ID_, and _version_ or _GAV_ of the project.  The group ID and version were
defined in the `build.gradle` file of the SD project:

**build.gradle**
```groovy
group = 'com.ngc.seaside'
version = '1.0.0-SNAPSHOT'
```

The artifact ID is the name of the project.  This was given in the `settings.gradle` file of the SD project:

**settings.gradle**
```groovy
rootProject.name = 'helpdesk.descriptor'
```

When we provide the GAV to Jellyfish, we seperate these values with a colon (`:`).  So the GAV for SD project is
`com.ngc.seaside:helpdesk.descriptor:1.0.0-SNAPSHOT`.

We need to use the `create-java-service-project` command to run create a new service project.  When using this command
we need to include the following arguments:

* `outputDirectory`: This is the directory to store the generated project.  Most of the time, a value of `.` is fine.
* `version`: Jellyfish will set the intial version of the generated project to this value.  Most projects will use
  `1.0.0-SNAPSHOT` as their first version.
* `model`: This is the fully qualified model name to reference when generating the project.  These are usually models
  that have a stereotype of `service`.
* `deploymentModel`: This is the deployment of the containing system to reference when generating the infrastructure.
  As noted previously, this is not required but allows us to have a running service out of the box.

We can generate the service project for the `SoftwareRequestService` model like this.  Note we are using the `\` 
character to break the long lines so the command line treats the contents as a single line.

**Generating the SoftwareRequestService project**
```
$> jellyfish create-java-service-project \
 outputDirectory=. \
 gav=com.ngc.seaside:helpdesk.descriptor:1.0.0-SNAPSHOT \
 model=com.helpco.helpdesk.SoftwareRequestService \
 deploymentModel=com.helpco.helpdesk.LocalSoftwareStore \
 version=1.0.0-SNAPSHOT
```

When this command completes, a new directory named `com.helpco.helpdesk.softwarestore` should be created.  This contains
the service project.  We'll explore the structure of this project in the next chapter.

We can now run Jellyfish to generate projects for the remaining sub-components.  Only the value of the `model` argument
needs to change.

**Generating the LicenseService project**
```
$> jellyfish create-java-service-project \
 outputDirectory=. \
 gav=com.ngc.seaside:helpdesk.descriptor:1.0.0-SNAPSHOT \
 model=com.helpco.helpdesk.LicenseService \
 deploymentModel=com.helpco.helpdesk.LocalSoftwareStore \
 version=1.0.0-SNAPSHOT
```

**Generating the PcManagementService project**
```
$> jellyfish create-java-service-project \
 outputDirectory=. \
 gav=com.ngc.seaside:helpdesk.descriptor:1.0.0-SNAPSHOT \
 model=com.helpco.helpdesk.PcManagementService \
 deploymentModel=com.helpco.helpdesk.LocalSoftwareStore \
 version=1.0.0-SNAPSHOT
```

**Generating the EmailService project**
```
$> jellyfish create-java-service-project \
 outputDirectory=. \
 gav=com.ngc.seaside:helpdesk.descriptor:1.0.0-SNAPSHOT \
 model=com.helpco.helpdesk.EmailService \
 deploymentModel=com.helpco.helpdesk.LocalSoftwareStore \
 version=1.0.0-SNAPSHOT
```

## Configuring Code Generation
By default, Jellyfish will use the name of the model to name the JAR/bundle and Java packages of the generated code.  If
this is not desireable, a modeler can include the `codegen` metadata to override the JAR and Java package names:

**Configuring the names of generated files**
```
model SoftwareRequestService {
  metadata {
     "stereotypes" : ["service"],
     "codegen": {
	   "alias": "srs"
	 }
  }
}
```

Now Jellyfish will refer to the `SoftwareRequestService` as `com.helpco.helpdesk.srs` when naming the JAR file and 
Java package.

# Generating a System Project
At this point, we have generated all the required services for the `SoftwareStore` system.  These services represent all
the components that will be deployed to realize this system in production.  However, we can use an additional Jellyfish
command to generate a project for the entire `SoftwareStore` system.  This system will _not_ contain any deployable 
code.  Instead, it contains the following:
1. A project can be used to test the entire system together.  This project is used to run tests against the full 
`SoftwareStore` system with all of its services deploye.d
1. A project that can be used to package all of the services for the system together.  This is used for convenience to
make it easy to produce a single ZIP file that contains all of services of the system.  

In order to generate this project, we need to run the command `create-java-system-project`:

**Generating a SoftwareSystem project**
```
$> jellyfish create-java-system-project \
 outputDirectory=. \
 gav=com.ngc.seaside:helpdesk.descriptor:1.0.0-SNAPSHOT \
 model=com.helpco.helpdesk.SoftwareStore \
 deploymentModel=com.helpco.helpdesk.LocalSoftwareStore \
 version=1.0.0-SNAPSHOT
```

When we run this command, we use `com.helpco.helpdesk.SoftwareStore` as the value of the `model` argument.  This is 
because we want to generate project for the top level system, not just a service.

We should now have a new directory named `com.helpco.helpdesk.softwarestore` in the current directory.  This project
contains artifacts for the entire system.

# Conclusion
We'll review the structure of each of these generated projects and learn how to build and run these projects 
in the next chapter.