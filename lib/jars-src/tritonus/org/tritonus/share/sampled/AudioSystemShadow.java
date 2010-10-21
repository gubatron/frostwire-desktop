/*     */ package org.tritonus.share.sampled;
/*     */ 
/*     */ import java.io.File;
/*     */ import java.io.IOException;
/*     */ import java.io.OutputStream;
/*     */ import javax.sound.sampled.AudioFileFormat;
/*     */ import javax.sound.sampled.AudioFormat;
/*     */ import org.tritonus.sampled.file.AiffAudioOutputStream;
/*     */ import org.tritonus.sampled.file.AuAudioOutputStream;
/*     */ import org.tritonus.sampled.file.WaveAudioOutputStream;
/*     */ import org.tritonus.share.sampled.file.AudioOutputStream;
/*     */ import org.tritonus.share.sampled.file.TDataOutputStream;
/*     */ import org.tritonus.share.sampled.file.TNonSeekableDataOutputStream;
/*     */ import org.tritonus.share.sampled.file.TSeekableDataOutputStream;
/*     */ 
/*     */ public class AudioSystemShadow
/*     */ {
/*     */   public static TDataOutputStream getDataOutputStream(File file)
/*     */     throws IOException
/*     */   {
/*  56 */     return new TSeekableDataOutputStream(file);
/*     */   }
/*     */ 
/*     */   public static TDataOutputStream getDataOutputStream(OutputStream stream)
/*     */     throws IOException
/*     */   {
/*  64 */     return new TNonSeekableDataOutputStream(stream);
/*     */   }
/*     */ 
/*     */   public static AudioOutputStream getAudioOutputStream(AudioFileFormat.Type type, AudioFormat audioFormat, long lLengthInBytes, TDataOutputStream dataOutputStream)
/*     */   {
/*  72 */     AudioOutputStream audioOutputStream = null;
/*     */ 
/*  74 */     if ((type.equals(AudioFileFormat.Type.AIFF)) || (type.equals(AudioFileFormat.Type.AIFF)))
/*     */     {
/*  77 */       audioOutputStream = new AiffAudioOutputStream(audioFormat, type, lLengthInBytes, dataOutputStream);
/*     */     }
/*  79 */     else if (type.equals(AudioFileFormat.Type.AU))
/*     */     {
/*  81 */       audioOutputStream = new AuAudioOutputStream(audioFormat, lLengthInBytes, dataOutputStream);
/*     */     }
/*  83 */     else if (type.equals(AudioFileFormat.Type.WAVE))
/*     */     {
/*  85 */       audioOutputStream = new WaveAudioOutputStream(audioFormat, lLengthInBytes, dataOutputStream);
/*     */     }
/*  87 */     return audioOutputStream;
/*     */   }
/*     */ 
/*     */   public static AudioOutputStream getAudioOutputStream(AudioFileFormat.Type type, AudioFormat audioFormat, long lLengthInBytes, File file)
/*     */     throws IOException
/*     */   {
/*  95 */     TDataOutputStream dataOutputStream = getDataOutputStream(file);
/*  96 */     AudioOutputStream audioOutputStream = getAudioOutputStream(type, audioFormat, lLengthInBytes, dataOutputStream);
/*  97 */     return audioOutputStream;
/*     */   }
/*     */ 
/*     */   public static AudioOutputStream getAudioOutputStream(AudioFileFormat.Type type, AudioFormat audioFormat, long lLengthInBytes, OutputStream outputStream)
/*     */     throws IOException
/*     */   {
/* 105 */     TDataOutputStream dataOutputStream = getDataOutputStream(outputStream);
/* 106 */     AudioOutputStream audioOutputStream = getAudioOutputStream(type, audioFormat, lLengthInBytes, dataOutputStream);
/* 107 */     return audioOutputStream;
/*     */   }
/*     */ }
