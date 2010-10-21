/*     */ package javazoom.spi.mpeg.sampled.file;
/*     */ 
/*     */ import javazoom.spi.mpeg.sampled.file.tag.MP3Tag;
/*     */ import javazoom.spi.mpeg.sampled.file.tag.TagParseEvent;
/*     */ import javazoom.spi.mpeg.sampled.file.tag.TagParseListener;
/*     */ 
/*     */ public class IcyListener
/*     */   implements TagParseListener
/*     */ {
/*  36 */   private static IcyListener instance = null;
/*  37 */   private MP3Tag lastTag = null;
/*  38 */   private String streamTitle = null;
/*  39 */   private String streamUrl = null;
/*     */ 
/*     */   public static synchronized IcyListener getInstance()
/*     */   {
/*  49 */     if (instance == null)
/*     */     {
/*  51 */       instance = new IcyListener();
/*     */     }
/*  53 */     return instance;
/*     */   }
/*     */ 
/*     */   public void tagParsed(TagParseEvent tpe)
/*     */   {
/*  61 */     this.lastTag = tpe.getTag();
/*  62 */     String name = this.lastTag.getName();
/*  63 */     if ((name != null) && (name.equalsIgnoreCase("streamtitle")))
/*     */     {
/*  65 */       this.streamTitle = ((String)this.lastTag.getValue());
/*     */     } else {
/*  67 */       if ((name == null) || (!name.equalsIgnoreCase("streamurl")))
/*     */         return;
/*  69 */       this.streamUrl = ((String)this.lastTag.getValue());
/*     */     }
/*     */   }
/*     */ 
/*     */   public MP3Tag getLastTag()
/*     */   {
/*  78 */     return this.lastTag;
/*     */   }
/*     */ 
/*     */   public void setLastTag(MP3Tag tag)
/*     */   {
/*  86 */     this.lastTag = tag;
/*     */   }
/*     */ 
/*     */   public String getStreamTitle()
/*     */   {
/*  94 */     return this.streamTitle;
/*     */   }
/*     */ 
/*     */   public String getStreamUrl()
/*     */   {
/* 102 */     return this.streamUrl;
/*     */   }
/*     */ 
/*     */   public void setStreamTitle(String string)
/*     */   {
/* 110 */     this.streamTitle = string;
/*     */   }
/*     */ 
/*     */   public void setStreamUrl(String string)
/*     */   {
/* 118 */     this.streamUrl = string;
/*     */   }
/*     */ 
/*     */   public void reset()
/*     */   {
/* 126 */     this.lastTag = null;
/* 127 */     this.streamTitle = null;
/* 128 */     this.streamUrl = null;
/*     */   }
/*     */ }
