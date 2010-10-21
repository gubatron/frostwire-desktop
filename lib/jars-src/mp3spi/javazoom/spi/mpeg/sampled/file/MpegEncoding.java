/*    */ package javazoom.spi.mpeg.sampled.file;
/*    */ 
/*    */ import javax.sound.sampled.AudioFormat;
/*    */ 
/*    */ public class MpegEncoding extends AudioFormat.Encoding
/*    */ {
/* 33 */   public static final AudioFormat.Encoding MPEG1L1 = new MpegEncoding("MPEG1L1");
/* 34 */   public static final AudioFormat.Encoding MPEG1L2 = new MpegEncoding("MPEG1L2");
/* 35 */   public static final AudioFormat.Encoding MPEG1L3 = new MpegEncoding("MPEG1L3");
/* 36 */   public static final AudioFormat.Encoding MPEG2L1 = new MpegEncoding("MPEG2L1");
/* 37 */   public static final AudioFormat.Encoding MPEG2L2 = new MpegEncoding("MPEG2L2");
/* 38 */   public static final AudioFormat.Encoding MPEG2L3 = new MpegEncoding("MPEG2L3");
/* 39 */   public static final AudioFormat.Encoding MPEG2DOT5L1 = new MpegEncoding("MPEG2DOT5L1");
/* 40 */   public static final AudioFormat.Encoding MPEG2DOT5L2 = new MpegEncoding("MPEG2DOT5L2");
/* 41 */   public static final AudioFormat.Encoding MPEG2DOT5L3 = new MpegEncoding("MPEG2DOT5L3");
/*    */ 
/*    */   public MpegEncoding(String strName)
/*    */   {
/* 45 */     super(strName);
/*    */   }
/*    */ }
