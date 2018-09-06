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
package com.ngc.seaside.jellyfish.service.property.api;

import java.io.IOException;
import java.nio.file.Path;

/**
 * This class provides common operations for dealing with properties.
 *
 * <p> The properties can contain dynamic data similar to the below.
 * groupId=com.ngc.seaside
 * artifactId=mybundle
 * package=$groupId.$artifactId
 * capitalized=${package.toUpperCase()} </p>
 *
 *<P> Any operation that a java.util.String can perform can be set in the property file and called on the property
 * A more elaborate example below will replace all the periods in a package with a - and convert it to upper case.<br>
 * ${package.trim().toUpperCase().replace(".", "-")} </P>
 */
public interface IPropertyService {

   /**
    * Load the given property file and return the contents as a map.
    * The iterator for the map should return the items in the order in which they are in the file.
    *
    * @param propertiesFile the path to the property file.
    * @return the Map of properties.
    * @throws IOException if the file is not found or not a valid property file.
    */
   IProperties load(Path propertiesFile) throws IOException;
}
