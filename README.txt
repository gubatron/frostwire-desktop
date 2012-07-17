Hi there FrostWire Hacker.

-=WHAT IS FROSTWIRE=-

FrostWire is a file sharing client and media management tool that was made 
using lots of cool open source projects. It was born from the legendary 
LimeWire Gnutella client, but it's evolved a hell of a lot since then.

FrostWire no longer supports Gnutella, it's a BitTorrent client, an Internet 
Radio client and Media Player.

Unlike most BitTorrent clients out there, FrostWire focuses on searching files 
and tries hard to make it as easy and convenient as possible to users.
 
Old FrostWire users were used to the Gnutella experience (searching for 
single files), so FrostWire makes use of BitTorrent a little differently to 
make it simple for them. 

FrostWire will connect to all the major BitTorrent indexes of the internet 
and pre-fetch torrents (via the Azureus DHT or via HTTP if it can't find it 
on the DHT), it will then index locally all the available metadata that's 
indexed by the torrent file, as the user searches, the local index gets better
and better to yield richer and instant results. 

This makes FrostWire a very powerful client that will help you find the rarest
of files on the bittorrent network, sometimes it will find files that even the
best BitTorrent indexes won't yield in the search results.

The main software architecture (how things are organized) depends on the late 
LimeWire 4, the BitTorrent power comes from the Azureus project (aka Vuze), 
media playback comes from the mplayer project, the good looks and skinning 
system comes from the Substance skinning project (which we've had to maintain 
on our repo to make it fit FrostWire needs), http interaction comes from the 
Apache Commons project, the search is built using the awesome H2 database and 
Lucene indexes, JSON parsing comes from google-gson, and so on and so on.

-=BUILD REQUIREMENTS=-

Introductions aside, here's how you build this.

1. Make sure your CLASSPATH, JAVA_HOME and your PATH variables are set 
   correctly.

Example of CLASSPATH, JAVA_HOME and PATH on a Ubuntu system's .bashrc file

JAVA_HOME=/usr/lib/jvm/java-7-sun
CLASSPATH=${CLASSPATH}:${JAVA_HOME}/lib
PATH=${PATH}:${JAVA_HOME}/bin

export JAVA_HOME CLASSPATH PATH

Must build problems are usually solved by having those environment variables 
set correctly. If you are a Windows or Mac user the process is fairly similar.

2. Try having the latest JDK available (OpenJDK or Sun's JDK should do it - 
   As of this document it can be built using Java 1.7)

3. ant

4. Mercurial to clone, check out the project to your machine.

We recommend using Eclipse as your development environment.

-=GET THE SOURCE=-
hg clone https://bitbucket.org/frostwire/frostwire.desktop

-=Get the latest updates from the main repository by issuing:=-
hg pull -u

This will pull the latest changes and automatically merge them with your local
copy of the repository. 

-=HOW TO BUILD=-

cd frostwire.desktop
ant clean
ant

-=HOW TO RUN=-

./run

-=HAVING ISSUES BUILDING?=-
"My environment variables are fine, my requirements are met, there's an error during the build."

It's very hard that it happens but we might have pushed out a broken build.

If you do have any issues building, please yell on the comments of the 
offending commit log at bitbucket.org so we can address the issue right away.

If the build is not broken, hit us up at the developer forum 
http://bit.ly/y0tr40

-=HOW CODE IS ORGANIZED=-

core/ Search, mp3 parsing, Json Engine, mplayer integration.

gui/  Everything the user sees on screen is here. Like Java Swing? 
      this is probably a great place to learn more about it.
      If you're going to be adding new UI elements make sure you put them
      inside com.frostwire.gui.* (Most of the stuff on com.limewire.gui are
      legacy code from LimeWire)

      Good starting points to see how it all works are the *Mediator.java
      files. Being the McDaddy GUIMediator.java

components/ This is the new school of thought in the process. Everytime
            we create new functionality we try to make it self containable
            and we put the code inside a component folder.
	    Two good examples are the azureus core, and the core code for the
            Library which is kept under "alexandria" (in honor to the Library
            of Alexandria)

components/resources This is where most graphical assets are stored.

lib/jars This is where we keep pre-compiled jars from projects we don't 
         maintain.

lib/jars-src This is where we keep the sources of those third party projects.
             We do this because we hope one day we'll be accepted into
             debian or ubuntu, and it's a requirement that your packages
             can be compiled without any binary dependency.
             This also helps us help those projects, sometimes we fix bugs
             that affect us and we send patches back to those projects.
             Also on eclipse it's awesome to be able to browse the source
             of those dependencies and to step-by-step debug to see what
             the hell those developers were thinking.

lib/messagebundles  Where we keep the translation files.

lib/icons  Where we keep the FrostWire launcher icons for the different
           operating systems.

splashes/  Where we keep all the splash screens for each major version of
           FrostWire. There are tools there to build the splash.jar and
           to build a collage of pictures with all the splashes for a release. 

-=SOURCE CODING STYLE=-

If you're using Eclipse, we suggest you use the "eclipse.formatter.xml" on your
project. We try to stick as much as we remember to the Google Java code style
except for a few things we don't like because we all work on eclipse and we do
have monitors with over 1200 pixels of width.	 

-=OBJECT ORIENTED MANTRAS=-

5 Object Oriented Programming Principles learned during the last 15 years
http://bit.ly/y0hdR4

Basically, Keep it simple and try not to repeat yourself at all.

-=OFFICIAL FROSTWIRE SITES=-

Main Website
http://frostwire.com

Forum
http://forum.frostwire.com

Git Repository at GitHub.com
https://github.com/frostwire/frostwire-desktop.git

-FrostWire Team
Last updated - July 17th 2012 12:08:50 EST
