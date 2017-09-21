package eu.jsparrow.core.visitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
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

import eu.jsparrow.core.util.ASTNodeUtil;

/**
 * Sorts the members of the class in the following order:
 * 		<ul>
 * 		 <li> fields </li>
 * 		 <li> initializers </li>
 * 		 <li> constructors </li>
 * 		 <li> methods </li>
 * 		 <li> enumeration declarations </li>
 * 		 <li> annotation declarations </li>
 * 		 <li> inner classes </li>
 * 		</ul>
 * <p>
 * Furthermore, the members of the same type (except for fields and methods), are also sorted
 * according to their modifier. The priority of the modifiers is 
 * as follows:
 * 		<ul>
 * 		<li> static </li>
 * 		<li> public </li>
 * 		<li> protected </li>
 * 		<li> package protected (no modifier) </li>
 * 		<li> private </li>
 * 		</ul>
 * 
 * @author Ardit Ymeri
 * @since 1.2
 *
 */
public class RearrangeClassMembersASTVisitor extends AbstractASTRewriteASTVisitor {
	
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
			
			// classify the body declarations according to their type
			
			// static fields and static initializers are handled together
			List<BodyDeclaration> staticFieldsAndInitializers = 
					applyFilter(
							bodyDeclarations, 
							member -> 
							isStaticMember(member)
							&& (FieldDeclaration.class.isInstance(member)
									|| Initializer.class.isInstance(member)));
			
			List<BodyDeclaration> instanceFields = 
					applyFilter(
							bodyDeclarations, 
							member -> 
							FieldDeclaration.class.isInstance(member) 
							&& !isStaticMember(member));
			
			List<BodyDeclaration> instanceInitializers = 
					applyFilter(
							bodyDeclarations, 
							member -> 
							Initializer.class.isInstance(member) 
									&& !isStaticMember(member));
			
			List<MethodDeclaration> methodsDeclarations = 
					filterByDeclarationType(bodyDeclarations, MethodDeclaration.class);
			
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
			
			List<EnumDeclaration> enums = 
					filterByDeclarationType(bodyDeclarations, EnumDeclaration.class);
			
			List<AnnotationTypeDeclaration> annotations = 
					filterByDeclarationType(bodyDeclarations, AnnotationTypeDeclaration.class);
			
			List<AnnotationTypeMemberDeclaration> annotationMembers = 
					filterByDeclarationType(bodyDeclarations, AnnotationTypeMemberDeclaration.class);
			
			// sort all body declarations
			List<BodyDeclaration> sortedDeclarations = new ArrayList<>();
			
			/**
			 * Fields are not sorted by the access modifier because they 
			 * may reference each-other during initialization.
			 */
			sortedDeclarations.addAll(staticFieldsAndInitializers);  
			sortedDeclarations.addAll(instanceFields); 
			sortedDeclarations.addAll(instanceInitializers);
			sortedDeclarations.addAll(sortMembers(constructors));
			sortedDeclarations.addAll(methods);
			sortedDeclarations.addAll(sortMembers(enums));
			sortedDeclarations.addAll(sortMembers(annotations));
			sortedDeclarations.addAll(annotationMembers);
			
			int startFrom = calcStartFromIndex(bodyDeclarations, sortedDeclarations);
			
			// swap the position according to the new order.
			if(!sortedDeclarations.isEmpty() && startFrom >= 0) {
				ASTRewrite astRewrite = getAstRewrite();
				ListRewrite listRewrite = 
						astRewrite.getListRewrite(node, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
				
				BodyDeclaration firstDeclaration = sortedDeclarations.get(startFrom);
				ASTNode firstTarget = astRewrite.createMoveTarget(firstDeclaration);
				
				List<Comment>firstDeclComments = boundedComments.get(firstDeclaration);
				firstDeclComments.forEach(comment -> comment.getAlternateRoot().delete());
				listRewrite.insertAt((BodyDeclaration)firstTarget, startFrom, null);
				listRewrite.remove(firstDeclaration, null);
				
				for(int i = startFrom + 1; i<sortedDeclarations.size(); i++) {
					
					BodyDeclaration declaration = sortedDeclarations.get(i);
					ASTNode target = astRewrite.createMoveTarget(declaration);
					List<Comment>declComments = boundedComments.get(declaration);
					declComments.forEach(comment -> comment.getAlternateRoot().delete());
					listRewrite.insertAfter((BodyDeclaration)target, (BodyDeclaration)firstTarget, null);
					
					listRewrite.remove(declaration, null);
					firstTarget = target;
				}
			}
		}
		
