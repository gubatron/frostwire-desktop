/*     */ package org.tritonus.share.sampled;
/*     */ 
/*     */ import java.util.Iterator;
/*     */ import javax.sound.sampled.AudioFormat;
/*     */ import javax.sound.sampled.AudioInputStream;
/*     */ 
/*     */ public class AudioUtils
/*     */ {
/*     */   public static long getLengthInBytes(AudioInputStream audioInputStream)
/*     */   {
/*  51 */     return getLengthInBytes(audioInputStream.getFormat(), audioInputStream.getFrameLength());
/*     */   }
/*     */ 
/*     */   public static long getLengthInBytes(AudioFormat audioFormat, long lLengthInFrames)
/*     */   {
/*  78 */     int nFrameSize = audioFormat.getFrameSize();
/*  79 */     if ((lLengthInFrames >= 0L) && (nFrameSize >= 1))
/*     */     {
/*  81 */       return lLengthInFrames * nFrameSize;
/*     */     }
/*     */ 
/*  85 */     return -1L;
/*     */   }
/*     */ 
/*     */   public static boolean containsFormat(AudioFormat sourceFormat, Iterator possibleFormats)
/*     */   {
/*  94 */     while (possibleFormats.hasNext())
/*     */     {
/*  96 */       AudioFormat format = (AudioFormat)possibleFormats.next();
/*  97 */       if (AudioFormats.matches(format, sourceFormat))
/*     */       {
/*  99 */         return true;
/*     */       }
/*     */     }
/* 102 */     return false;
/*     */   }
/*     */ 
/*     */   public static long millis2Bytes(long ms, AudioFormat format)
/*     */   {
/* 110 */     return millis2Bytes(ms, format.getFrameRate(), format.getFrameSize());
/*     */   }
/*     */ 
/*     */   public static long millis2Bytes(long ms, float frameRate, int frameSize) {
/* 114 */     return (long)((float)ms * frameRate / 1000.0F * frameSize);
/*     */   }
/*     */ 
/*     */   public static long millis2BytesFrameAligned(long ms, AudioFormat format)
/*     */   {
/* 121 */     return millis2BytesFrameAligned(ms, format.getFrameRate(), format.getFrameSize());
/*     */   }
/*     */ 
/*     */   public static long millis2BytesFrameAligned(long ms, float frameRate, int frameSize) {
/* 125 */     return (long)((float)ms * frameRate / 1000.0F) * frameSize;
/*     */   }
/*     */ 
/*     */   public static long millis2Frames(long ms, AudioFormat format)
/*     */   {
/* 132 */     return millis2Frames(ms, format.getFrameRate());
/*     */   }
/*     */ 
/*     */   public static long millis2Frames(long ms, float frameRate) {
/* 136 */     return (long)((float)ms * frameRate / 1000.0F);
/*     */   }
/*     */ 
/*     */   public static long bytes2Millis(long bytes, AudioFormat format)
/*     */   {
/* 143 */     return (long)((float)bytes / format.getFrameRate() * 1000.0F / format.getFrameSize());
/*     */   }
/*     */ 
/*     */   public static long frames2Millis(long frames, AudioFormat format)
/*     */   {
/* 150 */     return (long)((float)frames / format.getFrameRate() * 1000.0F);
/*     */   }
/*     */ 
/*     */   public static String NS_or_number(int number)
/*     */   {
/* 156 */     return (number == -1) ? "NOT_SPECIFIED" : String.valueOf(number);
/*     */   }
/*     */   public static String NS_or_number(float number) {
/* 159 */     return (number == -1.0F) ? "NOT_SPECIFIED" : String.valueOf(number);
/*     */   }
/*     */ 
/*     */   public static String format2ShortStr(AudioFormat format)
/*     */   {
/* 166 */     return format.getEncoding() + "-" + NS_or_number(format.getChannels()) + "ch-" + NS_or_number(format.getSampleSizeInBits()) + "bit-" + NS_or_number((int)format.getSampleRate()) + "Hz-" + ((format.isBigEndian()) ? "be" : "le");
/*     */   }
/*     */ }
