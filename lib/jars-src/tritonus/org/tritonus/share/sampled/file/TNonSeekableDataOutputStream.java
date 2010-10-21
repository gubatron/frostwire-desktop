/*    */ package org.tritonus.share.sampled.file;
/*    */ 
/*    */ import java.io.DataOutputStream;
/*    */ import java.io.IOException;
/*    */ import java.io.OutputStream;
/*    */ 
/*    */ public class TNonSeekableDataOutputStream extends DataOutputStream
/*    */   implements TDataOutputStream
/*    */ {
/*    */   public TNonSeekableDataOutputStream(OutputStream outputStream)
/*    */   {
/* 48 */     super(outputStream);
/*    */   }
/*    */ 
/*    */   public boolean supportsSeek()
/*    */   {
/* 55 */     return false;
/*    */   }
/*    */ 
/*    */   public void seek(long position)
/*    */     throws IOException
/*    */   {
/* 63 */     throw new IllegalArgumentException("TNonSeekableDataOutputStream: Call to seek not allowed.");
/*    */   }
/*    */ 
/*    */   public long getFilePointer()
/*    */     throws IOException
/*    */   {
/* 71 */     throw new IllegalArgumentException("TNonSeekableDataOutputStream: Call to getFilePointer not allowed.");
/*    */   }
/*    */ 
/*    */   public long length()
/*    */     throws IOException
/*    */   {
/* 79 */     throw new IllegalArgumentException("TNonSeekableDataOutputStream: Call to length not allowed.");
/*    */   }
/*    */ 
/*    */   public void writeLittleEndian32(int value)
/*    */     throws IOException
/*    */   {
/* 87 */     writeByte(value & 0xFF);
/* 88 */     writeByte(value >> 8 & 0xFF);
/* 89 */     writeByte(value >> 16 & 0xFF);
/* 90 */     writeByte(value >> 24 & 0xFF);
/*    */   }
/*    */ 
/*    */   public void writeLittleEndian16(short value)
/*    */     throws IOException
/*    */   {
/* 98 */     writeByte(value & 0xFF);
/* 99 */     writeByte(value >> 8 & 0xFF);
/*    */   }
/*    */ }

