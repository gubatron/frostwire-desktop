/*    */ package org.tritonus.share.sampled.file;
/*    */ 
/*    */ import java.io.File;
/*    */ import java.io.IOException;
/*    */ import java.io.RandomAccessFile;
/*    */ 
/*    */ public class TSeekableDataOutputStream extends RandomAccessFile
/*    */   implements TDataOutputStream
/*    */ {
/*    */   public TSeekableDataOutputStream(File file)
/*    */     throws IOException
/*    */   {
/* 49 */     super(file, "rw");
/*    */   }
/*    */ 
/*    */   public boolean supportsSeek()
/*    */   {
/* 56 */     return true;
/*    */   }
/*    */ 
/*    */   public void writeLittleEndian32(int value)
/*    */     throws IOException
/*    */   {
/* 64 */     writeByte(value & 0xFF);
/* 65 */     writeByte(value >> 8 & 0xFF);
/* 66 */     writeByte(value >> 16 & 0xFF);
/* 67 */     writeByte(value >> 24 & 0xFF);
/*    */   }
/*    */ 
/*    */   public void writeLittleEndian16(short value)
/*    */     throws IOException
/*    */   {
/* 75 */     writeByte(value & 0xFF);
/* 76 */     writeByte(value >> 8 & 0xFF);
/*    */   }
/*    */ }
