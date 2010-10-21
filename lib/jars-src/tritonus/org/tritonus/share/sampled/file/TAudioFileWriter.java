/*     */ package org.tritonus.share.sampled.file;
/*     */ 
/*     */ import java.io.File;
/*     */ import java.io.IOException;
/*     */ import java.io.OutputStream;
/*     */ import java.util.Collection;
/*     */ import java.util.Iterator;
/*     */ import javax.sound.sampled.AudioFileFormat;
/*     */ import javax.sound.sampled.AudioFormat;
/*     */ import javax.sound.sampled.AudioInputStream;
/*     */ import javax.sound.sampled.spi.AudioFileWriter;
/*     */ import org.tritonus.share.ArraySet;
/*     */ import org.tritonus.share.TDebug;
/*     */ import org.tritonus.share.sampled.AudioFormats;
/*     */ import org.tritonus.share.sampled.AudioUtils;
/*     */ import org.tritonus.share.sampled.Encodings;
/*     */ import org.tritonus.share.sampled.TConversionTool;
/*     */ 
/*     */ public abstract class TAudioFileWriter extends AudioFileWriter
/*     */ {
/*     */   protected static final int ALL = -1;
/*  67 */   public static AudioFormat.Encoding PCM_SIGNED = Encodings.getEncoding("PCM_SIGNED");
/*  68 */   public static AudioFormat.Encoding PCM_UNSIGNED = Encodings.getEncoding("PCM_UNSIGNED");
/*     */   private static final int BUFFER_LENGTH = 16384;
/*  77 */   protected static final AudioFileFormat.Type[] NULL_TYPE_ARRAY = new AudioFileFormat.Type[0];
/*     */   private Collection m_audioFileTypes;
/*     */   private Collection m_audioFormats;
/*     */ 
/*     */   protected TAudioFileWriter(Collection fileTypes, Collection audioFormats)
/*     */   {
/* 101 */     if (TDebug.TraceAudioFileWriter) TDebug.out("TAudioFileWriter.<init>(): begin");
/* 102 */     this.m_audioFileTypes = fileTypes;
/* 103 */     this.m_audioFormats = audioFormats;
/* 104 */     if (!TDebug.TraceAudioFileWriter) return; TDebug.out("TAudioFileWriter.<init>(): end");
/*     */   }
/*     */ 
/*     */   public AudioFileFormat.Type[] getAudioFileTypes()
/*     */   {
/* 110 */     return (AudioFileFormat.Type[])this.m_audioFileTypes.toArray(NULL_TYPE_ARRAY);
/*     */   }
/*     */ 
/*     */   public boolean isFileTypeSupported(AudioFileFormat.Type fileType)
/*     */   {
/* 117 */     return this.m_audioFileTypes.contains(fileType);
/*     */   }
/*     */ 
/*     */   public AudioFileFormat.Type[] getAudioFileTypes(AudioInputStream audioInputStream)
/*     */   {
/* 128 */     AudioFormat format = audioInputStream.getFormat();
/* 129 */     ArraySet res = new ArraySet();
/* 130 */     Iterator it = this.m_audioFileTypes.iterator();
/* 131 */     while (it.hasNext()) {
/* 132 */       AudioFileFormat.Type thisType = (AudioFileFormat.Type)it.next();
/* 133 */       if (isAudioFormatSupportedImpl(format, thisType)) {
/* 134 */         res.add(thisType);
/*     */       }
/*     */     }
/* 137 */     return (AudioFileFormat.Type[])res.toArray(NULL_TYPE_ARRAY);
/*     */   }
/*     */ 
/*     */   public boolean isFileTypeSupported(AudioFileFormat.Type fileType, AudioInputStream audioInputStream)
/*     */   {
/* 146 */     return (isFileTypeSupported(fileType)) && (((isAudioFormatSupportedImpl(audioInputStream.getFormat(), fileType)) || (findConvertableFormat(audioInputStream.getFormat(), fileType) != null)));
/*     */   }
/*     */ 
/*     */   public int write(AudioInputStream audioInputStream, AudioFileFormat.Type fileType, File file)
/*     */     throws IOException
/*     */   {
/* 163 */     if (TDebug.TraceAudioFileWriter)
/*     */     {
/* 165 */       TDebug.out(">TAudioFileWriter.write(.., File): called");
/* 166 */       TDebug.out("class: " + super.getClass().getName());
/*     */     }
/*     */ 
/* 169 */     if (!isFileTypeSupported(fileType)) {
/* 170 */       if (TDebug.TraceAudioFileWriter)
/*     */       {
/* 172 */         TDebug.out("< file type is not supported");
/*     */       }
/* 174 */       throw new IllegalArgumentException("file type is not supported.");
/*     */     }
/*     */ 
/* 177 */     AudioFormat inputFormat = audioInputStream.getFormat();
/* 178 */     if (TDebug.TraceAudioFileWriter) TDebug.out("input format: " + inputFormat);
/* 179 */     AudioFormat outputFormat = null;
/* 180 */     boolean bNeedsConversion = false;
/* 181 */     if (isAudioFormatSupportedImpl(inputFormat, fileType))
/*     */     {
/* 183 */       if (TDebug.TraceAudioFileWriter) TDebug.out("input format is supported directely");
/* 184 */       outputFormat = inputFormat;
/* 185 */       bNeedsConversion = false;
/*     */     }
/*     */     else
/*     */     {
/* 189 */       if (TDebug.TraceAudioFileWriter) TDebug.out("input format is not supported directely; trying to find a convertable format");
/* 190 */       outputFormat = findConvertableFormat(inputFormat, fileType);
/* 191 */       if (outputFormat != null)
/*     */       {
/* 193 */         bNeedsConversion = true;
/*     */ 
/* 196 */         if ((outputFormat.getSampleSizeInBits() == 8) && (outputFormat.getEncoding().equals(inputFormat.getEncoding())))
/*     */         {
/* 198 */           bNeedsConversion = false;
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/* 203 */         if (TDebug.TraceAudioFileWriter) TDebug.out("< input format is not supported and not convertable.");
/* 204 */         throw new IllegalArgumentException("format not supported and not convertable");
/*     */       }
/*     */     }
/* 207 */     long lLengthInBytes = AudioUtils.getLengthInBytes(audioInputStream);
/* 208 */     TDataOutputStream dataOutputStream = new TSeekableDataOutputStream(file);
/* 209 */     AudioOutputStream audioOutputStream = getAudioOutputStream(outputFormat, lLengthInBytes, fileType, dataOutputStream);
/*     */ 
/* 215 */     int written = writeImpl(audioInputStream, audioOutputStream, bNeedsConversion);
/*     */ 
/* 218 */     if (TDebug.TraceAudioFileWriter)
/*     */     {
/* 220 */       TDebug.out("< wrote " + written + " bytes.");
/*     */     }
/* 222 */     return written;
/*     */   }
/*     */ 
/*     */   public int write(AudioInputStream audioInputStream, AudioFileFormat.Type fileType, OutputStream outputStream)
/*     */     throws IOException
/*     */   {
/* 234 */     if (!isFileTypeSupported(fileType)) {
/* 235 */       throw new IllegalArgumentException("file type is not supported.");
/*     */     }
/* 237 */     if (TDebug.TraceAudioFileWriter)
/*     */     {
/* 239 */       TDebug.out(">TAudioFileWriter.write(.., OutputStream): called");
/* 240 */       TDebug.out("class: " + super.getClass().getName());
/*     */     }
/* 242 */     AudioFormat inputFormat = audioInputStream.getFormat();
/* 243 */     if (TDebug.TraceAudioFileWriter) TDebug.out("input format: " + inputFormat);
/* 244 */     AudioFormat outputFormat = null;
/* 245 */     boolean bNeedsConversion = false;
/* 246 */     if (isAudioFormatSupportedImpl(inputFormat, fileType))
/*     */     {
/* 248 */       if (TDebug.TraceAudioFileWriter) TDebug.out("input format is supported directely");
/* 249 */       outputFormat = inputFormat;
/* 250 */       bNeedsConversion = false;
/*     */     }
/*     */     else
/*     */     {
/* 254 */       if (TDebug.TraceAudioFileWriter) TDebug.out("input format is not supported directely; trying to find a convertable format");
/* 255 */       outputFormat = findConvertableFormat(inputFormat, fileType);
/* 256 */       if (outputFormat != null)
/*     */       {
/* 258 */         bNeedsConversion = true;
/*     */ 
/* 261 */         if ((outputFormat.getSampleSizeInBits() == 8) && (outputFormat.getEncoding().equals(inputFormat.getEncoding())))
/*     */         {
/* 263 */           bNeedsConversion = false;
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/* 268 */         if (TDebug.TraceAudioFileWriter) TDebug.out("< format is not supported");
/* 269 */         throw new IllegalArgumentException("format not supported and not convertable");
/*     */       }
/*     */     }
/* 272 */     long lLengthInBytes = AudioUtils.getLengthInBytes(audioInputStream);
/* 273 */     TDataOutputStream dataOutputStream = new TNonSeekableDataOutputStream(outputStream);
/* 274 */     AudioOutputStream audioOutputStream = getAudioOutputStream(outputFormat, lLengthInBytes, fileType, dataOutputStream);
/*     */ 
/* 280 */     int written = writeImpl(audioInputStream, audioOutputStream, bNeedsConversion);
/*     */ 
/* 283 */     if (TDebug.TraceAudioFileWriter) TDebug.out("< wrote " + written + " bytes.");
/* 284 */     return written;
/*     */   }
/*     */ 
/*     */   protected int writeImpl(AudioInputStream audioInputStream, AudioOutputStream audioOutputStream, boolean bNeedsConversion)
/*     */     throws IOException
/*     */   {
/* 295 */     if (TDebug.TraceAudioFileWriter)
/*     */     {
/* 297 */       TDebug.out(">TAudioFileWriter.writeImpl(): called");
/* 298 */       TDebug.out("class: " + super.getClass().getName());
/*     */     }
/* 300 */     int nTotalWritten = 0;
/* 301 */     AudioFormat inputFormat = audioInputStream.getFormat();
/* 302 */     AudioFormat outputFormat = audioOutputStream.getFormat();
/*     */ 
/* 305 */     int nBytesPerSample = outputFormat.getFrameSize() / outputFormat.getChannels();
/*     */ 
/* 308 */     int nBufferSize = 16384 / outputFormat.getFrameSize() * outputFormat.getFrameSize();
/* 309 */     byte[] abBuffer = new byte[nBufferSize];
/*     */     while (true)
/*     */     {
/* 312 */       if (TDebug.TraceAudioFileWriter) TDebug.out("trying to read (bytes): " + abBuffer.length);
/* 313 */       int nBytesRead = audioInputStream.read(abBuffer);
/* 314 */       if (TDebug.TraceAudioFileWriter) TDebug.out("read (bytes): " + nBytesRead);
/* 315 */       if (nBytesRead == -1) {
/*     */         break;
/*     */       }
/*     */ 
/* 319 */       if (bNeedsConversion)
/*     */       {
/* 321 */         TConversionTool.changeOrderOrSign(abBuffer, 0, nBytesRead, nBytesPerSample);
/*     */       }
/*     */ 
/* 324 */       int nWritten = audioOutputStream.write(abBuffer, 0, nBytesRead);
/* 325 */       nTotalWritten += nWritten;
/*     */     }
/* 327 */     if (TDebug.TraceAudioFileWriter) TDebug.out("<TAudioFileWriter.writeImpl(): after main loop. Wrote " + nTotalWritten + " bytes");
/* 328 */     audioOutputStream.close();
/*     */ 
/* 330 */     return nTotalWritten;
/*     */   }
/*     */ 
/*     */   protected Iterator getSupportedAudioFormats(AudioFileFormat.Type fileType)
/*     */   {
/* 342 */     return this.m_audioFormats.iterator();
/*     */   }
/*     */ 
/*     */   protected boolean isAudioFormatSupportedImpl(AudioFormat audioFormat, AudioFileFormat.Type fileType)
/*     */   {
/* 367 */     if (TDebug.TraceAudioFileWriter)
/*     */     {
/* 369 */       TDebug.out("> TAudioFileWriter.isAudioFormatSupportedImpl(): format to test: " + audioFormat);
/* 370 */       TDebug.out("class: " + super.getClass().getName());
/*     */     }
/* 372 */     Iterator audioFormats = getSupportedAudioFormats(fileType);
/* 373 */     while (audioFormats.hasNext())
/*     */     {
/* 375 */       AudioFormat handledFormat = (AudioFormat)audioFormats.next();
/* 376 */       if (TDebug.TraceAudioFileWriter) TDebug.out("matching against format : " + handledFormat);
/* 377 */       if (AudioFormats.matches(handledFormat, audioFormat))
/*     */       {
/* 379 */         if (TDebug.TraceAudioFileWriter) TDebug.out("<...succeeded.");
/* 380 */         return true;
/*     */       }
/*     */     }
/* 383 */     if (TDebug.TraceAudioFileWriter) TDebug.out("< ... failed");
/* 384 */     return false;
/*     */   }
/*     */ 
/*     */   protected abstract AudioOutputStream getAudioOutputStream(AudioFormat paramAudioFormat, long paramLong, AudioFileFormat.Type paramType, TDataOutputStream paramTDataOutputStream)
/*     */     throws IOException;
/*     */ 
/*     */   private AudioFormat findConvertableFormat(AudioFormat inputFormat, AudioFileFormat.Type fileType)
/*     */   {
/* 400 */     if (TDebug.TraceAudioFileWriter) TDebug.out("TAudioFileWriter.findConvertableFormat(): input format: " + inputFormat);
/* 401 */     if (!isFileTypeSupported(fileType)) {
/* 402 */       if (TDebug.TraceAudioFileWriter) TDebug.out("< input file type is not supported.");
/* 403 */       return null;
/*     */     }
/* 405 */     AudioFormat.Encoding inputEncoding = inputFormat.getEncoding();
/* 406 */     if ((((inputEncoding.equals(PCM_SIGNED)) || (inputEncoding.equals(PCM_UNSIGNED)))) && (inputFormat.getSampleSizeInBits() == 8))
/*     */     {
/* 409 */       AudioFormat outputFormat = convertFormat(inputFormat, true, false);
/* 410 */       if (TDebug.TraceAudioFileWriter) TDebug.out("trying output format: " + outputFormat);
/* 411 */       if (isAudioFormatSupportedImpl(outputFormat, fileType))
/*     */       {
/* 413 */         if (TDebug.TraceAudioFileWriter) TDebug.out("< ... succeeded");
/* 414 */         return outputFormat;
/*     */       }
/*     */ 
/* 417 */       outputFormat = convertFormat(inputFormat, false, true);
/* 418 */       if (TDebug.TraceAudioFileWriter) TDebug.out("trying output format: " + outputFormat);
/* 419 */       if (isAudioFormatSupportedImpl(outputFormat, fileType))
/*     */       {
/* 421 */         if (TDebug.TraceAudioFileWriter) TDebug.out("< ... succeeded");
/* 422 */         return outputFormat;
/*     */       }
/* 424 */       outputFormat = convertFormat(inputFormat, true, true);
/* 425 */       if (TDebug.TraceAudioFileWriter) TDebug.out("trying output format: " + outputFormat);
/* 426 */       if (isAudioFormatSupportedImpl(outputFormat, fileType))
/*     */       {
/* 428 */         if (TDebug.TraceAudioFileWriter) TDebug.out("< ... succeeded");
/* 429 */         return outputFormat;
/*     */       }
/* 431 */       if (TDebug.TraceAudioFileWriter) TDebug.out("< ... failed");
/* 432 */       return null;
/*     */     }
/* 434 */     if ((inputEncoding.equals(PCM_SIGNED)) && (((inputFormat.getSampleSizeInBits() == 16) || (inputFormat.getSampleSizeInBits() == 24) || (inputFormat.getSampleSizeInBits() == 32))))
/*     */     {
/* 441 */       AudioFormat outputFormat = convertFormat(inputFormat, false, true);
/* 442 */       if (TDebug.TraceAudioFileWriter) TDebug.out("trying output format: " + outputFormat);
/* 443 */       if (isAudioFormatSupportedImpl(outputFormat, fileType))
/*     */       {
/* 445 */         if (TDebug.TraceAudioFileWriter) TDebug.out("< ... succeeded");
/* 446 */         return outputFormat;
/*     */       }
/*     */ 
/* 450 */       if (TDebug.TraceAudioFileWriter) TDebug.out("< ... failed");
/* 451 */       return null;
/*     */     }
/*     */ 
/* 456 */     if (TDebug.TraceAudioFileWriter) TDebug.out("< ... failed");
/* 457 */     return null;
/*     */   }
/*     */ 
/*     */   private AudioFormat convertFormat(AudioFormat format, boolean changeSign, boolean changeEndian)
/*     */   {
/* 463 */     AudioFormat.Encoding enc = PCM_SIGNED;
/* 464 */     if (format.getEncoding().equals(PCM_UNSIGNED) != changeSign) {
/* 465 */       enc = PCM_UNSIGNED;
/*     */     }
/* 467 */     return new AudioFormat(enc, format.getSampleRate(), format.getSampleSizeInBits(), format.getChannels(), format.getFrameSize(), format.getFrameRate(), format.isBigEndian() ^ changeEndian);
/*     */   }
/*     */ }
