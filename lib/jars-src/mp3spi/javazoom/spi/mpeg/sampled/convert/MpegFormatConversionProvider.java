/*     */ package javazoom.spi.mpeg.sampled.convert;
/*     */ 
/*     */ import java.util.Arrays;
/*     */ import java.util.Collection;
/*     */ import javax.sound.sampled.AudioFormat;
/*     */ import javax.sound.sampled.AudioInputStream;
/*     */ import javazoom.spi.mpeg.sampled.file.MpegEncoding;
/*     */ import org.tritonus.share.TDebug;
/*     */ import org.tritonus.share.sampled.Encodings;
/*     */ import org.tritonus.share.sampled.convert.TEncodingFormatConversionProvider;
/*     */ 
/*     */ public class MpegFormatConversionProvider extends TEncodingFormatConversionProvider
/*     */ {
/*  45 */   private static final AudioFormat.Encoding MP3 = Encodings.getEncoding("MP3");
/*  46 */   private static final AudioFormat.Encoding PCM_SIGNED = Encodings.getEncoding("PCM_SIGNED");
/*     */ 
/*  49 */   private static final AudioFormat[] INPUT_FORMATS = { 
/*  51 */     new AudioFormat(MP3, -1.0F, -1, 1, -1, -1.0F, false), 
/*  52 */     new AudioFormat(MP3, -1.0F, -1, 1, -1, -1.0F, true), 
/*  54 */     new AudioFormat(MP3, -1.0F, -1, 2, -1, -1.0F, false), 
/*  55 */     new AudioFormat(MP3, -1.0F, -1, 2, -1, -1.0F, true) };
/*     */ 
/*  60 */   private static final AudioFormat[] OUTPUT_FORMATS = { 
/*  62 */     new AudioFormat(PCM_SIGNED, -1.0F, 16, 1, 2, -1.0F, false), 
/*  63 */     new AudioFormat(PCM_SIGNED, -1.0F, 16, 1, 2, -1.0F, true), 
/*  65 */     new AudioFormat(PCM_SIGNED, -1.0F, 16, 2, 4, -1.0F, false), 
/*  66 */     new AudioFormat(PCM_SIGNED, -1.0F, 16, 2, 4, -1.0F, true) };
/*     */ 
/*     */   public MpegFormatConversionProvider()
/*     */   {
/*  74 */     super((Collection)Arrays.asList(INPUT_FORMATS), (Collection)Arrays.asList(OUTPUT_FORMATS));
/*  75 */     if (!TDebug.TraceAudioConverter)
/*     */       return;
/*  77 */     TDebug.out(">MpegFormatConversionProvider()");
/*     */   }
/*     */ 
/*     */   public AudioInputStream getAudioInputStream(AudioFormat targetFormat, AudioInputStream audioInputStream)
/*     */   {
/*  83 */     if (TDebug.TraceAudioConverter)
/*     */     {
/*  85 */       TDebug.out(">MpegFormatConversionProvider.getAudioInputStream(AudioFormat targetFormat, AudioInputStream audioInputStream):");
/*     */     }
/*  87 */     return new DecodedMpegAudioInputStream(targetFormat, audioInputStream);
/*     */   }
/*     */ 
/*     */   public boolean isConversionSupported(AudioFormat targetFormat, AudioFormat sourceFormat)
/*     */   {
/*  98 */     if (TDebug.TraceAudioConverter)
/*     */     {
/* 100 */       TDebug.out(">MpegFormatConversionProvider.isConversionSupported(AudioFormat targetFormat, AudioFormat sourceFormat):");
/* 101 */       TDebug.out("checking if conversion possible");
/* 102 */       TDebug.out("from: " + sourceFormat);
/* 103 */       TDebug.out("to: " + targetFormat);
/*     */     }
/*     */ 
/* 106 */     boolean conversion = super.isConversionSupported(targetFormat, sourceFormat);
/* 107 */     if (!conversion)
/*     */     {
/* 109 */       AudioFormat.Encoding enc = sourceFormat.getEncoding();
/* 110 */       if ((enc instanceof MpegEncoding) && ((
/* 112 */         (sourceFormat.getFrameRate() != -1.0F) || (sourceFormat.getFrameSize() != -1))))
/*     */       {
/* 114 */         conversion = true;
/*     */       }
/*     */     }
/*     */ 
/* 118 */     return conversion;
/*     */   }
/*     */ }

