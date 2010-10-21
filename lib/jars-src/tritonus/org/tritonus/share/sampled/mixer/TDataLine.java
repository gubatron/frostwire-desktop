/*     */ package org.tritonus.share.sampled.mixer;
/*     */ 
/*     */ import java.util.Collection;
/*     */ import javax.sound.sampled.AudioFormat;
/*     */ import javax.sound.sampled.DataLine;
/*     */ import javax.sound.sampled.LineEvent;
/*     */ import org.tritonus.share.TDebug;
/*     */ 
/*     */ public abstract class TDataLine extends TLine
/*     */   implements DataLine
/*     */ {
/*     */   private static final int DEFAULT_BUFFER_SIZE = 128000;
/*     */   private AudioFormat m_format;
/*     */   private int m_nBufferSize;
/*     */   private boolean m_bRunning;
/*     */ 
/*     */   public TDataLine(TMixer mixer, DataLine.Info info)
/*     */   {
/*  64 */     super(mixer, info);
/*     */ 
/*  66 */     init(info);
/*     */   }
/*     */ 
/*     */   public TDataLine(TMixer mixer, DataLine.Info info, Collection controls)
/*     */   {
/*  75 */     super(mixer, info, controls);
/*     */ 
/*  78 */     init(info);
/*     */   }
/*     */ 
/*     */   private void init(DataLine.Info info)
/*     */   {
/*  86 */     this.m_format = null;
/*  87 */     this.m_nBufferSize = -1;
/*  88 */     setRunning(false);
/*     */   }
/*     */ 
/*     */   public void start()
/*     */   {
/* 102 */     if (TDebug.TraceSourceDataLine)
/*     */     {
/* 104 */       TDebug.out("TDataLine.start(): called");
/*     */     }
/* 106 */     setRunning(true);
/*     */   }
/*     */ 
/*     */   public void stop()
/*     */   {
/* 113 */     if (TDebug.TraceSourceDataLine)
/*     */     {
/* 115 */       TDebug.out("TDataLine.stop(): called");
/*     */     }
/* 117 */     setRunning(false);
/*     */   }
/*     */ 
/*     */   public boolean isRunning()
/*     */   {
/* 124 */     return this.m_bRunning;
/*     */   }
/*     */ 
/*     */   protected void setRunning(boolean bRunning)
/*     */   {
/* 132 */     boolean bOldValue = isRunning();
/* 133 */     this.m_bRunning = bRunning;
/* 134 */     if (bOldValue == isRunning())
/*     */       return;
/* 136 */     if (isRunning())
/*     */     {
/* 138 */       startImpl();
/* 139 */       notifyLineEvent(LineEvent.Type.START);
/*     */     }
/*     */     else
/*     */     {
/* 143 */       stopImpl();
/* 144 */       notifyLineEvent(LineEvent.Type.STOP);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void startImpl()
/*     */   {
/*     */   }
/*     */ 
/*     */   protected void stopImpl()
/*     */   {
/*     */   }
/*     */ 
/*     */   public boolean isActive()
/*     */   {
/* 170 */     return isRunning();
/*     */   }
/*     */ 
/*     */   public AudioFormat getFormat()
/*     */   {
/* 197 */     return this.m_format;
/*     */   }
/*     */ 
/*     */   protected void setFormat(AudioFormat format)
/*     */   {
/* 204 */     if (TDebug.TraceDataLine)
/*     */     {
/* 206 */       TDebug.out("TDataLine.setFormat(): setting: " + format);
/*     */     }
/* 208 */     this.m_format = format;
/*     */   }
/*     */ 
/*     */   public int getBufferSize()
/*     */   {
/* 215 */     return this.m_nBufferSize;
/*     */   }
/*     */ 
/*     */   protected void setBufferSize(int nBufferSize)
/*     */   {
/* 222 */     if (TDebug.TraceDataLine)
/*     */     {
/* 224 */       TDebug.out("TDataLine.setBufferSize(): setting: " + nBufferSize);
/*     */     }
/* 226 */     this.m_nBufferSize = nBufferSize;
/*     */   }
/*     */ 
/*     */   public int getFramePosition()
/*     */   {
/* 239 */     return -1;
/*     */   }
/*     */ 
/*     */   public long getMicrosecondPosition()
/*     */   {
/* 246 */     return (long)(getFramePosition() * getFormat().getFrameRate() * 1000000.0F);
/*     */   }
/*     */ 
/*     */   public float getLevel()
/*     */   {
/* 256 */     return -1.0F;
/*     */   }
/*     */ 
/*     */   protected void checkOpen()
/*     */   {
/* 263 */     if (getFormat() == null)
/*     */     {
/* 265 */       throw new IllegalStateException("format must be specified");
/*     */     }
/* 267 */     if (getBufferSize() != -1)
/*     */       return;
/* 269 */     setBufferSize(getDefaultBufferSize());
/*     */   }
/*     */ 
/*     */   protected int getDefaultBufferSize()
/*     */   {
/* 277 */     return 128000;
/*     */   }
/*     */ 
/*     */   protected void notifyLineEvent(LineEvent.Type type)
/*     */   {
/* 284 */     notifyLineEvent(new LineEvent(this, type, getFramePosition()));
/*     */   }
	
			public long getLongFramePosition()
			{
				// TODO:
				return -1;
			}
/*     */ }
