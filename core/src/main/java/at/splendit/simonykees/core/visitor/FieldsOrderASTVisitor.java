package at.splendit.simonykees.core.visitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import at.splendit.simonykees.core.util.ASTNodeUtil;

/**
 * 
 * @author Ardit Ymeri
 *
 */
public class FieldsOrderASTVisitor extends AbstractASTRewriteASTVisitor {
	
	private CompilationUnit compilationUnit;

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		this.compilationUnit = compilationUnit;
		return true;
	}
	
	@Override
	public boolean visit(TypeDeclaration node) {
		
		if(compilationUnit != null) {
			List<Comment> comments = 
					ASTNodeUtil
					.returnTypedList(compilationUnit.getCommentList(), Comment.class);
			
			List<BodyDeclaration> bodyDeclarations = 
					ASTNodeUtil.returnTypedList(node.bodyDeclarations(), BodyDeclaration.class);
			
			Map<BodyDeclaration, List<Comment>>boundedComments = 
					extractBoundedComments(node, comments, bodyDeclarations);
			
			List<FieldDeclaration> fields = 
					fileterByDeclarationType(bodyDeclarations, FieldDeclaration.class);
			
			List<MethodDeclaration> methodsDeclarations = 
					fileterByDeclarationType(bodyDeclarations, MethodDeclaration.class);
			
			List<MethodDeclaration> constructors = 
					methodsDeclarations
					.stream()
					.filter(MethodDeclaration::isConstructor)
					.collect(Collectors.toList());
			
			List<MethodDeclaration> methods = 
					methodsDeclarations
					.stream()
					.filter(method -> !method.isConstructor())
					.collect(Collectors.toList());
			
			List<Initializer> initializers = 
					fileterByDeclarationType(bodyDeclarations, Initializer.class);
			
			List<EnumDeclaration> enums = 
					fileterByDeclarationType(bodyDeclarations, EnumDeclaration.class);
			
			List<AnnotationTypeDeclaration> annotations = 
					fileterByDeclarationType(bodyDeclarations, AnnotationTypeDeclaration.class);
			
			List<AnnotationTypeMemberDeclaration> annotationMembers = 
					fileterByDeclarationType(bodyDeclarations, AnnotationTypeMemberDeclaration.class);
			
			List<BodyDeclaration> sortedDeclarations = new ArrayList<>();
			
			sortedDeclarations.addAll(sortByModifier(fields));
			sortedDeclarations.addAll(initializers);
			sortedDeclarations.addAll(sortByModifier(constructors));
			sortedDeclarations.addAll(sortByModifier(methods));
			sortedDeclarations.addAll(sortByModifier(enums));
			sortedDeclarations.addAll(sortByModifier(annotations));
			sortedDeclarations.addAll(annotationMembers);
			
			if(!sortedDeclarations.isEmpty()) {
				ASTRewrite astRewrite = getAstRewrite();
				ListRewrite listRewrite = 
						astRewrite.getListRewrite(node, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
				BodyDeclaration firstDeclaration = sortedDeclarations.get(0);
				ASTNode firstTarget = astRewrite.createMoveTarget(firstDeclaration);
				List<Comment>firstDeclComments = boundedComments.get(firstDeclaration);
				firstDeclComments.forEach(comment -> comment.getAlternateRoot().delete());
				listRewrite.insertFirst((BodyDeclaration)firstTarget, null);
				listRewrite.remove(firstDeclaration, null);
				
				for(int i = 1; i<sortedDeclarations.size(); i++) {
					
					BodyDeclaration declaration = sortedDeclarations.get(i);
					ASTNode target = astRewrite.createMoveTarget(declaration);
					List<Comment>declComments = boundedComments.get(declaration);
					declComments.forEach(comment -> comment.getAlternateRoot().delete());
					listRewrite.insertAfter((BodyDeclaration)target, (BodyDeclaration)firstTarget, null);
					
					listRewrite.remove(declaration, null);
					firstTarget = target;
				}
			}
			
//			Collections.reverse(sortedDeclarations);
//			
//			ASTRewrite astRewrite = getAstRewrite();
//			ListRewrite listRewrite = 
//					astRewrite.getListRewrite(node, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
//			sortedDeclarations.forEach(declaration -> {
//				
//				ASTNode target = astRewrite.createMoveTarget(declaration);
//				List<Comment>declComments = boundedComments.get(declaration);
//				declComments.forEach(comment -> comment.getAlternateRoot().delete());
//				listRewrite.insertFirst((BodyDeclaration)target, null);
//				
//				listRewrite.remove(declaration, null);
//				
//			});
			
		}
		
		return true;
	}
	
	private Map<BodyDeclaration, List<Comment>> extractBoundedComments(TypeDeclaration node, 
			List<Comment> comments, List<BodyDeclaration> bodyDeclarations) {
		
		Map<BodyDeclaration, List<Comment>> boundComments = new HashMap<>();
		if(!bodyDeclarations.isEmpty()) {
			List<Comment> firstDeclComments = extractFirstDeclCommetns(node, bodyDeclarations, comments);
			boundComments.put(bodyDeclarations.get(0), firstDeclComments);
			
			int j = 0;
			for(int i = 1; i<bodyDeclarations.size(); i++) {
				BodyDeclaration previousDecl = bodyDeclarations.get(i-1);
				BodyDeclaration decl = bodyDeclarations.get(i);
				List<Comment> declComments = new ArrayList<>();
				while(j<comments.size() && comments.get(j).getStartPosition() < decl.getStartPosition()) {
					if(fallsBetween(comments.get(j), previousDecl, decl)) {						
						declComments.add(comments.get(j));
					}
					j++;
				}
				
				boundComments.put(decl, declComments);
			}

		}
		return boundComments;
	}

	private <T extends BodyDeclaration> List<T> sortByModifier(List<T> member) {
		List<T> sortedDeclarations = new ArrayList<>();
		
		sortedDeclarations.addAll(filterByModifier(member, Modifier.PUBLIC));
		sortedDeclarations.addAll(filterByModifier(member, Modifier.PROTECTED));
		sortedDeclarations.addAll(filterByPackageProtectedModifier(member));
		sortedDeclarations.addAll(filterByModifier(member, Modifier.PRIVATE));
		
		return sortedDeclarations;
	}

	private <T extends BodyDeclaration> List<T> fileterByDeclarationType(List<BodyDeclaration> bodyDeclarations, Class<T>type) {
		return
				bodyDeclarations
				.stream()
				.filter(type::isInstance)
				.map(type::cast)
				.collect(Collectors.toList());
	}
	
	@SuppressWarnings("unchecked")
	private <T extends BodyDeclaration> List<T> filterByModifier(List<T>members, int modifierFlag) {
		
		return 
				members
				.stream()
				.filter(member -> {
					 
					 return 
							 member
							 .modifiers()
							 .stream()
							 .filter(Modifier.class::isInstance)
							 .filter(modifier -> ((Modifier)modifier).getKeyword().toFlagValue() == modifierFlag)
							 .findAny()
							 .isPresent();
				})
				.collect(Collectors.toList());
	}
	
	private <T extends BodyDeclaration> List<T> filterByPackageProtectedModifier(List<T>members) {
		
		return 
				members
				.stream()
				.filter(member -> {
					int flag = member.getModifiers();
					return 
							!Modifier.isProtected(flag)
							&& !Modifier.isPublic(flag)
							&& !Modifier.isPrivate(flag);
				})
				.collect(Collectors.toList());
	}
	
	private boolean fallsBetween(Comment comment, BodyDeclaration previousDeclaration, BodyDeclaration declaration) {
		
		int previousEndPos = previousDeclaration.getStartPosition() + previousDeclaration.getLength();
		int startPos = declaration.getStartPosition();
		int commentStartPos = comment.getStartPosition();
		int commentEndPos = commentStartPos + comment.getLength();
		
		return 
				commentStartPos > previousEndPos 
				&& commentEndPos < startPos; 
	}
	
	private List<Comment> extractFirstDeclCommetns(TypeDeclaration node, List<BodyDeclaration> bodyDeclarations, 
			List<Comment>comments) {
		List<Comment>firstDeclComments = new ArrayList<>();
		if(!bodyDeclarations.isEmpty()) {
			// get the comments above the first body declaration
			int nodeStartPos = node.getStartPosition();
			int declStartPos = bodyDeclarations.get(0).getStartPosition();
			for(Comment comment : comments) {
				int commentStartPos = comment.getStartPosition();
				int commentEndPos = comment.getStartPosition() + comment.getLength();
				if(commentStartPos > nodeStartPos) {
					if(commentEndPos < declStartPos) {
						firstDeclComments.add(comment);
					} else {
						break;
					}
				}
 
			}
		}

		return firstDeclComments;
	}
}
