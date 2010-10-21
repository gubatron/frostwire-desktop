/*     */ package org.tritonus.share.sampled.mixer;
/*     */ 
/*     */ import java.util.ArrayList;
/*     */ import java.util.Collection;
/*     */ import java.util.Iterator;
/*     */ import java.util.Set;
/*     */ import javax.sound.sampled.AudioFormat;
/*     */ import javax.sound.sampled.Clip;
/*     */ import javax.sound.sampled.DataLine;
/*     */ import javax.sound.sampled.Line;
/*     */ import javax.sound.sampled.LineUnavailableException;
/*     */ import javax.sound.sampled.Mixer;
/*     */ import javax.sound.sampled.Port;
/*     */ import javax.sound.sampled.SourceDataLine;
/*     */ import javax.sound.sampled.TargetDataLine;
/*     */ import org.tritonus.share.ArraySet;
/*     */ import org.tritonus.share.TDebug;
/*     */ import org.tritonus.share.sampled.AudioFormats;
/*     */ 
/*     */ public abstract class TMixer extends TLine
/*     */   implements Mixer
/*     */ {
/*  55 */   private static Line.Info[] EMPTY_LINE_INFO_ARRAY = new Line.Info[0];
/*  56 */   private static Line[] EMPTY_LINE_ARRAY = new Line[0];
/*     */   private Mixer.Info m_mixerInfo;
/*     */   private Collection m_supportedSourceFormats;
/*     */   private Collection m_supportedTargetFormats;
/*     */   private Collection m_supportedSourceLineInfos;
/*     */   private Collection m_supportedTargetLineInfos;
/*     */   private Set m_openSourceDataLines;
/*     */   private Set m_openTargetDataLines;
/*     */ 
/*     */   protected TMixer(Mixer.Info mixerInfo, Line.Info lineInfo)
/*     */   {
/*  72 */     this(mixerInfo, lineInfo, new ArrayList(), new ArrayList(), new ArrayList(), new ArrayList());
/*     */   }
/*     */ 
/*     */   protected TMixer(Mixer.Info mixerInfo, Line.Info lineInfo, Collection supportedSourceFormats, Collection supportedTargetFormats, Collection supportedSourceLineInfos, Collection supportedTargetLineInfos)
/*     */   {
/*  91 */     super(null, lineInfo);
/*     */ 
/*  93 */     if (TDebug.TraceMixer) TDebug.out("TMixer.<init>(): begin");
/*  94 */     this.m_mixerInfo = mixerInfo;
/*  95 */     setSupportInformation(supportedSourceFormats, supportedTargetFormats, supportedSourceLineInfos, supportedTargetLineInfos);
/*     */ 
/* 100 */     this.m_openSourceDataLines = new ArraySet();
/* 101 */     this.m_openTargetDataLines = new ArraySet();
/* 102 */     if (!TDebug.TraceMixer) return; TDebug.out("TMixer.<init>(): end");
/*     */   }
/*     */ 
/*     */   protected void setSupportInformation(Collection supportedSourceFormats, Collection supportedTargetFormats, Collection supportedSourceLineInfos, Collection supportedTargetLineInfos)
/*     */   {
/* 113 */     if (TDebug.TraceMixer) TDebug.out("TMixer.setSupportInformation(): begin");
/* 114 */     this.m_supportedSourceFormats = supportedSourceFormats;
/* 115 */     this.m_supportedTargetFormats = supportedTargetFormats;
/* 116 */     this.m_supportedSourceLineInfos = supportedSourceLineInfos;
/* 117 */     this.m_supportedTargetLineInfos = supportedTargetLineInfos;
/* 118 */     if (!TDebug.TraceMixer) return; TDebug.out("TMixer.setSupportInformation(): end");
/*     */   }
/*     */ 
/*     */   public Mixer.Info getMixerInfo()
/*     */   {
/* 125 */     if (TDebug.TraceMixer) TDebug.out("TMixer.getMixerInfo(): begin");
/* 126 */     if (TDebug.TraceMixer) TDebug.out("TMixer.getMixerInfo(): end");
/* 127 */     return this.m_mixerInfo;
/*     */   }
/*     */ 
/*     */   public Line.Info[] getSourceLineInfo()
/*     */   {
/* 134 */     if (TDebug.TraceMixer) TDebug.out("TMixer.getSourceLineInfo(): begin");
/* 135 */     Line.Info[] infos = (Line.Info[])this.m_supportedSourceLineInfos.toArray(EMPTY_LINE_INFO_ARRAY);
/* 136 */     if (TDebug.TraceMixer) TDebug.out("TMixer.getSourceLineInfo(): end");
/* 137 */     return infos;
/*     */   }
/*     */ 
/*     */   public Line.Info[] getTargetLineInfo()
/*     */   {
/* 144 */     if (TDebug.TraceMixer) TDebug.out("TMixer.getTargetLineInfo(): begin");
/* 145 */     Line.Info[] infos = (Line.Info[])this.m_supportedTargetLineInfos.toArray(EMPTY_LINE_INFO_ARRAY);
/* 146 */     if (TDebug.TraceMixer) TDebug.out("TMixer.getTargetLineInfo(): end");
/* 147 */     return infos;
/*     */   }
/*     */ 
/*     */   public Line.Info[] getSourceLineInfo(Line.Info info)
/*     */   {
/* 154 */     if (TDebug.TraceMixer) TDebug.out("TMixer.getSourceLineInfo(Line.Info): info to test: " + info);
/*     */ 
/* 156 */     return EMPTY_LINE_INFO_ARRAY;
/*     */   }
/*     */ 
/*     */   public Line.Info[] getTargetLineInfo(Line.Info info)
/*     */   {
/* 163 */     if (TDebug.TraceMixer) TDebug.out("TMixer.getTargetLineInfo(Line.Info): info to test: " + info);
/*     */ 
/* 165 */     return EMPTY_LINE_INFO_ARRAY;
/*     */   }
/*     */ 
/*     */   public boolean isLineSupported(Line.Info info)
/*     */   {
/* 172 */     if (TDebug.TraceMixer) TDebug.out("TMixer.isLineSupported(): info to test: " + info);
/* 173 */     Class lineClass = info.getLineClass();
/* 174 */     if (lineClass.equals(SourceDataLine.class))
/*     */     {
/* 176 */       return isLineSupportedImpl(info, this.m_supportedSourceLineInfos);
/*     */     }
/* 178 */     if (lineClass.equals(TargetDataLine.class))
/*     */     {
/* 180 */       return isLineSupportedImpl(info, this.m_supportedTargetLineInfos);
/*     */     }
/* 182 */     if (lineClass.equals(Port.class))
/*     */     {
/* 184 */       return (isLineSupportedImpl(info, this.m_supportedSourceLineInfos)) || (isLineSupportedImpl(info, this.m_supportedTargetLineInfos));
/*     */     }
/*     */ 
/* 188 */     return false;
/*     */   }
/*     */ 
/*     */   private static boolean isLineSupportedImpl(Line.Info info, Collection supportedLineInfos)
/*     */   {
/* 196 */     Iterator iterator = supportedLineInfos.iterator();
/* 197 */     while (iterator.hasNext())
/*     */     {
/* 199 */       Line.Info info2 = (Line.Info)iterator.next();
/* 200 */       if (info2.matches(info))
/*     */       {
/* 202 */         return true;
/*     */       }
/*     */     }
/* 205 */     return false;
/*     */   }
/*     */ 
/*     */   public Line getLine(Line.Info info)
/*     */     throws LineUnavailableException
/*     */   {
/* 213 */     if (TDebug.TraceMixer) TDebug.out("TMixer.getLine(): begin");
/* 214 */     Class lineClass = info.getLineClass();
/* 215 */     DataLine.Info dataLineInfo = null;
/* 216 */     Port.Info portInfo = null;
/* 217 */     AudioFormat[] aFormats = null;
/* 218 */     if (info instanceof DataLine.Info)
/*     */     {
/* 220 */       dataLineInfo = (DataLine.Info)info;
/* 221 */       aFormats = dataLineInfo.getFormats();
/*     */     }
/* 223 */     else if (info instanceof Port.Info)
/*     */     {
/* 225 */       portInfo = (Port.Info)info;
/*     */     }
/* 227 */     AudioFormat format = null;
/* 228 */     Line line = null;
/* 229 */     if (lineClass == SourceDataLine.class)
/*     */     {
/* 231 */       if (TDebug.TraceMixer) TDebug.out("TMixer.getLine(): type: SourceDataLine");
/* 232 */       if (dataLineInfo == null)
/*     */       {
/* 234 */         throw new IllegalArgumentException("need DataLine.Info for SourceDataLine");
/*     */       }
/* 236 */       format = getSupportedSourceFormat(aFormats);
/* 237 */       line = getSourceDataLine(format, dataLineInfo.getMaxBufferSize());
/*     */     }
/* 239 */     else if (lineClass == Clip.class)
/*     */     {
/* 241 */       if (TDebug.TraceMixer) TDebug.out("TMixer.getLine(): type: Clip");
/* 242 */       if (dataLineInfo == null)
/*     */       {
/* 244 */         throw new IllegalArgumentException("need DataLine.Info for Clip");
/*     */       }
/* 246 */       format = getSupportedSourceFormat(aFormats);
/* 247 */       line = getClip(format);
/*     */     }
/* 249 */     else if (lineClass == TargetDataLine.class)
/*     */     {
/* 251 */       if (TDebug.TraceMixer) TDebug.out("TMixer.getLine(): type: TargetDataLine");
/* 252 */       if (dataLineInfo == null)
/*     */       {
/* 254 */         throw new IllegalArgumentException("need DataLine.Info for TargetDataLine");
/*     */       }
/* 256 */       format = getSupportedTargetFormat(aFormats);
/* 257 */       line = getTargetDataLine(format, dataLineInfo.getMaxBufferSize());
/*     */     }
/* 259 */     else if (lineClass == Port.class)
/*     */     {
/* 261 */       if (TDebug.TraceMixer) TDebug.out("TMixer.getLine(): type: TargetDataLine");
/* 262 */       if (portInfo == null)
/*     */       {
/* 264 */         throw new IllegalArgumentException("need Port.Info for Port");
/*     */       }
/* 266 */       line = getPort(portInfo);
/*     */     }
/*     */     else
/*     */     {
/* 270 */       if (TDebug.TraceMixer) TDebug.out("TMixer.getLine(): unknown line type, will throw exception");
/* 271 */       throw new LineUnavailableException("unknown line class: " + lineClass);
/*     */     }
/* 273 */     if (TDebug.TraceMixer) TDebug.out("TMixer.getLine(): end");
/* 274 */     return line;
/*     */   }
/*     */ 
/*     */   protected SourceDataLine getSourceDataLine(AudioFormat format, int nBufferSize)
/*     */     throws LineUnavailableException
/*     */   {
/* 282 */     if (TDebug.TraceMixer) TDebug.out("TMixer.getSourceDataLine(): begin");
/* 283 */     throw new IllegalArgumentException("this mixer does not support SourceDataLines");
/*     */   }
/*     */ 
/*     */   protected Clip getClip(AudioFormat format)
/*     */     throws LineUnavailableException
/*     */   {
/* 291 */     if (TDebug.TraceMixer) TDebug.out("TMixer.getClip(): begin");
/* 292 */     throw new IllegalArgumentException("this mixer does not support Clips");
/*     */   }
/*     */ 
/*     */   protected TargetDataLine getTargetDataLine(AudioFormat format, int nBufferSize)
/*     */     throws LineUnavailableException
/*     */   {
/* 300 */     if (TDebug.TraceMixer) TDebug.out("TMixer.getTargetDataLine(): begin");
/* 301 */     throw new IllegalArgumentException("this mixer does not support TargetDataLines");
/*     */   }
/*     */ 
/*     */   protected Port getPort(Port.Info info)
/*     */     throws LineUnavailableException
/*     */   {
/* 309 */     if (TDebug.TraceMixer) TDebug.out("TMixer.getTargetDataLine(): begin");
/* 310 */     throw new IllegalArgumentException("this mixer does not support Ports");
/*     */   }
/*     */ 
/*     */   private AudioFormat getSupportedSourceFormat(AudioFormat[] aFormats)
/*     */   {
/* 317 */     if (TDebug.TraceMixer) TDebug.out("TMixer.getSupportedSourceFormat(): begin");
/* 318 */     AudioFormat format = null;
/* 319 */     for (int i = 0; i < aFormats.length; ++i)
/*     */     {
/* 321 */       if (TDebug.TraceMixer) TDebug.out("TMixer.getSupportedSourceFormat(): checking " + aFormats[i] + "...");
/* 322 */       if (isSourceFormatSupported(aFormats[i]))
/*     */       {
/* 324 */         if (TDebug.TraceMixer) TDebug.out("TMixer.getSupportedSourceFormat(): ...supported");
/* 325 */         format = aFormats[i];
/* 326 */         break;
/*     */       }
/*     */ 
/* 330 */       if (!TDebug.TraceMixer)
/*     */         continue;
/* 332 */       TDebug.out("TMixer.getSupportedSourceFormat(): ...no luck");
/*     */     }
/*     */ 
/* 336 */     if (format == null)
/*     */     {
/* 338 */       throw new IllegalArgumentException("no line matchine one of the passed formats");
/*     */     }
/* 340 */     if (TDebug.TraceMixer) TDebug.out("TMixer.getSupportedSourceFormat(): end");
/* 341 */     return format;
/*     */   }
/*     */ 
/*     */   private AudioFormat getSupportedTargetFormat(AudioFormat[] aFormats)
/*     */   {
/* 348 */     if (TDebug.TraceMixer) TDebug.out("TMixer.getSupportedTargetFormat(): begin");
/* 349 */     AudioFormat format = null;
/* 350 */     for (int i = 0; i < aFormats.length; ++i)
/*     */     {
/* 352 */       if (TDebug.TraceMixer) TDebug.out("TMixer.getSupportedTargetFormat(): checking " + aFormats[i] + " ...");
/* 353 */       if (isTargetFormatSupported(aFormats[i]))
/*     */       {
/* 355 */         if (TDebug.TraceMixer) TDebug.out("TMixer.getSupportedTargetFormat(): ...supported");
/* 356 */         format = aFormats[i];
/* 357 */         break;
/*     */       }
/*     */ 
/* 361 */       if (!TDebug.TraceMixer)
/*     */         continue;
/* 363 */       TDebug.out("TMixer.getSupportedTargetFormat(): ...no luck");
/*     */     }
/*     */ 
/* 367 */     if (format == null)
/*     */     {
/* 369 */       throw new IllegalArgumentException("no line matchine one of the passed formats");
/*     */     }
/* 371 */     if (TDebug.TraceMixer) TDebug.out("TMixer.getSupportedTargetFormat(): end");
/* 372 */     return format;
/*     */   }
/*     */ 
/*     */   public Line[] getSourceLines()
/*     */   {
/* 386 */     if (TDebug.TraceMixer) TDebug.out("TMixer.getSourceLines(): called");
/* 387 */     return (Line[])this.m_openSourceDataLines.toArray(EMPTY_LINE_ARRAY);
/*     */   }
/*     */ 
/*     */   public Line[] getTargetLines()
/*     */   {
/* 394 */     if (TDebug.TraceMixer) TDebug.out("TMixer.getTargetLines(): called");
/* 395 */     return (Line[])this.m_openTargetDataLines.toArray(EMPTY_LINE_ARRAY);
/*     */   }
/*     */ 
/*     */   public void synchronize(Line[] aLines, boolean bMaintainSync)
/*     */   {
/* 403 */     throw new IllegalArgumentException("synchronization not supported");
/*     */   }
/*     */ 
/*     */   public void unsynchronize(Line[] aLines)
/*     */   {
/* 410 */     throw new IllegalArgumentException("synchronization not supported");
/*     */   }
/*     */ 
/*     */   public boolean isSynchronizationSupported(Line[] aLines, boolean bMaintainSync)
/*     */   {
/* 418 */     return false;
/*     */   }
/*     */ 
/*     */   protected boolean isSourceFormatSupported(AudioFormat format)
/*     */   {
/* 425 */     if (TDebug.TraceMixer) TDebug.out("TMixer.isSourceFormatSupported(): format to test: " + format);
/* 426 */     Iterator iterator = this.m_supportedSourceFormats.iterator();
/* 427 */     while (iterator.hasNext())
/*     */     {
/* 429 */       AudioFormat supportedFormat = (AudioFormat)iterator.next();
/* 430 */       if (AudioFormats.matches(supportedFormat, format))
/*     */       {
/* 432 */         return true;
/*     */       }
/*     */     }
/* 435 */     return false;
/*     */   }
/*     */ 
/*     */   protected boolean isTargetFormatSupported(AudioFormat format)
/*     */   {
/* 442 */     if (TDebug.TraceMixer) TDebug.out("TMixer.isTargetFormatSupported(): format to test: " + format);
/* 443 */     Iterator iterator = this.m_supportedTargetFormats.iterator();
/* 444 */     while (iterator.hasNext())
/*     */     {
/* 446 */       AudioFormat supportedFormat = (AudioFormat)iterator.next();
/* 447 */       if (AudioFormats.matches(supportedFormat, format))
/*     */       {
/* 449 */         return true;
/*     */       }
/*     */     }
/* 452 */     return false;
/*     */   }
/*     */ 
/*     */   void registerOpenLine(Line line)
/*     */   {
/* 459 */     if (TDebug.TraceMixer) TDebug.out("TMixer.registerOpenLine(): line to register: " + line);
/*     */ 
/* 461 */     if (line instanceof SourceDataLine)
/*     */     {
/* 463 */       synchronized (this.m_openSourceDataLines)
/*     */       {
/* 465 */         this.m_openSourceDataLines.add(line);
/*     */       }
/*     */     } else {
/* 468 */       if (!(line instanceof TargetDataLine))
/*     */         return;
/* 470 */       synchronized (this.m_openSourceDataLines)
/*     */       {
/* 472 */         this.m_openTargetDataLines.add(line);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   void unregisterOpenLine(Line line)
/*     */   {
/* 481 */     if (TDebug.TraceMixer) TDebug.out("TMixer.unregisterOpenLine(): line to unregister: " + line);
/* 482 */     if (line instanceof SourceDataLine)
/*     */     {
/* 484 */       synchronized (this.m_openSourceDataLines)
/*     */       {
/* 486 */         this.m_openSourceDataLines.remove(line);
/*     */       }
/*     */     } else {
/* 489 */       if (!(line instanceof TargetDataLine))
/*     */         return;
/* 491 */       synchronized (this.m_openTargetDataLines)
/*     */       {
/* 493 */         this.m_openTargetDataLines.remove(line);
/*     */       }
/*     */     }
/*     */   }
/*     */ }

