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
package com.ngc.seaside.jellyfish.cli.gradle;

import groovy.lang.Closure;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DerivedRootProjectExtension {

   public static final String DEFAULT_SYSTEM_DESCRIPTOR_DIRECTORY_NAME = "models";
   public static final String DEFAULT_SYSTEM_DESCRIPTOR_TEST_DIRECTORY_NAME = "features";

   private String model;
   private String deploymentModel;
   private Dependency project;
   private File directory;
   private File testDirectory;
   private final List<DerivedRootProjectPartsExtension> parts = new ArrayList<>();

   private final Project gradleProject;

   /**
    * Constructor
    * 
    * @param project used to construct this object
    */
   public DerivedRootProjectExtension(Project project) {
      this.gradleProject = project;
      this.directory = new File(project.getBuildDir(), DEFAULT_SYSTEM_DESCRIPTOR_DIRECTORY_NAME);
      this.testDirectory = new File(project.getBuildDir(), DEFAULT_SYSTEM_DESCRIPTOR_TEST_DIRECTORY_NAME);
   }

   /**
    * The system descriptor project dependency.
    * 
    * @return system descriptor project dependency
    */
   public Dependency getProject() {
      return project;
   }

   /**
    * Sets the system descriptor project dependency. The dependency is evaluated using
    * {@link DependencyHandler#create(Object)}.
    *
    * @param dependency the dependency for the project
    * @return this
    */
   public DerivedRootProjectExtension setProject(Object dependency) {
      if (dependency == null) {
         project = null;
      } else {
         project = gradleProject.getDependencies().create(dependency);
      }
      return this;
   }

   /**
    * Returns the system descriptor model name.
    * 
    * @return the system descriptor model name
    */
   public String getModel() {
      return model;
   }

   /**
    * Sets the system descriptor model name.
    * 
    * @param model system descriptor model name
    * @return this
    */
   public DerivedRootProjectExtension setModel(String model) {
      this.model = model;
      return this;
   }

   /**
    * Returns the system descriptor deployment model name.
    * 
    * @return the system descriptor deployment model name
    */
   public String getDeploymentModel() {
      return deploymentModel;
   }

   /**
    * Sets the system descriptor deployment model name.
    * 
    * @param deploymentModel system descriptor deployment model name
    * @return this
    */
   public DerivedRootProjectExtension setDeploymentModel(String deploymentModel) {
      this.deploymentModel = deploymentModel;
      return this;
   }

   /**
    * Returns the directory to copy the system descriptor project into.
    * 
    * @return the directory to copy the system descriptor project into
    */
   public File getDirectory() {
      return directory;
   }

   /**
    * Sets the directory to copy the system descriptor test files into.
    * 
    * @param file the directory to copy the system descriptor test files into
    * @return this
    */
   public DerivedRootProjectExtension setDirectory(File file) {
      this.directory = file;
      return this;
   }

   /**
    * Returns the directory to copy the system descriptor test files into.
    * 
    * @return the directory to copy the system descriptor test files into
    */
   public File getTestDirectory() {
      return testDirectory;
   }

   /**
    * Sets the directory to copy the system descriptor test files into.
    * 
    * @param file the directory to copy the system descriptor test files into
    * @return this
    */
   public DerivedRootProjectExtension setTestDirectory(File file) {
      this.testDirectory = file;
      return this;
   }

   /**
    * Returns the parts of the system descriptor model.
    * 
    * @return the parts of the system descriptor model
    */
   public Collection<DerivedRootProjectPartsExtension> getParts() {
      return parts;
   }

   /**
    * Adds a part to the system descriptor model and applies the given action to it.
    * 
    * @param action action
    * @return this
    */
   public DerivedRootProjectExtension part(Action<DerivedRootProjectPartsExtension> action) {
      DerivedRootProjectPartsExtension part = new DerivedRootProjectPartsExtension(gradleProject);
      action.execute(part);
      parts.add(part);
      return this;
   }

   /**
    * Adds a part to the system descriptor model and applies the given closure to it.
    * 
    * @param closure closure
    * @return this
    */
   public DerivedRootProjectExtension part(Closure<?> closure) {
      DerivedRootProjectPartsExtension part = new DerivedRootProjectPartsExtension(gradleProject);
      closure.setDelegate(part);
      closure.setResolveStrategy(Closure.DELEGATE_FIRST);
      closure.call(part);
      parts.add(part);
      return this;
   }

}
