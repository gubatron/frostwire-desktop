/*     */ package org.tritonus.share.sampled.convert;
/*     */ 
/*     */ import java.io.ByteArrayInputStream;
/*     */ import java.io.IOException;
/*     */ import javax.sound.sampled.AudioFormat;
/*     */ import org.tritonus.share.TCircularBuffer;
/*     */ import org.tritonus.share.TCircularBuffer;
/*     */ import org.tritonus.share.TDebug;
/*     */ 
/*     */ public abstract class TAsynchronousFilteredAudioInputStream extends TAudioInputStream
/*     */   implements TCircularBuffer.Trigger
/*     */ {
/*     */   private static final int DEFAULT_BUFFER_SIZE = 327670;
/*     */   private static final int DEFAULT_MIN_AVAILABLE = 4096;
/*  56 */   private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
/*     */   private TCircularBuffer m_circularBuffer;
/*     */   private int m_nMinAvailable;
/*     */   private byte[] m_abSingleByte;
/*     */ 
/*     */   public TAsynchronousFilteredAudioInputStream(AudioFormat outputFormat, long lLength)
/*     */   {
/*  74 */     this(outputFormat, lLength, 327670, 4096);
/*     */   }
/*     */ 
/*     */   public TAsynchronousFilteredAudioInputStream(AudioFormat outputFormat, long lLength, int nBufferSize, int nMinAvailable)
/*     */   {
/* 105 */     super(new ByteArrayInputStream(EMPTY_BYTE_ARRAY), outputFormat, lLength);
/*     */ 
/* 108 */     if (TDebug.TraceAudioConverter) TDebug.out("TAsynchronousFilteredAudioInputStream.<init>(): begin");
/* 109 */     this.m_circularBuffer = new TCircularBuffer(nBufferSize, false, true, this);
/*     */ 
/* 114 */     this.m_nMinAvailable = nMinAvailable;
/* 115 */     if (!TDebug.TraceAudioConverter) return; TDebug.out("TAsynchronousFilteredAudioInputStream.<init>(): end");
/*     */   }
/*     */ 
/*     */   protected TCircularBuffer getCircularBuffer()
/*     */   {
/* 123 */     return this.m_circularBuffer;
/*     */   }
/*     */ 
/*     */   protected boolean writeMore()
/*     */   {
/* 141 */     return getCircularBuffer().availableWrite() > this.m_nMinAvailable;
/*     */   }
/*     */ 
/*     */   public int read()
/*     */     throws IOException
/*     */   {
/* 150 */     int nByte = -1;
/* 151 */     if (this.m_abSingleByte == null)
/*     */     {
/* 153 */       this.m_abSingleByte = new byte[1];
/*     */     }
/* 155 */     int nReturn = read(this.m_abSingleByte);
/* 156 */     if (nReturn == -1)
/*     */     {
/* 158 */       nByte = -1;
/*     */     }
/*     */     else
/*     */     {
/* 163 */       nByte = this.m_abSingleByte[0] & 0xFF;
/*     */     }
/*     */ 
/* 166 */     return nByte;
/*     */   }
/*     */ 
/*     */   public int read(byte[] abData)
/*     */     throws IOException
/*     */   {
/* 174 */     if (TDebug.TraceAudioConverter) TDebug.out("TAsynchronousFilteredAudioInputStream.read(byte[]): begin");
/* 175 */     int nRead = read(abData, 0, abData.length);
/* 176 */     if (TDebug.TraceAudioConverter) TDebug.out("TAsynchronousFilteredAudioInputStream.read(byte[]): end");
/* 177 */     return nRead;
/*     */   }
/*     */ 
/*     */   public int read(byte[] abData, int nOffset, int nLength)
/*     */     throws IOException
/*     */   {
/* 185 */     if (TDebug.TraceAudioConverter) TDebug.out("TAsynchronousFilteredAudioInputStream.read(byte[], int, int): begin");
/*     */ 
/* 189 */     int nRead = this.m_circularBuffer.read(abData, nOffset, nLength);
/* 190 */     if (TDebug.TraceAudioConverter) TDebug.out("TAsynchronousFilteredAudioInputStream.read(byte[], int, int): end");
/* 191 */     return nRead;
/*     */   }
/*     */ 
/*     */   public long skip(long lSkip)
/*     */     throws IOException
/*     */   {
/* 200 */     for (long lSkipped = 0L; lSkipped < lSkip; lSkipped += 1L)
/*     */     {
/* 202 */       int nReturn = read();
/* 203 */       if (nReturn == -1)
/*     */       {
/* 205 */         return lSkipped;
/*     */       }
/*     */     }
/* 208 */     return lSkip;
/*     */   }
/*     */ 
/*     */   public int available()
/*     */     throws IOException
/*     */   {
/* 216 */     return this.m_circularBuffer.availableRead();
/*     */   }
/*     */ 
/*     */   public void close()
/*     */     throws IOException
/*     */   {
/* 224 */     this.m_circularBuffer.close();
/*     */   }
/*     */ 
/*     */   public boolean markSupported()
/*     */   {
/* 231 */     return false;
/*     */   }
/*     */ 
/*     */   public void mark(int nReadLimit)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void reset()
/*     */     throws IOException
/*     */   {
/* 245 */     throw new IOException("mark not supported");
/*     */   }
/*     */ }
