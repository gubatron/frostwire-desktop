/*    */ package javazoom.spi.mpeg.sampled.file;
/*    */ 
/*    */ import javax.sound.sampled.AudioFileFormat;
/*    */ 
/*    */ public class MpegFileFormatType extends AudioFileFormat.Type
/*    */ {
/* 33 */   public static final AudioFileFormat.Type MPEG = new MpegFileFormatType("MPEG", "mpeg");
/* 34 */   public static final AudioFileFormat.Type MP3 = new MpegFileFormatType("MP3", "mp3");
/*    */ 
/*    */   public MpegFileFormatType(String strName, String strExtension)
/*    */   {
/* 38 */     super(strName, strExtension);
/*    */   }
/*    */ }
