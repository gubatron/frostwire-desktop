/*     */ package org.tritonus.share.sampled.mixer;
/*     */ 
/*     */ import java.io.ByteArrayInputStream;
/*     */ import java.io.IOException;
/*     */ import java.util.Collection;
/*     */ import javax.sound.sampled.AudioFormat;
/*     */ import javax.sound.sampled.AudioInputStream;
/*     */ import javax.sound.sampled.Clip;
/*     */ import javax.sound.sampled.DataLine;
/*     */ import javax.sound.sampled.LineUnavailableException;
/*     */ import org.tritonus.share.TDebug;
/*     */ 
/*     */ public class TClip extends TDataLine
/*     */   implements Clip
/*     */ {
/*  51 */   private static final Class[] CONTROL_CLASSES = new Class[0];
/*     */   private static final int BUFFER_FRAMES = 16384;
/*     */ 
/*     */   public TClip(DataLine.Info info)
/*     */   {
/*  62 */     super(null, info);
/*     */   }
/*     */ 
/*     */   public TClip(DataLine.Info info, Collection controls)
/*     */   {
/*  71 */     super(null, info, controls);
/*     */   }
/*     */ 
/*     */   public void open(AudioFormat audioFormat, byte[] abData, int nOffset, int nLength)
/*     */     throws LineUnavailableException
/*     */   {
/*  87 */     ByteArrayInputStream bais = new ByteArrayInputStream(abData, nOffset, nLength);
/*  88 */     AudioInputStream audioInputStream = new AudioInputStream(bais, audioFormat, -1L);
/*     */     try
/*     */     {
/*  91 */       open(audioInputStream);
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/*  95 */       if (TDebug.TraceAllExceptions)
/*     */       {
/*  97 */         TDebug.out(e);
/*     */       }
/*  99 */       throw new LineUnavailableException("IOException occured");
/*     */     }
/*     */   }
/*     */ 
/*     */   public void open(AudioInputStream audioInputStream)
/*     */     throws LineUnavailableException, IOException
/*     */   {
/* 108 */     AudioFormat audioFormat = audioInputStream.getFormat();
/*     */ 
/* 110 */     DataLine.Info info = new DataLine.Info(Clip.class, audioFormat, -1);
/*     */   }
/*     */ 
/*     */   public int getFrameLength()
/*     */   {
/* 169 */     return -1;
/*     */   }
/*     */ 
/*     */   public long getMicrosecondLength()
/*     */   {
/* 177 */     return -1L;
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
/* 199 */     return -1;
/*     */   }
/*     */ 
/*     */   public long getMicrosecondPosition()
/*     */   {
/* 207 */     return -1L;
/*     */   }
/*     */ 
/*     */   public void setLoopPoints(int nStart, int nEnd)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void loop(int nCount)
/*     */   {
/* 221 */     if (TDebug.TraceClip)
/*     */     {
/* 223 */       TDebug.out("TClip.loop(int): called; count = " + nCount);
/*     */     }
/*     */ 
/* 242 */     if (nCount == 0)
/*     */     {
/* 244 */       if (!TDebug.TraceClip)
/*     */         return;
/* 246 */       TDebug.out("TClip.loop(int): starting sample (once)");
/*     */     }
/*     */     else
/*     */     {
/* 258 */       if (!TDebug.TraceClip)
/*     */         return;
/* 260 */       TDebug.out("TClip.loop(int): starting sample (forever)");
/*     */     }
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
/* 303 */     if (TDebug.TraceClip)
/*     */     {
/* 305 */       TDebug.out("TClip.start(): called");
/*     */     }
/*     */ 
/* 311 */     if (TDebug.TraceClip)
/*     */     {
/* 313 */       TDebug.out("TClip.start(): calling 'loop(0)' [hack]");
/*     */     }
/* 315 */     loop(0);
/*     */   }
/*     */ 
/*     */   public void stop()
/*     */   {
/*     */   }
/*     */ 
/*     */   public int available()
/*     */   {
/* 334 */     return -1;
/*     */   }
/*     */ }
