/*     */ package org.tritonus.share.sampled.convert;
/*     */ 
/*     */ import java.util.Collection;
/*     */ import java.util.Iterator;
/*     */ import javax.sound.sampled.AudioFormat;
/*     */ import org.tritonus.share.ArraySet;
/*     */ import org.tritonus.share.TDebug;
/*     */ import org.tritonus.share.sampled.AudioFormats;
/*     */ 
/*     */ public abstract class TSimpleFormatConversionProvider extends TFormatConversionProvider
/*     */ {
/*     */   private Collection m_sourceEncodings;
/*     */   private Collection m_targetEncodings;
/*     */   private Collection m_sourceFormats;
/*     */   private Collection m_targetFormats;
/*     */ 
/*     */   protected TSimpleFormatConversionProvider(Collection sourceFormats, Collection targetFormats)
/*     */   {
/*  74 */     this.m_sourceEncodings = new ArraySet();
/*  75 */     this.m_targetEncodings = new ArraySet();
/*  76 */     this.m_sourceFormats = sourceFormats;
/*  77 */     this.m_targetFormats = targetFormats;
/*  78 */     collectEncodings(this.m_sourceFormats, this.m_sourceEncodings);
/*  79 */     collectEncodings(this.m_targetFormats, this.m_targetEncodings);
/*     */   }
/*     */ 
/*     */   protected void disable()
/*     */   {
/*  90 */     if (TDebug.TraceAudioConverter) TDebug.out("TSimpleFormatConversionProvider.disable(): disabling " + super.getClass().getName());
/*  91 */     this.m_sourceEncodings = new ArraySet();
/*  92 */     this.m_targetEncodings = new ArraySet();
/*  93 */     this.m_sourceFormats = new ArraySet();
/*  94 */     this.m_targetFormats = new ArraySet();
/*     */   }
/*     */ 
/*     */   private static void collectEncodings(Collection formats, Collection encodings)
/*     */   {
/* 102 */     Iterator iterator = formats.iterator();
/* 103 */     while (iterator.hasNext())
/*     */     {
/* 105 */       AudioFormat format = (AudioFormat)iterator.next();
/* 106 */       encodings.add(format.getEncoding());
/*     */     }
/*     */   }
/*     */ 
/*     */   public AudioFormat.Encoding[] getSourceEncodings()
/*     */   {
/* 114 */     return (AudioFormat.Encoding[])this.m_sourceEncodings.toArray(EMPTY_ENCODING_ARRAY);
/*     */   }
/*     */ 
/*     */   public AudioFormat.Encoding[] getTargetEncodings()
/*     */   {
/* 121 */     return (AudioFormat.Encoding[])this.m_targetEncodings.toArray(EMPTY_ENCODING_ARRAY);
/*     */   }
/*     */ 
/*     */   public boolean isSourceEncodingSupported(AudioFormat.Encoding sourceEncoding)
/*     */   {
/* 129 */     return this.m_sourceEncodings.contains(sourceEncoding);
/*     */   }
/*     */ 
/*     */   public boolean isTargetEncodingSupported(AudioFormat.Encoding targetEncoding)
/*     */   {
/* 137 */     return this.m_targetEncodings.contains(targetEncoding);
/*     */   }
/*     */ 
/*     */   public AudioFormat.Encoding[] getTargetEncodings(AudioFormat sourceFormat)
/*     */   {
/* 150 */     if (isAllowedSourceFormat(sourceFormat))
/*     */     {
/* 152 */       return getTargetEncodings();
/*     */     }
/*     */ 
/* 156 */     return EMPTY_ENCODING_ARRAY;
/*     */   }
/*     */ 
/*     */   public AudioFormat[] getTargetFormats(AudioFormat.Encoding targetEncoding, AudioFormat sourceFormat)
/*     */   {
/* 170 */     if (isConversionSupported(targetEncoding, sourceFormat))
/*     */     {
/* 172 */       return (AudioFormat[])this.m_targetFormats.toArray(EMPTY_FORMAT_ARRAY);
/*     */     }
/*     */ 
/* 176 */     return EMPTY_FORMAT_ARRAY;
/*     */   }
/*     */ 
/*     */   protected boolean isAllowedSourceEncoding(AudioFormat.Encoding sourceEncoding)
/*     */   {
/* 184 */     return this.m_sourceEncodings.contains(sourceEncoding);
/*     */   }
/*     */ 
/*     */   protected boolean isAllowedTargetEncoding(AudioFormat.Encoding targetEncoding)
/*     */   {
/* 191 */     return this.m_targetEncodings.contains(targetEncoding);
/*     */   }
/*     */ 
/*     */   protected boolean isAllowedSourceFormat(AudioFormat sourceFormat)
/*     */   {
/* 198 */     Iterator iterator = this.m_sourceFormats.iterator();
/* 199 */     while (iterator.hasNext())
/*     */     {
/* 201 */       AudioFormat format = (AudioFormat)iterator.next();
/* 202 */       if (AudioFormats.matches(format, sourceFormat))
/*     */       {
/* 204 */         return true;
/*     */       }
/*     */     }
/* 207 */     return false;
/*     */   }
/*     */ 
/*     */   protected boolean isAllowedTargetFormat(AudioFormat targetFormat)
/*     */   {
/* 214 */     Iterator iterator = this.m_targetFormats.iterator();
/* 215 */     while (iterator.hasNext())
/*     */     {
/* 217 */       AudioFormat format = (AudioFormat)iterator.next();
/* 218 */       if (AudioFormats.matches(format, targetFormat))
/*     */       {
/* 220 */         return true;
/*     */       }
/*     */     }
/* 223 */     return false;
/*     */   }
/*     */ 
/*     */   protected Collection getCollectionSourceEncodings()
/*     */   {
/* 228 */     return this.m_sourceEncodings;
/*     */   }
/*     */ 
/*     */   protected Collection getCollectionTargetEncodings() {
/* 232 */     return this.m_targetEncodings;
/*     */   }
/*     */ 
/*     */   protected Collection getCollectionSourceFormats() {
/* 236 */     return this.m_sourceFormats;
/*     */   }
/*     */ 
/*     */   protected Collection getCollectionTargetFormats() {
/* 240 */     return this.m_targetFormats;
/*     */   }
/*     */ 
/*     */   protected static boolean doMatch(int i1, int i2)
/*     */   {
/* 251 */     return (i1 == -1) || (i2 == -1) || (i1 == i2);
/*     */   }
/*     */ 
/*     */   protected static boolean doMatch(float f1, float f2)
/*     */   {
/* 261 */     return (f1 == -1.0F) || (f2 == -1.0F) || (Math.abs(f1 - f2) < 1.E-09D);
/*     */   }
/*     */ 
/*     */   protected AudioFormat replaceNotSpecified(AudioFormat sourceFormat, AudioFormat targetFormat)
/*     */   {
/* 283 */     boolean bSetSampleSize = false;
/* 284 */     boolean bSetChannels = false;
/* 285 */     boolean bSetSampleRate = false;
/* 286 */     boolean bSetFrameRate = false;
/* 287 */     if ((targetFormat.getSampleSizeInBits() == -1) && (sourceFormat.getSampleSizeInBits() != -1))
/*     */     {
/* 289 */       bSetSampleSize = true;
/*     */     }
/* 291 */     if ((targetFormat.getChannels() == -1) && (sourceFormat.getChannels() != -1))
/*     */     {
/* 293 */       bSetChannels = true;
/*     */     }
/* 295 */     if ((targetFormat.getSampleRate() == -1.0F) && (sourceFormat.getSampleRate() != -1.0F))
/*     */     {
/* 297 */       bSetSampleRate = true;
/*     */     }
/* 299 */     if ((targetFormat.getFrameRate() == -1.0F) && (sourceFormat.getFrameRate() != -1.0F))
/*     */     {
/* 301 */       bSetFrameRate = true;
/*     */     }
/* 303 */     if ((bSetSampleSize) || (bSetChannels) || (bSetSampleRate) || (bSetFrameRate) || ((targetFormat.getFrameSize() == -1) && (sourceFormat.getFrameSize() != -1)))
/*     */     {
/* 307 */       float sampleRate = (bSetSampleRate) ? sourceFormat.getSampleRate() : targetFormat.getSampleRate();
/*     */ 
/* 309 */       float frameRate = (bSetFrameRate) ? sourceFormat.getFrameRate() : targetFormat.getFrameRate();
/*     */ 
/* 311 */       int sampleSize = (bSetSampleSize) ? sourceFormat.getSampleSizeInBits() : targetFormat.getSampleSizeInBits();
/*     */ 
/* 313 */       int channels = (bSetChannels) ? sourceFormat.getChannels() : targetFormat.getChannels();
/*     */ 
/* 315 */       int frameSize = getFrameSize(targetFormat.getEncoding(), sampleRate, sampleSize, channels, frameRate, targetFormat.isBigEndian(), targetFormat.getFrameSize());
/*     */ 
/* 323 */       targetFormat = new AudioFormat(targetFormat.getEncoding(), sampleRate, sampleSize, channels, frameSize, frameRate, targetFormat.isBigEndian());
/*     */     }
/*     */ 
/* 332 */     return targetFormat;
/*     */   }
/*     */ 
/*     */   protected int getFrameSize(AudioFormat.Encoding encoding, float sampleRate, int sampleSize, int channels, float frameRate, boolean bigEndian, int oldFrameSize)
/*     */   {
/* 354 */     if ((sampleSize == -1) || (channels == -1)) {
/* 355 */       return -1;
/*     */     }
/* 357 */     return sampleSize * channels / 8;
/*     */   }
/*     */ }
