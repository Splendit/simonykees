package eu.jsparrow.core.visitor.make_final;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import eu.jsparrow.rules.common.util.ASTNodeUtil;

/**
 * This is a helper visitor for finding {@link FieldDeclaration}s that could be
 * made {@code final}. There are a few criteria which this visitor checks:
 * <ul>
 * <li><strong>{@code static final} fields</strong> must be initialized at the
 * declaration or in a static initializer but not in both.</li>
 * <li><strong>non-static {@code final} fields</strong> must be initialized in
 * only one of the following parts: at the declaration, in a non-static class
 * initializer or at the end of ALL constructors.</li>
 * </ul>
 * 
 * <strong>How to use:</strong>
 * <ol>
 * <li>Call the {@link ASTNode#accept(org.eclipse.jdt.core.dom.ASTVisitor)}
 * method of a {@link TypeDeclaration} or {@link CompilationUnit} with an
 * instance of this visitor</li>
 * <li>Get the {@code final} candidates by calling the
 * {@link #getFinalCandidates()} method
 * </ol>
 *
 * @since 3.12.0
 */
public class FinalInitializerCheckASTVisitor extends AbstractMakeFinalHelperVisitor {

	private final List<FieldDeclaration> fieldDeclarations = new ArrayList<>();

	private List<VariableDeclarationFragment> fieldInitializers = new ArrayList<>();
	private final List<VariableDeclarationFragment> nonStaticInitializerInitializers = new ArrayList<>();
	private final List<VariableDeclarationFragment> staticInitializerInitializers = new ArrayList<>();
	private final Map<Integer, List<VariableDeclarationFragment>> constructorInitializers = new HashMap<>();
	private List<VariableDeclarationFragment> multiplyAssignedDeclarations = new ArrayList<>();

	private Map<ASTNode, List<VariableDeclarationFragment>> tempAssignmentsInBlocksMap = new HashMap<>();
	private List<VariableDeclarationFragment> nonRootAssignment = new ArrayList<>();

	private int constructorCount = 0;
	private boolean isInConstructor = false;
	private ASTNode currentConstructor;

	@Override
	public boolean visit(FieldDeclaration fieldDeclaration) {
		int modifiers = fieldDeclaration.getModifiers();
		boolean isFinalOrVolatile = Modifier.isFinal(modifiers) || Modifier.isVolatile(modifiers);

		if (isFinalOrVolatile) {
			return true;
		}

		fieldDeclarations.add(fieldDeclaration);

		List<VariableDeclarationFragment> fragments = ASTNodeUtil.convertToTypedList(fieldDeclaration.fragments(),
				VariableDeclarationFragment.class);

		List<VariableDeclarationFragment> tempFieldInitializers = fragments.stream()
			.filter(f -> f.getInitializer() != null)
			.collect(Collectors.toList());

		fieldInitializers.addAll(tempFieldInitializers);

		return true;
	}
	
	@Override
	public boolean visit(LambdaExpression lambda) {
		
		if(isFieldInitializer(lambda)) {
			tempAssignmentsInBlocksMap.put(lambda, new ArrayList<>());
			currentConstructor = lambda;
		}
		
		return true;
	}
	
	@Override
	public void endVisit(LambdaExpression lambda) {
		if(isFieldInitializer(lambda)) {
			FieldDeclaration field = ASTNodeUtil.getSpecificAncestor(lambda, FieldDeclaration.class);
			List<VariableDeclarationFragment> tempAssignmentsInBlocks = tempAssignmentsInBlocksMap.getOrDefault(lambda, new ArrayList<>());
			if(ASTNodeUtil.hasModifier(field.modifiers(), Modifier::isStatic)) {
				staticInitializerInitializers.addAll(tempAssignmentsInBlocks);
			} else {
				nonStaticInitializerInitializers.addAll(tempAssignmentsInBlocks);
			}
			tempAssignmentsInBlocksMap.remove(lambda);
			currentConstructor = null;
		}
	}
	
	private boolean isFieldInitializer(LambdaExpression lambda) {
		if(lambda.getLocationInParent() == VariableDeclarationFragment.INITIALIZER_PROPERTY) {
			VariableDeclarationFragment fragment = (VariableDeclarationFragment)lambda.getParent();
			return fragment.getLocationInParent() == FieldDeclaration.FRAGMENTS_PROPERTY;
		}
		return false;
	}

	@Override
	public boolean visit(Initializer initializer) {
		tempAssignmentsInBlocksMap.put(initializer, new ArrayList<>());
		currentConstructor = initializer;
		return true;
	}

	@Override
	public void endVisit(Initializer initializer) {
		List<VariableDeclarationFragment> tempAssignmentsInBlocks = tempAssignmentsInBlocksMap.getOrDefault(initializer, new ArrayList<>());
		if (ASTNodeUtil.hasModifier(initializer.modifiers(), Modifier::isStatic)) {
			staticInitializerInitializers.addAll(tempAssignmentsInBlocks);
		} else {
			nonStaticInitializerInitializers.addAll(tempAssignmentsInBlocks);
		}

		tempAssignmentsInBlocksMap.remove(initializer);
		currentConstructor = null;
	}

	@Override
	public boolean visit(MethodDeclaration methodDeclaration) {
		if (!methodDeclaration.isConstructor()) {
			return isInConstructor;
		}
		currentConstructor = methodDeclaration;
		tempAssignmentsInBlocksMap.put(methodDeclaration, new ArrayList<>());
		isInConstructor = true;

		return true;
	}

