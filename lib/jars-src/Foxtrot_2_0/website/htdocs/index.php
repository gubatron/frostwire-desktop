<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>

<head>
<link rel="stylesheet" href="styles.css">
<title>Foxtrot - Easy API for JFC/Swing</title>
<meta name="description" content="Foxtrot - Easy API for JFC/Swing">
<meta name="keywords" content="Foxtrot,Swing,Threads,Swing Threads,SwingWorker,Worker,SwingUtilities,SwingUtilities.invokeLater,invokeLater,AWT,Simone Bordet">
</head>

<body>

<table width="100%" border="0" cellspacing="0" cellpadding="0">

<!-- Header start -->
<tr><td class="header-logo" colspan="3">
<div align="center"><img src="images/logo.gif"></div>
</td></tr>
<tr><td class="header-text" colspan="3">Foxtrot - Easy API for JFC/Swing</td></tr>
<!-- Header end -->

<tr>

<!-- Left Menu start -->
<td class="menu">
<table class="menu-header" width="100%" cellspacing="0" cellpadding="0">
<tr><td class="menu-header">Foxtrot Project</td></tr>
<tr><td>
<div class="menu-title">Community</div>
<div class="menu-entry">
<a href="http://sourceforge.net/projects/foxtrot/">Development Page</a>
</div>
<div class="menu-entry">
<a href="http://sourceforge.net/mail/?group_id=49197">Mailing Lists</a>
</div>

<div class="menu-title">Documentation</div>
<div class="menu-entry">
<a href="docs/toc.php">Foxtrot User's Guide</a>
</div>

<div class="menu-title">Download</div>
<div class="menu-entry">
<a href="http://sourceforge.net/project/showfiles.php?group_id=49197">Latest Release</a>
</div>

<p align="center"><a href="http://sourceforge.net">
<img src="http://sourceforge.net/sflogo.php?group_id=49197&type=4"
width="125" height="37" border="0" alt="SourceForge.net Logo"/></a>
</p>

<!-- Extreme Tracker -->
<p align="center">
<a target="_top" href="http://w.extreme-dm.com/?login=foxtrots">
<img name="im" src="http://w1.extreme-dm.com/i.gif" height="38"
border="0" width="41" alt=""></a><script language="javascript"><!--
an=navigator.appName;d=document;function
pr(){d.write("<img src=\"http://w0.extreme-dm.com",
"/0.gif?tag=foxtrots&j=y&srw="+srw+"&srb="+srb+"&",
"rs="+r+"&l="+escape(d.referrer)+"\" height=1 ",
"width=1>");}srb="na";srw="na";//-->
</script><script language="javascript1.2"><!--
s=screen;srw=s.width;an!="Netscape"?
srb=s.colorDepth:srb=s.pixelDepth;//-->
</script><script language="javascript"><!--
r=41;d.images?r=d.im.width:z=0;pr();//-->
</script><noscript><img height="1" width="1" alt=""
src="http://w0.extreme-dm.com/0.gif?tag=foxtrots&j=n"></noscript>
</p>

</td></tr>
</table>
</td>
<!-- Left Menu end -->

<!-- Content start -->
<td class="content">
<h2>Overview</h2>
<p><b>Foxtrot</b> is an easy and powerful API to use threads with the Java<sup><font size="-2">TM</font></sup>
Foundation Classes (JFC/Swing).</p>
<p>The Foxtrot API are based on a new concept, the <b>Synchronous Model</b>, that allow you to easily integrate
in your Swing code time-consuming operations without incurring in "GUI-freeze" problem, typical of Swing
applications.</p>
<p>While other solutions have been developed to solve this problem, being the
<a href="http://java.sun.com/products/jfc/tsc/articles/threads/threads3.html">SwingWorker</a>
(see also <a href="http://java.sun.com/products/jfc/tsc/articles/threads/update.html">here</a> for an update)
the most known, they are all based on the Asynchronous Model which, for non-trivial Swing applications,
carries several problems such as code asymmetry, bad code readability and difficult exception handling.</p>
<p>The Foxtrot API cleanly solves the problems that solutions based on the Asynchronous Model have, and it's
simpler to use.<br>
Your Swing code will immediately benefit of:
<ul><ul>
<li>code symmetry and readability
<li>easy exception handling
<li>improved mantainability
</ul></ul>
</p>
</td>
<!-- Content end -->

<!-- News start -->
<td class="news">
<h2>News</h2>
<?php include 'http://sourceforge.net/export/projnews.php?group_id=49197&limit=3&flat=0&show_summaries=0';?></p>
</td>
<!-- News end -->

</tr>
</table>

<div align="center">
<img src="images/acnvrule.gif"/>
</div>

<div align="center">
<small>
Last updated on $Date: 2002-03-21 06:52:02 -0500 (Thu, 21 Mar 2002) $. Send feedback to
<a href="mailto:biorn_steedom@users.sourceforge.net">Simone Bordet</a>.
</small>
</div>

</body>
</html>
