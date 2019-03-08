package eu.jsparrow.core.visitor.loop.bufferedreader;

import java.io.BufferedReader;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.WhileStatement;

/**
 * A helper visitor for finding and checking whether the  {@link BufferedReader}
 * and the line variable are used for any purpose other than iterating
 * through the lines of a file with a while loop.   
 * 
 * @since 3.3.0
 *
 */
public class BufferedReaderLinesPreconditionVisitor extends ASTVisitor {

	private WhileStatement whileStatement;
	private SimpleName line;
	private SimpleName bufferedReader;

	private boolean beforeLoop = true;
	private boolean inLoop = false;

	private boolean lineReferencesOutsideLoop;
	private boolean bufferReferencesBeforeLoop;

	private VariableDeclarationFragment lineDeclaration;
	private VariableDeclarationFragment bufferDeclaration;

	public BufferedReaderLinesPreconditionVisitor(WhileStatement whileStatement, SimpleName line,
			SimpleName bufferedReader) {
		this.whileStatement = whileStatement;
		this.line = line;
		this.bufferedReader = bufferedReader;
	}

	@Override
	public boolean visit(WhileStatement statement) {
		if (statement == whileStatement) {
			inLoop = true;
			beforeLoop = false;
		}
		return true;
	}

	@Override
	public void endVisit(WhileStatement statement) {
		if (statement == whileStatement) {
			inLoop = false;
		}
	}

	@Override
	public boolean visit(VariableDeclarationFragment fragment) {
		if (!beforeLoop) {
			return true;
		}
		SimpleName name = fragment.getName();
		String identifier = name.getIdentifier();
		if (identifier.equals(line.getIdentifier())) {
			lineDeclaration = fragment;
			lineReferencesOutsideLoop = false;
		}

		if (identifier.equals(bufferedReader.getIdentifier())) {
			bufferDeclaration = fragment;
			bufferReferencesBeforeLoop = false;

		}
		return true;
	}

	@Override
	public boolean visit(SimpleName simpleName) {
		if (inLoop) {
			return true;
		}

		String identifier = simpleName.getIdentifier();
		if (!identifier.equals(line.getIdentifier()) && !identifier.equals(bufferedReader.getIdentifier())) {
			return false;
		}

		StructuralPropertyDescriptor locationInParent = simpleName.getLocationInParent();
		if (locationInParent == VariableDeclarationFragment.NAME_PROPERTY
				|| locationInParent == FieldAccess.NAME_PROPERTY || locationInParent == QualifiedName.NAME_PROPERTY) {
			return false;
		}

		IBinding binding = simpleName.resolveBinding();
		if (binding == null || IBinding.VARIABLE != binding.getKind()) {
			return false;
		}

		if (beforeLoop && bufferDeclaration != null && identifier.equals(bufferedReader.getIdentifier())) {
			bufferReferencesBeforeLoop = true;
		}

		if (lineDeclaration != null && identifier.equals(line.getIdentifier())) {
			lineReferencesOutsideLoop = true;
		}

		return true;
	}

	public boolean isSatisfied() {
		return lineDeclaration != null && bufferDeclaration != null && !lineReferencesOutsideLoop
				&& !bufferReferencesBeforeLoop;
	}

	public VariableDeclarationFragment getLineDeclaration() {
		return this.lineDeclaration;
	}

}
