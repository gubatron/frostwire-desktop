/*    */ package org.tritonus.share;
/*    */ 
/*    */ import java.security.AccessControlException;
/*    */ 
/*    */ public class TSettings
/*    */ {
/* 38 */   public static boolean SHOW_ACCESS_CONTROL_EXCEPTIONS = false;
/*    */   private static final String PROPERTY_PREFIX = "tritonus.";
/* 42 */   public static boolean AlsaUsePlughw = getBooleanProperty("AlsaUsePlughw");
/*    */ 
/*    */   private static boolean getBooleanProperty(String strName)
/*    */   {
/* 48 */     String strPropertyName = "tritonus." + strName;
/* 49 */     String strValue = "false";
/*    */     try
/*    */     {
/* 52 */       strValue = System.getProperty(strPropertyName, "false");
/*    */     }
/*    */     catch (AccessControlException e)
/*    */     {
/* 56 */       if (SHOW_ACCESS_CONTROL_EXCEPTIONS)
/*    */       {
/* 58 */         TDebug.out(e);
/*    */       }
/*    */     }
/*    */ 
/* 62 */     boolean bValue = strValue.toLowerCase().equals("true");
/*    */ 
/* 64 */     return bValue;
/*    */   }
/*    */ }
