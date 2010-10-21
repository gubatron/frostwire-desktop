/*    */ package org.tritonus.share.sampled.mixer;
/*    */ 
/*    */ import javax.sound.sampled.CompoundControl;
/*    */ import javax.sound.sampled.Control;
/*    */ import org.tritonus.share.TDebug;
/*    */ 
/*    */ public class TCompoundControl extends CompoundControl
/*    */   implements TControllable
/*    */ {
/*    */   private TControlController m_controller;
/*    */ 
/*    */   public TCompoundControl(CompoundControl.Type type, Control[] aMemberControls)
/*    */   {
/* 66 */     super(type, aMemberControls);
/* 67 */     if (TDebug.TraceControl)
/*    */     {
/* 69 */       TDebug.out("TCompoundControl.<init>: begin");
/*    */     }
/* 71 */     this.m_controller = new TControlController();
/* 72 */     if (!TDebug.TraceControl)
/*    */       return;
/* 74 */     TDebug.out("TCompoundControl.<init>: end");
/*    */   }
/*    */ 
/*    */   public void setParentControl(TCompoundControl compoundControl)
/*    */   {
/* 82 */     this.m_controller.setParentControl(compoundControl);
/*    */   }
/*    */ 
/*    */   public TCompoundControl getParentControl()
/*    */   {
/* 89 */     return this.m_controller.getParentControl();
/*    */   }
/*    */ 
/*    */   public void commit()
/*    */   {
/* 96 */     this.m_controller.commit();
/*    */   }
/*    */ }

