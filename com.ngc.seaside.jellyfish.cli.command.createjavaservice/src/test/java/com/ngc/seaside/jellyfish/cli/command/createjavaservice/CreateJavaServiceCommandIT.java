package com.ngc.seaside.jellyfish.cli.command.createjavaservice;

import static com.ngc.seaside.jellyfish.cli.command.test.files.TestingFiles.assertFileContains;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.ngc.blocs.service.log.api.ILogService;
import com.ngc.seaside.command.api.DefaultParameter;
import com.ngc.seaside.command.api.DefaultParameterCollection;
import com.ngc.seaside.jellyfish.api.IJellyFishCommandOptions;
import com.ngc.seaside.jellyfish.cli.command.createjavaservice.dto.IServiceDtoFactory;
import com.ngc.seaside.jellyfish.cli.command.createjavaservice.dto.ServiceDtoFactory;
import com.ngc.seaside.jellyfish.cli.command.createjavaservicebase.dto.BaseServiceDtoFactory;
import com.ngc.seaside.jellyfish.cli.command.createjavaservicebase.dto.IBaseServiceDtoFactory;
import com.ngc.seaside.jellyfish.cli.command.test.systemdescriptor.ModelUtils;
import com.ngc.seaside.jellyfish.cli.command.test.template.MockedTemplateService;
import com.ngc.seaside.jellyfish.service.codegen.api.IDataFieldGenerationService;
import com.ngc.seaside.jellyfish.service.codegen.api.IJavaServiceGenerationService;
import com.ngc.seaside.jellyfish.service.codegen.api.dto.ArgumentDto;
import com.ngc.seaside.jellyfish.service.codegen.api.dto.ClassDto;
import com.ngc.seaside.jellyfish.service.codegen.api.dto.MethodDto;
import com.ngc.seaside.jellyfish.service.codegen.api.dto.PubSubMethodDto;
import com.ngc.seaside.jellyfish.service.codegen.api.dto.TypeDto;
import com.ngc.seaside.jellyfish.service.data.api.IDataService;
import com.ngc.seaside.jellyfish.service.name.api.IPackageNamingService;
import com.ngc.seaside.jellyfish.service.name.api.IProjectInformation;
import com.ngc.seaside.jellyfish.service.name.api.IProjectNamingService;
import com.ngc.seaside.jellyfish.service.scenario.api.IPublishSubscribeMessagingFlow;
import com.ngc.seaside.jellyfish.service.scenario.api.IScenarioService;
import com.ngc.seaside.systemdescriptor.model.api.INamedChild;
import com.ngc.seaside.systemdescriptor.model.api.IPackage;
import com.ngc.seaside.systemdescriptor.model.api.ISystemDescriptor;
import com.ngc.seaside.systemdescriptor.model.api.data.IData;
import com.ngc.seaside.systemdescriptor.model.api.model.IModel;
import com.ngc.seaside.systemdescriptor.model.api.model.scenario.IScenario;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Optional;

@RunWith(MockitoJUnitRunner.class)
public class CreateJavaServiceCommandIT {

   private CreateJavaServiceCommand command;

   private MockedTemplateService templateService;

   private IServiceDtoFactory serviceTemplateDaoFactory;
   
   private IBaseServiceDtoFactory baseServiceTemplateDaoFactory;

   private DefaultParameterCollection parameters;

   @Rule
   public final TemporaryFolder outputDirectory = new TemporaryFolder();

   @Mock
   private IJellyFishCommandOptions jellyFishCommandOptions;

   @Mock
   private ILogService logService;

   @Mock
   private IProjectNamingService projectService;

   @Mock
   private IPackageNamingService packageService;

   @Mock
   private IJavaServiceGenerationService generatorService;

   @Mock
   private ClassDto<?> interfaceDto;
   
   @Mock
   private IScenarioService scenarioService;

   @Mock
   private IDataService dataService;

   @Mock
   private IDataFieldGenerationService dataFieldGenerationService;
   
