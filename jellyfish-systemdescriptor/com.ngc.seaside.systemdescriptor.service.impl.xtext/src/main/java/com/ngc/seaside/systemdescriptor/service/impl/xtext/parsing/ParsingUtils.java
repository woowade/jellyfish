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
package com.ngc.seaside.systemdescriptor.service.impl.xtext.parsing;

import com.google.common.base.Preconditions;
import com.ngc.seaside.systemdescriptor.service.api.ParsingException;
import com.ngc.seaside.systemdescriptor.service.repository.api.IRepositoryService;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.eclipse.xtext.resource.XtextResource;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

class ParsingUtils {

   /**
    * The default path that contains the {@code .sd} files for a standard
    * system descriptor project.
    */
   private static final Path SD_MAIN_SOURCE_PATH = Paths.get("src", "main", "sd");
   private static final Path SD_TEST_SOURCE_PATH = Paths.get("src", "test", "gherkin");

   private static final Path SD_MAIN_CLASSPATH = Paths.get("build", "resources", "main");
   private static final Path SD_TEST_CLASSPATH = Paths.get("build", "resources", "test");

   private static final Path[] POM_PATHS = {Paths.get("build", "poms"),
                                            Paths.get("build", "publications", "mavenSd")};

   private static final String TESTS_CLASSIFIER = "tests";

   private final IRepositoryService repositoryService;

   public ParsingUtils(IRepositoryService repositoryService) {
      this.repositoryService = repositoryService;
   }

   /**
    * Returns the parsed XtextResources for the given project.
    *
    * @param projectDirectory directory of project
    * @param ctx              parsing context
    * @return collection of parsed XtextResources
    */
   public Collection<XtextResource> getProjectAndDependencies(Path projectDirectory, ParsingContext ctx) {
      Preconditions.checkNotNull(projectDirectory, "project directory may not be null!");
      Preconditions.checkNotNull(ctx, "parsing context may not be null!");
      try {
         return parseGradleProject(projectDirectory, ctx);
      } catch (IOException e) {
         throw new ParsingException(e);
      }
   }

   /**
    * Returns the parsed XtextResources for the project with the given gav.
    *
    * @param gav groupId:artifactId:version
    * @param ctx parsing context
    * @return collection of parsed XtextResources
    */
   public Collection<XtextResource> getProjectAndDependencies(String gav, ParsingContext ctx) {
      Preconditions.checkNotNull(gav, "gav may not be null!");
      Preconditions.checkArgument(gav.matches("[^:\\s]+:[^:\\s]+:[^:\\s]+"), "invalid gav: " + gav);
      Preconditions.checkNotNull(ctx, "parsing context may not be null!");
      try {
         return parseDependencies(Collections.singleton(gav), ctx, true);
      } catch (IOException e) {
         throw new ParsingException(e);
      }
   }

   /**
    * Parses the gradle project in the given directory. This project will attempt to locate system descriptor files in
    * the projectDirectory/build/resources/main; otherwise it will locate system descriptor files in src/main/sd.
    * Dependencies to the project are determined by the pom file found in projectDirectory/build/poms.
    *
    * @param projectDirectory directory of project
    * @param ctx              parsing context
    * @return collection of parsed XtextResources
    */
   private Collection<XtextResource> parseGradleProject(Path projectDirectory, ParsingContext ctx)
         throws IOException {
      Path resourcesDirectory = projectDirectory.resolve(SD_MAIN_SOURCE_PATH);
      Path testResourcesDirectory = projectDirectory.resolve(SD_TEST_SOURCE_PATH);
      if (!Files.isDirectory(resourcesDirectory)) {
         resourcesDirectory = projectDirectory.resolve(SD_MAIN_CLASSPATH);
         testResourcesDirectory = projectDirectory.resolve(SD_TEST_CLASSPATH);
         if (!Files.isDirectory(resourcesDirectory)) {
            throw new ParsingException("Cannot find location of system descriptor files");
         }
      }
      ctx.setMain(resourcesDirectory);
      if (Files.isDirectory(testResourcesDirectory)) {
         ctx.setTest(testResourcesDirectory);
      }

      Path pom = null;
      for (Path pomPath : POM_PATHS) {
         pom = projectDirectory.resolve(pomPath);
         if (Files.isDirectory(pom)) {
            List<Path> poms = Files.list(pom)
                  .filter(file -> file.toString().endsWith(".pom") || file.toString().endsWith(".xml"))
                  .collect(Collectors.toList());
            if (poms.isEmpty()) {
               pom = null;
            } else if (poms.size() == 1) {
               pom = poms.get(0);
            } else {
               pom = pom.resolve("pom-default.xml");
               if (!Files.isRegularFile(pom)) {
                  pom = null;
               }
            }
         } else {
            pom = null;
         }
         if (pom != null) {
            break;
         }
      }

      Collection<XtextResource> resources = new LinkedHashSet<>();

      Files.walk(resourcesDirectory)
            .filter(Files::isRegularFile)
            .filter(file -> file.toString().endsWith(".sd"))
            .map(ctx::resourceOf)
            .forEach(resources::add);

      if (pom != null) {
         resources.addAll(parseDependencies(pom, ctx, false));
      }
      return resources;
   }

