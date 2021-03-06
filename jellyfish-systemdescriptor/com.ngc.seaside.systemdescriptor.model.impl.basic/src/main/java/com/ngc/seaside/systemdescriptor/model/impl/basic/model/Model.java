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
package com.ngc.seaside.systemdescriptor.model.impl.basic.model;

import com.google.common.base.Preconditions;

import com.ngc.seaside.systemdescriptor.model.api.INamedChildCollection;
import com.ngc.seaside.systemdescriptor.model.api.IPackage;
import com.ngc.seaside.systemdescriptor.model.api.metadata.IMetadata;
import com.ngc.seaside.systemdescriptor.model.api.model.IDataReferenceField;
import com.ngc.seaside.systemdescriptor.model.api.model.IModel;
import com.ngc.seaside.systemdescriptor.model.api.model.IModelReferenceField;
import com.ngc.seaside.systemdescriptor.model.api.model.link.IModelLink;
import com.ngc.seaside.systemdescriptor.model.api.model.properties.IProperties;
import com.ngc.seaside.systemdescriptor.model.api.model.scenario.IScenario;
import com.ngc.seaside.systemdescriptor.model.impl.basic.NamedChildCollection;
import com.ngc.seaside.systemdescriptor.model.impl.basic.model.properties.Properties;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

/**
 * Implements the IModel interface.  Maintains all the data associated with the IModel.
 */
public class Model implements IModel {

   private final String name;
   private IPackage parent;
   private IMetadata metadata;
   private IModel refinedModel;
   private final INamedChildCollection<IModel, IDataReferenceField> inputs;
   private final INamedChildCollection<IModel, IDataReferenceField> outputs;
   private final INamedChildCollection<IModel, IModelReferenceField> requiredModels;
   private final INamedChildCollection<IModel, IModelReferenceField> parts;
   private final INamedChildCollection<IModel, IScenario> scenarios;
   private final Collection<IModelLink<?>> links;
   private IProperties properties;

   /**
    * Creates a new model.
    */
   public Model(String name) {
      Preconditions.checkNotNull(name, "name may not be null!");
      Preconditions.checkArgument(!name.trim().isEmpty(), "name may not be empty!");

      this.name = name;
      this.inputs = new NamedChildCollection<>();
      this.outputs = new NamedChildCollection<>();
      this.requiredModels = new NamedChildCollection<>();
      this.parts = new NamedChildCollection<>();
      this.scenarios = new NamedChildCollection<>();
      this.links = new ArrayList<>();
      this.properties = new Properties();
   }

   @Override
   public String getName() {
      return name;
   }

   @Override
   public IPackage getParent() {
      return parent;
   }

   @Override
   public IMetadata getMetadata() {
      return metadata;
   }

   @Override
   public Optional<IModel> getRefinedModel() {
      return Optional.ofNullable(refinedModel);
   }

   @Override
   public Model setRefinedModel(IModel model) {
      refinedModel = model;
      return this;
   }

   @Override
   public IModel setMetadata(IMetadata metadata) {
      this.metadata = metadata;
      return this;
   }

   @Override
   public INamedChildCollection<IModel, IDataReferenceField> getInputs() {
      return inputs;
   }

   @Override
   public INamedChildCollection<IModel, IDataReferenceField> getOutputs() {
      return outputs;
   }

   @Override
   public INamedChildCollection<IModel, IModelReferenceField> getRequiredModels() {
      return requiredModels;
   }

   @Override
   public INamedChildCollection<IModel, IModelReferenceField> getParts() {
      return parts;
   }

   @Override
   public INamedChildCollection<IModel, IScenario> getScenarios() {
      return scenarios;
   }

   @Override
   public Collection<IModelLink<?>> getLinks() {
      return links;
   }

   @Override
   public Optional<IModelLink<?>> getLinkByName(String name) {
      Preconditions.checkNotNull(name, "name may not be null!");
      Preconditions.checkArgument(!name.trim().isEmpty(), "name may not be empty!");
      return links.stream()
            .filter(link -> name.equals(link.getName().orElse(null)))
            .findFirst();
   }

   @Override
   public String getFullyQualifiedName() {
      return String.format("%s%s%s",
                           parent == null ? "" : parent.getName(),
                           parent == null ? "" : ".",
                           name);
   }

   public Model setParent(IPackage p) {
      parent = p;
      return this;
   }

   public Model addInput(IDataReferenceField input) {
      inputs.add(input);
      return this;
   }

   public Model addOutput(IDataReferenceField output) {
      outputs.add(output);
      return this;
   }

   public Model addRequiredModel(IModelReferenceField model) {
      requiredModels.add(model);
      return this;
   }

   public Model addPart(IModelReferenceField part) {
      parts.add(part);
      return this;
   }

   public Model addScenario(IScenario scenario) {
      scenarios.add(scenario);
      return this;
   }

   public Model addLink(IModelLink<?> link) {
      links.add(link);
      return this;
   }

   @Override
   public IProperties getProperties() {
      return properties;
   }

   @Override
   public IModel setProperties(IProperties properties) {
      this.properties = properties;
      return this;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      }
      if (!(o instanceof Model)) {
         return false;
      }
      Model model = (Model) o;
      return Objects.equals(name, model.name)
             && parent == model.parent
             && refinedModel == model.refinedModel
             && Objects.equals(metadata, model.metadata)
             && Objects.equals(inputs, model.inputs)
             && Objects.equals(outputs, model.outputs)
             && Objects.equals(requiredModels, model.requiredModels)
             && Objects.equals(parts, model.parts)
             && Objects.equals(scenarios, model.scenarios)
             && Objects.equals(links, model.links)
             && Objects.equals(properties, model.properties);
   }

   @Override
   public int hashCode() {
      return Objects.hash(name, System.identityHashCode(parent),
                          metadata, inputs, outputs, requiredModels, parts, scenarios, links, properties);
   }

   @Override
   public String toString() {
      return "Model["
             + "name='" + name + '\''
             + ", parent=" + (parent == null ? "null" : parent.getName())
             + ", refinedModel=" + (refinedModel == null ? "null" : refinedModel.getName())
             + ", metadata=" + metadata
             + ", inputs=" + inputs
             + ", outputs=" + outputs
             + ", requiredModels=" + requiredModels
             + ", parts=" + parts
             + ", scenarios=" + scenarios
             + ", links=" + links
             + ", properties=" + properties
             + ']';
   }
}
