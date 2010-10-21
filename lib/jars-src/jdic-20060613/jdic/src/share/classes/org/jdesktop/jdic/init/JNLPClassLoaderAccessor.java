package org.jdesktop.jdic.init;

//import com.sun.jnlp.*;

public class JNLPClassLoaderAccessor {
          static java.lang.reflect.Method mdJNLPClassLoader_findLibrary = null;
          static{
              java.security.AccessController.doPrivileged( new java.security.PrivilegedAction() {
                  public Object run() {
                      try {
                          mdJNLPClassLoader_findLibrary = Class
                                  .forName("com.sun.jnlp.JNLPClassLoader")
                                  .getDeclaredMethod(
                                      "findLibrary",
                                      new Class[]{String.class} );
                          mdJNLPClassLoader_findLibrary.setAccessible(true);
                      } catch (Exception e) {
                          e.printStackTrace();
                      }
                      // to please javac
                      return null;
                  }
              });
          }

          public static String findLibrary(netx.jnlp.runtime.JNLPClassLoader o, String name) {
              try {
                  return (String)mdJNLPClassLoader_findLibrary.invoke(o, new Object[]{ name });
              } catch (Exception e){
                  e.printStackTrace();
              }
              return null;
            }
}