   /**
    * Returns the parsed XtextResources contained in the given jar.
    *
    * @param jar jar file
    * @param ctx parsing context
    * @return collection of parsed XtextResources
    */
   public static Collection<XtextResource> parseJar(Path jar, ParsingContext ctx) throws IOException {
      Collection<XtextResource> resources = new LinkedHashSet<>();
      try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(jar))) {
         ZipEntry entry;
         while ((entry = zis.getNextEntry()) != null) {
            if (entry.getName().endsWith(".sd")) {
               XtextResource resource = ctx.resourceOf(jar, entry);
               if (resource != null) {
                  resources.add(resource);
               }
            }
         }
      }
      return resources;
   }

   /**
    * Uses the given maven pom file to locate the corresponding project and its dependencies and returns the parsed
    * XtextResources from them.
    *
    * @param pom         maven pom file
    * @param ctx         parsing context
    * @param includeSelf if {@code true} include the main project represented in the pom; otherwise, include only the
    *                    pom's dependencies
    * @return collection of parsed XtextResources
    */
   private Collection<XtextResource> parseDependencies(Path pom, ParsingContext ctx, boolean includeSelf)
         throws IOException {

      MavenXpp3Reader reader = new MavenXpp3Reader();
      Model model;

      try {
         model = reader.read(Files.newBufferedReader(pom));
      } catch (Exception e) {
         return Collections.emptySet();
      }
      Collection<String> gavs;
      if (includeSelf) {
         String gav = String.format("%s:%s:%s", model.getGroupId(), model.getArtifactId(), model.getVersion());
         gavs = Collections.singleton(gav);
      } else {
         // Because the project may not have been installed, parse the dependencies of this project's dependencies
         gavs = new LinkedHashSet<>();
         for (Dependency dependency : model.getDependencies()) {
            String gav = String.format("%s:%s:%s",
                                       dependency.getGroupId(),
                                       dependency.getArtifactId(),
                                       dependency.getVersion());
            gavs.add(gav);
         }
         includeSelf = true;
      }
      return parseDependencies(gavs, ctx, includeSelf);
   }

   /**
    * Locates the projects corresponding to the given gavs and their dependencies and returns the parsed XtextResources
    * from them.
    *
    * @param gavs        project gavs
    * @param ctx         parsing context
    * @param includeSelf if {@code true} include the main projects represented by the gavs; otherwise, include only the
    *                    poms' dependencies
    * @return collection of parsed XtextResources
    */
   private Collection<XtextResource> parseDependencies(Collection<String> gavs, ParsingContext ctx, boolean includeSelf)
         throws IOException {
      if (gavs == null || gavs.isEmpty()) {
         return Collections.emptySet();
      }
      Collection<XtextResource> resources = new LinkedHashSet<>();
      for (String gav : gavs) {
         String[] splitGav = gav.split(":");
         String artifactGav = String.format("%s:%s:zip:%s", splitGav[0], splitGav[1], splitGav[2]);
         // We also need to download the tests classifier for the project. This is needed because Gradle will refuse to
         // download the tests later since the ZIP file will already be in the local Maven repository. In this case,
         // Gradle thinks that the entire artifact has been downloaded and won't try to download the tests. Thus, we
         // need to download them both.
         String testArtifactGav = String.format("%s:%s:zip:%s:%s",
                                                splitGav[0],
                                                splitGav[1],
                                                TESTS_CLASSIFIER,
                                                splitGav[2]);
         if (includeSelf) {
            Path mainJar = repositoryService.getArtifact(artifactGav);
            resources.addAll(parseJar(mainJar, ctx));
            Path testJar = repositoryService.getArtifact(testArtifactGav);
            if (ctx.getMain() == null) {
               URI mainUri = URI.create("jar:file:" + mainJar.toUri().getPath());
               FileSystem mainFs = null;
               try {
                  mainFs = FileSystems.getFileSystem(mainUri);
               } catch (FileSystemNotFoundException e) {
                  mainFs = FileSystems.newFileSystem(mainUri, Collections.singletonMap("create", true));
               }
               ctx.setMain(mainFs.getPath("/"));
            }
            if (ctx.getTest() == null) {
               URI testUri = URI.create("jar:file:" + testJar.toUri().getPath());
               FileSystem testFs = null;
               try {
                  testFs = FileSystems.getFileSystem(testUri);
               } catch (FileSystemNotFoundException e) {
                  testFs = FileSystems.newFileSystem(testUri, Collections.singletonMap("create", true));
               }
               ctx.setTest(testFs.getPath("/"));
            }
         }
         for (Path path : repositoryService.getArtifactDependencies(artifactGav, true)) {
            resources.addAll(parseJar(path, ctx));
         }
      }
      return resources;
   }

}
