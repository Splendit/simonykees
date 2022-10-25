package eu.jsparrow.ui.wizard.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.Bullet;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GlyphMetrics;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import eu.jsparrow.core.statistic.RuleDocumentationURLGeneratorUtil;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.rules.common.Tag;
import eu.jsparrow.ui.util.LicenseUtil;

class RuleDescriptionStyledText extends StyledText {

	private static final String BENEFIT_FROM_ALL_ADVANTAGES = " now and benefit from all advantages of jSparrow.";
	private static final String UPGRADE_YOUR_LICENSE = "upgrade your license";
	private static final String TO_UNLOCK_RULES = "To unlock this and many other rules, ";

	RuleDescriptionStyledText(Composite parent) {
		super(parent, SWT.WRAP | SWT.V_SCROLL | SWT.BORDER);
	}

	/**
	 * Creates bottom part of select wizard containing Text field with
	 * description of selected rule if only one rule is selected, default
	 * description otherwise.
	 * 
	 * @param parent
	 */
	void createDescriptionViewer() {
		/*
		 * There is a known issue with automatically showing and hiding
		 * scrollbars and SWT.WRAP. Using StyledText and
		 * setAlwaysShowScrollBars(false) makes the vertical scroll work
		 * correctly at least.
		 */
		this.setAlwaysShowScrollBars(false);
		this.setEditable(false);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.minimumHeight = 110;
		this.setLayoutData(gridData);
		this.setMargins(2, 2, 2, 2);
		this.addListener(SWT.MouseDown, event -> {
			int offset;
			try {
				offset = getOffsetAtPoint(new Point(event.x, event.y));
			} catch (SWTException | IllegalArgumentException e) {
				offset = -1;
			}
			if (offset != -1 && SelectedRule.start < offset && offset < SelectedRule.end) {
				Program.launch(SelectedRule.link);
			} else if (offset != -1 && UpgradeLicense.start < offset && offset < UpgradeLicense.end) {
				Program.launch(UpgradeLicense.LINK);
			}
		});
	}

