package com.ngc.seaside.bootstrap.service.impl.templateservice;

import com.ngc.blocs.service.log.api.ILogService;
import com.ngc.blocs.service.resource.api.IResourceService;
import com.ngc.blocs.test.impl.common.log.PrintStreamLogService;
import com.ngc.seaside.bootstrap.service.promptuser.api.IPromptUserService;
import com.ngc.seaside.bootstrap.service.property.api.IProperties;
import com.ngc.seaside.bootstrap.service.property.api.IPropertyService;
import com.ngc.seaside.bootstrap.service.template.api.TemplateServiceException;
import com.ngc.seaside.command.api.DefaultParameter;
import com.ngc.seaside.command.api.DefaultParameterCollection;
import com.ngc.seaside.command.api.IParameterCollection;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentCaptor;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 *
 */
@SuppressWarnings("unchecked")
public class TemplateServiceTest {

   private ILogService logService;
   private IResourceService resourceService;
   private IPromptUserService promptUserService;
   private IPropertyService propertyService;

   private TemplateService templateService;

   @Rule
   public TemporaryFolder testFolder = new TemporaryFolder();

   @Before
   public void setup() throws URISyntaxException {
      logService = new PrintStreamLogService();
      resourceService = mock(IResourceService.class);

      URL resourcesDir = getClass().getClassLoader().getResource("templates");
      assertNotNull(resourcesDir);
      File file = new File(resourcesDir.toURI()).getParentFile();

      when(resourceService.getResourceRootPath()).thenReturn(Paths.get(file.getAbsolutePath()));

      promptUserService = mock(IPromptUserService.class);
      propertyService = mock(IPropertyService.class);

      templateService = new TemplateService();
      templateService.setLogService(logService);
      templateService.setResourceService(resourceService);
      templateService.setPromptUserService(promptUserService);
      templateService.setPropertyService(propertyService);
      templateService.activate();
   }

   @After
   public void shutdown() {
      templateService.deactivate();
      templateService.removeLogService(logService);
      templateService.removePromptUserService(promptUserService);
      templateService.removeResourceService(resourceService);
   }

   /**
    * Simple test to ensure that the templates exist but it does not validate they are correct. See test below.
    */
   @Test
   public void testTemplateExists() {
      assertFalse(templateService.templateExists("com.ngc.seaside.bootstrap.command.impl.invalid"));
      assertTrue(templateService.templateExists("com.ngc.seaside.bootstrap.command.impl.example"));
      assertTrue(templateService.templateExists("com.ngc.seaside.bootstrap.command.impl.example-with-hyphen"));
      assertTrue(templateService.templateExists("com.ngc.seaside.bootstrap.command.impl.duplicatefolderexample"));
      assertTrue(templateService.templateExists("com.ngc.seaside.bootstrap.command.impl.invalidnofile"));
      assertTrue(templateService.templateExists("com.ngc.seaside.bootstrap.command.impl.invalidnofolder"));
   }

   @Test
   public void doesCleanCorrectly() throws IOException {
      final String folder = "same-path";

      DefaultParameterCollection parameters = new DefaultParameterCollection();
      parameters.addParameter(new DefaultParameter<>("parameter1", folder));
      parameters.addParameter(new DefaultParameter<>("parameter2", folder));
      IProperties properties = mock(IProperties.class);
      when(properties.get("parameter1")).thenReturn(folder);
      when(properties.get("parameter2")).thenReturn(folder);
      when(properties.getKeys()).thenReturn(
         Arrays.asList(new String[] { "parameter1", "parameter2" }));
      when(propertyService.load(any())).thenReturn(properties);

      Path outputDirectory;

      outputDirectory = testFolder.newFolder("output1").toPath();
      templateService.unpack("com.ngc.seaside.bootstrap.command.impl.duplicatefolderexample", parameters, outputDirectory, false);

      assertEquals(4, Files.walk(outputDirectory).count());
      assertTrue(Files.isDirectory(outputDirectory.resolve(folder)));
      assertTrue(Files.isRegularFile(outputDirectory.resolve(Paths.get(folder, "File1.txt"))));
      assertTrue(Files.isRegularFile(outputDirectory.resolve(Paths.get(folder, "File2.txt"))));

      Files.createFile(outputDirectory.resolve(Paths.get(folder, "File3.txt")));
      Files.createDirectory(outputDirectory.resolve(folder).resolve("other-folder"));
      Files.createFile(outputDirectory.resolve(folder).resolve("other-folder").resolve("File4.txt"));

      templateService.unpack("com.ngc.seaside.bootstrap.command.impl.duplicatefolderexample", parameters, outputDirectory, true);

      assertEquals(4, Files.walk(outputDirectory).count());
      assertTrue(Files.isDirectory(outputDirectory.resolve(folder)));
      assertTrue(Files.isRegularFile(outputDirectory.resolve(Paths.get(folder, "File1.txt"))));
      assertTrue(Files.isRegularFile(outputDirectory.resolve(Paths.get(folder, "File2.txt"))));

   }

