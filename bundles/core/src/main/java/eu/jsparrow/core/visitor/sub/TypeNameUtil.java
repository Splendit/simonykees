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
 * Utility class to determine the corresponding {@link Name} for a given
 * {@link Type}. Additionally, it is possible to create a clone for a given
 * instance of {@link Name}.
 * 
 *
 */
public class TypeNameUtil {

	private TypeNameUtil() {
		// private constructor to hide the implicit public one
	}

	/**
	 * 
	 * @param name
	 *            expected to be the name of a {@link Type}.
	 * @return the clone of the {@link Name} given by the parameter if the
	 *         parameter is not null, otherwise null.
	 */
	public static Name cloneName(Name name) {
		if (name != null) {
			AST astNode = name.getAST();
			if (astNode != null) {
				if (name.isSimpleName()) {
					SimpleName simpleName = (SimpleName) name;
					return astNode.newSimpleName(simpleName.getIdentifier());
				}
				if (name.isQualifiedName()) {
					QualifiedName qualifiedName = (QualifiedName) name;
					Name qualifier = cloneName(qualifiedName.getQualifier());
					SimpleName simpleName = astNode.newSimpleName(qualifiedName.getName()
						.getIdentifier());
					return astNode.newQualifiedName(qualifier, simpleName);
				}
			}
		}
		return null;
	}

	/**
	 * Extracts a {@link Name} from a given {@link Type}.
	 * 
	 * @param type
	 *            Java type the name of which will be determined.
	 * @return a {@link Name} or null if no valid name could be found.
	 */
	public static Name getTypeName(Type type) {
		if (type == null) {
			return null;
		}

		AST astNode = type.getParent()
			.getAST();
		if (astNode == null) {
			return null;
		}

		if (type.isSimpleType()) {
			SimpleType simpleType = (SimpleType) type;
			return cloneName(simpleType.getName());

		}

		if (type.isNameQualifiedType()) {
			NameQualifiedType nqt = (NameQualifiedType) type;
			return cloneName(nqt.getQualifier());

		}

		if (type.isQualifiedType()) {
			QualifiedType qualifiedType = (QualifiedType) type;
			Name typeQualifyer = getTypeName(qualifiedType.getQualifier());
			SimpleName simpleTypeName = astNode.newSimpleName(qualifiedType.getName()
				.getIdentifier());
			return astNode.newQualifiedName(typeQualifyer, simpleTypeName);
		}

		ITypeBinding binding = type.resolveBinding();
		return astNode.newName(binding.getQualifiedName());
	}
}
