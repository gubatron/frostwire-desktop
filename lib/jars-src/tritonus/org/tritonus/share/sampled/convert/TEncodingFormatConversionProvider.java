/*     */ package org.tritonus.share.sampled.convert;
/*     */ 
/*     */ import java.util.Collection;
/*     */ import java.util.Iterator;
/*     */ import javax.sound.sampled.AudioFormat;
/*     */ import javax.sound.sampled.AudioFormat;
/*     */ import org.tritonus.share.ArraySet;
/*     */ import org.tritonus.share.TDebug;
/*     */ 
/*     */ public abstract class TEncodingFormatConversionProvider extends TSimpleFormatConversionProvider
/*     */ {
/*     */   protected TEncodingFormatConversionProvider(Collection sourceFormats, Collection targetFormats)
/*     */   {
/*  69 */     super(sourceFormats, targetFormats);
/*     */   }
/*     */ 
/*     */   public AudioFormat[] getTargetFormats(AudioFormat.Encoding targetEncoding, AudioFormat sourceFormat)
/*     */   {
/*  95 */     if (TDebug.TraceAudioConverter) {
/*  96 */       TDebug.out(">TEncodingFormatConversionProvider.getTargetFormats(AudioFormat.Encoding, AudioFormat):");
/*  97 */       TDebug.out("checking if conversion possible");
/*  98 */       TDebug.out("from: " + sourceFormat);
/*  99 */       TDebug.out("to: " + targetEncoding);
/*     */     }
/* 101 */     if (isConversionSupported(targetEncoding, sourceFormat))
/*     */     {
/* 103 */       ArraySet result = new ArraySet();
/* 104 */       Iterator iterator = getCollectionTargetFormats().iterator();
/* 105 */       while (iterator.hasNext()) {
/* 106 */         AudioFormat targetFormat = (AudioFormat)iterator.next();
/* 107 */         targetFormat = replaceNotSpecified(sourceFormat, targetFormat);
/* 108 */         result.add(targetFormat);
/*     */       }
/* 110 */       if (TDebug.TraceAudioConverter) {
/* 111 */         TDebug.out("< returning " + result.size() + " elements.");
/*     */       }
/* 113 */       return (AudioFormat[])result.toArray(EMPTY_FORMAT_ARRAY);
/*     */     }
/* 115 */     if (TDebug.TraceAudioConverter) {
/* 116 */       TDebug.out("< returning empty array.");
/*     */     }
/* 118 */     return EMPTY_FORMAT_ARRAY;
/*     */   }
/*     */ }

