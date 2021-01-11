package eu.jsparrow.core.visitor.files;

import java.util.List;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.visitor.helper.LocalVariableUsagesVisitor;

/**
 * Analyzes whether the TWR-Header of given {@link TryStatement} contains a
 * {@link VariableDeclarationExpression} with exactly one
 * {@link VariableDeclarationFragment}, declaring a resource with the specified
 * name. Additionally, it is expected that the given resource is used exactly
 * once and therefore may be removed in connection with certain re-factoring
 * operations.
 * <p>
 * Code example meeting the requirements for a resource with the name "writer":
 * <ul>
 * <li>
 * {@code FileWriter writer = new FileWriter(new File("/home/test/test-path"), StandardCharsets.UTF_8)}
 * </li>
 * </ul>
 * <p>
 * Code examples meeting the requirements for a resource with the name
 * "bufferedReader":
 * <ul>
 * <li>
 * {@code BufferedReader bufferedReader = new BufferedReader(resourceInitializer)}
 * </li>
 * <li>
 * {@code BufferedReader bufferedReader = Files.newBufferedReader(aPath, aCharset)}
 * </li>
 * </ul>
 * 
 *
 */
public class TryResourceAnalyzer {
	private TryStatement tryStatement;
	private VariableDeclarationExpression resource;
	private VariableDeclarationFragment resourceFragment;
	private Expression resourceInitializer;

	/**
	 * 
	 * @return {@code true} if the TWR-header of a given {@link TryStatement}
	 *         contains a resource corresponding to the specified
	 *         {@link SimpleName} and meeting all requirements as described
	 *         above.
	 */
	boolean analyzeResourceUsedOnce(TryStatement tryStatement, SimpleName expectedResourceUsage) {
		if (analyze(tryStatement, expectedResourceUsage)) {
			LocalVariableUsagesVisitor visitor = new LocalVariableUsagesVisitor(
					expectedResourceUsage);
			tryStatement.accept(visitor);
			List<SimpleName> usagesList = visitor.getUsages();
			SimpleName nameAtDeclaration = resourceFragment.getName();
			for (SimpleName usage : usagesList) {
				if (usage != nameAtDeclaration && usage != expectedResourceUsage) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * 
	 * @return true if in a given {@link TryStatement} a resource can be found
	 *         which has the specified name, otherwise false;
	 */
	private boolean analyze(TryStatement tryStatement, SimpleName resourceNameExpected) {
		this.tryStatement = tryStatement;
		List<VariableDeclarationExpression> resources = ASTNodeUtil.convertToTypedList(tryStatement.resources(),
				VariableDeclarationExpression.class);
		for (VariableDeclarationExpression r : resources) {
			resource = r;
			List<VariableDeclarationFragment> resourceFragments = ASTNodeUtil.convertToTypedList(resource.fragments(),
					VariableDeclarationFragment.class);
			if (resourceFragments.size() == 1) {
				resourceFragment = resourceFragments.get(0);
				SimpleName resourceName = resourceFragment.getName();
				if (resourceName.getIdentifier()
					.equals(resourceNameExpected.getIdentifier())) {
					this.resourceInitializer = resourceFragment.getInitializer();
					if (this.resourceInitializer != null) {
						return true;
					}
				}
			}
		}
		return false;
	}

	TryStatement getTryStatement() {
		return tryStatement;
	}

	VariableDeclarationExpression getResource() {
		return resource;
	}

	VariableDeclarationFragment getResourceFragment() {
		return resourceFragment;
	}

	Expression getResourceInitializer() {
		return resourceInitializer;
	}

}
