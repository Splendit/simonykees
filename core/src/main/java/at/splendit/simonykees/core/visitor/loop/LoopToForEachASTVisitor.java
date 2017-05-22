package at.splendit.simonykees.core.visitor.loop;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;

import at.splendit.simonykees.core.builder.NodeBuilder;
import at.splendit.simonykees.core.util.ASTNodeUtil;
import at.splendit.simonykees.core.visitor.AbstractAddImportASTVisitor;
import at.splendit.simonykees.core.visitor.sub.VariableDeclarationsVisitor;

public abstract class LoopToForEachASTVisitor extends AbstractAddImportASTVisitor {

	protected static final String ITERATOR_FULLY_QUALLIFIED_NAME = java.util.Iterator.class.getName();
	protected static final String ITERABLE_FULLY_QUALIFIED_NAME = java.lang.Iterable.class.getName();
	protected static final String SIZE = "size"; //$NON-NLS-1$
	protected static final String LENGTH = "length"; //$NON-NLS-1$
	protected static final String DEFAULT_ITERATOR_NAME = "iterator"; //$NON-NLS-1$
	protected static final String KEY_SEPARATOR = "->"; //$NON-NLS-1$
	protected static final String DOT = "."; //$NON-NLS-1$
	
	private CompilationUnit compilationUnit;
	private Map<String, String> tempIntroducedNames;
	private Set<String> newImports = new HashSet<>();
	
