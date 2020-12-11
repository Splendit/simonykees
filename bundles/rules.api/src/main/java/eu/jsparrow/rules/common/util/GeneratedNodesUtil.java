package eu.jsparrow.rules.common.util;

import java.lang.reflect.Field;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.rules.common.visitor.helper.RemoveGeneratedNodesVisitor;

/**
 * This class is used to remove {@link ASTNode}s that contain the
 * {@code $isGenerated} field and have it set to true.
 * <p>
 * As soon as one node does not contain the {@code $isGenerated} field, no
 * further removal attempts will be performed.
 * <p>
 * Background: We need to remove those Lombok generated nodes because rewriting
 * them in any visitor will result in a MalformedTreeException (see SIM-1578).
 * 
 * @since 3.7.0
 */
public class GeneratedNodesUtil {

	private static final Logger logger = LoggerFactory.getLogger(GeneratedNodesUtil.class);

	private static boolean needsChecking = true;

	private GeneratedNodesUtil() {
		/*
		 * Hide the public constructor.
		 */
	}

	/**
	 * See {@link RemoveGeneratedNodesVisitor} for more details.
	 * 
	 * @param astRoot
	 *            the {@link CompilationUnit} where generated nodes should be
	 *            removed.
	 */
	public static void removeAllGeneratedNodes(CompilationUnit astRoot) {
		if (needsChecking) {
			RemoveGeneratedNodesVisitor visitor = new RemoveGeneratedNodesVisitor();
			astRoot.accept(visitor);

			if (!visitor.isHasIsGeneratedField()) {
				needsChecking = false;
			}
		}
	}

	/**
	 * Checks whether an object has a property with the given name.
	 * 
	 * @param node
	 *            the object to be checked
	 * @param propertyName
	 *            the property name to be checked
	 * @return if the object contains the given property.
	 */
	public static boolean hasProperty(ASTNode node, String propertyName) {
		boolean hasField = false;
		Class<? extends ASTNode> clazz = node.getClass();
		try {
			clazz.getField(propertyName);
			hasField = true;
		} catch (NoSuchFieldException | SecurityException e) {
			logger.trace("No {} field present.", propertyName); //$NON-NLS-1$
		}
		return hasField;
	}

	/**
	 * Finds the boolean value of the object property with the given name.
	 * 
	 * @param node
	 *            the object to be checked
	 * @param propertyName
	 *            the name of the boolean property to be checked.
	 * @return the boolean property value if the object contains a property with
	 *         the given name or {@code false} otherwise.
	 */
	public static boolean findPropertyValue(ASTNode node, String propertyName) {
		Field field = null;
		boolean retVal = false;
		try {
			field = node.getClass()
				.getField(propertyName);
			retVal = (boolean) field.getBoolean(node);
		} catch (NoSuchFieldException e) {
			logger.trace("No {} field present.", propertyName); //$NON-NLS-1$
		} catch (SecurityException | IllegalAccessException | IllegalArgumentException e) {
			logger.error("Unable to access node", e); //$NON-NLS-1$
		}
		return retVal;
	}

}
