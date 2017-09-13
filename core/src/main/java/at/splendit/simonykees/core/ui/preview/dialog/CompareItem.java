package at.splendit.simonykees.core.ui.preview.dialog;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.compare.IModificationDate;
import org.eclipse.compare.IStreamContentAccessor;
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
public class CompareItem implements IStreamContentAccessor, ITypedElement, IModificationDate {
    private final String name;
    private final String contents;
    private final long time;

    public CompareItem(final String name,
                final String contents,
                final long time) {
        this.name = name;
        this.contents = contents;
        this.time = time;
    }

    @Override
    public InputStream getContents() throws CoreException {
        return new ByteArrayInputStream(contents.getBytes());
    }

    @Override
    public Image getImage() {
        return null;
    }

    @Override
    public long getModificationDate() {
        return time;
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
        return "java"; //$NON-NLS-1$
    }
}
