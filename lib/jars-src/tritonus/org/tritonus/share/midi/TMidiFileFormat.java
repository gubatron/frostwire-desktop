/*    */ package org.tritonus.share.midi;
/*    */ 
/*    */ import javax.sound.midi.MidiFileFormat;
/*    */ 
/*    */ public class TMidiFileFormat extends MidiFileFormat
/*    */ {
/*    */   private int m_nTrackCount;
/*    */ 
/*    */   public TMidiFileFormat(int nType, float fDivisionType, int nResolution, int nByteLength, long lMicrosecondLength, int nTrackCount)
/*    */   {
/* 51 */     super(nType, fDivisionType, nResolution, nByteLength, lMicrosecondLength);
/*    */ 
/* 56 */     this.m_nTrackCount = nTrackCount;
/*    */   }
/*    */ 
/*    */   public int getTrackCount()
/*    */   {
/* 63 */     return this.m_nTrackCount;
/*    */   }
/*    */ }
