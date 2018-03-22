package com.ngc.seaside.jellyfish.cli.command.createjavaprotobufconnector;

import com.ngc.seaside.jellyfish.api.CommonParameters;
import com.ngc.seaside.jellyfish.api.DefaultParameter;
import com.ngc.seaside.jellyfish.api.DefaultParameterCollection;
import com.ngc.seaside.jellyfish.api.DefaultUsage;
import com.ngc.seaside.jellyfish.api.IJellyFishCommandOptions;
import com.ngc.seaside.jellyfish.api.IUsage;
import com.ngc.seaside.jellyfish.cli.command.createjavaprotobufconnector.dto.ConnectorDto;
import com.ngc.seaside.jellyfish.service.codegen.api.IDataFieldGenerationService;
import com.ngc.seaside.jellyfish.service.codegen.api.IJavaServiceGenerationService;
import com.ngc.seaside.jellyfish.service.codegen.api.dto.EnumDto;
import com.ngc.seaside.jellyfish.service.config.api.ITransportConfigurationService;
import com.ngc.seaside.jellyfish.service.name.api.IProjectInformation;
import com.ngc.seaside.jellyfish.service.requirements.api.IRequirementsService;
import com.ngc.seaside.jellyfish.service.scenario.api.IPublishSubscribeMessagingFlow;
import com.ngc.seaside.jellyfish.service.scenario.api.IRequestResponseMessagingFlow;
import com.ngc.seaside.jellyfish.service.scenario.api.IScenarioService;
import com.ngc.seaside.jellyfish.utilities.command.AbstractMultiphaseJellyfishCommand;
import com.ngc.seaside.systemdescriptor.model.api.INamedChild;
import com.ngc.seaside.systemdescriptor.model.api.IPackage;
import com.ngc.seaside.systemdescriptor.model.api.data.IData;
import com.ngc.seaside.systemdescriptor.model.api.data.IDataField;
import com.ngc.seaside.systemdescriptor.model.api.data.IEnumeration;
import com.ngc.seaside.systemdescriptor.model.api.model.IDataReferenceField;
import com.ngc.seaside.systemdescriptor.model.api.model.IModel;
import com.ngc.seaside.systemdescriptor.model.api.model.scenario.IScenario;

import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;

public class CreateJavaProtobufConnectorCommand extends AbstractMultiphaseJellyfishCommand {

   private static final String NAME = "create-java-protobuf-connector";
   static final String PUBSUB_GENBUILD_TEMPLATE_SUFFIX = "genbuild";
   static final String PUBSUB_BUILD_TEMPLATE_SUFFIX = "build";

   private static final Function<IData, Collection<IDataField>> FIELDS_FUNCTION = data -> {
      Set<IDataField> fields = new LinkedHashSet<>();
      while (data != null) {
         fields.addAll(data.getFields());
         data = data.getExtendedDataType().orElse(null);
      }
      return fields;
   };

   private IScenarioService scenarioService;
   private ITransportConfigurationService transportConfigService;
   private IRequirementsService requirementsService;
   private IJavaServiceGenerationService generationService;
   private IDataFieldGenerationService dataFieldService;

   public CreateJavaProtobufConnectorCommand() {
      super(NAME);
   }

   @Override
   public void activate() {
      logService.trace(getClass(), "Activated");
   }

   @Override
   public void deactivate() {
      logService.trace(getClass(), "Deactivated");
   }

   public void setScenarioService(IScenarioService ref) {
      this.scenarioService = ref;
   }

   public void removeScenarioService(IScenarioService ref) {
      setLogService(null);
   }

   public void setTransportConfigurationService(ITransportConfigurationService ref) {
      this.transportConfigService = ref;
   }

   public void removeTransportConfigurationService(ITransportConfigurationService ref) {
      setTransportConfigurationService(null);
   }

   public void setRequirementsService(IRequirementsService ref) {
      this.requirementsService = ref;
   }

   public void removeRequirementsService(IRequirementsService ref) {
      setRequirementsService(null);
   }

   public void setJavaServiceGenerationService(IJavaServiceGenerationService ref) {
      this.generationService = ref;
   }

   public void removeJavaServiceGenerationService(IJavaServiceGenerationService ref) {
      setJavaServiceGenerationService(null);
   }

   public void setDataFieldGenerationService(IDataFieldGenerationService ref) {
      this.dataFieldService = ref;
   }

   public void removeDataFieldGenerationService(IDataFieldGenerationService ref) {
      setDataFieldGenerationService(null);
   }