   @Test(expected = TemplateServiceException.class)
   public void testInvalidTemplateFileThrowsATemplateServiceException() throws TemplateServiceException, IOException {
      templateService.unpack("Invalid", new DefaultParameterCollection(), Files.createTempDirectory(null), false);
   }

   /**
    * Using the Temporary folder, ensure that the example is actually unpacked.
    *
    * @throws IOException if the test output directory can't be created.
    */
   @Test
   public void doesUnpack() throws IOException {
      File outputDirectory = testFolder.newFolder("output");

      ArgumentCaptor<String> parameterCapture = ArgumentCaptor.forClass(String.class);
      ArgumentCaptor<String> defaultCapture = ArgumentCaptor.forClass(String.class);

      IProperties properties = mock(IProperties.class);
      when(properties.get("classname")).thenReturn("MyClass");
      when(properties.get("groupId")).thenReturn("com.ngc.seaside");
      when(properties.get("artifactId")).thenReturn("mybundle");
      when(properties.get("version")).thenReturn("1.0-SNAPSHOT");
      when(properties.get("package")).thenReturn("com.ngc.seaside.mybundle");
      when(properties.getKeys()).thenReturn(
               Arrays.asList(new String[] { "classname", "groupId", "artifactId", "version", "package" }));

      when(propertyService.load(any())).thenReturn(properties);
      when(promptUserService.prompt(
               parameterCapture.capture(), defaultCapture.capture(), any())).thenReturn("value");

      DefaultParameterCollection collection = new DefaultParameterCollection();
      collection.addParameter(new DefaultParameter<>("classname").setValue("MyUserClass"));
      collection.addParameter(new DefaultParameter<>("pojo", new TestablePojo("Bob", "Smith")));

      templateService.unpack("com.ngc.seaside.bootstrap.command.impl.example",
                             collection, Paths.get(outputDirectory.getAbsolutePath()), false);

      //verify that the prompt service isn't called for the default parameter that we passed in for classname!
      verify(promptUserService, times(4))
               .prompt(anyString(), anyString(), any());

      //this is called 'value' because of the "when" above that mocks the promptUserService
      File value = Paths.get(outputDirectory.getAbsolutePath(), "value.value").toFile();
      File src = Paths.get(value.getAbsolutePath(), "src").toFile();

      assertTrue(value.exists());
      assertTrue(src.exists());
   }

   /**
    * Ensure that the invalid templates don't unpack.
    *
    * @throws IOException if the test output directory can't be created.
    */
   @Test
   public void doesNotUnpack() throws IOException {
      File outputDirectory = testFolder.newFolder("output");

      IParameterCollection collection = mock(IParameterCollection.class);

      when(promptUserService.prompt(anyString(), anyString(), any())).thenReturn("value");

      try {
         templateService.unpack("com.ngc.seaside.bootstrap.command.impl.invalidnofile",
                                collection,
                                Paths.get(outputDirectory.getAbsolutePath()),
                                false);
      } catch (TemplateServiceException e) {
         assertEquals(e.getMessage(),
                      "An error occurred processing the template zip file: com.ngc.seaside.bootstrap.command.impl.invalidnofile");
         assertEquals(e.getCause().getMessage(),
                      "Invalid template. Each template must contain template.properties and a template folder named 'templateContent'");
      }

      try {
         templateService.unpack("com.ngc.seaside.bootstrap.command.impl.invalidnofolder",
                                collection,
                                Paths.get(outputDirectory.getAbsolutePath()),
                                false);
      } catch (TemplateServiceException e) {
         assertEquals(e.getMessage(),
                      "An error occurred processing the template zip file: com.ngc.seaside.bootstrap.command.impl.invalidnofolder");
         assertEquals(e.getCause().getMessage(),
                      "Invalid template. Each template must contain template.properties and a template folder named 'templateContent'");
      }
   }

