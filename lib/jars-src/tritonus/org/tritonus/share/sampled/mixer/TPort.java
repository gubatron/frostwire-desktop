/*    */ package org.tritonus.share.sampled.mixer;
/*    */ 
/*    */ import java.util.Collection;
/*    */ import javax.sound.sampled.Line;
/*    */ import javax.sound.sampled.Port;
/*    */ 
/*    */ public class TPort extends TLine
/*    */   implements Port
/*    */ {
/*    */   public TPort(TMixer mixer, Line.Info info)
/*    */   {
/* 61 */     super(mixer, info);
/*    */   }
/*    */ 
/*    */   public TPort(TMixer mixer, Line.Info info, Collection controls)
/*    */   {
/* 70 */     super(mixer, info, controls);
/*    */   }
/*    */ }