   @Override
   protected IUsage createUsage() {
      return new DefaultUsage(
            "Creates a new JellyFish Pub/Sub Connector project.",
            CommonParameters.OUTPUT_DIRECTORY.required(),
            CommonParameters.GROUP_ID,
            CommonParameters.ARTIFACT_ID,
            CommonParameters.MODEL.required(),
            CommonParameters.CLEAN,
            CommonParameters.UPDATE_GRADLE_SETTING,
            allPhasesParameter());
   }

   @Override
   protected void runDefaultPhase() {
      IModel model = getModel();
      Path outputDirectory = getOutputDirectory();
      boolean clean = getBooleanParameter(CommonParameters.CLEAN.getName());

      IProjectInformation info = projectNamingService.getConnectorProjectName(getOptions(), model);
      ConnectorDto dto = new ConnectorDto();
      dto.setProjectName(info.getDirectoryName());

      DefaultParameterCollection parameters = new DefaultParameterCollection();
      parameters.addParameter(new DefaultParameter<>("dto", dto));
      unpackSuffixedTemplate(PUBSUB_BUILD_TEMPLATE_SUFFIX, parameters, outputDirectory, clean);
      buildManagementService.registerProject(getOptions(), info);
   }

   @Override
   protected void runDeferredPhase() {
      final Path outputDirectory = getOutputDirectory();
      final IModel model = getModel();
      final boolean clean = getBooleanParameter(CommonParameters.CLEAN.getName());

      IProjectInformation info = projectNamingService.getConnectorProjectName(getOptions(), model);
      final String packageName = packageNamingService.getConnectorPackageName(getOptions(), model);

      ConnectorDto dto = new ConnectorDto();
      dto.setProjectName(info.getDirectoryName());
      dto.setPackageName(packageName);
      dto.setModel(model);
      dto.setOptions(getOptions());
      dto.setPackageService(packageNamingService);
      dto.setDataFieldService(dataFieldService);
      dto.setFields(FIELDS_FUNCTION);

      EnumDto transportTopics = generationService.getTransportTopicsDescription(getOptions(), model);
      dto.setTransportTopicsClass(transportTopics.getFullyQualifiedName());

      dto.setServiceInterface(generationService.getServiceInterfaceDescription(getOptions(), model));

      dto.setProjectDependencies(new LinkedHashSet<>(
            Arrays.asList(projectNamingService.getBaseServiceProjectName(getOptions(), model).getArtifactId(),
                          projectNamingService.getEventsProjectName(getOptions(), model).getArtifactId(),
                          projectNamingService.getMessageProjectName(getOptions(), model).getArtifactId())));

      evaluatePubSubIo(getOptions(), dto);
      evaluateReqResIo(getOptions(), dto);

      DefaultParameterCollection parameters = new DefaultParameterCollection();
      parameters.addParameter(new DefaultParameter<>("dto", dto));
      parameters.addParameter(new DefaultParameter<>("IData", IData.class));
      parameters.addParameter(new DefaultParameter<>("IEnumeration", IEnumeration.class));

      unpackSuffixedTemplate(PUBSUB_GENBUILD_TEMPLATE_SUFFIX, parameters, outputDirectory, clean);

      logService.info(CreateJavaProtobufConnectorCommand.class,
                      "%s project successfully created",
                      model.getFullyQualifiedName());
   }

   private void evaluatePubSubIo(IJellyFishCommandOptions options, ConnectorDto dto) {
      IModel model = dto.getModel();

      final Set<String> modelRequirements = requirementsService.getRequirements(options, model);
      for (IScenario scenario : model.getScenarios()) {
         final Set<String> scenarioRequirements = requirementsService.getRequirements(options, scenario);

         Optional<IPublishSubscribeMessagingFlow> optionalFlow =
               scenarioService.getPubSubMessagingFlow(options, scenario);
         if (optionalFlow.isPresent()) {
            IPublishSubscribeMessagingFlow flow = optionalFlow.get();

            for (IDataReferenceField input : flow.getInputs()) {
               final IData type = input.getType();
               dto.getAllInputs().add(type);

               final String topic = transportConfigService.getTransportTopicName(flow, input);
               IData previous = dto.getInputTopics().put(topic, type);
               if (previous != null && !previous.equals(type)) {
                  throw new IllegalStateException(String.format("Conflicting data types for topic <%s>: %s and %s",
                                                                topic,
                                                                previous.getClass(),
                                                                type.getClass()));
               }

               final Set<String> requirements = dto.getTopicRequirements().computeIfAbsent(topic, t -> new TreeSet<>());
               requirements.addAll(requirementsService.getRequirements(options, input));
               requirements.addAll(requirementsService.getRequirements(options, type));
               requirements.addAll(scenarioRequirements);
               requirements.addAll(modelRequirements);
            }

            for (IDataReferenceField output : flow.getOutputs()) {
               final IData type = output.getType();
               dto.getAllOutputs().add(type);

               final String topic = transportConfigService.getTransportTopicName(flow, output);
               IData previous = dto.getInputTopics().put(topic, type);
               if (previous != null && !previous.equals(type)) {
                  throw new IllegalStateException(String.format("Conflicting data types for topic <%s>: %s and %s",
                                                                topic,
                                                                previous.getClass(),
                                                                type.getClass()));
               }

               final Set<String> requirements = dto.getTopicRequirements().computeIfAbsent(topic, t -> new TreeSet<>());
               requirements.addAll(requirementsService.getRequirements(options, output));
               requirements.addAll(requirementsService.getRequirements(options, type));
               requirements.addAll(scenarioRequirements);
               requirements.addAll(modelRequirements);
            }
         }
      }

      addNestedData(dto.getAllInputs());
      addNestedData(dto.getAllOutputs());
   }

