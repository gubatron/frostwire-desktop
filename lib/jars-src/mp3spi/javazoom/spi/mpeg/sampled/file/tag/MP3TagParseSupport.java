/*    */ package javazoom.spi.mpeg.sampled.file.tag;
/*    */ 
/*    */ import java.util.ArrayList;
/*    */ 
/*    */ public class MP3TagParseSupport
/*    */ {
/*    */   ArrayList tagParseListeners;
/*    */ 
/*    */   public MP3TagParseSupport()
/*    */   {
/* 37 */     this.tagParseListeners = new ArrayList();
/*    */   }
/*    */ 
/*    */   public void addTagParseListener(TagParseListener tpl)
/*    */   {
/* 43 */     this.tagParseListeners.add(tpl);
/*    */   }
/*    */ 
/*    */   public void removeTagParseListener(TagParseListener tpl)
/*    */   {
/* 49 */     this.tagParseListeners.add(tpl);
/*    */   }
/*    */ 
/*    */   public void fireTagParseEvent(TagParseEvent tpe)
/*    */   {
/* 54 */     for (int i = 0; i < this.tagParseListeners.size(); ++i) {
/* 55 */       TagParseListener l = (TagParseListener)this.tagParseListeners.get(i);
/* 56 */       l.tagParsed(tpe);
/*    */     }
/*    */   }
/*    */ 
/*    */   public void fireTagParsed(Object source, MP3Tag tag) {
/* 60 */     fireTagParseEvent(new TagParseEvent(source, tag));
/*    */   }
/*    */ }
