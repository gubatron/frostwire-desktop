package com.limegroup.gnutella.gui;

import org.limewire.i18n.I18nMarker;

public class TipOfTheDayMessages {
    
    private static final String FIRST_MESSAGE = I18nMarker.marktr("Tired of downloads stopping halfway through? It helps to pick search results with a higher number in the '#' column. The # is the amount of unique places on the network that are hosting the file. The more sources, the better the chance of getting your file! In case a junk result appears to have a lot of sources, use the <font color=\"185ea8\">Junk</font> button to train FrostWire's junk filter.");

    /**
     * Determines whether or not the current locale language is English. Note
     * that the user setting may be empty, defaulting to the running system
     * locale which may be other than English. Here we check the effective
     * locale seen in the MessagesBundle.
     */
    static boolean hasLocalizedMessages() {
        return GUIMediator.isEnglishLocale() || !FIRST_MESSAGE.equals(I18n.tr(FIRST_MESSAGE));
    }   

    /**
     * Returns general tips that are shown on all operating systems.
     */
    public static String[] getGeneralMessages() {
        return new String[] {
                I18n.tr(FIRST_MESSAGE),
                I18n.tr("When your downloads say <font color=\"185ea8\">Needs More Sources</font>, try choosing that download and then clicking <font color=\"185ea8\">Find More Sources</font>. FrostWire will search the network for more places to download the file and automatically resume downloading if it finds any matches."),
                I18n.tr("You can change the look and feel of FrostWire by going to View &gt; Apply Skins and choosing a new skin. Brand new skins are on FrostWire's website, available by choosing 'Get More Skins'. If you download skins by the 'Download into FrostWire' links, FrostWire will automatically ask if you want to use the new skin when the download completes."),
                I18n.tr("You can sort your search results by clicking on a column. The most useful column to sort by is the '#' column, which can put the results that have the highest chance of downloading at the top of the list."),
                I18n.tr("The more stars a search result has, the better the chances of the download completing successfully! Some search results may even appear with four blue stars and a speed of 'Ethernet'. These results have the greatest chance of completing successfully."),
                I18n.tr("You can find out how many times someone has uploaded a file by choosing the 'Library' tab and looking at the Uploads column. The information is listed as 'X / Y', where 'Y' is the number of people that have attempted to upload that file, and 'X' is the number of people that have successfully uploaded it."),
                I18n.tr("Curious how many people are searching for your files? The 'hits' column in the Library will count how many times FrostWire has returned a result from someone searching for that file."),
                I18n.tr("It helps the network if you keep your FrostWire running. Others will connect to the network easier and searches will perform better."),
                I18n.tr("When FrostWire says your download is 'Waiting In Line', that means that source you are downloading from is temporarily busy and has queued your download. In order to successfully complete your download, wait while your request advances in line and is eventually serviced."),
                I18n.tr("Need more help? Visit <a href=\"{0}\">our message boards</a> and see if others have already answered your question! You can also review the <a href=\"{1}\">FAQ</a> for answers to commonly asked questions or the <a href=\"{2}\">User Guide</a> for more information."),
                I18n.tr("Accidentally killed your download? Click the library tab, choose the Incomplete folder, and then highlight the filename of the file you'd like to resume downloading and click resume. FrostWire will automatically search for that file and begin downloading it again."),
                I18n.tr("Do you have a tip for effectively using FrostWire? Add it to <a href=\"{0}\">the wiki</a> and it may make it into future Tips of the Day!"),
                I18n.tr("You can restart downloads that say <font color=\"185ea8\">Awaiting Sources</font> by searching for that file again. FrostWire will automatically restart the download if it finds any matching results."),
                I18n.tr("Do you have a cool feature you'd like to see added to FrostWire? Suggest it on FrostWire's <a href=\"{0}\">forum</a> and it may be added to future versions!"),
                I18n.tr("You can make your own FrostWire skins! Visit <a href=\"{0}\">FrostWire's website</a> for detailed information about how skins are created."),
                I18n.tr("You can preview downloaded files to make sure they're what you expect. Just select the download and click 'Preview'!"),
                I18n.tr("Curious what hosts FrostWire is connecting or connected to? Enable the Connections tab from View &gt; Show/Hide, and take a look at FrostWire's connections."),
                I18n.tr("Passionate about digital rights? Visit the <a href=\"{0}\">Electronic Frontier Foundation</a> and see what you can do to help."),
                I18n.tr("FrostWire is translated into many different languages including Chinese, French, German, Japanese, Italian, Spanish and many more. Visit FrostWire's <a href=\"{0}\">internationalization page</a> for information on how you can help translation efforts!"),
                I18n.tr("Sport the FrostWire look! Visit our <a href=\"{0}\">FrostWire gear</a> page and purchase a t-shirt or stickers!"),
                I18n.tr("Try an audio genre search!<br>Click on the 'Audio' Metadata search window, choose a genre from the drop-down box, and then press 'Search'. Most if not all of the files returned will be from the selected genre."),
                I18n.tr("Don't know what to look for? Try a <font color=\"185ea8\">What's New</font> search and see what's recently been added to the network."),
                I18n.tr("Bombarded with nonsense search results? Select the search result, and then click the Junk button. As you mark search results as Junk or Not Junk, FrostWire learns what to block and what not to block. After a short training period, FrostWire automatically blocks most junk results. Try it!"),
                I18n.tr("Small variations in the search title will still work. For example, if your buddy is sharing 'Frosty' but you searched for 'My Frosty', your buddy's file will still be found."),
                I18n.tr("To download a file with BitTorrent, just click a link to a .torrent file on the Web, or drag a .torrent file to FrostWire's Downloads section."),
                I18n.tr("Want to share a file on your desktop with FrostWire? Simply drag the file or folder from your desktop to the FrostWire Library. Select Individually Shared Files to see your newly shared file in the Library."),
                I18n.tr("Search results in FrostWire come from other users like you. Search results marked with a blue frosty icon in the Quality column are official communications from FrostWire, LLC."),
                I18n.tr("After starting a search, you can customize the three boxes on the left with titles like Media, Artist, and Album. You can change what these boxes show. Click the circle in the upper left corner of a box, and choose a new option from the menu."),
                I18n.tr("Do you want to browse your friend's FrostWire Library? Go to Search &gt; Direct Connect, then enter the ip address and port number of your friend's computer in the following format: 'ip address:port'. The port can be found in FrostWire's Options in the Advanced &gt; Firewall Config section."),
                I18n.tr("If you try to download a file that you already have, FrostWire lets you know, even if the file has a different name."),
                I18n.tr("Curious what people are searching for? Go to the Monitor tab, and select Show Incoming Searches to see other people's search queries that are entering your FrostWire. Double-click an incoming query to perform a search for that keyword."),
                I18n.tr("Have a lot of files in your Saved or Shared folder? " + 
                        "Try a <font color=\"185ea8\">Search in Shared files</font>" + 
                        " search at the top of the <font color=\"185ea8\">Library" + 
                        "</font> tab to find a file."),
                I18n.tr("You can publish your original work on the Gnutella network with a Creative Commons license. Select the .ogg or .mp3 shared file in the Library and click the Publish button. Visit the <a href=\"{0}\">creative commons website</a> for more information."),
                I18n.tr("Have you downloaded a file with incomplete or totally wrong information about its contents? Select the file in the Library and click Describe. Edit the file's metadata to your heart's content. You can even edit many files at once by selecting more than one file before clicking Describe."),
                I18n.tr("Are you behind a firewall? At the bottom of FrostWire in the status bar, look for the globe. If there is a brick wall in front of it, your Internet connection is firewalled."),
                I18n.tr("Wondering how many files you are sharing? Look in FrostWire's status bar at the number in a green oval. A beige oval means FrostWire is still building your Library. A red oval means files are not being shared."),
                I18n.tr("The numbers next to the up and down arrows in the status bar at the bottom of FrostWire show how fast all of your files are downloading or uploading combined."),
                I18n.tr("You can increase the text size via <font " + 
                        "color=\"185ea8\">View</font> &gt; <font " + 
                        "color=\"185ea8\">Increase Font Size</font>."),
                I18n.tr("Instead of putting all your files in a single folder, FrostWire can automatically sort them by media type. In the Options &gt; Saving window, select a Media Type like Audio, and click the Browse button. You can group files by media type in the Library even if all saved files are in one folder."),
                I18n.tr("While FrostWire is running, your audio and video files are automatically shared with iTunes and other programs that support the DAAP protocol. You can disable this sharing in the Options window by going to iTunes &gt; Sharing."),
                I18n.tr("Your search queries travel through a network of interconnected computers, which is why it takes time to receive all of the search results. FrostWire often takes shortcuts to deliver results faster, but if you think search results are blocked because your computer has problems with UDP packets, you can disable OOB (out-of-band) searching in the Options for more reliable results."), 
                I18n.tr("If you own the copyright to a file that is being shared on the network, and you don't want it to be shared, you can have it filtered out of the network search results. <a href=\"{0}\">More information</a> is available.", "http://www.frostwire.com/about/copyright.php"),
                I18n.tr("Unlike other peer-to-peer file-sharing programs, FrostWire can transfer files even if both parties are behind a firewall. You don't have to do anything extra because it happens automatically!"),
                I18n.tr("FrostWire uses a technology called DHT to help users better find rare files. In order to be eligible for inclusion in the DHT, make sure you are not behind a firewall, and leave FrostWire running for as long as possible."),
                I18n.tr("Want to share a large file? FrostWire supports file sizes up to 1 TB (terabyte)."), 
                I18n.tr("Want to be an Ultrapeer? Leave FrostWire running as long as possible. If you have a fast connection without a firewall, you will be promoted to Ultrapeer status after a while. Make sure that you have not disabled Ultrapeer capabilties in the Options."), 
                I18n.tr("Want to know more detailed information about the inner workings of FrostWire? You can view the same statistics that the developers use by choosing Tools &gt; Advanced &gt; Statistics, choosing 'Advanced', and opting to view Advanced Statistics."),
                I18n.tr("Wondering if someone browsed your shared library? Go to View &gt; Show/Hide &gt; Logging to see the recent activity."),
                I18n.tr("Ultrapeers help the network by distributing only pertinent network traffic to the leaf nodes."),             
                I18n.tr("Magnet links allow users to download files through FrostWire from a web page. When you put a magnet link on your web page (in the 'href' attribute of anchor tags), and a user clicks the link, a download will start in FrostWire."), 
        };
    }
    
