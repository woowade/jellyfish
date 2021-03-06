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
package com.ngc.seaside.systemdescriptor.model.impl.xtext.data;

import com.ngc.seaside.systemdescriptor.model.api.FieldCardinality;
import com.ngc.seaside.systemdescriptor.model.api.IPackage;
import com.ngc.seaside.systemdescriptor.model.api.data.DataTypes;
import com.ngc.seaside.systemdescriptor.model.api.data.IData;
import com.ngc.seaside.systemdescriptor.model.api.data.IDataField;
import com.ngc.seaside.systemdescriptor.model.api.metadata.IMetadata;
import com.ngc.seaside.systemdescriptor.model.impl.xtext.AbstractWrappedXtextTest;
import com.ngc.seaside.systemdescriptor.systemDescriptor.Data;
import com.ngc.seaside.systemdescriptor.systemDescriptor.Package;
import com.ngc.seaside.systemdescriptor.systemDescriptor.PrimitiveDataFieldDeclaration;
import com.ngc.seaside.systemdescriptor.systemDescriptor.PrimitiveDataType;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WrappedDataTest extends AbstractWrappedXtextTest {

   private WrappedData wrappedData;

   private Data data;

   private Data superType;

   @Mock
   private IPackage parent;

   @Mock
   private IPackage superTypePackage;

   @Mock
   private IData wrappedSuperType;

   @Before
   public void setup() throws Throwable {
      data = factory().createData();
      data.setName("Foo");

      superType = factory().createData();
      superType.setName("Super");

      PrimitiveDataFieldDeclaration field = factory().createPrimitiveDataFieldDeclaration();
      field.setName("field1");
      field.setType(PrimitiveDataType.STRING);
      data.getFields().add(field);

      Package p = factory().createPackage();
      p.setName("my.package");
      p.setElement(data);
      when(resolver().getWrapperFor(p)).thenReturn(parent);

      Package superP = factory().createPackage();
      superP.setName("my.super.package");
      superP.setElement(superType);

      when(resolver().getWrapperFor(superType)).thenReturn(wrappedSuperType);
   }

   @Test
   public void testDoesWrapXtextObject() throws Throwable {
      wrappedData = new WrappedData(resolver(), data);
      assertEquals("name not correct!",
                   wrappedData.getName(),
                   data.getName());
      assertEquals("fully qualified name not correct!",
                   "my.package.Foo",
                   wrappedData.getFullyQualifiedName());
      assertEquals("parent not correct!",
                   parent,
                   wrappedData.getParent());
      assertEquals("metadata not set!",
                   IMetadata.EMPTY_METADATA,
                   wrappedData.getMetadata());
      assertFalse("superType should not be set!",
                  wrappedData.getExtendedDataType().isPresent());

      String fieldName = data.getFields().get(0).getName();
      assertEquals("did not get fields!",
                   fieldName,
                   wrappedData.getFields().getByName(fieldName).get().getName());
   }

   @Test
   public void testDoesWrapXtextObjectWithSuperType() throws Throwable {
      data.setExtendedDataType(superType);
      wrappedData = new WrappedData(resolver(), data);
      assertEquals("did not return wrapper for superType!",
                   wrappedSuperType,
                   wrappedData.getExtendedDataType().get());
   }

   @Test
   public void testDoesUpdateXtextObject() throws Throwable {
      wrappedData = new WrappedData(resolver(), data);
      wrappedData.setMetadata(newMetadata("foo", "bar"));
      assertNotNull("metadata not set!",
                    data.getMetadata());
   }

   @Test
   public void testDoesUpdateXtextObjectWithSuperType() throws Throwable {
      String superTypePackageName = ((Package) superType.eContainer()).getName();
      when(resolver().findXTextData(superType.getName(), superTypePackageName))
            .thenReturn(Optional.of(superType));
      when(wrappedSuperType.getName()).thenReturn(superType.getName());
      when(wrappedSuperType.getParent()).thenReturn(superTypePackage);
      when(superTypePackage.getName()).thenReturn(superTypePackageName);

      data.setExtendedDataType(superType);
      wrappedData = new WrappedData(resolver(), data);
      wrappedData.setExtendedDataType(wrappedSuperType);
      assertEquals("did not update superType!",
                   superType.getName(),
                   wrappedData.getExtendedDataType().get().getName());
   }
}
