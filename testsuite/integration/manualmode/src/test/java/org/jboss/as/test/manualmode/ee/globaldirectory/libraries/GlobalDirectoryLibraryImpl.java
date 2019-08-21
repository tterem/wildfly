package org.jboss.as.test.manualmode.ee.globaldirectory.libraries;

import org.eclipse.microprofile.config.spi.ConfigSource;

/**
 * @author Tomas Terem (tterem@redhat.com)
 **/
public class GlobalDirectoryLibraryImpl implements GlobalDirectoryLibrary {

   private String s1 = "HELLO WORLD";

   public String get() {
      return String.valueOf(ConfigSource.DEFAULT_ORDINAL);
   }
}
