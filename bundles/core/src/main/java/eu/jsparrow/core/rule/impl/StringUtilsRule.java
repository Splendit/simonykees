package eu.jsparrow.core.rule.impl;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.osgi.util.NLS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.core.exception.runtime.ITypeNotFoundRuntimeException;
import eu.jsparrow.core.visitor.impl.StringUtilsASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see StringUtilsASTVisitor
 * 
 * @author Martin Huter
 * @since 0.9
 *
 */
public class StringUtilsRule extends RefactoringRuleImpl<StringUtilsASTVisitor> {

	private static final Logger logger = LoggerFactory.getLogger(StringUtilsRule.class);

	private static final String STRING_UTILS_QUALIFIED_NAME = org.apache.commons.lang3.StringUtils.class.getName();

	@SuppressWarnings("nls")
	private static final List<String> supportedVersion = Collections
		.unmodifiableList(Arrays.asList(
				"3.0",
				"3.0.1",
				"3.1",
				"3.2",
				"3.2.1",
				"3.3",
				"3.3.1",
				"3.3.2",
				"3.4",
				"3.5",
				"3.6",
				"3.7",
				"3.8",
				"3.8.1",
				"3.9",
				"3.10",
				"3.11"));

	public StringUtilsRule() {
		this.visitorClass = StringUtilsASTVisitor.class;
		this.id = "StringUtils"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(Messages.StringUtilsRule_name, Messages.StringUtilsRule_description,
				Duration.ofMinutes(10), Arrays.asList(Tag.JAVA_1_1, Tag.STRING_MANIPULATION));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_1;
	}

	@Override
	public boolean ruleSpecificImplementation(IJavaProject project) {
		try {
			String fullyQuallifiedClassName = STRING_UTILS_QUALIFIED_NAME;
			IType classtype = project.findType(fullyQuallifiedClassName);
			if (classtype != null) {

				IPackageFragmentRoot commonsLangLib = getProject(classtype.getParent());
				if (commonsLangLib != null) {
					// file with path to library jar
					IPath resourcePath = commonsLangLib.getPath();
					File file = new File(resourcePath.toString());

					return isImplementationVersionValid(file);
				}
			} else {
				String loggerDebug = NLS.bind(Messages.StringUtilsRule_classNotInClassPath, fullyQuallifiedClassName);
				logger.debug(loggerDebug);
			}
		} catch (JavaModelException e) {
			logger.error(e.getMessage(), new ITypeNotFoundRuntimeException());
		}
		return false;
	}

	@Override
	public String requiredLibraries() {
		return STRING_UTILS_QUALIFIED_NAME;
	}

	private IPackageFragmentRoot getProject(IJavaElement iJavaElement) {
		if (null == iJavaElement) {
			return null;
		}
		IJavaElement parent = iJavaElement.getParent();
		if (null == parent || parent instanceof IPackageFragmentRoot) {
			return (IPackageFragmentRoot) parent;
		}
		return getProject(parent);
	}

	private boolean isImplementationVersionValid(File file) {
		try (JarFile jar = new JarFile(file)) {

			Manifest manifest = jar.getManifest();
			Attributes attributes = manifest.getMainAttributes();

			if (attributes != null) {
				for (Object attribute : attributes.keySet()) {
					Name key = (Name) attribute;
					String keyword = key.toString();
					if ("Implementation-Version".equals(keyword)) { //$NON-NLS-1$
						return supportedVersion.stream()
							.anyMatch(s -> StringUtils.equals(attributes.getValue(key), s));
					}
				}
			}
		} catch (IOException e) {
			logger.debug("Jar Manifest load error in:", e); //$NON-NLS-1$
			// Resolving version failed, rule can't be executed
		}
		return false;
	}
}
