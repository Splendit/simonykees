package eu.jsparrow.core.visitor.files;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;

public class NewBufferedReaderAnalyzer {
	
	private VariableDeclarationExpression newBufferedReader;
	private Expression initializer;
	
	public NewBufferedReaderAnalyzer(VariableDeclarationExpression newBufferedReader) {
		this.newBufferedReader = newBufferedReader;
	}
	
	private boolean isBufferedReaderInstanceCreation() {
		Type type = this.newBufferedReader.getType();
		ITypeBinding typeBinding = type.resolveBinding();
		return ClassRelationUtil.isContentOfType(typeBinding, java.io.BufferedReader.class.getName());
	}
	
	public boolean isInitializedWith(SimpleName fileReaderName) {
		if(!isBufferedReaderInstanceCreation()) {
			return false;
		}
		
		List<VariableDeclarationFragment> fragments = ASTNodeUtil.convertToTypedList(newBufferedReader.fragments(), VariableDeclarationFragment.class);
		if(fragments.size() != 1) {
			return false;
		}
		VariableDeclarationFragment fragment = fragments.get(0);
		Expression initializer = fragment.getInitializer();
		if(initializer == null)  {
			return false;
		}
		this.initializer = initializer;
		
		if(initializer.getNodeType() != ASTNode.CLASS_INSTANCE_CREATION) {
			return false;
		}
		ClassInstanceCreation instanceCreation = (ClassInstanceCreation)initializer;
		List<Expression> arguments = ASTNodeUtil.convertToTypedList(instanceCreation.arguments(), Expression.class);
		if(arguments.size() != 1) {
			return false;
		}
		
		Expression argument = arguments.get(0);
		if(argument.getNodeType() != ASTNode.SIMPLE_NAME) {
			return false;
		}
		
		SimpleName argumentName = (SimpleName)argument;
		ASTMatcher matcher = new ASTMatcher();
		return matcher.match(fileReaderName, argumentName);
	}

	public Expression getInitializer() {
		return initializer;
	}

}
