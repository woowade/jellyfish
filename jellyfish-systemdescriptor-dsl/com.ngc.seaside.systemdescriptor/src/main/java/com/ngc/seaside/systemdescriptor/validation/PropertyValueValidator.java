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
package com.ngc.seaside.systemdescriptor.validation;

import com.google.inject.Inject;

import com.ngc.seaside.systemdescriptor.systemDescriptor.BooleanValue;
import com.ngc.seaside.systemdescriptor.systemDescriptor.DataFieldDeclaration;
import com.ngc.seaside.systemdescriptor.systemDescriptor.DblValue;
import com.ngc.seaside.systemdescriptor.systemDescriptor.EnumPropertyValue;
import com.ngc.seaside.systemdescriptor.systemDescriptor.IntValue;
import com.ngc.seaside.systemdescriptor.systemDescriptor.PrimitiveDataFieldDeclaration;
import com.ngc.seaside.systemdescriptor.systemDescriptor.PrimitiveDataType;
import com.ngc.seaside.systemdescriptor.systemDescriptor.PrimitivePropertyFieldDeclaration;
import com.ngc.seaside.systemdescriptor.systemDescriptor.PropertyFieldDeclaration;
import com.ngc.seaside.systemdescriptor.systemDescriptor.PropertyValue;
import com.ngc.seaside.systemdescriptor.systemDescriptor.PropertyValueAssignment;
import com.ngc.seaside.systemdescriptor.systemDescriptor.PropertyValueExpression;
import com.ngc.seaside.systemdescriptor.systemDescriptor.ReferencedDataModelFieldDeclaration;
import com.ngc.seaside.systemdescriptor.systemDescriptor.ReferencedPropertyFieldDeclaration;
import com.ngc.seaside.systemdescriptor.systemDescriptor.StringValue;
import com.ngc.seaside.systemdescriptor.systemDescriptor.SystemDescriptorPackage;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.xtext.naming.IQualifiedNameProvider;
import org.eclipse.xtext.validation.Check;

import java.util.stream.Collectors;

public class PropertyValueValidator extends AbstractUnregisteredSystemDescriptorValidator {

   @Inject
   private IQualifiedNameProvider nameProvider;

   /**
    * Validates the value of the primitive property matches the expected type.
    */
   @Check
   public void checkPrimitivePropertyValue(PropertyValue value) {
      // Note that PropertyValues and metadata values use the same
      // types. Thus, we only due the validation if the container
      // is properties related.
      if (value.eContainer() instanceof PropertyValueAssignment) {
         PropertyValueExpression exp = ((PropertyValueAssignment) value.eContainer())
               .getExpression();

         try {
            switch (value.eClass().getClassifierID()) {
               case SystemDescriptorPackage.INT_VALUE:
                  checkIntMatchesPropertyType(exp, (IntValue) value);
                  break;
               case SystemDescriptorPackage.DBL_VALUE:
                  checkFloatMatchesPropertyType(exp, (DblValue) value);
                  break;
               case SystemDescriptorPackage.BOOLEAN_VALUE:
                  checkBooleanMatchesPropertyType(exp, (BooleanValue) value);
                  break;
               case SystemDescriptorPackage.STRING_VALUE:
                  checkStringMatchesPropertyType(exp, (StringValue) value);
                  break;
               case SystemDescriptorPackage.ENUM_PROPERTY_VALUE:
                  checkEnumValueTypeMatchesPropertyType(exp, (EnumPropertyValue) value);
                  checkEnumValueIsValidConstant((EnumPropertyValue) value);
                  break;
               default:
                  // Do nothing.
            }
         } catch (AbortValidationDueToLinkingFailureException e) {
            // Do nothing. This exception is just used to short circuit the
            // validation logic. We don't need to do anything because the
            // elements are already invalid due to linking failures.
         }
      }
   }

   private void checkIntMatchesPropertyType(PropertyValueExpression expression, IntValue value) {
      Object propertyType = getPropertyType(expression);
      if (!propertyType.equals(PrimitiveDataType.INT)) {
         declareInvalidPropertyTypeError(value,
                                         expression,
                                         propertyType,
                                         SystemDescriptorPackage.Literals.INT_VALUE__VALUE);
      }
   }

   private void checkFloatMatchesPropertyType(PropertyValueExpression expression, DblValue value) {
      Object propertyType = getPropertyType(expression);
      if (!propertyType.equals(PrimitiveDataType.FLOAT)) {
         declareInvalidPropertyTypeError(value,
                                         expression,
                                         propertyType,
                                         SystemDescriptorPackage.Literals.DBL_VALUE__VALUE);
      }
   }

