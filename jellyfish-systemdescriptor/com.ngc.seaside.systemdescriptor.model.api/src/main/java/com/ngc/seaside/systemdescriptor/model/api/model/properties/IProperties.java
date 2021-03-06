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
package com.ngc.seaside.systemdescriptor.model.api.model.properties;

import com.google.common.base.Preconditions;

import com.ngc.seaside.systemdescriptor.model.api.FieldCardinality;
import com.ngc.seaside.systemdescriptor.model.api.INamedChildCollection;
import com.ngc.seaside.systemdescriptor.model.api.data.DataTypes;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;
import java.util.function.Function;

/**
 * A type of collection that contains properties. This type of collection contains extra operations to help resolve the
 * values of properties. For example, given the following model
 * <pre> {@code
 * package my.servers
 * model RedHatServer refines Server {
 *   properties {
 *     serverConfig.vendor = "HP"
 *     serverConfig.cores = 16
 *     serverConfig.performanceScore = 0.95
 *     serverConfig.isVirtual = true
 *   }
 * }
 * }
 * </pre>
 * it is possible to resolve the "vendor" property as follows:
 * <pre> {@code
 *    IModel model = systemDescriptor.findModel("my.servers.RedHatServer").get();
 *    String vendor = model.getProperties().resolveAsString("serverConfig", "vendor").get();
 * }
 * </pre>
 * Properties and their values are not meant to be mutated.
 */
public interface IProperties extends INamedChildCollection<IProperties, IProperty> {

   /**
    * An immutable singleton that is used to indicate empty or missing properties.
    */
   IProperties EMPTY_PROPERTIES = new PropertiesUtil.SimplePropertiesImpl() {
      @Override
      public Optional<IProperty> getByName(String name) {
         return Optional.empty();
      }

      @Override
      public IProperty get(int index) {
         throw new IndexOutOfBoundsException("properties is empty");
      }

      @Override
      public int size() {
         return 0;
      }
   };

   /**
    * Attempts to resolve the value of the property with the given name. Returns {@link Optional#empty()} if the values
    * cannot be resolved, including but not limited to the following cases:
    * <pre>
    * <ul>
    *    <li>The property or any of the nested fields are not defined</li>
    *    <li>The property is unset</li>
    *    <li>The property does not have a cardinality of {@link FieldCardinality#SINGLE}</li>
    *    <li>The property has an intermediate field of type {@link DataTypes#DATA} with cardinality of
    *    {@link FieldCardinality#MANY}
    *    </li>
    * </ul>
    * </pre>
    * If present, the returned value will have cardinality of {@link FieldCardinality#SINGLE}.
    *
    * @param propertyName the name of the property
    * @param fieldNames   the optional names of the fields in the nested type
    * @return the value of the property, or {@link Optional#empty()} if the value cannot be determined
    */
   default Optional<IPropertyValue> resolveValue(String propertyName, String... fieldNames) {
      Preconditions.checkNotNull(propertyName, "property name must not be null!");
      Preconditions.checkNotNull(fieldNames, "field names must not be null!");
      Optional<IPropertyValue> value = getByName(propertyName)
            .filter(property -> property.getCardinality() == FieldCardinality.SINGLE)
            .map(IProperty::getValue);
      for (String fieldName : fieldNames) {
         Preconditions.checkNotNull(fieldName, "field names cannot contain a null value!");
         value = value.filter(IPropertyValue::isData)
               .map(IPropertyDataValue.class::cast)
               .flatMap(dataValue -> dataValue.getFieldByName(fieldName)
                     .filter(field -> field.getCardinality() == FieldCardinality.SINGLE)
                     .map(dataValue::getValue));
      }
      return value;
   }

