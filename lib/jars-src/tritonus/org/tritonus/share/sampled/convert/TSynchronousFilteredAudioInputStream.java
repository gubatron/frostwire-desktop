/*     */ package org.tritonus.share.sampled.convert;
/*     */ 
/*     */ import java.io.IOException;
/*     */ import javax.sound.sampled.AudioFormat;
/*     */ import javax.sound.sampled.AudioInputStream;
/*     */ import org.tritonus.share.TDebug;
/*     */ import org.tritonus.share.sampled.AudioUtils;
/*     */ 
/*     */ public abstract class TSynchronousFilteredAudioInputStream extends TAudioInputStream
/*     */ {
/*     */   private AudioInputStream originalStream;
/*     */   private AudioFormat originalFormat;
/*     */   private int originalFrameSize;
/*     */   private int newFrameSize;
/*  65 */   protected byte[] buffer = null;
/*     */ 
/*  71 */   private boolean m_bConvertInPlace = false;
/*     */ 
/*     */   public TSynchronousFilteredAudioInputStream(AudioInputStream audioInputStream, AudioFormat newFormat)
/*     */   {
/*  75 */     super(audioInputStream, newFormat, audioInputStream.getFrameLength());
/*  76 */     this.originalStream = audioInputStream;
/*  77 */     this.originalFormat = audioInputStream.getFormat();
/*  78 */     this.originalFrameSize = ((this.originalFormat.getFrameSize() <= 0) ? 1 : this.originalFormat.getFrameSize());
/*     */ 
/*  80 */     this.newFrameSize = ((getFormat().getFrameSize() <= 0) ? 1 : getFormat().getFrameSize());
/*     */ 
/*  82 */     if (TDebug.TraceAudioConverter) {
/*  83 */       TDebug.out("TSynchronousFilteredAudioInputStream: original format =" + AudioUtils.format2ShortStr(this.originalFormat));
/*     */ 
/*  85 */       TDebug.out("TSynchronousFilteredAudioInputStream: converted format=" + AudioUtils.format2ShortStr(getFormat()));
/*     */     }
/*     */ 
/*  92 */     this.m_bConvertInPlace = false;
/*     */   }
/*     */ 
/*     */   protected boolean enableConvertInPlace() {
/*  96 */     if (this.newFrameSize >= this.originalFrameSize) {
/*  97 */       this.m_bConvertInPlace = true;
/*     */     }
/*  99 */     return this.m_bConvertInPlace;
/*     */   }
/*     */ 
/*     */   protected abstract int convert(byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, int paramInt1, int paramInt2);
/*     */ 
/*     */   protected void convertInPlace(byte[] buffer, int byteOffset, int frameCount)
/*     */   {
/* 123 */     throw new RuntimeException("Illegal call to convertInPlace");
/*     */   }
/*     */ 
/*     */   public int read() throws IOException
/*     */   {
/* 128 */     if (this.newFrameSize != 1) {
/* 129 */       throw new IOException("frame size must be 1 to read a single byte");
/*     */     }
/*     */ 
/* 133 */     byte[] temp = new byte[1];
/* 134 */     int result = read(temp);
/* 135 */     if (result == -1) {
/* 136 */       return -1;
/*     */     }
/* 138 */     if (result == 0)
/*     */     {
/* 140 */       return -1;
/*     */     }
/* 142 */     return temp[0] & 0xFF;
/*     */   }
/*     */ 
/*     */   private void clearBuffer()
/*     */   {
/* 148 */     this.buffer = null;
/*     */   }
/*     */ 
/*     */   public AudioInputStream getOriginalStream() {
/* 152 */     return this.originalStream;
/*     */   }
/*     */ 
/*     */   public AudioFormat getOriginalFormat() {
/* 156 */     return this.originalFormat;
/*     */   }
/*     */ 
/*     */   public int read(byte[] abData, int nOffset, int nLength)
/*     */     throws IOException
/*     */   {
/* 168 */     int nFrameLength = nLength / this.newFrameSize;
/*     */ 
/* 171 */     int originalBytes = nFrameLength * this.originalFrameSize;
/*     */ 
/* 173 */     if (TDebug.TraceAudioConverter) {
/* 174 */       TDebug.out("> TSynchronousFilteredAIS.read(buffer[" + abData.length + "], " + nOffset + " ," + nLength + " bytes ^=" + nFrameLength + " frames)");
/*     */     }
/*     */ 
/* 177 */     int nFramesConverted = 0;
/*     */     byte[] readBuffer;
/*     */     int readOffset;
/* 182 */     if (this.m_bConvertInPlace) {
/* 183 */       readBuffer = abData;
/* 184 */       readOffset = nOffset;
/*     */     }
/*     */     else {
/* 187 */       if ((this.buffer == null) || (this.buffer.length < originalBytes)) {
/* 188 */         this.buffer = new byte[originalBytes];
/*     */       }
/* 190 */       readBuffer = this.buffer;
/* 191 */       readOffset = 0;
/*     */     }
/* 193 */     int nBytesRead = this.originalStream.read(readBuffer, readOffset, originalBytes);
/* 194 */     if (nBytesRead == -1)
/*     */     {
/* 196 */       clearBuffer();
/* 197 */       return -1;
/*     */     }
/* 199 */     int nFramesRead = nBytesRead / this.originalFrameSize;
/* 200 */     if (TDebug.TraceAudioConverter) {
/* 201 */       TDebug.out("original.read returned " + nBytesRead + " bytes ^=" + nFramesRead + " frames");
/*     */     }
/*     */ 
/* 204 */     if (this.m_bConvertInPlace) {
/* 205 */       convertInPlace(abData, nOffset, nFramesRead);
/* 206 */       nFramesConverted = nFramesRead;
/*     */     } else {
/* 208 */       nFramesConverted = convert(this.buffer, abData, nOffset, nFramesRead);
/*     */     }
/* 210 */     if (TDebug.TraceAudioConverter) {
/* 211 */       TDebug.out("< converted " + nFramesConverted + " frames");
/*     */     }
/* 213 */     return nFramesConverted * this.newFrameSize;
/*     */   }
/*     */ 
/*     */   public long skip(long nSkip)
/*     */     throws IOException
/*     */   {
/* 220 */     long skipFrames = nSkip / this.newFrameSize;
/* 221 */     long originalSkippedBytes = this.originalStream.skip(skipFrames * this.originalFrameSize);
/* 222 */     long skippedFrames = originalSkippedBytes / this.originalFrameSize;
/* 223 */     return skippedFrames * this.newFrameSize;
/*     */   }
/*     */ 
/*     */   public int available()
/*     */     throws IOException
/*     */   {
/* 229 */     int origAvailFrames = this.originalStream.available() / this.originalFrameSize;
/* 230 */     return origAvailFrames * this.newFrameSize;
/*     */   }
/*     */ 
/*     */   public void close()
/*     */     throws IOException
/*     */   {
/* 236 */     this.originalStream.close();
/* 237 */     clearBuffer();
/*     */   }
/*     */ 
/*     */   public void mark(int readlimit)
/*     */   {
/* 243 */     int readLimitFrames = readlimit / this.newFrameSize;
/* 244 */     this.originalStream.mark(readLimitFrames * this.originalFrameSize);
/*     */   }
/*     */ 
/*     */   public void reset()
/*     */     throws IOException
/*     */   {
/* 251 */     this.originalStream.reset();
/*     */   }
/*     */ 
/*     */   public boolean markSupported()
/*     */   {
/* 256 */     return this.originalStream.markSupported();
/*     */   }
/*     */ 
/*     */   private int getFrameSize()
/*     */   {
/* 261 */     return getFormat().getFrameSize();
/*     */   }
/*     */ }
