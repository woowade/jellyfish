package com.ngc.seaside.systemdescriptor.service.impl.xtext.validation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.ngc.blocs.service.log.api.ILogService;
import com.ngc.seaside.systemdescriptor.extension.IValidatorExtension;
import com.ngc.seaside.systemdescriptor.model.api.IPackage;
import com.ngc.seaside.systemdescriptor.model.api.ISystemDescriptor;
import com.ngc.seaside.systemdescriptor.model.api.data.IData;
import com.ngc.seaside.systemdescriptor.model.api.data.IDataField;
import com.ngc.seaside.systemdescriptor.model.api.model.IDataReferenceField;
import com.ngc.seaside.systemdescriptor.model.api.model.IModel;
import com.ngc.seaside.systemdescriptor.model.api.model.IModelReferenceField;
import com.ngc.seaside.systemdescriptor.model.api.model.link.IModelLink;
import com.ngc.seaside.systemdescriptor.model.api.model.scenario.IScenario;
import com.ngc.seaside.systemdescriptor.model.api.model.scenario.IScenarioStep;
import com.ngc.seaside.systemdescriptor.model.impl.xtext.IUnwrappable;
import com.ngc.seaside.systemdescriptor.model.impl.xtext.WrappedSystemDescriptor;
import com.ngc.seaside.systemdescriptor.model.impl.xtext.exception.UnrecognizedXtextTypeException;
import com.ngc.seaside.systemdescriptor.systemDescriptor.BasePartDeclaration;
import com.ngc.seaside.systemdescriptor.systemDescriptor.Data;
import com.ngc.seaside.systemdescriptor.systemDescriptor.DataModel;
import com.ngc.seaside.systemdescriptor.systemDescriptor.InputDeclaration;
import com.ngc.seaside.systemdescriptor.systemDescriptor.LinkDeclaration;
import com.ngc.seaside.systemdescriptor.systemDescriptor.Model;
import com.ngc.seaside.systemdescriptor.systemDescriptor.OutputDeclaration;
import com.ngc.seaside.systemdescriptor.systemDescriptor.Package;
import com.ngc.seaside.systemdescriptor.systemDescriptor.PartDeclaration;
import com.ngc.seaside.systemdescriptor.systemDescriptor.PrimitiveDataFieldDeclaration;
import com.ngc.seaside.systemdescriptor.systemDescriptor.ReferencedDataModelFieldDeclaration;
import com.ngc.seaside.systemdescriptor.systemDescriptor.RefinedPartDeclaration;
import com.ngc.seaside.systemdescriptor.systemDescriptor.RequireDeclaration;
import com.ngc.seaside.systemdescriptor.systemDescriptor.Scenario;
import com.ngc.seaside.systemdescriptor.systemDescriptor.Step;
import com.ngc.seaside.systemdescriptor.systemDescriptor.SystemDescriptorPackage;
import com.ngc.seaside.systemdescriptor.validation.SystemDescriptorValidator;
import com.ngc.seaside.systemdescriptor.validation.api.ISystemDescriptorValidator;
import com.ngc.seaside.systemdescriptor.validation.api.IValidationContext;

/**
 * Handles validation related concerns.  This implementation will log and consume an exceptions generated by a
 * registered {@code ISystemDescriptorValidator}.  This prevents a misbehaving validator from halting parsing.
 */
public class ValidationDelegate implements IValidatorExtension {

   /**
    * All registered validators.
    */
   private final Collection<ISystemDescriptorValidator> validators = Collections.synchronizedList(new ArrayList<>());

   /**
    * The DSL validator.
    */
   private final SystemDescriptorValidator validator;

   /**
    * Used for logging.
    */
   private final ILogService logService;

   @Inject
   public ValidationDelegate(SystemDescriptorValidator validator,
                             ILogService logService,
                             ValidatorsHolder validatorsHolder) {
      this.validator = Preconditions.checkNotNull(validator, "validator may not be null!");
      this.logService = Preconditions.checkNotNull(logService, "logService may not be null!");
      // Register all injected validators.
      validatorsHolder.validators.forEach(this::addValidator);
   }

