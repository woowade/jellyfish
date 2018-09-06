/**
 * UNCLASSIFIED
 * Northrop Grumman Proprietary
 * ____________________________
 *
 * Copyright (C) 2018, Northrop Grumman Systems Corporation
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of
 * Northrop Grumman Systems Corporation. The intellectual and technical concepts
 * contained herein are proprietary to Northrop Grumman Systems Corporation and
 * may be covered by U.S. and Foreign Patents or patents in process, and are
 * protected by trade secret or copyright law. Dissemination of this information
 * or reproduction of this material is strictly forbidden unless prior written
 * permission is obtained from Northrop Grumman.
 */
package com.ngc.seaside.systemdescriptor.model.api.data;

import com.ngc.seaside.systemdescriptor.model.api.INamedChild;
import com.ngc.seaside.systemdescriptor.model.api.IPackage;
import com.ngc.seaside.systemdescriptor.model.api.metadata.IMetadata;

import java.util.Collection;

/**
 * Represents a set of discrete values that are referenced in data.
 */
public interface IEnumeration extends INamedChild<IPackage> {

   /**
    * Gets the metadata associated with this data type.
    *
    * @return the metadata associated with this data type
    */
   IMetadata getMetadata();

   /**
    * Sets the metadata associated with this data type.
    *
    * @param metadata the metadata associated with this data type
    * @return this data type
    */
   IEnumeration setMetadata(IMetadata metadata);

   /**
    * Gets the values in the order they are declared.  The returned list may not be modifiable if this object is
    * immutable.
    *
    * @return the values in the order they are declared
    */
   Collection<String> getValues();

   /**
    * Gets the fully qualified name of this enumeration type.  The fully qualified name is the name of the parent
    * package, appended with ".", appended with the name of this enumeration type.  For example, the fully qualified
    * name of a enumeration type named "HelloWorld" which resides in the package named "my.package" would be
    * "my.package.HelloWorld".
    *
    * @return the fully qualified name of this data type
    */
   String getFullyQualifiedName();
}
