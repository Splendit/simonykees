package eu.jsparrow.ui.wizard.impl;

import java.util.ArrayList;
import java.util.Arrays;
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
import eu.jsparrow.ui.dialog.JSparrowPricingLink;
import eu.jsparrow.ui.util.LicenseUtil;

@SuppressWarnings("nls")
class RuleDescriptionStyledText extends StyledText {
	
	private static final String FULLSTOP = "."; //$NON-NLS-1$
	private static final String UPGRADE_YOUR_LICENSE = "upgrade your license";

	private String selectedRuleLink = ""; //$NON-NLS-1$

	private OffsetRange selectedRuleOffsetRange = OffsetRange.NONE;
	private OffsetRange upgradeLicenseOffsetRange = OffsetRange.NONE;

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
			if (offset != -1 && selectedRuleOffsetRange.start < offset && offset < selectedRuleOffsetRange.end) {
				Program.launch(selectedRuleLink);

			} else if (offset != -1 && upgradeLicenseOffsetRange.start < offset
					&& offset < upgradeLicenseOffsetRange.end) {
				Program.launch(JSparrowPricingLink.getJSparrowPricingPageAddress());
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
				? "This rule is included in the free license." + lineDelimiter
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

		Consumer<StyleRange> red = style -> style.foreground = getShell().getDisplay()
			.getSystemColor(SWT.COLOR_RED);
		Consumer<StyleRange> green = style -> style.foreground = getShell().getDisplay()
			.getSystemColor(SWT.COLOR_GREEN);

		selectedRuleLink = RuleDocumentationURLGeneratorUtil.generateLinkToDocumentation(rule.getId());

		List<StyleContainer> descriptionList = new ArrayList<>();
		descriptionList.add(new StyleContainer(name, h1));
		descriptionList.add(createLineDelimiter());

		StyleContainer upgradeLicenseStyleContainer = null;
		if (licenseUtil.isFreeLicense()) {
			upgradeLicenseStyleContainer = createBlueLink(UPGRADE_YOUR_LICENSE);
			List<StyleContainer> unlockSuggestions = getUnlockSuggestions(rule, upgradeLicenseStyleContainer);
			descriptionList.addAll(unlockSuggestions);
		}

		StyleContainer documentationStyleContainer = createBlueLink(documentationLabel);
		descriptionList.add(documentationStyleContainer);
		descriptionList.add(createLineDelimiter());
		descriptionList.add(createLineDelimiter());
		descriptionList.add(new StyleContainer(description));
		descriptionList.add(createLineDelimiter());
		descriptionList.add(createLineDelimiter());
		descriptionList.add(new StyleContainer(requirementsLabel, bold));
		descriptionList.add(createLineDelimiter());

		StyleContainer minJavaVarsionStyleContainer = new StyleContainer(minJavaVersionLabel, h2);
		descriptionList.add(minJavaVarsionStyleContainer);
		descriptionList.add(new StyleContainer(minJavaVersionValue, bold.andThen(red), !rule.isSatisfiedJavaVersion()));
		descriptionList.add(createLineDelimiter());
		descriptionList.add(new StyleContainer(requiredLibrariesLabel, h2));
		descriptionList
			.add(new StyleContainer(requiredLibrariesValue, bold.andThen(red), !rule.isSatisfiedLibraries()));
		descriptionList.add(createLineDelimiter());
		descriptionList.add(new StyleContainer(jSparrowStarterValue, bold.andThen(green)));
		descriptionList.add(createLineDelimiter());
		descriptionList.add(new StyleContainer(tagsLabel, bold));
		descriptionList.add(createLineDelimiter());
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
		if (upgradeLicenseStyleContainer != null) {
			upgradeLicenseOffsetRange = findOffsetRange(descriptionList, upgradeLicenseStyleContainer);
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

	private static StyleContainer createLineDelimiter() {
		return new StyleContainer(Messages.AbstractSelectRulesWizardPage_descriptionStyledText_lineDelimiter);
	}

	private static List<StyleContainer> getUnlockSuggestions(RefactoringRule rule,
			StyleContainer upgradeLicenseStyleContainer) {

		if (rule.isFree()) {
			return Arrays.asList(
					createLineDelimiter(),
					new StyleContainer(
							"As one of the 20 free rules of jSparrow trial, this rule is already unlocked."),
					createLineDelimiter(),
					createLineDelimiter(),
					new StyleContainer("To unlock all our premium rules, "),
					upgradeLicenseStyleContainer,
					new StyleContainer(FULLSTOP), // $NON-NLS-1$
					createLineDelimiter(),
					createLineDelimiter());

		}
		return Arrays.asList(
				createLineDelimiter(),
				new StyleContainer("This is a premium rule. To unlock it and all other premium rules, "),
				upgradeLicenseStyleContainer,
				new StyleContainer(FULLSTOP), // $NON-NLS-1$
				createLineDelimiter(),
				createLineDelimiter());

	}

	private StyleContainer createBlueLink(String text) {
		return new StyleContainer(text, style -> {
			style.underline = true;
			style.underlineStyle = SWT.UNDERLINE_LINK;
			style.foreground = getShell().getDisplay()
				.getSystemColor(SWT.COLOR_BLUE);
		});
	}

	private static OffsetRange findOffsetRange(List<StyleContainer> styleContainerList,
			StyleContainer expectedStyleContainer) {
		int offset = findOffsetBefore(styleContainerList, expectedStyleContainer);
		if (offset == -1) {
			return OffsetRange.NONE;
		}
		int end = offset + expectedStyleContainer.getValue()
			.length();
		return new OffsetRange(offset, end);
	}

	private static int findOffsetBefore(List<StyleContainer> styleContainerList,
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
