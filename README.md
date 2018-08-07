# Smallpeice Robotics Computer Vision & AI Lecture: "Learning to See"

This Github repository stores the interactive slides for the Computer Vision & AI lecture I gave for the Smallpeice Robotics Summer School at the [University of Southampton](http://www.soton.ac.uk). The slides were built using [OpenIMAJ](http://www.openimaj.org).

From this page you can get the source-code for the presentation, which you can build yourself following the instructions below. If you just want to run the presentation, you can download the latest version of the pre-compiled runnable jar from [here](http://jenkins.ecs.soton.ac.uk/job/Smallpeice-Lecture/lastBuild/artifact/target/SmallpeiceLearningToSee-1.0-SNAPSHOT-jar-with-dependencies.jar). 

## Operating the presentation
You'll need a recent version of [Java](http://www.oracle.com/technetwork/java/index.html) installed to run the presentation. Most of the interactive demos require a webcam to be plugged in, although the actual presentation should launch without one. 

On most systems you should just be able to double click the jar file for it to launch. You can make it full screen by pressing "f" (press again to exit). For the presentations you can use the left and right arrow keys to navigate. Note that on some of the interactive slides, you might need to click on the slide background for the arrow keys to work if you clicked on any controls other than buttons.

## Building & running the code
You need to have [Apache Maven](http://maven.apache.org) installed to build the code (it should work with Maven 2 or Maven 3). Fork or clone the repository (or download the source [zip](https://github.com/jonhare/Smallpeice-Lecture/archive/master.zip)) & then from the command line navigate to the root directory of the source tree. Run `mvn install assembly:assembly` to build the presentation, and use `java -jar target/Vision101-1.0-SNAPSHOT-jar-with-dependencies.jar` to launch the presentation.
