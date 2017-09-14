package at.splendit.simonykees.core.ui.preview.dialog;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.eclipse.compare.IEncodedStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Image;

/**
 * This class is used to create item for {@link CompareInput}
 * 
 * @author Andreja Sambolec
 * @since 2.1
 *
 */
public class CompareItem implements IEncodedStreamContentAccessor, ITypedElement {
	private static final String ENCODING = "UTF-8"; //$NON-NLS-1$

	private final String name;
	private final String contents;

	public CompareItem(final String name, final String contents) {
		this.name = name;
		this.contents = contents;
	}

	@Override
	public InputStream getContents() throws CoreException {
		try {
			return new ByteArrayInputStream(contents.getBytes(ENCODING));
		} catch (UnsupportedEncodingException e) {
			return new ByteArrayInputStream(contents.getBytes());
		}
	}

	@Override
	public String getCharset() {
		return ENCODING;
	}

	@Override
	public Image getImage() {
		return null;
	}

	@Override
	public String getName() {
		return name;
	}

	public String getString() {
		return contents;
	}

	@Override
	public String getType() {
		return "JAVA"; //$NON-NLS-1$
	}
}
