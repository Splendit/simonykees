package eu.jsparrow.core.markers.visitor;

import java.util.List;
import java.util.function.Predicate;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.PrimitiveType.Code;

import eu.jsparrow.core.rule.RuleDescriptionFactory;
import eu.jsparrow.core.rule.impl.UseArraysStreamRule;
import eu.jsparrow.core.visitor.impl.UseArraysStreamASTVisitor;
import eu.jsparrow.rules.common.RefactoringEventImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.Resolver;

public class UseArraysStreamResolver extends UseArraysStreamASTVisitor implements Resolver {
	public static final String ID = "UseArraysStreamResolver"; //$NON-NLS-1$

	private Predicate<ASTNode> positionChecker;
	private RuleDescription description;

	public UseArraysStreamResolver(Predicate<ASTNode> positionChecker) {
		this.positionChecker = positionChecker;
		this.description = RuleDescriptionFactory
			.findByRuleId(UseArraysStreamRule.RULE_ID);
	}

	@Override
	public RuleDescription getDescription() {
		return this.description;
	}

	@Override
	public boolean visit(MethodInvocation methodInvocation) {
		if (positionChecker.test(methodInvocation.getParent())) {
			super.visit(methodInvocation);
		}
		return true;
	}
	
	@Override
	public void addMarkerEvent(MethodInvocation parent, List<Expression> arguments) {
		int credit = description.getCredit();
		MethodInvocation newNode = createRepresentingNode(parent, arguments);
		int highlightLength = 0;
		int offset = parent.getStartPosition();
		int length = parent.getLength();
		CompilationUnit cu = getCompilationUnit();
		int lineNumber = cu.getLineNumber(parent.getStartPosition());
		IJavaElement javaElement = cu.getJavaElement();
		RefactoringMarkerEvent event = new RefactoringEventImpl.Builder()
			.withResolver(ID)
			.withName(description.getName())
			.withMessage(description.getDescription())
			.withIJavaElement(javaElement)
			.withHighlightLength(highlightLength)
			.withOffset(offset)
			.withCodePreview(newNode.toString())
			.withLength(length)
			.withWeightValue(credit)
			.withLineNumber(lineNumber)
			.build();
		addMarkerEvent(event);
	}

	@Override
	public void addMarkerEvent(MethodInvocation parent, List<Expression> arguments, String name,
			Expression experssion) {
		int credit = description.getCredit();
		MethodInvocation newNode = createRepresentingNode(parent, arguments, name, experssion);
		int highlightLength = 0;
		int offset = parent.getStartPosition();
		int length = parent.getLength();
		CompilationUnit cu = getCompilationUnit();
		int lineNumber = cu.getLineNumber(parent.getStartPosition());
		IJavaElement javaElement = cu.getJavaElement();
		RefactoringMarkerEvent event = new RefactoringEventImpl.Builder()
			.withResolver(ID)
			.withName(description.getName())
			.withMessage(description.getDescription())
			.withIJavaElement(javaElement)
			.withHighlightLength(highlightLength)
			.withOffset(offset)
			.withCodePreview(newNode.toString())
			.withLength(length)
			.withWeightValue(credit)
			.withLineNumber(lineNumber)
			.build();
		addMarkerEvent(event);
	}


	@SuppressWarnings("unchecked")
	private MethodInvocation createRepresentingNode(MethodInvocation parent, List<Expression> arguments, String name,
			Expression experssion) {
		AST ast = parent.getAST();
		ArrayCreation arrayCreation = ast.newArrayCreation();
		Code primitiveType = PrimitiveType.toCode(name);
		ArrayType arrayType = ast.newArrayType(ast.newPrimitiveType(primitiveType));
		arrayCreation.setType(arrayType);
		ArrayInitializer initializer = ast.newArrayInitializer();
		List<Expression> initializerExpressions = initializer.expressions();
		arguments.stream()
			.map(arg -> (Expression) ASTNode.copySubtree(ast, arg))
			.forEach(initializerExpressions::add);
		arrayCreation.setInitializer(initializer);
		Expression streamExpression;
		if(experssion == null) {
			streamExpression = ast.newSimpleName("Arrays"); //$NON-NLS-1$
		} else {
			streamExpression = (Expression) ASTNode.copySubtree(ast, experssion);
		}
		
		MethodInvocation stream = ast.newMethodInvocation();
		stream.setName(ast.newSimpleName("stream")); //$NON-NLS-1$
		stream.setExpression(streamExpression);
		stream.arguments().add(arrayCreation);
		return stream;
	}

	@SuppressWarnings("unchecked")
	private MethodInvocation createRepresentingNode(MethodInvocation parent, List<Expression> arguments) {
		AST ast = parent.getAST();
		MethodInvocation of = ast.newMethodInvocation();
		of.setName(ast.newSimpleName("of")); //$NON-NLS-1$
		of.setExpression(ast.newSimpleName("Stream")); //$NON-NLS-1$
		for(Expression argument : arguments) {
			of.arguments().add((Expression)ASTNode.copySubtree(ast, argument));
		}
		return of;
	}
}
