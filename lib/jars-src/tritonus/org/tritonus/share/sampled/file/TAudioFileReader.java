/*     */ package org.tritonus.share.sampled.file;
/*     */ 
/*     */ import java.io.BufferedInputStream;
/*     */ import java.io.DataInputStream;
/*     */ import java.io.EOFException;
/*     */ import java.io.File;
/*     */ import java.io.FileInputStream;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.net.URL;
/*     */ import javax.sound.sampled.AudioFileFormat;
/*     */ import javax.sound.sampled.AudioInputStream;
/*     */ import javax.sound.sampled.UnsupportedAudioFileException;
/*     */ import javax.sound.sampled.spi.AudioFileReader;
/*     */ import org.tritonus.share.TDebug;
/*     */ 
/*     */ public abstract class TAudioFileReader extends AudioFileReader
/*     */ {
/*  60 */   private int m_nMarkLimit = -1;
/*     */   private boolean m_bRereading;
/*     */ 
/*     */   protected TAudioFileReader(int nMarkLimit)
/*     */   {
/*  66 */     this(nMarkLimit, false);
/*     */   }
/*     */ 
/*     */   protected TAudioFileReader(int nMarkLimit, boolean bRereading)
/*     */   {
/*  73 */     this.m_nMarkLimit = nMarkLimit;
/*  74 */     this.m_bRereading = bRereading;
/*     */   }
/*     */ 
/*     */   private int getMarkLimit()
/*     */   {
/*  81 */     return this.m_nMarkLimit;
/*     */   }
/*     */ 
/*     */   private boolean isRereading()
/*     */   {
/*  88 */     return this.m_bRereading;
/*     */   }
/*     */ 
/*     */   public AudioFileFormat getAudioFileFormat(File file)
/*     */     throws UnsupportedAudioFileException, IOException
/*     */   {
/* 106 */     if (TDebug.TraceAudioFileReader) TDebug.out("TAudioFileReader.getAudioFileFormat(File): begin");
/* 107 */     long lFileLengthInBytes = file.length();
/* 108 */     InputStream inputStream = new FileInputStream(file);
/* 109 */     AudioFileFormat audioFileFormat = null;
/*     */     try
/*     */     {
/* 112 */       audioFileFormat = getAudioFileFormat(inputStream, lFileLengthInBytes);
/*     */     }
/*     */     finally
/*     */     {
/* 116 */       inputStream.close();
/*     */     }
/* 118 */     if (TDebug.TraceAudioFileReader) TDebug.out("TAudioFileReader.getAudioFileFormat(File): end");
/* 119 */     return audioFileFormat;
/*     */   }
/*     */ 
/*     */   public AudioFileFormat getAudioFileFormat(URL url)
/*     */     throws UnsupportedAudioFileException, IOException
/*     */   {
/* 138 */     if (TDebug.TraceAudioFileReader) TDebug.out("TAudioFileReader.getAudioFileFormat(URL): begin");
/* 139 */     long lFileLengthInBytes = -1L;
/* 140 */     InputStream inputStream = url.openStream();
/* 141 */     AudioFileFormat audioFileFormat = null;
/*     */     try
/*     */     {
/* 144 */       audioFileFormat = getAudioFileFormat(inputStream, lFileLengthInBytes);
/*     */     }
/*     */     finally
/*     */     {
/* 148 */       inputStream.close();
/*     */     }
/* 150 */     if (TDebug.TraceAudioFileReader) TDebug.out("TAudioFileReader.getAudioFileFormat(URL): end");
/* 151 */     return audioFileFormat;
/*     */   }
/*     */ 
/*     */   public AudioFileFormat getAudioFileFormat(InputStream inputStream)
/*     */     throws UnsupportedAudioFileException, IOException
/*     */   {
/* 170 */     if (TDebug.TraceAudioFileReader) TDebug.out("TAudioFileReader.getAudioFileFormat(InputStream): begin");
/* 171 */     long lFileLengthInBytes = -1L;
/* 172 */     inputStream.mark(getMarkLimit());
/* 173 */     AudioFileFormat audioFileFormat = null;
/*     */     try
/*     */     {
/* 176 */       audioFileFormat = getAudioFileFormat(inputStream, lFileLengthInBytes);
/*     */     }
/*     */     finally
/*     */     {
/* 184 */       inputStream.reset();
/*     */     }
/* 186 */     if (TDebug.TraceAudioFileReader) TDebug.out("TAudioFileReader.getAudioFileFormat(InputStream): end");
/* 187 */     return audioFileFormat;
/*     */   }
/*     */ 
/*     */   protected abstract AudioFileFormat getAudioFileFormat(InputStream paramInputStream, long paramLong)
/*     */     throws UnsupportedAudioFileException, IOException;
/*     */ 
/*     */   public AudioInputStream getAudioInputStream(File file)
/*     */     throws UnsupportedAudioFileException, IOException
/*     */   {
/* 233 */     if (TDebug.TraceAudioFileReader) TDebug.out("TAudioFileReader.getAudioInputStream(File): begin");
/* 234 */     long lFileLengthInBytes = file.length();
/* 235 */     InputStream inputStream = new FileInputStream(file);
/* 236 */     AudioInputStream audioInputStream = null;
/*     */     try
/*     */     {
/* 239 */       audioInputStream = getAudioInputStream(inputStream, lFileLengthInBytes);
/*     */     }
/*     */     catch (UnsupportedAudioFileException e)
/*     */     {
/* 243 */       inputStream.close();
/* 244 */       throw e;
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 248 */       inputStream.close();
/* 249 */       throw e;
/*     */     }
/* 251 */     if (TDebug.TraceAudioFileReader) TDebug.out("TAudioFileReader.getAudioInputStream(File): end");
/* 252 */     return audioInputStream;
/*     */   }
/*     */ 
/*     */   public AudioInputStream getAudioInputStream(URL url)
/*     */     throws UnsupportedAudioFileException, IOException
/*     */   {
/* 271 */     if (TDebug.TraceAudioFileReader) TDebug.out("TAudioFileReader.getAudioInputStream(URL): begin");
/* 272 */     long lFileLengthInBytes = -1L;
/* 273 */     InputStream inputStream = url.openStream();
/* 274 */     AudioInputStream audioInputStream = null;
/*     */     try
/*     */     {
/* 277 */       audioInputStream = getAudioInputStream(inputStream, lFileLengthInBytes);
/*     */     }
/*     */     catch (UnsupportedAudioFileException e)
/*     */     {
/* 281 */       inputStream.close();
/* 282 */       throw e;
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 286 */       inputStream.close();
/* 287 */       throw e;
/*     */     }
/* 289 */     if (TDebug.TraceAudioFileReader) TDebug.out("TAudioFileReader.getAudioInputStream(URL): end");
/* 290 */     return audioInputStream;
/*     */   }
/*     */ 
/*     */   public AudioInputStream getAudioInputStream(InputStream inputStream)
/*     */     throws UnsupportedAudioFileException, IOException
/*     */   {
/* 309 */     if (TDebug.TraceAudioFileReader) TDebug.out("TAudioFileReader.getAudioInputStream(InputStream): begin");
/* 310 */     long lFileLengthInBytes = -1L;
/* 311 */     AudioInputStream audioInputStream = null;
/* 312 */     inputStream.mark(getMarkLimit());
/*     */     try
/*     */     {
/* 315 */       audioInputStream = getAudioInputStream(inputStream, lFileLengthInBytes);
/*     */     }
/*     */     catch (UnsupportedAudioFileException e)
/*     */     {
/* 319 */       inputStream.reset();
/* 320 */       throw e;
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 324 */       inputStream.reset();
/* 325 */       throw e;
/*     */     }
/* 327 */     if (TDebug.TraceAudioFileReader) TDebug.out("TAudioFileReader.getAudioInputStream(InputStream): end");
/* 328 */     return audioInputStream;
/*     */   }
/*     */ 
/*     */   protected AudioInputStream getAudioInputStream(InputStream inputStream, long lFileLengthInBytes)
/*     */     throws UnsupportedAudioFileException, IOException
/*     */   {
/* 354 */     if (TDebug.TraceAudioFileReader) TDebug.out("TAudioFileReader.getAudioInputStream(InputStream, long): begin");
/* 355 */     if (isRereading())
/*     */     {
/* 357 */       inputStream = new BufferedInputStream(inputStream, getMarkLimit());
/* 358 */       inputStream.mark(getMarkLimit());
/*     */     }
/* 360 */     AudioFileFormat audioFileFormat = getAudioFileFormat(inputStream, lFileLengthInBytes);
/* 361 */     if (isRereading())
/*     */     {
/* 363 */       inputStream.reset();
/*     */     }
/* 365 */     AudioInputStream audioInputStream = new AudioInputStream(inputStream, audioFileFormat.getFormat(), audioFileFormat.getFrameLength());
/*     */ 
/* 369 */     if (TDebug.TraceAudioFileReader) TDebug.out("TAudioFileReader.getAudioInputStream(InputStream, long): end");
/* 370 */     return audioInputStream;
/*     */   }
/*     */ 
/*     */   protected static int calculateFrameSize(int nSampleSize, int nNumChannels)
/*     */   {
/* 377 */     return (nSampleSize + 7) / 8 * nNumChannels;
/*     */   }
/*     */ 
/*     */   public static int readLittleEndianInt(InputStream is)
/*     */     throws IOException
/*     */   {
/* 385 */     int b0 = is.read();
/* 386 */     int b1 = is.read();
/* 387 */     int b2 = is.read();
/* 388 */     int b3 = is.read();
/* 389 */     if ((b0 | b1 | b2 | b3) < 0)
/*     */     {
/* 391 */       throw new EOFException();
/*     */     }
/* 393 */     return (b3 << 24) + (b2 << 16) + (b1 << 8) + (b0 << 0);
/*     */   }
/*     */ 
/*     */   public static short readLittleEndianShort(InputStream is)
/*     */     throws IOException
/*     */   {
/* 401 */     int b0 = is.read();
/* 402 */     int b1 = is.read();
/* 403 */     if ((b0 | b1) < 0)
/*     */     {
/* 405 */       throw new EOFException();
/*     */     }
/* 407 */     return (short)((b1 << 8) + (b0 << 0));
/*     */   }
/*     */ 
/*     */   public static double readIeeeExtended(DataInputStream dis)
/*     */     throws IOException
/*     */   {
/* 450 */     double f = 0.0D;
/* 451 */     int expon = 0;
/* 452 */     long hiMant = 0L;
/* 453 */     long loMant = 0L;
/* 454 */     double HUGE = 3.402823466385289E+38D;
/* 455 */     expon = dis.readUnsignedShort();
/* 456 */     long t1 = dis.readUnsignedShort();
/* 457 */     long t2 = dis.readUnsignedShort();
/* 458 */     hiMant = t1 << 16 | t2;
/* 459 */     t1 = dis.readUnsignedShort();
/* 460 */     t2 = dis.readUnsignedShort();
/* 461 */     loMant = t1 << 16 | t2;
/* 462 */     if ((expon == 0) && (hiMant == 0L) && (loMant == 0L))
/*     */     {
/* 464 */       f = 0.0D;
/*     */     }
/* 468 */     else if (expon == 32767)
/*     */     {
/* 470 */       f = HUGE;
/*     */     }
/*     */     else
/*     */     {
/* 474 */       expon -= 16383;
/* 475 */       expon -= 31;
/* 476 */       f = hiMant * Math.pow(2.0D, expon);
/* 477 */       expon -= 32;
/* 478 */       f += loMant * Math.pow(2.0D, expon);
/*     */     }
/*     */ 
/* 481 */     return f;
/*     */   }
/*     */ }
