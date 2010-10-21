/*    */ package org.tritonus.share.sampled.file;
/*    */ 
/*    */ import java.io.IOException;
/*    */ import java.util.Collection;
/*    */ import javax.sound.sampled.AudioFileFormat;
/*    */ import javax.sound.sampled.AudioFormat;
/*    */ import org.tritonus.share.TDebug;
/*    */ 
/*    */ public class THeaderlessAudioFileWriter extends TAudioFileWriter
/*    */ {
/*    */   protected THeaderlessAudioFileWriter(Collection fileTypes, Collection audioFormats)
/*    */   {
/* 52 */     super(fileTypes, audioFormats);
/* 53 */     if (TDebug.TraceAudioFileWriter) TDebug.out("THeaderlessAudioFileWriter.<init>(): begin");
/* 54 */     if (!TDebug.TraceAudioFileWriter) return; TDebug.out("THeaderlessAudioFileWriter.<init>(): end");
/*    */   }
/*    */ 
/*    */   protected AudioOutputStream getAudioOutputStream(AudioFormat audioFormat, long lLengthInBytes, AudioFileFormat.Type fileType, TDataOutputStream dataOutputStream)
/*    */     throws IOException
/*    */   {
/* 66 */     if (TDebug.TraceAudioFileWriter) TDebug.out("THeaderlessAudioFileWriter.getAudioOutputStream(): begin");
/* 67 */     AudioOutputStream aos = new HeaderlessAudioOutputStream(audioFormat, lLengthInBytes, dataOutputStream);
/*    */ 
/* 71 */     if (TDebug.TraceAudioFileWriter) TDebug.out("THeaderlessAudioFileWriter.getAudioOutputStream(): end");
/* 72 */     return aos;
/*    */   }
/*    */ }
