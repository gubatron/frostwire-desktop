/*     */ package org.tritonus.share.sampled;
/*     */ 
/*     */ import java.util.Iterator;
/*     */ import javax.sound.sampled.AudioFormat;
/*     */ import javax.sound.sampled.AudioSystem;
/*     */ import org.tritonus.share.StringHashedSet;
/*     */ 
/*     */ public class Encodings extends AudioFormat.Encoding
/*     */ {
/*  69 */   private static StringHashedSet encodings = new StringHashedSet();
/*     */ 
/*     */   Encodings(String name)
/*     */   {
/*  80 */     super(name);
/*     */   }
/*     */ 
/*     */   public static AudioFormat.Encoding getEncoding(String name)
/*     */   {
/* 112 */     AudioFormat.Encoding res = (AudioFormat.Encoding)encodings.get(name);
/* 113 */     if (res == null)
/*     */     {
/* 115 */       res = new Encodings(name);
/*     */ 
/* 117 */       encodings.add(res);
/*     */     }
/* 119 */     return res;
/*     */   }
/*     */ 
/*     */   public static boolean equals(AudioFormat.Encoding e1, AudioFormat.Encoding e2)
/*     */   {
/* 132 */     return e2.toString().equals(e1.toString());
/*     */   }
/*     */ 
/*     */   public static AudioFormat.Encoding[] getEncodings()
/*     */   {
/* 150 */     StringHashedSet iteratedSources = new StringHashedSet();
/* 151 */     StringHashedSet retrievedTargets = new StringHashedSet();
/* 152 */     Iterator sourceFormats = encodings.iterator();
/* 153 */     while (sourceFormats.hasNext()) {
/* 154 */       AudioFormat.Encoding source = (AudioFormat.Encoding)sourceFormats.next();
/* 155 */       iterateEncodings(source, iteratedSources, retrievedTargets);
/*     */     }
/* 157 */     return (AudioFormat.Encoding[])retrievedTargets.toArray(new AudioFormat.Encoding[retrievedTargets.size()]);
/*     */   }
/*     */ 
/*     */   private static void iterateEncodings(AudioFormat.Encoding source, StringHashedSet iteratedSources, StringHashedSet retrievedTargets)
/*     */   {
/* 165 */     if (!iteratedSources.contains(source)) {
/* 166 */       iteratedSources.add(source);
/* 167 */       AudioFormat.Encoding[] targets = AudioSystem.getTargetEncodings(source);
/* 168 */       for (int i = 0; i < targets.length; ++i) {
/* 169 */         AudioFormat.Encoding target = targets[i];
/* 170 */         if (retrievedTargets.add(target.toString()))
/* 171 */           iterateEncodings(target, iteratedSources, retrievedTargets);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   static
/*     */   {
/*  73 */     encodings.add(AudioFormat.Encoding.PCM_SIGNED);
/*  74 */     encodings.add(AudioFormat.Encoding.PCM_UNSIGNED);
/*  75 */     encodings.add(AudioFormat.Encoding.ULAW);
/*  76 */     encodings.add(AudioFormat.Encoding.ALAW);
/*     */   }
/*     */ }

