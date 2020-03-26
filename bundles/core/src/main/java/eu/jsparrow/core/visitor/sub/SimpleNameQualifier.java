package eu.jsparrow.core.visitor.sub;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NameQualifiedType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Type;

/**
 * Utility class to create a qualified name from a {@link SimpleName} and a {@link Type}.
 * 
 *
 */
public class SimpleNameQualifier {

	private SimpleNameQualifier() {
		// private constructor to hide the implicit public one
	}

	public static SimpleName cloneSimpleName(AST astNode, SimpleName simpleName) {
		return astNode.newSimpleName(simpleName.getIdentifier());
	}

	public static Name cloneName(AST astNode, Name name) {
		if (name.isSimpleName()) {
			return cloneSimpleName(astNode, (SimpleName) name);
		}
		if (name.isQualifiedName()) {
			QualifiedName qualifiedName = (QualifiedName) name;
			Name qualifier = cloneName(astNode, qualifiedName.getQualifier());
			SimpleName simpleName = cloneSimpleName(astNode, qualifiedName.getName());
			return astNode.newQualifiedName(qualifier, simpleName);
		}
		return null;
	}

	public static QualifiedName qualifyByType(Type type, SimpleName simpleNameToReplace) {

		AST astNode = simpleNameToReplace.getParent()
			.getAST();
		SimpleName simpleNameClone = cloneSimpleName(astNode, simpleNameToReplace);

		if (type.isSimpleType()) {
			SimpleType simpleType = (SimpleType) type;
			Name nameClone = cloneName(astNode, simpleType.getName());
			return astNode.newQualifiedName(nameClone, simpleNameClone);

		} else if (type.isQualifiedType()) {
			QualifiedType qualifiedType = (QualifiedType) type;
			Type typeNameQualifier = qualifiedType.getQualifier();
			SimpleName simpleTypeName = qualifiedType.getName();
			QualifiedName qualifyiedTypeName = qualifyByType(typeNameQualifier, simpleTypeName);
			return astNode.newQualifiedName(qualifyiedTypeName, simpleNameClone);

		} else if (type.isNameQualifiedType()) {
			NameQualifiedType nqt = (NameQualifiedType) type;
			Name qualifierClone = cloneName(astNode, nqt.getQualifier());
			return astNode.newQualifiedName(qualifierClone, simpleNameClone);

		} else {
			ITypeBinding binding = type.resolveBinding();
			Name qualifier = astNode.newName(binding.getQualifiedName());
			return astNode.newQualifiedName(qualifier, simpleNameClone);
		}
	}
}
