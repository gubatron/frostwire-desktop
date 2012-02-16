Hi there FrostWire Hacker.

WHAT IS FROSTWIRE.

FrostWire is a file sharing client and media management tool that was made 
using lots of cool open source projects. It was born from the legendary 
LimeWire Gnutella client, but it's evolved a hell of a lot since then.

FrostWire no longer supports Gnutella, it's a BitTorrent client, an Internet 
Radio client and Media Player.

Unlike most BitTorrent clients out there, FrostWire focuses on search files 
and tries hard to make it as easy and convenient as possible to users. 
Old FrostWire users were used to the Gnutella experience (searching for 
single files), so FrostWire makes use of BitTorrent a little differently to 
make it simple for them. 

FrostWire will connect to all the major BitTorrent indexes of the internet 
and pre-fetch torrents (via the Azureus DHT or via HTTP if it can't find it 
on the DHT), it will then index locally all the available metadata that's 
indexed by the torrent file, as the user searches the local index gets better
and better and search results are faster. This makes FrostWire a very powerful
client that will help you find the rarest of files on the bittorrent network, 
sometimes it will find files that even the BitTorrent indexes won't yield as 
search results.

The main software architecture (how things are organized) depends on the late 
LimeWire 4, the BitTorrent power comes from the Azureus project (aka Vuze), 
media playback comes from the mplayer project, the good looks and skinning 
system comes from the Substance skinning project (which we've had to maintain 
on our repo to make it fit FrostWire needs), http interaction comes from the 
Apache Commons project, the search is built using the awesome H2 database and 
Lucene indexes, JSON parsing comes from google-gson, and so on and so on.

BUILD REQUIREMENTS

Introductions aside, here's how you build this.

1. Make sure your CLASSPATH, JAVA_HOME and your PATH variables are set 
   correctly.

2. Try having the latest JDK available (OpenJDK or Sun's JDK should do it - 
   As of this document it can be built using Java 1.7)

3. ant

4. Mercurial to clone, check out the project to your machine.

Example of CLASSPATH, JAVA_HOME and PATH on a Ubuntu system's .bashrc file

JAVA_HOME=/usr/lib/jvm/java-7-sun
CLASSPATH=${CLASSPATH}:${JAVA_HOME}/lib
PATH=${PATH}:${JAVA_HOME}/bin

export JAVA_HOME CLASSPATH PATH

Must build problems are usually solved by having those environment variables set correctly. If you are a Windows or Mac user the process is fairly similar.

CHECK OUT THE PROJECT FROM BITBUCKET
hg clone https://bitbucket.org/frostwire/frostwire.desktop

HOW TO BUILD

cd frostwire.desktop
ant clean
ant

HOW TO RUN

./run

HAVING ISSUES BUILDING?

It's very hard that it happens but we might have pushed a broken build

If you do have any issues building, please yell on the comments of the 
offending commit log at bitbucket.org so we can address the issue right away.

If the build is not broken, hit us up at the developer forum 
http://bit.ly/y0tr40

OFFICIAL FROSTWIRE SITES

Main Website
http://frostwire.com

Forum
http://forum.frostwire.com

Mercurial Repository at BitBucket.org
https://bitbucket.org/frostwire/frostwire.desktop

(We recently started using Git for the FrostWire for Android source, we
 might be moving FrostWire for Desktop there eventually)

FrostWire Team
Last updated - February 16th 2012 16:54:21 EST