   @Override
   public void validate(EObject source, ValidationHelper helper) {
      // Walk the source object up the containment hierarchy to find the Package object.  Build a system descriptor
      // for the entire package.  Then instruct the validator to validate associated wrapper of the source object.
      ISystemDescriptor descriptor = new WrappedSystemDescriptor(findPackage(source));
      doValidate(source, helper, descriptor);
   }

   public void addValidator(ISystemDescriptorValidator validator) {
      Preconditions.checkNotNull(validator, "validator may not be null!");
      logService.debug(getClass(), "Adding validator %s.", validator);

      // Synchronize to ensure the add and size calls are safe.
      synchronized (validators) {
         validators.add(validator);
         // If this is the first validator added, register our self.
         if (validators.size() == 1) {
            registerSelf();
         }
      }
   }

   public boolean removeValidator(ISystemDescriptorValidator validator) {
      Preconditions.checkNotNull(validator, "validator may not be null!");
      boolean result;
      // Synchronize to ensure the remove and isEmpty calls are safe.
      synchronized (validators) {
         result = validators.remove(validator);
         if (result && validators.isEmpty()) {
            logService.debug(getClass(), "Removed validator %s.", validator);
            // If there are no validators, unregister our self with the DSL.
            unregisterSelf();
         }
      }
      return result;
   }

