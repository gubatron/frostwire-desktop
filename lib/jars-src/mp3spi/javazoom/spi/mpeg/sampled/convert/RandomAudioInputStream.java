/*    */ package javazoom.spi.mpeg.sampled.convert;
/*    */ 
/*    */ import java.io.IOException;
/*    */ import java.io.InputStream;
/*    */ import java.io.RandomAccessFile;
/*    */ 
/*    */ public class RandomAudioInputStream extends InputStream
/*    */ {
/*    */   private RandomAccessFile file;
/*    */   private long pos;
/*    */ 
/*    */   public RandomAudioInputStream(RandomAccessFile file)
/*    */   {
/* 13 */     this.file = file;
/* 14 */     this.pos = 0L;
/*    */   }
/*    */ 
/*    */   public int read() throws IOException
/*    */   {
/* 19 */     int a = this.file.read();
/* 20 */     this.pos += 1L;
/* 21 */     return a;
/*    */   }
/*    */ 
/*    */   public int read(byte[] b) throws IOException {
/* 25 */     return read(b, 0, b.length);
/*    */   }
/*    */ 
/*    */   public int read(byte[] b, int off, int len) throws IOException
/*    */   {
/* 30 */     if (this.file == null) {
/* 31 */       return -1;
/*    */     }
/* 33 */     int r = this.file.read(b, off, len);
/* 34 */     this.pos += r;
/* 35 */     return r;
/*    */   }
/*    */ 
/*    */   public int available()
/*    */     throws IOException
/*    */   {
/* 42 */     return (int)(this.file.length() - this.pos);
/*    */   }
/*    */ 
/*    */   public long skip(long l) throws IOException
/*    */   {
/* 47 */     this.file.seek(l);
/* 48 */     this.pos = l;
/* 49 */     return l;
/*    */   }
/*    */ 
/*    */   public void close() throws IOException
/*    */   {
/* 54 */     this.file.close();
/*    */   }
/*    */ }
