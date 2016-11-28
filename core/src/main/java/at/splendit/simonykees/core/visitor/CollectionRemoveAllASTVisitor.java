package at.splendit.simonykees.core.visitor;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Type;

/**
 * An Collection that removes it from itself is replaced with clear
 * collectionName.removeAll(collectionName) -> collectionName.clear()
 * 
 * @author Martin Huter
 * @since 0.9.2
 *
 */
public class CollectionRemoveAllASTVisitor extends AbstractCompilationUnitAstVisitor {
	
	public CollectionRemoveAllASTVisitor() {
		super();
	}

	private static String ITERATOR = "java.util.Iterator"; //$NON-NLS-1$
	private SimpleName iterationVariable = null;

	@Override
	public boolean visit(MethodInvocation node){
		if(StringUtils.equals("removeAll", node.getName().getFullyQualifiedName()) //$NON-NLS-1$
					&& node.getExpression() instanceof SimpleName){
			
			@SuppressWarnings("unchecked")
			List<Type> arguments = (List<Type>) node.typeArguments();
			if(arguments.size() == 1){
				Type firstArgument = arguments.get(0);
				firstArgument.resolveBinding();
			}
		}
		return true;
	}
	
	}
