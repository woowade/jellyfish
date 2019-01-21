/**
 * UNCLASSIFIED
 * Northrop Grumman Proprietary
 * ____________________________
 * 
 * Copyright (C) 2019, Northrop Grumman Systems Corporation
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
package com.model2.tests.di;

import com.google.inject.AbstractModule;

import com.model2.api.IModel2Adviser;
import com.model2.tests.config.Model2TestTransportConfiguration;

/**
 * This module configures Guice bindings for the Model2 steps.
 */
public class Model2TestModule extends AbstractModule {

   @Override
   protected void configure() {
      bind(Model2TestTransportConfiguration.class).asEagerSingleton();
      bind(IModel2Adviser.class).to(Model2TestAdviser.class);
   }
}
