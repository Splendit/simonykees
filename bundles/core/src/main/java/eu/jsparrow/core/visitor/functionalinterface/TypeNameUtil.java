package eu.jsparrow.core.visitor.functionalinterface;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NameQualifiedType;
import org.eclipse.jdt.core.dom.ParameterizedType;
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
 * @since 3.16.0
 */
public class TypeNameUtil {

	private TypeNameUtil() {
		// private constructor to hide the implicit public one
	}

	/**
	 * 
	 * @param name
	 *            expected to be a non-null instance of {@link Name}
	 *            representing a Java type.
	 * @return the clone of the {@link Name} given by the parameter.
	 */
	public static Name cloneName(Name name) {
		AST ast = name.getAST();

		if (name.isSimpleName()) {
			SimpleName simpleName = (SimpleName) name;
			return ast.newSimpleName(simpleName.getIdentifier());
		}
		if (name.isQualifiedName()) {
			QualifiedName qualifiedName = (QualifiedName) name;
			Name qualifierClone = cloneName(qualifiedName.getQualifier());
			SimpleName simpleNameClone = ast.newSimpleName(qualifiedName.getName()
				.getIdentifier());
			return ast.newQualifiedName(qualifierClone, simpleNameClone);
		}
		return null;
	}

	/**
	 * Extracts a {@link Name} from a given {@link Type}.
	 * 
	 * @param type
	 *            expected to be a non-null instance of {@link Type}
	 *            representing a Java type.
	 * @return {@link Name} representing the corresponding Java type or null if
	 *         no {@link Name} could be found.
	 */
	public static Name extractName(Type type) {
		AST ast = type.getAST();

		if (type.isNameQualifiedType()) {
			NameQualifiedType nameQualifiedType = (NameQualifiedType) type;
			Name qualifierClone = cloneName(nameQualifiedType.getQualifier());
			SimpleName simpleNameClone = ast.newSimpleName(nameQualifiedType.getName()
				.getIdentifier());
			return ast.newQualifiedName(qualifierClone, simpleNameClone);
		}

		if (type.isQualifiedType()) {
			QualifiedType qualifiedType = (QualifiedType) type;
			Name typeQualifyer = extractName(qualifiedType.getQualifier());
			SimpleName simpleTypeName = ast.newSimpleName(qualifiedType.getName()
				.getIdentifier());
			return ast.newQualifiedName(typeQualifyer, simpleTypeName);
		}

		if (type.isSimpleType()) {
			SimpleType simpleType = (SimpleType) type;
			return cloneName(simpleType.getName());
		}

		if (type.isParameterizedType()) {
			ParameterizedType parameterizedType = (ParameterizedType) type;
			Type erasure = parameterizedType.getType();
			return extractName(erasure);
		}

		return null;
	}
}