	@Override
	public void endVisit(MethodDeclaration methodDeclaration) {
		if (!methodDeclaration.isConstructor()) {
			return;
		}
		
		List<VariableDeclarationFragment> tempAssignmentsInBlocks = tempAssignmentsInBlocksMap.getOrDefault(methodDeclaration, new ArrayList<>());
		constructorInitializers.put(constructorCount, tempAssignmentsInBlocks);
		tempAssignmentsInBlocksMap.remove(methodDeclaration);
		currentConstructor = null;
		isInConstructor = false;
		constructorCount++;
	}

	@Override
	public boolean visit(PrefixExpression infixExpression) {
		Expression expression = infixExpression.getOperand();
		VariableDeclarationFragment variableDeclarationFragment = extractFieldDeclarationFragmentFromExpression(
				expression);
		multiplyAssignedDeclarations.add(variableDeclarationFragment);
		return true;
	}

	@Override
	public boolean visit(PostfixExpression postfixExpression) {
		Expression expression = postfixExpression.getOperand();
		VariableDeclarationFragment variableDeclarationFragment = extractFieldDeclarationFragmentFromExpression(
				expression);
		multiplyAssignedDeclarations.add(variableDeclarationFragment);

		return true;
	}

	@Override
	public boolean visit(Assignment assignment) {
		Expression leftHandSide = assignment.getLeftHandSide();
		VariableDeclarationFragment variableDeclarationFragment = extractFieldDeclarationFragmentFromExpression(
				leftHandSide);

		if (isInNestedBlock(leftHandSide.getParent())) {
			/*
			 * Otherwise, wee need control flow analysis to determine if the
			 * field is assigned exactly once in each branch of the control
			 * flow.
			 */
			nonRootAssignment.add(variableDeclarationFragment);
		}

		if (variableDeclarationFragment != null) {
			if (!isInConstructor && isAlreadyAssigned(variableDeclarationFragment)) {
				multiplyAssignedDeclarations.add(variableDeclarationFragment);
			} else {
				List<VariableDeclarationFragment> tempAssignmentsInBlocks = tempAssignmentsInBlocksMap.getOrDefault(currentConstructor, new ArrayList<>());
				tempAssignmentsInBlocks.add(variableDeclarationFragment);
			}
		}
		return true;
	}

	private boolean isInNestedBlock(ASTNode assignment) {
		if (assignment.getLocationInParent() != ExpressionStatement.EXPRESSION_PROPERTY) {
			return true;
		}
		ExpressionStatement statement = (ExpressionStatement) assignment.getParent();
		return statement.getParent()
			.getParent() != currentConstructor;
	}

	/**
	 * After executing this visitor, this method provides the {@code final}
	 * candidates for {@link FieldDeclaration}s.
	 * 
	 * @return candidate {@link FieldDeclaration}s which meet the criteria for
	 *         changing them to {@code final}.
	 */
	public List<FieldDeclaration> getFinalCandidates() {
		List<FieldDeclaration> candidates = new ArrayList<>();

		for (FieldDeclaration fieldDeclaration : fieldDeclarations) {
			boolean isStatic = ASTNodeUtil.hasModifier(fieldDeclaration.modifiers(), Modifier::isStatic);

			boolean isCandidate = false;
			if (isStatic) {
				isCandidate = isStaticFinalCandidate(fieldDeclaration);
			} else {
				isCandidate = isNonStaticFinalCandidate(fieldDeclaration);
			}

			if (isCandidate) {
				candidates.add(fieldDeclaration);
			}
		}

		return candidates;
	}

	private boolean isStaticFinalCandidate(FieldDeclaration fieldDeclaration) {
		List<VariableDeclarationFragment> fragments = ASTNodeUtil.convertToTypedList(fieldDeclaration.fragments(),
				VariableDeclarationFragment.class);

		/*
		 * This is the only visitor analyzing constructors
		 */
		boolean reassignedInConstructor = constructorInitializers.values()
			.stream()
			.filter(Objects::nonNull)
			.flatMap(List::stream)
			.filter(Objects::nonNull)
			.anyMatch(fragments::contains);

		return !reassignedInConstructor && fragments.stream()
			.allMatch(fragment -> (fieldInitializers.contains(fragment)
					^ staticInitializerInitializers.contains(fragment))
					&& !multiplyAssignedDeclarations.contains(fragment));
	}

	private boolean isNonStaticFinalCandidate(FieldDeclaration fieldDeclaration) {
		List<VariableDeclarationFragment> fragments = ASTNodeUtil.convertToTypedList(fieldDeclaration.fragments(),
				VariableDeclarationFragment.class);

		return fragments.stream()
			.allMatch(fragment -> {
				boolean declaration = fieldInitializers.contains(fragment);
				boolean initializer = nonStaticInitializerInitializers.contains(fragment);

				boolean constructor = !constructorInitializers.isEmpty() && constructorInitializers.entrySet()
					.stream()
					.map(Map.Entry::getValue)
					.allMatch((List<VariableDeclarationFragment> entry) -> entry.contains(fragment));
				boolean atLeastOneConstructor = constructorInitializers.entrySet()
					.stream()
					.map(Map.Entry::getValue)
					.anyMatch((List<VariableDeclarationFragment> entry) -> entry.contains(fragment));

				boolean multiplyAssigned = multiplyAssignedDeclarations.contains(fragment);
				boolean hasNonRootAssignment = nonRootAssignment.contains(fragment);

				return ((declaration ^ initializer ^ constructor) ^ (declaration && initializer && constructor))
						&& !multiplyAssigned && !(!constructor && atLeastOneConstructor) && !hasNonRootAssignment;
			});
	}

	private boolean isAlreadyAssigned(VariableDeclarationFragment fragment) {
		return fieldInitializers.contains(fragment) || staticInitializerInitializers.contains(fragment)
				|| nonStaticInitializerInitializers.contains(fragment) || constructorInitializers.entrySet()
					.stream()
					.map(Map.Entry::getValue)
					.anyMatch(list -> list.contains(fragment));
	}
}