		return true;
	}
	
	/**
	 * Finds the first body declaration in the list of the sorted body
	 * declarations, which is different from the corresponding position in the
	 * list of unsorted body declarations.
	 * 
	 * @param bodyDeclarations
	 *            unsorted list of body declarations
	 * @param sortedDeclarations
	 *            sorted list of body declarations
	 * @return the index of the first element in the sorted list which is
	 *         different from the unsorted list, or {@code -1} if every element
	 *         in the sorted list matches with the corresponding element in the
	 *         unsorted list.
	 */
	private int calcStartFromIndex(List<BodyDeclaration> bodyDeclarations, List<BodyDeclaration> sortedDeclarations) {
		int i = 0;
		while (i < sortedDeclarations.size()) {
			if (sortedDeclarations.get(i) != bodyDeclarations.get(i)) {
				return i;
			}
			i++;
		}

		return -1;
	}

	/**
	 * Sorts the given list of body declarations of the same type, 
	 * by the modifier. Static members have higher priority. Then, comes
	 * public, protected, package protected and private.
	 * 
	 * @param members body declarations to be sorted
	 * @return sorted list of body declarations
	 */
	private <T extends BodyDeclaration> List<T> sortMembers(List<T> members) {
		// extract static members
		List<T> staticMembers = 
				members
				.stream()
				.filter(this::isStaticMember)
				.collect(Collectors.toList());
		// extract instance members
		List<T> instanceMembers =
				members
				.stream()
				.filter(member -> !isStaticMember(member))
				.collect(Collectors.toList());
		
		List<T> sortedMembers = new ArrayList<>();

		// sort static members by modifier
		sortedMembers.addAll(sortByAccessModifier(staticMembers));
		// sort instance members by modifier
		sortedMembers.addAll(sortByAccessModifier(instanceMembers));
		
		return sortedMembers;
	}
	
	/**
	 * Checks whether the given body declaration is a static or 
	 * an instance declaration. 
	 *  
	 * @param member a body declaration
	 * @return if it is a static body declaration.
	 */
	private <T extends BodyDeclaration> boolean isStaticMember(T member) {
		return ASTNodeUtil.hasModifier(member.modifiers(), modifier -> modifier.isStatic());
	}

	/**
	 * Sorts the given list of body declarations according to their 
	 * access modifiers. The priority of the modifiers is as follows:
	 * <ul>
	 * 	<li>public</li>
	 *  <li>protected</li>
	 *  <li>(none)</li>
	 *  <li>private</li>
	 * </ul>
	 * 
	 * <b>Note</b>: The static modifier and the annotations are not considered! 
	 * 
	 * @param member body declarations of the same type, to be sorted.
	 * @return sorted list of body declarations
	 */
	private <T extends BodyDeclaration> List<T> sortByAccessModifier(List<T> member) {
		List<T> sortedDeclarations = new ArrayList<>();
		
		sortedDeclarations.addAll(filterByModifier(member, modifier -> modifier.isPublic()));
		sortedDeclarations.addAll(filterByModifier(member, modifier -> modifier.isProtected()));
		sortedDeclarations.addAll(filterByPackageProtectedModifier(member));
		sortedDeclarations.addAll(filterByModifier(member, modifier -> modifier.isPrivate()));
		
		return sortedDeclarations;
	}

	/**
	 * Extracts the body declarations of the given from the given list of 
	 * the body declarations.  
	 * 
	 * @param bodyDeclarations list of original body declarations
	 * @param type type of the declarations to be filtered. 
	 * @return 
	 */
	private <T extends BodyDeclaration> List<T> filterByDeclarationType(List<BodyDeclaration> bodyDeclarations, Class<T>type) {
		return
				bodyDeclarations
				.stream()
				.filter(type::isInstance)
				.map(type::cast)
				.collect(Collectors.toList());
	}
	
	/**
	 * Extracts the body declarations having the given modifier flag
	 * from the given list of body declarations. 
	 * 
	 * @see Modifier for the available modifier flags. 
	 *  
	 * @param members list of  body declarations
	 * @param modifierFlag modifier flag.
	 * @return list of body declarations having the given modifier.
	 */
	private <T extends BodyDeclaration> List<T> filterByModifier(List<T>members, Predicate<Modifier> modifierPredicate) {
		/*Predicate<T> filter = member -> {
			 
			 return 
					 ASTNodeUtil.convertToTypedList(member.modifiers(), Modifier.class)
					 .stream()
					 .anyMatch(modifier -> modifier.getKeyword().toFlagValue() == modifierFlag);
		};*/
		
		return applyFilter(members, member -> ASTNodeUtil.hasModifier(member.modifiers(), modifierPredicate));
	}
	
	/**
	 * Extracts the body declarations with <b>NO</b> access modifier.
	 * The difference from {@link #filterByModifier(List, int)} is that
	 * it checks for the absence of an access modifier.
	 *  
	 * @param members list of body declarations.
	 * @return list of body declarations with no access modifier.
	 */
	private <T extends BodyDeclaration> List<T> filterByPackageProtectedModifier(List<T>members) {
		Predicate<T> packageProtectedFilter = member -> {
			int flag = member.getModifiers();
			return 
					!Modifier.isProtected(flag)
					&& !Modifier.isPublic(flag)
					&& !Modifier.isPrivate(flag);
		};
		return applyFilter(members, packageProtectedFilter);
	}
	
	/** Applies the given filter to the given collection.
	 * 
	 * @param members list to filter
	 * @param filter defines the filter for the collection.
	 * @return list of members satisfying the filter
	 */
	private <T extends BodyDeclaration> List<T> applyFilter(List<T> members, Predicate<T> filter) {
		return 
				members
				.stream()
				.filter(filter)
				.collect(Collectors.toList());
	}
	
	/**
	 * Finds the comments that belong to a member of the class.
	 * A comment is considered to belong to a member of the class
	 * if it is placed above it, with possible empty lines in 
	 * between.
	 * 
	 * @param node represents the body of the whole class.
	 * @param comments list of all comments in the compilation unit.
	 * @param bodyDeclarations list of body declarations in the class.
	 * 
	 * @return a map representing the comments belonging to a member of the class.
	 */
	private Map<BodyDeclaration, List<Comment>> extractBoundedComments(TypeDeclaration node, 
			List<Comment> comments, List<BodyDeclaration> bodyDeclarations) {
		
		Map<BodyDeclaration, List<Comment>> boundComments = new HashMap<>();
		if(!bodyDeclarations.isEmpty()) {
			List<Comment> firstDeclComments = extractFirstDeclComments(node, bodyDeclarations, comments);
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
	
	/**
	 * Finds the comments belonging to the very first declaration in the 
	 * body of the class. 
	 * 
	 * @param node represents the body of the whole class.
	 * @param comments list of all comments in the compilation unit.
	 * @param bodyDeclarations list of body declarations in the class.
	 * 
	 * @return list of comments belonging to the very first declaration.
	 */
	private List<Comment> extractFirstDeclComments(TypeDeclaration node, List<BodyDeclaration> bodyDeclarations, 
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
	
	/**
	 * Checks whether a given comment is placed between the given 
	 * two consecutive body declarations. 
	 * 
	 * @param comment comment to be checked
	 * @param previousDeclaration 
	 * @param declaration
	 * 
	 * @return if the comment is placed after the previousDeclartion and before
	 * the declaration.
	 */
	private boolean fallsBetween(Comment comment, BodyDeclaration previousDeclaration, BodyDeclaration declaration) {
		
		int previousEndPos = previousDeclaration.getStartPosition() + previousDeclaration.getLength();
		int startPos = declaration.getStartPosition();
		int commentStartPos = comment.getStartPosition();
		int commentEndPos = commentStartPos + comment.getLength();
		
		return 
				commentStartPos > previousEndPos 
				&& commentEndPos < startPos; 
	}
}
