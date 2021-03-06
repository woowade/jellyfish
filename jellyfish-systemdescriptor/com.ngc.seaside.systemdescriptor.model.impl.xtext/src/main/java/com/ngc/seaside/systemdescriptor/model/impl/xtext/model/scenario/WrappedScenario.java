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
package com.ngc.seaside.systemdescriptor.model.impl.xtext.model.scenario;

import com.google.common.base.Preconditions;

import com.ngc.seaside.systemdescriptor.model.api.metadata.IMetadata;
import com.ngc.seaside.systemdescriptor.model.api.model.IModel;
import com.ngc.seaside.systemdescriptor.model.api.model.scenario.IScenario;
import com.ngc.seaside.systemdescriptor.model.api.model.scenario.IScenarioStep;
import com.ngc.seaside.systemdescriptor.model.impl.xtext.AbstractWrappedXtext;
import com.ngc.seaside.systemdescriptor.model.impl.xtext.collection.AutoWrappingCollection;
import com.ngc.seaside.systemdescriptor.model.impl.xtext.collection.SelfInitializingAutoWrappingCollection;
import com.ngc.seaside.systemdescriptor.model.impl.xtext.metadata.WrappedMetadata;
import com.ngc.seaside.systemdescriptor.model.impl.xtext.store.IWrapperResolver;
import com.ngc.seaside.systemdescriptor.systemDescriptor.Model;
import com.ngc.seaside.systemdescriptor.systemDescriptor.Scenario;
import com.ngc.seaside.systemdescriptor.systemDescriptor.SystemDescriptorFactory;

import java.util.Collection;

/**
 * Adapts a {@link Scenario} instance to {@link IScenario}.
 * This class is not threadsafe.
 */
public class WrappedScenario extends AbstractWrappedXtext<Scenario> implements IScenario {

   // Thread safety note: Absolutely no part of this implementation is thread safe.

   private IMetadata metadata;
   private Collection<IScenarioStep> givens;
   private Collection<IScenarioStep> whens;
   private Collection<IScenarioStep> thens;

   /**
    * Creates a new wrapped scenario.
    */
   public WrappedScenario(IWrapperResolver resolver, Scenario wrapped) {
      super(resolver, wrapped);
      this.metadata = WrappedMetadata.fromXtext(wrapped.getMetadata());

      // This next code looks terrible because we have to handle two cases when dealing with scenarios:
      // 1) The first case is easy.  The scenario has existing given, when, etc steps.  This means that
      // wrapped.getGiven(), wrapped.getWhen(), etc will not return null.  In this case, we just need to wrap the steps
      // as is, so we use the AutoWrappingCollection to wrap and unwrap on demand.
      // 2) The second case is harder.  If the scenario is empty (which is valid), wrapped.getGiven(), etc will return
      // null.  In this case we need to use the ScenarioInitializingCollection.  This collection will basically set a
      // new instance of a GivenDeclaration, WhenDeclaration, etc, when a new IScenarioStep is added to the collection
      // for the first time.  We can't just add empty declaration at construction time because that results in an
      // invalid model which XText won't accept.

      if (wrapped.getGiven() == null) {
         givens = new SelfInitializingAutoWrappingCollection<>(
               s -> new WrappedScenarioStep<>(resolver, s),
               AutoWrappingCollection.defaultUnwrapper(),
               () -> {
                  // On the first add, create the GivenDeclaration and make the collection start wrapping the EList
                  // within the declaration.
                  wrapped.setGiven(SystemDescriptorFactory.eINSTANCE.createGivenDeclaration());
                  return wrapped.getGiven().getSteps();
               });
      } else {
         // Otherwise, just wrap the steps that are in the existing declaration.
         givens = new AutoWrappingCollection<>(
               wrapped.getGiven().getSteps(),
               s -> new WrappedScenarioStep<>(resolver, s),
               AutoWrappingCollection.defaultUnwrapper());
      }

      if (wrapped.getWhen() == null) {
         whens = new SelfInitializingAutoWrappingCollection<>(
               s -> new WrappedScenarioStep<>(resolver, s),
               AutoWrappingCollection.defaultUnwrapper(),
               () -> {
                  // On the first add, create the WhenDeclaration and make the collection start wrapping the EList
                  // within the declaration.
                  wrapped.setWhen(SystemDescriptorFactory.eINSTANCE.createWhenDeclaration());
                  return wrapped.getWhen().getSteps();
               });
      } else {
         // Otherwise, just wrap the steps that are in the existing declaration.
         whens = new AutoWrappingCollection<>(
               wrapped.getWhen().getSteps(),
               s -> new WrappedScenarioStep<>(resolver, s),
               AutoWrappingCollection.defaultUnwrapper());
      }

      if (wrapped.getThen() == null) {
         thens = new SelfInitializingAutoWrappingCollection<>(
               s -> new WrappedScenarioStep<>(resolver, s),
               AutoWrappingCollection.defaultUnwrapper(),
               () -> {
                  // On the first add, create the ThenDeclaration and make the collection start wrapping the EList
                  // within the declaration.
                  wrapped.setThen(SystemDescriptorFactory.eINSTANCE.createThenDeclaration());
                  return wrapped.getThen().getSteps();
               });
      } else {
         // Otherwise, just wrap the steps that are in the existing declaration.
         thens = new AutoWrappingCollection<>(
               wrapped.getThen().getSteps(),
               s -> new WrappedScenarioStep<>(resolver, s),
               AutoWrappingCollection.defaultUnwrapper());
      }
   }

   @Override
   public IMetadata getMetadata() {
      return metadata;
   }

   @Override
   public IScenario setMetadata(IMetadata metadata) {
      Preconditions.checkNotNull(metadata, "metadata may not be null!");
      this.metadata = metadata;
      wrapped.setMetadata(WrappedMetadata.toXtext(metadata));
      return this;
   }

   @Override
   public Collection<IScenarioStep> getGivens() {
      return givens;
   }

   @Override
   public Collection<IScenarioStep> getWhens() {
      return whens;
   }

   @Override
   public Collection<IScenarioStep> getThens() {
      return thens;
   }

   @Override
   public String getName() {
      return wrapped.getName();
   }

   @Override
   public IModel getParent() {
      return resolver.getWrapperFor((Model) wrapped.eContainer());
   }

   /**
    * Coverts the given object to an XText object.
    */
   public static Scenario toXtextScenario(IScenario scenario) {
      Scenario s = SystemDescriptorFactory.eINSTANCE.createScenario();
      s.setName(scenario.getName());
      s.setMetadata(WrappedMetadata.toXtext(scenario.getMetadata()));
      s.setGiven(SystemDescriptorFactory.eINSTANCE.createGivenDeclaration());
      s.setWhen(SystemDescriptorFactory.eINSTANCE.createWhenDeclaration());
      s.setThen(SystemDescriptorFactory.eINSTANCE.createThenDeclaration());
      scenario.getGivens()
            .stream()
            .map(WrappedScenarioStep::toXtextGivenStep)
            .forEach(s.getGiven().getSteps()::add);
      scenario.getWhens()
            .stream()
            .map(WrappedScenarioStep::toXtextWhenStep)
            .forEach(s.getWhen().getSteps()::add);
      scenario.getThens()
            .stream()
            .map(WrappedScenarioStep::toXtextThenStep)
            .forEach(s.getThen().getSteps()::add);
      return s;
   }
}
