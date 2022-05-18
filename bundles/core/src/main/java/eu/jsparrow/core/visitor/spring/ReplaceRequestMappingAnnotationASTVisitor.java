package eu.jsparrow.core.visitor.spring;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.visitor.AbstractAddImportASTVisitor;

public class ReplaceRequestMappingAnnotationASTVisitor extends AbstractAddImportASTVisitor {

	private static final String GET = "GET"; //$NON-NLS-1$
	private static final String GET_MAPPING = "org.springframework.web.bind.annotation.GetMapping"; //$NON-NLS-1$
	private static final String METHOD = "method"; //$NON-NLS-1$
	private static final String REQUEST_MAPPING = "org.springframework.web.bind.annotation.RequestMapping"; //$NON-NLS-1$
	private static final String REQUEST_METHOD = "org.springframework.web.bind.annotation.RequestMethod"; //$NON-NLS-1$

	@Override
	public boolean visit(ImportDeclaration node) {
		IBinding binding = node.resolveBinding();
		return true;
	}

	@Override
	public boolean visit(NormalAnnotation node) {
		IAnnotationBinding annotationBinding = node.resolveAnnotationBinding();
		if (annotationBinding == null) {
			return true;
		}
		ITypeBinding annotationType = annotationBinding.getAnnotationType();
		String annotationTypeQualifiedName = annotationType.getQualifiedName();
		if (!annotationTypeQualifiedName.equals(REQUEST_MAPPING)) {
			return true;
		}
		List<MemberValuePair> memberValuePairs = ASTNodeUtil.convertToTypedList(node.values(), MemberValuePair.class);

		MemberValuePair methodPair = memberValuePairs.stream()
			.filter(memberValuePair -> memberValuePair.getName()
				.getIdentifier()
				.equals(METHOD))
			.findFirst()
			.orElse(null);

		if (methodPair == null) {
			return true;
		}

		Expression methodValue = methodPair.getValue();

		String requestMethodIdentifier;

		if (methodValue.getNodeType() == ASTNode.ARRAY_INITIALIZER) {
			ArrayInitializer arrayInitializer = (ArrayInitializer) methodValue;
			List<Expression> expressions = ASTNodeUtil.convertToTypedList(arrayInitializer.expressions(),
					Expression.class);
			if (expressions.size() != 1) {
				return true;
			}
			methodValue = expressions.get(0);
		}

		String methodValueTypeQualifiedName = methodValue.resolveTypeBinding()
			.getQualifiedName();
		if (!methodValueTypeQualifiedName.equals(REQUEST_METHOD)) {
			return true;
		}

		if (methodValue.getNodeType() == ASTNode.QUALIFIED_NAME) {
			QualifiedName qualifiedName = (QualifiedName) methodValue;
			requestMethodIdentifier = qualifiedName.getName()
				.getIdentifier();
		} else if (methodValue.getNodeType() == ASTNode.SIMPLE_NAME) {
			SimpleName simpleName = (SimpleName) methodValue;
			requestMethodIdentifier = simpleName.getIdentifier();
		} else {
			return true;
		}

		if (!requestMethodIdentifier.equals(GET)) {
			return true;
		}
		String newAnnotationQualifiedTypeName = GET_MAPPING;
		List<MemberValuePair> memberValuePairsToCopy = memberValuePairs.stream()
			.filter(memberValuePair -> memberValuePair != methodPair)
			.collect(Collectors.toList());

		NormalAnnotation annotationReplacement = createRequestMappingAnnotationReplacement(node,
				newAnnotationQualifiedTypeName, memberValuePairsToCopy);

		astRewrite.replace(node, annotationReplacement, null);
		onRewrite();
		return true;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private NormalAnnotation createRequestMappingAnnotationReplacement(NormalAnnotation context,
			String newAnnotationQualifiedTypeName, List<MemberValuePair> memberValuePairsToCopy) {
		verifyImport(getCompilationUnit(), newAnnotationQualifiedTypeName);
		AST ast = astRewrite.getAST();
		NormalAnnotation annotationReplacement = ast.newNormalAnnotation();
		Name newAnnotationTypeName = addImport(newAnnotationQualifiedTypeName, context);
		annotationReplacement.setTypeName(newAnnotationTypeName);

		List memberValuePairReplacements = annotationReplacement.values();
		memberValuePairsToCopy.stream()
			.map(astRewrite::createCopyTarget)
			.forEach(memberValuePairReplacements::add);

		return annotationReplacement;
	}

}
