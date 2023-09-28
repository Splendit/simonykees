package eu.jsparrow.core.visitor.spring;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;

import eu.jsparrow.core.markers.common.ReplaceRequestMappingAnnotationEvent;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractAddImportASTVisitor;

/**
 * This visitor looks for '@RequestMapping' annotations on methods. If the given
 * annotation specifies a certain request method like {@code RequestMethod.GET}
 * or {@code RequestMethod.POST}, then it can be replaced by one of the
 * corresponding short cut annotations like '@GetMapping' or '@PostMapping'.
 * <p>
 * Example:
 * 
 * <pre>
 * &#64;RequestMapping(value = "/hello", method = RequestMethod.GET
 * </pre>
 * 
 * can be replaced by
 * 
 * <pre>
 * &#64;GetMapping(value = "/hello"
 * </pre>
 * 
 * @since 4.12.0
 * 
 */
public class ReplaceRequestMappingAnnotationASTVisitor extends AbstractAddImportASTVisitor
		implements ReplaceRequestMappingAnnotationEvent {
	private static final String WEB_BIND_ANNOTATION_PACKAGE_PREFIX = "org.springframework.web.bind.annotation."; //$NON-NLS-1$
	private static final String REQUEST_MAPPING = WEB_BIND_ANNOTATION_PACKAGE_PREFIX + "RequestMapping"; //$NON-NLS-1$
	private static final String REQUEST_METHOD = WEB_BIND_ANNOTATION_PACKAGE_PREFIX + "RequestMethod"; //$NON-NLS-1$
	private static final String GET = "GET"; //$NON-NLS-1$
	private static final String GET_MAPPING = "GetMapping"; //$NON-NLS-1$
	private static final String PUT = "PUT"; //$NON-NLS-1$
	private static final String PUT_MAPPING = "PutMapping"; //$NON-NLS-1$
	private static final String POST = "POST"; //$NON-NLS-1$
	private static final String POST_MAPPING = "PostMapping"; //$NON-NLS-1$
	private static final String PATCH = "PATCH"; //$NON-NLS-1$
	private static final String PATCH_MAPPING = "PatchMapping"; //$NON-NLS-1$
	private static final String DELETE = "DELETE"; //$NON-NLS-1$
	private static final String DELETE_MAPPING = "DeleteMapping"; //$NON-NLS-1$
	private static final String METHOD = "method"; //$NON-NLS-1$
	private static final Map<String, String> MAP_TO_NEW_ANNOTATION;

	static {
		Map<String, String> tmpMap = new HashMap<>();
		tmpMap.put(GET, WEB_BIND_ANNOTATION_PACKAGE_PREFIX + GET_MAPPING);
		tmpMap.put(PUT, WEB_BIND_ANNOTATION_PACKAGE_PREFIX + PUT_MAPPING);
		tmpMap.put(POST, WEB_BIND_ANNOTATION_PACKAGE_PREFIX + POST_MAPPING);
		tmpMap.put(PATCH, WEB_BIND_ANNOTATION_PACKAGE_PREFIX + PATCH_MAPPING);
		tmpMap.put(DELETE, WEB_BIND_ANNOTATION_PACKAGE_PREFIX + DELETE_MAPPING);
		MAP_TO_NEW_ANNOTATION = Collections.unmodifiableMap(tmpMap);
	}

	@Override
	public boolean visit(NormalAnnotation node) {
		if (node.getLocationInParent() != MethodDeclaration.MODIFIERS2_PROPERTY) {
			return true;
		}

		if (!ClassRelationUtil.isContentOfType(node.resolveTypeBinding(), REQUEST_MAPPING)) {
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

		if (methodValue.getNodeType() == ASTNode.ARRAY_INITIALIZER) {
			ArrayInitializer arrayInitializer = (ArrayInitializer) methodValue;
			methodValue = ASTNodeUtil
				.findSingletonListElement(arrayInitializer.expressions(), Expression.class)
				.orElse(null);
			if (methodValue == null) {
				return true;
			}
		}

		if (!ClassRelationUtil.isContentOfType(methodValue.resolveTypeBinding(), REQUEST_METHOD)) {
			return true;
		}

		String requestMethodIdentifier;
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
		if (!MAP_TO_NEW_ANNOTATION.containsKey(requestMethodIdentifier)) {
			return true;
		}
		String newAnnotationQualifiedTypeName = MAP_TO_NEW_ANNOTATION.get(requestMethodIdentifier);

		List<MemberValuePair> memberValuePairsToCopy = memberValuePairs.stream()
			.filter(memberValuePair -> memberValuePair != methodPair)
			.collect(Collectors.toList());

		NormalAnnotation annotationReplacement = createRequestMappingAnnotationReplacement(node,
				newAnnotationQualifiedTypeName, memberValuePairsToCopy);

		astRewrite.replace(node, annotationReplacement, null);
		addMarkerEvent(node);
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
