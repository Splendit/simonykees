package eu.jsparrow.core.visitor.security;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

/**
 * This class will be deleted, see below at
 * {@link #isWithinAncestorBlocks(UserSuppliedInput, List)}.
 * 
 * @since 3.19.0
 *
 */
public class SQLQueryReplaceableParameterCollector extends UserSuppliedInputCollector {

	private static final int ONE_BASED_INDEX_OFFSET = 1;

	@SuppressWarnings("nls")
	private static final Map<String, String> SETTERS_MAP = Collections.unmodifiableMap(new HashMap<String, String>() {
		private static final long serialVersionUID = 572293158475249398L;
		{
			put(java.sql.Array.class.getName(), "setArray");
			put(java.math.BigDecimal.class.getName(), "setBigDecimal");
			put(java.sql.Blob.class.getName(), "setBlob");
			put(java.sql.Clob.class.getName(), "setClob");
			put(java.sql.Date.class.getName(), "setDate");
			put(java.lang.Object.class.getName(), "setObject");
			put(java.sql.Ref.class.getName(), "setRef");
			put(java.lang.String.class.getName(), "setString");
			put(java.sql.Time.class.getName(), "setTime");
			put(java.sql.Timestamp.class.getName(), "setTimestamp");
			put(java.net.URL.class.getName(), "setURL");

			put("bytes", "setBytes");

			put("boolean", "setBoolean");
			put(java.lang.Boolean.class.getName(), "setBoolean");
			put("byte", "setByte");
			put(java.lang.Byte.class.getName(), "setByte");
			put("double", "setDouble");
			put(java.lang.Double.class.getName(), "setDouble");
			put("float", "setFloat");
			put(java.lang.Float.class.getName(), "setFloat");
			put("int", "setInt");
			put(java.lang.Integer.class.getName(), "setInt");
			put("long", "setLong");
			put(java.lang.Long.class.getName(), "setLong");
			put("short", "setShort");
			put(java.lang.Short.class.getName(), "setShort");
		}
	});

	protected static final String SIMPLE_QUOTATION_MARK = "'"; //$NON-NLS-1$

	public SQLQueryReplaceableParameterCollector() {
		super(true, s -> s.endsWith(SIMPLE_QUOTATION_MARK), s -> s.startsWith(SIMPLE_QUOTATION_MARK));
	}

	private String computeSetterKey(ITypeBinding type) {
		if (type.isPrimitive()) {
			return type.getName();
		}

		if (type.isArray() && "byte".equals(type.getComponentType() //$NON-NLS-1$
			.getName())) {
			return "bytes"; //$NON-NLS-1$
		}
		return type.getErasure()
			.getQualifiedName();
	}

	private String findSetterName(Expression component) {
		ITypeBinding type = component.resolveTypeBinding();
		String key = computeSetterKey(type);
		return SETTERS_MAP.get(key);
	}

	protected ReplaceableParameter createReplaceableParameter(UserSuppliedInput userSuppliedInput,
			int parameterPosition) {

		Expression nonLiteralComponent = userSuppliedInput.getInput();
		String setterName = findSetterName(nonLiteralComponent);
		if (setterName == null) {
			return null;
		}
		return new ReplaceableParameter(userSuppliedInput.getPrevious(), userSuppliedInput.getNext(),
				nonLiteralComponent, setterName, parameterPosition);
	}

	private List<ReplaceableParameter> createReplaceableParameterList(List<UserSuppliedInput> userSuppliedInputList) {
		List<ReplaceableParameter> parameters = new ArrayList<>();
		int parameterPosition = getIndexOffset();
		for (UserSuppliedInput userSuppliedInput : userSuppliedInputList) {
			ReplaceableParameter parameter = createReplaceableParameter(userSuppliedInput, parameterPosition);
			if (parameter != null) {
				parameters.add(parameter);
				parameterPosition++;
			}
		}
		return parameters;
	}

	List<ReplaceableParameter> createReplaceableParameterList(SqlVariableAnalyzerVisitor sqlVariableVisitor) {
		List<Expression> queryComponents = sqlVariableVisitor.getDynamicQueryComponents();
		List<UserSuppliedInput> userSuppliedInputList = collectUserSuppliedInput(queryComponents);
		List<UserSuppliedInput> userSuppliedInputListForParameters = new ArrayList<>();

		List<Block> blockAncestorList = createBlockAncestorList(sqlVariableVisitor.getSimpleNameAtUsage(),
				sqlVariableVisitor.getVariableDeclarationFragment());

		if (blockAncestorList.isEmpty()) {
			return Collections.emptyList();
		}
		for (UserSuppliedInput userSuppliedInput : userSuppliedInputList) {
			if (isWithinAncestorBlocks(userSuppliedInput, blockAncestorList)) {
				userSuppliedInputListForParameters.add(userSuppliedInput);
			}
		}
		return createReplaceableParameterList(userSuppliedInputListForParameters);
	}

