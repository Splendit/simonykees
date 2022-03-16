package eu.jsparrow.core.visitor.impl;

import static eu.jsparrow.rules.common.util.ASTNodeUtil.convertToTypedList;

import java.util.Arrays;

import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import eu.jsparrow.core.markers.common.RemoveModifiersInInterfacePropertiesEvent;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

/**
 * Removes the {@code public} modifier from the methods and
 * {@code public static final} modifiers from fields declared in interfaces.
 * 
 * @since 3.3.0
 *
 */
public class RemoveModifiersInInterfacePropertiesASTVisitor extends AbstractASTRewriteASTVisitor implements RemoveModifiersInInterfacePropertiesEvent {

	@Override
	public boolean visit(TypeDeclaration interfaceDeclaration) {

		if (!interfaceDeclaration.isInterface()) {
			return true;
		}

		Arrays.stream(interfaceDeclaration.getFields())
			.flatMap(field -> convertToTypedList(field.modifiers(), Modifier.class).stream())
			.filter(modifier -> modifier.isPublic() || modifier.isStatic() || modifier.isFinal())
			.forEach(this::removeModifier);

		Arrays.stream(interfaceDeclaration.getMethods())
			.flatMap(method -> convertToTypedList(method.modifiers(), Modifier.class).stream())
			.filter(Modifier::isPublic)
			.forEach(this::removeModifier);

		return true;
	}

	protected void removeModifier(Modifier modifier) {
		astRewrite.remove(modifier, null);
		onRewrite();
		addMarkerEvent(modifier);
	}

}
