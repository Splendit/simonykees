package eu.jsparrow.core.visitor.files;

import java.util.List;
import java.util.Optional;

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

public class FileReaderAnalyzer {
	
	private VariableDeclarationExpression variableDeclaration;
	private SimpleName fileReaderName;
	private Expression charset;
	private Expression file;
	
	public FileReaderAnalyzer(VariableDeclarationExpression variableDeclaration) {
		this.variableDeclaration = variableDeclaration;
	}
	
	public boolean isFileReaderDeclaration() {
		Type type = variableDeclaration.getType();
		ITypeBinding typeBinding = type.resolveBinding();
		if(!ClassRelationUtil.isContentOfType(typeBinding, java.io.FileReader.class.getName())) {
			return false;
		}
		List<VariableDeclarationFragment> fragments = ASTNodeUtil.convertToTypedList(variableDeclaration.fragments(), VariableDeclarationFragment.class);
		if(fragments.size() != 1) {
			return false;
		}
		VariableDeclarationFragment fragment = fragments.get(0);
		this.fileReaderName = fragment.getName();
		
		Expression initialzier = fragment.getInitializer();
		if(initialzier == null || initialzier.getNodeType() != ASTNode.CLASS_INSTANCE_CREATION) {
			return false;
		}
		
		ClassInstanceCreation fileReaderCreation = (ClassInstanceCreation) initialzier;
		List<Expression> arguments = ASTNodeUtil.convertToTypedList(fileReaderCreation.arguments(), Expression.class);
		int argumentSize = arguments.size();
		if(argumentSize == 0 || argumentSize > 2) {
			return false;
		}
		Expression file = arguments.get(0);
		if(!isFileInstanceCreation(file)) {
			return false;
		}
		this.file = file;
		//TODO: get the path of the file.
		if(argumentSize == 2) {
			Expression charset = arguments.get(1);
			ITypeBinding charsetBinding = charset.resolveTypeBinding();
			if(ClassRelationUtil.isContentOfType(charsetBinding, java.nio.charset.Charset.class.getName())) {
				this.charset = charset;
			} else {
				return false;
			}
			
			
		}
		
		return true;
	}
	
	private boolean isFileInstanceCreation(Expression file) {
		//TODO: implement this method. 
		return false;
	}

	public Optional<Expression> getCharset() {
		return Optional.ofNullable(charset);
	}
	
	public Expression getFileInstanceCreation() {
		return this.file;
	}
	
	public SimpleName getFileReaderName() {
		return this.fileReaderName;
	}

	public Expression computePathExpression() {
		// TODO Auto-generated method stub
		return null;
	}

	public Optional<Expression> computeCharsetExpression() {
		// TODO Auto-generated method stub
		return null;
	}

}
