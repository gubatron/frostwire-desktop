/*    */ package org.tritonus.share;
/*    */ 
/*    */ import java.util.Collection;
/*    */ import java.util.Iterator;
/*    */ 
/*    */ public class StringHashedSet extends ArraySet
/*    */ {
/*    */   public StringHashedSet()
/*    */   {
/*    */   }
/*    */ 
/*    */   public StringHashedSet(Collection c)
/*    */   {
/* 61 */     super(c);
/*    */   }
/*    */ 
/*    */   public boolean add(Object elem) {
/* 65 */     if (elem == null) {
/* 66 */       return false;
/*    */     }
/* 68 */     return super.add(elem);
/*    */   }
/*    */ 
/*    */   public boolean contains(Object elem) {
/* 72 */     if (elem == null) {
/* 73 */       return false;
/*    */     }
/* 75 */     String comp = elem.toString();
/* 76 */     Iterator it = iterator();
/* 77 */     while (it.hasNext()) {
/* 78 */       if (comp.equals(it.next().toString())) {
/* 79 */         return true;
/*    */       }
/*    */     }
/* 82 */     return false;
/*    */   }
/*    */ 
/*    */   public Object get(Object elem) {
/* 86 */     if (elem == null) {
/* 87 */       return null;
/*    */     }
/* 89 */     String comp = elem.toString();
/* 90 */     Iterator it = iterator();
/* 91 */     while (it.hasNext()) {
/* 92 */       Object thisElem = it.next();
/* 93 */       if (comp.equals(thisElem.toString())) {
/* 94 */         return thisElem;
/*    */       }
/*    */     }
/* 97 */     return null;
/*    */   }
/*    */ }

