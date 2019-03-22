/**
 * UNCLASSIFIED
 * Northrop Grumman Proprietary
 * ____________________________
 *
 * Copyright (C) 2019, Northrop Grumman Systems Corporation
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of
 * Northrop Grumman Systems Corporation. The intellectual and technical concepts
 * contained herein are proprietary to Northrop Grumman Systems Corporation and
 * may be covered by U.S. and Foreign Patents or patents in process, and are
 * protected by trade secret or copyright law. Dissemination of this information
 * or reproduction of this material is strictly forbidden unless prior written
 * permission is obtained from Northrop Grumman.
 */
package com.ngc.seaside.systemdescriptor.service.impl.xtext.source;

import com.ngc.seaside.systemdescriptor.service.api.ParsingException;
import com.ngc.seaside.systemdescriptor.service.source.api.ISourceLocation;

import org.eclipse.emf.common.util.URI;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Objects;

public class SourceLocation implements ISourceLocation {

   private final Path path;
   private final int lineNumber;
   private final int column;
   private final int length;

   /**
    * Constructs a source location.
    * 
    * @param path path of location
    * @param lineNumber line number of location
    * @param column offset of location from start of line
    * @param length length of location
    */
   public SourceLocation(Path path, int lineNumber, int column, int length) {
      this.path = path;
      this.lineNumber = lineNumber;
      this.column = column;
      this.length = length;
   }

   /**
    * Constructs a source location.
    * 
    * @param uri uri of location
    * @param lineNumber line number of location
    * @param column offset of location from start of line
    * @param length length of location
    */
   public SourceLocation(URI uri, int lineNumber, int column, int length) {
      this.path = getPathFromUri(uri);
      this.lineNumber = lineNumber;
      this.column = column;
      this.length = length;
   }

   @Override
   public Path getPath() {
      return path;
   }

   @Override
   public int getLineNumber() {
      return lineNumber;
   }

   @Override
   public int getColumn() {
      return column;
   }

   @Override
   public int getLength() {
      return length;
   }

   @Override
   public boolean equals(Object o) {
      if (!(o instanceof SourceLocation)) {
         return false;
      }
      SourceLocation that = (SourceLocation) o;
      return Objects.equals(this.path, that.path) && this.lineNumber == that.lineNumber && this.column == that.column
               && this.length == that.length;
   }

   @Override
   public int hashCode() {
      return Objects.hash(path, lineNumber, column, length);
   }

   @Override
   public String toString() {
      return String.format("%s [line %s, col %s, len %s]",
               getPath(),
               getLineNumber(),
               getColumn(),
               getLength());
   }

   private static Path getPathFromUri(URI uri) {
      Path path = null;
      if (uri != null) {
         String fileString = uri.toFileString();
         if (fileString != null) {
            path = new File(fileString).toPath();
         } else {
            String devicePath = uri.devicePath();
            String filePath;
            int index = devicePath.toLowerCase().indexOf(".zip!");
            if (index >= 0) {
               index += 4;
               filePath = devicePath.substring(index + 1);
               devicePath = "jar:" + devicePath.substring(0, index);
            } else {
               throw new ParsingException("Unable to get path for " + uri);
            }
            java.net.URI i;
            try {
               i = new java.net.URI(devicePath);
            } catch (URISyntaxException e) {
               throw new ParsingException(e);
            }
            FileSystem fileSystem;
            try {
               fileSystem = FileSystems.getFileSystem(i);
            } catch (FileSystemNotFoundException e) {
               try {
                  fileSystem = FileSystems.newFileSystem(i, Collections.emptyMap());
               } catch (IOException e2) {
                  throw new ParsingException(e);
               }
            }
            path = fileSystem.getPath(filePath);
         }
      }
      return path;
   }

}