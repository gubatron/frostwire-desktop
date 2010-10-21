/*     */ package javazoom.spi.mpeg.sampled.file.tag;
/*     */ 
/*     */ import java.io.BufferedInputStream;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.io.PrintStream;
/*     */ import java.io.UnsupportedEncodingException;
/*     */ import java.net.URL;
/*     */ import java.net.URLConnection;
/*     */ import java.util.Collection;
/*     */ import java.util.HashMap;
/*     */ import java.util.StringTokenizer;
/*     */ 
/*     */ public class IcyInputStream extends BufferedInputStream
/*     */   implements MP3MetadataParser
/*     */ {
/*  68 */   public static boolean DEBUG = false;
/*     */   MP3TagParseSupport tagParseSupport;
/*     */   protected static final String INLINE_TAG_SEPARATORS = "";
/*     */   HashMap tags;
/* 108 */   protected byte[] crlfBuffer = new byte[1024];
/*     */ 
/* 113 */   protected int metaint = -1;
/*     */ 
/* 117 */   protected int bytesUntilNextMetadata = -1;
/*     */ 
/*     */   public IcyInputStream(InputStream in)
/*     */     throws IOException
/*     */   {
/* 125 */     super(in);
/* 126 */     this.tags = new HashMap();
/* 127 */     this.tagParseSupport = new MP3TagParseSupport();
/*     */ 
/* 131 */     readInitialHeaders();
/* 132 */     IcyTag metaIntTag = (IcyTag)getTag("icy-metaint");
/* 133 */     if (DEBUG) System.out.println("METATAG:" + metaIntTag);
/* 134 */     if (metaIntTag != null) {
/* 135 */       String metaIntString = metaIntTag.getValueAsString();
/*     */       try {
/* 137 */         this.metaint = Integer.parseInt(metaIntString.trim());
/* 138 */         if (DEBUG) System.out.println("METAINT:" + this.metaint);
/* 139 */         this.bytesUntilNextMetadata = this.metaint;
/*     */       }
/*     */       catch (NumberFormatException localNumberFormatException)
/*     */       {
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public IcyInputStream(InputStream in, String metaIntString)
/*     */     throws IOException
/*     */   {
/* 153 */     super(in);
/* 154 */     this.tags = new HashMap();
/* 155 */     this.tagParseSupport = new MP3TagParseSupport();
/*     */     try
/*     */     {
/* 158 */       this.metaint = Integer.parseInt(metaIntString.trim());
/* 159 */       if (DEBUG) System.out.println("METAINT:" + this.metaint);
/* 160 */       this.bytesUntilNextMetadata = this.metaint;
/*     */     }
/*     */     catch (NumberFormatException localNumberFormatException)
/*     */     {
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void readInitialHeaders()
/*     */     throws IOException
/*     */   {
/* 171 */     String line = null;
/* 172 */     while (!(line = readCRLFLine()).equals("")) {
/* 173 */       int colonIndex = line.indexOf(':');
/*     */ 
/* 175 */       if (colonIndex == -1)
/*     */         continue;
/* 177 */       IcyTag tag = 
/* 178 */         new IcyTag(
/* 179 */         line.substring(0, colonIndex), 
/* 180 */         line.substring(colonIndex + 1));
/*     */ 
/* 182 */       addTag(tag);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected String readCRLFLine()
/*     */     throws IOException
/*     */   {
/* 189 */     int i = 0;
/* 190 */     for (; i < this.crlfBuffer.length; ++i) {
/* 191 */       byte aByte = (byte)read();
/* 192 */       if (aByte == 13)
/*     */       {
/* 194 */         byte anotherByte = (byte)read();
/* 195 */         ++i;
/* 196 */         if (anotherByte == 10)
/*     */         {
/*     */           break;
/*     */         }
/*     */ 
/* 201 */         this.crlfBuffer[(i - 1)] = aByte;
/* 202 */         this.crlfBuffer[i] = anotherByte;
/*     */       }
/*     */       else
/*     */       {
/* 207 */         this.crlfBuffer[i] = aByte;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 212 */     return new String(this.crlfBuffer, 0, i - 1);
/*     */   }
/*     */ 
/*     */   public int read()
/*     */     throws IOException
/*     */   {
/* 220 */     if (this.bytesUntilNextMetadata > 0) {
/* 221 */       this.bytesUntilNextMetadata -= 1;
/* 222 */       return super.read();
/*     */     }
/* 224 */     if (this.bytesUntilNextMetadata == 0)
/*     */     {
/* 226 */       readMetadata();
/* 227 */       this.bytesUntilNextMetadata = (this.metaint - 1);
/*     */ 
/* 229 */       return super.read();
/*     */     }
/*     */ 
/* 233 */     return super.read();
/*     */   }
/*     */ 
/*     */   public int read(byte[] buf, int offset, int length)
/*     */     throws IOException
/*     */   {
/* 247 */     if (this.bytesUntilNextMetadata > 0) {
/* 248 */       int adjLength = Math.min(length, this.bytesUntilNextMetadata);
/* 249 */       int got = super.read(buf, offset, adjLength);
/* 250 */       this.bytesUntilNextMetadata -= got;
/* 251 */       return got;
/*     */     }
/* 253 */     if (this.bytesUntilNextMetadata == 0)
/*     */     {
/* 255 */       readMetadata();
/*     */ 
/* 264 */       this.bytesUntilNextMetadata = this.metaint;
/* 265 */       int adjLength = Math.min(length, this.bytesUntilNextMetadata);
/* 266 */       int got = super.read(buf, offset, adjLength);
/* 267 */       this.bytesUntilNextMetadata -= got;
/*     */ 
/* 271 */       return got;
/*     */     }
/*     */ 
/* 275 */     return super.read(buf, offset, length);
/*     */   }
/*     */ 
/*     */   public int read(byte[] buf)
/*     */     throws IOException
/*     */   {
/* 281 */     return read(buf, 0, buf.length);
/*     */   }
/*     */ 
/*     */   protected void readMetadata()
/*     */     throws IOException
/*     */   {
/* 289 */     int blockCount = super.read();
/* 290 */     if (DEBUG) System.out.println("BLOCKCOUNT:" + blockCount);
/*     */ 
/* 292 */     int byteCount = blockCount * 16;
/* 293 */     if (byteCount < 0)
/* 294 */       return;
/* 295 */     byte[] metadataBlock = new byte[byteCount];
/* 296 */     int index = 0;
/*     */ 
/* 298 */     while (byteCount > 0) {
/* 299 */       int bytesRead = super.read(metadataBlock, index, byteCount);
/* 300 */       index += bytesRead;
/* 301 */       byteCount -= bytesRead;
/*     */     }
/*     */ 
/* 304 */     if (blockCount > 0)
/* 305 */       parseInlineIcyTags(metadataBlock);
/*     */   }
/*     */ 
/*     */   protected void parseInlineIcyTags(byte[] tagBlock)
/*     */   {
/* 326 */     String blockString = null;
/*     */     try
/*     */     {
/* 330 */       blockString = new String(tagBlock, "ISO-8859-1");
/*     */     }
/*     */     catch (UnsupportedEncodingException e)
/*     */     {
/* 334 */       blockString = new String(tagBlock);
/*     */     }
/* 336 */     if (DEBUG) System.out.println("BLOCKSTR:" + blockString);
/* 337 */     StringTokenizer izer = 
/* 338 */       new StringTokenizer(blockString, "");
/* 339 */     int i = 0;
/* 340 */     while (izer.hasMoreTokens()) {
/* 341 */       String tagString = izer.nextToken();
/* 342 */       int separatorIdx = tagString.indexOf('=');
/* 343 */       if (separatorIdx == -1) {
/*     */         continue;
/*     */       }
/* 346 */       int valueStartIdx = 
/* 347 */         (tagString.charAt(separatorIdx + 1) == '\'') ? 
/* 348 */         separatorIdx + 2 : 
/* 349 */         separatorIdx + 1;
/* 350 */       int valueEndIdx = 
/* 351 */         (tagString.charAt(tagString.length() - 1) == '\'') ? 
/* 352 */         tagString.length() - 1 : 
/* 353 */         tagString.length();
/* 354 */       String name = tagString.substring(0, separatorIdx);
/* 355 */       String value = tagString.substring(valueStartIdx, valueEndIdx);
/*     */ 
/* 357 */       IcyTag tag = new IcyTag(name, value);
/* 358 */       addTag(tag);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void addTag(IcyTag tag)
/*     */   {
/* 366 */     this.tags.put(tag.getName(), tag);
/*     */ 
/* 368 */     this.tagParseSupport.fireTagParsed(this, tag);
/*     */   }
/*     */ 
/*     */   public MP3Tag getTag(String tagName)
/*     */   {
/* 374 */     return (MP3Tag)this.tags.get(tagName);
/*     */   }
/*     */ 
/*     */   public MP3Tag[] getTags()
/*     */   {
/* 379 */     return (MP3Tag[])this.tags.values().toArray(new MP3Tag[0]);
/*     */   }
/*     */ 
/*     */   public HashMap getTagHash()
/*     */   {
/* 385 */     return this.tags;
/*     */   }
/*     */ 
/*     */   public void addTagParseListener(TagParseListener tpl)
/*     */   {
/* 391 */     this.tagParseSupport.addTagParseListener(tpl);
/*     */   }
/*     */ 
/*     */   public void removeTagParseListener(TagParseListener tpl)
/*     */   {
/* 397 */     this.tagParseSupport.removeTagParseListener(tpl);
/*     */   }
/*     */ 
/*     */   public static void main(String[] args)
/*     */   {
/* 402 */     byte[] chow = new byte[200];
/* 403 */     if (args.length != 1)
/*     */     {
/* 405 */       return;
/*     */     }
/*     */     try {
/* 408 */       URL url = new URL(args[0]);
/* 409 */       URLConnection conn = url.openConnection();
/* 410 */       conn.setRequestProperty("Icy-Metadata", "1");
/* 411 */       IcyInputStream icy = 
/* 412 */         new IcyInputStream(
/* 413 */         new BufferedInputStream(conn.getInputStream()));
/* 414 */       while (icy.available() > -1)
/*     */       {
/* 416 */         icy.read(chow, 0, chow.length);
/*     */       }
/*     */     }
/*     */     catch (Exception e) {
/* 420 */       e.printStackTrace();
/*     */     }
/*     */   }
/*     */ }

