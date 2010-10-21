/*      */ package org.tritonus.share.sampled.mixer;
/*      */ 
/*      */ import java.util.ArrayList;
/*      */ import java.util.Collection;
/*      */ import java.util.HashSet;
/*      */ import java.util.Iterator;
/*      */ import java.util.List;
/*      */ import java.util.Set;
/*      */ import javax.sound.sampled.Control;
/*      */ import javax.sound.sampled.Line;
/*      */ import javax.sound.sampled.LineEvent;
/*      */ import javax.sound.sampled.LineListener;
/*      */ import javax.sound.sampled.LineUnavailableException;
/*      */ import org.tritonus.share.TDebug;
/*      */ import org.tritonus.share.TNotifier;
/*      */ 
/*      */ public abstract class TLine
/*      */   implements Line
/*      */ {
/*   57 */   private static final Control[] EMPTY_CONTROL_ARRAY = new Control[0];
/*      */   private Line.Info m_info;
/*      */   private boolean m_bOpen;
/*      */   private List m_controls;
/*      */   private Set m_lineListeners;
/*      */   private TMixer m_mixer;
/*      */ 
/*      */   protected TLine(TMixer mixer, Line.Info info)
/*      */   {
/*   70 */     setLineInfo(info);
/*   71 */     setOpen(false);
/*   72 */     this.m_controls = new ArrayList();
/*   73 */     this.m_lineListeners = new HashSet();
/*   74 */     this.m_mixer = mixer;
/*      */   }
/*      */ 
/*      */   protected TLine(TMixer mixer, Line.Info info, Collection controls)
/*      */   {
/*   83 */     this(mixer, info);
/*   84 */     this.m_controls.addAll(controls);
/*      */   }
/*      */ 
/*      */   protected TMixer getMixer()
/*      */   {
/*   90 */     return this.m_mixer;
/*      */   }
/*      */ 
/*      */   public Line.Info getLineInfo()
/*      */   {
/*   96 */     return this.m_info;
/*      */   }
/*      */ 
/*      */   protected void setLineInfo(Line.Info info)
/*      */   {
/*  103 */     if (TDebug.TraceLine)
/*      */     {
/*  105 */       TDebug.out("TLine.setLineInfo(): setting: " + info);
/*      */     }
/*  107 */     synchronized (this)
/*      */     {
/*  109 */       this.m_info = info;
/*      */     }
/*      */   }
/*      */ 
/*      */   public void open()
/*      */     throws LineUnavailableException
/*      */   {
/*  118 */     if (TDebug.TraceLine)
/*      */     {
/*  120 */       TDebug.out("TLine.open(): called");
/*      */     }
/*  122 */     if (!isOpen())
/*      */     {
/*  124 */       if (TDebug.TraceLine)
/*      */       {
/*  126 */         TDebug.out("TLine.open(): opening");
/*      */       }
/*  128 */       openImpl();
/*  129 */       if (getMixer() != null)
/*      */       {
/*  131 */         getMixer().registerOpenLine(this);
/*      */       }
/*  133 */       setOpen(true);
/*      */     }
/*      */     else
/*      */     {
/*  137 */       if (!TDebug.TraceLine)
/*      */         return;
/*  139 */       TDebug.out("TLine.open(): already open");
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void openImpl()
/*      */     throws LineUnavailableException
/*      */   {
/*  152 */     if (!TDebug.TraceLine)
/*      */       return;
/*  154 */     TDebug.out("TLine.openImpl(): called");
/*      */   }
/*      */ 
/*      */   public void close()
/*      */   {
/*  162 */     if (TDebug.TraceLine)
/*      */     {
/*  164 */       TDebug.out("TLine.close(): called");
/*      */     }
/*  166 */     if (isOpen())
/*      */     {
/*  168 */       if (TDebug.TraceLine)
/*      */       {
/*  170 */         TDebug.out("TLine.close(): closing");
/*      */       }
/*  172 */       if (getMixer() != null)
/*      */       {
/*  174 */         getMixer().unregisterOpenLine(this);
/*      */       }
/*  176 */       closeImpl();
/*  177 */       setOpen(false);
/*      */     }
/*      */     else
/*      */     {
/*  181 */       if (!TDebug.TraceLine)
/*      */         return;
/*  183 */       TDebug.out("TLine.close(): not open");
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void closeImpl()
/*      */   {
/*  195 */     if (!TDebug.TraceLine)
/*      */       return;
/*  197 */     TDebug.out("TLine.closeImpl(): called");
/*      */   }
/*      */ 
/*      */   public boolean isOpen()
/*      */   {
/*  207 */     return this.m_bOpen;
/*      */   }
/*      */ 
/*      */   protected void setOpen(boolean bOpen)
/*      */   {
/*  215 */     if (TDebug.TraceLine)
/*      */     {
/*  217 */       TDebug.out("TLine.setOpen(): called, value: " + bOpen);
/*      */     }
/*  219 */     boolean bOldValue = isOpen();
/*  220 */     this.m_bOpen = bOpen;
/*  221 */     if (bOldValue == isOpen())
/*      */       return;
/*  223 */     if (isOpen())
/*      */     {
/*  225 */       if (TDebug.TraceLine)
/*      */       {
/*  227 */         TDebug.out("TLine.setOpen(): opened");
/*      */       }
/*  229 */       notifyLineEvent(LineEvent.Type.OPEN);
/*      */     }
/*      */     else
/*      */     {
/*  233 */       if (TDebug.TraceLine)
/*      */       {
/*  235 */         TDebug.out("TLine.setOpen(): closed");
/*      */       }
/*  237 */       notifyLineEvent(LineEvent.Type.CLOSE);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void addControl(Control control)
/*      */   {
/*  246 */     synchronized (this.m_controls)
/*      */     {
/*  248 */       this.m_controls.add(control);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void removeControl(Control control)
/*      */   {
/*  256 */     synchronized (this.m_controls)
/*      */     {
/*  258 */       this.m_controls.remove(control);
/*      */     }
/*      */   }
/*      */ 
/*      */   public Control[] getControls()
/*      */   {
/*  266 */     synchronized (this.m_controls)
/*      */     {
/*  268 */       return (Control[])this.m_controls.toArray(EMPTY_CONTROL_ARRAY);
/*      */     }
/*      */   }
/*      */ 
/*      */   public Control getControl(Control.Type controlType)
/*      */   {
/*  276 */     synchronized (this.m_controls)
/*      */     {
/*  278 */       Iterator it = this.m_controls.iterator();
/*  279 */       while (it.hasNext())
/*      */       {
/*  281 */         Control control = (Control)it.next();
/*  282 */         if (control.getType().equals(controlType))
/*      */         {
/*  284 */           return control;
/*      */         }
/*      */       }
/*  287 */       throw new IllegalArgumentException("no control of type " + controlType);
/*      */     }
/*      */   }
/*      */ 
/*      */   public boolean isControlSupported(Control.Type controlType)
/*      */   {
/*      */     try
/*      */     {
/*  298 */       return getControl(controlType) != null;
/*      */     }
/*      */     catch (IllegalArgumentException e)
/*      */     {
/*  302 */       if (TDebug.TraceAllExceptions)
/*      */       {
/*  304 */         TDebug.out(e);
/*      */       }
/*      */     }
/*  307 */     return false;
/*      */   }
/*      */ 
/*      */   public void addLineListener(LineListener listener)
/*      */   {
/*  316 */     synchronized (this.m_lineListeners)
/*      */     {
/*  318 */       this.m_lineListeners.add(listener);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void removeLineListener(LineListener listener)
/*      */   {
/*  326 */     synchronized (this.m_lineListeners)
/*      */     {
/*  328 */       this.m_lineListeners.remove(listener);
/*      */     }
/*      */   }
/*      */ 
/*      */   private Set getLineListeners()
/*      */   {
/*  336 */     synchronized (this.m_lineListeners)
/*      */     {
/*  338 */       return new HashSet(this.m_lineListeners);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void notifyLineEvent(LineEvent.Type type)
/*      */   {
/*  346 */     notifyLineEvent(new LineEvent(this, type, -1L));
/*      */   }
/*      */ 
/*      */   protected void notifyLineEvent(LineEvent event)
/*      */   {
/*  355 */     TNotifier.notifier.addEntry(event, getLineListeners());
/*      */   }
/*      */ }

