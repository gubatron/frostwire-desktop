/*    */ package org.tritonus.share.sampled.mixer;
/*    */ 
/*    */ import java.util.Collection;
/*    */ import javax.sound.sampled.AudioFormat;
/*    */ import javax.sound.sampled.DataLine;
/*    */ import javax.sound.sampled.LineUnavailableException;
/*    */ import org.tritonus.share.TDebug;
/*    */ 
/*    */ public abstract class TBaseDataLine extends TDataLine
/*    */ {
/*    */   public TBaseDataLine(TMixer mixer, DataLine.Info info)
/*    */   {
/* 50 */     super(mixer, info);
/*    */   }
/*    */ 
/*    */   public TBaseDataLine(TMixer mixer, DataLine.Info info, Collection controls)
/*    */   {
/* 60 */     super(mixer, info, controls);
/*    */   }
/*    */ 
/*    */   public void open(AudioFormat format, int nBufferSize)
/*    */     throws LineUnavailableException
/*    */   {
/* 70 */     if (TDebug.TraceDataLine) TDebug.out("TBaseDataLine.open(AudioFormat, int): called with buffer size: " + nBufferSize);
/* 71 */     setBufferSize(nBufferSize);
/* 72 */     open(format);
/*    */   }
/*    */ 
/*    */   public void open(AudioFormat format)
/*    */     throws LineUnavailableException
/*    */   {
/* 80 */     if (TDebug.TraceDataLine) TDebug.out("TBaseDataLine.open(AudioFormat): called");
/* 81 */     setFormat(format);
/* 82 */     open();
/*    */   }
/*    */ 
/*    */   protected void finalize()
/*    */   {
/* 90 */     if (!isOpen())
/*    */       return;
/* 92 */     close();
/*    */   }
/*    */ }