   /**
    * Attempts to resolve the data value of the property with the given name. Returns {@link Optional#empty()} if the
    * values cannot be resolved, including but not limited to the following cases:
    * <pre>
    * <ul>
    *    <li>The property or any of the nested fields are not defined</li>
    *    <li>The property is unset</li> <li>The type of the property is not {@link DataTypes#DATA}</li>
    *    <li>The property does not have a cardinality of {@link FieldCardinality#SINGLE}</li>
    *    <li>The property has an intermediate field of type {@link DataTypes#DATA} with cardinality of
    *    {@link FieldCardinality#MANY}</li>
    * </ul>
    * </pre>
    * If present, the returned value will have cardinality of {@link FieldCardinality#SINGLE}.
    *
    * @param propertyName the name of the property
    * @param fieldNames   the optional names of the fields in the nested type
    * @return the data value of the property, or {@link Optional#empty()} if the value cannot be determined
    */
   default Optional<IPropertyDataValue> resolveAsData(String propertyName, String... fieldNames) {
      return resolveValue(propertyName, fieldNames)
            .filter(IPropertyValue::isData)
            .map(IPropertyDataValue.class::cast);
   }

   /**
    * Attempts to resolve the enumeration value of the property with the given name. Returns {@link Optional#empty()} if
    * the values cannot be resolved, including but not limited to the following cases:
    * <pre>
    * <ul>
    *    <li>The property or any of the nested fields are not defined</li>
    *    <li>The property is unset</li>
    *    <li>The type of the property is not {@link DataTypes#ENUM}</li>
    *    <li>The property does not have a cardinality of {@link FieldCardinality#SINGLE}</li>
    *    <li>The property has an intermediate field of type {@link DataTypes#DATA} with cardinality of
    *    {@link FieldCardinality#MANY}</li>
    * </ul>
    * </pre>
    * If present, the returned value will have cardinality of {@link FieldCardinality#SINGLE}.
    *
    * @param propertyName the name of the property
    * @param fieldNames   the optional names of the fields in the nested type
    * @return the enumeration value of the property, or {@link Optional#empty()} if the value cannot be determined
    */
   default Optional<IPropertyEnumerationValue> resolveAsEnumeration(String propertyName, String... fieldNames) {
      return resolveValue(propertyName, fieldNames)
            .filter(IPropertyValue::isEnumeration)
            .map(IPropertyEnumerationValue.class::cast);
   }

   /**
    * Attempts to resolve the primitive value of the property with the given name. Returns {@link Optional#empty()} if
    * the values cannot be resolved, including but not limited to the following cases:
    * <pre>
    * <ul>
    *    <li>The property or any of the nested fields are not defined</li>
    *    <li>The property is unset</li>
    *    <li>The type of the property is not a primitive type</li>
    *    <li>The property does not have a cardinality of {@link FieldCardinality#SINGLE}</li>
    *    <li>The property has an intermediate field of type {@link DataTypes#DATA} with cardinality of
    *    {@link FieldCardinality#MANY}</li>
    * </ul>
    * </pre>
    * If present, the returned value will have cardinality of {@link FieldCardinality#SINGLE}.
    *
    * @param propertyName the name of the property
    * @param fieldNames   the optional names of the fields in the nested type
    * @return the primitive value of the property, or {@link Optional#empty()} if the value cannot be determined
    */
   default Optional<IPropertyPrimitiveValue> resolveAsPrimitive(String propertyName, String... fieldNames) {
      return resolveValue(propertyName, fieldNames)
            .filter(IPropertyValue::isPrimitive)
            .map(IPropertyPrimitiveValue.class::cast);
   }

   /**
    * Attempts to resolve the integer value of the property with the given name. Returns {@link Optional#empty()} if the
    * values cannot be resolved, including but not limited to the following cases:
    * <pre>
    * <ul>
    *    <li>The property or any of the nested fields are not defined</li>
    *    <li>The property is unset</li>
    *    <li>The type of the property is not {@link DataTypes#INT}</li>
    *    <li>The property does not have a cardinality of {@link FieldCardinality#SINGLE}</li>
    *    <li>The property has an intermediate field of type {@link DataTypes#DATA} with cardinality of
    *    {@link FieldCardinality#MANY}</li>
    * </ul>
    * </pre>
    *
    * @param propertyName the name of the property
    * @param fieldNames   the optional names of the fields in the nested type
    * @return the integer value of the property, or {@link Optional#empty()} if the values cannot be determined
    */
   default Optional<BigInteger> resolveAsInteger(String propertyName, String... fieldNames) {
      return resolveAsPrimitive(propertyName, fieldNames)
            .filter(v -> v.getType() == DataTypes.INT)
            .map(IPropertyPrimitiveValue::getInteger);
   }

