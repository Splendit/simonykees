package eu.jsparrow.core.rule.impl;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.apache.commons.lang3.JavaVersion;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.core.exception.runtime.ITypeNotFoundRuntimeException;
import eu.jsparrow.core.rule.RefactoringRule;
import eu.jsparrow.core.rule.RuleDescription;
import eu.jsparrow.core.util.TagUtil;
import eu.jsparrow.core.visitor.impl.StringUtilsASTVisitor;
import eu.jsparrow.i18n.Messages;

/**
 * @see StringUtilsASTVisitor
 * 
 * @author Martin Huter
 * @since 0.9
 *
 */
public class StringUtilsRule extends RefactoringRule<StringUtilsASTVisitor> {

	private final String version31 = "3.1"; //$NON-NLS-1$

	Logger logger = LoggerFactory.getLogger(StringUtilsRule.class);

	private List<String> supportedVersion = new ArrayList<>();

	public StringUtilsRule() {
		super();
		this.visitorClass = StringUtilsASTVisitor.class;
		this.supportedVersion.add(version31);
		this.id = "StringUtils"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(Messages.StringUtilsRule_name, Messages.StringUtilsRule_description,
				Duration.ofMinutes(10), TagUtil.getTagsForRule(this.getClass()));
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_1;
	}

	@Override
	public boolean ruleSpecificImplementation(IJavaProject project) {
		try {
			String fullyQuallifiedClassName = "org.apache.commons.lang3.StringUtils"; //$NON-NLS-1$
			IType classtype = project.findType(fullyQuallifiedClassName);
			if (classtype != null) {

				IPackageFragmentRoot commonsLangLib = getProject(classtype.getParent());
				// file with path to libary jar
				File file = new File(commonsLangLib.getPath()
					.toString());

				try (JarFile jar = new java.util.jar.JarFile(file)) {

					Manifest manifest = jar.getManifest();
					Attributes attributes = manifest.getMainAttributes();

					if (attributes != null) {
						for (Object attribute : attributes.keySet()) {
							Name key = (Name) attribute;
							String keyword = key.toString();
							if ("Implementation-Version".equals(keyword)) { //$NON-NLS-1$
								if (supportedVersion.stream()
									.anyMatch(s -> StringUtils.startsWith(attributes.getValue(key), s))) {
									return true;
								} else {
									return false;
								}
							}
						}
					}
				} catch (IOException e) {
					logger.debug("Jar Manifest load error in:", e); //$NON-NLS-1$
					// Resolving version failed, rule cant be executed
				}
			} else {
				logger.debug(String.format("Class not in classpath [%s]", fullyQuallifiedClassName)); //$NON-NLS-1$
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
}
