package eu.jsparrow.core.visitor.junit;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

/**
 * A utility class for analyzing or editing {@link MethodDeclaration}s annotated
 * with {@code @Test(...)}.
 * 
 * @since 3.26.0
 *
 */
public class TestMethodUtil {

	private static final String ORG_JUNIT_TEST = "org.junit.Test"; //$NON-NLS-1$

	private TestMethodUtil() {
		/*
		 * Hide the default constructor.
		 */
	}

	/**
	 * Checks if a {@link MethodDeclaration} is a test case, i.e., annotated
	 * with @link {@link NormalAnnotation} {code @Test(...)}.
	 * 
	 * @param methodDeclaration
	 *            the method to be checked.
	 * @return a {@link NormalAnnotation} or {@code null} if the method
	 *         declaration is not annotated with {@link NormalAnnotation}.
	 */
	public static Optional<NormalAnnotation> findTestAnnotatedMethod(MethodDeclaration methodDeclaration) {
		List<NormalAnnotation> annotations = ASTNodeUtil.convertToTypedList(methodDeclaration.modifiers(),
				NormalAnnotation.class);

		for (NormalAnnotation annotation : annotations) {
			Name typeName = annotation.getTypeName();
			ITypeBinding annotationTypeBinding = typeName.resolveTypeBinding();
			boolean isTest = ClassRelationUtil.isContentOfTypes(annotationTypeBinding,
					Arrays.asList(ORG_JUNIT_TEST));
			if (isTest) {
				return Optional.of(annotation);
			}
		}
		return Optional.empty();
	}

	/**
	 * If the {@link NormalAnnotation} has multiple {@link MemberValuePair}s,
	 * then only the given one is removed. Otherwise, if the
	 * {@link NormalAnnotation} contains a single property, then it is replaced
	 * with a {@link MarkerAnnotation}.
	 * 
	 * @param astRewrite
	 *            rewriter to record the changes. Should be provided by
	 *            {@link AbstractASTRewriteASTVisitor}.
	 * @param annotation
	 *            the {@link NormalAnnotation} to be updated
	 * @param memberValuePair
	 *            the {@link MemberValuePair} to be removed
	 */
	public static void removeAnnotationProperty(ASTRewrite astRewrite,
			NormalAnnotation annotation, MemberValuePair memberValuePair) {
		AST ast = astRewrite.getAST();
		@SuppressWarnings("unchecked")
		List<MemberValuePair> annotationProperties = annotation.values();
		if (annotationProperties.size() > 1) {
			astRewrite.remove(memberValuePair, null);
		} else {
			MarkerAnnotation markerAnnotation = ast.newMarkerAnnotation();
			markerAnnotation.setTypeName((Name) astRewrite.createCopyTarget(annotation.getTypeName()));
			astRewrite.replace(annotation, markerAnnotation, null);
		}
	}

	/**
	 * Finds the {@link MemberValuePair} with the given key in the given
	 * {@link NormalAnnotation}.
	 * 
	 * @param annotation
	 *            an annotation with optional properties
	 * @param annotationKey
	 *            the property key to search for
	 * @return the matching {@link MemberValuePair} or null if none found.
	 */
	public static MemberValuePair findNamedValuePair(NormalAnnotation annotation, String annotationKey) {
		List<MemberValuePair> values = ASTNodeUtil.convertToTypedList(annotation.values(), MemberValuePair.class);
		for (MemberValuePair memberValuePair : values) {
			SimpleName name = memberValuePair.getName();
			String identifier = name.getIdentifier();
			if (annotationKey.equals(identifier)) {
				return memberValuePair;
			}
		}
		return null;
	}

}