    /**
     * Returns general tips that are shown on operating systems that are <b>not</b>
     * Mac OS X. Useful for tips that reference the About Window or the
     * Preferences Window, or right-clicking
     */
    public static String[] getNonMacOSXMessages() {
        return new String[] {
                I18n.tr("You can customize FrostWire to your heart's content by changing various preferences such as your Save Folder, Shared Folder, Upload Bandwidth, etc... These preferences (and more) are available at the Tools &gt; Options menu."),
                I18n.tr("You can find out which version of FrostWire you are using by choosing 'About FrostWire' from the Help menu."),
                I18n.tr("Be a good network participant, don't close FrostWire if someone is uploading from you. You can tell FrostWire to automatically close after all downloads and uploads are complete by going to Tools &gt; Options and looking at the 'Shutdown' options."),
                I18n.tr("FrostWire does not automatically share all types of files. If you have a specific file you'd like to share, make sure its extension is being shared. This can be done via Tools &gt; Options and altering the 'Sharing' options."),
                I18n.tr("You can turn autocomplete for searching on or off by choosing 'Options' from the 'Tools' menu, and changing the value of 'Autocomplete Text' under the 'View' option."),
                I18n.tr("Want to see dialogs for questions which you previously marked 'Do not display this message again' or 'Always Use This Answer'? Go to Tools &gt; Options, and check 'Revert To Default' under View &gt; Popups."),
                I18n.tr("You can ban certain words from appearing in your search results by choosing 'Options' from the 'Tools' menu and adding new words to those listed under Filters &gt; Keywords."),
                I18n.tr("You can change the columns that display search result information. Columns you can choose include, 'Title', 'Genre', 'Track', 'Length', and many more. Just right-click on the search result column headers, choose audio or video, and check the columns you want to see!"),
                I18n.tr("Hate tool tips? Love tool tips? You can turn them on or off in most tables by right-clicking on the column headers and choosing 'More Options'. You can toggle other options here too, like whether or not to sort tables automatically and if you prefer the rows to be striped."),
                I18n.tr("You can sort uploads, downloads, etc..., by clicking on a column. The table keeps resorting as the information changes. You can turn this automatic-sorting behavior off by right-clicking on a column header, choosing 'More Options' and un-checking 'Sort Automatically'."),
                I18n.tr("You can tell FrostWire which downloads to start next. Right-click on the column headers in the download area and check the 'Priority' column. Select the download you want to start next and click the up arrow until it's at the top!"),
                I18n.tr("FrostWire is written in Java, which means that having the latest version of Java will improve FrostWire's speed and stability. You can find the most recent Java release at <a href=\"{0}\">www.java.com</a>.", "http://www.java.com/"),
                I18n.tr("Do you reach your search limit and all the results are spam or not related to your search? Try using the keyword filter by going to Tools &gt; Options &gt; Filters &gt; Keywords. Any search result that contains a word from the keyword filter will not even reach your computer."),
                I18n.tr("Are you unhappy with the small number of search results you received? Right-click a search result, then choose Search More, then Get More Results."),
                I18n.tr("Can't connect to the Gnutella network? If you are using a firewall, make sure that a port is opened for FrostWire (both incoming and outgoing, UPD and TCP). Go to Tools &gt; Options &gt; Advanced &gt; Firewall Config to find the port number."),
                I18n.tr("You can save individual downloads to custom locations. Right-click a search result and choose Download As. You can even change the location of a download in progress by right-clicking the download and choosing Change File Location."),
                I18n.tr("Did you just download a file that you don't want to share? In the Library, right-click it and choose Stop Sharing File."),
                I18n.tr("Did you know you can save different types of files, such as audio, video, or images, in different folders on your computer automatically? Just go to Tools &gt; Options &gt; Saving to choose the location for each type of file."),
                I18n.tr("Make FrostWire's Junk filter block more results by making the filter stricter. Adjust that and more by going to Tools &gt; Options &gt; Filters &gt; Junk."),
                I18n.tr("Do you like trying out new product features as soon as they are released? Feedback on new features is always welcomed and appreciated! Go to Tools &gt; Options &gt; Updates to get notified of beta releases."),
        };
    }


