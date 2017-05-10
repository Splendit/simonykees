package at.splendit.simonykees.core.rule;

import java.util.List;

import org.apache.commons.lang3.JavaVersion;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.splendit.simonykees.core.visitor.AbstractASTRewriteASTVisitor;
import at.splendit.simonykees.i18n.Messages;

/**
 * Wrapper Class for {@link AbstractASTRewriteASTVisitor} that holds UI name,
 * description, if its enabled and the document changes for
 * {@link ICompilationUnit} that are processed
 * 
 * @author Martin Huter, Hannes Schweighofer, Ludwig Werzowa
 * @since 0.9
 *
 * @param <T>
 *            is the {@link AbstractASTRewriteASTVisitor} implementation that is
 *            applied by this rule
 */
public abstract class RefactoringRule<T extends AbstractASTRewriteASTVisitor> {

	private static final Logger logger = LoggerFactory.getLogger(RefactoringRule.class);

	protected String id;

	protected String name = Messages.RefactoringRule_default_name;

	protected String description = Messages.RefactoringRule_default_description;
	
	protected final JavaVersion requiredJavaVersion;

	protected final List<Tag> tags;

	protected boolean enabled = true;

	private Class<T> visitor;

	public RefactoringRule(Class<T> visitor) {
		this.visitor = visitor;
		// TODO maybe add a better id
		this.id = this.getClass().getSimpleName();
		this.tags = Tag.getTagsForRule(this.getClass());
		this.requiredJavaVersion = provideRequiredJavaVersion();
	}
	
	/**
	 * the required java version of the implemented rule
	 * @return
	 */
	protected abstract JavaVersion provideRequiredJavaVersion();

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}
	
	public JavaVersion getRequiredJavaVersion() {
		return requiredJavaVersion;
	}
	
	public List<Tag> getTags() {
		return tags;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public Class<T> getVisitor() {
		return visitor;
	}

	public String getId() {
		return id;
	}

	/** Responsible to calculate of the rule is executable in the current project. 
	 * 
	 * @param project
	 */
	public void calculateEnabledForProject(IJavaProject project){
		String compilerCompliance = project.getOption(JavaCore.COMPILER_COMPLIANCE, true);
		if(null != compilerCompliance){
			String enumRepresentation = "JAVA_"+compilerCompliance.replace(".", "_"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			enabled = JavaVersion.valueOf(enumRepresentation).atLeast(requiredJavaVersion);
			if(enabled){
				enabled = ruleSpecificImplementation(project);
			}	
		}
	}
	
	/** JavaVersion independent requirements for rules that need to be defined for each rule.
	 * 	Returns true as default implementation
	 * 
	 * @param project
	 * @return
	 */
	public boolean ruleSpecificImplementation(IJavaProject project){
		return true;
	}
}
