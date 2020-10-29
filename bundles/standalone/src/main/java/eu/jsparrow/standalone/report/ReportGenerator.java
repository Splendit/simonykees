package eu.jsparrow.standalone.report;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import eu.jsparrow.standalone.report.model.ReportData;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

/**
 * Uses Apache FreeMarker to create a HTML report.
 * 
 * @since 3.23.0
 *
 */
public class ReportGenerator {

	private static final String RULES = "rules"; //$NON-NLS-1$
	private static final String DATE = "date"; //$NON-NLS-1$
	private static final String TOTAL_TIME_SAVED = "totalTimeSaved"; //$NON-NLS-1$
	private static final String TOTAL_ISSUES_FIXED = "totalIssuesFixed"; //$NON-NLS-1$
	private static final String TOTAL_FILES_CHANGED = "totalFilesChanged"; //$NON-NLS-1$
	private static final String TOTAL_FILES_COUNT = "totalFilesCount"; //$NON-NLS-1$
	private static final String PROJECT_NAME = "projectName"; //$NON-NLS-1$
	private static final String REPORT_FILE_NAME = "jSparrowReport.html"; //$NON-NLS-1$

	public void writeReport(ReportData reportData, String reportOutputFolder, File templateFolder) throws IOException {
		Map<String, Object> root = new HashMap<>();
		root.put(PROJECT_NAME, reportData.getProjectName());
		root.put(TOTAL_FILES_COUNT, reportData.getTotalFilesCount());
		root.put(TOTAL_FILES_CHANGED, reportData.getTotalFilesChanged());
		root.put(TOTAL_ISSUES_FIXED, reportData.getTotalIssuesFixed());
		root.put(TOTAL_TIME_SAVED, reportData.getTotalTimeSaved());
		root.put(DATE, reportData.getDate());
		root.put(RULES, reportData.getRuleDataModels());

		Configuration configuration = new Configuration(Configuration.VERSION_2_3_22);
		configuration.setDirectoryForTemplateLoading(templateFolder);
		configuration.setDefaultEncoding(StandardCharsets.UTF_8.name());
		configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		configuration.setLogTemplateExceptions(false);

		Template template = configuration.getTemplate(REPORT_FILE_NAME);
		String oututPath = String.join(File.separator, reportOutputFolder, REPORT_FILE_NAME);
		Writer out = new OutputStreamWriter(new FileOutputStream(oututPath));
		try {
			template.process(root, out);
		} catch (TemplateException | IOException e) {
			e.printStackTrace();
		}
	}
}