   @Test
   public void doesUnpackIgnore() throws IOException {
      File outputDirectory = testFolder.newFolder("output");

      ArgumentCaptor<String> parameterCapture = ArgumentCaptor.forClass(String.class);
      ArgumentCaptor<String> defaultCapture = ArgumentCaptor.forClass(String.class);

      IProperties properties = mock(IProperties.class);
      when(properties.get("classname")).thenReturn("MyClass");
      when(properties.get("groupId")).thenReturn("com.ngc.seaside");
      when(properties.get("artifactId")).thenReturn("mybundle");
      when(properties.get("commandName")).thenReturn("my-bundle");
      when(properties.get("package")).thenReturn("com.ngc.seaside.mybundle");
      when(properties.getKeys()).thenReturn(
               Arrays.asList(new String[] { "classname", "groupId", "artifactId", "commandName", "package" }));

      when(propertyService.load(any())).thenReturn(properties);
      when(promptUserService.prompt(
               parameterCapture.capture(), defaultCapture.capture(), any())).thenReturn("value");

      IParameterCollection collection = mock(IParameterCollection.class);
      when(collection.containsParameter("classname")).thenReturn(true);
      when(collection.containsParameter("groupId")).thenReturn(true);
      when(collection.containsParameter("artifactId")).thenReturn(true);
      when(collection.containsParameter("commandName")).thenReturn(true);
      when(collection.containsParameter("package")).thenReturn(true);
      when(collection.getParameter("classname")).thenReturn(new DefaultParameter<>("classname").setValue("MyClass"));
      when(collection.getParameter("groupId")).thenReturn(new DefaultParameter<>("classname").setValue("com.ngc.seaside"));
      when(collection.getParameter("artifactId")).thenReturn(new DefaultParameter<>("artifactId").setValue("mybundle"));
      when(collection.getParameter("commandName")).thenReturn(new DefaultParameter<>("commandName").setValue("my-bundle"));
      when(collection.getParameter("package")).thenReturn(new DefaultParameter<>("classname").setValue("com.ngc.seaside.mybundle"));


      templateService.unpack("com.ngc.seaside.bootstrap.command.impl.ignoreexample",
                             collection, Paths.get(outputDirectory.getAbsolutePath()), false);

      //verify that the prompt service isn't called for the default parameter that we passed in for classname!
      verify(promptUserService, never()).prompt(anyString(), anyString(), any());

      //this is called 'value' because of the "when" above that mocks the promptUserService
      File bundle = Paths.get(outputDirectory.getAbsolutePath(), "com.ngc.seaside.mybundle").toFile();
      File src = Paths.get(bundle.getAbsolutePath(), "src").toFile();
      File main = Paths.get(src.getAbsolutePath(), "main").toFile();
      File template = Paths.get(main.getAbsolutePath(), "template").toFile();
      File templateContent = Paths.get(template.getAbsolutePath(), "templateContent").toFile();
      File groupArtifact = Paths.get(templateContent.getAbsolutePath(), "${groupId}.${artifactId}").toFile();

      assertTrue(bundle.exists());
      assertTrue(src.exists());
      assertTrue(main.exists());
      assertTrue(template.exists());
      assertTrue(templateContent.exists());
      assertTrue(groupArtifact.exists());

//      listFiles(bundle, "");
   }

   private void listFiles(File file, String indent) {
      System.out.println(String.format("%s%s", indent, file.getAbsolutePath()));
      if(file.isFile()) {
         return;
      }
      String newIndent = indent + "";
      if(file.isDirectory()) {
         for(File sub : file.listFiles()) {
            listFiles(sub, newIndent);
         }
      }
   }

}
