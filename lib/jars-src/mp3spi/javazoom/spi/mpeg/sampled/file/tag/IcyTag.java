/*    */ package javazoom.spi.mpeg.sampled.file.tag;
/*    */ 
/*    */ public class IcyTag extends MP3Tag
/*    */   implements StringableTag
/*    */ {
/*    */   public IcyTag(String name, String stringValue)
/*    */   {
/* 36 */     super(name, stringValue);
/*    */   }
/*    */ 
/*    */   public String getValueAsString() {
/* 40 */     return (String)getValue();
/*    */   }
/*    */ }

