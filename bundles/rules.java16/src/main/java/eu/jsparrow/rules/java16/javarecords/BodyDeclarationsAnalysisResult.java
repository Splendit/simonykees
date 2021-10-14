package eu.jsparrow.rules.java16.javarecords;

import java.util.List;

import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public class BodyDeclarationsAnalysisResult {
	private final TypeDeclaration typeDeclarationToReplace;
	private final List<SingleVariableDeclaration> canonicalConstructorParameters;
	private final List<BodyDeclaration> recordBodyDeclarations;

	public BodyDeclarationsAnalysisResult(TypeDeclaration typeDeclarationToReplace,
			List<SingleVariableDeclaration> canonicalConstructorParameters,
			List<BodyDeclaration> recordBodyDeclarations) {
		this.typeDeclarationToReplace = typeDeclarationToReplace;
		this.canonicalConstructorParameters = canonicalConstructorParameters;
		this.recordBodyDeclarations = recordBodyDeclarations;
	}

	public TypeDeclaration getTypeDeclarationToReplace() {
		return typeDeclarationToReplace;
	}

	public List<SingleVariableDeclaration> getCanonicalConstructorParameters() {
		return canonicalConstructorParameters;
	}

	public List<BodyDeclaration> getRecordBodyDeclarations() {
		return recordBodyDeclarations;
	}
}
