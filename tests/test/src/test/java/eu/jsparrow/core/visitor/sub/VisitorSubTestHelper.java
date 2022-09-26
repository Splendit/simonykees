package eu.jsparrow.core.visitor.sub;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public class VisitorSubTestHelper {

	static MethodDeclaration findUniqueMethodDeclaration(TypeDeclaration typeDeclaration, String methodName) {
		List<MethodDeclaration> methodDeclarations = Arrays.stream(typeDeclaration.getMethods())
			.filter(declaration -> declaration.getName()
				.getIdentifier()
				.equals(methodName))
			.collect(Collectors.toList());
		assertEquals(1, methodDeclarations.size());
		return methodDeclarations.get(0);
	}

	static ThrowStatement findUniqueThrowStatement(ASTNode visitedNode) {
		return findUniqueThrowStatement(visitedNode, throwStatement -> true);
	}

	static ThrowStatement findUniqueThrowStatement(ASTNode visitedNode, Predicate<ThrowStatement> predicate) {
		List<ThrowStatement> listWithThrowStatement = new ArrayList<>();
		ASTVisitor visitor = new ASTVisitor() {

			@Override
			public boolean visit(ThrowStatement node) {
				if (predicate.test(node)) {
					listWithThrowStatement.add(node);
				}
				return true;
			}

		};
		visitedNode.accept(visitor);
		assertEquals(1, listWithThrowStatement.size());
		return listWithThrowStatement.get(0);
	}

	static List<MethodInvocation> findMethodInvocations(ASTNode visitedNode) {
		List<MethodInvocation> methodInvocations = new ArrayList<>();
		ASTVisitor visitor = new ASTVisitor() {

			@Override
			public boolean visit(MethodInvocation node) {
				methodInvocations.add(node);
				return true;
			}

		};
		visitedNode.accept(visitor);
		return methodInvocations;
	}

	static MethodInvocation findUniqueMethodInvocation(ASTNode visitedNode) {
		List<MethodInvocation> methodInvocations = findMethodInvocations(visitedNode);
		assertEquals(1, methodInvocations.size());
		return methodInvocations.get(0);
	}

	static List<ClassInstanceCreation> findClassInstanceCreations(ASTNode visitedNode) {
		List<ClassInstanceCreation> classInstanceCreations = new ArrayList<>();
		ASTVisitor visitor = new ASTVisitor() {

			@Override
			public boolean visit(ClassInstanceCreation node) {
				classInstanceCreations.add(node);
				return true;
			}

		};
		visitedNode.accept(visitor);
		return classInstanceCreations;
	}

	static ClassInstanceCreation findUniqueClassInstanceCreation(ASTNode visitedNode) {
		List<ClassInstanceCreation> classInstanceCreations = findClassInstanceCreations(visitedNode);
		assertEquals(1, classInstanceCreations.size());
		return classInstanceCreations.get(0);
	}

	static List<TryStatement> findTryStatements(ASTNode visitedNode) {
		List<TryStatement> classInstanceCreations = new ArrayList<>();
		ASTVisitor visitor = new ASTVisitor() {

			@Override
			public boolean visit(TryStatement node) {
				classInstanceCreations.add(node);
				return true;
			}

		};
		visitedNode.accept(visitor);
		return classInstanceCreations;
	}

	static TryStatement findUniqueTryStatement(ASTNode visitedNode) {
		List<TryStatement> classInstanceCreations = findTryStatements(visitedNode);
		assertEquals(1, classInstanceCreations.size());
		return classInstanceCreations.get(0);
	}
}