    /**
     * Returns general tips that are shown on Mac OS X.
     */
    public static String[] getMacOSXMessages() {
        return new String[] {
                I18n.tr("FrostWire is written in Java, which means that having the latest version of Java will improve FrostWire's speed and stability. You can find the most recent Java release at <a href=\"{0}\">www.java.com</a>.","http://www.java.com"),
                I18n.tr("Tired of the OSX Aqua look? Try using a new theme, available from the View &gt; Apply Themes menu."),
                I18n.tr("You can customize FrostWire to your heart's content by changing various preferences such as your Save Folder, Shared Folder, Upload Bandwidth, etc... These preferences (and more) are available at the Tools &gt; Options menu."),
                I18n.tr("You can find out which version of FrostWire you are using by choosing 'About FrostWire' from the FrostWire menu."),
                I18n.tr("Be a good network participant, don't close FrostWire if someone is uploading from you. You can tell FrostWire to automatically close after all downloads and uploads are complete by going to Tools &gt; Options and looking at the 'Shutdown' options."),
                I18n.tr("FrostWire does not automatically share all types of files. If you have a specific file you'd like to share, make sure its extension is being shared. This can be done via FrostWire &gt; Preferences and altering the 'Sharing' options."),
                I18n.tr("Want to see where on your computer a shared file is? Go to the Library, highlight the file and click 'Explore'. FrostWire will open that folder in the Finder."),
                I18n.tr("Using OSX's built-in firewall? Make sure that FrostWire's port is opened by clicking on the Apple Menu &gt; System Preferences &gt; Sharing &gt; Firewall &gt; New &gt; and choosing to open 'Gnutella/FrostWire (6346)'."),
                I18n.tr("You can turn autocomplete for searching on or off by choosing 'Preferences' from the 'FrostWire' menu, and changing the value of 'Autocomplete Text' under the 'View' option."),
                I18n.tr("Want to see dialogs for questions which you previously marked 'Do not display this message again' or 'Always Use This Answer'? Go to FrostWire &gt; Preferences, and check 'Revert To Default' under View &gt; Popups."),                                           
                I18n.tr("You can ban certain words from appearing in your search results by choosing 'Preferences' from the 'FrostWire' menu and adding new words to those listed under Filters &gt; Keywords."),
                I18n.tr("You can change the columns that display search result information. Columns you can choose from include, 'Title', 'Genre', 'Track', 'Length', and many more. Just control-click on the search result column headers, choose audio or video, and check the columns you want to see!"),
                I18n.tr("Hate tool tips? Love tool tips? You can turn them on or off in most tables by control-clicking on the column headers and choosing 'More Options'. You can toggle other options here too, like whether or not to sort tables automatically and if you prefer the rows to be striped."),
                I18n.tr("You can sort uploads, downloads, etc..., by clicking on a column. The table keeps resorting as the information changes. You can turn this automatic-sorting behavior off by control-clicking on a column header, choosing 'More Options' and un-checking 'Sort Automatically'."),
                I18n.tr("Don't want to see the Chat (or other) columns in downloads? Control-click on the column headers and uncheck that column. You can also drag the columns around to make them appear in the order you'd like."),
                I18n.tr("Do you reach your search limit and all the results are spam or not related to your search? Try using the keyword filter by going to FrostWire &gt; Preferences &gt; Filters &gt; Keywords. Any search result that contains a word from the keyword filter will not even reach your computer."),
                I18n.tr("Are you unhappy with the small number of search results you received? Control-click a search result, then choose Search More, then Get More Results."),
                I18n.tr("Be careful not to share sensitive information like tax documents, passwords, etc. If you attempt to share a folder that people generally use to store sensitive data (such as Users, System, Desktop, etc), FrostWire will warn you. To share a single file instead of the whole folder, just drag that file to FrostWire's Library."),
                I18n.tr("You can save individual downloads to custom locations. Control-click a search result and choose Download As. You can even change the location of a download in progress by control-clicking the download and choosing Change File Location."),
                I18n.tr("Did you just download a file that you don't want to share? In the Library, control-click it and choose 'Stop Sharing File'."),
                I18n.tr("FrostWire's Library is a file manager, not just an MP3 Playlist. That means that when you delete a file from the Library, you have the option to either permanently delete the file from your computer or move it to the Trash."),
                I18n.tr("Want to play music in your default media player instead of in FrostWire? Control-click the FrostWire status bar and deselect 'Show Media Player'."),
                I18n.tr("Did you know you can save different types of files, such as audio, video, or images, in different folders on your computer automatically? Just go to FrostWire &gt; Preferences &gt; Saving to choose the location for each type of file."),
                I18n.tr("Make FrostWire's Junk filter block more results by making the filter stricter. Adjust that and more by going to FrostWire &gt; Preferences &gt; Filters &gt; Junk."),               
        };
      // Not available in Frostwire 4.17
     /*
 I18n.tr("Do you like trying out new product features as soon as they are released? Feedback on new features is always welcomed and appreciated! Go to FrostWire &gt; Preferences &gt; Updates to get notified of beta releases."),
*/
    }

