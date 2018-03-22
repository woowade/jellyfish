/*
 * generated by Xtext 2.10.0
 */
package com.ngc.seaside.systemdescriptor.tests

import com.google.inject.Inject
import org.eclipse.xtext.junit4.InjectWith
import org.eclipse.xtext.junit4.XtextRunner
import org.eclipse.xtext.junit4.util.ParseHelper
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(XtextRunner)
@InjectWith(SystemDescriptorInjectorProvider)
class SystemDescriptorParsingTest{

	@Inject
	ParseHelper<com.ngc.seaside.systemdescriptor.systemDescriptor.Package> parseHelper

	@Test 
	def void loadModel() {
		val result = parseHelper.parse('''
			Hello Xtext!
		''')
		Assert.assertNotNull(result)
	}

}