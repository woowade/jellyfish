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
package com.ngc.seaside.systemdescriptor.validation;

import com.google.common.base.Objects;
import com.google.inject.Inject;

import com.ngc.seaside.systemdescriptor.systemDescriptor.BaseLinkDeclaration;
import com.ngc.seaside.systemdescriptor.systemDescriptor.LinkDeclaration;
import com.ngc.seaside.systemdescriptor.systemDescriptor.Links;
import com.ngc.seaside.systemdescriptor.systemDescriptor.Model;
import com.ngc.seaside.systemdescriptor.systemDescriptor.RefinedLinkDeclaration;
import com.ngc.seaside.systemdescriptor.systemDescriptor.RefinedLinkNameDeclaration;
import com.ngc.seaside.systemdescriptor.systemDescriptor.SystemDescriptorPackage;
import com.ngc.seaside.systemdescriptor.utils.SdUtils;

import org.eclipse.xtext.naming.IQualifiedNameProvider;
import org.eclipse.xtext.validation.Check;

import java.util.HashSet;
import java.util.Set;

public class RefinedLinkValidator extends AbstractUnregisteredSystemDescriptorValidator {

   @Inject
   private IQualifiedNameProvider nameProvider;

   /**
    * Validates the refined link actually refines a link.
    */
   @Check
   public void checkRefinedLinkedDeclaration(RefinedLinkDeclaration link) {
      Model model = (Model) link
            .eContainer() // Links
            .eContainer(); // Model

      if (checkModelRefinesAnotherModel(link, model)) {
         // Only do these checks if a model is being refined.
         checkLinkDoesNotDeclareANewName(link, model);
         checkLinkRefinesABaseLink(link, model);
      }
   }

   /**
    * Validates the refined link actually refines a link.
    */
   @Check
   public void checkRefinedLinkedDeclaration(RefinedLinkNameDeclaration link) {
      Model model = (Model) link
            .eContainer() // Links
            .eContainer(); // Model
      if (checkModelRefinesAnotherModel(link, model)) {
         // Only do these checks if a model is being refined.
         checkLinkIsDeclaredInRefinedModel(link, model);
      }
   }

   private boolean checkModelRefinesAnotherModel(LinkDeclaration link, Model model) {
      boolean refinesModel = model.getRefinedModel() != null;

      if (!refinesModel) {
         Links links = (Links) link.eContainer();
         String msg = String.format(
               "Cannot refine a link because the model '%s' does not refine another model.",
               nameProvider.getFullyQualifiedName(model));
         int index = links.getDeclarations().indexOf(link);
         error(msg, links, SystemDescriptorPackage.Literals.LINKS__DECLARATIONS, index);
      }

      return refinesModel;
   }

   private void checkLinkIsDeclaredInRefinedModel(RefinedLinkNameDeclaration link, Model model) {
      Set<String> linkNames = new HashSet<>();

      Model refinedModel = model.getRefinedModel();
      while (refinedModel != null) {
         if (refinedModel.getLinks() != null) {
            refinedModel.getLinks().getDeclarations()
                  .stream()
                  .filter(l -> l.getName() != null)
                  .forEach(l -> linkNames.add(l.getName()));
         }

         refinedModel = refinedModel.getRefinedModel();
      }

      if (!linkNames.contains(link.getName())) {
         String msg = String.format(
               "No link named '%s' declared in the refinement hierarchy of '%s'.",
               link.getName(),
               nameProvider.getFullyQualifiedName(model));
         error(msg, link, SystemDescriptorPackage.Literals.LINK_DECLARATION__NAME);
      }
   }

   private void checkLinkDoesNotDeclareANewName(RefinedLinkDeclaration link, Model model) {
      BaseLinkDeclaration baseLink = getBaseLinkDeclaration(link, model);
      if (baseLink != null
            && !Objects.equal(link.getName(), baseLink.getName())) {
         Model baseLinkModel = (Model) baseLink
               .eContainer() // Links
               .eContainer(); // Model
         String msg = String.format(
               "Cannot change the name of a refined link; the model '%s'"
                     + " declares the same link with the name '%s'.",
               nameProvider.getFullyQualifiedName(baseLinkModel),
               baseLink.getName());
         error(msg, link, SystemDescriptorPackage.Literals.LINK_DECLARATION__NAME);
      }
   }

   private void checkLinkRefinesABaseLink(RefinedLinkDeclaration link, Model model) {
      if (getBaseLinkDeclaration(link, model) == null) {
         Links links = (Links) link.eContainer();
         String msg = String.format(
               "Cannot refine a link that is not declared in the refinement"
                     + " hierarchy of '%s'.",
               nameProvider.getFullyQualifiedName(model));
         int index = links.getDeclarations().indexOf(link);
         error(msg, links, SystemDescriptorPackage.Literals.LINKS__DECLARATIONS, index);
      }
   }

   private static BaseLinkDeclaration getBaseLinkDeclaration(
         RefinedLinkDeclaration refinedLink,
         Model model) {
      BaseLinkDeclaration baseLink = null;

      String refinedLinkRawSource = SdUtils.getRawSource(
            refinedLink,
            SystemDescriptorPackage.Literals.BASE_LINK_DECLARATION__SOURCE);
      String refinedLinkRawTarget = SdUtils.getRawSource(
            refinedLink,
            SystemDescriptorPackage.Literals.BASE_LINK_DECLARATION__TARGET);

      model = model.getRefinedModel();
      while (model != null && baseLink == null) {
         if (model.getLinks() != null) {
            baseLink = model.getLinks().getDeclarations()
                  .stream()
                  .filter(l -> l instanceof BaseLinkDeclaration)
                  .map(l -> (BaseLinkDeclaration) l)
                  .filter(l -> areLinksEqual(l, refinedLinkRawSource, refinedLinkRawTarget))
                  .findFirst()
                  .orElse(null);
         }

         model = model.getRefinedModel();
      }

      return baseLink;
   }

   private static boolean areLinksEqual(
         BaseLinkDeclaration baseLink,
         String refinedLinkRawSource,
         String refinedLinkRawTarget) {
      String baseLinkRawSource = SdUtils.getRawSource(
            baseLink,
            SystemDescriptorPackage.Literals.BASE_LINK_DECLARATION__SOURCE);
      String baseLinkRawTarget = SdUtils.getRawSource(
            baseLink,
            SystemDescriptorPackage.Literals.BASE_LINK_DECLARATION__TARGET);
      return refinedLinkRawSource.equals(baseLinkRawSource) && refinedLinkRawTarget.equals(baseLinkRawTarget);
   }
}
