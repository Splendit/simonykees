package eu.jsparrow.ui.treeview;

/**
 * Implementations of this interface are used by method
 * {@link CheckBoxTreeLabelProvider#getText(Object)} to retrieve the text which
 * is returned.
 * 
 * @since 4.17.0
 *
 */
public interface ILabelTextProvider {

	/**
	 * 
	 * 
	 * @return the String to be returned by
	 *         {@link CheckBoxTreeLabelProvider#getText(Object)}.
	 */
	String getLabelText();

}
