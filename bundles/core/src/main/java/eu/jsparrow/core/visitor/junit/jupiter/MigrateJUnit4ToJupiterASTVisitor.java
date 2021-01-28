package eu.jsparrow.core.visitor.junit.jupiter;

import java.util.List;

import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.CompilationUnit;

import eu.jsparrow.rules.common.visitor.AbstractAddImportASTVisitor;

/**
 * 
 * @since 3.27.0
 *
 */
public class MigrateJUnit4ToJupiterASTVisitor extends AbstractAddImportASTVisitor {

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		boolean continueVisiting = super.visit(compilationUnit);

		if (!continueVisiting) {
			return false;
		}

		JUnit4AnnotationCollectorVisitor collectorVisitor = new JUnit4AnnotationCollectorVisitor();
		compilationUnit.accept(collectorVisitor);
		List<Annotation> jUnit4Annotations = collectorVisitor.getJUnit4Annotations();
		jUnit4Annotations.size();
		
		return true;
	}

}