   private void evaluateReqResIo(IJellyFishCommandOptions options, ConnectorDto dto) {
      IModel model = dto.getModel();

      final Set<String> modelRequirements = requirementsService.getRequirements(options, model);
      for (IScenario scenario : model.getScenarios()) {
         final Set<String> scenarioRequirements = requirementsService.getRequirements(options, scenario);

         Optional<IRequestResponseMessagingFlow> optionalFlow =
               scenarioService.getRequestResponseMessagingFlows(options, scenario);
         if (optionalFlow.isPresent()) {
            dto.setRequiresInjectedService(true);

            IRequestResponseMessagingFlow flow = optionalFlow.get();

            IData type = flow.getInput().getType();
            dto.getAllInputs().add(type);

            String topic = transportConfigService.getTransportTopicName(flow, flow.getInput());
            IData previous = dto.getInputTopics().put(topic, type);
            if (previous != null && !previous.equals(type)) {
               throw new IllegalStateException(String.format("Conflicting data types for topic <%s>: %s and %s",
                                                             topic,
                                                             previous.getClass(),
                                                             type.getClass()));
            }

            Set<String> requirements = dto.getTopicRequirements().computeIfAbsent(topic, t -> new TreeSet<>());
            requirements.addAll(requirementsService.getRequirements(options, flow.getInput()));
            requirements.addAll(requirementsService.getRequirements(options, type));
            requirements.addAll(scenarioRequirements);
            requirements.addAll(modelRequirements);

            type = flow.getOutput().getType();
            dto.getAllOutputs().add(type);

            topic = transportConfigService.getTransportTopicName(flow, flow.getOutput());
            previous = dto.getInputTopics().put(topic, type);
            if (previous != null && !previous.equals(type)) {
               throw new IllegalStateException(String.format("Conflicting data types for topic <%s>: %s and %s",
                                                             topic,
                                                             previous.getClass(),
                                                             type.getClass()));
            }

            requirements = dto.getTopicRequirements().computeIfAbsent(topic, t -> new TreeSet<>());
            requirements.addAll(requirementsService.getRequirements(options, flow.getOutput()));
            requirements.addAll(requirementsService.getRequirements(options, type));
            requirements.addAll(scenarioRequirements);
            requirements.addAll(modelRequirements);
         }
      }

      addNestedData(dto.getAllInputs());
      addNestedData(dto.getAllOutputs());
   }

   private static void addNestedData(Set<INamedChild<IPackage>> data) {
      Queue<INamedChild<IPackage>> queue = new ArrayDeque<>(data);
      Set<IData> superTypes = new LinkedHashSet<>();
      while (!queue.isEmpty()) {
         INamedChild<IPackage> value = queue.poll();
         IData datum;
         if (value instanceof IData) {
            datum = (IData) value;
         } else {
            continue;
         }
         for (IDataField field : datum.getFields()) {
            switch (field.getType()) {
               case DATA:
                  if (data.add(field.getReferencedDataType())) {
                     queue.add(field.getReferencedDataType());
                  }
                  break;
               case ENUM:
                  data.add(field.getReferencedEnumeration());
                  break;
               default:
                  // Ignore primitive field types
                  break;
            }
         }

         while (datum.getExtendedDataType().isPresent()) {
            datum = datum.getExtendedDataType().get();
            if (superTypes.add(datum)) {
               queue.add(datum);
            }
         }
      }
   }
}
