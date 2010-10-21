/*    */ package org.tritonus.share.sampled.mixer;
/*    */ 
/*    */ import javax.sound.sampled.EnumControl;
/*    */ import org.tritonus.share.TDebug;
/*    */ 
/*    */ public class TEnumControl extends EnumControl
/*    */   implements TControllable
/*    */ {
/*    */   private TControlController m_controller;
/*    */ 
/*    */   public TEnumControl(EnumControl.Type type, Object[] aValues, Object value)
/*    */   {
/* 54 */     super(type, aValues, value);
/*    */ 
/* 57 */     if (TDebug.TraceControl)
/*    */     {
/* 59 */       TDebug.out("TEnumControl.<init>: begin");
/*    */     }
/* 61 */     this.m_controller = new TControlController();
/* 62 */     if (!TDebug.TraceControl)
/*    */       return;
/* 64 */     TDebug.out("TEnumControl.<init>: end");
/*    */   }
/*    */ 
/*    */   public void setParentControl(TCompoundControl compoundControl)
/*    */   {
/* 72 */     this.m_controller.setParentControl(compoundControl);
/*    */   }
/*    */ 
/*    */   public TCompoundControl getParentControl()
/*    */   {
/* 79 */     return this.m_controller.getParentControl();
/*    */   }
/*    */ 
/*    */   public void commit()
/*    */   {
/* 86 */     this.m_controller.commit();
/*    */   }
/*    */ }
