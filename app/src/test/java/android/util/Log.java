 package android.util;

 /**
  * Mock implementation of android.util.Log for unit tests.
  * This prevents RuntimeExceptions when Log methods are called in unit tests.
  */
 public class Log {
     public static int d(String tag, String msg) {
         System.out.println("DEBUG: " + tag + ": " + msg);
         return 0;
     }

     public static int i(String tag, String msg) {
         System.out.println("INFO: " + tag + ": " + msg);
         return 0;
     }

     public static int w(String tag, String msg) {
         System.out.println("WARN: " + tag + ": " + msg);
         return 0;
     }

     public static int e(String tag, String msg) {
         System.out.println("ERROR: " + tag + ": " + msg);
         return 0;
     }

     public static int v(String tag, String msg) {
         System.out.println("VERBOSE: " + tag + ": " + msg);
         return 0;
     }

     public static int wtf(String tag, String msg) {
         System.out.println("WTF: " + tag + ": " + msg);
         return 0;
     }
 }
