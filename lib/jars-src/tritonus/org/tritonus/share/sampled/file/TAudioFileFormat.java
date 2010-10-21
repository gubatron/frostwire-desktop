/*     */ package org.tritonus.share.sampled.file;
/*     */ 
/*     */ import java.util.Collections;
/*     */ import java.util.HashMap;
/*     */ import java.util.Map;
/*     */ import javax.sound.sampled.AudioFileFormat;
/*     */ import javax.sound.sampled.AudioFormat;
/*     */ 
/*     */ public class TAudioFileFormat extends AudioFileFormat
/*     */ {
/*     */   private Map m_properties;
/*     */   private Map m_unmodifiableProperties;
/*     */ 
/*     */   public TAudioFileFormat(AudioFileFormat.Type type, AudioFormat audioFormat, int nLengthInFrames, int nLengthInBytes)
/*     */   {
/*  60 */     super(type, nLengthInBytes, audioFormat, nLengthInFrames);
/*     */   }
/*     */ 
/*     */   public TAudioFileFormat(AudioFileFormat.Type type, AudioFormat audioFormat, int nLengthInFrames, int nLengthInBytes, Map properties)
/*     */   {
/*  73 */     super(type, nLengthInBytes, audioFormat, nLengthInFrames);
/*     */ 
/*  77 */     initMaps(properties);
/*     */   }
/*     */ 
/*     */   private void initMaps(Map properties)
/*     */   {
/*  86 */     this.m_properties = new HashMap();
/*  87 */     this.m_properties.putAll(properties);
/*  88 */     this.m_unmodifiableProperties = Collections.unmodifiableMap(this.m_properties);
/*     */   }
/*     */ 
/*     */   public Map properties()
/*     */   {
/*  94 */     return this.m_unmodifiableProperties;
/*     */   }
/*     */ 
/*     */   protected void setProperty(String key, Object value)
/*     */   {
/* 101 */     this.m_properties.put(key, value);
/*     */   }
/*     */ }