   /**
    * Attempts to resolve the decimal value of the property with the given name. Returns {@link Optional#empty()} if the
    * values cannot be resolved, including but not limited to the following cases:
    * <pre>
    * <ul>
    *    <li>The property or any of the nested fields are not defined</li>
    *    <li>The property is unset</li>
    *    <li>The type of the property is not {@link DataTypes#FLOAT}</li>
    *    <li>The property does not have a cardinality of {@link FieldCardinality#SINGLE}</li>
    *    <li>The property has an intermediate field of type {@link DataTypes#DATA} with cardinality of
    *    {@link FieldCardinality#MANY}</li>
    * </ul>
    * </pre>
    *
    * @param propertyName the name of the property
    * @param fieldNames   the optional names of the fields in the nested type
    * @return the decimal value of the property, or {@link Optional#empty()} if the values cannot be determined
    */
   default Optional<BigDecimal> resolveAsDecimal(String propertyName, String... fieldNames) {
      return resolveAsPrimitive(propertyName, fieldNames)
            .filter(v -> v.getType() == DataTypes.FLOAT)
            .map(IPropertyPrimitiveValue::getDecimal);
   }

   /**
    * Attempts to resolve the boolean value of the property with the given name. Returns {@link Optional#empty()} if the
    * values cannot be resolved, including but not limited to the following cases:
    * <pre>
    * <ul>
    *    <li>The property or any of the nested fields are not defined</li>
    *    <li>The property is unset</li>
    *    <li>The type of the property is not {@link DataTypes#BOOLEAN}</li>
    *    <li>The property does not have a cardinality of {@link FieldCardinality#SINGLE}</li>
    *    <li>The property has an intermediate field of type {@link DataTypes#DATA} with cardinality of
    *    {@link FieldCardinality#MANY}</li>
    * </ul>
    * </pre>
    *
    * @param propertyName the name of the property
    * @param fieldNames   the optional names of the fields in the nested type
    * @return the boolean value of the property, or {@link Optional#empty()} if the values cannot be determined
    */
   default Optional<Boolean> resolveAsBoolean(String propertyName, String... fieldNames) {
      return resolveAsPrimitive(propertyName, fieldNames)
            .filter(v -> v.getType() == DataTypes.BOOLEAN)
            .map(IPropertyPrimitiveValue::getBoolean);
   }

   /**
    * Attempts to resolve the string value of the property with the given name. Returns {@link Optional#empty()} if the
    * values cannot be resolved, including but not limited to the following cases:
    * <pre>
    * <ul>
    *    <li>The property or any of the nested fields are not defined</li>
    *    <li>The property is unset</li> <li>The type of the property is not {@link DataTypes#STRING}</li>
    *    <li>The property does not have a cardinality of {@link FieldCardinality#SINGLE}</li>
    *    <li>The property has an intermediate field of type {@link DataTypes#DATA} with cardinality of
    *    {@link FieldCardinality#MANY}</li>
    * </ul>
    * </pre>
    *
    * @param propertyName the name of the property
    * @param fieldNames   the optional names of the fields in the nested type
    * @return the string value of the property, or {@link Optional#empty()} if the values cannot be determined
    */
   default Optional<String> resolveAsString(String propertyName, String... fieldNames) {
      return resolveAsPrimitive(propertyName, fieldNames)
            .filter(v -> v.getType() == DataTypes.STRING)
            .map(IPropertyPrimitiveValue::getString);
   }

   /**
    * Attempts to resolve the values of the property with the given name. Returns {@link IPropertyValues#isSet() unset
    * values} if the values cannot be resolved, including but not limited to the following cases:
    * <pre>
    * <ul>
    *    <li>The property or any of the nested fields are not defined</li>
    *    <li>The property is unset</li>
    *    <li>The property does not have a cardinality of {@link FieldCardinality#MANY}</li>
    *    <li>The property has an intermediate field of type {@link DataTypes#DATA} with cardinality of
    *    {@link FieldCardinality#MANY}</li>
    * </ul>
    * </pre>
    *
    * @param propertyName the name of the property
    * @param fieldNames   the optional names of the fields in the nested type
    * @return the values of the property which may not bet set
    */
   default IPropertyValues<IPropertyValue> resolveValues(String propertyName, String... fieldNames) {
      return PropertiesUtil.resolveValues(this,
                                          p -> true,
                                          Function.identity(),
                                          propertyName,
                                          fieldNames);
   }

