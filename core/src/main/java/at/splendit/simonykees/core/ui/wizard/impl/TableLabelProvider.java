package at.splendit.simonykees.core.ui.wizard.impl;

import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.visitor.AbstractASTRewriteASTVisitor;
import at.splendit.simonykees.i18n.ExceptionMessages;

/**
 * Label provider for right view in select rules wizard
 * 
 * @author Andreja Sambolec
 * @since 1.2
 *
 */
public class TableLabelProvider extends BaseLabelProvider implements ITableLabelProvider {

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		@SuppressWarnings("unchecked")
		RefactoringRule<? extends AbstractASTRewriteASTVisitor> rule = (RefactoringRule<? extends AbstractASTRewriteASTVisitor>) element;
		switch (columnIndex) {
		case 0:
			return rule.getName();
		case 1:
			return rule.getDescription();
		}
		return ExceptionMessages.TableLabelProvider_not_supported;
	}

}
