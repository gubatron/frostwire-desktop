/*     */ package org.tritonus.share.sampled.convert;
/*     */ 
/*     */ import java.io.InputStream;
/*     */ import java.util.Collections;
/*     */ import java.util.HashMap;
/*     */ import java.util.Map;
/*     */ import javax.sound.sampled.AudioFormat;
/*     */ import javax.sound.sampled.AudioInputStream;
/*     */ 
/*     */ public class TAudioInputStream extends AudioInputStream
/*     */ {
/*     */   private Map m_properties;
/*     */   private Map m_unmodifiableProperties;
/*     */ 
/*     */   public TAudioInputStream(InputStream inputStream, AudioFormat audioFormat, long lLengthInFrames)
/*     */   {
/*  60 */     super(inputStream, audioFormat, lLengthInFrames);
/*  61 */     initMaps(new HashMap());
/*     */   }
/*     */ 
/*     */   public TAudioInputStream(InputStream inputStream, AudioFormat audioFormat, long lLengthInFrames, Map properties)
/*     */   {
/*  75 */     super(inputStream, audioFormat, lLengthInFrames);
/*  76 */     initMaps(properties);
/*     */   }
/*     */ 
/*     */   private void initMaps(Map properties)
/*     */   {
/*  85 */     this.m_properties = properties;
/*  86 */     this.m_unmodifiableProperties = Collections.unmodifiableMap(this.m_properties);
/*     */   }
/*     */ 
/*     */   public Map properties()
/*     */   {
/*  98 */     return this.m_unmodifiableProperties;
/*     */   }
/*     */ 
/*     */   protected void setProperty(String key, Object value)
/*     */   {
/* 108 */     this.m_properties.put(key, value);
/*     */   }
/*     */ }
