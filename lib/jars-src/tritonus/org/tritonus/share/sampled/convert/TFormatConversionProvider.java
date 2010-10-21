/*     */ package org.tritonus.share.sampled.convert;
/*     */ 
/*     */ import javax.sound.sampled.AudioFormat;
/*     */ import javax.sound.sampled.AudioInputStream;
/*     */ import javax.sound.sampled.spi.FormatConversionProvider;
/*     */ import org.tritonus.share.TDebug;
/*     */ import org.tritonus.share.sampled.AudioFormats;
/*     */ 
/*     */ public abstract class TFormatConversionProvider extends FormatConversionProvider
/*     */ {
/*  46 */   protected static final AudioFormat.Encoding[] EMPTY_ENCODING_ARRAY = new AudioFormat.Encoding[0];
/*  47 */   protected static final AudioFormat[] EMPTY_FORMAT_ARRAY = new AudioFormat[0];
/*     */ 
/*     */   public AudioInputStream getAudioInputStream(AudioFormat.Encoding targetEncoding, AudioInputStream audioInputStream)
/*     */   {
/*  54 */     AudioFormat sourceFormat = audioInputStream.getFormat();
/*  55 */     AudioFormat targetFormat = new AudioFormat(targetEncoding, -1.0F, -1, -1, -1, -1.0F, sourceFormat.isBigEndian());
/*     */ 
/*  63 */     if (TDebug.TraceAudioConverter)
/*     */     {
/*  65 */       TDebug.out("TFormatConversionProvider.getAudioInputStream(AudioFormat.Encoding, AudioInputStream):");
/*  66 */       TDebug.out("trying to convert to " + targetFormat);
/*     */     }
/*  68 */     return getAudioInputStream(targetFormat, audioInputStream);
/*     */   }
/*     */ 
/*     */   public boolean isConversionSupported(AudioFormat targetFormat, AudioFormat sourceFormat)
/*     */   {
/*  88 */     if (TDebug.TraceAudioConverter)
/*     */     {
/*  90 */       TDebug.out(">TFormatConversionProvider.isConversionSupported(AudioFormat, AudioFormat):");
/*  91 */       TDebug.out("class: " + super.getClass().getName());
/*  92 */       TDebug.out("checking if conversion possible");
/*  93 */       TDebug.out("from: " + sourceFormat);
/*  94 */       TDebug.out("to: " + targetFormat);
/*     */     }
/*  96 */     AudioFormat[] aTargetFormats = getTargetFormats(targetFormat.getEncoding(), sourceFormat);
/*  97 */     for (int i = 0; i < aTargetFormats.length; ++i)
/*     */     {
/*  99 */       if (TDebug.TraceAudioConverter)
/*     */       {
/* 101 */         TDebug.out("checking against possible target format: " + aTargetFormats[i]);
/*     */       }
/* 103 */       if ((aTargetFormats[i] == null) || (!AudioFormats.matches(aTargetFormats[i], targetFormat))) {
/*     */         continue;
/*     */       }
/* 106 */       if (TDebug.TraceAudioConverter)
/*     */       {
/* 108 */         TDebug.out("<result=true");
/*     */       }
/* 110 */       return true;
/*     */     }
/*     */ 
/* 113 */     if (TDebug.TraceAudioConverter) {
/* 114 */       TDebug.out("<result=false");
/*     */     }
/* 116 */     return false;
/*     */   }
/*     */ 
/*     */   public AudioFormat getMatchingFormat(AudioFormat targetFormat, AudioFormat sourceFormat)
/*     */   {
/* 131 */     if (TDebug.TraceAudioConverter)
/*     */     {
/* 133 */       TDebug.out(">TFormatConversionProvider.isConversionSupported(AudioFormat, AudioFormat):");
/* 134 */       TDebug.out("class: " + super.getClass().getName());
/* 135 */       TDebug.out("checking if conversion possible");
/* 136 */       TDebug.out("from: " + sourceFormat);
/* 137 */       TDebug.out("to: " + targetFormat);
/*     */     }
/* 139 */     AudioFormat[] aTargetFormats = getTargetFormats(targetFormat.getEncoding(), sourceFormat);
/* 140 */     for (int i = 0; i < aTargetFormats.length; ++i)
/*     */     {
/* 142 */       if (TDebug.TraceAudioConverter)
/*     */       {
/* 144 */         TDebug.out("checking against possible target format: " + aTargetFormats[i]);
/*     */       }
/* 146 */       if ((aTargetFormats[i] == null) || (!AudioFormats.matches(aTargetFormats[i], targetFormat))) {
/*     */         continue;
/*     */       }
/* 149 */       if (TDebug.TraceAudioConverter)
/*     */       {
/* 151 */         TDebug.out("<result=true");
/*     */       }
/* 153 */       return aTargetFormats[i];
/*     */     }
/*     */ 
/* 156 */     if (TDebug.TraceAudioConverter) {
/* 157 */       TDebug.out("<result=false");
/*     */     }
/* 159 */     return null;
/*     */   }
/*     */ }
