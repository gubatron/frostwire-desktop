/*     */ package javazoom.spi.mpeg.sampled.convert;
/*     */ 
/*     */ import java.io.BufferedInputStream;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.util.HashMap;
/*     */ import java.util.Map;
/*     */ import javax.sound.sampled.AudioFormat;
/*     */ import javax.sound.sampled.AudioInputStream;
/*     */ import javazoom.jl.decoder.Bitstream;
/*     */ import javazoom.jl.decoder.BitstreamException;
/*     */ import javazoom.jl.decoder.Decoder;
/*     */ import javazoom.jl.decoder.DecoderException;
/*     */ import javazoom.jl.decoder.Equalizer;
/*     */ import javazoom.jl.decoder.Header;
/*     */ import javazoom.jl.decoder.Obuffer;
/*     */ import javazoom.spi.PropertiesContainer;
/*     */ import javazoom.spi.mpeg.sampled.file.IcyListener;
/*     */ import javazoom.spi.mpeg.sampled.file.tag.TagParseEvent;
/*     */ import javazoom.spi.mpeg.sampled.file.tag.TagParseListener;
/*     */ import org.tritonus.share.TCircularBuffer;
/*     */ import org.tritonus.share.TDebug;
/*     */ import org.tritonus.share.sampled.convert.TAsynchronousFilteredAudioInputStream;
/*     */ 
/*     */ public class DecodedMpegAudioInputStream extends TAsynchronousFilteredAudioInputStream
/*     */   implements PropertiesContainer, TagParseListener
/*     */ {
/*     */   private InputStream m_encodedStream;
/*     */   private Bitstream m_bitstream;
/*     */   private Decoder m_decoder;
/*     */   private Equalizer m_equalizer;
/*     */   private float[] m_equalizer_values;
/*     */   private Header m_header;
/*     */   private DMAISObuffer m_oBuffer;
/*  63 */   private long byteslength = -1L;
/*  64 */   private long currentByte = 0L;
/*     */ 
/*  66 */   private int frameslength = -1;
/*  67 */   private long currentFrame = 0L;
/*  68 */   private int currentFramesize = 0;
/*  69 */   private int currentBitrate = -1;
/*     */ 
/*  71 */   private long currentMicrosecond = 0L;
/*     */ 
/*  73 */   private IcyListener shoutlst = null;
/*     */ 
/*  76 */   private HashMap properties = null;
/*     */ 
/*     */   public DecodedMpegAudioInputStream(AudioFormat outputFormat, AudioInputStream inputStream)
/*     */   {
/*  80 */     super(outputFormat, -1L);
/*  81 */     if (TDebug.TraceAudioConverter)
/*     */     {
/*  83 */       TDebug.out(">DecodedMpegAudioInputStream(AudioFormat outputFormat, AudioInputStream inputStream)");
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/*  88 */       this.byteslength = inputStream.available();
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/*  92 */       TDebug.out("DecodedMpegAudioInputStream : Cannot run inputStream.available() : " + e.getMessage());
/*  93 */       this.byteslength = -1L;
/*     */     }
/*  95 */     this.m_encodedStream = inputStream;
/*  96 */     this.shoutlst = IcyListener.getInstance();
/*  97 */     this.shoutlst.reset();
/*  98 */     this.m_bitstream = new Bitstream(inputStream);
/*  99 */     this.m_decoder = new Decoder(null);
/* 100 */     this.m_equalizer = new Equalizer();
/* 101 */     this.m_equalizer_values = new float[32];
/* 102 */     for (int b = 0; b < this.m_equalizer.getBandCount(); ++b)
/*     */     {
/* 104 */       this.m_equalizer_values[b] = this.m_equalizer.getBand(b);
/*     */     }
/* 106 */     this.m_decoder.setEqualizer(this.m_equalizer);
/* 107 */     this.m_oBuffer = new DMAISObuffer(outputFormat.getChannels());
/* 108 */     this.m_decoder.setOutputBuffer(this.m_oBuffer);
/*     */     try
/*     */     {
/* 111 */       this.m_header = this.m_bitstream.readFrame();
/* 112 */       if ((this.m_header != null) && (this.frameslength == -1) && (this.byteslength > 0L)) this.frameslength = this.m_header.max_number_of_frames((int)this.byteslength);
/*     */     }
/*     */     catch (BitstreamException e)
/*     */     {
/* 116 */       TDebug.out("DecodedMpegAudioInputStream : Cannot read first frame : " + e.getMessage());
/* 117 */       this.byteslength = -1L;
/*     */     }
/* 119 */     this.properties = new HashMap();
/*     */   }
/*     */ 
/*     */   public Map properties()
/*     */   {
/* 140 */     this.properties.put("mp3.frame", new Long(this.currentFrame));
/* 141 */     this.properties.put("mp3.frame.bitrate", new Integer(this.currentBitrate));
/* 142 */     this.properties.put("mp3.frame.size.bytes", new Integer(this.currentFramesize));
/* 143 */     this.properties.put("mp3.position.byte", new Long(this.currentByte));
/* 144 */     this.properties.put("mp3.position.microseconds", new Long(this.currentMicrosecond));
/* 145 */     this.properties.put("mp3.equalizer", this.m_equalizer_values);
/*     */ 
/* 147 */     if (this.shoutlst != null)
/*     */     {
/* 149 */       String surl = this.shoutlst.getStreamUrl();
/* 150 */       String stitle = this.shoutlst.getStreamTitle();
/* 151 */       if ((stitle != null) && (stitle.trim().length() > 0)) this.properties.put("mp3.shoutcast.metadata.StreamTitle", stitle);
/* 152 */       if ((surl != null) && (surl.trim().length() > 0)) this.properties.put("mp3.shoutcast.metadata.StreamUrl", surl);
/*     */     }
/* 154 */     return this.properties;
/*     */   }
/*     */ 
/*     */   public void execute()
/*     */   {
/* 159 */     if (TDebug.TraceAudioConverter) TDebug.out("execute() : begin");
/*     */ 
/*     */     try
/*     */     {
/* 163 */       Header header = null;
/* 164 */       if (this.m_header == null) header = this.m_bitstream.readFrame(); else
/* 165 */         header = this.m_header;
/* 166 */       if (TDebug.TraceAudioConverter) TDebug.out("execute() : header = " + header);
/* 167 */       if (header == null)
/*     */       {
/* 169 */         if (TDebug.TraceAudioConverter)
/*     */         {
/* 171 */           TDebug.out("header is null (end of mpeg stream)");
/*     */         }
/* 173 */         getCircularBuffer().close();
/* 174 */         return;
/*     */       }
/* 176 */       this.currentFrame += 1L;
/* 177 */       this.currentBitrate = header.bitrate_instant();
/* 178 */       this.currentFramesize = header.calculate_framesize();
/* 179 */       this.currentByte += this.currentFramesize;
/* 180 */       this.currentMicrosecond = (long)((float)this.currentFrame * header.ms_per_frame() * 1000.0F);
/* 181 */       for (int b = 0; b < this.m_equalizer_values.length; ++b)
/*     */       {
/* 183 */         this.m_equalizer.setBand(b, this.m_equalizer_values[b]);
/*     */       }
/* 185 */       this.m_decoder.setEqualizer(this.m_equalizer);
/* 186 */       Obuffer decoderOutput = this.m_decoder.decodeFrame(header, this.m_bitstream);
/* 187 */       this.m_bitstream.closeFrame();
/* 188 */       getCircularBuffer().write(this.m_oBuffer.getBuffer(), 0, this.m_oBuffer.getCurrentBufferSize());
/* 189 */       this.m_oBuffer.reset();
/* 190 */       if (this.m_header != null) this.m_header = null;
/*     */     }
/*     */     catch (BitstreamException e)
/*     */     {
/* 194 */       if (TDebug.TraceAudioConverter)
/*     */       {
/* 196 */         TDebug.out(e);
/*     */       }
/*     */     }
/*     */     catch (DecoderException e)
/*     */     {
/* 201 */       if (TDebug.TraceAudioConverter)
/*     */       {
/* 203 */         TDebug.out(e);
/*     */       }
/*     */     }
/* 206 */     if (!TDebug.TraceAudioConverter) return; TDebug.out("execute() : end");
/*     */   }
/*     */ 
/*     */   public long skip(long bytes)
/*     */   {
/* 211 */     if ((this.byteslength > 0L) && (this.frameslength > 0))
/*     */     {
/* 213 */       float ratio = (float)bytes * 1.0F / (float)this.byteslength * 1.0F;
/* 214 */       long bytesread = skipFrames((long)(ratio * this.frameslength));
/* 215 */       this.currentByte += bytesread;
/* 216 */       this.m_header = null;
/* 217 */       return bytesread;
/*     */     }
/*     */ 
/* 221 */     return -1L;
/*     */   }
/*     */ 
/*     */   public long skipFrames(long frames)
/*     */   {
/* 232 */     if (TDebug.TraceAudioConverter) TDebug.out("skip(long frames) : begin");
/*     */ 
/* 234 */     if (this.m_encodedStream instanceof BufferedInputStream)
/*     */     {
/* 236 */       AudioInputStream a = (AudioInputStream)this.m_encodedStream;
/* 237 */       long skipped = -1L;
/*     */       try {
/* 239 */         skipped = this.m_encodedStream.skip(frames);
/*     */       } catch (IOException e) {
/* 241 */         e.printStackTrace();
/*     */       }
/* 243 */       return skipped;
/*     */     }
/*     */ 
/* 247 */     int framesRead = 0;
/* 248 */     int bytesReads = 0;
/*     */     try
/*     */     {
/* 251 */       for (int i = 0; i < frames; ++i)
/*     */       {
/* 253 */         Header header = this.m_bitstream.readFrame();
/* 254 */         if (header != null)
/*     */         {
/* 256 */           int fsize = header.calculate_framesize();
/* 257 */           bytesReads += fsize;
/*     */         }
/* 259 */         this.m_bitstream.closeFrame();
/* 260 */         ++framesRead;
/*     */       }
/*     */     }
/*     */     catch (BitstreamException e)
/*     */     {
/* 265 */       if (TDebug.TraceAudioConverter) TDebug.out(e);
/*     */     }
/* 267 */     if (TDebug.TraceAudioConverter) TDebug.out("skip(long frames) : end");
/* 268 */     this.currentFrame += framesRead;
/* 269 */     return bytesReads;
/*     */   }
/*     */ 
/*     */   private boolean isBigEndian()
/*     */   {
/* 275 */     return getFormat().isBigEndian();
/*     */   }
/*     */ 
/*     */   public void close() throws IOException
/*     */   {
/* 280 */     super.close();
/* 281 */     this.m_encodedStream.close();
/*     */   }
/*     */   public void tagParsed(TagParseEvent tpe) {
/*     */   }
/*     */   private class DMAISObuffer extends Obuffer {
/*     */     private int m_nChannels;
/*     */     private byte[] m_abBuffer;
/*     */     private int[] m_anBufferPointers;
/*     */     private boolean m_bIsBigEndian;
/*     */ 
/*     */     public DMAISObuffer(int nChannels) {
/* 292 */       this.m_nChannels = nChannels;
/* 293 */       this.m_abBuffer = new byte[2304 * nChannels];
/* 294 */       this.m_anBufferPointers = new int[nChannels];
/* 295 */       reset();
/* 296 */       this.m_bIsBigEndian = DecodedMpegAudioInputStream.this.isBigEndian();
/*     */     }
/*     */ 
/*     */     public void append(int nChannel, short sValue)
/*     */     {
/*     */       byte bFirstByte;
/*     */       byte bSecondByte;
/* 302 */       if (this.m_bIsBigEndian)
/*     */       {
/* 304 */         bFirstByte = (byte)(sValue >>> 8 & 0xFF);
/* 305 */         bSecondByte = (byte)(sValue & 0xFF);
/*     */       }
/*     */       else
/*     */       {
/* 309 */         bFirstByte = (byte)(sValue & 0xFF);
/* 310 */         bSecondByte = (byte)(sValue >>> 8 & 0xFF);
/*     */       }
/* 312 */       this.m_abBuffer[this.m_anBufferPointers[nChannel]] = bFirstByte;
/* 313 */       this.m_abBuffer[(this.m_anBufferPointers[nChannel] + 1)] = bSecondByte;
/* 314 */       this.m_anBufferPointers[nChannel] += this.m_nChannels * 2;
/*     */     }
/*     */ 
/*     */     public void set_stop_flag() {
/*     */     }
/*     */ 
/*     */     public void close() {
/*     */     }
/*     */ 
/*     */     public void write_buffer(int nValue) {
/*     */     }
/*     */ 
/*     */     public void clear_buffer() {
/*     */     }
/*     */ 
/*     */     public byte[] getBuffer() {
/* 330 */       return this.m_abBuffer;
/*     */     }
/*     */ 
/*     */     public int getCurrentBufferSize() {
/* 334 */       return this.m_anBufferPointers[0];
/*     */     }
/*     */ 
/*     */     public void reset() {
/* 338 */       for (int i = 0; i < this.m_nChannels; ++i)
/*     */       {
/* 344 */         this.m_anBufferPointers[i] = (i * 2);
/*     */       }
/*     */     }
/*     */   }
/*     */ }
