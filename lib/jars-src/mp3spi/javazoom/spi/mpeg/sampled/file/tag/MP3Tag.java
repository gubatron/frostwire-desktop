/*    */ package javazoom.spi.mpeg.sampled.file.tag;
/*    */ 
/*    */ public abstract class MP3Tag
/*    */ {
/*    */   protected String name;
/*    */   protected Object value;
/*    */ 
/*    */   public MP3Tag(String name, Object value)
/*    */   {
/* 36 */     this.name = name;
/* 37 */     this.value = value;
/*    */   }
/*    */   public String getName() {
/* 40 */     return this.name;
/*    */   }
/*    */   public Object getValue() {
/* 43 */     return this.value;
/*    */   }
/*    */   public String toString() {
/* 46 */     return super.getClass().getName() + 
/* 47 */       " -- " + 
/* 48 */       getName() + 
/* 49 */       ":" + 
/* 50 */       getValue().toString();
/*    */   }
/*    */ }

