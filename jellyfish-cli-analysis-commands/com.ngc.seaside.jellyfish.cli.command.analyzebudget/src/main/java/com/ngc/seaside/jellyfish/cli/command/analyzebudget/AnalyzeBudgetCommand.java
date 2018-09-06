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
package com.ngc.seaside.jellyfish.cli.command.analyzebudget;

import com.ngc.seaside.jellyfish.api.CommonParameters;
import com.ngc.seaside.jellyfish.api.DefaultUsage;
import com.ngc.seaside.jellyfish.api.IUsage;
import com.ngc.seaside.jellyfish.cli.command.analyzebudget.budget.Budget;
import com.ngc.seaside.jellyfish.cli.command.analyzebudget.budget.BudgetResult;
import com.ngc.seaside.jellyfish.cli.command.analyzebudget.budget.SdBudgetAdapter;
import com.ngc.seaside.jellyfish.service.analysis.api.SystemDescriptorFinding;
import com.ngc.seaside.jellyfish.utilities.command.AbstractJellyfishAnalysisCommand;
import com.ngc.seaside.systemdescriptor.model.api.model.IModel;
import com.ngc.seaside.systemdescriptor.service.source.api.ISourceLocation;

import java.util.Set;
import java.util.concurrent.ForkJoinPool;

import javax.measure.Quantity;

public class AnalyzeBudgetCommand extends AbstractJellyfishAnalysisCommand {

   public static final String NAME = "analyze-budget";

   private SdBudgetAdapter budgetAdapter;

   public AnalyzeBudgetCommand() {
      super(NAME);
   }

   public void setBudgetAdapter(SdBudgetAdapter ref) {
      this.budgetAdapter = ref;
   }

   @Override
   protected void analyzeModel(IModel topModel) {
      ForkJoinPool commonPool = ForkJoinPool.commonPool();

      BudgetAnalysisTask task = new BudgetAnalysisTask(topModel, budgetAdapter);
      Set<BudgetResult<? extends Quantity<?>>> results = commonPool.invoke(task);

      for (BudgetResult<? extends Quantity<?>> result : results) {
         createIssue(result);
      }
   }

   /**
    * Creates an issue when the total is outside the budget.
    * 
    * @param result budget result
    */
   private <T extends Quantity<T>> void createIssue(BudgetResult<T> result) {
      Budget<T> budget = result.getBudget();
      String message;
      String budgetScope = result.overBudget() ? "over" : (result.withinBudget() ? "within" : "under");
      message = budget.getProperty() + " is " + budgetScope + " budget: "
               + result.getActual() + ". Budget range: [" + budget.getMinimum() + ", " + budget.getMaximum() + "]";
      ISourceLocation location = sourceLocatorService.getLocation(budget.getSource(), false);
      double complexity = computeComplexity(result);

      SystemDescriptorFinding<?> finding;
      if (result.withinBudget()) {
         finding = BudgetFindingTypes.WITHIN_BUDGET_ANALYSIS.createFinding(message, location, complexity);
      } else {
         finding = BudgetFindingTypes.OUT_OF_BUDGET_ANALYSIS.createFinding(message, location, complexity);
      }
      super.reportFinding(finding);
   }

   /**
    * Returns the complexity of the issue. This implementation returns the relative difference between the actual and
    * the minimum or maximum out of {@code 100}.
    * 
    * @param result budget result
    * @return complexity
    */
   private static <T extends Quantity<T>> double computeComplexity(BudgetResult<T> result) {
      if (result.withinBudget()) {
         return 0;
      }
      Budget<T> budget = result.getBudget();
      Quantity<T> actual = result.getActual();
      Quantity<T> expected;
      if (result.overBudget()) {
         expected = budget.getMaximum();
      } else {
         expected = budget.getMinimum();
      }
      Quantity<T> divisor;
      if (expected.getValue().doubleValue() == 0) {
         divisor = actual;
      } else {
         divisor = expected;
      }
      double relativeError = expected.subtract(actual).divide(divisor).getValue().doubleValue();
      return Math.abs(relativeError);
   }

   @Override
   protected IUsage createUsage() {
      return new DefaultUsage("Analyzes the budgets of a model and its parts. This command is rarely ran directly;"
               + " instead it is run with the 'analyze' command.",
               CommonParameters.MODEL,
               CommonParameters.STEREOTYPES);
   }

}
