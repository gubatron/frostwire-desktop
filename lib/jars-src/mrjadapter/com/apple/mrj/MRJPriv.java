/*     */ package com.apple.mrj;
/*     */ 
/*     */ import java.awt.Menu;
/*     */ import java.awt.MenuItem;
/*     */ import java.io.File;
/*     */ import java.io.FileNotFoundException;
/*     */ import java.io.IOException;
/*     */ 
/*     */ class MRJPriv
/*     */ {
/*     */   static void setDefaultFileType(MRJOSType paramMRJOSType)
/*     */   {
/*     */   }
/*     */ 
/*     */   static void setDefaultFileCreator(MRJOSType paramMRJOSType)
/*     */   {
/*     */   }
/*     */ 
/*     */   static final void setFileTypeAndCreator(File paramFile, MRJOSType paramMRJOSType1, MRJOSType paramMRJOSType2)
/*     */     throws IOException
/*     */   {
/*     */   }
/*     */ 
/*     */   static final void setFileType(File paramFile, MRJOSType paramMRJOSType)
/*     */     throws IOException
/*     */   {
/*     */   }
/*     */ 
/*     */   static final void setFileCreator(File paramFile, MRJOSType paramMRJOSType)
/*     */     throws IOException
/*     */   {
/*     */   }
/*     */ 
/*     */   static final MRJOSType getFileType(File paramFile)
/*     */     throws IOException
/*     */   {
/*  44 */     return new MRJOSType("????");
/*     */   }
/*     */ 
/*     */   static final MRJOSType getFileCreator(File paramFile)
/*     */     throws IOException
/*     */   {
/*  50 */     return new MRJOSType("????");
/*     */   }
/*     */ 
/*     */   static File findFolder(MRJOSType paramMRJOSType)
/*     */     throws FileNotFoundException
/*     */   {
/*  56 */     return new File("");
/*     */   }
/*     */ 
/*     */   static File findFolder(short paramShort, MRJOSType paramMRJOSType)
/*     */     throws FileNotFoundException
/*     */   {
/*  62 */     return new File("");
/*     */   }
/*     */ 
/*     */   static File findFolder(short paramShort, MRJOSType paramMRJOSType, boolean paramBoolean)
/*     */     throws FileNotFoundException
/*     */   {
/*  68 */     return new File("");
/*     */   }
/*     */ 
/*     */   static File findApplication(MRJOSType paramMRJOSType)
/*     */     throws FileNotFoundException
/*     */   {
/*  74 */     return new File("");
/*     */   }
/*     */ 
/*     */   static void openURL(String paramString)
/*     */     throws IOException
/*     */   {
/*     */   }
/*     */ 
/*     */   static final void registerAboutHandler(MRJAboutHandler paramMRJAboutHandler)
/*     */   {
/*     */   }
/*     */ 
/*     */   static final void registerOpenApplicationHandler(MRJOpenApplicationHandler paramMRJOpenApplicationHandler)
/*     */   {
/*     */   }
/*     */ 
/*     */   static final void registerOpenDocumentHandler(MRJOpenDocumentHandler paramMRJOpenDocumentHandler)
/*     */   {
/*     */   }
/*     */ 
/*     */   static final void registerPrintDocumentHandler(MRJPrintDocumentHandler paramMRJPrintDocumentHandler)
/*     */   {
/*     */   }
/*     */ 
/*     */   static final void registerQuitHandler(MRJQuitHandler paramMRJQuitHandler)
/*     */   {
/*     */   }
/*     */ 
/*     */   static final void registerPrefsHandler(MRJPrefsHandler paramMRJPrefsHandler)
/*     */   {
/*     */   }
/*     */ 
/*     */   static final void setMenuItemCmdKey(Menu paramMenu, int paramInt, char paramChar)
/*     */   {
/*     */   }
/*     */ 
/*     */   static final void setMenuItemCmdKey(MenuItem paramMenuItem, char paramChar)
/*     */   {
/*     */   }
/*     */ 
/*     */   static final boolean isMRJToolkitAvailable()
/*     */   {
/*     */     try
/*     */     {
/* 118 */       Class localClass = Class.forName("com.apple.mrj.MRJShellLibrary");
/* 119 */       if (localClass != null)
/* 120 */         return true;
/*     */     } catch (Exception localException) {
/*     */     } catch (Error localError) {
/*     */     }
/* 124 */     return false;
/*     */   }
/*     */ 
/*     */   static boolean setFileLastModified(File paramFile, long paramLong)
/*     */   {
/* 129 */     return false;
/*     */   }
/*     */ }
