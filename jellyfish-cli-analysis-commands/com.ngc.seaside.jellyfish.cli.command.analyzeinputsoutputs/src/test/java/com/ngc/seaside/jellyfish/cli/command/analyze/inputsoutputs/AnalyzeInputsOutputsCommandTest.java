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
package com.ngc.seaside.jellyfish.cli.command.analyze.inputsoutputs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.ngc.seaside.jellyfish.service.analysis.api.IAnalysisService;
import com.ngc.seaside.jellyfish.service.analysis.api.SystemDescriptorFinding;
import com.ngc.seaside.systemdescriptor.model.impl.basic.model.DataReferenceField;
import com.ngc.seaside.systemdescriptor.model.impl.basic.model.Model;
import com.ngc.seaside.systemdescriptor.service.log.api.ILogService;
import com.ngc.seaside.systemdescriptor.service.source.api.ISourceLocation;
import com.ngc.seaside.systemdescriptor.service.source.api.ISourceLocatorService;

@RunWith(MockitoJUnitRunner.class)
public class AnalyzeInputsOutputsCommandTest {

   private AnalyzeInputsOutputsCommand command;

   @Mock
   private ILogService logService;

   @Mock
   private IAnalysisService analysisService;

   @Mock
   private ISourceLocatorService sourceLocatorService;

   @Before
   public void setup() {
      command = new AnalyzeInputsOutputsCommand();
      command.setLogService(logService);
      command.setAnalysisService(analysisService);
      command.setSourceLocatorService(sourceLocatorService);
      command.activate();
   }

   @Test
   public void testDoesCreateFindingIfModelHasNoInputs() {
      Model model = new Model("com.Foo");
      model.addInput(new DataReferenceField("field1"));

      ISourceLocation location = mock(ISourceLocation.class);
      when(sourceLocatorService.getLocation(model, false)).thenReturn(location);

      command.analyzeModel(model);

      ArgumentCaptor<SystemDescriptorFinding<?>> captor = ArgumentCaptor.forClass(SystemDescriptorFinding.class);
      verify(analysisService).addFinding(captor.capture());
      assertNotNull("message not set!",
                    captor.getValue().getMessage());
      assertEquals("location not correct!",
                   location,
                   captor.getValue().getLocation().orElse(null));
      assertEquals("finding type not correct!",
                   InputsOutputsFindingTypes.INPUTS_WITH_NO_OUTPUTS,
                   captor.getValue().getType());
   }

   @Test
   public void testDoesNotCreateFindingIfModelHasOutput() {
      Model model = new Model("com.Foo");
      model.addInput(new DataReferenceField("field1"));
      model.addOutput(new DataReferenceField("field2"));
      command.analyzeModel(model);
      verify(analysisService, never()).addFinding(any());
   }

   @Test
   public void testDoesNotCreateFindingIfModelHasNoInputs() {
      Model model = new Model("com.Foo");
      command.analyzeModel(model);
      verify(analysisService, never()).addFinding(any());
   }
}
