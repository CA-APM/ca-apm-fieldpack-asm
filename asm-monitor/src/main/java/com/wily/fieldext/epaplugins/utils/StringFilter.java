/*    */ package com.wily.fieldext.epaplugins.utils;
/*    */ 
/*    */ public abstract interface StringFilter
/*    */ {
/*  9 */   public static final StringFilter VOID = new StringFilter() {
/*    */     public String filter(String input) {
/* 11 */       return input;
/*    */     }
/*  9 */   };
/*    */ 
/* 15 */   public static final StringFilter INVALID = new StringFilter() {
/*    */     public String filter(String input) {
/* 17 */       throw new Error("Must not be reached");
/*    */     }
/* 15 */   };
/*    */ 
/*    */   public abstract String filter(String paramString);
/*    */ }

/* Location:           \\vmware-host\Shared Folders\Documents\1APM-Wily\EPA\CloudMonitor\APMCMAgent\lib\APMCMPlugin.jar
 * Qualified Name:     com.wily.fieldext.epaplugins.utils.StringFilter
 * JD-Core Version:    0.6.0
 */