package eu.jsparrow.rules.common.visitor.helper;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.ExpressionMethodReference;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NameQualifiedType;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.RecordDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SuperMethodReference;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeMethodReference;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import eu.jsparrow.rules.common.util.ASTNodeUtil;

/**
 * Collects instances of SimpleName which may be valid references to a given
 * variable declaration. For example, for the following piece of code
 * 
 * <pre>
 * int x = 1;
 * x++;
 * </pre>
 * 
 * exactly one candidate can be found which is the {@code x} in {@code x++;}
 * because the {@code x} in {@code int x = 1;} belongs to the declaration itself
 * and therefore is not counted as reference.
 * <p>
 * Simple names in constructs like for example {@code this::x}, {@code x()} or
 * {@code void x(){}} are also excluded because it is clear that will never be
 * references to variables.
 * <p>
 * This visitor can be used in order to avoid to resolve too many times the
 * binding for a simple in order to get a variable binding.
 *
 */
public class VariableReferenceCandidatesVisitor extends ASTVisitor {

	private final String expectedIdentifier;
	private final List<SimpleName> referenceCandidates = new ArrayList<>();

	protected VariableReferenceCandidatesVisitor(String expectedIdentifier) {
		this.expectedIdentifier = expectedIdentifier;
	}

	public boolean isReferenceCandidate(SimpleName simpleName) {
		if (!simpleName.getIdentifier()
			.equals(expectedIdentifier)) {
			return false;
		}

		if (ASTNodeUtil.isLabel(simpleName)) {
			return false;
		}
		if (isDeclarationNameProperty(simpleName)) {
			return false;
		}

		if (isAnnotationTypeNameProperty(simpleName)) {
			return false;
		}

		if (isNamePropertyWithMethodBinding(simpleName)) {
			return false;
		}
		return simpleName.getLocationInParent() != SimpleType.NAME_PROPERTY ||
				simpleName.getLocationInParent() != QualifiedType.NAME_PROPERTY ||
				simpleName.getLocationInParent() != NameQualifiedType.NAME_PROPERTY ||
				simpleName.getLocationInParent() != NameQualifiedType.QUALIFIER_PROPERTY;

	}

	private static boolean isDeclarationNameProperty(SimpleName simpleName) {
		return simpleName.getLocationInParent() == VariableDeclarationFragment.NAME_PROPERTY ||
				simpleName.getLocationInParent() == SingleVariableDeclaration.NAME_PROPERTY ||
				simpleName.getLocationInParent() == EnumConstantDeclaration.NAME_PROPERTY ||
				simpleName.getLocationInParent() == MethodDeclaration.NAME_PROPERTY ||
				simpleName.getLocationInParent() == TypeDeclaration.NAME_PROPERTY ||
				simpleName.getLocationInParent() == EnumDeclaration.NAME_PROPERTY ||
				simpleName.getLocationInParent() == AnnotationTypeDeclaration.NAME_PROPERTY ||
				simpleName.getLocationInParent() == AnnotationTypeMemberDeclaration.NAME_PROPERTY ||
				simpleName.getLocationInParent() == RecordDeclaration.NAME_PROPERTY;
	}

	private static boolean isNamePropertyWithMethodBinding(SimpleName simpleName) {
		return simpleName.getLocationInParent() == MethodInvocation.NAME_PROPERTY &&
				simpleName.getLocationInParent() == SuperMethodInvocation.NAME_PROPERTY ||
				simpleName.getLocationInParent() == ExpressionMethodReference.NAME_PROPERTY ||
				simpleName.getLocationInParent() == SuperMethodReference.NAME_PROPERTY ||
				simpleName.getLocationInParent() == TypeMethodReference.NAME_PROPERTY ||
				simpleName.getLocationInParent() == MemberValuePair.NAME_PROPERTY;
	}

	private static boolean isAnnotationTypeNameProperty(Name name) {
		StructuralPropertyDescriptor locationInParent = name.getLocationInParent();
		return locationInParent == MarkerAnnotation.TYPE_NAME_PROPERTY ||
				locationInParent == SingleMemberAnnotation.TYPE_NAME_PROPERTY ||
				locationInParent == NormalAnnotation.TYPE_NAME_PROPERTY;
	}

	@Override
	public boolean visit(QualifiedName node) {
		if (isAnnotationTypeNameProperty(node)) {
			return false;
		}
		return node.getLocationInParent() != SimpleType.NAME_PROPERTY &&
				node.getLocationInParent() != NameQualifiedType.QUALIFIER_PROPERTY;
	}

	@Override
	public boolean visit(SimpleName node) {
		if (isReferenceCandidate(node)) {
			referenceCandidates.add(node);
		}
		return false;
	}
}
