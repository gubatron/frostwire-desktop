/*     */ package org.tritonus.share.sampled;
/*     */ 
/*     */ import java.util.Collection;
/*     */ import java.util.Iterator;
/*     */ import javax.sound.sampled.AudioFormat;
/*     */ import org.tritonus.share.ArraySet;
/*     */ 
/*     */ public class AudioFormatSet extends ArraySet
/*     */ {
/*  61 */   protected static final AudioFormat[] EMPTY_FORMAT_ARRAY = new AudioFormat[0];
/*     */ 
/*     */   public AudioFormatSet()
/*     */   {
/*     */   }
/*     */ 
/*     */   public AudioFormatSet(Collection c) {
/*  68 */     super(c);
/*     */   }
/*     */ 
/*     */   public boolean add(Object elem) {
/*  72 */     if ((elem == null) || (!(elem instanceof AudioFormat))) {
/*  73 */       return false;
/*     */     }
/*  75 */     return super.add(elem);
/*     */   }
/*     */ 
/*     */   public boolean contains(Object elem) {
/*  79 */     if ((elem == null) || (!(elem instanceof AudioFormat))) {
/*  80 */       return false;
/*     */     }
/*  82 */     AudioFormat comp = (AudioFormat)elem;
/*  83 */     Iterator it = iterator();
/*  84 */     while (it.hasNext()) {
/*  85 */       if (AudioFormats.equals(comp, (AudioFormat)it.next())) {
/*  86 */         return true;
/*     */       }
/*     */     }
/*  89 */     return false;
/*     */   }
/*     */ 
/*     */   public Object get(Object elem) {
/*  93 */     if ((elem == null) || (!(elem instanceof AudioFormat))) {
/*  94 */       return null;
/*     */     }
/*  96 */     AudioFormat comp = (AudioFormat)elem;
/*  97 */     Iterator it = iterator();
/*  98 */     while (it.hasNext()) {
/*  99 */       AudioFormat thisElem = (AudioFormat)it.next();
/* 100 */       if (AudioFormats.equals(comp, thisElem)) {
/* 101 */         return thisElem;
/*     */       }
/*     */     }
/* 104 */     return null;
/*     */   }
/*     */ 
/*     */   public AudioFormat getAudioFormat(AudioFormat elem) {
/* 108 */     return (AudioFormat)get(elem);
/*     */   }
/*     */ 
/*     */   public AudioFormat matches(AudioFormat elem)
/*     */   {
/* 120 */     if (elem == null) {
/* 121 */       return null;
/*     */     }
/* 123 */     Iterator it = iterator();
/* 124 */     while (it.hasNext()) {
/* 125 */       AudioFormat thisElem = (AudioFormat)it.next();
/* 126 */       if (AudioFormats.matches(elem, thisElem)) {
/* 127 */         return thisElem;
/*     */       }
/*     */     }
/* 130 */     return null;
/*     */   }
/*     */ 
/*     */   public AudioFormat[] toAudioFormatArray() {
/* 134 */     return (AudioFormat[])toArray(EMPTY_FORMAT_ARRAY);
/*     */   }
/*     */ 
/*     */   public void add(int index, Object element)
/*     */   {
/* 139 */     throw new UnsupportedOperationException("unsupported");
/*     */   }
/*     */ 
/*     */   public Object set(int index, Object element) {
/* 143 */     throw new UnsupportedOperationException("unsupported");
/*     */   }
/*     */ }

