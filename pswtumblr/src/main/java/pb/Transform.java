package pb;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.velocity.VelocityContext;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.common.io.LineProcessor;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Read JSON and emit PHP
 * 
 * @author perbergman
 * 
 */
public class Transform extends VeloAware {

	public Transform() {
		super("");
	}

	public void run(String inFile, final String outDir) {

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

						// $slug $id $media $caption $tags $date
						public void process(JsonObject obj) {
							Map<String, String> ctx = Maps.newHashMap();
							String key = "id";
							ctx.put(key, obj.get(key).getAsString()); // Long
							key = "slug";
							ctx.put(key, obj.get(key).getAsString());
							key = "type";
							ctx.put(key, obj.get(key).getAsString());
							key = "date";
							ctx.put(key, obj.get(key).getAsString());
							key = "caption";
							ctx.put(key, obj.get(key).getAsString());

							JsonArray tags = obj.get("tags").getAsJsonArray();
							key = "tags";
							ctx.put(key,
									Joiner.on(",").join(JsonUtils.toList(tags)));

							key = "media";
							String media = "";
							if (obj.get("type").getAsString().equals("video")) {
								if (obj.has("video_url")) {
									String videoUrl = obj.get("video_url")
											.getAsString();

									media = "<video controls=\"controls\" width=\"320\" height=\"240\"><source src=\""
											+ videoUrl
											+ "\" type=\"video/mp4\" > Your browser does not support the video tag.</video>";
								} else if (obj.has("permalink_url")) {
									String permaLinkUrl = obj.get(
											"permalink_url").getAsString();
									media = "[tube]" + permaLinkUrl + "[/tube]";
								} else {
									System.out.println("*VIDEO UNKNOWN TYPE");
								}
							} else {
								JsonArray photos = obj.get("photos")
										.getAsJsonArray();
								JsonObject original = photos.get(0)
										.getAsJsonObject().get("original_size")
										.getAsJsonObject();

								String purl = original.get("url").getAsString();
								int w = original.get("width").getAsInt();
								int h = original.get("height").getAsInt();
								// <img src="smiley.gif" alt="Smiley face"
								// height="42" width="42" />
								media = "<img src=\"" + purl + "\" height=\""
										+ h + "\" width=\"" + w + "\" />";
							}
							ctx.put(key, media);
							// System.out.println(ctx);
							VelocityContext vctx = new VelocityContext(ctx);
							String name = outDir + File.separator
									+ JsonUtils.paddedInt(count, 5) + ".php";
							runVelo("post_contents.vm", vctx, name);
							count++;
						}
					});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		Transform t = new Transform();
		t.run("out/short.txt", "php");
	}
}
