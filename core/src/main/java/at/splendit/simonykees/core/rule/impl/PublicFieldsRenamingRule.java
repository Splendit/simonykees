package at.splendit.simonykees.core.rule.impl;

import org.apache.commons.lang3.JavaVersion;

import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.visitor.renaming.FieldDeclarationMetadata;
import at.splendit.simonykees.core.visitor.renaming.PublicFieldsRenamingASTVisitor;

/**
 * 
 * @author Ardit Ymeri
 * @since 2.1.0
 *
 */
public class PublicFieldsRenamingRule extends RefactoringRule<PublicFieldsRenamingASTVisitor> {

	private FieldDeclarationMetadata metaData;
	
	public PublicFieldsRenamingRule(Class<PublicFieldsRenamingASTVisitor> visitor,
			FieldDeclarationMetadata metaData) {
		super(visitor);
		this.metaData = metaData;
		this.name = "Rename public fields";
		this.description = "Renames the public non-final fields to comply with the naming convention: "
				+ "\"^[a-z][a-zA-Z0-9]*$\" i.e. a lower case prefix followed by any sequence of "
				+ "alpha-numeric characters";
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_1;
	}

	@Override
	public PublicFieldsRenamingASTVisitor visitorFactory() {
		return new PublicFieldsRenamingASTVisitor(metaData);
	}
}