   /**
    * Attempts to resolve the data values of the property with the given name. Returns {@link IPropertyValues#isSet()
    * unset values} if the values cannot be resolved, including but not limited to the following cases:
    * <pre>
    * <ul>
    *    <li>The property or any of the nested fields are not defined</li>
    *    <li>The property is unset</li>
    *    <li>The type of the property is not {@link DataTypes#DATA}</li>
    *    <li>The property does not have a cardinality of {@link FieldCardinality#MANY}</li>
    *    <li>The property has an intermediate field of type {@link DataTypes#DATA} with
    *    cardinality of {@link FieldCardinality#MANY}</li>
    * </ul>
    * </pre>
    *
    * @param propertyName the name of the property
    * @param fieldNames   the optional names of the fields in the nested type
    * @return the data values of the property which may not bet set
    */
   default IPropertyValues<IPropertyDataValue> resolveAsDatas(String propertyName, String... fieldNames) {
      return PropertiesUtil.resolveValues(this,
                                          IPropertyValue::isData,
                                          p -> (IPropertyDataValue) p,
                                          propertyName,
                                          fieldNames);
   }

   /**
    * Attempts to resolve the enumeration values of the property with the given name. Returns {@link
    * IPropertyValues#isSet() unset values} if the values cannot be resolved, including but not limited to the following
    * cases:
    * <pre>
    * <ul>
    *    <li>The property or any of the nested fields are not defined</li>
    *    <li>The property is unset</li> <li>The type of the property is not {@link DataTypes#ENUM}</li>
    *    <li>The property does not have a cardinality of {@link FieldCardinality#MANY}</li>
    *    <li>The property has an intermediate field of type {@link DataTypes#DATA} with cardinality of
    *    {@link FieldCardinality#MANY}</li>
    * </ul>
    * </pre>
    *
    * @param propertyName the name of the property
    * @param fieldNames   the optional names of the fields in the nested type
    * @return the enumeration values of the property which may not bet set
    */
   default IPropertyValues<IPropertyEnumerationValue> resolveAsEnumerations(String propertyName,
                                                                            String... fieldNames) {
      return PropertiesUtil.resolveValues(this,
                                          IPropertyValue::isEnumeration,
                                          p -> (IPropertyEnumerationValue) p,
                                          propertyName,
                                          fieldNames);
   }

   /**
    * Attempts to resolve the primitive values of the property with the given name. Returns {@link
    * IPropertyValues#isSet() unset values} if the values cannot be resolved, including but not limited to the following
    * cases:
    * <pre>
    * <ul>
    *    <li>The property or any of the nested fields are not defined</li>
    *    <li>The property is unset</li>
    *    <li>The type of the property is not a primitive</li>
    *    <li>The property does not have a cardinality of {@link FieldCardinality#MANY}</li>
    *    <li>The property has an intermediate field of type {@link DataTypes#DATA} with cardinality of
    *    {@link FieldCardinality#MANY}</li>
    * </ul>
    * </pre>
    *
    * @param propertyName the name of the property
    * @param fieldNames   the optional names of the fields in the nested type
    * @return the primitive values of the property which may not bet set
    */
   default IPropertyValues<IPropertyPrimitiveValue> resolveAsPrimitives(String propertyName,
                                                                        String... fieldNames) {
      return PropertiesUtil.resolveValues(this,
                                          IPropertyValue::isPrimitive,
                                          p -> (IPropertyPrimitiveValue) p,
                                          propertyName,
                                          fieldNames);
   }