   @Before
   public void setup() throws Throwable {
      outputDirectory.newFile("settings.gradle");

      templateService = new MockedTemplateService()
                                                   .useRealPropertyService()
                                                   .setTemplateDirectory(
                                                      CreateJavaServiceCommand.class.getPackage().getName(),
                                                      Paths.get("src", "main", "template"));

      serviceTemplateDaoFactory = new ServiceDtoFactory(projectService, packageService);

      baseServiceTemplateDaoFactory = new BaseServiceDtoFactory(projectService, packageService, generatorService, scenarioService, dataService, dataFieldGenerationService, logService);
      
      ISystemDescriptor systemDescriptor = mock(ISystemDescriptor.class);
      IModel testModel = newModelForTesting();

      when(systemDescriptor.findModel("com.ngc.seaside.threateval.EngagementTrackPriorityService")).thenReturn(
         Optional.of(testModel));

      parameters = new DefaultParameterCollection();
      when(jellyFishCommandOptions.getParameters()).thenReturn(parameters);
      when(jellyFishCommandOptions.getSystemDescriptor()).thenReturn(systemDescriptor);

      command = new CreateJavaServiceCommand();
      command.setLogService(logService);
      command.setServiceTemplateDaoFactory(serviceTemplateDaoFactory);
      command.setBaseServiceTemplateDaoFactory(baseServiceTemplateDaoFactory);
      command.setTemplateService(templateService);
      command.setProjectNamingService(projectService);

      when(projectService.getServiceProjectName(any(), any())).thenAnswer(args -> {
         IModel model = args.getArgument(1);
         IProjectInformation information = mock(IProjectInformation.class);
         when(information.getDirectoryName()).thenReturn(model.getFullyQualifiedName().toLowerCase());
         return information;
      });
      when(projectService.getBaseServiceProjectName(any(), any())).thenAnswer(args -> {
         IModel model = args.getArgument(1);
         IProjectInformation information = mock(IProjectInformation.class);
         when(information.getDirectoryName()).thenReturn(model.getFullyQualifiedName().toLowerCase() + ".base");
         when(information.getArtifactId()).thenReturn(model.getName().toLowerCase() + ".base");
         return information;
      });
      when(projectService.getEventsProjectName(any(), any())).thenAnswer(args -> {
         IModel model = args.getArgument(1);
         IProjectInformation information = mock(IProjectInformation.class);
         when(information.getArtifactId()).thenReturn(model.getName().toLowerCase() + ".events");
         return information;
      });
      when(packageService.getServiceBaseImplementationPackageName(any(), any())).thenAnswer(args -> {
         IModel model = args.getArgument(1);
         return model.getFullyQualifiedName().toLowerCase() + ".base.impl";
      });
      when(packageService.getServiceImplementationPackageName(any(), any())).thenAnswer(args -> {
         IModel model = args.getArgument(1);
         return model.getFullyQualifiedName().toLowerCase() + ".impl";
      });
      when(packageService.getServiceInterfacePackageName(any(), any())).thenAnswer(args -> {
         IModel model = args.getArgument(1);
         return model.getFullyQualifiedName().toLowerCase() + ".api";
      });
      when(generatorService.getServiceInterfaceDescription(any(), any())).thenAnswer(args -> {
         IJellyFishCommandOptions options = args.getArgument(0);
         IModel model = args.getArgument(1);
         ClassDto<MethodDto> interfaceDto = new ClassDto<>();
         interfaceDto.setName("I" + model.getName())
                     .setPackageName(packageService.getServiceInterfacePackageName(options, model))
                     .setMethods(Collections.singletonList(
                        new MethodDto().setName("calculateTrackPriority")
                                       .setOverride(false)
                                       .setReturnArgument(
                                          new ArgumentDto().setTypeName("TrackPriority")
                                                           .setPackageName("com.ngc.seaside.threateval")
                                                           .setName("trackPriority"))
                                       .setArguments(Collections.singletonList(
                                          new ArgumentDto().setTypeName("TrackEngagementStatus")
                                                           .setPackageName("com.ngc.seaside.threateval")
                                                           .setName("trackEngagementStatus")))))
                     .setImports(new LinkedHashSet<>(Arrays.asList(
                        "com.ngc.seaside.threateval.engagementtrackpriorityservice.events.TrackEngagementStatus",
                        "com.ngc.seaside.threateval.engagementtrackpriorityservice.events.TrackPriority")));
         return interfaceDto;
      });
      ClassDto<PubSubMethodDto> abstractClassDto = new ClassDto<>();
      
      abstractClassDto.setName("Abstract" + testModel.getName())
            .setPackageName(packageService.getServiceBaseImplementationPackageName(jellyFishCommandOptions, testModel))
            .setImports(new HashSet<>(Arrays.asList("com.ngc.blocs.service.event.api.IEvent",
                                                    "com.ngc.seaside.threateval.engagementtrackpriorityservice.api.IEngagementTrackPriorityService",
                                                    "com.ngc.seaside.threateval.engagementtrackpriorityservice.events.TrackEngagementStatus",
                                                    "com.ngc.seaside.threateval.engagementtrackpriorityservice.events.TrackPriority")));

      when(generatorService.getBaseServiceDescription(any(), any())).thenReturn(abstractClassDto);
      when(dataService.getEventClass(any(), any())).thenAnswer(args -> {
         INamedChild<IPackage> child = args.getArgument(1);
         TypeDto<?> typeDto = new ArgumentDto();
         typeDto.setPackageName(child.getParent().getName() + ".engagementtrackpriorityservice.events");
         typeDto.setTypeName(child.getName());
         return typeDto;
      });
      
      IPublishSubscribeMessagingFlow flow = mock(IPublishSubscribeMessagingFlow.class);  
      IScenario scenario = testModel.getScenarios().getByName("calculateTrackPriority").get();
      when(flow.getScenario()).thenReturn(scenario);
      when(scenarioService.getPubSubMessagingFlow(any(), any())).thenReturn(Optional.of(flow));
   }

