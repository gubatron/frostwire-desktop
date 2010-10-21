/*    */ package org.tritonus.share.sampled.file;
/*    */ 
/*    */ import java.io.IOException;
/*    */ import javax.sound.sampled.AudioFormat;
/*    */ 
/*    */ public class HeaderlessAudioOutputStream extends TAudioOutputStream
/*    */ {
/*    */   public HeaderlessAudioOutputStream(AudioFormat audioFormat, long lLength, TDataOutputStream dataOutputStream)
/*    */   {
/* 44 */     super(audioFormat, lLength, dataOutputStream, false);
/*    */   }
/*    */ 
/*    */   protected void writeHeader()
/*    */     throws IOException
/*    */   {
/*    */   }
/*    */ }

