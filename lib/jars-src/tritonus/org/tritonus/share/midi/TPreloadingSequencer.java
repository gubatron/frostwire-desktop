/*     */ package org.tritonus.share.midi;
/*     */ 
/*     */ import java.util.Collection;
/*     */ import javax.sound.midi.MidiDevice;
/*     */ import javax.sound.midi.MidiMessage;
/*     */ import org.tritonus.share.TDebug;
/*     */ 
/*     */ public abstract class TPreloadingSequencer extends TSequencer
/*     */ {
/*     */   private static final int DEFAULT_LATENCY = 100;
/*     */   private int m_nLatency;
/*     */   private Thread m_loaderThread;
/*     */ 
/*     */   protected TPreloadingSequencer(MidiDevice.Info info, Collection masterSyncModes, Collection slaveSyncModes)
/*     */   {
/*  76 */     super(info, masterSyncModes, slaveSyncModes);
/*     */ 
/*  78 */     if (TDebug.TraceSequencer) TDebug.out("TPreloadingSequencer.<init>(): begin");
/*  79 */     this.m_nLatency = 100;
/*  80 */     if (!TDebug.TraceSequencer) return; TDebug.out("TPreloadingSequencer.<init>(): end");
/*     */   }
/*     */ 
/*     */   public void setLatency(int nLatency)
/*     */   {
/*  96 */     this.m_nLatency = nLatency;
/*     */   }
/*     */ 
/*     */   public int getLatency()
/*     */   {
/* 108 */     return this.m_nLatency;
/*     */   }
/*     */ 
/*     */   protected void openImpl()
/*     */   {
/* 115 */     if (!TDebug.TraceSequencer) return; TDebug.out("AlsaSequencer.openImpl(): begin");
/*     */   }
/*     */ 
/*     */   public abstract void sendMessageTick(MidiMessage paramMidiMessage, long paramLong);
/*     */ }
