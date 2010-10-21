/*     */ package javazoom.spi.mpeg.sampled.file;
/*     */ 
/*     */ import java.io.BufferedInputStream;
/*     */ import java.io.File;
/*     */ import java.io.FileInputStream;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.io.PushbackInputStream;
/*     */ import java.io.UnsupportedEncodingException;
/*     */ import java.net.URL;
/*     */ import java.net.URLConnection;
/*     */ import java.security.AccessControlException;
/*     */ import java.util.HashMap;
/*     */ import javax.sound.sampled.AudioFileFormat;
/*     */ import javax.sound.sampled.AudioFormat;
/*     */ import javax.sound.sampled.AudioInputStream;
/*     */ import javax.sound.sampled.UnsupportedAudioFileException;
/*     */ import javazoom.jl.decoder.Bitstream;
/*     */ import javazoom.jl.decoder.Header;
/*     */ import javazoom.spi.mpeg.sampled.file.tag.IcyInputStream;
/*     */ import javazoom.spi.mpeg.sampled.file.tag.MP3Tag;
/*     */ import org.tritonus.share.TDebug;
/*     */ import org.tritonus.share.sampled.file.TAudioFileReader;
/*     */ 
/*     */ public class MpegAudioFileReader extends TAudioFileReader
/*     */ {
/*     */   public static final String VERSION = "MP3SPI 1.9.4";
/*  74 */   private final int SYNC = -2097152;
/*  75 */   private String weak = null;
/*  76 */   private final AudioFormat.Encoding[][] sm_aEncodings = { 
/*  77 */     { MpegEncoding.MPEG2L1, MpegEncoding.MPEG2L2, MpegEncoding.MPEG2L3 }, 
/*  78 */     { MpegEncoding.MPEG1L1, MpegEncoding.MPEG1L2, MpegEncoding.MPEG1L3 }, 
/*  79 */     { MpegEncoding.MPEG2DOT5L1, MpegEncoding.MPEG2DOT5L2, MpegEncoding.MPEG2DOT5L3 } };
/*     */   public static final int INITAL_READ_LENGTH = 128000;
/*     */   private static final int MARK_LIMIT = 128001;
/*  83 */   private static final String[] id3v1genres = { 
/*  84 */     "Blues", 
/*  85 */     "Classic Rock", 
/*  86 */     "Country", 
/*  87 */     "Dance", 
/*  88 */     "Disco", 
/*  89 */     "Funk", 
/*  90 */     "Grunge", 
/*  91 */     "Hip-Hop", 
/*  92 */     "Jazz", 
/*  93 */     "Metal", 
/*  94 */     "New Age", 
/*  95 */     "Oldies", 
/*  96 */     "Other", 
/*  97 */     "Pop", 
/*  98 */     "R&B", 
/*  99 */     "Rap", 
/* 100 */     "Reggae", 
/* 101 */     "Rock", 
/* 102 */     "Techno", 
/* 103 */     "Industrial", 
/* 104 */     "Alternative", 
/* 105 */     "Ska", 
/* 106 */     "Death Metal", 
/* 107 */     "Pranks", 
/* 108 */     "Soundtrack", 
/* 109 */     "Euro-Techno", 
/* 110 */     "Ambient", 
/* 111 */     "Trip-Hop", 
/* 112 */     "Vocal", 
/* 113 */     "Jazz+Funk", 
/* 114 */     "Fusion", 
/* 115 */     "Trance", 
/* 116 */     "Classical", 
/* 117 */     "Instrumental", 
/* 118 */     "Acid", 
/* 119 */     "House", 
/* 120 */     "Game", 
/* 121 */     "Sound Clip", 
/* 122 */     "Gospel", 
/* 123 */     "Noise", 
/* 124 */     "AlternRock", 
/* 125 */     "Bass", 
/* 126 */     "Soul", 
/* 127 */     "Punk", 
/* 128 */     "Space", 
/* 129 */     "Meditative", 
/* 130 */     "Instrumental Pop", 
/* 131 */     "Instrumental Rock", 
/* 132 */     "Ethnic", 
/* 133 */     "Gothic", 
/* 134 */     "Darkwave", 
/* 135 */     "Techno-Industrial", 
/* 136 */     "Electronic", 
/* 137 */     "Pop-Folk", 
/* 138 */     "Eurodance", 
/* 139 */     "Dream", 
/* 140 */     "Southern Rock", 
/* 141 */     "Comedy", 
/* 142 */     "Cult", 
/* 143 */     "Gangsta", 
/* 144 */     "Top 40", 
/* 145 */     "Christian Rap", 
/* 146 */     "Pop/Funk", 
/* 147 */     "Jungle", 
/* 148 */     "Native American", 
/* 149 */     "Cabaret", 
/* 150 */     "New Wave", 
/* 151 */     "Psychadelic", 
/* 152 */     "Rave", 
/* 153 */     "Showtunes", 
/* 154 */     "Trailer", 
/* 155 */     "Lo-Fi", 
/* 156 */     "Tribal", 
/* 157 */     "Acid Punk", 
/* 158 */     "Acid Jazz", 
/* 159 */     "Polka", 
/* 160 */     "Retro", 
/* 161 */     "Musical", 
/* 162 */     "Rock & Roll", 
/* 163 */     "Hard Rock", 
/* 164 */     "Folk", 
/* 165 */     "Folk-Rock", 
/* 166 */     "National Folk", 
/* 167 */     "Swing", 
/* 168 */     "Fast Fusion", 
/* 169 */     "Bebob", 
/* 170 */     "Latin", 
/* 171 */     "Revival", 
/* 172 */     "Celtic", 
/* 173 */     "Bluegrass", 
/* 174 */     "Avantgarde", 
/* 175 */     "Gothic Rock", 
/* 176 */     "Progressive Rock", 
/* 177 */     "Psychedelic Rock", 
/* 178 */     "Symphonic Rock", 
/* 179 */     "Slow Rock", 
/* 180 */     "Big Band", 
/* 181 */     "Chorus", 
/* 182 */     "Easy Listening", 
/* 183 */     "Acoustic", 
/* 184 */     "Humour", 
/* 185 */     "Speech", 
/* 186 */     "Chanson", 
/* 187 */     "Opera", 
/* 188 */     "Chamber Music", 
/* 189 */     "Sonata", 
/* 190 */     "Symphony", 
/* 191 */     "Booty Brass", 
/* 192 */     "Primus", 
/* 193 */     "Porn Groove", 
/* 194 */     "Satire", 
/* 195 */     "Slow Jam", 
/* 196 */     "Club", 
/* 197 */     "Tango", 
/* 198 */     "Samba", 
/* 199 */     "Folklore", 
/* 200 */     "Ballad", 
/* 201 */     "Power Ballad", 
/* 202 */     "Rhythmic Soul", 
/* 203 */     "Freestyle", 
/* 204 */     "Duet", 
/* 205 */     "Punk Rock", 
/* 206 */     "Drum Solo", 
/* 207 */     "A Capela", 
/* 208 */     "Euro-House", 
/* 209 */     "Dance Hall", 
/* 210 */     "Goa", 
/* 211 */     "Drum & Bass", 
/* 212 */     "Club-House", 
/* 213 */     "Hardcore", 
/* 214 */     "Terror", 
/* 215 */     "Indie", 
/* 216 */     "BritPop", 
/* 217 */     "Negerpunk", 
/* 218 */     "Polsk Punk", 
/* 219 */     "Beat", 
/* 220 */     "Christian Gangsta Rap", 
/* 221 */     "Heavy Metal", 
/* 222 */     "Black Metal", 
/* 223 */     "Crossover", 
/* 224 */     "Contemporary Christian", 
/* 225 */     "Christian Rock", 
/* 226 */     "Merengue", 
/* 227 */     "Salsa", 
/* 228 */     "Thrash Metal", 
/* 229 */     "Anime", 
/* 230 */     "JPop", 
/* 231 */     "SynthPop" };
/*     */ 
/*     */   public MpegAudioFileReader()
/*     */   {
/* 236 */     super(128001, true);
/* 237 */     if (TDebug.TraceAudioFileReader) TDebug.out("MP3SPI 1.9.4");
/*     */     try
/*     */     {
/* 240 */       this.weak = System.getProperty("mp3spi.weak");
/*     */     }
/*     */     catch (AccessControlException localAccessControlException)
/*     */     {
/*     */     }
/*     */   }
/*     */ 
/*     */   public AudioFileFormat getAudioFileFormat(File file)
/*     */     throws UnsupportedAudioFileException, IOException
/*     */   {
/* 252 */     return super.getAudioFileFormat(file);
/*     */   }
/*     */ 
/*     */   public AudioFileFormat getAudioFileFormat(URL url)
/*     */     throws UnsupportedAudioFileException, IOException
/*     */   {
/* 260 */     if (TDebug.TraceAudioFileReader)
/*     */     {
/* 262 */       TDebug.out("MpegAudioFileReader.getAudioFileFormat(URL): begin");
/*     */     }
/* 264 */     long lFileLengthInBytes = -1L;
/* 265 */     URLConnection conn = url.openConnection();
/*     */ 
/* 267 */     conn.setRequestProperty("Icy-Metadata", "1");
/* 268 */     InputStream inputStream = conn.getInputStream();
/* 269 */     AudioFileFormat audioFileFormat = null;
/*     */     try
/*     */     {
/* 272 */       audioFileFormat = getAudioFileFormat(inputStream, lFileLengthInBytes);
/*     */     }
/*     */     finally
/*     */     {
/* 276 */       inputStream.close();
/*     */     }
/* 278 */     if (TDebug.TraceAudioFileReader)
/*     */     {
/* 280 */       TDebug.out("MpegAudioFileReader.getAudioFileFormat(URL): end");
/*     */     }
/* 282 */     return audioFileFormat;
/*     */   }
/*     */ 
/*     */   public AudioFileFormat getAudioFileFormat(InputStream inputStream, long mediaLength)
/*     */     throws UnsupportedAudioFileException, IOException
/*     */   {
/* 290 */     if (TDebug.TraceAudioFileReader) TDebug.out(">MpegAudioFileReader.getAudioFileFormat(InputStream inputStream, long mediaLength): begin");
/* 291 */     HashMap aff_properties = new HashMap();
/* 292 */     HashMap af_properties = new HashMap();
/* 293 */     int mLength = (int)mediaLength;
/* 294 */     int size = inputStream.available();
/* 295 */     PushbackInputStream pis = new PushbackInputStream(inputStream, 128001);
/* 296 */     byte[] head = new byte[22];
/* 297 */     pis.read(head);
/* 298 */     if (TDebug.TraceAudioFileReader)
/*     */     {
/* 300 */       TDebug.out("InputStream : " + inputStream + " =>" + new String(head));
/*     */     }
/*     */ 
/* 305 */     if ((head[0] == 82) && (head[1] == 73) && (head[2] == 70) && (head[3] == 70) && (head[8] == 87) && (head[9] == 65) && (head[10] == 86) && (head[11] == 69))
/*     */     {
/* 307 */       if (TDebug.TraceAudioFileReader) TDebug.out("RIFF/WAV stream found");
/* 308 */       int isPCM = head[21] << 8 & 0xFF00 | head[20] & 0xFF;
/* 309 */       if ((this.weak == null) && 
/* 311 */         (isPCM == 1)) throw new UnsupportedAudioFileException("WAV PCM stream found");
/*     */ 
/*     */     }
/* 315 */     else if ((head[0] == 46) && (head[1] == 115) && (head[2] == 110) && (head[3] == 100))
/*     */     {
/* 317 */       if (TDebug.TraceAudioFileReader) TDebug.out("AU stream found");
/* 318 */       if (this.weak == null) throw new UnsupportedAudioFileException("AU stream found");
/*     */     }
/* 320 */     else if ((head[0] == 70) && (head[1] == 79) && (head[2] == 82) && (head[3] == 77) && (head[8] == 65) && (head[9] == 73) && (head[10] == 70) && (head[11] == 70))
/*     */     {
/* 322 */       if (TDebug.TraceAudioFileReader) TDebug.out("AIFF stream found");
/* 323 */       if (this.weak == null) throw new UnsupportedAudioFileException("AIFF stream found");
/*     */     }
/* 325 */     else if ((((head[0] == 77) ? 1 : 0) | ((head[0] == 109) ? 1 : 0)) != 0) { if ((((head[1] == 65) ? 1 : 0) | ((head[1] == 97) ? 1 : 0)) != 0) if ((((head[2] == 67) ? 1 : 0) | ((head[2] == 99) ? 1 : 0)) != 0)
/*     */         {
/* 327 */           if (TDebug.TraceAudioFileReader) TDebug.out("APE stream found");
/* 328 */           if (this.weak == null) throw new UnsupportedAudioFileException("APE stream found");
/*     */         }  }
/* 330 */     else if ((((head[0] == 70) ? 1 : 0) | ((head[0] == 102) ? 1 : 0)) != 0) { if ((((head[1] == 76) ? 1 : 0) | ((head[1] == 108) ? 1 : 0)) != 0) if ((((head[2] == 65) ? 1 : 0) | ((head[2] == 97) ? 1 : 0)) != 0) if ((((head[3] == 67) ? 1 : 0) | ((head[3] == 99) ? 1 : 0)) != 0)
/*     */           {
/* 332 */             if (TDebug.TraceAudioFileReader) TDebug.out("FLAC stream found");
/* 333 */             if (this.weak == null) throw new UnsupportedAudioFileException("FLAC stream found");
/*     */           } }
/* 336 */     else if ((((head[0] == 73) ? 1 : 0) | ((head[0] == 105) ? 1 : 0)) != 0) { if ((((head[1] == 67) ? 1 : 0) | ((head[1] == 99) ? 1 : 0)) != 0) if ((((head[2] == 89) ? 1 : 0) | ((head[2] == 121) ? 1 : 0)) != 0)
/*     */         {
/* 338 */           pis.unread(head);
/*     */ 
/* 340 */           loadShoutcastInfo(pis, aff_properties);
/*     */         } }
/* 343 */     else if ((((head[0] == 79) ? 1 : 0) | ((head[0] == 111) ? 1 : 0)) != 0) { if ((((head[1] == 71) ? 1 : 0) | ((head[1] == 103) ? 1 : 0)) != 0) if ((((head[2] == 71) ? 1 : 0) | ((head[2] == 103) ? 1 : 0)) != 0)
/*     */         {
/* 345 */           if (TDebug.TraceAudioFileReader) TDebug.out("Ogg stream found");
/* 346 */           if (this.weak == null) throw new UnsupportedAudioFileException("Ogg stream found");
/*     */         } }
/*     */     else
/*     */     {
/* 351 */       pis.unread(head);
/*     */     }
/*     */ 
/* 354 */     int nVersion = -1;
/* 355 */     int nLayer = -1;
/* 356 */     int nSFIndex = -1;
/* 357 */     int nMode = -1;
/* 358 */     int FrameSize = -1;
/* 359 */     int nFrameSize = -1;
/* 360 */     int nFrequency = -1;
/* 361 */     int nTotalFrames = -1;
/* 362 */     float FrameRate = -1.0F;
/* 363 */     int BitRate = -1;
/* 364 */     int nChannels = -1;
/* 365 */     int nHeader = -1;
/* 366 */     int nTotalMS = -1;
/* 367 */     boolean nVBR = false;
/* 368 */     AudioFormat.Encoding encoding = null;
/*     */     try
/*     */     {
/* 371 */       Bitstream m_bitstream = new Bitstream(pis);
/* 372 */       aff_properties.put("mp3.header.pos", new Integer(m_bitstream.header_pos()));
/* 373 */       Header m_header = m_bitstream.readFrame();
/* 374 */       m_bitstream.unreadFrame();
/*     */ 
/* 376 */       nVersion = m_header.version();
/* 377 */       if (nVersion == 2) aff_properties.put("mp3.version.mpeg", Float.toString(2.5F)); else {
/* 378 */         aff_properties.put("mp3.version.mpeg", Integer.toString(2 - nVersion));
/*     */       }
/* 380 */       nLayer = m_header.layer();
/* 381 */       aff_properties.put("mp3.version.layer", Integer.toString(nLayer));
/* 382 */       nSFIndex = m_header.sample_frequency();
/* 383 */       nMode = m_header.mode();
/* 384 */       aff_properties.put("mp3.mode", new Integer(nMode));
/* 385 */       nChannels = (nMode == 3) ? 1 : 2;
/* 386 */       aff_properties.put("mp3.channels", new Integer(nChannels));
/* 387 */       nVBR = m_header.vbr();
/* 388 */       af_properties.put("vbr", new Boolean(nVBR));
/* 389 */       aff_properties.put("mp3.vbr", new Boolean(nVBR));
/* 390 */       aff_properties.put("mp3.vbr.scale", new Integer(m_header.vbr_scale()));
/* 391 */       FrameSize = m_header.calculate_framesize();
/* 392 */       aff_properties.put("mp3.framesize.bytes", new Integer(FrameSize));
/* 393 */       if (FrameSize < 0) throw new UnsupportedAudioFileException("Invalid FrameSize : " + FrameSize);
/* 394 */       nFrequency = m_header.frequency();
/* 395 */       aff_properties.put("mp3.frequency.hz", new Integer(nFrequency));
/* 396 */       FrameRate = (float)(1.0D / m_header.ms_per_frame() * 1000.0D);
/* 397 */       aff_properties.put("mp3.framerate.fps", new Float(FrameRate));
/* 398 */       if (FrameRate < 0.0F) throw new UnsupportedAudioFileException("Invalid FrameRate : " + FrameRate);
/* 399 */       if (mLength != -1)
/*     */       {
/* 401 */         aff_properties.put("mp3.length.bytes", new Integer(mLength));
/* 402 */         nTotalFrames = m_header.max_number_of_frames(mLength);
/* 403 */         aff_properties.put("mp3.length.frames", new Integer(nTotalFrames));
/*     */       }
/* 405 */       BitRate = m_header.bitrate();
/* 406 */       af_properties.put("bitrate", new Integer(BitRate));
/* 407 */       aff_properties.put("mp3.bitrate.nominal.bps", new Integer(BitRate));
/*     */ 
/* 409 */       nHeader = m_header.getSyncHeader();
/* 410 */       encoding = this.sm_aEncodings[nVersion][(nLayer - 1)];
/* 411 */       aff_properties.put("mp3.version.encoding", encoding.toString());
/* 412 */       if (mLength != -1)
/*     */       {
/* 414 */         nTotalMS = Math.round(m_header.total_ms(mLength));
/* 415 */         aff_properties.put("duration", new Long(nTotalMS * 1000L));
/*     */       }
/* 417 */       aff_properties.put("mp3.copyright", new Boolean(m_header.copyright()));
/* 418 */       aff_properties.put("mp3.original", new Boolean(m_header.original()));
/* 419 */       aff_properties.put("mp3.crc", new Boolean(m_header.checksums()));
/* 420 */       aff_properties.put("mp3.padding", new Boolean(m_header.padding()));
/* 421 */       m_bitstream.unreadFrame();
/* 422 */       InputStream id3v2 = m_bitstream.getRawID3v2();
/* 423 */       if (id3v2 != null)
/*     */       {
/* 425 */         aff_properties.put("mp3.id3tag.v2", id3v2);
/* 426 */         parseID3v2Frames(id3v2, aff_properties);
/*     */       }
/* 428 */       if (TDebug.TraceAudioFileReader) TDebug.out(m_header.toString());
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 432 */       if (TDebug.TraceAudioFileReader) TDebug.out("not a MPEG stream:" + e.getMessage());
/* 433 */       throw new UnsupportedAudioFileException("not a MPEG stream:" + e.getMessage());
/*     */     }
/*     */ 
/* 436 */     int cVersion = nHeader >> 19 & 0x3;
/* 437 */     if (cVersion == 1)
/*     */     {
/* 439 */       if (TDebug.TraceAudioFileReader) TDebug.out("not a MPEG stream: wrong version");
/* 440 */       throw new UnsupportedAudioFileException("not a MPEG stream: wrong version");
/*     */     }
/* 442 */     int cSFIndex = nHeader >> 10 & 0x3;
/* 443 */     if (cSFIndex == 3)
/*     */     {
/* 445 */       if (TDebug.TraceAudioFileReader) TDebug.out("not a MPEG stream: wrong sampling rate");
/* 446 */       throw new UnsupportedAudioFileException("not a MPEG stream: wrong sampling rate");
/*     */     }
/*     */ 
/* 449 */     if ((size == mediaLength) && (mediaLength != -1L))
/*     */     {
/* 451 */       FileInputStream fis = (FileInputStream)inputStream;
/* 452 */       byte[] id3v1 = new byte[128];
/* 453 */       long bytesSkipped = fis.skip(inputStream.available() - id3v1.length);
/* 454 */       int read = fis.read(id3v1, 0, id3v1.length);
/* 455 */       if ((id3v1[0] == 84) && (id3v1[1] == 65) && (id3v1[2] == 71))
/*     */       {
/* 457 */         parseID3v1Frames(id3v1, aff_properties);
/*     */       }
/*     */     }
/*     */ 
/* 461 */     inputStream.mark(128001);
/*     */ 
/* 464 */     AudioFormat format = new MpegAudioFormat(encoding, nFrequency, -1, 
/* 465 */       nChannels, 
/* 466 */       -1, 
/* 467 */       FrameRate, 
/* 468 */       true, af_properties);
/* 469 */     return new MpegAudioFileFormat(MpegFileFormatType.MP3, format, nTotalFrames, mLength, aff_properties);
/*     */   }
/*     */ 
/*     */   public AudioInputStream getAudioInputStream(File file)
/*     */     throws UnsupportedAudioFileException, IOException
/*     */   {
/* 477 */     if (TDebug.TraceAudioFileReader) TDebug.out("getAudioInputStream(File file)");
/* 478 */     InputStream inputStream = new FileInputStream(file);
/*     */     try
/*     */     {
/* 481 */       return getAudioInputStream(inputStream);
/*     */     }
/*     */     catch (UnsupportedAudioFileException e)
/*     */     {
/* 485 */       if (inputStream != null) inputStream.close();
/* 486 */       throw e;
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 490 */       if (inputStream != null) inputStream.close();
/* 491 */       throw e;
/*     */     }
/*     */   }
/*     */ 
/*     */   public AudioInputStream getAudioInputStream(URL url)
/*     */     throws UnsupportedAudioFileException, IOException
/*     */   {
/* 500 */     if (TDebug.TraceAudioFileReader)
/*     */     {
/* 502 */       TDebug.out("MpegAudioFileReader.getAudioInputStream(URL): begin");
/*     */     }
/* 504 */     long lFileLengthInBytes = -1L;
/* 505 */     URLConnection conn = url.openConnection();
/*     */ 
/* 507 */     boolean isShout = false;
/* 508 */     int toRead = 4;
/* 509 */     byte[] head = new byte[toRead];
/* 510 */     conn.setRequestProperty("Icy-Metadata", "1");
/* 511 */     BufferedInputStream bInputStream = new BufferedInputStream(conn.getInputStream());
/* 512 */     bInputStream.mark(toRead);
/* 513 */     int read = bInputStream.read(head, 0, toRead);
/* 514 */     if (read > 2) if ((((head[0] == 73) ? 1 : 0) | ((head[0] == 105) ? 1 : 0)) != 0) if ((((head[1] == 67) ? 1 : 0) | ((head[1] == 99) ? 1 : 0)) != 0) if ((((head[2] == 89) ? 1 : 0) | ((head[2] == 121) ? 1 : 0)) != 0) isShout = true;
/* 515 */     bInputStream.reset();
/* 516 */     InputStream inputStream = null;
/*     */ 
/* 518 */     if (isShout)
/*     */     {
/* 521 */       IcyInputStream icyStream = new IcyInputStream(bInputStream);
/* 522 */       icyStream.addTagParseListener(IcyListener.getInstance());
/* 523 */       inputStream = icyStream;
/*     */     }
/*     */     else
/*     */     {
/* 528 */       String metaint = conn.getHeaderField("icy-metaint");
/* 529 */       if (metaint != null)
/*     */       {
/* 532 */         IcyInputStream icyStream = new IcyInputStream(bInputStream, metaint);
/* 533 */         icyStream.addTagParseListener(IcyListener.getInstance());
/* 534 */         inputStream = icyStream;
/*     */       }
/*     */       else
/*     */       {
/* 539 */         inputStream = bInputStream;
/*     */       }
/*     */     }
/* 542 */     AudioInputStream audioInputStream = null;
/*     */     try
/*     */     {
/* 545 */       audioInputStream = getAudioInputStream(inputStream, lFileLengthInBytes);
/*     */     }
/*     */     catch (UnsupportedAudioFileException e)
/*     */     {
/* 549 */       inputStream.close();
/* 550 */       throw e;
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 554 */       inputStream.close();
/* 555 */       throw e;
/*     */     }
/* 557 */     if (TDebug.TraceAudioFileReader)
/*     */     {
/* 559 */       TDebug.out("MpegAudioFileReader.getAudioInputStream(URL): end");
/*     */     }
/* 561 */     return audioInputStream;
/*     */   }
/*     */ 
/*     */   public AudioInputStream getAudioInputStream(InputStream inputStream)
/*     */     throws UnsupportedAudioFileException, IOException
/*     */   {
/* 569 */     if (TDebug.TraceAudioFileReader) TDebug.out("MpegAudioFileReader.getAudioInputStream(InputStream inputStream)");
/* 570 */     if (!inputStream.markSupported()) inputStream = new BufferedInputStream(inputStream);
/* 571 */     return super.getAudioInputStream(inputStream);
/*     */   }
/*     */ 
/*     */   protected void parseID3v1Frames(byte[] frames, HashMap props)
/*     */   {
/* 581 */     if (TDebug.TraceAudioFileReader) TDebug.out("Parsing ID3v1");
/* 582 */     String tag = null;
/*     */     try
/*     */     {
/* 585 */       tag = new String(frames, 0, frames.length, "ISO-8859-1");
/*     */     }
/*     */     catch (UnsupportedEncodingException e)
/*     */     {
/* 589 */       tag = new String(frames, 0, frames.length);
/* 590 */       if (TDebug.TraceAudioFileReader) TDebug.out("Cannot use ISO-8859-1");
/*     */     }
/* 592 */     if (TDebug.TraceAudioFileReader) TDebug.out("ID3v1 frame dump='" + tag + "'");
/* 593 */     int start = 3;
/* 594 */     String titlev1 = chopSubstring(tag, start, start += 30);
/* 595 */     String titlev2 = (String)props.get("title");
/* 596 */     if ((((titlev2 == null) || (titlev2.length() == 0))) && (titlev1 != null)) props.put("title", titlev1);
/* 597 */     String artistv1 = chopSubstring(tag, start, start += 30);
/* 598 */     String artistv2 = (String)props.get("author");
/* 599 */     if ((((artistv2 == null) || (artistv2.length() == 0))) && (artistv1 != null)) props.put("author", artistv1);
/* 600 */     String albumv1 = chopSubstring(tag, start, start += 30);
/* 601 */     String albumv2 = (String)props.get("album");
/* 602 */     if ((((albumv2 == null) || (albumv2.length() == 0))) && (albumv1 != null)) props.put("album", albumv1);
/* 603 */     String yearv1 = chopSubstring(tag, start, start += 4);
/* 604 */     String yearv2 = (String)props.get("year");
/* 605 */     if ((((yearv2 == null) || (yearv2.length() == 0))) && (yearv1 != null)) props.put("date", yearv1);
/* 606 */     String commentv1 = chopSubstring(tag, start, start += 28);
/* 607 */     String commentv2 = (String)props.get("comment");
/* 608 */     if ((((commentv2 == null) || (commentv2.length() == 0))) && (commentv1 != null)) props.put("comment", commentv1);
/* 609 */     String trackv1 = "" + ((int) (frames[126] & 0xff));
/* 610 */     String trackv2 = (String)props.get("mp3.id3tag.track");
/* 611 */     if ((((trackv2 == null) || (trackv2.length() == 0))) && (trackv1 != null)) props.put("mp3.id3tag.track", trackv1);
/* 612 */     int genrev1 = frames[127] & 0xFF;
/* 613 */     if ((genrev1 >= 0) && (genrev1 < id3v1genres.length))
/*     */     {
/* 615 */       String genrev2 = (String)props.get("mp3.id3tag.genre");
/* 616 */       if ((genrev2 == null) || (genrev2.length() == 0)) props.put("mp3.id3tag.genre", id3v1genres[genrev1]);
/*     */     }
/* 618 */     if (!TDebug.TraceAudioFileReader) return; TDebug.out("ID3v1 parsed");
/*     */   }
/*     */ 
/*     */   private String chopSubstring(String s, int start, int end)
/*     */   {
/* 630 */     String str = null;
/*     */     try
/*     */     {
/* 634 */       str = s.substring(start, end);
/* 635 */       int loc = str.indexOf(0);
/* 636 */       if (loc != -1) str = str.substring(0, loc);
/*     */ 
/*     */     }
/*     */     catch (StringIndexOutOfBoundsException e)
/*     */     {
/* 641 */       if (TDebug.TraceAudioFileReader) TDebug.out("Cannot chopSubString " + e.getMessage());
/*     */     }
/* 643 */     return str;
/*     */   }
/*     */ 
/*     */   protected void parseID3v2Frames(InputStream frames, HashMap props)
/*     */   {
/* 653 */     if (TDebug.TraceAudioFileReader) TDebug.out("Parsing ID3v2");
/* 654 */     byte[] bframes = (byte[])null;
/* 655 */     int size = -1;
/*     */     try
/*     */     {
/* 658 */       size = frames.available();
/* 659 */       bframes = new byte[size];
/* 660 */       frames.mark(size);
/* 661 */       frames.read(bframes);
/* 662 */       frames.reset();
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 666 */       if (TDebug.TraceAudioFileReader) TDebug.out("Cannot parse ID3v2 :" + e.getMessage());
/*     */     }
/* 668 */     if (!"ID3".equals(new String(bframes, 0, 3)))
/*     */     {
/* 670 */       TDebug.out("No ID3v2 header found!");
/* 671 */       return;
/*     */     }
/* 673 */     int v2version = bframes[3] & 0xFF;
/* 674 */     props.put("mp3.id3tag.v2.version", String.valueOf(v2version));
/* 675 */     if ((v2version < 2) || (v2version > 4))
/*     */     {
/* 677 */       TDebug.out("Unsupported ID3v2 version " + v2version + "!");
/* 678 */       return;
/*     */     }
/*     */     try
/*     */     {
/* 682 */       if (TDebug.TraceAudioFileReader) TDebug.out("ID3v2 frame dump='" + new String(bframes, 0, bframes.length) + "'");
/*     */ 
/* 684 */       String value = null;
/* 685 */       int i = 10;
/*     */       do {
/* 687 */         if ((v2version == 3) || (v2version == 4))
/*     */         {
/* 690 */           String code = new String(bframes, i, 4);
/* 691 */           size = bframes[(i + 4)] << 24 & 0xFF000000 | bframes[(i + 5)] << 16 & 0xFF0000 | bframes[(i + 6)] << 8 & 0xFF00 | bframes[(i + 7)] & 0xFF;
/* 692 */           i += 10;
/* 693 */           if ((code.equals("TALB")) || (code.equals("TIT2")) || (code.equals("TYER")) || 
/* 694 */             (code.equals("TPE1")) || (code.equals("TCOP")) || (code.equals("COMM")) || 
/* 695 */             (code.equals("TCON")) || (code.equals("TRCK")) || (code.equals("TPOS")) || 
/* 696 */             (code.equals("TDRC")) || (code.equals("TCOM")) || (code.equals("TIT1")) || 
/* 697 */             (code.equals("TENC")) || (code.equals("TPUB")) || (code.equals("TPE2")) || 
/* 698 */             (code.equals("TLEN")))
/*     */           {
/* 700 */             if (code.equals("COMM")) value = parseText(bframes, i, size, 5); else
/* 701 */               value = parseText(bframes, i, size, 1);
/* 702 */             if ((value != null) && (value.length() > 0))
/*     */             {
/* 704 */               if (code.equals("TALB")) props.put("album", value);
/* 705 */               else if (code.equals("TIT2")) props.put("title", value);
/* 706 */               else if (code.equals("TYER")) props.put("date", value);
/* 708 */               else if (code.equals("TDRC")) props.put("date", value);
/* 709 */               else if (code.equals("TPE1")) props.put("author", value);
/* 710 */               else if (code.equals("TCOP")) props.put("copyright", value);
/* 711 */               else if (code.equals("COMM")) props.put("comment", value);
/* 712 */               else if (code.equals("TCON")) props.put("mp3.id3tag.genre", value);
/* 713 */               else if (code.equals("TRCK")) props.put("mp3.id3tag.track", value);
/* 714 */               else if (code.equals("TPOS")) props.put("mp3.id3tag.disc", value);
/* 715 */               else if (code.equals("TCOM")) props.put("mp3.id3tag.composer", value);
/* 716 */               else if (code.equals("TIT1")) props.put("mp3.id3tag.grouping", value);
/* 717 */               else if (code.equals("TENC")) props.put("mp3.id3tag.encoded", value);
/* 718 */               else if (code.equals("TPUB")) props.put("mp3.id3tag.publisher", value);
/* 719 */               else if (code.equals("TPE2")) props.put("mp3.id3tag.orchestra", value);
/* 720 */               else if (code.equals("TLEN")) props.put("mp3.id3tag.length", value);
/*     */             }
/*     */           }
/*     */ 
/*     */         }
/*     */         else
/*     */         {
/* 727 */           String scode = new String(bframes, i, 3);
/* 728 */           size = 0 + (bframes[(i + 3)] << 16) + (bframes[(i + 4)] << 8) + bframes[(i + 5)];
/* 729 */           i += 6;
/* 730 */           if ((scode.equals("TAL")) || (scode.equals("TT2")) || (scode.equals("TP1")) || 
/* 731 */             (scode.equals("TYE")) || (scode.equals("TRK")) || (scode.equals("TPA")) || 
/* 732 */             (scode.equals("TCR")) || (scode.equals("TCO")) || (scode.equals("TCM")) || 
/* 733 */             (scode.equals("COM")) || (scode.equals("TT1")) || (scode.equals("TEN")) || 
/* 734 */             (scode.equals("TPB")) || (scode.equals("TP2")) || (scode.equals("TLE")))
/*     */           {
/* 736 */             if (scode.equals("COM")) value = parseText(bframes, i, size, 5); else
/* 737 */               value = parseText(bframes, i, size, 1);
/* 738 */             if ((value != null) && (value.length() > 0))
/*     */             {
/* 740 */               if (scode.equals("TAL")) props.put("album", value);
/* 741 */               else if (scode.equals("TT2")) props.put("title", value);
/* 742 */               else if (scode.equals("TYE")) props.put("date", value);
/* 743 */               else if (scode.equals("TP1")) props.put("author", value);
/* 744 */               else if (scode.equals("TCR")) props.put("copyright", value);
/* 745 */               else if (scode.equals("COM")) props.put("comment", value);
/* 746 */               else if (scode.equals("TCO")) props.put("mp3.id3tag.genre", value);
/* 747 */               else if (scode.equals("TRK")) props.put("mp3.id3tag.track", value);
/* 748 */               else if (scode.equals("TPA")) props.put("mp3.id3tag.disc", value);
/* 749 */               else if (scode.equals("TCM")) props.put("mp3.id3tag.composer", value);
/* 750 */               else if (scode.equals("TT1")) props.put("mp3.id3tag.grouping", value);
/* 751 */               else if (scode.equals("TEN")) props.put("mp3.id3tag.encoded", value);
/* 752 */               else if (scode.equals("TPB")) props.put("mp3.id3tag.publisher", value);
/* 753 */               else if (scode.equals("TP2")) props.put("mp3.id3tag.orchestra", value);
/* 754 */               else if (scode.equals("TLE")) props.put("mp3.id3tag.length", value);
/*     */             }
/*     */           }
/*     */         }
/* 685 */         i += size; if (i >= bframes.length);
/*     */       }
/* 685 */       while (bframes[i] > 0);
/*     */     }
/*     */     catch (RuntimeException e)
/*     */     {
/* 763 */       if (TDebug.TraceAudioFileReader) TDebug.out("Cannot parse ID3v2 :" + e.getMessage());
/*     */     }
/* 765 */     if (!TDebug.TraceAudioFileReader) return; TDebug.out("ID3v2 parsed");
/*     */   }
/*     */ 
/*     */   protected String parseText(byte[] bframes, int offset, int size, int skip)
/*     */   {
/* 779 */     String value = null;
/*     */     try
/*     */     {
/* 782 */       String[] ENC_TYPES = { "ISO-8859-1", "UTF16", "UTF-16BE", "UTF-8" };
/* 783 */       value = new String(bframes, offset + skip, size - skip, ENC_TYPES[bframes[offset]]);
/* 784 */       value = chopSubstring(value, 0, value.length());
/*     */     }
/*     */     catch (UnsupportedEncodingException e)
/*     */     {
/* 788 */       if (TDebug.TraceAudioFileReader) TDebug.out("ID3v2 Encoding error :" + e.getMessage());
/*     */     }
/* 790 */     return value;
/*     */   }
/*     */ 
/*     */   protected void loadShoutcastInfo(InputStream input, HashMap props)
/*     */     throws IOException
/*     */   {
/* 802 */     IcyInputStream icy = new IcyInputStream(new BufferedInputStream(input));
/* 803 */     HashMap metadata = icy.getTagHash();
/* 804 */     MP3Tag titleMP3Tag = icy.getTag("icy-name");
/* 805 */     if (titleMP3Tag != null) props.put("title", ((String)titleMP3Tag.getValue()).trim());
/* 806 */     MP3Tag[] meta = icy.getTags();
/* 807 */     if (meta == null)
/*     */       return;
/* 809 */     StringBuffer metaStr = new StringBuffer();
/* 810 */     for (int i = 0; i < meta.length; ++i)
/*     */     {
/* 812 */       String key = meta[i].getName();
/* 813 */       String value = ((String)icy.getTag(key).getValue()).trim();
/* 814 */       props.put("mp3.shoutcast.metadata." + key, value);
/*     */     }
/*     */   }
/*     */ }

