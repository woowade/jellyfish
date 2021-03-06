/**
 * UNCLASSIFIED
 *
 * Copyright 2020 Northrop Grumman Systems Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
 * Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.ngc.seaside.jellyfish.utilities.parsing;

import com.ngc.seaside.systemdescriptor.service.api.IParsingIssue;
import com.ngc.seaside.systemdescriptor.service.api.IParsingResult;
import com.ngc.seaside.systemdescriptor.service.gherkin.api.IGherkinParsingResult;
import com.ngc.seaside.systemdescriptor.service.source.api.ISourceLocation;
import com.ngc.seaside.systemdescriptor.validation.api.Severity;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Aids in logging the results of parsing a System Descriptor project.
 */
public class ParsingResultLogging {
   
   private static final int LINES_TO_PRINT_BEFORE_ERROR = 3;
   private static final int LINES_TO_PRINT_AFTER_ERROR = 2;

   private ParsingResultLogging() {}

   /**
    * Gets a collection of strings that should be printed one per line that describes the parsing errors in the given
    * result.
    */
   public static Collection<String> logErrors(IParsingResult result) {
      Collection<String> lines = new ArrayList<>();

      Collection<IParsingIssue> errors = result.getIssues()
                                               .stream()
                                               .filter(i -> i.getSeverity() == Severity.ERROR)
                                               .collect(Collectors.toList());
      if (errors.size() > 0) {
         lines.add(String.format("System Descriptor project contains %d errors.", errors.size()));
      }
      for (IParsingIssue error : errors) {
         lines.addAll(printIssue(error));
      }

      return lines;
   }

   /**
    * Gets a collection of strings that should be printed one per line that describes the parsing errors in the given
    * result.
    */
   public static Collection<String> logErrors(IGherkinParsingResult result) {
      Collection<String> lines = new ArrayList<>();

      Collection<IParsingIssue> errors = result.getIssues()
                                               .stream()
                                               .filter(i -> i.getSeverity() == Severity.ERROR)
                                               .collect(Collectors.toList());
      if (errors.size() > 0) {
         lines.add(String.format("Feature files contain %d errors.", errors.size()));
      }
      for (IParsingIssue error : errors) {
         lines.addAll(printIssue(error));
      }

      return lines;
   }

   /**
    * Gets a collection of strings that should be printed one per line that describes the parsing warnings in the given
    * result.
    */
   public static Collection<String> logWarnings(IParsingResult result) {
      Collection<String> lines = new ArrayList<>();

      Collection<IParsingIssue> warnings = result.getIssues()
                                                 .stream()
                                                 .filter(i -> i.getSeverity() == Severity.WARNING)
                                                 .collect(Collectors.toList());
      if (warnings.size() > 0) {
         lines.add(String.format("System Descriptor project contains %d warnings.", warnings.size()));
         for (IParsingIssue warning : warnings) {
            lines.addAll(printIssue(warning));
         }
      }

      return lines;
   }

   /**
    * Gets a collection of strings that should be printed one per line that describes the parsing warnings in the given
    * result.
    */
   public static Collection<String> logWarnings(IGherkinParsingResult result) {
      Collection<String> lines = new ArrayList<>();

      Collection<IParsingIssue> warnings = result.getIssues()
                                                 .stream()
                                                 .filter(i -> i.getSeverity() == Severity.WARNING)
                                                 .collect(Collectors.toList());
      if (warnings.size() > 0) {
         lines.add(String.format("Feature files contain %d warnings.", warnings.size()));
         for (IParsingIssue warning : warnings) {
            lines.addAll(printIssue(warning));
         }
      }

      return lines;
   }

   private static Collection<String> printIssue(IParsingIssue issue) {
      Collection<String> lines = new ArrayList<>();

      Path offendingFile = issue.getLocation().getPath();
      lines.add("----------------------------------------");
      lines.add(String.format("File: %s", offendingFile == null ? "unknown" : offendingFile.toAbsolutePath()));
      lines.add(String.format("%s: %s", issue.getSeverity(), issue.getMessage()));
      if (offendingFile != null && Files.isRegularFile(offendingFile)) {
         lines.addAll(printOffendingLine(issue));
      }

      return lines;
   }
   
   private static Collection<String> printOffendingLine(IParsingIssue issue) {
      Collection<String> lines = new ArrayList<>();

      try {
         ISourceLocation location = issue.getLocation();
         List<String> contents = Files.readAllLines(location.getPath(), StandardCharsets.UTF_8);
         int offendingLineIndex = location.getLineNumber() - 1;
         int startLineIndex = Math.max(0, offendingLineIndex - LINES_TO_PRINT_BEFORE_ERROR);
         int endLineIndex = Math.min(contents.size() - 1, offendingLineIndex + LINES_TO_PRINT_AFTER_ERROR);
         
         for (int i = startLineIndex; i <= endLineIndex; i++) {
            String line = contents.get(i);
            lines.add(line);
            if (i == offendingLineIndex) {
               StringBuilder marker = new StringBuilder();
               IntStream.range(1, location.getColumn()).forEach(__ -> marker.append(" "));
               IntStream.rangeClosed(0, line.length() - location.getColumn()).forEach(__ -> marker.append("^"));
               lines.add(marker.toString());
            }
         }
         
      } catch (IOException e) {
         // Do nothing.
      }

      return lines;
   }

}
