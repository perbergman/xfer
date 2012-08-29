package pb;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Properties;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

import com.google.common.base.Strings;
import com.google.common.io.Closeables;

public class VeloAware {
	protected static final String ENC = "UTF-8";

	protected VeloAware() {
		Properties props = new Properties();
		// props.setProperty("resource.loader", "classpath, file");
		props.setProperty("resource.loader", "classpath");
		props.setProperty("classpath.resource.loader.class",
				ClasspathResourceLoader.class.getName());
		props.setProperty("classpath.resource.loader.cache", "true");

		// props.setProperty("file.resource.loader.class",
		// FileResourceLoader.class.getName());
		// props.setProperty("file.resource.loader.path", propPath);
		// props.setProperty("file.resource.loader.cache", "true");

		Velocity.init(props);
	}

	protected void runVelo(String tpl, VelocityContext context, String fileName) {
		Writer w = new StringWriter();
		if (!Strings.isNullOrEmpty(fileName)) {
			try {
				w = new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream(fileName), ENC));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		Velocity.mergeTemplate(tpl, ENC, context, w);
		if (Strings.isNullOrEmpty(fileName)) {
			System.out.println(w);
		}
		Closeables.closeQuietly(w);
	}

}
