/*    */ package org.tritonus.share.sampled;
/*    */ 
/*    */ import java.util.Collections;
/*    */ import java.util.HashMap;
/*    */ import java.util.Map;
/*    */ import javax.sound.sampled.AudioFormat;
/*    */ 
/*    */ public class TAudioFormat extends AudioFormat
/*    */ {
/*    */   private Map m_properties;
/*    */   private Map m_unmodifiableProperties;
/*    */ 
/*    */   public TAudioFormat(AudioFormat.Encoding encoding, float sampleRate, int sampleSizeInBits, int channels, int frameSize, float frameRate, boolean bigEndian, Map properties)
/*    */   {
/* 49 */     super(encoding, sampleRate, sampleSizeInBits, channels, frameSize, frameRate, bigEndian);
/*    */ 
/* 56 */     initMaps(properties);
/*    */   }
/*    */ 
/*    */   public TAudioFormat(float sampleRate, int sampleSizeInBits, int channels, boolean signed, boolean bigEndian, Map properties)
/*    */   {
/* 67 */     super(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
/*    */ 
/* 72 */     initMaps(properties);
/*    */   }
/*    */ 
/*    */   private void initMaps(Map properties)
/*    */   {
/* 82 */     this.m_properties = new HashMap();
/* 83 */     this.m_properties.putAll(properties);
/* 84 */     this.m_unmodifiableProperties = Collections.unmodifiableMap(this.m_properties);
/*    */   }
/*    */ 
/*    */   public Map properties()
/*    */   {
/* 91 */     return this.m_unmodifiableProperties;
/*    */   }
/*    */ 
/*    */   protected void setProperty(String key, Object value)
/*    */   {
/* 98 */     this.m_properties.put(key, value);
/*    */   }
/*    */ }
