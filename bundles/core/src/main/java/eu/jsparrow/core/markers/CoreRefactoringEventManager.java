package eu.jsparrow.core.markers;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;
import org.osgi.framework.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.core.refactorer.WorkingCopyOwnerDecorator;
import eu.jsparrow.rules.common.markers.RefactoringEventManager;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.util.JdtCoreVersionBindingUtil;
import eu.jsparrow.rules.common.util.RefactoringUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

/**
 * Provides functionalities for generating and resolving
 * {@link RefactoringMarkerEvent}.
 * 
 * @since 4.0.0
 *
 */
public class CoreRefactoringEventManager implements RefactoringEventManager {

	private static final Logger logger = LoggerFactory.getLogger(CoreRefactoringEventManager.class);

	@Override
	public void discoverRefactoringEvents(ICompilationUnit iCompilationUnit, List<String>markerIds) {
		CompilationUnit compilationUnit = RefactoringUtil.parse(iCompilationUnit);
		List<AbstractASTRewriteASTVisitor> resolvers = ResolverVisitorsFactory.getAllResolvers(markerIds, node -> true);
		for (AbstractASTRewriteASTVisitor resolver : resolvers) {
			final ASTRewrite astRewrite = ASTRewrite.create(compilationUnit.getAST());
			resolver.setASTRewrite(astRewrite);
			compilationUnit.accept(resolver);
		}
	}

	@Override
	public void resolve(ICompilationUnit iCompilationUnit, String resolverName, int offset) {
		Predicate<ASTNode> positionChecker = node -> {
			int startPosition = node.getStartPosition();
			int endPosition = startPosition + node.getLength();
			return startPosition <= offset && endPosition >= offset;
		};
		Function<Predicate<ASTNode>, AbstractASTRewriteASTVisitor> resolverGenerator = ResolverVisitorsFactory
			.getResolverGenerator(resolverName);
		AbstractASTRewriteASTVisitor resolver = resolverGenerator.apply(positionChecker);
		if (resolver == null) {
			return;
		}

		Version jdtVersion = JdtCoreVersionBindingUtil.findCurrentJDTCoreVersion();
		WorkingCopyOwnerDecorator workingCopyOwner = new WorkingCopyOwnerDecorator();
		ICompilationUnit workingCopy;
		try {
			workingCopy = iCompilationUnit.getWorkingCopy(workingCopyOwner, new NullProgressMonitor());
		} catch (JavaModelException e) {
			logger.error("Cannot create working copy for resolving jSparrow markers", e); //$NON-NLS-1$
			return;
		}

		CompilationUnit cu = RefactoringUtil.parse(workingCopy);
		resolve(jdtVersion, workingCopy, cu, resolver);
		try {
			workingCopy.commitWorkingCopy(false, new NullProgressMonitor());
			workingCopy.discardWorkingCopy();
			workingCopy.close();
		} catch (JavaModelException e) {
			logger.error("Cannot discard working copy after resolving jSparrow markers", e); //$NON-NLS-1$
		}
	}

	private CompilationUnit resolve(Version jdtVersion, ICompilationUnit workingCopy, CompilationUnit compilationUnit,
			AbstractASTRewriteASTVisitor resolver) {

		final ASTRewrite astRewrite = ASTRewrite.create(compilationUnit.getAST());
		resolver.setASTRewrite(astRewrite);
		compilationUnit.accept(resolver);

		try {
			Document document = new Document(workingCopy.getSource());
			TextEdit edits = astRewrite.rewriteAST(document, workingCopy.getJavaProject()
				.getOptions(true));
			if (edits.hasChildren()) {
				workingCopy.applyTextEdit(edits, new NullProgressMonitor());
				return workingCopy.reconcile(JdtCoreVersionBindingUtil.findJLSLevel(jdtVersion), true, null, null);
			}
		} catch (JavaModelException e) {
			logger.error("Cannot resolve jSparrow Marker", e); //$NON-NLS-1$
		}
		return compilationUnit;
	}

}
