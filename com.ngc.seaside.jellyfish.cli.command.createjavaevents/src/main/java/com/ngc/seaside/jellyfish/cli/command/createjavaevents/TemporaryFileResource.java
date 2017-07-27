package com.ngc.seaside.jellyfish.cli.command.createjavaevents;

import com.google.common.base.Preconditions;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class TemporaryFileResource implements ITemporaryFileResource {

   private final URL url;
   private final String fileName;
   private Path temporaryFile;

   public TemporaryFileResource(URL url, String temporaryFileName) {
      Preconditions.checkNotNull(url, "url may not be null!");
      Preconditions.checkNotNull(temporaryFileName, "temporaryFileName may not be null!");
      Preconditions.checkArgument(!temporaryFileName.trim().isEmpty(), "temporaryFileName may not be empty!");
      this.url = url;
      this.fileName = temporaryFileName;
   }

   @Override
   public Path getTemporaryFile() {
      Preconditions.checkState(temporaryFile != null,
                               "temporaryFile not yet created, read(..) not invoked!");
      return temporaryFile;
   }

   @Override
   public URL getURL() {
      return url;
   }

   @Override
   public boolean read(InputStream stream) {
      boolean success = true;
      try {
         Path tempDirectory = Files.createTempDirectory("blocs");
         tempDirectory.toFile().deleteOnExit();
         temporaryFile = tempDirectory.resolve(fileName);
         Files.copy(stream, temporaryFile);
      } catch (IOException e) {
         success = false;
      }
      return success;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      }
      if (!(o instanceof TemporaryFileResource)) {
         return false;
      }
      TemporaryFileResource that = (TemporaryFileResource) o;
      return Objects.equals(url, that.url) &&
             Objects.equals(temporaryFile, that.temporaryFile);
   }

   @Override
   public int hashCode() {
      return Objects.hash(url, temporaryFile);
   }

   public static ITemporaryFileResource forJarResource(Class<?> clazz,
                                                       String resourceName) {
      Preconditions.checkNotNull(clazz, "clazz may not be null!");
      Preconditions.checkNotNull(resourceName, "resourceName may not be null!");
      Preconditions.checkArgument(!resourceName.trim().isEmpty(), "resourceName may not be empty!");
      URL url = clazz.getClassLoader().getResource(resourceName);
      Preconditions.checkArgument(url != null, "%s could not be loaded by class loader %s!",
                                  resourceName,
                                  clazz.getClassLoader());
      return new TemporaryFileResource(url, resourceName);
   }
}
