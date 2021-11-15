package eu.jsparrow.ui.preview;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.jdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.jdt.internal.ui.dialogs.StatusUtil;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.jsparrow.core.refactorer.RefactoringPipeline;
import eu.jsparrow.core.refactorer.StandaloneStatisticsData;
import eu.jsparrow.core.refactorer.StandaloneStatisticsMetadata;
import eu.jsparrow.core.statistic.entity.JsparrowMetric;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.ui.Activator;
import eu.jsparrow.ui.PartialMatchContentProposalProvider;
import eu.jsparrow.ui.dialog.SimonykeesMessageDialog;
import eu.jsparrow.ui.preview.comparator.SortableViewerComparator;
import eu.jsparrow.ui.preview.model.RefactoringPreviewWizardModel;
import eu.jsparrow.ui.preview.model.summary.AbstractSummaryWizardPageModel;
import eu.jsparrow.ui.preview.model.summary.FileViewerFilter;
import eu.jsparrow.ui.preview.statistics.StatisticsSection;

@SuppressWarnings({ "restriction" })
public abstract class AbstractSummaryWizardPage<T extends AbstractSummaryWizardPageModel> extends WizardPage {

	private static final Logger logger = LoggerFactory.getLogger(AbstractSummaryWizardPage.class);

	private static final int REPORT_PATH_LIMIT = 32;

	protected Composite rootComposite;

	private StatisticsSection statisticsArea;

	protected TableViewer fileTableViewer;
	protected TableViewer rulesPerFileTableViewer;
	protected Text searchText;

	protected T summaryWizardPageModel;

	protected int displayHeight;

	private boolean enabledFinishButton;
	private StandaloneStatisticsMetadata statisticsMetadata;
	private long endTime;

	protected AbstractSummaryWizardPage(RefactoringPipeline refactoringPipeline,
			RefactoringPreviewWizardModel wizardModel, boolean enabledFinishButton,
			StandaloneStatisticsMetadata statisticsMetadata, StatisticsSection statisticsArea) {
		this(refactoringPipeline, wizardModel, enabledFinishButton, statisticsArea);
		this.statisticsMetadata = statisticsMetadata;
		this.endTime = Instant.now()
			.getEpochSecond();
	}

	protected AbstractSummaryWizardPage(RefactoringPipeline refactoringPipeline,
			RefactoringPreviewWizardModel wizardModel, boolean enabledFinishButton, StatisticsSection statisticsArea) {
		super("wizardPage"); //$NON-NLS-1$
		ContextInjectionFactory.inject(this, Activator.getEclipseContext());

		setTitle(Messages.SummaryWizardPage_RunSummary);
		this.summaryWizardPageModel = summaryPageModelFactory(refactoringPipeline, wizardModel);
		this.enabledFinishButton = enabledFinishButton;
		displayHeight = Display.getCurrent()
			.getPrimaryMonitor()
			.getBounds().height;
		this.statisticsArea = statisticsArea;
	}

	protected abstract T summaryPageModelFactory(RefactoringPipeline pipeline,
			RefactoringPreviewWizardModel wizardModel);

	protected T getSummaryPageModel() {
		return summaryWizardPageModel;
	}

	/**
	 * Create contents of the wizard.
	 *
	 * @param parent
	 */
	@Override
	public void createControl(Composite parent) {
		rootComposite = new Composite(parent, SWT.NONE);
		setControl(rootComposite);
		rootComposite.setLayout(new GridLayout(1, false));
	}

	@Override
	public void performHelp() {
		SimonykeesMessageDialog.openDefaultHelpMessageDialog(getShell());
	}

	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			setStatusInfo();
			summaryWizardPageModel.updateData();
			saveStatisticsData();

