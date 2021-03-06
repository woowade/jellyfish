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
package com.ngc.seaside.jellyfish.cli.command.analyze;

import com.ngc.seaside.jellyfish.api.DefaultParameter;
import com.ngc.seaside.jellyfish.api.DefaultUsage;
import com.ngc.seaside.jellyfish.api.ICommand;
import com.ngc.seaside.jellyfish.api.ICommandOptions;
import com.ngc.seaside.jellyfish.api.ICommandProvider;
import com.ngc.seaside.jellyfish.api.IJellyFishCommandProvider;
import com.ngc.seaside.jellyfish.api.IParameter;
import com.ngc.seaside.jellyfish.api.IUsage;
import com.ngc.seaside.jellyfish.api.ParameterCategory;
import com.ngc.seaside.jellyfish.utilities.command.AbstractJellyfishCommand;

import java.util.Arrays;
import java.util.Collection;

/**
 * The top level analyze command.  This command uses the {@code analyses} and {@code reports} parameters to call
 * one or more analysis and reporting commands.  This command does little work itself.  It mostly delegates to other
 * commands.
 */
public class AnalyzeCommand extends AbstractJellyfishCommand {

   /**
    * The name of the command.
    */
   public static final String NAME = "analyze";

   /**
    * The parameter that controls the analyses to run.
    */
   public static final String ANALYSES_PARAMETER_NAME = "analyses";

   /**
    * The parameter that controls the reports to generate.
    */
   public static final String REPORTS_PARAMETER_NAME = "reports";

   /**
    * The delimiter that separates the analyses and reports commands.
    */
   private static final String COMMAND_DELIMITER = ",";

   /**
    * Used to invoke commands which are Jellyfish commands.  Most analysis commands are Jellyfish commands.
    */
   private IJellyFishCommandProvider jellyFishCommandProvider;

   /**
    * Used to invoke commands which are basic non-Jellyfish commands.  These commands don't require a valid system
    * descriptor project to execute.  Most reports are basic commands.
    */
   private ICommandProvider<ICommandOptions, ICommand<ICommandOptions>, ICommandOptions> commandProvider;

   /**
    * Creates a new command.
    */
   public AnalyzeCommand() {
      super(NAME);
   }

   @Override
   public void activate() {
      logService.debug(AnalyzeCommand.class, "Activated.");
   }

   @Override
   public void deactivate() {
      logService.debug(AnalyzeCommand.class, "Deactivated.");
   }

   /**
    * Sets the Jellyfish command provider.
    */
   public void setJellyFishCommandProvider(IJellyFishCommandProvider ref) {
      this.jellyFishCommandProvider = ref;
   }

   /**
    * Removes the Jellyfish command provider.
    */
   public void removeJellyFishCommandProvider(IJellyFishCommandProvider ref) {
      setJellyFishCommandProvider(null);
   }

   /**
    * Sets the basic command provider.
    */
   public void setCommandProvider(
         ICommandProvider<ICommandOptions, ICommand<ICommandOptions>, ICommandOptions> ref) {
      this.commandProvider = ref;
   }

   /**
    * Removes the basic command provider.
    */
   public void removeCommandProvider(
         ICommandProvider<ICommandOptions, ICommand<ICommandOptions>, ICommandOptions> ref) {
      setCommandProvider(null);
   }

   @Override
   protected IUsage createUsage() {
      return new DefaultUsage(
            "Runs various types of analysis and generates reports",
            new DefaultParameter<>(ANALYSES_PARAMETER_NAME)
                  .setDescription("Configures the analysis to execute.  The values are comma (,) separated.")
                  .setParameterCategory(ParameterCategory.REQUIRED),
            new DefaultParameter<>(REPORTS_PARAMETER_NAME)
                  .setDescription("Configures the reports to generated after performing analysis. The values are comma"
                                  + " (,) separated.")
                  .optional());
   }

   @Override
   protected void doRun() {
      // First, run the analyses.
      runCommands(parseCommands(getOptions().getParameters().getParameter(ANALYSES_PARAMETER_NAME).getStringValue()));
      // Next, run the reports.
      IParameter<?> reports = getOptions().getParameters().getParameter(REPORTS_PARAMETER_NAME);
      if (reports != null) {
         runCommands(parseCommands(reports.getStringValue()));
      }
   }

   @SuppressWarnings({"unchecked", "rawtypes"})
   private void runCommands(Collection<String> commands) {
      for (String command : commands) {
         ICommandProvider provider = getProviderForCommand(command);
         // We don't need to check the required parameters because the command providers will do that.
         provider.run(command, getOptions());
      }
   }

   private ICommandProvider<?, ?, ?> getProviderForCommand(String command) {
      // The command could either be a Jellyfish command or basic command that does not require a valid system
      // descriptor.  Therefore, we check to see which provider actually manages the command.
      if (jellyFishCommandProvider.getCommand(command) != null) {
         return jellyFishCommandProvider;
      } else if (commandProvider.getCommand(command) != null) {
         return commandProvider;
      } else {
         throw new IllegalArgumentException(String.format(
               "could not find a command named '%s'!  Run 'help' to see a list of analysis and reports.",
               command));
      }
   }

   private static Collection<String> parseCommands(String commaSeparatedValue) {
      return Arrays.asList(commaSeparatedValue.split(COMMAND_DELIMITER));
   }
}
