package eu.jsparrow.core.visitor.junit;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Expression;
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

public class TestMethodUtil {
	
	private static final String ORG_JUNIT_TEST = "org.junit.Test"; //$NON-NLS-1$

	public static NormalAnnotation isTestAnnotatedMethod(MethodDeclaration methodDeclaration) {
		List<NormalAnnotation> annotations = ASTNodeUtil.convertToTypedList(methodDeclaration.modifiers(),
				NormalAnnotation.class);
	
		for (NormalAnnotation annotation : annotations) {
			Name typeName = annotation.getTypeName();
			ITypeBinding annotationTypeBinding = typeName.resolveTypeBinding();
			boolean isTest = ClassRelationUtil.isContentOfTypes(annotationTypeBinding,
					Arrays.asList(ORG_JUNIT_TEST));
			if (isTest) {
				return annotation;
			}
		}
		return null;
	}

	public static void removeAnnotationProperty(ASTRewrite astRewrite, 
			NormalAnnotation annotation, Expression timeoutPair) {
		AST ast = astRewrite.getAST();
		List<MemberValuePair> annotationProperties = annotation.values();
		if (annotationProperties.size() > 1) {
			astRewrite.remove(timeoutPair.getParent(), null);
		} else {
			MarkerAnnotation markerAnnotation = ast.newMarkerAnnotation();
			markerAnnotation.setTypeName((Name) astRewrite.createCopyTarget(annotation.getTypeName()));
			astRewrite.replace(annotation, markerAnnotation, null);
		}
	}

	public static MemberValuePair findExpectedValuePair(NormalAnnotation annotation, String annotationKey) {
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
