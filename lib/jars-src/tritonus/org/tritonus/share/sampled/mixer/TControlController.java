/*    */ package org.tritonus.share.sampled.mixer;
/*    */ 
/*    */ import org.tritonus.share.TDebug;
/*    */ 
/*    */ public class TControlController
/*    */   implements TControllable
/*    */ {
/*    */   private TCompoundControl m_parentControl;
/*    */ 
/*    */   public void setParentControl(TCompoundControl compoundControl)
/*    */   {
/* 72 */     this.m_parentControl = compoundControl;
/*    */   }
/*    */ 
/*    */   public TCompoundControl getParentControl()
/*    */   {
/* 78 */     return this.m_parentControl;
/*    */   }
/*    */ 
/*    */   public void commit()
/*    */   {
/* 84 */     if (TDebug.TraceControl)
/*    */     {
/* 86 */       TDebug.out("TControlController.commit(): called [" + super.getClass().getName() + "]");
/*    */     }
/* 88 */     if (getParentControl() == null)
/*    */       return;
/* 90 */     getParentControl().commit();
/*    */   }
/*    */ }