	/**
	 * Rejected the approach that user input could selectively excluded from
	 * transformation when one of {@link UserSuppliedInput#getPrevious()},
	 * {@link UserSuppliedInput#getNext()}, and
	 * {@link UserSuppliedInput#getInput()} are not created in statements
	 * visible to the execute method.
	 * <p>
	 * Therefore this class will be deleted.
	 * 
	 * @param userSuppliedInput
	 * @param blockAncestorList
	 * @return
	 */
	private boolean isWithinAncestorBlocks(UserSuppliedInput userSuppliedInput, List<Block> blockAncestorList) {
		if (!isWithinAncestorBlocks(userSuppliedInput.getPrevious(), blockAncestorList)) {
			return false;
		}
		if (!isWithinAncestorBlocks(userSuppliedInput.getInput(), blockAncestorList)) {
			return false;
		}

		if (userSuppliedInput.getNext() == null) {
			return true;
		}
		return isWithinAncestorBlocks(userSuppliedInput.getNext(), blockAncestorList);
	}

	// TODO: Complete this method!!
	/**
	 * 
	 * @param expression
	 * @param blockAncestorList
	 * @return true if the expression belongs to an expression statement or a
	 *         variable declaration statement the parent is a block which can be
	 *         found in the given block ancestor list.
	 */
	private boolean isWithinAncestorBlocks(Expression expression, List<Block> blockAncestorList) {
		return true;
	}

	private int getIndexOffset() {
		return ONE_BASED_INDEX_OFFSET;
	}

	Block getBlockSurroundingSimpleNameAtUsage(SimpleName simpleNameAtUsage) {

		if (simpleNameAtUsage.getLocationInParent() != MethodInvocation.ARGUMENTS_PROPERTY) {
			return null;
		}
		MethodInvocation invocation = (MethodInvocation) simpleNameAtUsage.getParent();

		Statement statement = null;
		if (invocation.getLocationInParent() == ExpressionStatement.EXPRESSION_PROPERTY) {
			statement = (ExpressionStatement) invocation.getParent();
		}

		if (invocation.getLocationInParent() == Assignment.RIGHT_HAND_SIDE_PROPERTY) {
			Assignment assignment = (Assignment) invocation.getParent();
			if (assignment.getLocationInParent() == ExpressionStatement.EXPRESSION_PROPERTY) {
				statement = (ExpressionStatement) assignment.getParent();
			}
		}

		if (invocation.getLocationInParent() == VariableDeclarationFragment.INITIALIZER_PROPERTY) {
			VariableDeclarationFragment fragment = (VariableDeclarationFragment) invocation.getParent();
			if (fragment.getLocationInParent() == VariableDeclarationStatement.FRAGMENTS_PROPERTY) {
				statement = (VariableDeclarationStatement) fragment.getParent();
			}
		}
		if (statement == null) {
			return null;
		}
		if (statement.getLocationInParent() != Block.STATEMENTS_PROPERTY) {
			return null;
		}
		return (Block) statement.getParent();

	}

	/**
	 * 
	 * @param variableDeclarationFragment
	 * @return if the given {@link VariableDeclarationFragment} is a fragment of
	 *         a {@link VariableDeclarationStatement} surrounded by a
	 *         {@link Block}, then the {@link Block} surrounding the
	 *         {@link VariableDeclarationFragment} is returned. Otherwise, null
	 *         is returned.
	 */
	Block getBlockOfLocalVariableDeclaration(VariableDeclarationFragment variableDeclarationFragment) {
		if (variableDeclarationFragment.getLocationInParent() != VariableDeclarationStatement.FRAGMENTS_PROPERTY) {
			return null;
		}
		VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) variableDeclarationFragment
			.getParent();

		if (variableDeclarationStatement.getLocationInParent() != Block.STATEMENTS_PROPERTY) {
			return null;
		}
		return (Block) variableDeclarationStatement.getParent();
	}

	/**
	 * 
	 * @return a list containing at least one {@link Block} if a valid path of
	 *         ancestor blocks can be found. Otherwise an empty list is
	 *         returned.
	 */
	List<Block> createBlockAncestorList(SimpleName simpleNameAtUsage,
			VariableDeclarationFragment variableDeclarationFragment) {

		Block blockOfLocalVariableDeclaration = getBlockOfLocalVariableDeclaration(variableDeclarationFragment);
		if (blockOfLocalVariableDeclaration == null) {
			return Collections.emptyList();
		}
		Block blockSurroundingSimpleNameAtUsage = getBlockSurroundingSimpleNameAtUsage(simpleNameAtUsage);
		if (blockSurroundingSimpleNameAtUsage == null) {
			return Collections.emptyList();
		}

		List<Block> ancestorsList = new ArrayList<>();
		ASTNode parentNode = blockSurroundingSimpleNameAtUsage;
		while (parentNode != null) {
			if (parentNode.getNodeType() == ASTNode.BLOCK) {
				ancestorsList.add((Block) parentNode);
			}
			if (parentNode == blockOfLocalVariableDeclaration) {
				break;
			}
			parentNode = parentNode.getParent();
		}
		return ancestorsList;
	}

}