   protected void doValidate(EObject source, ValidationHelper helper, ISystemDescriptor descriptor) {
      switch (source.eClass().getClassifierID()) {
         case SystemDescriptorPackage.PACKAGE:
            String name = ((Package) source).getName();
            IValidationContext<IPackage> ctx1 = newContext(
                  descriptor.getPackages()
                        .getByName(name)
                        .orElseThrow(() -> new IllegalStateException("failed to find wrapper for package " + name)),
                  helper);
            doValidation(ctx1);
            break;
         case SystemDescriptorPackage.DATA:
            String packageName = ((Package) source.eContainer()).getName();
            IValidationContext<IData> ctx2 = newContext(
                  descriptor.findData(packageName, ((Data) source).getName()).get(),
                  helper);
            doValidation(ctx2);
            break;
         case SystemDescriptorPackage.PRIMITIVE_DATA_FIELD_DECLARATION:
            String fieldName = ((PrimitiveDataFieldDeclaration) source).getName();
            String dataName = ((Data) source.eContainer()).getName();
            packageName = ((Package) source.eContainer().eContainer()).getName();
            IValidationContext<IDataField> ctx3 = newContext(
                  descriptor.findData(packageName, dataName).get()
                        .getFields()
                        .getByName(fieldName)
                        .get(),
                  helper);
            doValidation(ctx3);
            break;
         case SystemDescriptorPackage.REFERENCED_DATA_MODEL_FIELD_DECLARATION:
            validateReferenceDataField((ReferencedDataModelFieldDeclaration) source, helper, descriptor);
            break;
         case SystemDescriptorPackage.MODEL:
            packageName = ((Package) source.eContainer()).getName();
            IValidationContext<IModel> ctx4 = newContext(
                  descriptor.findModel(packageName, ((Model) source).getName()).get(),
                  helper);
            doValidation(ctx4);
            break;
         case SystemDescriptorPackage.INPUT_DECLARATION:
            fieldName = ((InputDeclaration) source).getName();
            String modelName = ((Model) source.eContainer().eContainer()).getName();
            packageName = ((Package) source.eContainer().eContainer().eContainer()).getName();
            IValidationContext<IDataReferenceField> ctx5 = newContext(
                  descriptor.findModel(packageName, modelName)
                        .orElseThrow(() -> new IllegalStateException("failed to find wrapper for model " + modelName))
                        .getInputs()
                        .getByName(fieldName)
                        .get(),
                  helper);
            doValidation(ctx5);
            break;
         case SystemDescriptorPackage.OUTPUT_DECLARATION:
            fieldName = ((OutputDeclaration) source).getName();
            modelName = ((Model) source.eContainer().eContainer()).getName();
            packageName = ((Package) source.eContainer().eContainer().eContainer()).getName();
            IValidationContext<IDataReferenceField> ctx6 = newContext(
                  descriptor.findModel(packageName, modelName).get()
                        .getOutputs()
                        .getByName(fieldName)
                        .get(),
                  helper);
            doValidation(ctx6);
            break;
         case SystemDescriptorPackage.PART_DECLARATION:
            fieldName = ((PartDeclaration) source).getName();
            modelName = ((Model) source.eContainer().eContainer()).getName();
            packageName = ((Package) source.eContainer().eContainer().eContainer()).getName();
            IValidationContext<IModelReferenceField> ctx7 = newContext(
                  descriptor.findModel(packageName, modelName).get()
                        .getParts()
                        .getByName(fieldName)
                        .get(),
                  helper);
            doValidation(ctx7);
            break;
         case SystemDescriptorPackage.BASE_PART_DECLARATION:
            fieldName = ((BasePartDeclaration) source).getName();
            modelName = ((Model) source.eContainer().eContainer()).getName();
            packageName = ((Package) source.eContainer().eContainer().eContainer()).getName();
            IValidationContext<IModelReferenceField> ctx8 = newContext(
               descriptor.findModel(packageName, modelName).get()
                     .getParts()
                     .getByName(fieldName)
                     .get(),
               helper);
            doValidation(ctx8);
            break;
         case SystemDescriptorPackage.REFINED_PART_DECLARATION:
            fieldName = ((RefinedPartDeclaration) source).getName();
            modelName = ((Model) source.eContainer().eContainer()).getName();
            packageName = ((Package) source.eContainer().eContainer().eContainer()).getName();
            IValidationContext<IModelReferenceField> ctx9 = newContext(
               descriptor.findModel(packageName, modelName).get()
                     .getParts()
                     .getByName(fieldName)
                     .get(),
               helper);
            doValidation(ctx9);
            break;
         case SystemDescriptorPackage.REQUIRE_DECLARATION:
            fieldName = ((RequireDeclaration) source).getName();
            modelName = ((Model) source.eContainer().eContainer()).getName();
            packageName = ((Package) source.eContainer().eContainer().eContainer()).getName();
            IValidationContext<IModelReferenceField> ctx10 = newContext(
                  descriptor.findModel(packageName, modelName).get()
                        .getRequiredModels()
                        .getByName(fieldName)
                        .get(),
                  helper);
            doValidation(ctx10);
            break;
         case SystemDescriptorPackage.LINK_DECLARATION:
            modelName = ((Model) source.eContainer().eContainer()).getName();
            packageName = ((Package) source.eContainer().eContainer().eContainer()).getName();
            IValidationContext<IModelLink<?>> ctx11 = newContext(
                  findLink(descriptor.findModel(packageName, modelName).get(), (LinkDeclaration) source),
                  helper);
            doValidation(ctx11);
            break;
         case SystemDescriptorPackage.SCENARIO:
            String scenarioName = ((Scenario) source).getName();
            modelName = ((Model) source.eContainer()).getName();
            packageName = ((Package) source.eContainer().eContainer()).getName();
            IValidationContext<IScenario> ctx12 = newContext(
                  descriptor.findModel(packageName, modelName).get()
                        .getScenarios()
                        .getByName(scenarioName)
                        .get(),
                  helper);
            doValidation(ctx12);
            break;
         case SystemDescriptorPackage.GIVEN_STEP:
         case SystemDescriptorPackage.WHEN_STEP:
         case SystemDescriptorPackage.THEN_STEP:
            scenarioName = ((Scenario) source.eContainer().eContainer()).getName();
            modelName = ((Model) source.eContainer().eContainer().eContainer()).getName();
            packageName = ((Package) source.eContainer().eContainer().eContainer().eContainer()).getName();
            IValidationContext<IScenarioStep> ctx13 = newContext(
                  findStep(descriptor.findModel(packageName, modelName).get(), scenarioName, (Step) source),
                  helper);
            doValidation(ctx13);
            break;
         default:
            // Do nothing, this is not a type we want to validate.
      }
   }

   /**
    * Template method invoked to create a new validation context.
    */
   protected <T> IValidationContext<T> newContext(T object, ValidationHelper helper) {
      return new ProxyingValidationContext<>(object, helper);
   }

   /**
    * Template method invoked to perform validation with the given context.
    */
   protected void doValidation(IValidationContext<?> context) {
      synchronized (validators) {
         for (ISystemDescriptorValidator validator : validators) {
            safelyInvokeValidator(validator, context);
         }
      }
   }

