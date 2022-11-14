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
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import eu.jsparrow.core.statistic.RuleDocumentationURLGeneratorUtil;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.rules.common.Tag;
import eu.jsparrow.ui.startup.registration.RegistrationDialog;
import eu.jsparrow.ui.util.LicenseUtil;

class RuleDescriptionStyledText extends StyledText {
	private final AbstractSelectRulesWizardPage selectRulesWizardPage;
	private static final String UPGRADE_LICENSE_LINK = "https://jsparrow.io/pricing/";
	private static final String TO_UNLOCK_RULES = "To unlock this and many other rules, ";
	private static final String UPGRADE_YOUR_LICENSE = "upgrade your license";
	private static final String BENEFIT_FROM_ALL_ADVANTAGES = " now and benefit from all advantages of jSparrow.";

	private String selectedRuleLink = ""; //$NON-NLS-1$

	private OffsetRange selectedRuleOffsetRange = OffsetRange.NONE;
	private OffsetRange registerForFreeOffsetRange = OffsetRange.NONE;
	private OffsetRange upgradeLicenseOffsetRange = OffsetRange.NONE;

	RuleDescriptionStyledText(Composite parent, AbstractSelectRulesWizardPage selectRulesWizardPage) {
		super(parent, SWT.WRAP | SWT.V_SCROLL | SWT.BORDER);
		this.selectRulesWizardPage = selectRulesWizardPage;
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
			if (offset != -1 && selectedRuleOffsetRange.start < offset && offset < selectedRuleOffsetRange.end) {
				Program.launch(selectedRuleLink);

			} else if (offset != -1 && registerForFreeOffsetRange.start < offset
					&& offset < registerForFreeOffsetRange.end) {
				// setCapture(false);

				Shell activeShell = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow()
					.getShell();
				RegistrationDialog registrationDialog = new RegistrationDialog(activeShell,
						selectRulesWizardPage::afterLicenseUpdate);

				registrationDialog.open();
				// this.getShell().getChildren() [0].setFocus();

			} else if (offset != -1 && upgradeLicenseOffsetRange.start < offset
					&& offset < upgradeLicenseOffsetRange.end) {
				Program.launch(UPGRADE_LICENSE_LINK);
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

		selectedRuleLink = RuleDocumentationURLGeneratorUtil.generateLinkToDocumentation(rule.getId());
		Consumer<StyleRange> documentationConfig = style -> {
			style.underline = true;
			style.underlineStyle = SWT.UNDERLINE_LINK;
			style.data = selectedRuleLink;
		};

		Consumer<StyleRange> registerForFreeConfig = style -> {
			style.underline = true;
			style.underlineStyle = SWT.UNDERLINE_LINK;
			style.data = ""; //$NON-NLS-1$
		};

		Consumer<StyleRange> updateLicenseConfig = style -> {
			style.underline = true;
			style.underlineStyle = SWT.UNDERLINE_LINK;
			style.data = UPGRADE_LICENSE_LINK;
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

		StyleContainer registerForFreeStyleContainer = null;
		StyleContainer upgradeLicenseStyleContainer = null;
		if (unlockRulesSuggestion) {
			if (rule.isFree() && !licenseUtil.isActiveRegistration()) {
				descriptionList.add(new StyleContainer(lineDelimiter));
				descriptionList.add(new StyleContainer("This Rule is free. To unlock it, "));
				registerForFreeStyleContainer = new StyleContainer("register for a free jSparrow trial ",
						blue.andThen(registerForFreeConfig));
				descriptionList.add(registerForFreeStyleContainer);
				descriptionList.add(new StyleContainer(" and you will be able to apply 20 of our most liked rules."));
				descriptionList.add(new StyleContainer(lineDelimiter));
			}
			upgradeLicenseStyleContainer = new StyleContainer(UPGRADE_YOUR_LICENSE, blue.andThen(updateLicenseConfig));
			descriptionList.add(new StyleContainer(lineDelimiter));
			descriptionList.add(new StyleContainer(TO_UNLOCK_RULES));
			descriptionList.add(upgradeLicenseStyleContainer);
			descriptionList.add(new StyleContainer(BENEFIT_FROM_ALL_ADVANTAGES));
			descriptionList.add(new StyleContainer(lineDelimiter));
			descriptionList.add(new StyleContainer(lineDelimiter));
		}

		StyleContainer documentationStyleContainer = new StyleContainer(documentationLabel,
				blue.andThen(documentationConfig));
		descriptionList.add(documentationStyleContainer);
		descriptionList.add(new StyleContainer(lineDelimiter));
		descriptionList.add(new StyleContainer(lineDelimiter));
		descriptionList.add(new StyleContainer(description));
		descriptionList.add(new StyleContainer(lineDelimiter));
		descriptionList.add(new StyleContainer(lineDelimiter));
		descriptionList.add(new StyleContainer(requirementsLabel, bold));
		descriptionList.add(new StyleContainer(lineDelimiter));

		StyleContainer minJavaVarsionStyleContainer = new StyleContainer(minJavaVersionLabel, h2);
		descriptionList.add(minJavaVarsionStyleContainer);
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
		for (StyleContainer iterator : descriptionList) {
			if (!lineDelimiter.equals(iterator.getValue()) && iterator.isEnabled()) {
				setStyleRange(iterator.generateStyle(offset));
			}
			offset += iterator.getValue()
				.length();
		}

		selectedRuleOffsetRange = findOffsetRange(descriptionList, documentationStyleContainer);
		upgradeLicenseOffsetRange = OffsetRange.NONE;
		registerForFreeOffsetRange = OffsetRange.NONE;
		if (upgradeLicenseStyleContainer != null) {
			upgradeLicenseOffsetRange = findOffsetRange(descriptionList, upgradeLicenseStyleContainer);
		}
		if (registerForFreeStyleContainer != null) {
			registerForFreeOffsetRange = findOffsetRange(descriptionList, registerForFreeStyleContainer);
		}

		int requirementsBulletingStartLine = getLineAtOffset(
				findOffsetBefore(descriptionList, minJavaVarsionStyleContainer));

		StyleRange bulletPointStyle = new StyleRange();
		bulletPointStyle.metrics = new GlyphMetrics(0, 0, 40);
		bulletPointStyle.foreground = getShell().getDisplay()
			.getSystemColor(SWT.COLOR_BLACK);
		Bullet bulletPoint = new Bullet(bulletPointStyle);

		setLineBullet(requirementsBulletingStartLine, jSparrowStarterValue.isEmpty() ? 2 : 3,
				bulletPoint);
	}

	private OffsetRange findOffsetRange(List<StyleContainer> styleContainerList,
			StyleContainer expectedStyleContainer) {
		int offset = findOffsetBefore(styleContainerList, expectedStyleContainer);
		if (offset == -1) {
			return OffsetRange.NONE;
		}
		int end = offset + expectedStyleContainer.getValue()
			.length();
		return new OffsetRange(offset, end);
	}

	private int findOffsetBefore(List<StyleContainer> styleContainerList,
			StyleContainer expectedStyleContainer) {
		int offset = 0;
		for (StyleContainer styleContainer : styleContainerList) {
			if (styleContainer == expectedStyleContainer) {
				return offset;
			}
			offset += styleContainer.getValue()
				.length();
		}
		return -1;
	}

	static class OffsetRange {

		static final OffsetRange NONE = new OffsetRange();

		OffsetRange() {
			this(-1, -1);
		}

		OffsetRange(int start, int end) {
			this.start = start;
			this.end = end;
		}

		private int start;
		private int end;

	}
}
