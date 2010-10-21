/*     */ package org.tritonus.share.sampled;
/*     */ 
/*     */ import javax.sound.sampled.AudioFileFormat;
/*     */ import org.tritonus.share.StringHashedSet;
/*     */ 
/*     */ public class AudioFileTypes extends AudioFileFormat.Type
/*     */ {
/*  70 */   private static StringHashedSet types = new StringHashedSet();
/*     */ 
/*     */   AudioFileTypes(String name, String ext)
/*     */   {
/*  82 */     super(name, ext);
/*     */   }
/*     */ 
/*     */   public static AudioFileFormat.Type getType(String name)
/*     */   {
/* 100 */     return getType(name, null);
/*     */   }
/*     */ 
/*     */   public static AudioFileFormat.Type getType(String name, String extension)
/*     */   {
/* 123 */     AudioFileFormat.Type res = (AudioFileFormat.Type)types.get(name);
/* 124 */     if (res == null)
/*     */     {
/* 126 */       if (extension == null) {
/* 127 */         return null;
/*     */       }
/*     */ 
/* 130 */       res = new AudioFileTypes(name, extension);
/*     */ 
/* 132 */       types.add(res);
/*     */     }
/* 134 */     return res;
/*     */   }
/*     */ 
/*     */   public static boolean equals(AudioFileFormat.Type t1, AudioFileFormat.Type t2)
/*     */   {
/* 147 */     return t2.toString().equals(t1.toString());
/*     */   }
/*     */ 
/*     */   static
/*     */   {
/*  74 */     types.add(AudioFileFormat.Type.AIFF);
/*  75 */     types.add(AudioFileFormat.Type.AIFC);
/*  76 */     types.add(AudioFileFormat.Type.AU);
/*  77 */     types.add(AudioFileFormat.Type.SND);
/*  78 */     types.add(AudioFileFormat.Type.WAVE);
/*     */   }
/*     */ }
