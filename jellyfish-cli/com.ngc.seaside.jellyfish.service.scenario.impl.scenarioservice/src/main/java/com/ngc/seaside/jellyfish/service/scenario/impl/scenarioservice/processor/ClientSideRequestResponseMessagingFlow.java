package com.ngc.seaside.jellyfish.service.scenario.impl.scenarioservice.processor;

import com.ngc.seaside.jellyfish.service.scenario.api.IRequestResponseMessagingFlow;
import com.ngc.seaside.jellyfish.service.scenario.correlation.api.ICorrelationDescription;
import com.ngc.seaside.systemdescriptor.model.api.model.IDataReferenceField;
import com.ngc.seaside.systemdescriptor.model.api.model.IModelReferenceField;
import com.ngc.seaside.systemdescriptor.model.api.model.scenario.IScenario;

import java.util.Optional;

public class ClientSideRequestResponseMessagingFlow implements IRequestResponseMessagingFlow {

   private IDataReferenceField input;

   private IDataReferenceField output;

   private IScenario scenario;

   private IModelReferenceField invokedServerSideComponent;

   private IScenario invokedServerSideScenario;

   @Override
   public FlowType getFlowType() {
      return FlowType.CLIENT;
   }

   @Override
   public IDataReferenceField getInput() {
      return input;
   }

   public ClientSideRequestResponseMessagingFlow setInput(IDataReferenceField input) {
      this.input = input;
      return this;
   }

   @Override
   public IDataReferenceField getOutput() {
      return output;
   }

   public ClientSideRequestResponseMessagingFlow setOutput(IDataReferenceField output) {
      this.output = output;
      return this;
   }

   @Override
   public IScenario getScenario() {
      return scenario;
   }

   public ClientSideRequestResponseMessagingFlow setScenario(IScenario scenario) {
      this.scenario = scenario;
      return this;
   }

   @Override
   public Optional<IModelReferenceField> getInvokedServerSideComponent() {
      // invokedServerSideComponent should be non-null before this object is exposed to clients.
      return Optional.of(invokedServerSideComponent);
   }


   public ClientSideRequestResponseMessagingFlow setInvokedServerSideComponent(
         IModelReferenceField invokedServerSideComponent) {
      this.invokedServerSideComponent = invokedServerSideComponent;
      return this;
   }

   @Override
   public Optional<IScenario> getInvokedServerSideScenario() {
      // invokedServerSideScenario should be non-null before this object is exposed to clients.
      return Optional.of(invokedServerSideScenario);
   }

   public ClientSideRequestResponseMessagingFlow setInvokedServerSideScenario(IScenario invokedServerSideScenario) {
      this.invokedServerSideScenario = invokedServerSideScenario;
      return this;
   }

   @Override
   public Optional<ICorrelationDescription> getCorrelationDescription() {
      // Correlation is not support for request/response flows.
      return Optional.empty();
   }
}