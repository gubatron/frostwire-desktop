/*     */ package org.tritonus.share;
/*     */ 
/*     */ import java.util.ArrayList;
/*     */ import java.util.Collection;
/*     */ import java.util.EventObject;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import javax.sound.sampled.LineEvent;
/*     */ import javax.sound.sampled.LineListener;
/*     */ 
/*     */ public class TNotifier extends Thread
/*     */ {
/*  74 */   public static TNotifier notifier = null;
/*     */   private List m_entries;
/*     */ 
/*     */   public TNotifier()
/*     */   {
/*  93 */     super("Tritonus Notifier");
/*  94 */     this.m_entries = new ArrayList();
/*     */   }
/*     */ 
/*     */   public void addEntry(EventObject event, Collection listeners)
/*     */   {
/* 102 */     synchronized (this.m_entries)
/*     */     {
/* 104 */       this.m_entries.add(new NotifyEntry(event, listeners));
/* 105 */       this.m_entries.notifyAll();
/*     */     }
/*     */   }
/*     */ 
/*     */   public void run()
/*     */   {
/*     */     while (true)
/*     */     {
/* 115 */       NotifyEntry entry = null;
/* 116 */       synchronized (this.m_entries)
/*     */       {
/* 118 */         while (this.m_entries.size() == 0)
/*     */         {
/*     */           try
/*     */           {
/* 122 */             this.m_entries.wait();
/*     */           }
/*     */           catch (InterruptedException e)
/*     */           {
/* 126 */             if (TDebug.TraceAllExceptions)
/*     */             {
/* 128 */               TDebug.out(e);
/*     */             }
/*     */           }
/*     */         }
/* 132 */         entry = (NotifyEntry)this.m_entries.remove(0);
/*     */       }
/* 134 */       entry.deliver();
/*     */     }
/*     */   }
/*     */ 
/*     */   static
/*     */   {
/*  78 */     notifier = new TNotifier();
/*  79 */     notifier.setDaemon(true);
/*  80 */     notifier.start();
/*     */   }
/*     */ 
/*     */   public static class NotifyEntry
/*     */   {
/*     */     private EventObject m_event;
/*     */     private List m_listeners;
/*     */ 
/*     */     public NotifyEntry(EventObject event, Collection listeners)
/*     */     {
/*  49 */       this.m_event = event;
/*  50 */       this.m_listeners = new ArrayList(listeners);
/*     */     }
/*     */ 
/*     */     public void deliver()
/*     */     {
/*  57 */       Iterator iterator = this.m_listeners.iterator();
/*  58 */       while (iterator.hasNext())
/*     */       {
/*  60 */         Object listener = iterator.next();
/*  61 */         if (listener instanceof LineListener)
/*     */         {
/*  63 */           ((LineListener)listener).update((LineEvent)this.m_event);
/*     */         }
/*     */       }
/*     */     }
/*     */   }
/*     */ }
