package eu.jsparrow.core.rule.impl;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.apache.commons.lang3.StringUtils;
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

	private static final String VERSION_3_0 = "3.0"; //$NON-NLS-1$
	private static final String VERSION_3_0_1 = "3.0.1"; //$NON-NLS-1$
	private static final String VERSION_3_1 = "3.1"; //$NON-NLS-1$
	private static final String VERSION_3_2 = "3.2"; //$NON-NLS-1$
	private static final String VERSION_3_2_1 = "3.2.1"; //$NON-NLS-1$
	private static final String VERSION_3_3 = "3.3"; //$NON-NLS-1$
	private static final String VERSION_3_3_1 = "3.3.1"; //$NON-NLS-1$
	private static final String VERSION_3_3_2 = "3.3.2"; //$NON-NLS-1$
	private static final String VERSION_3_4 = "3.4"; //$NON-NLS-1$
	private static final String VERSION_3_5 = "3.5"; //$NON-NLS-1$
	private static final String VERSION_3_6 = "3.6"; //$NON-NLS-1$
	private static final String VERSION_3_7 = "3.7"; //$NON-NLS-1$
	private static final String VERSION_3_8 = "3.8"; //$NON-NLS-1$
	private static final String VERSION_3_8_1 = "3.8.1"; //$NON-NLS-1$
	private static final String VERSION_3_9 = "3.9"; //$NON-NLS-1$
	private static final String VERSION_3_10 = "3.10"; //$NON-NLS-1$
	private static final String VERSION_3_11 = "3.11"; //$NON-NLS-1$

	Logger logger = LoggerFactory.getLogger(StringUtilsRule.class);

	private List<String> supportedVersion;

	public StringUtilsRule() {
		this.visitorClass = StringUtilsASTVisitor.class;
		this.supportedVersion = Arrays.asList(VERSION_3_0, VERSION_3_0_1, VERSION_3_1, VERSION_3_2, VERSION_3_2_1,
				VERSION_3_3, VERSION_3_3_1, VERSION_3_3_2, VERSION_3_4, VERSION_3_5, VERSION_3_6, VERSION_3_7, VERSION_3_8, 
				VERSION_3_8_1, VERSION_3_9, VERSION_3_10, VERSION_3_11);
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
			String fullyQuallifiedClassName = "org.apache.commons.lang3.StringUtils"; //$NON-NLS-1$
			IType classtype = project.findType(fullyQuallifiedClassName);
			if (classtype != null) {

				IPackageFragmentRoot commonsLangLib = getProject(classtype.getParent());
				if (commonsLangLib != null) {
					// file with path to library jar
					File file = new File(commonsLangLib.getPath()
						.toString());

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
		return "org.apache.commons.lang3.StringUtils"; //$NON-NLS-1$
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
		try (JarFile jar = new java.util.jar.JarFile(file)) {

			Manifest manifest = jar.getManifest();
			Attributes attributes = manifest.getMainAttributes();

			if (attributes != null) {
				for (Object attribute : attributes.keySet()) {
					Name key = (Name) attribute;
					String keyword = key.toString();
					if ("Implementation-Version".equals(keyword)) { //$NON-NLS-1$
						return supportedVersion.stream()
							.anyMatch(s -> StringUtils.startsWith(attributes.getValue(key), s));
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