   private void safelyInvokeValidator(ISystemDescriptorValidator validator, IValidationContext<?> context) {
      // Do not allow a misbehaving validator stop the entire parsing process.
      try {
         validator.validate(context);
      } catch (Throwable t) {
         logService.error(getClass(),
                          t,
                          "Validator %s threw an exception, consuming exception so parsing may continue.",
                          validator.getClass());
      }
   }

   private void registerSelf() {
      // Note this method and unregisterSelf get called while by guarded by the validators list.  This means this
      // and unregisterSelf can't be called concurrently.
      logService.trace(getClass(), "Registering self as a DSL validation extension.");
      validator.addValidatorExtension(this);
   }

   private void unregisterSelf() {
      // See registerSelf comments above.
      logService.trace(getClass(), "Unregistering self as a DSL validation extension.");
      validator.removeValidatorExtension(this);
   }

   private void validateReferenceDataField(ReferencedDataModelFieldDeclaration source,
                                           ValidationHelper helper,
                                           ISystemDescriptor descriptor) {
      DataModel dataModel = source.getDataModel();
      if (dataModel != null) {
         switch (dataModel.eClass().getClassifierID()) {
            case SystemDescriptorPackage.DATA:
            case SystemDescriptorPackage.ENUMERATION:
               String fieldName = source.getName();
               String dataName = ((Data) source.eContainer()).getName();
               String packageName = ((Package) source.eContainer().eContainer()).getName();
               IValidationContext<?> ctx = newContext(
                     descriptor.findData(packageName, dataName).get()
                           .getFields()
                           .getByName(fieldName)
                           .get(),
                     helper);
               doValidation(ctx);
            default:
               // Do nothing, ignore this.  This means the field is not valid anyway and the default validation will
               // indicate an error.  This can happen if the field is correct but the type of the field cannot be
               // resolved.
         }
      }
   }

   private static Package findPackage(EObject source) {
      if (source.eClass().equals(SystemDescriptorPackage.Literals.PACKAGE)) {
         return (Package) source;
      }
      EObject parent = source.eContainer();
      if (parent == null) {
         throw new IllegalStateException(String.format(
               "unable to find a root container object of type %s while walking the containment hierarchy of %s!",
               Package.class.getName(),
               source));
      }
      return findPackage(parent);
   }

   private static IModelLink<?> findLink(IModel model, LinkDeclaration xtext) {
      return (IModelLink<?>) model.getLinks()
            .stream()
            .map(l -> (IUnwrappable<?>) l)
            .filter(l -> EcoreUtil.equals(l.unwrap(), xtext))
            .findAny()
            .orElseThrow(() -> new IllegalStateException("failed to find the wrapper for link " + xtext));
   }

   @SuppressWarnings("unchecked")
   private static IScenarioStep findStep(IModel model, String scenarioName, Step xtext) {
      IScenario scenario = model.getScenarios()
            .getByName(scenarioName)
            .orElseThrow(() -> new IllegalStateException("failed to find the wrapper for scenario " + scenarioName));
      Collection<IScenarioStep> steps;
      switch (xtext.eClass().getClassifierID()) {
         case SystemDescriptorPackage.GIVEN_STEP:
            steps = scenario.getGivens();
            break;
         case SystemDescriptorPackage.WHEN_STEP:
            steps = scenario.getWhens();
            break;
         case SystemDescriptorPackage.THEN_STEP:
            steps = scenario.getThens();
            break;
         default:
            throw new UnrecognizedXtextTypeException(xtext);
      }
      return (IScenarioStep) steps.stream()
            .map(s -> (IUnwrappable<Step>) s)
            .filter(s -> EcoreUtil.equals(xtext, s.unwrap()))
            .findAny()
            .orElseThrow(() -> new IllegalStateException("failed to find the wrapper for step " + xtext));
   }

   /**
    * A value holder to hold {@link ISystemDescriptorValidator}s.  This is a workaround to Guice to
    * enable optional constructor parameters.
    */
   public static class ValidatorsHolder {

      @Inject(optional = true)
      Set<ISystemDescriptorValidator> validators = Collections.emptySet();
   }
}
