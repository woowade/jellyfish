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
package com.ngc.seaside.jellyfish.service.name.project.impl;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import com.google.common.base.Preconditions;
import com.ngc.seaside.jellyfish.api.IJellyFishCommandOptions;
import com.ngc.seaside.jellyfish.service.name.MetadataNames;
import com.ngc.seaside.jellyfish.service.name.api.IProjectInformation;
import com.ngc.seaside.jellyfish.service.name.api.IProjectNamingService;
import com.ngc.seaside.systemdescriptor.model.api.model.IModel;
import com.ngc.seaside.systemdescriptor.service.log.api.ILogService;

@Component(service = IProjectNamingService.class)
public class ProjectNamingService implements IProjectNamingService {

   private static final String GROUP_ID_PROPERTY = "groupId";
   private static final String ARTIFACT_ID_PROPERTY = "artifactId";
   private static final String DOMAIN_ARTIFACT_ID_SUFFIX = "domain";
   private static final String EVENT_ARTIFACT_ID_SUFFIX = "events";
   private static final String MESSAGES_ARTIFACT_ID_SUFFIX = "messages";
   private static final String CONNECTOR_ARTIFACT_ID_SUFFIX = "connector";
   private static final String SERVICE_ARTIFACT_ID_SUFFIX = "impl";
   private static final String SERVICE_BASE_ARTIFACT_ID_SUFFIX = "base";
   private static final String DISTRIBUTION_ARTIFACT_ID_SUFFIX = "distribution";
   private static final String CUCUMBER_TESTS_ARTIFACT_ID_SUFFIX = "tests";
   private static final String CONFIG_ARTIFACT_ID_SUFFIX = "config";
   private static final String TESTS_CONFIG_ARTIFACT_ID_SUFFIX = "testsconfig";
   private static final String PUB_SUB_BRIDGE_ARTIFACT_ID_SUFFIX = "pubsubbridge";


   /**
    * The default name of the directory that will contain generated-projects that should never be edited.
    */
   public static final String DEFAULT_GENERATED_PROJECTS_DIRECTORY_NAME = "generated-projects";

   private ILogService logService;

   @Override
   public String getRootProjectName(IJellyFishCommandOptions options, IModel model) {
      Preconditions.checkNotNull(options, "options may not be null!");
      Preconditions.checkNotNull(model, "model may not be null!");
      String modelPackageName = model.getParent().getName();
      String modelName = model.getName();
      return modelPackageName.toLowerCase() + "." + modelName.toLowerCase();
   }

   @Override
   public IProjectInformation getDomainProjectName(IJellyFishCommandOptions options, IModel model) {
      Preconditions.checkNotNull(options, "options may not be null!");
      Preconditions.checkNotNull(model, "model may not be null!");
      String modelName = model.getName();
      String versionPropertyName = modelName + "DomainVersion";
      versionPropertyName = versionPropertyName.substring(0, 1).toLowerCase() + versionPropertyName.substring(1);
      String groupId = evaluateGroupId(options, model);
      String artifactId = evaluateArtifactId(options, model, DOMAIN_ARTIFACT_ID_SUFFIX);

      return new ProjectInformation()
            .setGroupId(groupId)
            .setArtifactId(artifactId)
            .setVersionPropertyName(versionPropertyName);
   }

   @Override
   public IProjectInformation getEventsProjectName(IJellyFishCommandOptions options, IModel model) {
      Preconditions.checkNotNull(options, "options may not be null!");
      Preconditions.checkNotNull(model, "model may not be null!");
      String modelPackageName = model.getParent().getName();
      String modelName = model.getName();
      String versionPropertyName = modelName + "EventsVersion";
      versionPropertyName = versionPropertyName.substring(0, 1).toLowerCase() + versionPropertyName.substring(1);
      String artifactId = evaluateArtifactId(options, model, EVENT_ARTIFACT_ID_SUFFIX);

      return new ProjectInformation()
            .setGroupId(modelPackageName.toLowerCase())
            .setArtifactId(artifactId)
            .setVersionPropertyName(versionPropertyName)
            .setGenerated(true)
            .setGeneratedDirectoryName(DEFAULT_GENERATED_PROJECTS_DIRECTORY_NAME);
   }

   @Override
   public IProjectInformation getMessageProjectName(IJellyFishCommandOptions options, IModel model) {
      Preconditions.checkNotNull(options, "options may not be null!");
      Preconditions.checkNotNull(model, "model may not be null!");
      String modelPackageName = model.getParent().getName();
      String modelName = model.getName();
      String versionPropertyName = modelName + "MessagesVersion";
      versionPropertyName = versionPropertyName.substring(0, 1).toLowerCase() + versionPropertyName.substring(1);
      String artifactId = evaluateArtifactId(options, model, MESSAGES_ARTIFACT_ID_SUFFIX);

      return new ProjectInformation()
            .setGroupId(modelPackageName.toLowerCase())
            .setArtifactId(artifactId)
            .setVersionPropertyName(versionPropertyName)
            .setGenerated(true)
            .setGeneratedDirectoryName(DEFAULT_GENERATED_PROJECTS_DIRECTORY_NAME);
   }

