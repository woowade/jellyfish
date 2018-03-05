package com.ngc.seaside.systemdescriptor.formatting2

import com.ngc.seaside.systemdescriptor.systemDescriptor.RequireDeclaration
import com.ngc.seaside.systemdescriptor.systemDescriptor.Requires
import org.eclipse.xtext.formatting2.IFormattableDocument
import com.ngc.seaside.systemdescriptor.systemDescriptor.SystemDescriptorPackage
import com.ngc.seaside.systemdescriptor.systemDescriptor.BaseRequireDeclaration
import com.ngc.seaside.systemdescriptor.systemDescriptor.RefinedRequireDeclaration

class RequiresFormatter extends AbstractSystemDescriptorFormatter {
    def dispatch void format(Requires requires, extension IFormattableDocument document) {
        var begin = requires.regionFor.keyword('requires')
        var end = requires.regionFor.keyword('}').append[newLines = 2]
        interior(begin, end)[indent]

        requires.regionFor.keyword('{').prepend[oneSpace].append[newLine]

        for (RequireDeclaration d : requires.declarations) {
            d.format
            if (d.definition !== null && d != requires.declarations.last) {
                d.append[newLines = 2]
            } else {
                d.append[newLine]
            }
        }
    }

    def dispatch void format(RequireDeclaration declaration, extension IFormattableDocument document) {
		formatDeclaration(declaration, document)
    }
    
    def dispatch void format(BaseRequireDeclaration declaration, extension IFormattableDocument document) {
		formatDeclaration(declaration, document)
		declaration.regionFor.feature(SystemDescriptorPackage.Literals.BASE_REQUIRE_DECLARATION__TYPE)
			.append[oneSpace]
    }
    
    def dispatch void format(RefinedRequireDeclaration declaration, extension IFormattableDocument document) {
		formatDeclaration(declaration, document)
		declaration.regionFor.keyword('refine')
    		.prepend[noSpace]
    		.append[oneSpace]
    }
    
    private def void formatDeclaration(RequireDeclaration declaration, extension IFormattableDocument document) {
    	declaration.prepend[noSpace]

		if(declaration.definition !== null) {
			declaration.regionFor.feature(SystemDescriptorPackage.Literals.FIELD_DECLARATION__NAME)
				.append[oneSpace]
			declaration.definition.format
		} else {
			declaration.regionFor.feature(SystemDescriptorPackage.Literals.FIELD_DECLARATION__NAME)
				.prepend[newLines = 0]
				.append[newLine]
		}
    }
}