/*     */ package org.tritonus.share.sampled.convert;
/*     */ 
/*     */ import java.util.Collection;
/*     */ import java.util.HashMap;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Map.Entry;
/*     */ import java.util.Set;
/*     */ import javax.sound.sampled.AudioFormat;
/*     */ import org.tritonus.share.ArraySet;
/*     */ import org.tritonus.share.sampled.AudioFormats;
/*     */ 
/*     */ public abstract class TMatrixFormatConversionProvider extends TSimpleFormatConversionProvider
/*     */ {
/*     */   private Map m_targetEncodingsFromSourceFormat;
/*     */   private Map m_targetFormatsFromSourceFormat;
/*     */ 
/*     */   protected TMatrixFormatConversionProvider(List sourceFormats, List targetFormats, boolean[][] abConversionPossible)
/*     */   {
/*  85 */     super(sourceFormats, targetFormats);
/*     */ 
/*  87 */     this.m_targetEncodingsFromSourceFormat = new HashMap();
/*  88 */     this.m_targetFormatsFromSourceFormat = new HashMap();
/*     */ 
/*  90 */     int nSourceFormat = 0;
/*  91 */     while (nSourceFormat < sourceFormats.size())
/*     */     {
/*  94 */       AudioFormat sourceFormat = (AudioFormat)sourceFormats.get(nSourceFormat);
/*  95 */       List supportedTargetEncodings = new ArraySet();
/*  96 */       this.m_targetEncodingsFromSourceFormat.put(sourceFormat, supportedTargetEncodings);
/*  97 */       Map targetFormatsFromTargetEncodings = new HashMap();
/*  98 */       this.m_targetFormatsFromSourceFormat.put(sourceFormat, targetFormatsFromTargetEncodings);
/*  99 */       int nTargetFormat = 0;
/* 100 */       while (nTargetFormat < targetFormats.size())
/*     */       {
/* 103 */         AudioFormat targetFormat = (AudioFormat)targetFormats.get(nTargetFormat);
/* 104 */         if (abConversionPossible[nSourceFormat][nTargetFormat] != false)
/*     */         {
/* 106 */           AudioFormat.Encoding targetEncoding = targetFormat.getEncoding();
/* 107 */           supportedTargetEncodings.add(targetEncoding);
/* 108 */           Collection supportedTargetFormats = (Collection)targetFormatsFromTargetEncodings.get(targetEncoding);
/* 109 */           if (supportedTargetFormats == null)
/*     */           {
/* 111 */             supportedTargetFormats = new ArraySet();
/* 112 */             targetFormatsFromTargetEncodings.put(targetEncoding, supportedTargetFormats);
/*     */           }
/* 114 */           supportedTargetFormats.add(targetFormat);
/*     */         }
/* 101 */         ++nTargetFormat;
/*     */       }
/*  92 */       ++nSourceFormat;
/*     */     }
/*     */   }
/*     */ 
/*     */   public AudioFormat.Encoding[] getTargetEncodings(AudioFormat sourceFormat)
/*     */   {
/* 124 */     Iterator iterator = this.m_targetEncodingsFromSourceFormat.entrySet().iterator();
/* 125 */     while (iterator.hasNext())
/*     */     {
/* 127 */       Map.Entry entry = (Map.Entry)iterator.next();
/* 128 */       AudioFormat format = (AudioFormat)entry.getKey();
/* 129 */       if (AudioFormats.matches(format, sourceFormat))
/*     */       {
/* 131 */         Collection targetEncodings = (Collection)entry.getValue();
/* 132 */         return (AudioFormat.Encoding[])targetEncodings.toArray(EMPTY_ENCODING_ARRAY);
/*     */       }
/*     */     }
/*     */ 
/* 136 */     return EMPTY_ENCODING_ARRAY;
/*     */   }
/*     */ 
/*     */   public AudioFormat[] getTargetFormats(AudioFormat.Encoding targetEncoding, AudioFormat sourceFormat)
/*     */   {
/* 154 */     Iterator iterator = this.m_targetFormatsFromSourceFormat.entrySet().iterator();
/* 155 */     while (iterator.hasNext())
/*     */     {
/* 157 */       Map.Entry entry = (Map.Entry)iterator.next();
/* 158 */       AudioFormat format = (AudioFormat)entry.getKey();
/* 159 */       if (AudioFormats.matches(format, sourceFormat))
/*     */       {
/* 161 */         Map targetEncodings = (Map)entry.getValue();
/* 162 */         Collection targetFormats = (Collection)targetEncodings.get(targetEncoding);
/* 163 */         if (targetFormats != null)
/*     */         {
/* 165 */           return (AudioFormat[])targetFormats.toArray(EMPTY_FORMAT_ARRAY);
/*     */         }
/*     */ 
/* 169 */         return EMPTY_FORMAT_ARRAY;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 174 */     return EMPTY_FORMAT_ARRAY;
/*     */   }
/*     */ }
