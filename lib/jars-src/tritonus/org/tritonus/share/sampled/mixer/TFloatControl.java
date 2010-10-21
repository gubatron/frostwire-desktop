/*     */ package org.tritonus.share.sampled.mixer;
/*     */ 
/*     */ import javax.sound.sampled.FloatControl;
/*     */ import org.tritonus.share.TDebug;
/*     */ 
/*     */ public class TFloatControl extends FloatControl
/*     */   implements TControllable
/*     */ {
/*     */   private TControlController m_controller;
/*     */ 
/*     */   public TFloatControl(FloatControl.Type type, float fMinimum, float fMaximum, float fPrecision, int nUpdatePeriod, float fInitialValue, String strUnits)
/*     */   {
/*  58 */     super(type, fMinimum, fMaximum, fPrecision, nUpdatePeriod, fInitialValue, strUnits);
/*     */ 
/*  65 */     if (TDebug.TraceControl)
/*     */     {
/*  67 */       TDebug.out("TFloatControl.<init>: begin");
/*     */     }
/*  69 */     this.m_controller = new TControlController();
/*  70 */     if (!TDebug.TraceControl)
/*     */       return;
/*  72 */     TDebug.out("TFloatControl.<init>: end");
/*     */   }
/*     */ 
/*     */   public TFloatControl(FloatControl.Type type, float fMinimum, float fMaximum, float fPrecision, int nUpdatePeriod, float fInitialValue, String strUnits, String strMinLabel, String strMidLabel, String strMaxLabel)
/*     */   {
/*  89 */     super(type, fMinimum, fMaximum, fPrecision, nUpdatePeriod, fInitialValue, strUnits, strMinLabel, strMidLabel, strMaxLabel);
/*     */ 
/*  99 */     if (TDebug.TraceControl)
/*     */     {
/* 101 */       TDebug.out("TFloatControl.<init>: begin");
/*     */     }
/* 103 */     this.m_controller = new TControlController();
/* 104 */     if (!TDebug.TraceControl)
/*     */       return;
/* 106 */     TDebug.out("TFloatControl.<init>: end");
/*     */   }
/*     */ 
/*     */   public void setParentControl(TCompoundControl compoundControl)
/*     */   {
/* 114 */     this.m_controller.setParentControl(compoundControl);
/*     */   }
/*     */ 
/*     */   public TCompoundControl getParentControl()
/*     */   {
/* 121 */     return this.m_controller.getParentControl();
/*     */   }
/*     */ 
/*     */   public void commit()
/*     */   {
/* 128 */     this.m_controller.commit();
/*     */   }
/*     */ }