   @Override
   public IProjectInformation getDistributionProjectName(IJellyFishCommandOptions options, IModel model) {
      Preconditions.checkNotNull(options, "options may not be null!");
      Preconditions.checkNotNull(model, "model may not be null!");
      String modelName = model.getName();
      String versionPropertyName = modelName + "DistributionVersion";
      versionPropertyName = versionPropertyName.substring(0, 1).toLowerCase() + versionPropertyName.substring(1);
      String groupId = evaluateGroupId(options, model);
      String artifactId = evaluateArtifactId(options, model, DISTRIBUTION_ARTIFACT_ID_SUFFIX);

      return new ProjectInformation()
            .setGroupId(groupId)
            .setArtifactId(artifactId)
            .setVersionPropertyName(versionPropertyName);
   }

   @Override
   public IProjectInformation getCucumberTestsProjectName(IJellyFishCommandOptions options, IModel model) {
      Preconditions.checkNotNull(options, "options may not be null!");
      Preconditions.checkNotNull(model, "model may not be null!");
      String modelName = model.getName();
      String versionPropertyName = modelName + "CucumberTestsVersion";
      versionPropertyName = versionPropertyName.substring(0, 1).toLowerCase() + versionPropertyName.substring(1);
      String groupId = evaluateGroupId(options, model);
      String artifactId = evaluateArtifactId(options, model, CUCUMBER_TESTS_ARTIFACT_ID_SUFFIX);

      return new ProjectInformation()
            .setGroupId(groupId)
            .setArtifactId(artifactId)
            .setVersionPropertyName(versionPropertyName);
   }

   @Override
   public IProjectInformation getConnectorProjectName(IJellyFishCommandOptions options, IModel model) {
      Preconditions.checkNotNull(options, "options may not be null!");
      Preconditions.checkNotNull(model, "model may not be null!");
      String modelPackageName = model.getParent().getName();
      String modelName = model.getName();
      String versionPropertyName = modelName + "ConnectorVersion";
      versionPropertyName = versionPropertyName.substring(0, 1).toLowerCase() + versionPropertyName.substring(1);
      String artifactId = evaluateArtifactId(options, model, CONNECTOR_ARTIFACT_ID_SUFFIX);

      return new ProjectInformation()
            .setGroupId(modelPackageName.toLowerCase())
            .setArtifactId(artifactId)
            .setVersionPropertyName(versionPropertyName)
            .setGenerated(true)
            .setGeneratedDirectoryName(DEFAULT_GENERATED_PROJECTS_DIRECTORY_NAME);
   }

   @Override
   public IProjectInformation getConfigProjectName(IJellyFishCommandOptions options, IModel model) {
      Preconditions.checkNotNull(options, "options may not be null!");
      Preconditions.checkNotNull(model, "model may not be null!");
      String modelPackageName = model.getParent().getName();
      String modelName = model.getName();
      String versionPropertyName = modelName + "ConfigVersion";
      versionPropertyName = versionPropertyName.substring(0, 1).toLowerCase() + versionPropertyName.substring(1);
      String artifactId = evaluateArtifactId(options, model, CONFIG_ARTIFACT_ID_SUFFIX);

      return new ProjectInformation()
            .setGroupId(modelPackageName.toLowerCase())
            .setArtifactId(artifactId)
            .setVersionPropertyName(versionPropertyName);
   }

   @Override
   public IProjectInformation getGeneratedConfigProjectName(IJellyFishCommandOptions options, IModel model) {
      Preconditions.checkNotNull(options, "options may not be null!");
      Preconditions.checkNotNull(model, "model may not be null!");
      String modelPackageName = model.getParent().getName();
      String modelName = model.getName();
      String versionPropertyName = modelName + "ConfigVersion";
      versionPropertyName = versionPropertyName.substring(0, 1).toLowerCase() + versionPropertyName.substring(1);
      String artifactId = evaluateArtifactId(options, model, CONFIG_ARTIFACT_ID_SUFFIX);

      return new ProjectInformation()
            .setGroupId(modelPackageName.toLowerCase())
            .setArtifactId(artifactId)
            .setVersionPropertyName(versionPropertyName)
            .setGenerated(true)
            .setGeneratedDirectoryName(DEFAULT_GENERATED_PROJECTS_DIRECTORY_NAME);
   }

