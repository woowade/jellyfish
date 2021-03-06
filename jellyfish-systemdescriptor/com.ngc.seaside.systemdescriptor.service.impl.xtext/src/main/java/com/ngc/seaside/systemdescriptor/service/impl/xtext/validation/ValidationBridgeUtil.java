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
package com.ngc.seaside.systemdescriptor.service.impl.xtext.validation;

import com.ngc.seaside.systemdescriptor.model.api.IPackage;
import com.ngc.seaside.systemdescriptor.model.api.SystemDescriptors;
import com.ngc.seaside.systemdescriptor.model.api.data.IData;
import com.ngc.seaside.systemdescriptor.model.api.data.IDataField;
import com.ngc.seaside.systemdescriptor.model.api.model.IDataReferenceField;
import com.ngc.seaside.systemdescriptor.model.api.model.IModel;
import com.ngc.seaside.systemdescriptor.model.api.model.IModelReferenceField;
import com.ngc.seaside.systemdescriptor.model.api.model.link.IModelLink;
import com.ngc.seaside.systemdescriptor.model.api.model.properties.IProperty;
import com.ngc.seaside.systemdescriptor.model.api.model.properties.IPropertyPrimitiveValue;
import com.ngc.seaside.systemdescriptor.model.api.model.scenario.IScenario;
import com.ngc.seaside.systemdescriptor.model.api.model.scenario.IScenarioStep;
import com.ngc.seaside.systemdescriptor.model.impl.xtext.exception.UnconvertableTypeException;
import com.ngc.seaside.systemdescriptor.systemDescriptor.SystemDescriptorPackage;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;

import java.lang.reflect.Method;

/**
 * A utility that uses method names to feature out the XText structural feature
 * the method references. This is used to allow XText to declare validation
 * issues on particular fields of models.
 */
public class ValidationBridgeUtil {

   /**
    * Gets the structural feature as identified by the name of the method.
    *
    * @param object the source object on which the method is called
    * @param xtext  the raw, unwrapped XText type that corresponds to the type that is being wrapped
    * @param method the method that was called on the object
    * @return the structural feature
    * @throws IllegalValidationDeclarationException if there is no structural feature associated with the method. This
    *                                               usually happens if the user calls a method that returns a derived
    *                                               value or if the method returns an object that is no owned by the
    *                                               object the {@link ProxyingValidationContext} was created for.
    */
   public static EStructuralFeature getFeature(Object object, EObject xtext, Method method) {
      if (object instanceof IPackage) {
         return doGetFeature((IPackage) object, xtext, method);
      }
      if (object instanceof IData) {
         return doGetFeature((IData) object, xtext, method);
      }
      if (object instanceof IDataField) {
         return doGetFeature((IDataField) object, xtext, method);
      }
      if (object instanceof IModel) {
         return doGetFeature((IModel) object, xtext, method);
      }
      if (object instanceof IModelLink) {
         return doGetFeature((IModelLink<?>) object, xtext, method);
      }
      if (object instanceof IDataReferenceField) {
         return doGetFeature((IDataReferenceField) object, xtext, method);
      }
      if (object instanceof IModelReferenceField) {
         return doGetFeature((IModelReferenceField) object, xtext, method);
      }
      if (object instanceof IScenario) {
         return doGetFeature((IScenario) object, xtext, method);
      }
      if (object instanceof IScenarioStep) {
         return doGetFeature((IScenarioStep) object, xtext, method);
      }
      if (object instanceof IProperty) {
         return doGetFeature((IProperty) object, xtext, method);
      }
      if (object instanceof IPropertyPrimitiveValue) {
         return doGetFeature((IPropertyPrimitiveValue) object, xtext, method);
      }
      throw new UnconvertableTypeException(object);
   }

   private static EStructuralFeature doGetFeature(IPackage object, EObject xtext, Method method) {
      switch (method.getName()) {
         case "getName":
            return SystemDescriptorPackage.Literals.PACKAGE__NAME;
         default:
            throw new IllegalValidationDeclarationException(illegalDeclaration(object, xtext, method));
      }
   }

   private static EStructuralFeature doGetFeature(IData object, EObject xtext, Method method) {
      switch (method.getName()) {
         case "getName":
            return SystemDescriptorPackage.Literals.ELEMENT__NAME;
         default:
            throw new IllegalValidationDeclarationException(illegalDeclaration(object, xtext, method));
      }
   }

   private static EStructuralFeature doGetFeature(IDataField object, EObject xtext, Method method) {
      switch (method.getName()) {
         case "getName":
            return SystemDescriptorPackage.Literals.DATA_FIELD_DECLARATION__NAME;
         case "getType":
            if (!SystemDescriptors.isPrimitiveDataFieldDeclaration(object)) {
               throw new IllegalValidationDeclarationException(
                     "IDataField references data, can only declare a validation error on the 'type' if the field"
                           + " references a primitive type!");
            }
            return SystemDescriptorPackage.Literals.PRIMITIVE_DATA_FIELD_DECLARATION__TYPE;
         case "getReferencedDataType":
         case "getReferencedEnumeration":
            if (SystemDescriptors.isPrimitiveDataFieldDeclaration(object)) {
               throw new IllegalValidationDeclarationException(
                     "IDataField references a primitive, can only declare a validation error on the if the field"
                           + " references a data type or enumeration!");
            }
            return SystemDescriptorPackage.Literals.REFERENCED_DATA_MODEL_FIELD_DECLARATION__DATA_MODEL;
         default:
            throw new IllegalValidationDeclarationException(illegalDeclaration(object, xtext, method));
      }
   }

   private static EStructuralFeature doGetFeature(IModel object, EObject xtext, Method method) {
      switch (method.getName()) {
         case "getName":
            return SystemDescriptorPackage.Literals.ELEMENT__NAME;
         default:
            throw new IllegalValidationDeclarationException(illegalDeclaration(object, xtext, method));
      }
   }

