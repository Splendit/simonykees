package eu.jsparrow.core.visitor.make_final;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

/**
 * This is a helper visitor for finding {@link FieldDeclaration}s that could be
 * made {@code final}. There are a few criteria which this visitor checks:
 * <ul>
 * <li><strong>{@code static final} fields</strong> must be initialised at the
 * declaration or in a static initialiser but not in both.</li>
 * <li><strong>non-static {@code final} fields</strong> must be initialised in
 * only one of the following parts: at the declaration, in a non-static class
 * initialiser or at the end of ALL constructors.</li>
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
public class FinalInitializerCheckASTVisitor extends AbstractASTRewriteASTVisitor {

	private final List<FieldDeclaration> fieldDeclarations = new ArrayList<>();

	private List<VariableDeclarationFragment> fieldInitializers = new ArrayList<>();
	private final List<VariableDeclarationFragment> nonStaticInitializerInitializers = new ArrayList<>();
	private final List<VariableDeclarationFragment> staticInitializerInitializers = new ArrayList<>();
	private final Map<Integer, List<VariableDeclarationFragment>> constructorInitializers = new HashMap<>();
	private List<VariableDeclarationFragment> multiplyAssignedDeclarations = new ArrayList<>();

	private List<VariableDeclarationFragment> tempAssignmentsInBlocks;

	private int constructorCount = 0;

	@Override
	public boolean visit(FieldDeclaration fieldDeclaration) {
		fieldDeclarations.add(fieldDeclaration);

		List<VariableDeclarationFragment> fragments = ASTNodeUtil.convertToTypedList(fieldDeclaration.fragments(),
				VariableDeclarationFragment.class);

		fieldInitializers = fragments.stream()
			.filter(f -> f.getInitializer() != null)
			.collect(Collectors.toList());

		return true;
	}

	@Override
	public boolean visit(Initializer initializer) {
		tempAssignmentsInBlocks = new LinkedList<>();
		return true;
	}

	@Override
	public void endVisit(Initializer initializer) {
		if (ASTNodeUtil.hasModifier(initializer.modifiers(), Modifier::isStatic)) {
			staticInitializerInitializers.addAll(tempAssignmentsInBlocks);
		} else {
			nonStaticInitializerInitializers.addAll(tempAssignmentsInBlocks);
		}

		tempAssignmentsInBlocks = null;
	}

	@Override
	public boolean visit(MethodDeclaration methodDeclaration) {
		if (!methodDeclaration.isConstructor()) {
			return false;
		}

		tempAssignmentsInBlocks = new LinkedList<>();

		return true;
	}

	@Override
	public void endVisit(MethodDeclaration methodDeclaration) {
		if (!methodDeclaration.isConstructor()) {
			return;
		}

		constructorInitializers.put(constructorCount, tempAssignmentsInBlocks);
		tempAssignmentsInBlocks = null;
		constructorCount++;
	}

	@Override
	public boolean visit(Assignment assignment) {
		Expression leftHandSide = assignment.getLeftHandSide();
		CompilationUnit compilationUnit = ASTNodeUtil.getSpecificAncestor(assignment, CompilationUnit.class);

		VariableDeclarationFragment variableDeclarationFragment;
		switch (leftHandSide.getNodeType()) {
		case ASTNode.FIELD_ACCESS:
			variableDeclarationFragment = getVariableDeclarationFragment(compilationUnit, (FieldAccess) leftHandSide);
			break;
		case ASTNode.QUALIFIED_NAME:
		case ASTNode.SIMPLE_NAME:
			variableDeclarationFragment = getVariableDeclarationFragment(compilationUnit, (Name) leftHandSide);
			break;
		default:
			variableDeclarationFragment = null;
		}

		if (variableDeclarationFragment != null) {
			if (tempAssignmentsInBlocks.contains(variableDeclarationFragment)) {
				multiplyAssignedDeclarations.add(variableDeclarationFragment);
			} else {
				tempAssignmentsInBlocks.add(variableDeclarationFragment);
			}
		}

		return false;
	}

	private VariableDeclarationFragment getVariableDeclarationFragment(CompilationUnit compilationUnit,
			FieldAccess fieldAccess) {
		IVariableBinding binding = fieldAccess.resolveFieldBinding();
		return getVariableDeclarationFragmentFromBinding(compilationUnit, binding);
	}

	private VariableDeclarationFragment getVariableDeclarationFragment(CompilationUnit compilationUnit, Name name) {
		IBinding binding = name.resolveBinding();
		return getVariableDeclarationFragmentFromBinding(compilationUnit, binding);
	}

	private VariableDeclarationFragment getVariableDeclarationFragmentFromBinding(CompilationUnit compilationUnit,
			IBinding binding) {
		if (binding == null) {
			return null;
		}

		ASTNode bindingNode = compilationUnit.findDeclaringNode(binding);
		if (bindingNode instanceof VariableDeclarationFragment) {
			return (VariableDeclarationFragment) bindingNode;
		}

		return null;
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

		return fragments.stream()
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
					.allMatch((Map.Entry<Integer, List<VariableDeclarationFragment>> entry) -> entry.getValue()
						.contains(fragment));
				boolean multiplyAssigned = multiplyAssignedDeclarations.contains(fragment);

				return ((declaration ^ initializer ^ constructor) ^ (declaration && initializer && constructor))
						&& !multiplyAssigned;
			});
	}
}
