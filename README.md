![alt text](http://biz.prlog.org/frostwire/logo.png "Frostwire")


Hi there FrostWire Hacker

-------------------



FrostWire is a file sharing client and media management tool that was made using
lots of cool open source projects. It was born from the legendary *LimeWire
Gnutella* client, but it's evolved a hell of a lot since then.



FrostWire no longer supports Gnutella,** it's a BitTorrent client, an Internet
Radio client and Media Player.**



Unlike most BitTorrent clients out there, FrostWire focuses on searching files
and tries hard to make it as easy and convenient as possible to users.



Old FrostWire users were used to the Gnutella experience (searching for single
files), so FrostWire makes use of BitTorrent a little differently to make it
simple for them.



FrostWire will connect to all the major BitTorrent indexes of the internet and
pre-fetch torrents (via the Azureus DHT or via HTTP if it can't find it on the
DHT), it will then index locally all the available metadata that's indexed by
the torrent file, as the user searches, the local index gets better and better
to yield richer and instant results.

This makes FrostWire a very powerful client that will help you find the rarest
of files on the bittorrent network, sometimes it will find files that even the
best BitTorrent indexes won't yield in the search results.

The main software architecture (how things are organized) depends on the late
*LimeWire 4,*

    -   The BitTorrent power comes from the Azureus project (aka Vuze)

    -   Media playback comes from the *mplayer* project

    -   The good looks and skinning system comes from the Substance skinning
        project (which we've had to maintain on our repo to make it fit
        FrostWire needs)

    -   HTTP interaction comes from the Apache Commons project

    -   The search is built using the awesome H2 database and Lucene indexes

    -   JSON parsing comes from google-gson, and so on and so on.

    

BUILD REQUIREMENTS 
-------------------



Introductions aside, here's how you build this.

Make sure your **CLASSPATH**, **JAVA_HOME** and your **PATH** variables are set
correctly.

Example on a Ubuntu system's .bashrc file:

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

JAVA_HOME=/usr/lib/jvm/java-7-sun

CLASSPATH=${CLASSPATH}:${JAVA_HOME}/lib

PATH=${PATH}:${JAVA_HOME}/bin

export JAVA_HOME CLASSPATH PATH
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


Must build problems are usually solved by having those environment variables set
correctly. If you are a Windows or Mac user the process is fairly similar.



-   Try having the latest JDK available (OpenJDK or Sun's JDK should do it -
    As of this document it can be built using Java 1.7)

-   ant

-   GIT to clone, check out the project to your machine.



We recommend using [Eclipse][1] as your development environment.

[1]: <http://www.eclipse.org/downloads/>





GETTING THE SOURCE 
-------------------



~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
git clone https://github.com/frostwire/frostwire-desktop.git
cd frostwire-desktop/
git pull
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~



This will pull the latest changes and automatically merge them with your local
copy of the repository.



HOW TO BUILD
------------

Be sure that you're in the project root, else then execute :

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
cd frostwire-desktop/
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

If you're already in project root, simply execute :

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
ant clean
ant
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~



HOW TO RUN
----------

Then run the project with :

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
./run
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~



### HAVING ISSUES BUILDING?

   * "My environment variables are fine, my requirements are met, there's an
    error during the build."*

    It's very hard that it happens but we might have pushed out a broken
    build.

    If you do have any issues building, please yell on the comments of the
    offending commit log at github.com so we can address the issue right
    away.

    If the build is not broken, hit us up at the [Developer Forum][2]

[2]: <http://forum.frostwire.com/>



HOW CODE IS ORGANIZED
---------------------


| Location        | Contains    |
| ------------- |:-------------:| 
| **core/**     | Search, mp3 parsing, Json Engine, mplayer integration. | 
| **gui/**      | Everything the user sees on screen is here.Like Java Swing? this is  probably a great place to learn more about it. If you're going to be adding new UI elements make sure you put them inside **com.frostwire.gui.\*** (Most of the stuff on **com.limewire.gui** are legacy code from LimeWire) Good starting points to see how it all works are the **\*Mediator.java** files. Being the McDaddy **GUIMediator.java** | 
| **components/** | This is the new school of thought in the process. Everytime we create new functionality we try to make it self containable and we put the code inside a component folder. Two good examples are the azureus core, and the core code for the Library which is kept under "alexandria" (in honor to the Library of Alexandria)     | 
| **components/resources** | This is where most graphical assets are stored.      | 
| **lib/jars** | This is where we keep pre-compiled jars from projects we don't maintain.     | 
| **lib/jars-src** | This is where we keep the sources of those third party projects. We do this because we hope one day we'll be accepted into  debian or ubuntu and it's a requirement that your packages  can be compiled without any binary dependency. This also helps us help those projects, sometimes we fix bugs that affect us and we send patches back to those projects.  Also on eclipse it's awesome to be able to browse the source  of those dependencies and to step-by-step debug to see what  the hell those developers were thinking.|
|   **lib/messagebundles**  |   Where we keep the translation files. |
|    **lib/icons**  |  Where we keep the FrostWire launcher icons for the differentoperating systems. |
| **splashes/**  |  Where we keep all the splash screens for each major version of FrostWire. There are tools there to build the splash.jar and to build a collage of pictures with all the splashes for a release. |

 
SOURCE CODING STYLE
---------------------

If you're using Eclipse, we suggest you use the "eclipse.formatter.xml" on your
project. We try to stick as much as we remember to the Google Java code style
except for a few things we don't like because we all work on eclipse and we do
have monitors with over 1200 pixels of width.

OBJECT ORIENTED MANTRAS
-------------------------
[5 Object Oriented Programming Principles learned during the last 15 years][3]
[3]: <http://bit.ly/y0hdR4>

Basically, Keep it simple and try not to repeat yourself at all.

CONTRIBUTE
------------

If you want to contribute code, start by looking at the open issues on
github.com 
[https://github.com/frostwire/frostwire-desktop/issues][4]
[4]: <https://github.com/frostwire/frostwire-desktop/issues>

If you want to fix a new issue that's not listed there, create the issue, see if
we can discuss a solution, if you already have one the best way to proceed is
to:

* Fork the source. 
* Clone it locally 
* Create a branch with a descriptive name of the issue you are solving. 
* Code, Commit, Push, Code, Commit, Push, until you are done with your branch. 
* If you can add tests to demonstrate the issue and the fix, even better. 
* Submit a pull request that's as descriptive as possible. 
* We'll code review you, maybe ask you for some more changes, and after we've tested it we'll merge your changes.

_Repeat and rinse, if you send enough patches to demonstrate you have a good
coding skills, we'll just give you commit access on the real repo and you will
be part of the development team._

OFFICIAL FROSTWIRE SITES
---------------------------

Main Website [Frostwire.com][5]
[5]: <http://www.frostwire.com>

Frostwire [Forum] [6]
[6]: <http://forum.frostwire.com>

[Git Repository at GitHub.com][7]
[7]: <https://github.com/frostwire/frostwire-desktop.git>

Twitter [@frostwire][8]
[8]: <https://twitter.com/frostwire>

[Facebook][9]
[9]: <http://www.facebook.com/FrostWireOfficial>
[Tumblr][10]
[10]: <http://tumblr.frostwire.com>

###### FrostWire Team  Last updated - June 21th 2013 11:58:29 EST
