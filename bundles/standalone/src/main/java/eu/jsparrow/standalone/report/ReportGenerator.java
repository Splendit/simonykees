package eu.jsparrow.standalone.report;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

import eu.jsparrow.core.statistic.entity.JsparrowData;
import eu.jsparrow.standalone.Activator;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

public class ReportGenerator {
	
	private JsparrowData jSparrowdata;
	
	public ReportGenerator(JsparrowData data) {
		this.jSparrowdata = data;
	}
	
	public void writeReport(String path) throws IOException, URISyntaxException {
		Map<String, Object> root = new HashMap<>();
		root.put("project", jSparrowdata.getProjectName());
		
		Bundle bundle = Platform.getBundle(Activator.PLUGIN_ID);

		IPath iPathReport = new Path("report");
		URL url = FileLocator.find(bundle, iPathReport, new HashMap<>());
		URL templateDirecotryUrl = FileLocator.toFileURL(url);
		File file = new File(templateDirecotryUrl.toURI());
		
		Configuration configuration = new Configuration(Configuration.VERSION_2_3_22);
		configuration.setDirectoryForTemplateLoading(file);
		configuration.setDefaultEncoding(StandardCharsets.UTF_8.name());
		configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		configuration.setLogTemplateExceptions(false);
		
		Template template = configuration.getTemplate("myTemplate.html");
		
		Writer out = new OutputStreamWriter(new FileOutputStream(path));
		try {
			template.process(root, out);
		} catch (TemplateException | IOException e) {
			e.printStackTrace();
		}
	}

}