   /**
    * Attempts to resolve the integer values of the property with the given name. Returns {@link IPropertyValues#isSet()
    * unset values} if the values cannot be resolved, including but not limited to the following cases:
    * <pre>
    * <ul>
    *    <li>The property or any of the nested fields are not defined</li>
    *    <li>The property is unset</li>
    *    <li>The type of the property is not {@link DataTypes#INT}</li>
    *    <li>The property does not have a cardinality of {@link FieldCardinality#MANY}</li>
    *    <li>The property has an intermediate field of type {@link DataTypes#DATA} with cardinality of
    *    {@link FieldCardinality#MANY}</li>
    * </ul>
    * </pre>
    *
    * @param propertyName the name of the property
    * @param fieldNames   the optional names of the fields in the nested type
    * @return the integer values of the property which may not bet set
    */
   default IPropertyValues<BigInteger> resolveAsIntegers(String propertyName, String... fieldNames) {
      return PropertiesUtil.resolveValues(this,
                                          p -> p.isPrimitive() && p.getType() == DataTypes.INT,
                                          p -> ((IPropertyPrimitiveValue) p).getInteger(),
                                          propertyName,
                                          fieldNames);
   }

   /**
    * Attempts to resolve the decimal values of the property with the given name. Returns {@link IPropertyValues#isSet()
    * unset values} if the values cannot be resolved, including but not limited to the following cases:
    * <pre>
    * <ul>
    *    <li>The property or any of the nested fields are not defined</li>
    *    <li>The property is unset</li>
    *    <li>The type of the property is not {@link DataTypes#FLOAT}</li>
    *    <li>The property does not have a cardinality of {@link FieldCardinality#MANY}</li>
    *    <li>The property has an intermediate field of type {@link DataTypes#DATA} with cardinality of
    *    {@link FieldCardinality#MANY}</li>
    * </ul>
    * </pre>
    *
    * @param propertyName the name of the property
    * @param fieldNames   the optional names of the fields in the nested type
    * @return the decimal values of the property which may not bet set
    */
   default IPropertyValues<BigDecimal> resolveAsDecimals(String propertyName, String... fieldNames) {
      return PropertiesUtil.resolveValues(this,
                                          p -> p.isPrimitive() && p.getType() == DataTypes.FLOAT,
                                          p -> ((IPropertyPrimitiveValue) p).getDecimal(),
                                          propertyName,
                                          fieldNames);
   }

   /**
    * Attempts to resolve the boolean values of the property with the given name. Returns {@link IPropertyValues#isSet()
    * unset values} if the values cannot be resolved, including but not limited to the following cases:
    * <pre>
    * <ul>
    *    <li>The property or any of the nested fields are not defined</li>
    *    <li>The property is unset</li>
    *    <li>The type of the property is not {@link DataTypes#BOOLEAN}</li>
    *    <li>The property does not have a cardinality of {@link FieldCardinality#MANY}</li>
    *    <li>The property has an intermediate field of type {@link DataTypes#DATA} with cardinality of
    *    {@link FieldCardinality#MANY}</li>
    * </ul>
    * </pre>
    *
    * @param propertyName the name of the property
    * @param fieldNames   the optional names of the fields in the nested type
    * @return the boolean values of the property which may not bet set
    */
   default IPropertyValues<Boolean> resolveAsBooleans(String propertyName, String... fieldNames) {
      return PropertiesUtil.resolveValues(this,
                                          p -> p.isPrimitive() && p.getType() == DataTypes.BOOLEAN,
                                          p -> ((IPropertyPrimitiveValue) p).getBoolean(),
                                          propertyName,
                                          fieldNames);
   }

   /**
    * Attempts to resolve the string values of the property with the given name. Returns {@link Optional#empty()} if the
    * values cannot be resolved, including but not limited to the following cases:
    * <pre>
    * <ul>
    *    <li>The property or any of the nested fields are not defined</li>
    *    <li>The property is unset</li> <li>The type of the property is not {@link DataTypes#STRING}</li>
    *    <li>The property does not have a cardinality of {@link FieldCardinality#MANY}</li>
    *    <li>The property has an intermediate field of type {@link DataTypes#DATA} with cardinality of
    *    {@link FieldCardinality#MANY}</li>
    * </ul>
    * </pre>
    *
    * @param propertyName the name of the property
    * @param fieldNames   the optional names of the fields in the nested type
    * @return the string values of the property which may not bet set
    */
   default IPropertyValues<String> resolveAsStrings(String propertyName, String... fieldNames) {
      return PropertiesUtil.resolveValues(this,
                                          p -> p.isPrimitive() && p.getType() == DataTypes.STRING,
                                          p -> ((IPropertyPrimitiveValue) p).getString(),
                                          propertyName,
                                          fieldNames);
   }
}

