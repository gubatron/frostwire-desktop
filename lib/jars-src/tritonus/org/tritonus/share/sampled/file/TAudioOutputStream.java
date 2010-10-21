/*     */ package org.tritonus.share.sampled.file;
/*     */ 
/*     */ import java.io.IOException;
/*     */ import javax.sound.sampled.AudioFormat;
/*     */ import org.tritonus.share.TDebug;
/*     */ 
/*     */ public abstract class TAudioOutputStream
/*     */   implements AudioOutputStream
/*     */ {
/*     */   private AudioFormat m_audioFormat;
/*     */   private long m_lLength;
/*     */   private long m_lCalculatedLength;
/*     */   private TDataOutputStream m_dataOutputStream;
/*     */   private boolean m_bDoBackPatching;
/*     */   private boolean m_bHeaderWritten;
/*     */ 
/*     */   protected TAudioOutputStream(AudioFormat audioFormat, long lLength, TDataOutputStream dataOutputStream, boolean bDoBackPatching)
/*     */   {
/*  60 */     this.m_audioFormat = audioFormat;
/*  61 */     this.m_lLength = lLength;
/*  62 */     this.m_lCalculatedLength = 0L;
/*  63 */     this.m_dataOutputStream = dataOutputStream;
/*  64 */     this.m_bDoBackPatching = bDoBackPatching;
/*  65 */     this.m_bHeaderWritten = false;
/*     */   }
/*     */ 
/*     */   public AudioFormat getFormat()
/*     */   {
/*  72 */     return this.m_audioFormat;
/*     */   }
/*     */ 
/*     */   public long getLength()
/*     */   {
/*  83 */     return this.m_lLength;
/*     */   }
/*     */ 
/*     */   public long getCalculatedLength()
/*     */   {
/*  93 */     return this.m_lCalculatedLength;
/*     */   }
/*     */ 
/*     */   protected TDataOutputStream getDataOutputStream()
/*     */   {
/*  98 */     return this.m_dataOutputStream;
/*     */   }
/*     */ 
/*     */   public int write(byte[] abData, int nOffset, int nLength)
/*     */     throws IOException
/*     */   {
/* 108 */     if (TDebug.TraceAudioOutputStream)
/*     */     {
/* 110 */       TDebug.out("TAudioOutputStream.write(): wanted length: " + nLength);
/*     */     }
/* 112 */     if (!this.m_bHeaderWritten)
/*     */     {
/* 114 */       writeHeader();
/* 115 */       this.m_bHeaderWritten = true;
/*     */     }
/*     */ 
/* 119 */     long lTotalLength = getLength();
/* 120 */     if ((lTotalLength != -1L) && (this.m_lCalculatedLength + nLength > lTotalLength)) {
/* 121 */       if (TDebug.TraceAudioOutputStream) {
/* 122 */         TDebug.out("TAudioOutputStream.write(): requested more bytes to write than possible.");
/*     */       }
/* 124 */       nLength = (int)(lTotalLength - this.m_lCalculatedLength);
/*     */ 
/* 126 */       if (nLength < 0) {
/* 127 */         nLength = 0;
/*     */       }
/*     */     }
/*     */ 
/* 131 */     if (nLength > 0) {
/* 132 */       this.m_dataOutputStream.write(abData, nOffset, nLength);
/* 133 */       this.m_lCalculatedLength += nLength;
/*     */     }
/* 135 */     if (TDebug.TraceAudioOutputStream)
/*     */     {
/* 137 */       TDebug.out("TAudioOutputStream.write(): calculated (total) length: " + this.m_lCalculatedLength + " bytes = " + this.m_lCalculatedLength / getFormat().getFrameSize() + " frames");
/*     */     }
/* 139 */     return nLength;
/*     */   }
/*     */ 
/*     */   protected abstract void writeHeader()
/*     */     throws IOException;
/*     */ 
/*     */   public void close()
/*     */     throws IOException
/*     */   {
/* 158 */     if (TDebug.TraceAudioOutputStream)
/*     */     {
/* 160 */       TDebug.out("TAudioOutputStream.close(): called");
/*     */     }
/*     */ 
/* 163 */     if (this.m_bDoBackPatching)
/*     */     {
/* 165 */       if (TDebug.TraceAudioOutputStream)
/*     */       {
/* 167 */         TDebug.out("TAudioOutputStream.close(): patching header");
/*     */       }
/* 169 */       patchHeader();
/*     */     }
/* 171 */     this.m_dataOutputStream.close();
/*     */   }
/*     */ 
/*     */   protected void patchHeader()
/*     */     throws IOException
/*     */   {
/* 179 */     TDebug.out("TAudioOutputStream.patchHeader(): called");
/*     */   }
/*     */ 
/*     */   protected void setLengthFromCalculatedLength()
/*     */   {
/* 187 */     this.m_lLength = this.m_lCalculatedLength;
/*     */   }
/*     */ }