   private static EStructuralFeature doGetFeature(IModelLink<?> object, EObject xtext, Method method) {
      switch (method.getName()) {
         case "getSource":
            return SystemDescriptorPackage.Literals.BASE_LINK_DECLARATION__SOURCE;
         case "getTarget":
            return SystemDescriptorPackage.Literals.BASE_LINK_DECLARATION__TARGET;
         default:
            throw new IllegalValidationDeclarationException(illegalDeclaration(object, xtext, method));
      }
   }

   private static EStructuralFeature doGetFeature(IDataReferenceField object, EObject xtext, Method method) {
      return SystemDescriptors.isInput(object) ? doGetFeatureOfInput(object, xtext, method)
                                               : doGetFeatureOfOutput(object, xtext, method);
   }

   private static EStructuralFeature doGetFeature(IModelReferenceField object, EObject xtext, Method method) {
      return SystemDescriptors.isPart(object) ? doGetFeatureOfPart(object, xtext, method)
                                              : doGetFeatureOfRequiredModel(object, xtext, method);
   }

   private static EStructuralFeature doGetFeature(IScenario object, EObject xtext, Method method) {
      switch (method.getName()) {
         case "getName":
            return SystemDescriptorPackage.Literals.SCENARIO__NAME;
         case "getGivens":
            return SystemDescriptorPackage.Literals.SCENARIO__GIVEN;
         case "getWhens":
            return SystemDescriptorPackage.Literals.SCENARIO__WHEN;
         case "getThens":
            return SystemDescriptorPackage.Literals.SCENARIO__THEN;
         default:
            throw new IllegalValidationDeclarationException(illegalDeclaration(object, xtext, method));
      }
   }

   private static EStructuralFeature doGetFeature(IScenarioStep object, EObject xtext, Method method) {
      switch (method.getName()) {
         case "getKeyword":
            return SystemDescriptorPackage.Literals.STEP__KEYWORD;
         case "getParameters":
            return SystemDescriptorPackage.Literals.STEP__PARAMETERS;
         default:
            throw new IllegalValidationDeclarationException(illegalDeclaration(object, xtext, method));
      }
   }

   private static EStructuralFeature doGetFeature(IProperty object, EObject xtext, Method method) {
      switch (method.getName()) {
         case "getName":
            return SystemDescriptorPackage.Literals.PROPERTY_FIELD_DECLARATION__NAME;
         default:
            throw new IllegalValidationDeclarationException(illegalDeclaration(object, xtext, method));
      }
   }

   private static EStructuralFeature doGetFeature(IPropertyPrimitiveValue object, EObject xtext, Method method) {
      switch (method.getName()) {
         case "getBoolean":
            return SystemDescriptorPackage.Literals.BOOLEAN_VALUE__VALUE;
         case "getDecimal":
            return SystemDescriptorPackage.Literals.DBL_VALUE__VALUE;
         case "getInteger":
            return SystemDescriptorPackage.Literals.INT_VALUE__VALUE;
         case "getString":
            return SystemDescriptorPackage.Literals.STRING_VALUE__VALUE;
         default:
            throw new IllegalValidationDeclarationException(illegalDeclaration(object, xtext, method));
      }
   }

   private static EStructuralFeature doGetFeatureOfInput(IDataReferenceField object, EObject xtext, Method method) {
      switch (method.getName()) {
         case "getName":
            return SystemDescriptorPackage.Literals.FIELD_DECLARATION__NAME;
         case "getType":
            return SystemDescriptorPackage.Literals.INPUT_DECLARATION__TYPE;
         case "getCardinality":
            return SystemDescriptorPackage.Literals.INPUT_DECLARATION__CARDINALITY;
         default:
            throw new IllegalValidationDeclarationException(illegalDeclaration(object, xtext, method));
      }
   }

   private static EStructuralFeature doGetFeatureOfOutput(IDataReferenceField object, EObject xtext, Method method) {
      switch (method.getName()) {
         case "getName":
            return SystemDescriptorPackage.Literals.FIELD_DECLARATION__NAME;
         case "getType":
            return SystemDescriptorPackage.Literals.OUTPUT_DECLARATION__TYPE;
         case "getCardinality":
            return SystemDescriptorPackage.Literals.OUTPUT_DECLARATION__CARDINALITY;
         default:
            throw new IllegalValidationDeclarationException(illegalDeclaration(object, xtext, method));
      }
   }

   private static EStructuralFeature doGetFeatureOfPart(IModelReferenceField object, EObject xtext, Method method) {
      switch (method.getName()) {
         case "getName":
            return SystemDescriptorPackage.Literals.FIELD_DECLARATION__NAME;
         case "getType":
            return SystemDescriptorPackage.Literals.BASE_PART_DECLARATION__TYPE;
         default:
            throw new IllegalValidationDeclarationException(illegalDeclaration(object, xtext, method));
      }
   }

   private static EStructuralFeature doGetFeatureOfRequiredModel(IModelReferenceField object, EObject xtext,
                                                                 Method method) {
      switch (method.getName()) {
         case "getName":
            return SystemDescriptorPackage.Literals.FIELD_DECLARATION__NAME;
         case "getType":
            return SystemDescriptorPackage.Literals.BASE_REQUIRE_DECLARATION__TYPE;
         default:
            throw new IllegalValidationDeclarationException(illegalDeclaration(object, xtext, method));
      }

   }

   private static String illegalDeclaration(Object object, EObject xtext, Method method) {
      return String.format(
            "cannot declare a validation issue against %s.%s!  Make sure this is not a readonly or derived property.",
            object.getClass().getCanonicalName(), method.getName());
   }
}
