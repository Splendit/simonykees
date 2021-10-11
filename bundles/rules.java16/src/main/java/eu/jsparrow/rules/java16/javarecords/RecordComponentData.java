package eu.jsparrow.rules.java16.javarecords;

import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

class RecordComponentData {

	private final FieldDeclaration fieldDeclaration;
	private final VariableDeclarationFragment variableDeclarationFragment;
	private final Type type;
	private final SimpleName name;

	RecordComponentData(FieldDeclaration fieldDeclaration, VariableDeclarationFragment variableDeclarationFragment) {
		this.fieldDeclaration = fieldDeclaration;
		this.variableDeclarationFragment = variableDeclarationFragment;
		this.type = fieldDeclaration.getType();
		this.name = variableDeclarationFragment.getName();
	}

	FieldDeclaration getFieldDeclaration() {
		return fieldDeclaration;
	}

	VariableDeclarationFragment getVariableDeclarationFragment() {
		return variableDeclarationFragment;
	}

	Type getType() {
		return type;
	}

	SimpleName getName() {
		return name;
	}
}
