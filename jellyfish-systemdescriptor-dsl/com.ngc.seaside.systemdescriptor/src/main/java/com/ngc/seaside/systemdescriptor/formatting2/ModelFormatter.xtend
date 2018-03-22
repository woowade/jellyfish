package com.ngc.seaside.systemdescriptor.formatting2

import com.ngc.seaside.systemdescriptor.systemDescriptor.Model
import org.eclipse.xtext.formatting2.IFormattableDocument

class ModelFormatter extends AbstractSystemDescriptorFormatter {
	def dispatch void format(Model model, extension IFormattableDocument document) {
		model.regionFor.keyword('model').prepend[noIndentation]
		
		if (model.refinedModel !== null) {
			model.regionFor.keyword('refines').prepend[oneSpace].append[oneSpace]
		}

		model.metadata?.format

		model.input?.format
		model.output?.format
		model.requires?.format
		model.parts?.format
		model.links?.format
		model.properties?.format
		model.scenarios.forEach[format]
	}
}