package eu.jsparrow.core.markers.visitor;

import java.util.List;
import java.util.function.Predicate;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

import eu.jsparrow.core.markers.RefactoringEventImpl;
import eu.jsparrow.core.markers.common.Resolver;
import eu.jsparrow.core.rule.RuleDescriptionFactory;
import eu.jsparrow.core.rule.impl.FunctionalInterfaceRule;
import eu.jsparrow.core.visitor.functionalinterface.FunctionalInterfaceASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RuleDescription;

/**
 * A visitor for resolving one issue of type
 * {@link FunctionalInterfaceASTVisitor}.
 * 
 * @since 4.0.0
 *
 */
public class FunctionalInterfaceResolver extends FunctionalInterfaceASTVisitor implements Resolver {

	public static final String ID = "FunctionalInterfaceResolver"; //$NON-NLS-1$

	private IJavaElement javaElement;
	private Predicate<ASTNode> positionChecker;
	private RuleDescription description;

	public FunctionalInterfaceResolver(Predicate<ASTNode> positionChecker) {
		this.positionChecker = positionChecker;
		this.description = RuleDescriptionFactory.findByRuleId(FunctionalInterfaceRule.RULE_ID);
	}

	@Override
	public RuleDescription getDescription() {
		return this.description;
	}

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		javaElement = compilationUnit.getJavaElement();
		return super.visit(compilationUnit);
	}

	@Override
	public boolean visit(AnonymousClassDeclaration node) {
		if (positionChecker.test(node.getParent())) {
			return super.visit(node);
		}
		return false;
	}

	@Override
	public void addMarkerEvent(ClassInstanceCreation classInstanceCreation, List<SingleVariableDeclaration> parameters,
			Block block) {
		LambdaExpression representingNode = createRepresentingNode(parameters, block);
		int highlightLenght = representingNode.toString()
			.length();
		int credit = description.getCredit();
		RefactoringEventImpl event = new RefactoringEventImpl(ID, Messages.FunctionalInterfaceResolver_name,
				Messages.FunctionalInterfaceResolver_message, javaElement,
				highlightLenght, classInstanceCreation,
				representingNode, credit);
		addMarkerEvent(event);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private LambdaExpression createRepresentingNode(List<SingleVariableDeclaration> parameters,
			Block moveBlock) {
		AST ast = moveBlock.getAST();
		LambdaExpression lambda = ast.newLambdaExpression();
		if (parameters != null) {
			List lambdaParameters = lambda.parameters();
			for (SingleVariableDeclaration parameter : parameters) {
				SingleVariableDeclaration copy = (SingleVariableDeclaration) ASTNode.copySubtree(ast, parameter);
				lambdaParameters.add(copy);
			}
		}
		Block blockCopy = (Block) ASTNode.copySubtree(ast, moveBlock);
		lambda.setBody(blockCopy);
		return lambda;
	}
}
