package eu.jsparrow.core.visitor.impl.entryset;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import eu.jsparrow.core.markers.common.IterateMapEntrySetEvent;
import eu.jsparrow.rules.common.exception.UnresolvedBindingException;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;
import eu.jsparrow.rules.common.visitor.helper.FindVariableBinding;
import eu.jsparrow.rules.common.visitor.helper.SafeVariableNameFactory;

/**
 * 
 */
public class IterateMapEntrySetASTVisitor extends AbstractASTRewriteASTVisitor
		implements IterateMapEntrySetEvent {

	private static final String ENTRY = "entry"; //$NON-NLS-1$
	private static final String TYPE_NAME_ENTRY = "Entry"; //$NON-NLS-1$
	private static final String TYPE_NAME_MAP = "Map"; //$NON-NLS-1$
	private static final String ENTRY_SET = "entrySet"; //$NON-NLS-1$
	private static final String GET_KEY = "getKey"; //$NON-NLS-1$
	private static final String GET_VALUE = "getValue"; //$NON-NLS-1$

	private static final List<String> JAVA_UTIL_MAP_SINGLETON_LIST = Collections
		.singletonList(java.util.Map.class.getName());

	private final SafeVariableNameFactory variableNameFactory = new SafeVariableNameFactory();

	@Override
	public boolean visit(EnhancedForStatement enhancedForStatement) {
		extractTransformationData(enhancedForStatement).ifPresent(this::transform);
		return true;
	}

	@Override
	public void endVisit(CompilationUnit compilationUnit) {
		variableNameFactory.clearCompilationUnitScope(compilationUnit);
		super.endVisit(compilationUnit);
	}

	@Override
	public void endVisit(TypeDeclaration typeDeclaration) {
		variableNameFactory.clearFieldScope(typeDeclaration);
	}

	@Override
	public void endVisit(MethodDeclaration methodDeclaration) {
		variableNameFactory.clearLocalVariablesScope(methodDeclaration);
	}

	@Override
	public void endVisit(FieldDeclaration fieldDeclaration) {
		variableNameFactory.clearLocalVariablesScope(fieldDeclaration);
	}

	@Override
	public void endVisit(Initializer initializer) {
		variableNameFactory.clearLocalVariablesScope(initializer);
	}

	private void transform(TransformationData transformationData) {

		String mapEntryIdentifier = transformationData.getMapEntryIdentifier();
		SingleVariableDeclaration keyParameterTpReplace = transformationData.getLoopParameter();
		SingleVariableDeclaration newMapEntryParameter = createNewMapEntryParameter(transformationData,
				mapEntryIdentifier);
		Expression keySetExpressionToReplace = transformationData.getLoopExpression();
		Expression newEntrySetExpression = createNewMapEntrySetInvocation(transformationData);
		Expression valueInitializerToReplace = transformationData.getMapGetterInvocationToReplace();
		MethodInvocation valueInitializerReplecement = createEntryGetterInvocation(mapEntryIdentifier,
				GET_VALUE);
		astRewrite.replace(keyParameterTpReplace, newMapEntryParameter, null);
		astRewrite.replace(keySetExpressionToReplace, newEntrySetExpression, null);
		if (transformationData.isKeyVariableDeclarationNecessary()) {
			VariableDeclarationStatement newKeyDeclarationStatement = createKeyDeclarationStatement(transformationData,
					mapEntryIdentifier);
			astRewrite.getListRewrite(transformationData.getLoopBody(), Block.STATEMENTS_PROPERTY)
				.insertAt(newKeyDeclarationStatement, 0, null);
		}
		astRewrite.replace(valueInitializerToReplace, valueInitializerReplecement, null);
		onRewrite();
	}

	private SingleVariableDeclaration createNewMapEntryParameter(TransformationData transformationData,
			String mapEntryIdentifier) {
		AST ast = astRewrite.getAST();
		SingleVariableDeclaration newMapEntryParameter = ast.newSingleVariableDeclaration();
		ParameterizedType parameterizedEntryType = createParameterizedEntryType(transformationData);
		newMapEntryParameter.setType(parameterizedEntryType);
		newMapEntryParameter.setName(ast.newSimpleName(mapEntryIdentifier));

		return newMapEntryParameter;
	}

	private ParameterizedType createParameterizedEntryType(TransformationData transformationData) {
		AST ast = astRewrite.getAST();
		SimpleName simpleEntryTypeName = ast.newSimpleName(TYPE_NAME_ENTRY);
		SimpleName simpleMapTypeName = ast.newSimpleName(TYPE_NAME_MAP);
		QualifiedName qualifiedEntryTypeName = ast.newQualifiedName(simpleMapTypeName, simpleEntryTypeName);
		SimpleType simpleEntryType = ast.newSimpleType(qualifiedEntryTypeName);

		ParameterizedType parameterizedMapType = transformationData.getParameterizedMapType();
		ParameterizedType parameterizedSubtreeCopy = (ParameterizedType) ASTNode.copySubtree(ast, parameterizedMapType);
		parameterizedSubtreeCopy.setType(simpleEntryType);
		return parameterizedSubtreeCopy;
	}

	private MethodInvocation createNewMapEntrySetInvocation(TransformationData transformationData) {
		AST ast = astRewrite.getAST();
		MethodInvocation newMapEntrySetInvocation = ast.newMethodInvocation();
		newMapEntrySetInvocation.setName(ast.newSimpleName(ENTRY_SET));
		String mapVariableIdentifier = transformationData.getMapVariableIdentifier();
		SimpleName newMapVariableName = ast.newSimpleName(mapVariableIdentifier);
		newMapEntrySetInvocation.setExpression(newMapVariableName);
		return newMapEntrySetInvocation;
	}

	@SuppressWarnings("unchecked")
	private VariableDeclarationStatement createKeyDeclarationStatement(TransformationData transformationData,
			String mapEntryIdentifier) {
		AST ast = astRewrite.getAST();

		VariableDeclarationFragment keyDeclarationFragment = ast.newVariableDeclarationFragment();
		SingleVariableDeclaration loopParameter = transformationData.getLoopParameter();
		SimpleName newKeyName = (SimpleName) ASTNode.copySubtree(ast, loopParameter.getName());
		keyDeclarationFragment.setName(newKeyName);

		keyDeclarationFragment.extraDimensions()
			.addAll(ASTNode.copySubtrees(ast, loopParameter.extraDimensions()));

		Expression newKeyInitializer = createEntryGetterInvocation(mapEntryIdentifier, GET_KEY);
		keyDeclarationFragment.setInitializer(newKeyInitializer);
		VariableDeclarationStatement keyDeclarationStatement = ast
			.newVariableDeclarationStatement(keyDeclarationFragment);

		Type newKeyType = (Type) ASTNode.copySubtree(ast, loopParameter.getType());
		keyDeclarationStatement.setType(newKeyType);
		return keyDeclarationStatement;
	}

	private MethodInvocation createEntryGetterInvocation(String mapEntryIdentifier, String getterIdentifier) {
		AST ast = astRewrite.getAST();
		MethodInvocation valueInitializerReplacement = ast.newMethodInvocation();
		SimpleName methodNameGetValue = ast.newSimpleName(getterIdentifier);
		valueInitializerReplacement.setName(methodNameGetValue);
		SimpleName mapEntryName = ast.newSimpleName(mapEntryIdentifier);
		valueInitializerReplacement.setExpression(mapEntryName);
		return valueInitializerReplacement;
	}

	private Optional<TransformationData> extractTransformationData(EnhancedForStatement enhancedForStatement) {

		SupportedLoopStructure supportedForStatementData = SupportedLoopStructure
			.findSupportedLoopStructure(enhancedForStatement)
			.orElse(null);
		if (supportedForStatementData == null) {
			return Optional.empty();
		}
		SimpleName assumedMapVariableName = supportedForStatementData.getAssumedMapVariableName();

		IBinding binding = assumedMapVariableName.resolveBinding();
		if (binding == null) {
			return Optional.empty();
			// or better: throw exception...
		}
		if (binding.getKind() != IBinding.VARIABLE) {
			return Optional.empty();
		}
		IVariableBinding mapVariableBinding = (IVariableBinding) binding;
		if (mapVariableBinding.isField()) {
			return Optional.empty();
		}

		ITypeBinding typeBinding = mapVariableBinding.getType();
		if (!ClassRelationUtil.isContentOfTypes(typeBinding, JAVA_UTIL_MAP_SINGLETON_LIST) &&
				!ClassRelationUtil.isInheritingContentOfTypes(typeBinding, JAVA_UTIL_MAP_SINGLETON_LIST)) {
			return Optional.empty();
		}

		ITypeBinding[] typeArguments = typeBinding.getTypeArguments();
		if (typeArguments.length != 2) {
			return Optional.empty();
		}

		ITypeBinding keyTypeBinding = typeArguments[0];
		ITypeBinding valueTypeBinding = typeArguments[1];
		if (!isTypeSafe(keyTypeBinding) || !isTypeSafe(valueTypeBinding)) {
			return Optional.empty();
		}

		ParameterizedType parameterizedMapType = Optional.of(mapVariableBinding)
			.map(variableBinding -> getCompilationUnit().findDeclaringNode(variableBinding))
			.flatMap(this::findMapVariableType)
			.filter(Type::isParameterizedType)
			.map(ParameterizedType.class::cast)
			.orElse(null);

		if (parameterizedMapType == null) {
			return Optional.empty();
		}

		String mapEntryIdentifier = variableNameFactory.createSafeVariableName(enhancedForStatement, ENTRY);
		boolean keyVariableDeclarationNecessary = isKeyVariableDeclarationNecessary(supportedForStatementData);

		return Optional.of(new TransformationData(supportedForStatementData, parameterizedMapType, mapEntryIdentifier,
				keyVariableDeclarationNecessary));
	}

	private boolean isKeyVariableDeclarationNecessary(SupportedLoopStructure supportedForStatementData) {
		SingleVariableDeclaration loopParameter = supportedForStatementData.getParameter();
		String expectedKeyIdentifier = loopParameter.getName()
			.getIdentifier();
		SimpleNamesCollectorVisitor namesCollectorVisitor = new SimpleNamesCollectorVisitor(expectedKeyIdentifier);
		supportedForStatementData.getBody()
			.accept(namesCollectorVisitor);
		List<SimpleName> matchingSimpleNames = namesCollectorVisitor.getMatchingSimpleNames();
		SimpleName assumedMapGetterArgument = supportedForStatementData.getAssumedMapGetterArgument();
		return matchingSimpleNames.stream()
			.filter(name -> name != assumedMapGetterArgument)
			.filter(NameLocationInParent::canBeReferenceToLocalVariable)
			.anyMatch(name -> isReference(name, loopParameter));

	}

	private boolean isReference(SimpleName simpleName, SingleVariableDeclaration loopParameter) {


		IVariableBinding variableBinding;
		try {
			variableBinding = FindVariableBinding.findVariableBinding(simpleName)
				.orElse(null);
		} catch (UnresolvedBindingException e) {
			return false;
		}

		if (variableBinding == null || variableBinding.isField() || variableBinding.isParameter()) {
			return false;
		}

		ASTNode declaringNode = getCompilationUnit().findDeclaringNode(variableBinding);
		return declaringNode == loopParameter;
	}

	private Optional<Type> findMapVariableType(ASTNode declaringNode) {

		Optional<ASTNode> optionalDeclaringNode = Optional.of(declaringNode);
		if (declaringNode.getLocationInParent() == VariableDeclarationStatement.FRAGMENTS_PROPERTY) {
			return optionalDeclaringNode.map(ASTNode::getParent)
				.map(VariableDeclarationStatement.class::cast)
				.map(VariableDeclarationStatement::getType);
		}

		if (declaringNode.getLocationInParent() == VariableDeclarationExpression.FRAGMENTS_PROPERTY) {
			return optionalDeclaringNode.map(ASTNode::getParent)
				.map(VariableDeclarationExpression.class::cast)
				.map(VariableDeclarationExpression::getType);
		}

		if (declaringNode.getNodeType() == ASTNode.SINGLE_VARIABLE_DECLARATION) {
			return optionalDeclaringNode
				.map(SingleVariableDeclaration.class::cast)
				.map(SingleVariableDeclaration::getType);
		}

		return Optional.empty();

	}

	/**
	 * Copy of
	 * {@link eu.jsparrow.core.visitor.loop.stream.AbstractEnhancedForLoopToStreamASTVisitor#isTypeSafe(ITypeBinding)}
	 * TODO: if this method is really needed here, then create a Utility class
	 * with this method which can be both accessed by
	 * {@link eu.jsparrow.core.visitor.loop.stream.AbstractEnhancedForLoopToStreamASTVisitor}
	 * and by this class.
	 */
	protected boolean isTypeSafe(ITypeBinding typeBinding) {
		if (typeBinding.isRawType()) {
			return false;
		}

		if (typeBinding.isCapture()) {
			return false;
		}

		if (typeBinding.isWildcardType()) {
			return false;
		}

		if (typeBinding.isParameterizedType()) {
			for (ITypeBinding argument : typeBinding.getTypeArguments()) {
				if (!isTypeSafe(argument)) {
					return false;
				}
			}
		}

		return true;
	}

}
