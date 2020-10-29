package eu.jsparrow.standalone.report;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

import eu.jsparrow.core.statistic.entity.JsparrowData;
import eu.jsparrow.standalone.Activator;
import eu.jsparrow.standalone.report.model.RuleDataModel;
import eu.jsparrow.rules.common.RefactoringRule;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

/**
 * 
 * 
 *@since 3.23.0
 *
 */
public class ReportGenerator {
	
	private static final String RULES = "rules"; //$NON-NLS-1$
	private static final String DATE = "date"; //$NON-NLS-1$
	private static final String TOTAL_TIME_SAVED = "totalTimeSaved"; //$NON-NLS-1$
	private static final String TOTAL_ISSUES_FIXED = "totalIssuesFixed";  //$NON-NLS-1$
	private static final String TOTAL_FILES_CHANGED = "totalFilesChanged";  //$NON-NLS-1$
	private static final String TOTAL_FILES_COUNT = "totalFilesCount";  //$NON-NLS-1$
	private static final String PROJECT_NAME = "projectName";  //$NON-NLS-1$
	private static final String REPORT_FILE_NAME = "jSparrowReport.html"; //$NON-NLS-1$
	private JsparrowData jSparrowData;
	private Map<String, RefactoringRule> refactoringRules;
	
	public ReportGenerator(JsparrowData data, Map<String, RefactoringRule>refactoringRules) {
		this.jSparrowData = data;
		this.refactoringRules = refactoringRules;
	}
	
	public void writeReport(String path) throws IOException, URISyntaxException {
		Map<String, Object> root = new HashMap<>();
		root.put(PROJECT_NAME, jSparrowData.getProjectName());
		root.put(TOTAL_FILES_COUNT, jSparrowData.getTotalFilesCount());
		root.put(TOTAL_FILES_CHANGED, jSparrowData.getTotalFilesChanged());
		root.put(TOTAL_ISSUES_FIXED, jSparrowData.getTotalIssuesFixed());
		root.put(TOTAL_TIME_SAVED, jSparrowData.getTotalTimeSaved());
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy"); //$NON-NLS-1$
		String date = dateFormat.format(new Date());
		root.put(DATE, date);
		
		Bundle bundle = Platform.getBundle(Activator.PLUGIN_ID);
		
		List<RuleDataModel> rules = ReportDataUtil.mapToReportDataModel(refactoringRules, jSparrowData.getRules());
		root.put(RULES, rules);

		IPath iPathReport = new Path("report");
		URL url = FileLocator.find(bundle, iPathReport, new HashMap<>());
		URL templateDirecotryUrl = FileLocator.toFileURL(url);
		File file = new File(templateDirecotryUrl.toURI());
		
		Configuration configuration = new Configuration(Configuration.VERSION_2_3_22);
		configuration.setDirectoryForTemplateLoading(file);
		configuration.setDefaultEncoding(StandardCharsets.UTF_8.name());
		configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		configuration.setLogTemplateExceptions(false);
		
		Template template = configuration.getTemplate("jSparrowFindingsReport.html");
		
		Writer out = new OutputStreamWriter(new FileOutputStream(path));
		try {
			template.process(root, out);
		} catch (TemplateException | IOException e) {
			e.printStackTrace();
		}
	}

}
