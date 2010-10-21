/*     */ package com.apple.mrj;
/*     */ 
/*     */ import java.io.File;
/*     */ import java.io.FileNotFoundException;
/*     */ import java.io.IOException;
/*     */ 
/*     */ public class MRJFileUtils
/*     */ {
/*  91 */   public static final MRJOSType kSystemFolderType = new MRJOSType(1835098995);
/*  92 */   public static final MRJOSType kDesktopFolderType = new MRJOSType(1684370283);
/*  93 */   public static final MRJOSType kTrashFolderType = new MRJOSType(1953657704);
/*  94 */   public static final MRJOSType kWhereToEmptyTrashFolderType = new MRJOSType(1701671028);
/*  95 */   public static final MRJOSType kPrintMonitorDocsFolderType = new MRJOSType(1886547572);
/*  96 */   public static final MRJOSType kStartupFolderType = new MRJOSType(1937011316);
/*  97 */   public static final MRJOSType kShutdownFolderType = new MRJOSType(1936221286);
/*  98 */   public static final MRJOSType kAppleMenuFolderType = new MRJOSType(1634561653);
/*  99 */   public static final MRJOSType kControlPanelFolderType = new MRJOSType(1668575852);
/* 100 */   public static final MRJOSType kExtensionFolderType = new MRJOSType(1702392942);
/* 101 */   public static final MRJOSType kFontsFolderType = new MRJOSType(1718578804);
/* 102 */   public static final MRJOSType kPreferencesFolderType = new MRJOSType(1886545254);
/* 103 */   public static final MRJOSType kChewableItemsFolderType = new MRJOSType(1718382196);
/* 104 */   public static final MRJOSType kTemporaryFolderType = new MRJOSType(1952804208);
/*     */ 
/*     */   public static boolean setFileLastModified(File paramFile, long paramLong)
/*     */   {
/*  13 */     return MRJPriv.setFileLastModified(paramFile, paramLong);
/*     */   }
/*     */ 
/*     */   public static void setDefaultFileType(MRJOSType paramMRJOSType)
/*     */   {
/*  18 */     MRJPriv.setDefaultFileType(paramMRJOSType);
/*     */   }
/*     */ 
/*     */   public static void setDefaultFileCreator(MRJOSType paramMRJOSType)
/*     */   {
/*  23 */     MRJPriv.setDefaultFileCreator(paramMRJOSType);
/*     */   }
/*     */ 
/*     */   public static final void setFileTypeAndCreator(File paramFile, MRJOSType paramMRJOSType1, MRJOSType paramMRJOSType2)
/*     */     throws IOException
/*     */   {
/*  29 */     MRJPriv.setFileTypeAndCreator(paramFile, paramMRJOSType1, paramMRJOSType2);
/*     */   }
/*     */ 
/*     */   public static final void setFileType(File paramFile, MRJOSType paramMRJOSType)
/*     */     throws IOException
/*     */   {
/*  35 */     MRJPriv.setFileType(paramFile, paramMRJOSType);
/*     */   }
/*     */ 
/*     */   public static final void setFileCreator(File paramFile, MRJOSType paramMRJOSType)
/*     */     throws IOException
/*     */   {
/*  41 */     MRJPriv.setFileCreator(paramFile, paramMRJOSType);
/*     */   }
/*     */ 
/*     */   public static final MRJOSType getFileType(File paramFile)
/*     */     throws IOException
/*     */   {
/*  47 */     return MRJPriv.getFileType(paramFile);
/*     */   }
/*     */ 
/*     */   public static final MRJOSType getFileCreator(File paramFile)
/*     */     throws IOException
/*     */   {
/*  53 */     return MRJPriv.getFileCreator(paramFile);
/*     */   }
/*     */ 
/*     */   public static File findFolder(MRJOSType paramMRJOSType)
/*     */     throws FileNotFoundException
/*     */   {
/*  59 */     return MRJPriv.findFolder(paramMRJOSType);
/*     */   }
/*     */ 
/*     */   public static File findFolder(short paramShort, MRJOSType paramMRJOSType)
/*     */     throws FileNotFoundException
/*     */   {
/*  65 */     return MRJPriv.findFolder(paramShort, paramMRJOSType);
/*     */   }
/*     */ 
/*     */   public static File findFolder(short paramShort, MRJOSType paramMRJOSType, boolean paramBoolean)
/*     */     throws FileNotFoundException
/*     */   {
/*  71 */     return MRJPriv.findFolder(paramShort, paramMRJOSType, paramBoolean);
/*     */   }
/*     */ 
/*     */   public static File findApplication(MRJOSType paramMRJOSType)
/*     */     throws FileNotFoundException
/*     */   {
/*  78 */     return MRJPriv.findApplication(paramMRJOSType);
/*     */   }
/*     */ 
/*     */   public static void openURL(String paramString)
/*     */     throws IOException
/*     */   {
/*  84 */     MRJPriv.openURL(paramString);
/*     */   }
/*     */ }
