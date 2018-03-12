package com.ngc.seaside.systemdescriptor.model.impl.view;

import com.google.common.base.Preconditions;

import com.ngc.seaside.systemdescriptor.model.api.INamedChild;
import com.ngc.seaside.systemdescriptor.model.api.INamedChildCollection;
import com.ngc.seaside.systemdescriptor.model.api.IPackage;
import com.ngc.seaside.systemdescriptor.model.api.metadata.IMetadata;
import com.ngc.seaside.systemdescriptor.model.api.model.IDataReferenceField;
import com.ngc.seaside.systemdescriptor.model.api.model.IModel;
import com.ngc.seaside.systemdescriptor.model.api.model.IModelReferenceField;
import com.ngc.seaside.systemdescriptor.model.api.model.link.IModelLink;
import com.ngc.seaside.systemdescriptor.model.api.model.scenario.IScenario;
import com.ngc.seaside.systemdescriptor.model.impl.basic.NamedChildCollection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;

/**
 * Provides an aggregated view of a model by taking into account the model's refinement hierarchy.
 */
public class AggregatedModelView implements IModel {

   private final IModel wrapped;
   private final INamedChildCollection<IModel, IDataReferenceField> aggregatedInputs;
   private final INamedChildCollection<IModel, IDataReferenceField> aggregatedOutputs;
   private final INamedChildCollection<IModel, IModelReferenceField> aggregatedParts;
   private final INamedChildCollection<IModel, IModelReferenceField> aggregatedRequirements;
   private final INamedChildCollection<IModel, IScenario> aggregatedScenarios;
   private final Collection<IModelLink<?>> aggregatedLinks;
   private IMetadata aggregatedMetadata;

   public AggregatedModelView(IModel wrapped) {
      this.wrapped = Preconditions.checkNotNull(wrapped, "wrapped may not be null!");
      this.aggregatedInputs = getAggregatedFields(IModel::getInputs);
      this.aggregatedOutputs = getAggregatedFields(IModel::getOutputs);
      this.aggregatedParts = getAggregatedFields(IModel::getParts);
      this.aggregatedRequirements = getAggregatedFields(IModel::getRequiredModels);
      this.aggregatedScenarios = getAggregatedFields(IModel::getScenarios);
      this.aggregatedLinks = getAggregatedLinks();
      this.aggregatedMetadata = AggregatedMetadataView.getAggregatedMetadata(wrapped);
   }

   @Override
   public IMetadata getMetadata() {
      return aggregatedMetadata;
   }

   @Override
   public IModel setMetadata(IMetadata metadata) {
      wrapped.setMetadata(metadata);
      aggregatedMetadata = AggregatedMetadataView.getAggregatedMetadata(wrapped);
      return this;
   }

   @Override
   public INamedChildCollection<IModel, IDataReferenceField> getInputs() {
      return aggregatedInputs;
   }

   @Override
   public INamedChildCollection<IModel, IDataReferenceField> getOutputs() {
      return aggregatedOutputs;
   }

   @Override
   public INamedChildCollection<IModel, IModelReferenceField> getRequiredModels() {
      return aggregatedRequirements;
   }

   @Override
   public INamedChildCollection<IModel, IModelReferenceField> getParts() {
      return aggregatedParts;
   }

   @Override
   public INamedChildCollection<IModel, IScenario> getScenarios() {
      return aggregatedScenarios;
   }

   @Override
   public Collection<IModelLink<?>> getLinks() {
      return aggregatedLinks;
   }

   @Override
   public Optional<IModelLink<?>> getLinkByName(String name) {
      Preconditions.checkNotNull(name, "name may not be null!");
      Preconditions.checkState(!name.trim().isEmpty(), "name may not be empty!");
      return aggregatedLinks
            .stream()
            .filter(link -> name.equals(link.getName().orElse(null)))
            .findFirst();
   }

   @Override
   public Optional<IModel> getRefinedModel() {
      return wrapped.getRefinedModel();
   }

   @Override
   public IModel setRefinedModel(IModel refinedModel) {
      wrapped.setRefinedModel(refinedModel);
      return this;
   }

   @Override
   public String getFullyQualifiedName() {
      return wrapped.getFullyQualifiedName();
   }

   @Override
   public String getName() {
      return wrapped.getName();
   }

   @Override
   public IPackage getParent() {
      return wrapped.getParent();
   }

   @Override
   public String toString() {
      return wrapped.toString();
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      }
      if (o instanceof AggregatedModelView) {
         return wrapped.equals(((AggregatedModelView) o).wrapped);
      }
      return o instanceof IModel && wrapped.equals(o);
   }

   @Override
   public int hashCode() {
      return wrapped.hashCode();
   }

   private <T extends INamedChild<IModel>> INamedChildCollection<IModel, T> getAggregatedFields(
         Function<IModel, INamedChildCollection<IModel, T>> fieldFinder) {
      NamedChildCollection<IModel, T> collection = new NamedChildCollection<>();
      IModel model = wrapped;
      while (model != null) {
         collection.addAll(fieldFinder.apply(model));
         model = model.getRefinedModel().orElse(null);
      }
      return collection;
   }

   private Collection<IModelLink<?>> getAggregatedLinks() {
      Collection<IModelLink<?>> collection = new ArrayList<>();
      IModel model = wrapped;
      while (model != null) {
         collection.addAll(model.getLinks());
         model = model.getRefinedModel().orElse(null);
      }
      return collection;
   }
}