	protected LoopToForEachASTVisitor() {
		this.tempIntroducedNames = new HashMap<>();
	}
	
	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		this.compilationUnit = compilationUnit;
		return true;
	}
	
	@Override
	public void endVisit(CompilationUnit cu) {
		PackageDeclaration cuPackage = cu.getPackage();
		Name packageName = cuPackage.getName();
		String packageQualifiedName = packageName.getFullyQualifiedName();
		List<AbstractTypeDeclaration> cuDeclaredTypes = ASTNodeUtil.convertToTypedList(compilationUnit.types(),
				AbstractTypeDeclaration.class);

		List<String> toBeAdded = newImports.stream()
				.filter(newImport -> !isInSamePackage(newImport, packageQualifiedName, cuDeclaredTypes))
				.collect(Collectors.toList());
		super.addImports.addAll(toBeAdded);
		super.endVisit(cu);
	}
	
	/**
	 * Finds the {@link Type} of the new iterator object from the type of the
	 * iterable object. If the type is a wild card then gets its upper bound.
	 * Furthermore, it collects the names of the new import statements that are
	 * necessary to be added after introducing the iterator object.
	 * 
	 * @param iterableNode
	 *            node expected to represent a parameterized type object
	 * @return type binding of the iterator
	 */
	protected Type findIteratorType(ITypeBinding iterableTypeBinding) {
		Type iteratorType = null;
		ITypeBinding iteratorTypeBinding = null;
		if (iterableTypeBinding.isParameterizedType()) {

			ITypeBinding[] typeArguments = iterableTypeBinding.getTypeArguments();
			if (typeArguments.length == 1) {
				iteratorTypeBinding = typeArguments[0];
				if (iteratorTypeBinding != null && iteratorTypeBinding.getTypeBounds().length > 0) {
					iteratorTypeBinding = iteratorTypeBinding.getTypeBounds()[0];
				}
			}
		} else if (iterableTypeBinding.isArray()) {
			iteratorTypeBinding = iterableTypeBinding.getComponentType();
		}

		if (iteratorTypeBinding != null && !iteratorTypeBinding.getName().isEmpty()) {
			ASTRewrite astRewrite = getAstRewrite();
			ImportRewrite importRewrite = ImportRewrite.create(compilationUnit, true);
			iteratorType = importRewrite.addImport(iteratorTypeBinding, astRewrite.getAST());
			if(!iteratorTypeBinding.isMember()) {				
				String[] addedImports = importRewrite.getAddedImports();
				for (String addedImport : addedImports) {
					if (!addedImport.startsWith(JAVA_LANG_PACKAGE)) {
						newImports.add(addedImport);
					}
				}
			}
		}

		return iteratorType;
	}
	
	/**
	 * Generates a unique name for the iterator of the enhanced for loop, by
	 * adding a suffix to the given preferred name if there is another variable
	 * with the same name declared in the scope of the body of the loop. Uses
	 * the {@value #DEFAULT_ITERATOR_NAME} if the given name is null.
	 * 
	 * @param preferedName
	 *            a preferred name for the iterator
	 * @param loopBody
	 *            the body of the loop
	 * @return a new name for the iterator.
	 */
	protected Map<String, Boolean> generateNewIteratorName(SimpleName preferedName, Statement loopBody) {
		VariableDeclarationsVisitor loopBodyDeclarationsVisitor = new VariableDeclarationsVisitor();
		loopBody.accept(loopBodyDeclarationsVisitor);
		List<SimpleName> loobBodyDeclarations = loopBodyDeclarationsVisitor.getVariableDeclarationNames();
		List<String> declaredNames = loobBodyDeclarations.stream().filter(name -> name != preferedName)
				.map(SimpleName::getIdentifier).collect(Collectors.toList());

		String newName;
		Boolean allowedPreferedName;
		if (preferedName == null || declaredNames.contains(preferedName.getIdentifier())
				|| tempIntroducedNames.containsValue(preferedName)) {
			allowedPreferedName = false;
			int counter = 0;
			String suffix = ""; //$NON-NLS-1$
			ASTNode scope = findScopeOfLoop(loopBody);
			VariableDeclarationsVisitor loopScopeVisitor = new VariableDeclarationsVisitor();
			scope.accept(loopScopeVisitor);
			List<SimpleName> scopeDeclaredNames = loopScopeVisitor.getVariableDeclarationNames();

			declaredNames = scopeDeclaredNames.stream().map(SimpleName::getIdentifier).collect(Collectors.toList());
			while (declaredNames.contains(DEFAULT_ITERATOR_NAME + suffix)
					|| tempIntroducedNames.containsValue(DEFAULT_ITERATOR_NAME + suffix)) {
				counter++;
				suffix = Integer.toString(counter);
			}
			newName = DEFAULT_ITERATOR_NAME + suffix;
		} else {
			allowedPreferedName = true;
			newName = preferedName.getIdentifier();
		}

		Map<String, Boolean> nameMap = new HashMap<>();
		nameMap.put(newName, allowedPreferedName);

		return nameMap;
	}
	
	/**
	 * Finds the scope where the statement belongs to. A scope is either the
	 * body of:
	 * <ul>
	 * <li>a method</li>
	 * <li>an initializer</li>
	 * <li>a class/interface</li>
	 * <li>an enumeration</li>
	 * <li>an annotation declaration</li>
	 * </ul>
	 * 
	 * @param statement
	 *            a statement to look for the scope where it falls into.
	 * @return an {@link ASTNode} representing either of the above
	 */
	private ASTNode findScopeOfLoop(Statement statement) {
		ASTNode parent = statement.getParent();
		while (parent != null && parent.getNodeType() != ASTNode.METHOD_DECLARATION
				&& parent.getNodeType() != ASTNode.INITIALIZER && parent.getNodeType() != ASTNode.TYPE_DECLARATION
				&& parent.getNodeType() != ASTNode.ENUM_DECLARATION
				&& parent.getNodeType() != ASTNode.ANNOTATION_TYPE_DECLARATION) {

			parent = parent.getParent();
		}
		return parent;
	}
	
	protected void storeTempName(Statement node, String newIteratorIdentifier) {
		String key = generateTempIteratorKey(node);
		tempIntroducedNames.put(key, newIteratorIdentifier);

	}
	
	/**
	 * Checks whether the new import points to a class in the same package or in
	 * the same file as the compilation unit.
	 * 
	 * @param newImport
	 *            qualified name of the new import
	 * @param cuPackageQualifiedName
	 *            qualified name of the compilation unit's package
	 * @param cuDeclaredTypes
	 *            types declared in the compilation unit.
	 * @return true if the new import points to a type in the same package as
	 *         the compilation unit or to a type declared inside the compilation
	 *         unit.
	 */
	private boolean isInSamePackage(String newImport, String cuPackageQualifiedName,
			List<AbstractTypeDeclaration> cuDeclaredTypes) {
		boolean isInSamePackage = false;

		if (newImport.startsWith(cuPackageQualifiedName)) {
			int dotLastIndex = newImport.lastIndexOf(DOT);
			String suffix = newImport.substring(dotLastIndex);
			List<String> suffixComponents = Arrays.asList(suffix.split(DOT));
			if (suffixComponents.size() > 1) {
				isInSamePackage = cuDeclaredTypes.stream().map(type -> type.getName().getIdentifier())
						.filter(name -> name.equals(suffixComponents.get(0))).findAny().isPresent();
			} else {
				isInSamePackage = true;
			}
		}

		return isInSamePackage;
	}
	
	protected void replaceWithEnhancedFor(Statement loop, Statement loopBody, SimpleName iterableNode,
			LoopIteratingIndexASTVisitor indexVisitor, Type iteratorType) {
		/*
		 * invocations of List::get to be replaced with the iterator object
		 */
		List<ASTNode> toBeReplaced = indexVisitor.getIteratingObjectInitializers();
		List<ASTNode> toBeRemoved = indexVisitor.getNodesToBeRemoved();
		SimpleName preferredIteratorName = indexVisitor.getIteratorName();
		
		// generate a safe iterator name
		Map<String, Boolean> nameMap = generateNewIteratorName(preferredIteratorName, loopBody);
		String newIteratorIdentifier = nameMap.keySet().iterator().next();
		storeTempName(loop, newIteratorIdentifier);
		boolean eligiblePreferredName = nameMap.get(newIteratorIdentifier);
		if (eligiblePreferredName && indexVisitor.getPreferredNameFragment() != null) {
			toBeRemoved.add(indexVisitor.getPreferredNameFragment());
		}

		// remove the redundant nodes
		toBeRemoved.forEach(remove -> {
			if (remove.getLocationInParent() == VariableDeclarationStatement.FRAGMENTS_PROPERTY) {
				VariableDeclarationStatement declStatement = (VariableDeclarationStatement) remove.getParent();
				if (declStatement.fragments().size() == 1) {
					astRewrite.remove(declStatement, null);
				}
			}
			astRewrite.remove(remove, null);
		});

		AST ast = astRewrite.getAST();

		/*
		 * replace the List::get invocations with the new iterator
		 */
		toBeReplaced.forEach(target -> astRewrite.replace(target, ast.newSimpleName(newIteratorIdentifier), null));

		// create a declaration of the new iterator
		SingleVariableDeclaration iteratorDecl = NodeBuilder.newSingleVariableDeclaration(loopBody.getAST(),
				ast.newSimpleName(newIteratorIdentifier), iteratorType);

		// create the new enhanced for loop
		EnhancedForStatement newFor = NodeBuilder.newEnhancedForStatement(loopBody.getAST(),
				(Statement) astRewrite.createCopyTarget(loopBody),
				(Expression) astRewrite.createCopyTarget(iterableNode), iteratorDecl);

		// replace the existing for loop with
		astRewrite.replace(loop, newFor, null);
	}

	private String generateTempIteratorKey(Statement node) {
		return node.getStartPosition() + KEY_SEPARATOR + node.getLength();
	}
	
	protected void clearTempItroducedNames(Statement node) {
		this.tempIntroducedNames.remove(generateTempIteratorKey(node));
		
	}
	
}
