package pb;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.io.Closeables;
import com.google.common.io.Files;
import com.google.common.io.LineProcessor;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Create Excel doc with title mapping from json file
 * 
 * @author perbergman
 * 
 */
public class Titles {

	public Titles() {
	}

	public void run(String inFile, String badFile, final PrintWriter outFile,
			final List<String> filters) {

		try {
			Files.readLines(new File(inFile), Charsets.UTF_8,
					new LineProcessor<String>() {
						private int count = 1;

						@Override
						public String getResult() {
							return null;
						}

						@Override
						public boolean processLine(String jsonLine)
								throws IOException {
							JsonElement post = new JsonParser().parse(jsonLine);
							this.process(post.getAsJsonObject());
							return true;
						}

						// index id type slug url
						public void process(JsonObject obj) {

							String index = "" + count;
							String id = this.safeGet(obj, "id");

							String type = this.safeGet(obj, "type");
							String slug = this.safeGet(obj, "slug");
							String url = this.safeGet(obj, "post_url");

							String title = "?";
							if (type.equals("text")) {
								title = this.safeGet(obj, "title");
							}

							String csv = index + "," + id + "," + type + ","
									+ slug + "," + title + "," + url;

							System.out.println(csv);
							outFile.println(csv);
							count++;
						}

						private String safeGet(JsonObject obj, String prop) {
							if (obj != null && !Strings.isNullOrEmpty(prop)
									&& obj.has(prop)) {
								return obj.get(prop).getAsString();
							}
							return null;
						}
					});

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		Titles t = new Titles();
		PrintWriter outFile = null; // new StringWriter();
		boolean goOn = true;
		try {
			outFile = new PrintWriter(new OutputStreamWriter(
					new FileOutputStream(new File("out/titles.csv")),
					VeloAware.ENC));
		} catch (Exception e) {
			e.printStackTrace();
			goOn = false;
		}

		if (goOn) {
			List<String> filters = Lists.newArrayList();
			t.run("out/blog.txt", "out/bad.txt", outFile, filters);
		}
		Closeables.closeQuietly(outFile);
	}
}
