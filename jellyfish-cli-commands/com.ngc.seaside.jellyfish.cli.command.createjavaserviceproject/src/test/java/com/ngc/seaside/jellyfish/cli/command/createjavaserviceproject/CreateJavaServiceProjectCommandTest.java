/**
 * UNCLASSIFIED
 * Northrop Grumman Proprietary
 * ____________________________
 *
 * Copyright (C) 2018, Northrop Grumman Systems Corporation
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
package com.ngc.seaside.jellyfish.cli.command.createjavaserviceproject;

import com.ngc.blocs.service.log.api.ILogService;
import com.ngc.seaside.jellyfish.api.DefaultParameter;
import com.ngc.seaside.jellyfish.api.DefaultParameterCollection;
import com.ngc.seaside.jellyfish.api.IJellyFishCommandOptions;
import com.ngc.seaside.jellyfish.api.IJellyFishCommandProvider;
import com.ngc.seaside.systemdescriptor.model.api.IPackage;
import com.ngc.seaside.systemdescriptor.model.api.ISystemDescriptor;
import com.ngc.seaside.systemdescriptor.model.api.model.IModel;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.nio.file.Paths;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CreateJavaServiceProjectCommandTest {

   private CreateJavaServiceProjectCommand command;

   @Mock
   private IJellyFishCommandOptions options;

   @Mock
   private ILogService logService;

   @Mock
   private IJellyFishCommandProvider commandProvider;

   @Mock
   private ISystemDescriptor systemDescriptor;

   @Mock
   private IModel model;

   @Rule
   public TemporaryFolder outputDirectory = new TemporaryFolder();

   private String outputDirectoryName;

   private DefaultParameterCollection parameters;

   @Before
   public void setup() {
      when(options.getSystemDescriptor()).thenReturn(systemDescriptor);

      IPackage packagez = mock(IPackage.class);
      when(packagez.getName()).thenReturn("example.of");

      when(model.getName()).thenReturn("MyModel");
      when(model.getFullyQualifiedName()).thenReturn("example.of.MyModel");
      when(model.getParent()).thenReturn(packagez);
      when(systemDescriptor.findModel("example.of.MyModel")).thenReturn(Optional.of(model));

      parameters = new DefaultParameterCollection();
      when(options.getParameters()).thenReturn(parameters);

      outputDirectoryName = outputDirectory.getRoot().getAbsolutePath();

      command = new CreateJavaServiceProjectCommand();
      command.setLogService(logService);
      command.setJellyFishCommandProvider(commandProvider);
      command.activate();
   }

   @Test
   public void testDoesCreateProjectWithSuppliedParameters() {

      parameters.addParameter(new DefaultParameter<>(CreateJavaServiceProjectCommand.MODEL_PROPERTY,
                                                     model.getFullyQualifiedName()));
      parameters.addParameter(new DefaultParameter<>(CreateJavaServiceProjectCommand.OUTPUT_DIRECTORY_PROPERTY,
                                                     outputDirectoryName));
      parameters.addParameter(new DefaultParameter<>(CreateJavaServiceProjectCommand.GROUP_ID_PROPERTY,
                                                     "my.group"));
      parameters.addParameter(new DefaultParameter<>(CreateJavaServiceProjectCommand.PROJECT_NAME_PROPERTY,
                                                     "my-project"));

      command.run(options);

      ArgumentCaptor<IJellyFishCommandOptions> capture = ArgumentCaptor.forClass(IJellyFishCommandOptions.class);
      verify(commandProvider).run(eq(CreateJavaServiceProjectCommand.CREATE_JELLYFISH_GRADLE_PROJECT_COMMAND_NAME),
                                  capture.capture());
      verifyParametersForCreateJellyFishGradleProjectCommand(capture.getValue(), "my.group", "my-project");

      verify(commandProvider).run(eq(CreateJavaServiceProjectCommand.CREATE_DOMAIN_COMMAND_NAME),
                                  capture.capture());
      verifyParametersForCreateDomainCommand(capture.getValue(), "my-project");

      verify(commandProvider).run(eq(CreateJavaServiceProjectCommand.CREATE_JAVA_CUCUMBER_TESTS_COMMAND_NAME),
                                  capture.capture());
      verifyParametersForCreateCucumberTestsCommand(capture.getValue(), "my-project");

      verify(commandProvider).run(eq(CreateJavaServiceProjectCommand.CREATE_JAVA_DISTRIBUTION_COMMAND_NAME),
                                  capture.capture());
      verifyParametersForCreateDistributionCommand(capture.getValue(), "my-project");

      verify(commandProvider).run(eq(CreateJavaServiceProjectCommand.CREATE_JAVA_SERVICE_COMMAND_NAME),
                                  capture.capture());
      verifyParametersForCreateServiceCommand(capture.getValue(), "my-project");

      verify(commandProvider).run(eq(CreateJavaServiceProjectCommand.CREATE_JAVA_SERVICE_CONFIG_COMMAND_NAME),
                                  capture.capture());
      verifyParametersForCreateServiceConfigCommand(capture.getValue(), "my-project");

      verify(commandProvider).run(eq(CreateJavaServiceProjectCommand.CREATE_JAVA_SERVICE_BASE_COMMAND_NAME),
                                  capture.capture());
      verifyParametersForCreateServiceBaseCommand(capture.getValue(), "my-project");

      verify(commandProvider).run(eq(CreateJavaServiceProjectCommand.CREATE_JAVA_EVENTS_COMMAND_NAME),
                                  capture.capture());
      verifyParametersForCreateEventsCommand(capture.getValue(), "my-project");

      verify(commandProvider).run(eq(CreateJavaServiceProjectCommand.CREATE_PROTOCOLBUFFER_MESSAGES_COMMAND_NAME),
                                  capture.capture());
      verifyParametersForCreatePbMessagesCommand(capture.getValue(), "my-project");

      verify(commandProvider).run(eq(CreateJavaServiceProjectCommand.CREATE_JAVA_PUBSUB_CONNECTOR_COMMAND_NAME),
                                  capture.capture());
      verifyParametersForCreatePubsubConnectorCommand(capture.getValue(), "my-project");

      verify(commandProvider).run(eq(CreateJavaServiceProjectCommand.CREATE_JAVA_SERVICE_PUBSUB_BRIDGE_COMMAND_NAME),
                                  capture.capture());
      verifyParametersForCreateJavaServicePubsubBridgeCommand(capture.getValue(), "my-project");
   }


   @Test
   public void testWithoutDomain() {
      String modelName = model.getFullyQualifiedName();

      parameters.addParameter(
            new DefaultParameter<>(CreateJavaServiceProjectCommand.OUTPUT_DIRECTORY_PROPERTY, outputDirectoryName));
      parameters.addParameter(new DefaultParameter<>(CreateJavaServiceProjectCommand.MODEL_PROPERTY, modelName));
      parameters.addParameter(new DefaultParameter<>(CreateJavaServiceProjectCommand.CREATE_SERVICE_DOMAIN_PROPERTY,
                                                     "false"));

      command.run(options);

      ArgumentCaptor<IJellyFishCommandOptions> capture = ArgumentCaptor.forClass(IJellyFishCommandOptions.class);

      verify(commandProvider, never()).run(eq(CreateJavaServiceProjectCommand.CREATE_DOMAIN_COMMAND_NAME),
                                           capture.capture());
   }

   private void verifyParametersForCreateJellyFishGradleProjectCommand(IJellyFishCommandOptions options,
                                                                       String groupId,
                                                                       String projectName) {
      requireParameter(options, CreateJavaServiceProjectCommand.GROUP_ID_PROPERTY, groupId);
      requireParameter(options, CreateJavaServiceProjectCommand.PROJECT_NAME_PROPERTY, projectName);
      requireParameter(options, CreateJavaServiceProjectCommand.OUTPUT_DIRECTORY_PROPERTY, outputDirectoryName);
      requireParameter(options, CreateJavaServiceProjectCommand.MODEL_PROPERTY, model.getFullyQualifiedName());
   }

   private void verifyParametersForCreateDomainCommand(IJellyFishCommandOptions options,
                                                       String projectName) {
      requireParameter(options,
                       CreateJavaServiceProjectCommand.OUTPUT_DIRECTORY_PROPERTY,
                       Paths.get(outputDirectoryName, projectName).toAbsolutePath().toString());
   }

   private void verifyParametersForCreateCucumberTestsCommand(IJellyFishCommandOptions options,
                                                              String projectName) {
      requireParameter(options,
                       CreateJavaServiceProjectCommand.OUTPUT_DIRECTORY_PROPERTY,
                       Paths.get(outputDirectoryName, projectName).toAbsolutePath().toString());
   }

   private void verifyParametersForCreateDistributionCommand(IJellyFishCommandOptions options,
                                                             String projectName) {
      requireParameter(options,
                       CreateJavaServiceProjectCommand.OUTPUT_DIRECTORY_PROPERTY,
                       Paths.get(outputDirectoryName, projectName).toAbsolutePath().toString());
   }

   private void verifyParametersForCreateServiceCommand(IJellyFishCommandOptions options,
                                                        String projectName) {
      requireParameter(options,
                       CreateJavaServiceProjectCommand.OUTPUT_DIRECTORY_PROPERTY,
                       Paths.get(outputDirectoryName, projectName).toAbsolutePath().toString());
   }

   private void verifyParametersForCreateServiceBaseCommand(IJellyFishCommandOptions options,
                                                            String projectName) {
      requireParameter(options,
                       CreateJavaServiceProjectCommand.OUTPUT_DIRECTORY_PROPERTY,
                       Paths.get(outputDirectoryName, projectName).toAbsolutePath().toString());
   }

   private void verifyParametersForCreateServiceConfigCommand(IJellyFishCommandOptions options,
                                                              String projectName) {
      requireParameter(options,
                       CreateJavaServiceProjectCommand.OUTPUT_DIRECTORY_PROPERTY,
                       Paths.get(outputDirectoryName, projectName).toAbsolutePath().toString());
   }

   private void verifyParametersForCreatePbMessagesCommand(IJellyFishCommandOptions options,
                                                           String projectName) {
      requireParameter(options,
                       CreateJavaServiceProjectCommand.OUTPUT_DIRECTORY_PROPERTY,
                       Paths.get(outputDirectoryName, projectName).toAbsolutePath().toString());
   }

   private void verifyParametersForCreatePubsubConnectorCommand(IJellyFishCommandOptions options,
                                                                String projectName) {
      requireParameter(options,
                       CreateJavaServiceProjectCommand.OUTPUT_DIRECTORY_PROPERTY,
                       Paths.get(outputDirectoryName, projectName).toAbsolutePath().toString());
   }

   private void verifyParametersForCreateJavaServicePubsubBridgeCommand(IJellyFishCommandOptions options,
                                                                        String projectName) {
      requireParameter(options,
                       CreateJavaServiceProjectCommand.OUTPUT_DIRECTORY_PROPERTY,
                       Paths.get(outputDirectoryName, projectName).toAbsolutePath().toString());
   }

   private void verifyParametersForCreateEventsCommand(IJellyFishCommandOptions options,
                                                       String projectName) {
      requireParameter(options,
                       CreateJavaServiceProjectCommand.OUTPUT_DIRECTORY_PROPERTY,
                       Paths.get(outputDirectoryName, projectName).toAbsolutePath().toString());
   }

   private static void requireParameter(IJellyFishCommandOptions options, String parameter, Object value) {
      assertTrue("missing parameter " + parameter,
                 options.getParameters().containsParameter(parameter));
      assertEquals("value of parameter " + parameter + " incorrect!",
                   value,
                   options.getParameters().getParameter(parameter).getValue());
   }
}
