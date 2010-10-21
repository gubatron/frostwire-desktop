/*     */ package org.tritonus.share.midi;
/*     */ 
/*     */ import java.io.ByteArrayOutputStream;
/*     */ import java.io.IOException;
/*     */ import java.io.OutputStream;
/*     */ import org.tritonus.share.TDebug;
/*     */ 
/*     */ public class MidiUtils
/*     */ {
/*     */   public static int getUnsignedInteger(byte b)
/*     */   {
/*  43 */     return (b < 0) ? b + 256 : b;
/*     */   }
/*     */ 
/*     */   public static int get14bitValue(int nLSB, int nMSB)
/*     */   {
/*  50 */     return nLSB & 0x7F | (nMSB & 0x7F) << 7;
/*     */   }
/*     */ 
/*     */   public static int get14bitMSB(int nValue)
/*     */   {
/*  57 */     return nValue >> 7 & 0x7F;
/*     */   }
/*     */ 
/*     */   public static int get14bitLSB(int nValue)
/*     */   {
/*  64 */     return nValue & 0x7F;
/*     */   }
/*     */ 
/*     */   public static byte[] getVariableLengthQuantity(long lValue)
/*     */   {
/*  71 */     ByteArrayOutputStream data = new ByteArrayOutputStream();
/*     */     try
/*     */     {
/*  74 */       writeVariableLengthQuantity(lValue, data);
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/*  78 */       if (TDebug.TraceAllExceptions) TDebug.out(e);
/*     */     }
/*  80 */     return data.toByteArray();
/*     */   }
/*     */ 
/*     */   public static int writeVariableLengthQuantity(long lValue, OutputStream outputStream)
/*     */     throws IOException
/*     */   {
/*  88 */     int nLength = 0;
/*     */ 
/*  90 */     boolean bWritingStarted = false;
/*  91 */     int nByte = (int)(lValue >> 21 & 0x7F);
/*  92 */     if (nByte != 0)
/*     */     {
/*  94 */       if (outputStream != null)
/*     */       {
/*  96 */         outputStream.write(nByte | 0x80);
/*     */       }
/*  98 */       ++nLength;
/*  99 */       bWritingStarted = true;
/*     */     }
/* 101 */     nByte = (int)(lValue >> 14 & 0x7F);
/* 102 */     if ((nByte != 0) || (bWritingStarted))
/*     */     {
/* 104 */       if (outputStream != null)
/*     */       {
/* 106 */         outputStream.write(nByte | 0x80);
/*     */       }
/* 108 */       ++nLength;
/* 109 */       bWritingStarted = true;
/*     */     }
/* 111 */     nByte = (int)(lValue >> 7 & 0x7F);
/* 112 */     if ((nByte != 0) || (bWritingStarted))
/*     */     {
/* 114 */       if (outputStream != null)
/*     */       {
/* 116 */         outputStream.write(nByte | 0x80);
/*     */       }
/* 118 */       ++nLength;
/*     */     }
/* 120 */     nByte = (int)(lValue & 0x7F);
/* 121 */     if (outputStream != null)
/*     */     {
/* 123 */       outputStream.write(nByte);
/*     */     }
/* 125 */     ++nLength;
/* 126 */     return nLength;
/*     */   }
/*     */ }
