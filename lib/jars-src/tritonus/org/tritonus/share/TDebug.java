/*     */ package org.tritonus.share;
/*     */ 
/*     */ import java.io.PrintStream;
/*     */ import java.security.AccessControlException;
/*     */ import java.util.StringTokenizer;
/*     */ 
/*     */ public class TDebug
/*     */ {
/*  33 */   public static boolean SHOW_ACCESS_CONTROL_EXCEPTIONS = false;
/*     */   private static final String PROPERTY_PREFIX = "tritonus.";
/*  36 */   public static PrintStream m_printStream = System.out;
/*     */ 
/*  38 */   private static String indent = "";
/*     */ 
/*  41 */   public static boolean TraceAllExceptions = getBooleanProperty("TraceAllExceptions");
/*  42 */   public static boolean TraceAllWarnings = getBooleanProperty("TraceAllWarnings");
/*     */ 
/*  45 */   public static boolean TraceInit = getBooleanProperty("TraceInit");
/*  46 */   public static boolean TraceCircularBuffer = getBooleanProperty("TraceCircularBuffer");
/*  47 */   public static boolean TraceService = getBooleanProperty("TraceService");
/*     */ 
/*  50 */   public static boolean TraceAudioSystem = getBooleanProperty("TraceAudioSystem");
/*  51 */   public static boolean TraceAudioConfig = getBooleanProperty("TraceAudioConfig");
/*  52 */   public static boolean TraceAudioInputStream = getBooleanProperty("TraceAudioInputStream");
/*  53 */   public static boolean TraceMixerProvider = getBooleanProperty("TraceMixerProvider");
/*  54 */   public static boolean TraceControl = getBooleanProperty("TraceControl");
/*  55 */   public static boolean TraceLine = getBooleanProperty("TraceLine");
/*  56 */   public static boolean TraceDataLine = getBooleanProperty("TraceDataLine");
/*  57 */   public static boolean TraceMixer = getBooleanProperty("TraceMixer");
/*  58 */   public static boolean TraceSourceDataLine = getBooleanProperty("TraceSourceDataLine");
/*  59 */   public static boolean TraceTargetDataLine = getBooleanProperty("TraceTargetDataLine");
/*  60 */   public static boolean TraceClip = getBooleanProperty("TraceClip");
/*  61 */   public static boolean TraceAudioFileReader = getBooleanProperty("TraceAudioFileReader");
/*  62 */   public static boolean TraceAudioFileWriter = getBooleanProperty("TraceAudioFileWriter");
/*  63 */   public static boolean TraceAudioConverter = getBooleanProperty("TraceAudioConverter");
/*  64 */   public static boolean TraceAudioOutputStream = getBooleanProperty("TraceAudioOutputStream");
/*     */ 
/*  67 */   public static boolean TraceEsdNative = getBooleanProperty("TraceEsdNative");
/*  68 */   public static boolean TraceEsdStreamNative = getBooleanProperty("TraceEsdStreamNative");
/*  69 */   public static boolean TraceEsdRecordingStreamNative = getBooleanProperty("TraceEsdRecordingStreamNative");
/*  70 */   public static boolean TraceAlsaNative = getBooleanProperty("TraceAlsaNative");
/*  71 */   public static boolean TraceAlsaMixerNative = getBooleanProperty("TraceAlsaMixerNative");
/*  72 */   public static boolean TraceAlsaPcmNative = getBooleanProperty("TraceAlsaPcmNative");
/*  73 */   public static boolean TraceMixingAudioInputStream = getBooleanProperty("TraceMixingAudioInputStream");
/*  74 */   public static boolean TraceOggNative = getBooleanProperty("TraceOggNative");
/*  75 */   public static boolean TraceVorbisNative = getBooleanProperty("TraceVorbisNative");
/*     */ 
/*  78 */   public static boolean TraceMidiSystem = getBooleanProperty("TraceMidiSystem");
/*  79 */   public static boolean TraceMidiConfig = getBooleanProperty("TraceMidiConfig");
/*  80 */   public static boolean TraceMidiDeviceProvider = getBooleanProperty("TraceMidiDeviceProvider");
/*  81 */   public static boolean TraceSequencer = getBooleanProperty("TraceSequencer");
/*  82 */   public static boolean TraceMidiDevice = getBooleanProperty("TraceMidiDevice");
/*     */ 
/*  85 */   public static boolean TraceAlsaSeq = getBooleanProperty("TraceAlsaSeq");
/*  86 */   public static boolean TraceAlsaSeqDetails = getBooleanProperty("TraceAlsaSeqDetails");
/*  87 */   public static boolean TraceAlsaSeqNative = getBooleanProperty("TraceAlsaSeqNative");
/*  88 */   public static boolean TracePortScan = getBooleanProperty("TracePortScan");
/*  89 */   public static boolean TraceAlsaMidiIn = getBooleanProperty("TraceAlsaMidiIn");
/*  90 */   public static boolean TraceAlsaMidiOut = getBooleanProperty("TraceAlsaMidiOut");
/*  91 */   public static boolean TraceAlsaMidiChannel = getBooleanProperty("TraceAlsaMidiChannel");
/*     */ 
/*  94 */   public static boolean TraceAlsaCtlNative = getBooleanProperty("TraceAlsaCtlNative");
/*  95 */   public static boolean TraceCdda = getBooleanProperty("TraceCdda");
/*  96 */   public static boolean TraceCddaNative = getBooleanProperty("TraceCddaNative");
/*     */ 
/*     */   public static void out(String strMessage)
/*     */   {
/* 103 */     if ((strMessage.length() > 0) && (strMessage.charAt(0) == '<')) {
/* 104 */       if (indent.length() > 2)
/* 105 */         indent = indent.substring(2);
/*     */       else {
/* 107 */         indent = "";
/*     */       }
/*     */     }
/* 110 */     String newMsg = null;
/* 111 */     if ((indent != "") && (strMessage.indexOf("\n") >= 0)) {
/* 112 */       newMsg = "";
/* 113 */       StringTokenizer tokenizer = new StringTokenizer(strMessage, "\n");
/* 114 */       while (tokenizer.hasMoreTokens())
/* 115 */         newMsg = newMsg + indent + tokenizer.nextToken() + "\n";
/*     */     }
/*     */     else {
/* 118 */       newMsg = indent + strMessage;
/*     */     }
/* 120 */     m_printStream.println(newMsg);
/* 121 */     if ((strMessage.length() > 0) && (strMessage.charAt(0) == '>'))
/* 122 */       indent += "  ";
/*     */   }
/*     */ 
/*     */   public static void out(Throwable throwable)
/*     */   {
/* 130 */     throwable.printStackTrace(m_printStream);
/*     */   }
/*     */ 
/*     */   public static void assertion(boolean bAssertion)
/*     */   {
/* 137 */     if (bAssertion)
/*     */       return;
/* 139 */     throw new AssertException();
/*     */   }
/*     */ 
/*     */   private static boolean getBooleanProperty(String strName)
/*     */   {
/* 163 */     String strPropertyName = "tritonus." + strName;
/* 164 */     String strValue = "false";
/*     */     try
/*     */     {
/* 167 */       strValue = System.getProperty(strPropertyName, "false");
/*     */     }
/*     */     catch (AccessControlException e)
/*     */     {
/* 171 */       if (SHOW_ACCESS_CONTROL_EXCEPTIONS)
/*     */       {
/* 173 */         out(e);
/*     */       }
/*     */     }
/*     */ 
/* 177 */     boolean bValue = strValue.toLowerCase().equals("true");
/*     */ 
/* 179 */     return bValue;
/*     */   }
/*     */ 
/*     */   public static class AssertException extends RuntimeException
/*     */   {
/*     */     public AssertException()
/*     */     {
/*     */     }
/*     */ 
/*     */     public AssertException(String sMessage)
/*     */     {
/* 155 */       super(sMessage);
/*     */     }
/*     */   }
/*     */ }

