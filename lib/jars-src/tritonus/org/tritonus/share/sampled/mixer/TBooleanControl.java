/*     */ package org.tritonus.share.sampled.mixer;
/*     */ 
/*     */ import javax.sound.sampled.BooleanControl;
/*     */ import org.tritonus.share.TDebug;
/*     */ 
/*     */ public class TBooleanControl extends BooleanControl
/*     */   implements TControllable
/*     */ {
/*     */   private TControlController m_controller;
/*     */ 
/*     */   public TBooleanControl(BooleanControl.Type type, boolean bInitialValue)
/*     */   {
/*  53 */     this(type, bInitialValue, null);
/*     */   }
/*     */ 
/*     */   public TBooleanControl(BooleanControl.Type type, boolean bInitialValue, TCompoundControl parentControl)
/*     */   {
/*  62 */     super(type, bInitialValue);
/*  63 */     if (TDebug.TraceControl)
/*     */     {
/*  65 */       TDebug.out("TBooleanControl.<init>: begin");
/*     */     }
/*  67 */     this.m_controller = new TControlController();
/*  68 */     if (!TDebug.TraceControl)
/*     */       return;
/*  70 */     TDebug.out("TBooleanControl.<init>: end");
/*     */   }
/*     */ 
/*     */   public TBooleanControl(BooleanControl.Type type, boolean bInitialValue, String strTrueStateLabel, String strFalseStateLabel)
/*     */   {
/*  81 */     this(type, bInitialValue, strTrueStateLabel, strFalseStateLabel, null);
/*     */   }
/*     */ 
/*     */   public TBooleanControl(BooleanControl.Type type, boolean bInitialValue, String strTrueStateLabel, String strFalseStateLabel, TCompoundControl parentControl)
/*     */   {
/*  92 */     super(type, bInitialValue, strTrueStateLabel, strFalseStateLabel);
/*  93 */     if (TDebug.TraceControl)
/*     */     {
/*  95 */       TDebug.out("TBooleanControl.<init>: begin");
/*     */     }
/*  97 */     this.m_controller = new TControlController();
/*  98 */     if (!TDebug.TraceControl)
/*     */       return;
/* 100 */     TDebug.out("TBooleanControl.<init>: end");
/*     */   }
/*     */ 
/*     */   public void setParentControl(TCompoundControl compoundControl)
/*     */   {
/* 108 */     this.m_controller.setParentControl(compoundControl);
/*     */   }
/*     */ 
/*     */   public TCompoundControl getParentControl()
/*     */   {
/* 115 */     return this.m_controller.getParentControl();
/*     */   }
/*     */ 
/*     */   public void commit()
/*     */   {
/* 122 */     this.m_controller.commit();
/*     */   }
/*     */ }
