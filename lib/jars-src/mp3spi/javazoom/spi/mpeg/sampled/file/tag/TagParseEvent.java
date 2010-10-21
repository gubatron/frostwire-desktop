/*    */ package javazoom.spi.mpeg.sampled.file.tag;
/*    */ 
/*    */ import java.util.EventObject;
/*    */ 
/*    */ public class TagParseEvent extends EventObject
/*    */ {
/*    */   protected MP3Tag tag;
/*    */ 
/*    */   public TagParseEvent(Object source, MP3Tag tag)
/*    */   {
/* 36 */     super(source);
/* 37 */     this.tag = tag;
/*    */   }
/*    */ 
/*    */   public MP3Tag getTag()
/*    */   {
/* 42 */     return this.tag;
/*    */   }
/*    */ }

