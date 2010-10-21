/*     */ package org.tritonus.share.sampled.mixer;
/*     */ 
/*     */ import java.util.ArrayList;
/*     */ import java.util.HashMap;
/*     */ import java.util.HashSet;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Set;
/*     */ import javax.sound.sampled.Mixer;
/*     */ import javax.sound.sampled.spi.MixerProvider;
/*     */ import org.tritonus.share.TDebug;
/*     */ 
/*     */ public abstract class TMixerProvider extends MixerProvider
/*     */ {
/*  45 */   private static final Mixer.Info[] EMPTY_MIXER_INFO_ARRAY = new Mixer.Info[0];
/*     */ 
/*  47 */   private static Map sm_mixerProviderStructs = new HashMap();
/*     */ 
/*  49 */   private boolean m_bDisabled = false;
/*     */ 
/*     */   public TMixerProvider()
/*     */   {
/*  56 */     if (TDebug.TraceMixerProvider) TDebug.out("TMixerProvider.<init>(): begin");
/*     */ 
/*  58 */     if (!TDebug.TraceMixerProvider) return; TDebug.out("TMixerProvider.<init>(): end");
/*     */   }
/*     */ 
/*     */   protected void staticInit()
/*     */   {
/*     */   }
/*     */ 
/*     */   private MixerProviderStruct getMixerProviderStruct()
/*     */   {
/*  74 */     if (TDebug.TraceMixerProvider) TDebug.out("TMixerProvider.getMixerProviderStruct(): begin");
/*  75 */     Class cls = super.getClass();
/*  76 */     if (TDebug.TraceMixerProvider) TDebug.out("TMixerProvider.getMixerProviderStruct(): called from " + cls);
/*     */ 
/*  78 */     synchronized (TMixerProvider.class)
/*     */     {
/*  80 */       MixerProviderStruct struct = (MixerProviderStruct)sm_mixerProviderStructs.get(cls);
/*  81 */       if (struct == null)
/*     */       {
/*  83 */         if (TDebug.TraceMixerProvider) TDebug.out("TMixerProvider.getMixerProviderStruct(): creating new MixerProviderStruct for " + cls);
/*  84 */         struct = new MixerProviderStruct();
/*  85 */         sm_mixerProviderStructs.put(cls, struct);
/*     */       }
/*  87 */       if (TDebug.TraceMixerProvider) TDebug.out("TMixerProvider.getMixerProviderStruct(): end");
/*  88 */       return struct;
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void disable()
/*     */   {
/*  96 */     if (TDebug.TraceMixerProvider) TDebug.out("disabling " + super.getClass().getName());
/*  97 */     this.m_bDisabled = true;
/*     */   }
/*     */ 
/*     */   protected boolean isDisabled()
/*     */   {
/* 103 */     return this.m_bDisabled;
/*     */   }
/*     */ 
/*     */   protected void addMixer(Mixer mixer)
/*     */   {
/* 110 */     if (TDebug.TraceMixerProvider) TDebug.out("TMixerProvider.addMixer(): begin");
/* 111 */     MixerProviderStruct struct = getMixerProviderStruct();
/* 112 */     synchronized (struct)
/*     */     {
/* 114 */       struct.m_mixers.add(mixer);
/* 115 */       if (struct.m_defaultMixer == null)
/*     */       {
/* 117 */         struct.m_defaultMixer = mixer;
/*     */       }
/*     */     }
/* 120 */     if (!TDebug.TraceMixerProvider) return; TDebug.out("TMixerProvider.addMixer(): end");
/*     */   }
/*     */ 
/*     */   protected void removeMixer(Mixer mixer)
/*     */   {
/* 127 */     if (TDebug.TraceMixerProvider) TDebug.out("TMixerProvider.removeMixer(): begin");
/* 128 */     MixerProviderStruct struct = getMixerProviderStruct();
/* 129 */     synchronized (struct)
/*     */     {
/* 131 */       struct.m_mixers.remove(mixer);
/*     */ 
/* 133 */       if (struct.m_defaultMixer == mixer)
/*     */       {
/* 135 */         struct.m_defaultMixer = null;
/*     */       }
/*     */     }
/* 138 */     if (!TDebug.TraceMixerProvider) return; TDebug.out("TMixerProvider.removeMixer(): end");
/*     */   }
/*     */ 
/*     */   public boolean isMixerSupported(Mixer.Info info)
/*     */   {
/* 145 */     if (TDebug.TraceMixerProvider) TDebug.out("TMixerProvider.isMixerSupported(): begin");
/* 146 */     boolean bIsSupported = false;
/* 147 */     Mixer.Info[] infos = getMixerInfo();
/* 148 */     for (int i = 0; i < infos.length; ++i)
/*     */     {
/* 150 */       if (!infos[i].equals(info))
/*     */         continue;
/* 152 */       bIsSupported = true;
/* 153 */       break;
/*     */     }
/*     */ 
/* 156 */     if (TDebug.TraceMixerProvider) TDebug.out("TMixerProvider.isMixerSupported(): end");
/* 157 */     return bIsSupported;
/*     */   }
/*     */ 
/*     */   public Mixer getMixer(Mixer.Info info)
/*     */   {
/* 166 */     if (TDebug.TraceMixerProvider) TDebug.out("TMixerProvider.getMixer(): begin");
/* 167 */     MixerProviderStruct struct = getMixerProviderStruct();
/* 168 */     Mixer mixerResult = null;
/* 169 */     synchronized (struct)
/*     */     {
/* 171 */       if (info == null)
/*     */       {
/* 173 */         mixerResult = struct.m_defaultMixer;
/*     */       }
/*     */       else
/*     */       {
/* 177 */         Iterator mixers = struct.m_mixers.iterator();
/* 178 */         while (mixers.hasNext())
/*     */         {
/* 180 */           Mixer mixer = (Mixer)mixers.next();
/* 181 */           if (mixer.getMixerInfo().equals(info))
/*     */           {
/* 183 */             mixerResult = mixer;
/* 184 */             break;
/*     */           }
/*     */         }
/*     */       }
/*     */     }
/* 189 */     if (mixerResult == null)
/*     */     {
/* 191 */       throw new IllegalArgumentException("no mixer available for " + info);
/*     */     }
/* 193 */     if (TDebug.TraceMixerProvider) TDebug.out("TMixerProvider.getMixer(): end");
/* 194 */     return mixerResult;
/*     */   }
/*     */ 
/*     */   public Mixer.Info[] getMixerInfo()
/*     */   {
/* 201 */     if (TDebug.TraceMixerProvider) TDebug.out("TMixerProvider.getMixerInfo(): begin");
/* 202 */     Set mixerInfos = new HashSet();
/* 203 */     MixerProviderStruct struct = getMixerProviderStruct();
/* 204 */     synchronized (struct)
/*     */     {
/* 206 */       Iterator mixers = struct.m_mixers.iterator();
/* 207 */       while (mixers.hasNext())
/*     */       {
/* 209 */         Mixer mixer = (Mixer)mixers.next();
/* 210 */         mixerInfos.add(mixer.getMixerInfo());
/*     */       }
/*     */     }
/* 213 */     if (TDebug.TraceMixerProvider) TDebug.out("TMixerProvider.getMixerInfo(): end");
/* 214 */     return (Mixer.Info[])mixerInfos.toArray(EMPTY_MIXER_INFO_ARRAY);
/*     */   }
/*     */ 
/*     */   private class MixerProviderStruct
/*     */   {
/*     */     public List m_mixers;
/*     */     public Mixer m_defaultMixer;
/*     */ 
/*     */     public MixerProviderStruct()
/*     */     {
/* 228 */       this.m_mixers = new ArrayList();
/* 229 */       this.m_defaultMixer = null;
/*     */     }
/*     */   }
/*     */ }

