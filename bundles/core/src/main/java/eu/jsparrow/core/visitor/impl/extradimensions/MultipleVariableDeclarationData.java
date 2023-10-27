package eu.jsparrow.core.visitor.impl.extradimensions;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.ChildPropertyDescriptor;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import eu.jsparrow.rules.common.util.ASTNodeUtil;

public class MultipleVariableDeclarationData {
	private final ASTNode multipleDeclaration;
	private final List<VariableDeclarationFragment> variableDeclarationFragments;
	private final ChildListPropertyDescriptor locationInParent;
	private final ChildListPropertyDescriptor fragmentsProperty;
	private final ChildPropertyDescriptor typeProperty;
	private final Supplier<ASTNode> copyWithoutFragmentsSupplyer;

	Optional<MultipleVariableDeclarationData> findMultipleVariableDeclarationData(
			VariableDeclarationStatement variableDeclarationStatement) {
		if (variableDeclarationStatement.fragments()
			.size() <= 1) {
			return Optional.empty();
		}
		if (variableDeclarationStatement.getLocationInParent() != Block.STATEMENTS_PROPERTY) {
			return Optional.empty();
		}
		return Optional
			.of(new MultipleVariableDeclarationData(variableDeclarationStatement, Block.STATEMENTS_PROPERTY));
	}

	protected MultipleVariableDeclarationData(VariableDeclarationStatement multipleDeclaration,
			ChildListPropertyDescriptor locationInParent) {
		this.multipleDeclaration = multipleDeclaration;
		this.variableDeclarationFragments = ASTNodeUtil.convertToTypedList(multipleDeclaration.fragments(),
				VariableDeclarationFragment.class);
		this.locationInParent = locationInParent;
		this.fragmentsProperty = VariableDeclarationStatement.FRAGMENTS_PROPERTY;
		this.typeProperty = VariableDeclarationStatement.TYPE_PROPERTY;
		this.copyWithoutFragmentsSupplyer = () -> {
			VariableDeclarationStatement newVariableDeclarationStatement = (VariableDeclarationStatement) ASTNode
				.copySubtree(multipleDeclaration.getAST(), multipleDeclaration);
			newVariableDeclarationStatement.fragments()
				.clear();
			return newVariableDeclarationStatement;
		};

	}

	protected MultipleVariableDeclarationData(FieldDeclaration multipleDeclaration,
			ChildListPropertyDescriptor locationInParent) {
		this.multipleDeclaration = multipleDeclaration;
		this.variableDeclarationFragments = ASTNodeUtil.convertToTypedList(multipleDeclaration.fragments(),
				VariableDeclarationFragment.class);
		this.locationInParent = locationInParent;
		this.fragmentsProperty = FieldDeclaration.FRAGMENTS_PROPERTY;
		this.typeProperty = FieldDeclaration.TYPE_PROPERTY;
		this.copyWithoutFragmentsSupplyer = () -> {
			FieldDeclaration newFieldDeclaration = (FieldDeclaration) ASTNode
				.copySubtree(multipleDeclaration.getAST(), multipleDeclaration);
			newFieldDeclaration.fragments()
				.clear();
			return newFieldDeclaration;
		};

	}

	ASTNode getMultipleDeclaration() {
		return multipleDeclaration;
	}

	List<VariableDeclarationFragment> getVariableDeclarationFragments() {
		return variableDeclarationFragments;
	}

	ChildListPropertyDescriptor getLocationInParent() {
		return locationInParent;
	}

	ChildListPropertyDescriptor getFragmentsProperty() {
		return fragmentsProperty;
	}

	ChildPropertyDescriptor getTypeProperty() {
		return typeProperty;
	}

	Supplier<ASTNode> getCopyWithoutFragmentsSupplyer() {
		return copyWithoutFragmentsSupplyer;
	}

}
