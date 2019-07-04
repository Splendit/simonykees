package eu.jsparrow.rules.common.visitor.helper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;

/**
 * This helper class is used to find generated {@link ASTNode}s (i.e., generated
 * by Lombok). {@link MethodDeclaration} and {@link FieldDeclaration} instances
 * that have the 'isGenerated' field added and set to true will be returned as a
 * list.
 * <p>
 * More information can be found in SIM-1578 and related tickets. 
 * 
 * @author Ludwig Werzowa
 * @since 3.7.0
 */
public class GeneratedNodeASTVisitor extends ASTVisitor {
	private List<ASTNode> nodesToIgnore;

	public GeneratedNodeASTVisitor() {
		nodesToIgnore = new ArrayList<>();
	}
	
	// TODO make this nice
	private boolean isGenerated(ASTNode node) {
		Field field = null;
		boolean retVal = false;
			try {
				field = node.getClass().getField("$isGenerated"); //$NON-NLS-1$
			} catch (NoSuchFieldException | SecurityException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		
		try {
			retVal = (boolean) field.getBoolean(node);
		} catch (IllegalAccessException | IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return retVal;
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		if (isGenerated(node)) {
			nodesToIgnore.add(node);
		}
		return false;
	}
	
	@Override
	public boolean visit(FieldDeclaration node) {
		if (isGenerated(node)) {
			nodesToIgnore.add(node);
		}
		return false;
	}

	public List<ASTNode> getNodesToIgnore() {
		return nodesToIgnore;
	}

}
