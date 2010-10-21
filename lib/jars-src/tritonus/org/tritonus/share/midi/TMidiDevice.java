/*     */ package org.tritonus.share.midi;
/*     */ 
/*     */ import java.util.ArrayList;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import javax.sound.midi.InvalidMidiDataException;
/*     */ import javax.sound.midi.MetaMessage;
/*     */ import javax.sound.midi.MidiDevice;
/*     */ import javax.sound.midi.MidiMessage;
/*     */ import javax.sound.midi.MidiUnavailableException;
/*     */ import javax.sound.midi.Receiver;
/*     */ import javax.sound.midi.Transmitter;
/*     */ import org.tritonus.share.TDebug;
/*     */ 
/*     */ public abstract class TMidiDevice
/*     */   implements MidiDevice
/*     */ {
/*     */   private MidiDevice.Info m_info;
/*     */   private boolean m_bOpen;
/*     */   private boolean m_bUseIn;
/*     */   private boolean m_bUseOut;
/*     */   private int m_nNumReceivers;
/*     */   private List m_transmitters;
/*     */ 
/*     */   public TMidiDevice(MidiDevice.Info info)
/*     */   {
/*  93 */     this(info, true, true);
/*     */   }
/*     */ 
/*     */   public TMidiDevice(MidiDevice.Info info, boolean bUseIn, boolean bUseOut)
/*     */   {
/* 109 */     if (TDebug.TraceMidiDevice) TDebug.out("TMidiDevice.<init>(): begin");
/* 110 */     this.m_info = info;
/* 111 */     this.m_bUseIn = bUseIn;
/* 112 */     this.m_bUseOut = bUseOut;
/* 113 */     this.m_bOpen = false;
/* 114 */     this.m_nNumReceivers = 0;
/* 115 */     this.m_transmitters = new ArrayList();
/* 116 */     if (!TDebug.TraceMidiDevice) return; TDebug.out("TMidiDevice.<init>(): end");
/*     */   }
/*     */ 
/*     */   public MidiDevice.Info getDeviceInfo()
/*     */   {
/* 130 */     if (TDebug.TraceMidiDevice) TDebug.out("TMidiDevice.getDeviceInfo(): begin");
/* 131 */     if (TDebug.TraceMidiDevice) TDebug.out("TMidiDevice.getDeviceInfo(): end");
/* 132 */     return this.m_info;
/*     */   }
/*     */ 
/*     */   public void open()
/*     */     throws MidiUnavailableException
/*     */   {
/* 140 */     if (TDebug.TraceMidiDevice) TDebug.out("TMidiDevice.open(): begin");
/* 141 */     if (!isOpen())
/*     */     {
/* 143 */       this.m_bOpen = true;
/* 144 */       openImpl();
/*     */     }
/* 146 */     if (!TDebug.TraceMidiDevice) return; TDebug.out("TMidiDevice.open(): end");
/*     */   }
/*     */ 
/*     */   protected void openImpl()
/*     */     throws MidiUnavailableException
/*     */   {
/* 158 */     if (TDebug.TraceMidiDevice) TDebug.out("TMidiDevice.openImpl(): begin");
/* 159 */     if (!TDebug.TraceMidiDevice) return; TDebug.out("TMidiDevice.openImpl(): end");
/*     */   }
/*     */ 
/*     */   public void close()
/*     */   {
/* 166 */     if (TDebug.TraceMidiDevice) TDebug.out("TMidiDevice.close(): begin");
/* 167 */     if (isOpen())
/*     */     {
/* 169 */       closeImpl();
/*     */ 
/* 171 */       this.m_bOpen = false;
/*     */     }
/* 173 */     if (!TDebug.TraceMidiDevice) return; TDebug.out("TMidiDevice.close(): end");
/*     */   }
/*     */ 
/*     */   protected void closeImpl()
/*     */   {
/* 184 */     if (TDebug.TraceMidiDevice) TDebug.out("TMidiDevice.closeImpl(): begin");
/* 185 */     if (!TDebug.TraceMidiDevice) return; TDebug.out("TMidiDevice.closeImpl(): end");
/*     */   }
/*     */ 
/*     */   public boolean isOpen()
/*     */   {
/* 192 */     return this.m_bOpen;
/*     */   }
/*     */ 
/*     */   protected boolean getUseIn()
/*     */   {
/* 205 */     return this.m_bUseIn;
/*     */   }
/*     */ 
/*     */   protected boolean getUseOut()
/*     */   {
/* 218 */     return this.m_bUseOut;
/*     */   }
/*     */ 
/*     */   public long getMicrosecondPosition()
/*     */   {
/* 230 */     return -1L;
/*     */   }
/*     */ 
/*     */   public int getMaxReceivers()
/*     */   {
/* 237 */     int nMaxReceivers = 0;
/* 238 */     if (getUseOut())
/*     */     {
/* 243 */       nMaxReceivers = -1;
/*     */     }
/* 245 */     return nMaxReceivers;
/*     */   }
/*     */ 
/*     */   public int getMaxTransmitters()
/*     */   {
/* 252 */     int nMaxTransmitters = 0;
/* 253 */     if (getUseIn())
/*     */     {
/* 258 */       nMaxTransmitters = -1;
/*     */     }
/* 260 */     return nMaxTransmitters;
/*     */   }
/*     */ 
/*     */   public Receiver getReceiver()
/*     */     throws MidiUnavailableException
/*     */   {
/* 272 */     if (!getUseOut())
/*     */     {
/* 274 */       throw new MidiUnavailableException("Receivers are not supported by this device");
/*     */     }
/* 276 */     return new TReceiver();
/*     */   }
/*     */ 
/*     */   public Transmitter getTransmitter()
/*     */     throws MidiUnavailableException
/*     */   {
/* 288 */     if (!getUseIn())
/*     */     {
/* 290 */       throw new MidiUnavailableException("Transmitters are not supported by this device");
/*     */     }
/* 292 */     return new TTransmitter();
/*     */   }
/*     */ 
/*     */   protected void receive(MidiMessage message, long lTimeStamp)
/*     */   {
/* 303 */     if (!TDebug.TraceMidiDevice) return; TDebug.out("### [should be overridden] TMidiDevice.receive(): message " + message);
/*     */   }
/*     */ 
/*     */   private void addReceiver()
/*     */   {
/* 310 */     this.m_nNumReceivers += 1;
/*     */   }
/*     */ 
/*     */   private void removeReceiver()
/*     */   {
/* 317 */     this.m_nNumReceivers -= 1;
/*     */   }
/*     */ 
/*     */   private void addTransmitter(Transmitter transmitter)
/*     */   {
/* 325 */     synchronized (this.m_transmitters)
/*     */     {
/* 327 */       this.m_transmitters.add(transmitter);
/*     */     }
/*     */   }
/*     */ 
/*     */   private void removeTransmitter(Transmitter transmitter)
/*     */   {
/* 334 */     synchronized (this.m_transmitters)
/*     */     {
/* 336 */       this.m_transmitters.remove(transmitter);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void sendImpl(MidiMessage message, long lTimeStamp)
/*     */   {
/* 348 */     if (TDebug.TraceMidiDevice) TDebug.out("TMidiDevice.sendImpl(): begin");
/* 349 */     Iterator transmitters = this.m_transmitters.iterator();
/* 350 */     while (transmitters.hasNext())
/*     */     {
/* 352 */       TTransmitter transmitter = (TTransmitter)transmitters.next();
/*     */ 
/* 358 */       MidiMessage copiedMessage = null;
/* 359 */       if (message instanceof MetaMessage)
/*     */       {
/* 361 */         MetaMessage origMessage = (MetaMessage)message;
/* 362 */         MetaMessage metaMessage = new MetaMessage();
/*     */         try
/*     */         {
/* 365 */           metaMessage.setMessage(origMessage.getType(), origMessage.getData(), origMessage.getData().length);
/*     */         }
/*     */         catch (InvalidMidiDataException e)
/*     */         {
/* 369 */           if (TDebug.TraceAllExceptions) TDebug.out(e);
/*     */         }
/* 371 */         copiedMessage = metaMessage;
/*     */       }
/*     */       else
/*     */       {
/* 375 */         copiedMessage = (MidiMessage)message.clone();
/*     */       }
/*     */ 
/* 378 */       if (message instanceof MetaMessage)
/*     */       {
/* 380 */         if (TDebug.TraceMidiDevice) TDebug.out("TMidiDevice.sendImpl(): MetaMessage.getData().length (original): " + ((MetaMessage)message).getData().length);
/* 381 */         if (TDebug.TraceMidiDevice) TDebug.out("TMidiDevice.sendImpl(): MetaMessage.getData().length (cloned): " + ((MetaMessage)copiedMessage).getData().length);
/*     */       }
/* 383 */       transmitter.send(copiedMessage, lTimeStamp);
/*     */     }
/* 385 */     if (!TDebug.TraceMidiDevice) return; TDebug.out("TMidiDevice.sendImpl(): end");
/*     */   }
/*     */ 
/*     */   public static class Info extends MidiDevice.Info
/*     */   {
/*     */     public Info(String a, String b, String c, String d)
/*     */     {
/* 519 */       super(a, b, c, d);
/*     */     }
/*     */   }
/*     */ 
/*     */   public class TTransmitter
/*     */     implements Transmitter
/*     */   {
/*     */     private Receiver m_receiver;
/*     */ 
/*     */     public TTransmitter()
/*     */     {
/* 461 */       TMidiDevice.this.addTransmitter(this);
/*     */     }
/*     */ 
/*     */     public void setReceiver(Receiver receiver)
/*     */     {
/* 468 */       synchronized (this)
/*     */       {
/* 470 */         this.m_receiver = receiver;
/*     */       }
/*     */     }
/*     */ 
/*     */     public Receiver getReceiver()
/*     */     {
/* 478 */       return this.m_receiver;
/*     */     }
/*     */ 
/*     */     public void send(MidiMessage message, long lTimeStamp)
/*     */     {
/* 485 */       if (getReceiver() == null)
/*     */         return;
/* 487 */       getReceiver().send(message, lTimeStamp);
/*     */     }
/*     */ 
/*     */     public void close()
/*     */     {
/* 500 */       TMidiDevice.this.removeTransmitter(this);
/* 501 */       synchronized (this)
/*     */       {
/* 503 */         this.m_receiver = null;
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public class TReceiver
/*     */     implements Receiver
/*     */   {
/*     */     private boolean m_bOpen;
/*     */ 
/*     */     public TReceiver()
/*     */     {
/* 407 */       TMidiDevice.this.addReceiver();
/* 408 */       this.m_bOpen = true;
/*     */     }
/*     */ 
/*     */     protected boolean isOpen()
/*     */     {
/* 415 */       return this.m_bOpen;
/*     */     }
/*     */ 
/*     */     public void send(MidiMessage message, long lTimeStamp)
/*     */     {
/* 425 */       if (TDebug.TraceMidiDevice) TDebug.out("TMidiDevice.TReceiver.send(): message " + message);
/* 426 */       if (this.m_bOpen)
/*     */       {
/* 428 */         TMidiDevice.this.receive(message, lTimeStamp);
/*     */       }
/*     */       else
/*     */       {
/* 432 */         throw new IllegalStateException("receiver is not open");
/*     */       }
/*     */     }
/*     */ 
/*     */     public void close()
/*     */     {
/* 444 */       TMidiDevice.this.removeReceiver();
/* 445 */       this.m_bOpen = false;
/*     */     }
/*     */   }
/*     */ }
