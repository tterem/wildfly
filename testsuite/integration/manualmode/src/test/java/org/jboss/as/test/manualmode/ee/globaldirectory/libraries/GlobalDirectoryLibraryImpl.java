package org.jboss.as.test.manualmode.ee.globaldirectory.libraries;

/**
 * @author Tomas Terem (tterem@redhat.com)
 **/
public class GlobalDirectoryLibraryImpl implements GlobalDirectoryLibrary {

   private String s1 = "HELLO WORLD";
   private String s2 = "RED HAT";

   public String get() {
      return s1;
   }
}