			// We must wait to set selection until control is visible
			setInitialFileSelection();

		}
		super.setVisible(visible);
	}

	private void saveStatisticsData() {
		/*
		 * statistics data is only saved, when the java system property
		 * eu.jsparrow.statistics.save.path is set to a path. if it's empty or
		 * null, nothing will be saved.
		 */
		String path = System.getProperty("eu.jsparrow.statistics.save.path"); //$NON-NLS-1$
		if (path == null || path.isEmpty()) {
			return;
		}

		RefactoringPipeline refactoringPipeline = summaryWizardPageModel.getRefactoringPipeline();

		StandaloneStatisticsData statisticsData = new StandaloneStatisticsData(refactoringPipeline.getFileCount(),
				statisticsMetadata.getRepoName(), statisticsMetadata, refactoringPipeline);

		statisticsData.setMetricData();

		statisticsData.setEndTime(endTime);
		Optional<JsparrowMetric> metric = statisticsData.getMetricData();
		metric.ifPresent(m -> {
			try {
				ObjectMapper om = new ObjectMapper();
				final Path filePath = Paths.get(path,
						StringUtils.substring(statisticsMetadata.getRepoName(), 0, REPORT_PATH_LIMIT), Instant.now()
							.getEpochSecond() + ".json"); //$NON-NLS-1$

				File file = filePath.toFile();
				file.getParentFile()
					.mkdirs();
				om.writeValue(filePath.toFile(), m);
			} catch (IOException e) {
				logger.debug(e.getMessage(), e);
			}
		});
	}

	protected void addHeader() {
		this.statisticsArea.createView(rootComposite);
	}

	protected TableViewerColumn createSortableTableViewerColumn(TableViewer tableViewer, String title,
			String toolTipText,
			int colNumber, SortableViewerComparator comparator) {
		TableViewerColumn tableViewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn column = tableViewerColumn.getColumn();

		column.setResizable(false);
		column.setText(title);
		column.setToolTipText(toolTipText);
		column.addSelectionListener(
				getSelectionAdapterForRulesTableViewer(tableViewer, column, colNumber, comparator));

		return tableViewerColumn;
	}

	private SelectionAdapter getSelectionAdapterForRulesTableViewer(final TableViewer tableViewer,
			final TableColumn column, final int index, SortableViewerComparator comparator) {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				comparator.setColumn(index);
				int dir = comparator.getDirection();
				tableViewer.getTable()
					.setSortDirection(dir);
				tableViewer.getTable()
					.setSortColumn(column);
				tableViewer.refresh();
			}
		};
	}

	protected void addFilesSection() {
		Group filesGroup = new Group(rootComposite, SWT.SHADOW_ETCHED_IN);
		filesGroup.setLayout(new GridLayout(1, true));
		filesGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		filesGroup.setText(Messages.SummaryWizardPage_Files);

		Composite searchComposite = new Composite(filesGroup, SWT.NONE);
		searchComposite.setLayout(new GridLayout(1, false));
		GridData searchGroupGridData = new GridData(SWT.LEFT, SWT.FILL, false, false);
		searchGroupGridData.widthHint = 600;
		searchComposite.setLayoutData(searchGroupGridData);

		searchText = new Text(searchComposite, SWT.SEARCH | SWT.CANCEL | SWT.ICON_SEARCH);
		searchText.setMessage(Messages.AbstractSummaryWizardPage_searchLabel);
		searchText.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		searchText.setToolTipText(Messages.AbstractSummaryWizardPage_searchBoxToolTipText);

		// content for autocomplete proposal window with specified size
		IContentProposalProvider proposalProvider = new PartialMatchContentProposalProvider(
				summaryWizardPageModel.getProposalProviderContents());
		ContentProposalAdapter proposalAdapter = new ContentProposalAdapter(searchText, new TextContentAdapter(),
				proposalProvider, null, null);
		proposalAdapter.setPropagateKeys(true);
		proposalAdapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
		proposalAdapter.setPopupSize(new Point(580, 80));

		Composite filesSectionComposite = new Composite(filesGroup, SWT.NONE);
		filesSectionComposite.setLayout(new GridLayout(2, true));
		filesSectionComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		addFileTableViewerSection(filesSectionComposite);
		addRulePerFileSection(filesSectionComposite);

		FileViewerFilter filter = new FileViewerFilter();

		/*
		 * Used to handle the case when a suggestion is double clicked (and
		 * therefore inserted as text into the searchText). Without this, the
		 * selection will not change.
		 */
		searchText.addModifyListener((ModifyEvent e) -> updateSearch(searchText, filter));

		/*
		 * Handles hitting the delete search text button and clicking on the
		 * search icon
		 */
		searchText.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				if (e.detail == SWT.CANCEL) {
					searchText.setText(Messages.SelectRulesWizardPage_emptyString);
					updateSearch(searchText, filter);
				} else if (e.detail == SWT.ICON_SEARCH) {
					updateSearch(searchText, filter);
				}
			}
		});

		searchText.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				updateSearch(searchText, filter);
			}
		});
		fileTableViewer.addFilter(filter);
	}

	protected abstract void addRulePerFileSection(Composite filesSectionComposite);

	protected abstract void addFileTableViewerSection(Composite filesSectionComposite);

	private void updateSearch(Text searchText, FileViewerFilter filter) {
		filter.setSearchString(searchText.getText());
		fileTableViewer.refresh();
		rulesPerFileTableViewer.refresh();
		setInitialFileSelection();
	}

	protected void initializeDataBindings() {
		this.statisticsArea.initializeDataBindings();
		initializeFileTableViewer();
	}

	protected abstract void initializeFileTableViewer();

	private void setStatusInfo() {
		StatusInfo statusInfo = new StatusInfo();
		if (!enabledFinishButton) {
			statusInfo.setWarning(Messages.RefactoringSummaryWizardPage_warn_disableFinishWhenFree);
		}
		StatusUtil.applyToStatusLine(this, statusInfo);
	}

	private void setInitialFileSelection() {
		Object item = fileTableViewer.getElementAt(0);
		if (item != null) {
			fileTableViewer.setSelection(new StructuredSelection(item));
		} else {
			Table table = rulesPerFileTableViewer.getTable();
			table.clearAll();
		}
	}

}
