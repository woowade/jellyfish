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
package com.ngc.seaside.systemdescriptor.model.impl.xtext.model.properties;

import com.ngc.seaside.systemdescriptor.model.impl.xtext.exception.UnrecognizedXtextTypeException;
import com.ngc.seaside.systemdescriptor.systemDescriptor.BaseLinkDeclaration;
import com.ngc.seaside.systemdescriptor.systemDescriptor.DeclarationDefinition;
import com.ngc.seaside.systemdescriptor.systemDescriptor.FieldDeclaration;
import com.ngc.seaside.systemdescriptor.systemDescriptor.LinkDeclaration;
import com.ngc.seaside.systemdescriptor.systemDescriptor.Model;
import com.ngc.seaside.systemdescriptor.systemDescriptor.Properties;
import com.ngc.seaside.systemdescriptor.systemDescriptor.PropertyValueAssignment;
import com.ngc.seaside.systemdescriptor.systemDescriptor.PropertyValueExpressionPathSegment;
import com.ngc.seaside.systemdescriptor.systemDescriptor.ReferencedPropertyFieldDeclaration;
import com.ngc.seaside.systemdescriptor.systemDescriptor.RefinedLinkDeclaration;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

public class NestedPropertyValueResolver {

   protected final Collection<Properties> propertiesToSearch = new ArrayList<>();
   protected final ReferencedPropertyFieldDeclaration declaration;

   /**
    * Creates a new resolver.
    */
   public NestedPropertyValueResolver(ReferencedPropertyFieldDeclaration declaration,
                                      Properties properties) {
      this.declaration = declaration;
      if (arePropertiesOfModel(properties)) {
         populatePropertiesFromModel(properties);
      } else if (arePropertiesOfLink(properties)) {
         populatePropertiesFromLink(properties);
      } else if (arePropertiesOfField(properties)) {
         populatePropertiesFromField(properties);
      } else {
         handleUnrecognizedPropertiesContainer(properties);
      }
   }

   /**
    * Attempts to resolve a value by traversing the given path segments.
    */
   public Optional<PropertyValueAssignment> resoleValue(Collection<String> fieldNames) {
      String flatPath = fieldNames.stream().collect(Collectors.joining("."));
      return propertiesToSearch.stream()
            .flatMap(p -> p.getAssignments().stream())
            .filter(a -> a.getExpression().getDeclaration().equals(declaration))
            .filter(a -> arePathsSame(flatPath, a.getExpression().getPathSegments()))
            .findFirst();
   }

   private static boolean arePathsSame(String flatPath,
                                       Collection<PropertyValueExpressionPathSegment> segments) {
      String flatSegmentPath = segments.stream()
            .map(s -> s.getFieldDeclaration().getName())
            .collect(Collectors.joining("."));
      return flatPath.equals(flatSegmentPath);
   }

   private void populatePropertiesFromModel(Properties properties) {
      Model model = ((Model) properties.eContainer());

      while (model != null) {
         if (model.getProperties() != null) {
            propertiesToSearch.add(model.getProperties());
         }
         model = model.getRefinedModel();
      }
   }

   private void populatePropertiesFromLink(Properties properties) {
      LinkDeclaration link = (LinkDeclaration) properties.eContainer() // DeclarationDefinition
            .eContainer(); // Link
      Model model = (Model) link.eContainer() // Links
            .eContainer(); // Model

      while (model != null) {
         LinkDeclaration currentLink = findLink(model, link);

         if (currentLink != null
               && currentLink.getDefinition() != null
               && currentLink.getDefinition().getProperties() != null) {
            propertiesToSearch.add(currentLink.getDefinition().getProperties());
         }
         model = model.getRefinedModel();
      }
   }

   protected void handleUnrecognizedPropertiesContainer(Properties properties) {
      // Tests will override this method to make them easier to write.
      throw new UnrecognizedXtextTypeException(properties.eContainer());
   }

   private void populatePropertiesFromField(Properties properties) {
      FieldDeclaration field = (FieldDeclaration) properties.eContainer() // DeclarationDefinition
            .eContainer(); // FieldDeclaration
      Model model = (Model) field.eContainer() // Parts or requirements
            .eContainer(); // Model

      while (model != null) {
         Collection<FieldDeclaration> fields = new ArrayList<>();
         if (model.getParts() != null) {
            fields.addAll(model.getParts().getDeclarations());
         }
         if (model.getRequires() != null) {
            fields.addAll(model.getRequires().getDeclarations());
         }

         FieldDeclaration currentField = fields.stream()
               .filter(f -> f.getName().equals(field.getName()))
               .findFirst()
               .orElse(null);

         if (currentField != null
               && currentField.getDefinition() != null
               && currentField.getDefinition().getProperties() != null) {
            propertiesToSearch.add(currentField.getDefinition().getProperties());
         }
         model = model.getRefinedModel();
      }
   }

   private static boolean arePropertiesOfModel(Properties properties) {
      return properties.eContainer() instanceof Model;
   }

   private static boolean arePropertiesOfLink(Properties properties) {
      return properties.eContainer() instanceof DeclarationDefinition
            && properties.eContainer().eContainer() instanceof LinkDeclaration;
   }

   private static boolean arePropertiesOfField(Properties properties) {
      return properties.eContainer() instanceof DeclarationDefinition
            && properties.eContainer().eContainer() instanceof FieldDeclaration;
   }

   private static LinkDeclaration findLink(Model model, LinkDeclaration linkDeclaration) {
      if (model.getLinks() == null) {
         return null;
      }

      if (linkDeclaration.getName() != null) {
         for (LinkDeclaration link : model.getLinks().getDeclarations()) {
            if (linkDeclaration.getName().equals(link.getName())) {
               return link;
            }

         }

         return null;
      }

      EObject source;
      EObject target;
      if (linkDeclaration instanceof BaseLinkDeclaration) {
         source = ((BaseLinkDeclaration) linkDeclaration).getSource();
         target = ((BaseLinkDeclaration) linkDeclaration).getTarget();
      } else {
         source = ((RefinedLinkDeclaration) linkDeclaration).getSource();
         target = ((RefinedLinkDeclaration) linkDeclaration).getTarget();
      }

      for (LinkDeclaration link : model.getLinks().getDeclarations()) {
         if (link instanceof BaseLinkDeclaration
               && EcoreUtil.equals(source, ((BaseLinkDeclaration) link).getSource())
               && EcoreUtil.equals(target, ((BaseLinkDeclaration) link).getTarget())) {
            return link;
         } else if (link instanceof RefinedLinkDeclaration
               && EcoreUtil.equals(source, ((RefinedLinkDeclaration) link).getSource())
               && EcoreUtil.equals(target, ((RefinedLinkDeclaration) link).getTarget())) {
            return link;
         }
      }

      return null;
   }
}
