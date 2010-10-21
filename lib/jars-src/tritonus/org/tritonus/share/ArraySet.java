/*    */ package org.tritonus.share;
/*    */ 
/*    */ import java.util.ArrayList;
/*    */ import java.util.Collection;
/*    */ import java.util.Set;
/*    */ 
/*    */ public class ArraySet extends ArrayList
/*    */   implements Set
/*    */ {
/*    */   public ArraySet()
/*    */   {
/*    */   }
/*    */ 
/*    */   public ArraySet(Collection c)
/*    */   {
/* 49 */     addAll(c);
/*    */   }
/*    */ 
/*    */   public boolean add(Object element)
/*    */   {
/* 56 */     if (!contains(element))
/*    */     {
/* 58 */       super.add(element);
/* 59 */       return true;
/*    */     }
/*    */ 
/* 63 */     return false;
/*    */   }
/*    */ 
/*    */   public void add(int index, Object element)
/*    */   {
/* 71 */     throw new UnsupportedOperationException("ArraySet.add(int index, Object element) unsupported");
/*    */   }
/*    */ 
/*    */   public Object set(int index, Object element)
/*    */   {
/* 76 */     throw new UnsupportedOperationException("ArraySet.set(int index, Object element) unsupported");
/*    */   }
/*    */ }