   @Test
   public void testDoesGenerateServiceWithSuppliedCommands() throws Throwable {
      parameters.addParameter(new DefaultParameter<>(CreateJavaServiceCommand.MODEL_PROPERTY,
         "com.ngc.seaside.threateval.EngagementTrackPriorityService"));
      parameters.addParameter(new DefaultParameter<>(CreateJavaServiceCommand.OUTPUT_DIRECTORY_PROPERTY,
         outputDirectory.getRoot().getAbsolutePath()));

      command.run(jellyFishCommandOptions);

      Path gradleBuildPath = Paths.get(outputDirectory.getRoot().getAbsolutePath(),
         "com.ngc.seaside.threateval.engagementtrackpriorityservice",
         "build.gradle");
      
      assertFileContains(gradleBuildPath, "\\bproject\\(['\"]:engagementtrackpriorityservice.events['\"]\\)");
      assertFileContains(gradleBuildPath, "\\bproject\\(['\"]:engagementtrackpriorityservice.base['\"]\\)");

      Path servicePath = Paths.get(outputDirectory.getRoot().getAbsolutePath(),
         "com.ngc.seaside.threateval.engagementtrackpriorityservice",
         "src/main/java/com/ngc/seaside/threateval/engagementtrackpriorityservice/impl/EngagementTrackPriorityService.java");
      
      assertFileContains(servicePath, "\\bpackage\\s+com.ngc.seaside.threateval.engagementtrackpriorityservice.impl\\s*;");
      assertFileContains(servicePath,
         "\\bimport\\s+com.ngc.seaside.threateval.engagementtrackpriorityservice.api.IEngagementTrackPriorityService\\s*;");
      assertFileContains(servicePath,
         "\\bimport\\s+com.ngc.seaside.threateval.engagementtrackpriorityservice.base.impl.AbstractEngagementTrackPriorityService\\s*;");
      assertFileContains(servicePath,
         "\\bimport\\s+com.ngc.seaside.threateval.engagementtrackpriorityservice.events.TrackEngagementStatus\\s*;");
      assertFileContains(servicePath,
         "\\bimport\\s+com.ngc.seaside.threateval.engagementtrackpriorityservice.events.TrackPriority\\s*;");
      assertFileContains(servicePath, "\\bclass\\s+EngagementTrackPriorityService\\b");
      assertFileContains(servicePath, "extends\\s+\\S*?AbstractEngagementTrackPriorityService");
      
      //TODO These two assertions are incorrect
      assertFileContains(servicePath, "\\bTrackPriority\\s+calculateTrackPriority\\s*\\(");
      
      Path testPath = Paths.get(outputDirectory.getRoot().getAbsolutePath(), "com.ngc.seaside.threateval.engagementtrackpriorityservice",
         "src/test/java/com/ngc/seaside/threateval/engagementtrackpriorityservice/impl/EngagementTrackPriorityServiceTest.java");

      assertFileContains(testPath, "\\bpackage\\s+com.ngc.seaside.threateval.engagementtrackpriorityservice.impl\\s*;");
      assertFileContains(testPath, "\\bclass\\s+EngagementTrackPriorityServiceTest\\b");
      
   }

   public static IModel newModelForTesting() {
      ModelUtils.PubSubModel model = new ModelUtils.PubSubModel("com.ngc.seaside.threateval.EngagementTrackPriorityService");
      IData trackEngagementStatus = ModelUtils.getMockNamedChild(IData.class, "com.ngc.seaside.threateval.TrackEngagementStatus");
      IData trackPriority = ModelUtils.getMockNamedChild(IData.class, "com.ngc.seaside.threateval.TrackPriority");

      model.addPubSub("calculateTrackPriority", "trackEngagementStatus", trackEngagementStatus, "trackPriority", trackPriority);

      return model;
   }

   @SafeVarargs
   private static <T> ArrayList<T> listOf(T... things) {
      // TODO TH:
      // Fix the basic model impl. ArrayList SHOULD NOT be in the signature.
      return new ArrayList<>(Arrays.asList(things));
   }
}
