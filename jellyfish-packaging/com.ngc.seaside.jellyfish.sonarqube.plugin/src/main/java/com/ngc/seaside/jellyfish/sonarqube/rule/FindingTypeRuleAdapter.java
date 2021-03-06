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
package com.ngc.seaside.jellyfish.sonarqube.rule;

import com.google.common.base.Preconditions;

import com.ngc.seaside.jellyfish.service.analysis.api.IReportingOutputService;
import com.ngc.seaside.jellyfish.service.analysis.api.ISystemDescriptorFindingType;

import org.sonar.api.rule.RuleKey;
import org.sonar.api.rule.RuleStatus;
import org.sonar.api.rule.Severity;
import org.sonar.api.rules.RuleType;
import org.sonar.api.server.rule.RulesDefinition;

/**
 * Adapts instances of Jellyfish {@code ISystemDescriptorFindingType} to a Sonarqube {@code AbstractRule}.
 */
public class FindingTypeRuleAdapter extends AbstractRule {

   /**
    * The finding type that is being adapted.
    */
   private final ISystemDescriptorFindingType findingType;

   private IReportingOutputService reportingOutputService;

   /**
    * Creates a new adapter for the given finding type.
    *
    * @param findingType the finding type to adapt
    */
   public FindingTypeRuleAdapter(ISystemDescriptorFindingType findingType) {
      super(RuleKey.of(SystemDescriptorRulesDefinition.REPOSITORY_KEY, findingType.getId()));
      this.findingType = findingType;
   }

   /**
    * Gets the finding type that is being adapted.
    *
    * @return the finding type begin adapted
    */
   public ISystemDescriptorFindingType getFindingType() {
      return findingType;
   }

   public void setReportingOutputService(IReportingOutputService reportingOutputService) {
      Preconditions.checkNotNull(reportingOutputService, "reportingOutputService may not be null!");
      this.reportingOutputService = reportingOutputService;
   }

   @Override
   protected void configure(RulesDefinition.NewRule rule) {
      Preconditions.checkNotNull(reportingOutputService,
                                 "reportingOutputService is null! Did you call setReportingOutputService()?");

      // Do the actual mapping between the Jellyfish API and Sonarqube here.
      rule.setName(findingType.getId())
            .setHtmlDescription(reportingOutputService.convert(findingType.getDescription()))
            .setSeverity(convert(findingType.getSeverity()))
            // Make all Jellyfish rules ready.
            .setStatus(RuleStatus.READY)
            // By default, we make all Jellyfish rules code smells.
            .setType(RuleType.CODE_SMELL)
            // Use a default linear dept function with a "gap" 1 hour.  When we create each issue
            // we'll use the complexity of the SystemDescriptorFinding as the "gap" value on the Sonarqube issue.
            // This means that complexity values in SystemDescriptorFinding are effectively units of hours.
            .setDebtRemediationFunction(rule.debtRemediationFunctions().linear("1 h"));
   }

   private static String convert(ISystemDescriptorFindingType.Severity severity) {
      switch (severity) {
         case ERROR:
            return Severity.MAJOR;
         case WARNING:
            return Severity.MINOR;
         case INFO:
            // Intentionally fall-through
         default:
            return Severity.INFO;
      }
   }
}