   private void checkBooleanMatchesPropertyType(PropertyValueExpression expression, BooleanValue value) {
      Object propertyType = getPropertyType(expression);
      if (!propertyType.equals(PrimitiveDataType.BOOLEAN)) {
         declareInvalidPropertyTypeError(value,
                                         expression,
                                         propertyType,
                                         SystemDescriptorPackage.Literals.BOOLEAN_VALUE__VALUE);
      }
   }

   private void checkStringMatchesPropertyType(PropertyValueExpression expression, StringValue value) {
      Object propertyType = getPropertyType(expression);
      if (!propertyType.equals(PrimitiveDataType.STRING)) {
         declareInvalidPropertyTypeError(value,
                                         expression,
                                         propertyType,
                                         SystemDescriptorPackage.Literals.STRING_VALUE__VALUE);
      }
   }

   private void checkEnumValueTypeMatchesPropertyType(PropertyValueExpression expression, EnumPropertyValue value) {
      Object propertyType = getPropertyType(expression);
      if (!propertyType.equals(value.getEnumeration())) {
         declareInvalidPropertyTypeError(
               value,
               expression,
               propertyType,
               SystemDescriptorPackage.Literals.ENUM_PROPERTY_VALUE__VALUE);
      }
   }

   private void checkEnumValueIsValidConstant(EnumPropertyValue value) {
      boolean valid = value.getEnumeration().getValues()
            .stream()
            .anyMatch(d -> d.getValue().equals(value.getValue()));
      if (!valid) {
         String msg = String.format(
               "The enumeration '%s' contains no constant named '%s'.",
               nameProvider.getFullyQualifiedName(value.getEnumeration()),
               value.getValue());
         error(msg, value, SystemDescriptorPackage.Literals.ENUM_PROPERTY_VALUE__VALUE);
      }
   }

   private void declareInvalidPropertyTypeError(
         PropertyValue value,
         PropertyValueExpression expression,
         Object propertyType,
         EStructuralFeature feature) {
      String propertyTypeName = propertyType instanceof Enum
                                ? propertyType.toString()
                                : nameProvider.getFullyQualifiedName((EObject) propertyType).toString();

      String propertyPath = expression.getPathSegments()
            .stream()
            .map(s -> s.getFieldDeclaration().getName())
            .collect(Collectors.joining("."));

      String msg = String.format(
            "Expected a value of type '%s' for the property '%s%s'.",
            propertyTypeName,
            expression.getDeclaration().getName(),
            propertyPath.isEmpty() ? "" : "." + propertyPath);
      error(msg, value, feature);
   }

   private static Object getPropertyType(PropertyValueExpression expression) {
      Object type;

      if (expression.getPathSegments().isEmpty()) {
         type = getPropertyType(expression.getDeclaration());
      } else {
         DataFieldDeclaration lastField = expression.getPathSegments()
               .get(expression.getPathSegments().size() - 1)
               .getFieldDeclaration();
         switch (lastField.eClass().getClassifierID()) {
            case SystemDescriptorPackage.PRIMITIVE_DATA_FIELD_DECLARATION:
               type = ((PrimitiveDataFieldDeclaration) lastField).getType();
               break;
            case SystemDescriptorPackage.REFERENCED_DATA_MODEL_FIELD_DECLARATION:
               type = ((ReferencedDataModelFieldDeclaration) lastField).getDataModel();
               break;
            case SystemDescriptorPackage.DATA_FIELD_DECLARATION:
               // This means the field is a proxy and linking has failed.
               throw new AbortValidationDueToLinkingFailureException();
            default:
               throw new IllegalStateException(
                     "update this method to support the new data field declaration " + lastField);
         }
      }

      return type;
   }

   private static Object getPropertyType(PropertyFieldDeclaration declaration) {
      Object type = null;
      switch (declaration.eClass().getClassifierID()) {
         case SystemDescriptorPackage.PRIMITIVE_PROPERTY_FIELD_DECLARATION:
            type = ((PrimitivePropertyFieldDeclaration) declaration).getType();
            break;
         case SystemDescriptorPackage.REFERENCED_PROPERTY_FIELD_DECLARATION:
            type = ((ReferencedPropertyFieldDeclaration) declaration).getDataModel();
            break;
         case SystemDescriptorPackage.PROPERTY_FIELD_DECLARATION:
            // This means the field is a proxy and linking has failed.
            throw new AbortValidationDueToLinkingFailureException();
         default:
            throw new IllegalStateException(
                  "update this method to support the new property declaration " + declaration);
      }
      return type;
   }
}
