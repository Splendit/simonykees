package eu.jsparrow.rules.java16.javarecords;

import java.util.List;

import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

/**
 * Wrapper class with all informations needed to replace an immutable class by a
 * corresponding record.
 * 
 * @since 4.5.0
 */
class BodyDeclarationsAnalysisResult {
	private final TypeDeclaration typeDeclarationToReplace;
	private final List<SingleVariableDeclaration> canonicalConstructorParameters;
	private final List<BodyDeclaration> recordBodyDeclarations;

	BodyDeclarationsAnalysisResult(TypeDeclaration typeDeclarationToReplace,
			List<SingleVariableDeclaration> canonicalConstructorParameters,
			List<BodyDeclaration> recordBodyDeclarations) {
		this.typeDeclarationToReplace = typeDeclarationToReplace;
		this.canonicalConstructorParameters = canonicalConstructorParameters;
		this.recordBodyDeclarations = recordBodyDeclarations;
	}

	TypeDeclaration getTypeDeclarationToReplace() {
		return typeDeclarationToReplace;
	}

	List<SingleVariableDeclaration> getCanonicalConstructorParameters() {
		return canonicalConstructorParameters;
	}

	List<BodyDeclaration> getRecordBodyDeclarations() {
		return recordBodyDeclarations;
	}
}