    /**
     * Returns general tips that are shown on Windows.
     */
    public static String[] getWindowsMessages() {
    // This tip is not available, it's useless may cause panic in some users?
    /*     I18n.tr("FrostWire automatically adds itself to the list " + 
                        "of exceptions for your Windows firewall. All " + 
                        "this is done in the background to make your " + 
                        "experience as smooth as possible."), */
        return new String[] {           
                I18n.tr("Want FrostWire to look like your other Windows programs? Choose the Windows theme, available from the View &gt; Apply Themes menu."),
                I18n.tr("Want to play music in your default media player instead of in FrostWire? Right-click the FrostWire status bar and deselect 'Show Media Player'."),
                I18n.tr("Want to see where on your computer a shared file is? Go to the Library, highlight the file and click 'Explore'. FrostWire will open that folder in the Explorer."),
                I18n.tr("Be careful not to share sensitive information like tax documents, passwords, etc. If you attempt to share a folder that people generally use to store sensitive data (such as My Documents, Desktop, Program Files, etc), FrostWire will warn you. To share a single file instead of the whole folder, just drag that file to FrostWire's Library."),
                I18n.tr("The icons that you see next to your search results in the '?' column are symbols of the program used to open that particular type of file. To change the program associated with a file, go to the Folder Options in Windows Explorer (from the Tools menu). This is a Windows setting, not a FrostWire setting."),
                I18n.tr("FrostWire's Library is a file manager, not just an MP3 Playlist. That means that when you delete a file from the Library, you have the option to either permanently delete the file from your computer or move it to the Recycle Bin."),
                I18n.tr("Set FrostWire to be your default program for opening magnet links and .torrent files for the smoothest experience using FrostWire with the web. Go to Tools &gt; Options &gt; Advanced &gt; File Associations."),
                I18n.tr("When you close FrostWire, it minimizes to the system tray. To exit, right-click the system tray icon (next to the time), and select Exit. You can change this behavior by going to Tools &gt; Options &gt; System Tray."),
        };
    }

    /**
     * Returns general tips that are shown on Linux.
     */
    public static String[] getLinuxMessages() {
        return new String[] {
                I18n.tr("Be careful not to share sensitive information like tax documents, passwords, etc. If you attempt to share a folder that people generally use to store sensitive data (such as bin, dev, home, etc), FrostWire will warn you. To share a single file instead of the whole folder, just drag that file to FrostWire's Library."),
                I18n.tr("Want to play music in your preferred media player instead of in FrostWire? Right-click the FrostWire status bar and deselect 'Show Media Player'. Then go to Tools &gt; Options &gt; Helper Apps to set the program of your choice."),
        };
    }

    /**
     * Returns general tips that are shown operating systems other than Windows, Mac OS X or Linux.
     */
    public static String[] getOtherMessages() {
        return new String[] {
        };
    }

    /**
     * Returns general tips that are shown for FrostWire.
     */
    public static String[] getFrostWireMessages() {
        return new String[] {
                I18n.tr("Thank you for using FrostWire"),
                I18n.tr("Visit us at www.frostwire.com"),
        };
    }
}
