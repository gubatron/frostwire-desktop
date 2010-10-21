/*     */ package org.tritonus.share;
/*     */ 
/*     */ public class TCircularBuffer
/*     */ {
/*     */   private boolean m_bBlockingRead;
/*     */   private boolean m_bBlockingWrite;
/*     */   private byte[] m_abData;
/*     */   private int m_nSize;
/*     */   private long m_lReadPos;
/*     */   private long m_lWritePos;
/*     */   private Trigger m_trigger;
/*     */   private boolean m_bOpen;
/*     */ 
/*     */   public TCircularBuffer(int nSize, boolean bBlockingRead, boolean bBlockingWrite, Trigger trigger)
/*     */   {
/*  52 */     this.m_bBlockingRead = bBlockingRead;
/*  53 */     this.m_bBlockingWrite = bBlockingWrite;
/*  54 */     this.m_nSize = nSize;
/*  55 */     this.m_abData = new byte[this.m_nSize];
/*  56 */     this.m_lReadPos = 0L;
/*  57 */     this.m_lWritePos = 0L;
/*  58 */     this.m_trigger = trigger;
/*  59 */     this.m_bOpen = true;
/*     */   }
/*     */ 
/*     */   public void close()
/*     */   {
/*  66 */     this.m_bOpen = false;
/*     */   }
/*     */ 
/*     */   private boolean isOpen()
/*     */   {
/*  74 */     return this.m_bOpen;
/*     */   }
/*     */ 
/*     */   public int availableRead()
/*     */   {
/*  80 */     return (int)(this.m_lWritePos - this.m_lReadPos);
/*     */   }
/*     */ 
/*     */   public int availableWrite()
/*     */   {
/*  87 */     return this.m_nSize - availableRead();
/*     */   }
/*     */ 
/*     */   private int getReadPos()
/*     */   {
/*  94 */     return (int)(this.m_lReadPos % this.m_nSize);
/*     */   }
/*     */ 
/*     */   private int getWritePos()
/*     */   {
/* 101 */     return (int)(this.m_lWritePos % this.m_nSize);
/*     */   }
/*     */ 
/*     */   public int read(byte[] abData)
/*     */   {
/* 108 */     return read(abData, 0, abData.length);
/*     */   }
/*     */ 
/*     */   public int read(byte[] abData, int nOffset, int nLength)
/*     */   {
/* 115 */     if (TDebug.TraceCircularBuffer)
/*     */     {
/* 117 */       TDebug.out(">TCircularBuffer.read(): called.");
/* 118 */       dumpInternalState();
/*     */     }
/* 120 */     if (!isOpen())
/*     */     {
/* 122 */       if (availableRead() > 0)
/*     */       {
/* 124 */         nLength = Math.min(nLength, availableRead());
/* 125 */         if (TDebug.TraceCircularBuffer) TDebug.out("reading rest in closed buffer, length: " + nLength);
/*     */       }
/*     */       else
/*     */       {
/* 129 */         if (TDebug.TraceCircularBuffer) TDebug.out("< not open. returning -1.");
/* 130 */         return -1;
/*     */       }
/*     */     }
/* 133 */     synchronized (this)
/*     */     {
/* 135 */       if ((this.m_trigger != null) && (availableRead() < nLength))
/*     */       {
/* 137 */         if (TDebug.TraceCircularBuffer) TDebug.out("executing trigger.");
/* 138 */         this.m_trigger.execute();
/*     */       }
/* 140 */       if (!this.m_bBlockingRead)
/*     */       {
/* 142 */         nLength = Math.min(availableRead(), nLength);
/*     */       }
/* 144 */       int nRemainingBytes = nLength;
/* 145 */       while (nRemainingBytes > 0)
/*     */       {
/* 147 */         while (availableRead() == 0)
/*     */         {
/*     */           try
/*     */           {
/* 151 */             super.wait();
/*     */           }
/*     */           catch (InterruptedException e)
/*     */           {
/* 155 */             if (TDebug.TraceAllExceptions)
/*     */             {
/* 157 */               TDebug.out(e);
/*     */             }
/*     */           }
/*     */         }
/* 161 */         int nAvailable = Math.min(availableRead(), nRemainingBytes);
/* 162 */         while (nAvailable > 0)
/*     */         {
/* 164 */           int nToRead = Math.min(nAvailable, this.m_nSize - getReadPos());
/* 165 */           System.arraycopy(this.m_abData, getReadPos(), abData, nOffset, nToRead);
/* 166 */           this.m_lReadPos += nToRead;
/* 167 */           nOffset += nToRead;
/* 168 */           nAvailable -= nToRead;
/* 169 */           nRemainingBytes -= nToRead;
/*     */         }
/* 171 */         super.notifyAll();
/*     */       }
/* 173 */       if (TDebug.TraceCircularBuffer)
/*     */       {
/* 175 */         TDebug.out("After read:");
/* 176 */         dumpInternalState();
/* 177 */         TDebug.out("< completed. Read " + nLength + " bytes");
/*     */       }
/* 179 */       return nLength;
/*     */     }
/*     */   }
/*     */ 
/*     */   public int write(byte[] abData)
/*     */   {
/* 186 */     return write(abData, 0, abData.length);
/*     */   }
/*     */ 
/*     */   public int write(byte[] abData, int nOffset, int nLength)
/*     */   {
/* 193 */     if (TDebug.TraceCircularBuffer)
/*     */     {
/* 195 */       TDebug.out(">TCircularBuffer.write(): called; nLength: " + nLength);
/* 196 */       dumpInternalState();
/*     */     }
/* 198 */     synchronized (this)
/*     */     {
/* 200 */       if (TDebug.TraceCircularBuffer) TDebug.out("entered synchronized block.");
/* 201 */       if (!this.m_bBlockingWrite)
/*     */       {
/* 203 */         nLength = Math.min(availableWrite(), nLength);
/*     */       }
/* 205 */       int nRemainingBytes = nLength;
/* 206 */       while (nRemainingBytes > 0)
/*     */       {
/* 208 */         while (availableWrite() == 0)
/*     */         {
/*     */           try
/*     */           {
/* 212 */             super.wait();
/*     */           }
/*     */           catch (InterruptedException e)
/*     */           {
/* 216 */             if (TDebug.TraceAllExceptions)
/*     */             {
/* 218 */               TDebug.out(e);
/*     */             }
/*     */           }
/*     */         }
/* 222 */         int nAvailable = Math.min(availableWrite(), nRemainingBytes);
/* 223 */         while (nAvailable > 0)
/*     */         {
/* 225 */           int nToWrite = Math.min(nAvailable, this.m_nSize - getWritePos());
/*     */ 
/* 227 */           System.arraycopy(abData, nOffset, this.m_abData, getWritePos(), nToWrite);
/* 228 */           this.m_lWritePos += nToWrite;
/* 229 */           nOffset += nToWrite;
/* 230 */           nAvailable -= nToWrite;
/* 231 */           nRemainingBytes -= nToWrite;
/*     */         }
/* 233 */         super.notifyAll();
/*     */       }
/* 235 */       if (TDebug.TraceCircularBuffer)
/*     */       {
/* 237 */         TDebug.out("After write:");
/* 238 */         dumpInternalState();
/* 239 */         TDebug.out("< completed. Wrote " + nLength + " bytes");
/*     */       }
/* 241 */       return nLength;
/*     */     }
/*     */   }
/*     */ 
/*     */   private void dumpInternalState()
/*     */   {
/* 249 */     TDebug.out("m_lReadPos  = " + this.m_lReadPos + " ^= " + getReadPos());
/* 250 */     TDebug.out("m_lWritePos = " + this.m_lWritePos + " ^= " + getWritePos());
/* 251 */     TDebug.out("availableRead()  = " + availableRead());
/* 252 */     TDebug.out("availableWrite() = " + availableWrite());
/*     */   }
/*     */ 
/*     */   public static abstract interface Trigger
/*     */   {
/*     */     public abstract void execute();
/*     */   }
/*     */ }