	/**
	 * Creating description for rule to be displayed using StyledText
	 * 
	 * @param rule
	 */
	void createTextForDescription(RefactoringRule rule) {

		final String lineDelimiter = Messages.AbstractSelectRulesWizardPage_descriptionStyledText_lineDelimiter;
		final String requirementsLabel = Messages.AbstractSelectRulesWizardPage_descriptionStyledText_requirementsLabel;
		final String minJavaVersionLabel = Messages.AbstractSelectRulesWizardPage_descriptionStyledText_minJavaVersionLabel;
		final String requiredLibrariesLabel = Messages.AbstractSelectRulesWizardPage_descriptionStyledText_librariesLabel;
		final String tagsLabel = Messages.AbstractSelectRulesWizardPage_descriptionStyledText_tagsLabel;
		final String documentationLabel = Messages.AbstractSelectRulesWizardPage_seeDocumentation;

		LicenseUtil licenseUtil = LicenseUtil.get();

		String name = rule.getRuleDescription()
			.getName();
		String description = rule.getRuleDescription()
			.getDescription();
		String minJavaVersionValue = rule.getRequiredJavaVersion();
		String requiredLibrariesValue = (null != rule.requiredLibraries()) ? rule.requiredLibraries()
				: Messages.AbstractSelectRulesWizardPage_descriptionStyledText_librariesNoneLabel;
		String jSparrowStarterValue = (rule.isFree() && licenseUtil.isFreeLicense())
				? Messages.AbstractSelectRulesWizardPage_freemiumRegirementsMessage + lineDelimiter
				: ""; //$NON-NLS-1$
		String tagsValue = StringUtils.join(rule.getRuleDescription()
			.getTags()
			.stream()
			.map(Tag::getTagNames)
			.collect(Collectors.toList()), "  "); //$NON-NLS-1$

		FontData data = getFont()
			.getFontData()[0];
		Shell shell = getShell();
		Display display = shell.getDisplay();
		Consumer<StyleRange> h1 = style -> {
			style.font = new Font(display, data.getName(), data.getHeight() * 3 / 2, data.getStyle());
			shell.addDisposeListener(e -> style.font.dispose());
		};
		Consumer<StyleRange> h2 = style -> {
			style.font = new Font(display, data.getName(), data.getHeight(), data.getStyle());
			shell.addDisposeListener(e -> style.font.dispose());
		};
		Consumer<StyleRange> bold = style -> {
			style.font = new Font(display, data.getName(), data.getHeight(), SWT.BOLD);
			shell.addDisposeListener(e -> style.font.dispose());
		};

		Consumer<StyleRange> blue = style -> style.foreground = getShell().getDisplay()
			.getSystemColor(SWT.COLOR_BLUE);
		Consumer<StyleRange> red = style -> style.foreground = getShell().getDisplay()
			.getSystemColor(SWT.COLOR_RED);
		Consumer<StyleRange> green = style -> style.foreground = getShell().getDisplay()
			.getSystemColor(SWT.COLOR_GREEN);

		SelectedRule.link = RuleDocumentationURLGeneratorUtil.generateLinkToDocumentation(rule.getId());
		Consumer<StyleRange> documentationConfig = style -> {
			style.underline = true;
			style.underlineStyle = SWT.UNDERLINE_LINK;
			style.data = SelectedRule.link;
		};

		Consumer<StyleRange> updateLicenseConfig = style -> {
			style.underline = true;
			style.underlineStyle = SWT.UNDERLINE_LINK;
			style.data = UpgradeLicense.LINK;
		};

		boolean freeLicense = licenseUtil.isFreeLicense();
		boolean unlockRulesSuggestion;
		if (freeLicense) {
			unlockRulesSuggestion = !rule.isFree() || !licenseUtil.isActiveRegistration();
		} else {
			unlockRulesSuggestion = false;
		}

		List<StyleContainer> descriptionList = new ArrayList<>();
		descriptionList.add(new StyleContainer(name, h1));
		descriptionList.add(new StyleContainer(lineDelimiter));

		if (unlockRulesSuggestion) {

			descriptionList.add(new StyleContainer(lineDelimiter));
			descriptionList.add(new StyleContainer(TO_UNLOCK_RULES));
			descriptionList.add(new StyleContainer(UPGRADE_YOUR_LICENSE, blue.andThen(updateLicenseConfig)));
			descriptionList.add(new StyleContainer(BENEFIT_FROM_ALL_ADVANTAGES));
			descriptionList.add(new StyleContainer(lineDelimiter));
			descriptionList.add(new StyleContainer(lineDelimiter));

		}

		descriptionList.add(new StyleContainer(documentationLabel, blue.andThen(documentationConfig)));
		descriptionList.add(new StyleContainer(lineDelimiter));
		descriptionList.add(new StyleContainer(lineDelimiter));
		descriptionList.add(new StyleContainer(description));
		descriptionList.add(new StyleContainer(lineDelimiter));
		descriptionList.add(new StyleContainer(lineDelimiter));
		descriptionList.add(new StyleContainer(requirementsLabel, bold));
		descriptionList.add(new StyleContainer(lineDelimiter));

		descriptionList.add(new StyleContainer(minJavaVersionLabel, h2));
		descriptionList.add(new StyleContainer(minJavaVersionValue, bold.andThen(red), !rule.isSatisfiedJavaVersion()));
		descriptionList.add(new StyleContainer(lineDelimiter));
		descriptionList.add(new StyleContainer(requiredLibrariesLabel, h2));
		descriptionList
			.add(new StyleContainer(requiredLibrariesValue, bold.andThen(red), !rule.isSatisfiedLibraries()));
		descriptionList.add(new StyleContainer(lineDelimiter));
		descriptionList.add(new StyleContainer(jSparrowStarterValue, bold.andThen(green)));
		descriptionList.add(new StyleContainer(lineDelimiter));
		descriptionList.add(new StyleContainer(tagsLabel, bold));
		descriptionList.add(new StyleContainer(lineDelimiter));
		descriptionList.add(new StyleContainer(tagsValue));

		String descriptionText = descriptionList.stream()
			.map(StyleContainer::getValue)
			.collect(Collectors.joining());

		setText(descriptionText);

		int offset = 0;
		UpgradeLicense.start = -1;
		UpgradeLicense.end = -1;
		for (StyleContainer iterator : descriptionList) {
			if (!lineDelimiter.equals(iterator.getValue()) && iterator.isEnabled()) {
				setStyleRange(iterator.generateStyle(offset));
				if (documentationLabel.equals(iterator.getValue())) {
					SelectedRule.start = offset;
					SelectedRule.end = offset + iterator.getValue()
						.length();
				}
				if (unlockRulesSuggestion && UPGRADE_YOUR_LICENSE.equals(iterator.getValue())) {
					UpgradeLicense.start = offset;
					UpgradeLicense.end = offset + iterator.getValue()
						.length();
				}
			}
			offset += iterator.getValue()
				.length();
		}

		int requirementsBulletingStartLine = getLineAtOffset(
				name.length() +
						lineDelimiter.length() +
						(unlockRulesSuggestion
								? lineDelimiter.length() +
										TO_UNLOCK_RULES.length() +
										UPGRADE_YOUR_LICENSE.length() +
										BENEFIT_FROM_ALL_ADVANTAGES.length() +
										lineDelimiter.length() +
										lineDelimiter.length()
								: 0)
						+
						documentationLabel.length() +
						2 * lineDelimiter.length() +
						description.length() +
						2 * lineDelimiter.length() +
						requirementsLabel.length() +
						lineDelimiter.length());

		StyleRange bulletPointStyle = new StyleRange();
		bulletPointStyle.metrics = new GlyphMetrics(0, 0, 40);
		bulletPointStyle.foreground = getShell().getDisplay()
			.getSystemColor(SWT.COLOR_BLACK);
		Bullet bulletPoint = new Bullet(bulletPointStyle);

		setLineBullet(requirementsBulletingStartLine, jSparrowStarterValue.isEmpty() ? 2 : 3,
				bulletPoint);
	}

	private static class SelectedRule {
		private SelectedRule() {

		}

		static int start = 0;
		static int end = 0;
		static String link = ""; //$NON-NLS-1$
	}

	private static class UpgradeLicense {
		private UpgradeLicense() {

		}

		static int start = 0;
		static int end = 0;
		static final String LINK = "https://jsparrow.io/pricing/"; //$NON-NLS-1$
	}
}
