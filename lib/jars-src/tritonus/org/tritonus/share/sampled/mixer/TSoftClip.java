/*     */ package org.tritonus.share.sampled.mixer;
/*     */ 
/*     */ import java.io.ByteArrayOutputStream;
/*     */ import java.io.IOException;
/*     */ import javax.sound.sampled.AudioFormat;
/*     */ import javax.sound.sampled.AudioInputStream;
/*     */ import javax.sound.sampled.AudioSystem;
/*     */ import javax.sound.sampled.DataLine;
/*     */ import javax.sound.sampled.LineUnavailableException;
/*     */ import javax.sound.sampled.Mixer;
/*     */ import javax.sound.sampled.SourceDataLine;
/*     */ import org.tritonus.share.TDebug;
/*     */ 
/*     */ public class TSoftClip extends TClip
/*     */   implements Runnable
/*     */ {
/*  51 */   private static final Class[] CONTROL_CLASSES = new Class[0];
/*     */   private static final int BUFFER_SIZE = 16384;
/*     */   private Mixer m_mixer;
/*     */   private SourceDataLine m_line;
/*     */   private byte[] m_abClip;
/*     */   private int m_nRepeatCount;
/*     */   private Thread m_thread;
/*     */ 
/*     */   public TSoftClip(Mixer mixer, AudioFormat format)
/*     */     throws LineUnavailableException
/*     */   {
/*  69 */     super(null);
/*  70 */     this.m_mixer = mixer;
/*  71 */     DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
/*     */ 
/*  75 */     this.m_line = ((SourceDataLine)AudioSystem.getLine(info));
/*     */   }
/*     */ 
/*     */   public void open(AudioInputStream audioInputStream)
/*     */     throws LineUnavailableException, IOException
/*     */   {
/*  83 */     AudioFormat audioFormat = audioInputStream.getFormat();
/*  84 */     setFormat(audioFormat);
/*  85 */     int nFrameSize = audioFormat.getFrameSize();
/*  86 */     if (nFrameSize < 1)
/*     */     {
/*  88 */       throw new IllegalArgumentException("frame size must be positive");
/*     */     }
/*  90 */     if (TDebug.TraceClip)
/*     */     {
/*  92 */       TDebug.out("TSoftClip.open(): format: " + audioFormat);
/*     */     }
/*     */ 
/*  95 */     byte[] abData = new byte[16384];
/*  96 */     ByteArrayOutputStream baos = new ByteArrayOutputStream();
/*  97 */     int nBytesRead = 0;
/*  98 */     while (nBytesRead != -1)
/*     */     {
/*     */       try
/*     */       {
/* 102 */         nBytesRead = audioInputStream.read(abData, 0, abData.length);
/*     */       }
/*     */       catch (IOException e)
/*     */       {
/* 106 */         if ((TDebug.TraceClip) || (TDebug.TraceAllExceptions))
/*     */         {
/* 108 */           TDebug.out(e);
/*     */         }
/*     */       }
/* 111 */       if (nBytesRead < 0)
/*     */         continue;
/* 113 */       if (TDebug.TraceClip)
/*     */       {
/* 115 */         TDebug.out("TSoftClip.open(): Trying to write: " + nBytesRead);
/*     */       }
/* 117 */       baos.write(abData, 0, nBytesRead);
/* 118 */       if (!TDebug.TraceClip)
/*     */         continue;
/* 120 */       TDebug.out("TSoftClip.open(): Written: " + nBytesRead);
/*     */     }
/*     */ 
/* 124 */     this.m_abClip = baos.toByteArray();
/* 125 */     setBufferSize(this.m_abClip.length);
/*     */ 
/* 127 */     this.m_line.open(getFormat());
/*     */   }
/*     */ 
/*     */   public int getFrameLength()
/*     */   {
/* 136 */     if (isOpen())
/*     */     {
/* 138 */       return getBufferSize() / getFormat().getFrameSize();
/*     */     }
/*     */ 
/* 142 */     return -1;
/*     */   }
/*     */ 
/*     */   public long getMicrosecondLength()
/*     */   {
/* 150 */     if (isOpen())
/*     */     {
/* 152 */       return (long)(getFrameLength() * getFormat().getFrameRate() * 1000000.0F);
/*     */     }
/*     */ 
/* 156 */     return -1L;
/*     */   }
/*     */ 
/*     */   public void setFramePosition(int nPosition)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void setMicrosecondPosition(long lPosition)
/*     */   {
/*     */   }
/*     */ 
/*     */   public int getFramePosition()
/*     */   {
/* 179 */     return -1;
/*     */   }
/*     */ 
/*     */   public long getMicrosecondPosition()
/*     */   {
/* 187 */     return -1L;
/*     */   }
/*     */ 
/*     */   public void setLoopPoints(int nStart, int nEnd)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void loop(int nCount)
/*     */   {
/* 201 */     if (TDebug.TraceClip)
/*     */     {
/* 203 */       TDebug.out("TSoftClip.loop(int): called; count = " + nCount);
/*     */     }
/*     */ 
/* 222 */     this.m_nRepeatCount = nCount;
/* 223 */     this.m_thread = new Thread(this);
/* 224 */     this.m_thread.start();
/*     */   }
/*     */ 
/*     */   public void flush()
/*     */   {
/*     */   }
/*     */ 
/*     */   public void drain()
/*     */   {
/*     */   }
/*     */ 
/*     */   public void close()
/*     */   {
/*     */   }
/*     */ 
/*     */   public void open()
/*     */   {
/*     */   }
/*     */ 
/*     */   public void start()
/*     */   {
/* 264 */     if (TDebug.TraceClip)
/*     */     {
/* 266 */       TDebug.out("TSoftClip.start(): called");
/*     */     }
/*     */ 
/* 272 */     if (TDebug.TraceClip)
/*     */     {
/* 274 */       TDebug.out("TSoftClip.start(): calling 'loop(0)' [hack]");
/*     */     }
/* 276 */     loop(0);
/*     */   }
/*     */ 
/*     */   public void stop()
/*     */   {
/*     */   }
/*     */ 
/*     */   public int available()
/*     */   {
/* 295 */     return -1;
/*     */   }
/*     */ 
/*     */   public void run()
/*     */   {
/* 302 */     while (this.m_nRepeatCount >= 0)
/*     */     {
/* 304 */       this.m_line.write(this.m_abClip, 0, this.m_abClip.length);
/* 305 */       this.m_nRepeatCount -= 1;
/*     */     }
/*     */   }
/*     */ }
