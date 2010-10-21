/*     */ package org.tritonus.share.midi;
/*     */ 
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.util.BitSet;
/*     */ import java.util.Collection;
/*     */ import java.util.Iterator;
/*     */ import java.util.Set;
/*     */ import javax.sound.midi.ControllerEventListener;
/*     */ import javax.sound.midi.InvalidMidiDataException;
/*     */ import javax.sound.midi.MetaEventListener;
/*     */ import javax.sound.midi.MetaMessage;
/*     */ import javax.sound.midi.MidiDevice;
/*     */ import javax.sound.midi.MidiMessage;
/*     */ import javax.sound.midi.MidiSystem;
/*     */ import javax.sound.midi.Sequence;
/*     */ import javax.sound.midi.Sequencer;
/*     */ import javax.sound.midi.Sequencer.SyncMode;
/*     */ import javax.sound.midi.ShortMessage;
/*     */ import org.tritonus.share.ArraySet;
/*     */ import org.tritonus.share.TDebug;
/*     */ 
/*     */ public abstract class TSequencer extends TMidiDevice
/*     */   implements Sequencer
/*     */ {
/*     */   private static final float MPQ_BPM_FACTOR = 60000000.0F;
/*  56 */   private static final Sequencer.SyncMode[] EMPTY_SYNCMODE_ARRAY = new Sequencer.SyncMode[0];
/*     */   private boolean m_bRunning;
/*     */   private Sequence m_sequence;
/*     */   private Set m_metaListeners;
/*     */   private Set[] m_aControllerListeners;
/*     */   private float m_fNominalTempoInMPQ;
/*     */   private float m_fTempoFactor;
/*     */   private Collection m_masterSyncModes;
/*     */   private Collection m_slaveSyncModes;
/*     */   private Sequencer.SyncMode m_masterSyncMode;
/*     */   private Sequencer.SyncMode m_slaveSyncMode;
/*     */   private BitSet m_muteBitSet;
/*     */   private BitSet m_soloBitSet;
/*     */   private BitSet m_enabledBitSet;
/*     */ 
/*     */   protected TSequencer(MidiDevice.Info info, Collection masterSyncModes, Collection slaveSyncModes)
/*     */   {
/*  99 */     super(info);
/* 100 */     if (TDebug.TraceSequencer) TDebug.out("TSequencer.<init>(): begin");
/* 101 */     this.m_bRunning = false;
/* 102 */     this.m_sequence = null;
/* 103 */     this.m_metaListeners = new ArraySet();
/* 104 */     this.m_aControllerListeners = new Set[128];
/* 105 */     setTempoInMPQ(500000.0F);
/* 106 */     setTempoFactor(1.0F);
/* 107 */     this.m_masterSyncModes = masterSyncModes;
/* 108 */     this.m_slaveSyncModes = slaveSyncModes;
/* 109 */     if (getMasterSyncModes().length > 0)
/*     */     {
/* 111 */       this.m_masterSyncMode = getMasterSyncModes()[0];
/*     */     }
/* 113 */     if (getSlaveSyncModes().length > 0)
/*     */     {
/* 115 */       this.m_slaveSyncMode = getSlaveSyncModes()[0];
/*     */     }
/* 117 */     this.m_muteBitSet = new BitSet();
/* 118 */     this.m_soloBitSet = new BitSet();
/* 119 */     this.m_enabledBitSet = new BitSet();
/* 120 */     updateEnabled();
/* 121 */     if (!TDebug.TraceSequencer) return; TDebug.out("TSequencer.<init>(): end");
/*     */   }
/*     */ 
/*     */   public void setSequence(Sequence sequence)
/*     */     throws InvalidMidiDataException
/*     */   {
/* 129 */     if (TDebug.TraceSequencer) TDebug.out("TSequencer.setSequence(Sequence): begin");
/*     */ 
/* 131 */     this.m_sequence = sequence;
/*     */ 
/* 133 */     setTempoFactor(1.0F);
/* 134 */     if (!TDebug.TraceSequencer) return; TDebug.out("TSequencer.setSequence(Sequence): end");
/*     */   }
/*     */ 
/*     */   public void setSequence(InputStream inputStream)
/*     */     throws InvalidMidiDataException, IOException
/*     */   {
/* 142 */     if (TDebug.TraceSequencer) TDebug.out("TSequencer.setSequence(InputStream): begin");
/* 143 */     Sequence sequence = MidiSystem.getSequence(inputStream);
/* 144 */     setSequence(sequence);
/* 145 */     if (!TDebug.TraceSequencer) return; TDebug.out("TSequencer.setSequence(InputStream): end");
/*     */   }
/*     */ 
/*     */   public Sequence getSequence()
/*     */   {
/* 152 */     if (TDebug.TraceSequencer) TDebug.out("TSequencer.getSequence(): begin");
/* 153 */     if (TDebug.TraceSequencer) TDebug.out("TSequencer.getSequence(): end");
/* 154 */     return this.m_sequence;
/*     */   }
/*     */ 
/*     */   public synchronized void start()
/*     */   {
/* 161 */     if (TDebug.TraceSequencer) TDebug.out("TSequencer.start(): begin");
/* 162 */     checkOpen();
/* 163 */     if (!isRunning())
/*     */     {
/* 165 */       this.m_bRunning = true;
/*     */ 
/* 167 */       startImpl();
/*     */     }
/* 169 */     if (!TDebug.TraceSequencer) return; TDebug.out("TSequencer.start(): end");
/*     */   }
/*     */ 
/*     */   protected void startImpl()
/*     */   {
/* 179 */     if (TDebug.TraceMidiDevice) TDebug.out("TSequencer.startImpl(): begin");
/* 180 */     if (!TDebug.TraceMidiDevice) return; TDebug.out("TSequencer.startImpl(): end");
/*     */   }
/*     */ 
/*     */   public synchronized void stop()
/*     */   {
/* 187 */     if (TDebug.TraceSequencer) TDebug.out("TSequencer.stop(): begin");
/* 188 */     checkOpen();
/* 189 */     if (isRunning())
/*     */     {
/* 191 */       stopImpl();
/* 192 */       this.m_bRunning = false;
/*     */     }
/* 194 */     if (!TDebug.TraceSequencer) return; TDebug.out("TSequencer.stop(): end");
/*     */   }
/*     */ 
/*     */   protected void stopImpl()
/*     */   {
/* 205 */     if (TDebug.TraceMidiDevice) TDebug.out("TSequencer.stopImpl(): begin");
/* 206 */     if (!TDebug.TraceMidiDevice) return; TDebug.out("TSequencer.stopImpl(): end");
/*     */   }
/*     */ 
/*     */   public synchronized boolean isRunning()
/*     */   {
/* 213 */     return this.m_bRunning;
/*     */   }
/*     */ 
/*     */   protected void checkOpen()
/*     */   {
/* 229 */     if (isOpen())
/*     */       return;
/* 231 */     throw new IllegalStateException("Sequencer is not open");
/*     */   }
/*     */ 
/*     */   protected int getResolution()
/*     */   {
/* 242 */     if (TDebug.TraceSequencer) TDebug.out("TSequencer.getResolution(): begin");
/* 243 */     Sequence sequence = getSequence();
/*     */     int nResolution;
/*     */     if (sequence != null)
/*     */     {
/* 247 */       nResolution = sequence.getResolution();
/*     */     }
/*     */     else
/*     */     {
/* 251 */       nResolution = 1;
/*     */     }
/* 253 */     if (TDebug.TraceSequencer) TDebug.out("TSequencer.getResolution(): end");
/* 254 */     return nResolution;
/*     */   }
/*     */ 
/*     */   protected void setRealTempo()
/*     */   {
/* 261 */     if (TDebug.TraceSequencer) TDebug.out("TSequencer.setRealTempo(): begin");
/* 262 */     float fRealTempo = getTempoInMPQ() / getTempoFactor();
/* 263 */     if (TDebug.TraceSequencer) TDebug.out("TSequencer.setRealTempo(): real tempo: " + fRealTempo);
/* 264 */     setTempoImpl(fRealTempo);
/* 265 */     if (!TDebug.TraceSequencer) return; TDebug.out("TSequencer.setRealTempo(): end");
/*     */   }
/*     */ 
/*     */   public float getTempoInBPM()
/*     */   {
/* 272 */     if (TDebug.TraceSequencer) TDebug.out("TSequencer.getTempoInBPM(): begin");
/* 273 */     float fBPM = 60000000.0F / getTempoInMPQ();
/* 274 */     if (TDebug.TraceSequencer) TDebug.out("TSequencer.getTempoInBPM(): end");
/* 275 */     return fBPM;
/*     */   }
/*     */ 
/*     */   public void setTempoInBPM(float fBPM)
/*     */   {
/* 282 */     if (TDebug.TraceSequencer) TDebug.out("TSequencer.setTempoInBPM(): begin");
/* 283 */     float fMPQ = 60000000.0F / fBPM;
/* 284 */     setTempoInMPQ(fMPQ);
/* 285 */     if (!TDebug.TraceSequencer) return; TDebug.out("TSequencer.setTempoInBPM(): end");
/*     */   }
/*     */ 
/*     */   public float getTempoInMPQ()
/*     */   {
/* 292 */     if (TDebug.TraceSequencer) TDebug.out("TSequencer.getTempoInMPQ(): begin");
/* 293 */     if (TDebug.TraceSequencer) TDebug.out("TSequencer.getTempoInMPQ(): end");
/* 294 */     return this.m_fNominalTempoInMPQ;
/*     */   }
/*     */ 
/*     */   public void setTempoInMPQ(float fMPQ)
/*     */   {
/* 304 */     if (TDebug.TraceSequencer) TDebug.out("TSequencer.setTempoInMPQ(): begin");
/* 305 */     this.m_fNominalTempoInMPQ = fMPQ;
/* 306 */     setRealTempo();
/* 307 */     if (!TDebug.TraceSequencer) return; TDebug.out("TSequencer.setTempoInMPQ(): end");
/*     */   }
/*     */ 
/*     */   public void setTempoFactor(float fFactor)
/*     */   {
/* 314 */     if (TDebug.TraceSequencer) TDebug.out("TSequencer.setTempoFactor(): begin");
/* 315 */     this.m_fTempoFactor = fFactor;
/* 316 */     setRealTempo();
/* 317 */     if (!TDebug.TraceSequencer) return; TDebug.out("TSequencer.setTempoFactor(): end");
/*     */   }
/*     */ 
/*     */   public float getTempoFactor()
/*     */   {
/* 324 */     if (TDebug.TraceSequencer) TDebug.out("TSequencer.getTempoFactor(): begin");
/* 325 */     if (TDebug.TraceSequencer) TDebug.out("TSequencer.getTempoFactor(): end");
/* 326 */     return this.m_fTempoFactor;
/*     */   }
/*     */ 
/*     */   protected abstract void setTempoImpl(float paramFloat);
/*     */ 
/*     */   public long getTickLength()
/*     */   {
/* 344 */     if (TDebug.TraceSequencer) TDebug.out("TSequencer.getTickLength(): begin");
/* 345 */     long lLength = 0L;
/* 346 */     if (getSequence() != null)
/*     */     {
/* 348 */       lLength = getSequence().getTickLength();
/*     */     }
/* 350 */     if (TDebug.TraceSequencer) TDebug.out("TSequencer.getTickLength(): end");
/* 351 */     return lLength;
/*     */   }
/*     */ 
/*     */   public long getMicrosecondLength()
/*     */   {
/* 359 */     if (TDebug.TraceSequencer) TDebug.out("TSequencer.getMicrosecondLength(): begin");
/* 360 */     long lLength = 0L;
/* 361 */     if (getSequence() != null)
/*     */     {
/* 363 */       lLength = getSequence().getMicrosecondLength();
/*     */     }
/* 365 */     if (TDebug.TraceSequencer) TDebug.out("TSequencer.getMicrosecondLength(): end");
/* 366 */     return lLength;
/*     */   }
/*     */ 
/*     */   public boolean addMetaEventListener(MetaEventListener listener)
/*     */   {
/* 374 */     synchronized (this.m_metaListeners)
/*     */     {
/* 376 */       return this.m_metaListeners.add(listener);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void removeMetaEventListener(MetaEventListener listener)
/*     */   {
/* 384 */     synchronized (this.m_metaListeners)
/*     */     {
/* 386 */       this.m_metaListeners.remove(listener);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected Iterator getMetaEventListeners()
/*     */   {
/* 393 */     synchronized (this.m_metaListeners)
/*     */     {
/* 395 */       return this.m_metaListeners.iterator();
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void sendMetaMessage(MetaMessage message)
/*     */   {
/* 403 */     Iterator iterator = getMetaEventListeners();
/* 404 */     while (iterator.hasNext())
/*     */     {
/* 406 */       MetaEventListener metaEventListener = (MetaEventListener)iterator.next();
/* 407 */       MetaMessage copiedMessage = (MetaMessage)message.clone();
/* 408 */       metaEventListener.meta(copiedMessage);
/*     */     }
/*     */   }
/*     */ 
/*     */   public int[] addControllerEventListener(ControllerEventListener listener, int[] anControllers)
/*     */   {
/* 416 */     synchronized (this.m_aControllerListeners)
/*     */     {
/* 418 */       if (anControllers == null)
/*     */       {
/* 425 */         for (int i = 0; i < 128; ++i)
/*     */         {
/* 427 */           addControllerListener(i, listener);
/*     */         }
/*     */ 
/*     */       }
/*     */       else {
/* 432 */         for (int i = 0; i < anControllers.length; ++i)
/*     */         {
/* 434 */           addControllerListener(anControllers[i], listener);
/*     */         }
/*     */       }
/*     */     }
/* 438 */     return getListenedControllers(listener);
/*     */   }
/*     */ 
/*     */   private void addControllerListener(int i, ControllerEventListener listener)
/*     */   {
/* 446 */     if (this.m_aControllerListeners[i] == null)
/*     */     {
/* 448 */       this.m_aControllerListeners[i] = new ArraySet();
/*     */     }
/* 450 */     this.m_aControllerListeners[i].add(listener);
/*     */   }
/*     */ 
/*     */   public int[] removeControllerEventListener(ControllerEventListener listener, int[] anControllers)
/*     */   {
/* 457 */     synchronized (this.m_aControllerListeners)
/*     */     {
/* 459 */       if (anControllers == null)
/*     */       {
/* 465 */         for (int i = 0; i < 128; ++i)
/*     */         {
/* 467 */           removeControllerListener(i, listener);
/*     */         }
/*     */ 
/*     */       }
/*     */       else {
/* 472 */         for (int i = 0; i < anControllers.length; ++i)
/*     */         {
/* 474 */           removeControllerListener(anControllers[i], listener);
/*     */         }
/*     */       }
/*     */     }
/* 478 */     return getListenedControllers(listener);
/*     */   }
/*     */ 
/*     */   private void removeControllerListener(int i, ControllerEventListener listener)
/*     */   {
/* 486 */     if (this.m_aControllerListeners[i] == null)
/*     */       return;
/* 488 */     this.m_aControllerListeners[i].add(listener);
/*     */   }
/*     */ 
/*     */   private int[] getListenedControllers(ControllerEventListener listener)
/*     */   {
/* 496 */     int[] anControllers = new int[128];
/* 497 */     int nIndex = 0;
/* 498 */     for (int nController = 0; nController < 128; ++nController)
/*     */     {
/* 500 */       if ((this.m_aControllerListeners[nController] == null) || (!this.m_aControllerListeners[nController].contains(listener))) {
/*     */         continue;
/*     */       }
/* 503 */       anControllers[nIndex] = nController;
/* 504 */       ++nIndex;
/*     */     }
/*     */ 
/* 507 */     int[] anResultControllers = new int[nIndex];
/* 508 */     System.arraycopy(anControllers, 0, anResultControllers, 0, nIndex);
/* 509 */     return anResultControllers;
/*     */   }
/*     */ 
/*     */   protected void sendControllerEvent(ShortMessage message)
/*     */   {
/* 517 */     int nController = message.getData1();
/* 518 */     if (this.m_aControllerListeners[nController] == null)
/*     */       return;
/* 520 */     Iterator iterator = this.m_aControllerListeners[nController].iterator();
/* 521 */     while (iterator.hasNext())
/*     */     {
/* 523 */       ControllerEventListener controllerEventListener = (ControllerEventListener)iterator.next();
/* 524 */       ShortMessage copiedMessage = (ShortMessage)message.clone();
/* 525 */       controllerEventListener.controlChange(copiedMessage);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void notifyListeners(MidiMessage message)
/*     */   {
/* 534 */     if (TDebug.TraceSequencer) TDebug.out("TSequencer.sendToListeners(): begin");
/* 535 */     if (message instanceof MetaMessage)
/*     */     {
/* 538 */       sendMetaMessage((MetaMessage)message);
/*     */     }
/* 540 */     else if ((message instanceof ShortMessage) && (((ShortMessage)message).getCommand() == 176))
/*     */     {
/* 542 */       sendControllerEvent((ShortMessage)message);
/*     */     }
/* 544 */     if (!TDebug.TraceSequencer) return; TDebug.out("TSequencer.sendToListeners(): end");
/*     */   }
/*     */ 
/*     */   public Sequencer.SyncMode getMasterSyncMode()
/*     */   {
/* 551 */     if (TDebug.TraceSequencer) TDebug.out("TSequencer.getMasterSyncMode(): begin");
/* 552 */     if (TDebug.TraceSequencer) TDebug.out("TSequencer.getMasterSyncMode(): end");
/* 553 */     return this.m_masterSyncMode;
/*     */   }
/*     */ 
/*     */   public void setMasterSyncMode(Sequencer.SyncMode syncMode)
/*     */   {
/* 560 */     if (TDebug.TraceSequencer) TDebug.out("TSequencer.setMasterSyncMode(): begin");
/* 561 */     if (this.m_masterSyncModes.contains(syncMode))
/*     */     {
/* 563 */       if (!getMasterSyncMode().equals(syncMode))
/*     */       {
/* 565 */         this.m_masterSyncMode = syncMode;
/* 566 */         setMasterSyncModeImpl(syncMode);
/*     */       }
/*     */ 
/*     */     }
/*     */     else {
/* 571 */       throw new IllegalArgumentException("sync mode not allowed: " + syncMode);
/*     */     }
/* 573 */     if (!TDebug.TraceSequencer) return; TDebug.out("TSequencer.setMasterSyncMode(): end");
/*     */   }
/*     */ 
/*     */   protected void setMasterSyncModeImpl(Sequencer.SyncMode syncMode)
/*     */   {
/* 582 */     if (TDebug.TraceSequencer) TDebug.out("TSequencer.setMasterSyncModeImpl(): begin");
/*     */ 
/* 584 */     if (!TDebug.TraceSequencer) return; TDebug.out("TSequencer.setMasterSyncModeImpl(): end");
/*     */   }
/*     */ 
/*     */   public Sequencer.SyncMode[] getMasterSyncModes()
/*     */   {
/* 591 */     if (TDebug.TraceSequencer) TDebug.out("TSequencer.getMasterSyncModes(): begin");
/* 592 */     Sequencer.SyncMode[] syncModes = (Sequencer.SyncMode[])this.m_masterSyncModes.toArray(EMPTY_SYNCMODE_ARRAY);
/* 593 */     if (TDebug.TraceSequencer) TDebug.out("TSequencer.getMasterSyncModes(): end");
/* 594 */     return syncModes;
/*     */   }
/*     */ 
/*     */   public Sequencer.SyncMode getSlaveSyncMode()
/*     */   {
/* 601 */     if (TDebug.TraceSequencer) TDebug.out("TSequencer.getSlaveSyncMode(): begin");
/* 602 */     if (TDebug.TraceSequencer) TDebug.out("TSequencer.getSlaveSyncMode(): end");
/* 603 */     return this.m_slaveSyncMode;
/*     */   }
/*     */ 
/*     */   public void setSlaveSyncMode(Sequencer.SyncMode syncMode)
/*     */   {
/* 610 */     if (TDebug.TraceSequencer) TDebug.out("TSequencer.setSlaveSyncMode(): begin");
/* 611 */     if (this.m_slaveSyncModes.contains(syncMode))
/*     */     {
/* 613 */       if (!getSlaveSyncMode().equals(syncMode))
/*     */       {
/* 615 */         this.m_slaveSyncMode = syncMode;
/* 616 */         setSlaveSyncModeImpl(syncMode);
/*     */       }
/*     */ 
/*     */     }
/*     */     else {
/* 621 */       throw new IllegalArgumentException("sync mode not allowed: " + syncMode);
/*     */     }
/* 623 */     if (!TDebug.TraceSequencer) return; TDebug.out("TSequencer.setSlaveSyncMode(): end");
/*     */   }
/*     */ 
/*     */   protected void setSlaveSyncModeImpl(Sequencer.SyncMode syncMode)
/*     */   {
/* 633 */     if (TDebug.TraceSequencer) TDebug.out("TSequencer.setSlaveSyncModeImpl(): begin");
/*     */ 
/* 635 */     if (!TDebug.TraceSequencer) return; TDebug.out("TSequencer.setSlaveSyncModeImpl(): end");
/*     */   }
/*     */ 
/*     */   public Sequencer.SyncMode[] getSlaveSyncModes()
/*     */   {
/* 642 */     if (TDebug.TraceSequencer) TDebug.out("TSequencer.getSlaveSyncModes(): begin");
/* 643 */     Sequencer.SyncMode[] syncModes = (Sequencer.SyncMode[])this.m_slaveSyncModes.toArray(EMPTY_SYNCMODE_ARRAY);
/* 644 */     if (TDebug.TraceSequencer) TDebug.out("TSequencer.getSlaveSyncModes(): end");
/* 645 */     return syncModes;
/*     */   }
/*     */ 
/*     */   public boolean getTrackSolo(int nTrack)
/*     */   {
/* 652 */     boolean bSoloed = false;
/* 653 */     if ((getSequence() != null) && 
/* 655 */       (nTrack < getSequence().getTracks().length))
/*     */     {
/* 657 */       bSoloed = this.m_soloBitSet.get(nTrack);
/*     */     }
/*     */ 
/* 660 */     return bSoloed;
/*     */   }
/*     */ 
/*     */   public void setTrackSolo(int nTrack, boolean bSolo)
/*     */   {
/* 667 */     if ((getSequence() == null) || 
/* 669 */       (nTrack >= getSequence().getTracks().length))
/*     */       return;
/* 671 */     boolean bOldState = this.m_soloBitSet.get(nTrack);
/* 672 */     if (bSolo == bOldState)
/*     */       return;
/* 674 */     if (bSolo)
/*     */     {
/* 676 */       this.m_soloBitSet.set(nTrack);
/*     */     }
/*     */     else
/*     */     {
/* 680 */       this.m_soloBitSet.clear(nTrack);
/*     */     }
/* 682 */     updateEnabled();
/* 683 */     setTrackSoloImpl(nTrack, bSolo);
/*     */   }
/*     */ 
/*     */   protected void setTrackSoloImpl(int nTrack, boolean bSolo)
/*     */   {
/*     */   }
/*     */ 
/*     */   public boolean getTrackMute(int nTrack)
/*     */   {
/* 699 */     boolean bMuted = false;
/* 700 */     if ((getSequence() != null) && 
/* 702 */       (nTrack < getSequence().getTracks().length))
/*     */     {
/* 704 */       bMuted = this.m_muteBitSet.get(nTrack);
/*     */     }
/*     */ 
/* 707 */     return bMuted;
/*     */   }
/*     */ 
/*     */   public void setTrackMute(int nTrack, boolean bMute)
/*     */   {
/* 714 */     if ((getSequence() == null) || 
/* 716 */       (nTrack >= getSequence().getTracks().length))
/*     */       return;
/* 718 */     boolean bOldState = this.m_muteBitSet.get(nTrack);
/* 719 */     if (bMute == bOldState)
/*     */       return;
/* 721 */     if (bMute)
/*     */     {
/* 723 */       this.m_muteBitSet.set(nTrack);
/*     */     }
/*     */     else
/*     */     {
/* 727 */       this.m_muteBitSet.clear(nTrack);
/*     */     }
/* 729 */     updateEnabled();
/* 730 */     setTrackMuteImpl(nTrack, bMute);
/*     */   }
/*     */ 
/*     */   protected void setTrackMuteImpl(int nTrack, boolean bMute)
/*     */   {
/*     */   }
/*     */ 
/*     */   private void updateEnabled()
/*     */   {
/* 745 */     BitSet oldEnabledBitSet = (BitSet)this.m_enabledBitSet.clone();
/* 746 */     boolean bSoloExists = this.m_soloBitSet.length() > 0;
/* 747 */     if (bSoloExists)
/*     */     {
/* 749 */       this.m_enabledBitSet = ((BitSet)this.m_soloBitSet.clone());
/*     */     }
/*     */     else
/*     */     {
/* 753 */       for (int i = 0; i < this.m_muteBitSet.size(); ++i)
/*     */       {
/* 755 */         if (this.m_muteBitSet.get(i))
/*     */         {
/* 757 */           this.m_enabledBitSet.clear(i);
/*     */         }
/*     */         else
/*     */         {
/* 761 */           this.m_enabledBitSet.set(i);
/*     */         }
/*     */       }
/*     */     }
/* 765 */     oldEnabledBitSet.xor(this.m_enabledBitSet);
/*     */ 
/* 769 */     for (int i = 0; i < oldEnabledBitSet.size(); ++i)
/*     */     {
/* 771 */       if (!oldEnabledBitSet.get(i))
/*     */         continue;
/* 773 */       setTrackEnabledImpl(i, this.m_enabledBitSet.get(i));
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void setTrackEnabledImpl(int nTrack, boolean bEnabled)
/*     */   {
/*     */   }
/*     */ 
/*     */   protected boolean isTrackEnabled(int nTrack)
/*     */   {
/* 798 */     return this.m_enabledBitSet.get(nTrack);
/*     */   }
/*     */ 
/*     */   public void setLatency(int nMilliseconds)
/*     */   {
/*     */   }
/*     */ 
/*     */   public int getLatency()
/*     */   {
/* 821 */     return -1;
/*     */   }
/*     */ }