   @Override
   public IProjectInformation getServiceProjectName(IJellyFishCommandOptions options, IModel model) {
      Preconditions.checkNotNull(options, "options may not be null!");
      Preconditions.checkNotNull(model, "model may not be null!");
      String modelPackageName = model.getParent().getName();
      String modelName = model.getName();
      String versionPropertyName = modelName + "ServiceVersion";
      versionPropertyName = versionPropertyName.substring(0, 1).toLowerCase() + versionPropertyName.substring(1);
      String artifactId = evaluateArtifactId(options, model, SERVICE_ARTIFACT_ID_SUFFIX);

      return new ProjectInformation()
            .setGroupId(modelPackageName.toLowerCase())
            .setArtifactId(artifactId)
            .setVersionPropertyName(versionPropertyName);
   }

   @Override
   public IProjectInformation getBaseServiceProjectName(IJellyFishCommandOptions options, IModel model) {
      Preconditions.checkNotNull(options, "options may not be null!");
      Preconditions.checkNotNull(model, "model may not be null!");
      String modelPackageName = model.getParent().getName();
      String modelName = model.getName();
      String versionPropertyName = modelName + "BaseServiceVersion";
      versionPropertyName = versionPropertyName.substring(0, 1).toLowerCase() + versionPropertyName.substring(1);
      String artifactId = evaluateArtifactId(options, model, SERVICE_BASE_ARTIFACT_ID_SUFFIX);

      return new ProjectInformation()
            .setGroupId(modelPackageName.toLowerCase())
            .setArtifactId(artifactId)
            .setVersionPropertyName(versionPropertyName)
            .setGenerated(true)
            .setGeneratedDirectoryName(DEFAULT_GENERATED_PROJECTS_DIRECTORY_NAME);
   }

   @Override
   public IProjectInformation getCucumberTestsConfigProjectName(IJellyFishCommandOptions options, IModel model) {
      Preconditions.checkNotNull(options, "options may not be null!");
      Preconditions.checkNotNull(model, "model may not be null!");
      String modelPackageName = model.getParent().getName();
      String modelName = model.getName();
      String versionPropertyName = modelName + "ConfigVersion";
      versionPropertyName = versionPropertyName.substring(0, 1).toLowerCase() + versionPropertyName.substring(1);
      String artifactId = evaluateArtifactId(options, model, TESTS_CONFIG_ARTIFACT_ID_SUFFIX);

      return new ProjectInformation()
            .setGroupId(modelPackageName.toLowerCase())
            .setArtifactId(artifactId)
            .setVersionPropertyName(versionPropertyName)
            .setGenerated(true)
            .setGeneratedDirectoryName(DEFAULT_GENERATED_PROJECTS_DIRECTORY_NAME);
   }

   @Override
   public IProjectInformation getPubSubBridgeProjectName(IJellyFishCommandOptions options, IModel model) {
      Preconditions.checkNotNull(options, "options may not be null!");
      Preconditions.checkNotNull(model, "model may not be null!");
      String modelPackageName = model.getParent().getName();
      String modelName = model.getName();
      String versionPropertyName = modelName + "PubSubVersion";
      versionPropertyName = versionPropertyName.substring(0, 1).toLowerCase() + versionPropertyName.substring(1);
      String artifactId = evaluateArtifactId(options, model, PUB_SUB_BRIDGE_ARTIFACT_ID_SUFFIX);

      return new ProjectInformation()
            .setGroupId(modelPackageName.toLowerCase())
            .setArtifactId(artifactId)
            .setVersionPropertyName(versionPropertyName)
            .setGenerated(true)
            .setGeneratedDirectoryName(DEFAULT_GENERATED_PROJECTS_DIRECTORY_NAME);
   }

   @Activate
   public void activate() {
      logService.debug(getClass(), "activated");
   }

   @Deactivate
   public void deactivate() {
      logService.debug(getClass(), "deactivated");
   }

   @Reference(cardinality = ReferenceCardinality.MANDATORY,
         policy = ReferencePolicy.STATIC)
   public void setLogService(ILogService ref) {
      this.logService = ref;
   }

   public void removeLogService(ILogService ref) {
      setLogService(null);
   }

   private String evaluateArtifactId(IJellyFishCommandOptions options, IModel model, String suffix) {
      String artifactId;
      if (options.getParameters().containsParameter(ARTIFACT_ID_PROPERTY)) {
         artifactId = options.getParameters().getParameter(ARTIFACT_ID_PROPERTY).getStringValue();
      } else {
         // Use the alias if one is provided.
         artifactId = MetadataNames.getAlias(model).orElse(model.getName().toLowerCase()) + "." + suffix;
      }
      return artifactId;
   }

   private String evaluateGroupId(IJellyFishCommandOptions options, IModel model) {
      String groupId;
      if (options.getParameters().containsParameter(GROUP_ID_PROPERTY)) {
         groupId = options.getParameters().getParameter(GROUP_ID_PROPERTY).getStringValue();
      } else {
         groupId = model.getParent().getName();
      }
      return groupId;
   }

}
