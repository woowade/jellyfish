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
import com.ngc.seaside.systemdescriptor.model.api.IPackage;
import com.ngc.seaside.systemdescriptor.model.api.ISystemDescriptor;
import com.ngc.seaside.systemdescriptor.model.api.data.DataTypes;
import com.ngc.seaside.systemdescriptor.model.api.data.IData;
import com.ngc.seaside.systemdescriptor.model.api.model.IModel;
import com.ngc.seaside.systemdescriptor.model.api.model.IModelReferenceField;
import com.ngc.seaside.systemdescriptor.model.api.model.properties.IProperty;
import com.ngc.seaside.systemdescriptor.model.impl.basic.NamedChildCollection;
import com.ngc.seaside.systemdescriptor.model.impl.basic.model.properties.Properties;
import com.ngc.seaside.systemdescriptor.service.api.ISystemDescriptorService;
import com.ngc.seaside.systemdescriptor.validation.api.IValidationContext;
import com.ngc.seaside.systemdescriptor.validation.api.Severity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static com.ngc.seaside.jellyfish.cli.command.analyzebudget.budget.SdBudgetAdapterTest.getMockedBudget;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BudgetValidatorTest {

   private BudgetValidator validator;

   @Mock
   private IModel model;

   @Mock
   private ISystemDescriptorService sdService;

   @Mock
   private IValidationContext<IModel> context;

   private SdBudgetAdapter adapter;

   @Before
   public void before() {
      when(context.getObject()).thenReturn(model);

      adapter = new SdBudgetAdapter();
      adapter.setSdService(sdService);

      when(sdService.getAggregatedView(any(IModel.class))).thenAnswer(args -> args.getArgument(0));
      validator = new BudgetValidator(adapter, sdService);
      when(model.getParts()).thenReturn(new NamedChildCollection<>());
   }

   @Test
   public void testInvalidBudget() {
      setupForBudgetProperty(true);

      Properties properties = new Properties();
      when(model.getProperties()).thenReturn(properties);

      properties.add(getMockedBudget("1.3 kg", "10.9 kW", "mass1"));

      validator.validate(context);
      verify(context, times(1)).declare(eq(Severity.ERROR), any(), any());
   }

   @Test
   public void testInvalidBudgetPart() {
      setupForBudgetProperty(true);

      Properties properties = new Properties();
      when(model.getProperties()).thenReturn(properties);

      properties.add(getMockedBudget("0", "1 kW", "power"));

      IModelReferenceField partField = mock(IModelReferenceField.class);
      IModel part = mock(IModel.class);
      model.getParts().add(partField);
      when(partField.getType()).thenReturn(part);
      when(part.getProperties()).thenReturn(new Properties());
      when(part.getParts()).thenReturn(new NamedChildCollection<>());

      validator.validate(context);
      verify(context, never()).declare(eq(Severity.ERROR), any(), any());

      IProperty partProperty = mock(IProperty.class, RETURNS_DEEP_STUBS);
      when(partProperty.getName()).thenReturn("power");
      when(partProperty.getType()).thenReturn(DataTypes.STRING);
      when(partProperty.getCardinality()).thenReturn(FieldCardinality.SINGLE);
      when(partProperty.getPrimitive().isSet()).thenReturn(true);
      when(partProperty.getPrimitive().getString()).thenReturn("1 m");
      part.getProperties().add(partProperty);

      validator.validate(context);
      verify(context, times(1)).declare(eq(Severity.ERROR), any(), any());
   }

   @Test
   public void testDoesRequireBudgetPropertyToBeIncludedInTheProject() {
      setupForBudgetProperty(false);

      validator.validate(context);
      verify(context, never()).declare(any(), any(), any());
   }

   private void setupForBudgetProperty(boolean registerBudgetDataType) {
      ISystemDescriptor sd = mock(ISystemDescriptor.class);
      IPackage packagez = mock(IPackage.class);

      when(packagez.getParent()).thenReturn(sd);
      when(model.getParent()).thenReturn(packagez);

      if (registerBudgetDataType) {
         IData budget = mock(IData.class);
         when(sd.findData(SdBudgetAdapter.BUDGET_QUALIFIED_NAME)).thenReturn(Optional.of(budget));
      } else {
         when(sd.findData(SdBudgetAdapter.BUDGET_QUALIFIED_NAME)).thenReturn(Optional.empty());
      }
   }
}
