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
package com.ngc.seaside.jellyfish.cli.command.analyzebudget.budget;

import com.ngc.seaside.systemdescriptor.model.api.FieldCardinality;
import com.ngc.seaside.systemdescriptor.model.api.data.DataTypes;
import com.ngc.seaside.systemdescriptor.model.api.model.IModel;
import com.ngc.seaside.systemdescriptor.model.api.model.IModelReferenceField;
import com.ngc.seaside.systemdescriptor.model.api.model.properties.IProperty;
import com.ngc.seaside.systemdescriptor.model.api.model.properties.IPropertyPrimitiveValue;
import com.ngc.seaside.systemdescriptor.service.api.ISystemDescriptorService;
import com.ngc.seaside.systemdescriptor.validation.api.AbstractSystemDescriptorValidator;
import com.ngc.seaside.systemdescriptor.validation.api.IValidationContext;
import com.ngc.seaside.systemdescriptor.validation.api.Severity;

import javax.inject.Inject;

public class BudgetValidator extends AbstractSystemDescriptorValidator {

   private SdBudgetAdapter adapter;
   private ISystemDescriptorService sdService;

   @Inject
   public BudgetValidator(SdBudgetAdapter adapter, ISystemDescriptorService sdService) {
      this.adapter = adapter;
      this.sdService = sdService;
   }

   @Override
   protected void validateModel(IValidationContext<IModel> context) {
      // Only run this validator if the Budget property is actually included in the project.  We do this check because
      // some projects may opt out of including the default SD language artifacts as dependencies.  If a project does
      // this and then imports the Budget property and then runs Jellyfish, this validator will still be executed.
      // When we call sdService.getAggregatedView(model) we get lots of exceptions because that Budget property can't
      // be resolved.  However, since validators are called by XText in loops, we gets lots of exceptions that are
      // thrown to the user.  This is confusing since this hides the actual issue of the import not being resolved.
      // This is really an edge case that we don't have to worry much about, but this simple check helps make it more
      // clear to the user what is going on.
      if (context.getObject().getParent().getParent().findData(SdBudgetAdapter.BUDGET_QUALIFIED_NAME).isPresent()) {
         doValidateModel(context);
      }
   }

   private void doValidateModel(IValidationContext<IModel> context) {
      IModel model = context.getObject();

      IModel aggregatedModel = sdService.getAggregatedView(model);
      for (IProperty property : aggregatedModel.getProperties()) {
         boolean performValidation =
               property.getType() == DataTypes.DATA
               && property.getCardinality() == FieldCardinality.SINGLE
               && SdBudgetAdapter.BUDGET_QUALIFIED_NAME
                     .equals(property.getReferencedDataType().getFullyQualifiedName());
         if (performValidation) {
            Object source = adapter.getSource(model, property.getName());
            Budget<?> budget = null;
            try {
               budget = adapter.getBudget(aggregatedModel, property, source);
            } catch (BudgetValidationException e) {
               Object errorSource = context.declare(Severity.ERROR, e.getSimpleMessage(), e.getSource());
               callOffendingError(errorSource);
               performValidation = false;
            }

            if (performValidation) {
               checkParts(context, model, budget);
            }
         }
      }
   }

   private void checkParts(IValidationContext<IModel> context, IModel model, Budget<?> budget) {
      try {
         adapter.getBudgetValue(model, budget);
      } catch (BudgetValidationException e) {
         Object errorSource = context.declare(Severity.ERROR, e.getSimpleMessage(), e.getSource());
         callOffendingError(errorSource);
      }

      for (IModelReferenceField part : model.getParts()) {
         checkParts(context, part.getType(), budget);
      }
   }

   private void callOffendingError(Object errorSource) {
      if (errorSource instanceof IProperty) {
         ((IProperty) errorSource).getName();
      } else if (errorSource instanceof IPropertyPrimitiveValue) {
         ((IPropertyPrimitiveValue) errorSource).getString();
      }
   }

}
