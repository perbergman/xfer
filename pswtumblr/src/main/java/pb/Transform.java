package pb;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Map;

import org.apache.velocity.VelocityContext;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.io.Closeables;
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

	private static int VIDEO_W = 320;
	private static int VIDEO_H = 240;
	private static String AUTH = "1";
	private static String NORMAL_CAT = "5";
	private static String SUSPECT_CAT = "250";

	final private Map<String, String> titleMap = Maps.newHashMap();

	public Transform() {
	}

	public void loadTitles(String csvFile) {
		try {
			titleMap.putAll(Files.readLines(new File(csvFile), Charsets.UTF_8,
					new LineProcessor<Map<String, String>>() {

						private Map<String, String> titles = Maps.newHashMap();
						private int count = 1;

						@Override
						public Map<String, String> getResult() {
							return titles;
						}

						@Override
						public boolean processLine(String csvLine)
								throws IOException {
							String[] parts = csvLine.split(",");
							System.out.println(count + " HAS " + parts[4]);
							titleMap.put("" + (count), parts[4]);
							count++;
							return true;
						}
					}));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected boolean safePut(Map<String, String> ctx, String key,
			JsonObject obj, int counter, PrintWriter errors) {
		if (obj.has(key)) {
			ctx.put(key, obj.get(key).getAsString());
		} else {
			String fail = "safePut key " + key + ", counter " + counter
					+ ", obj" + obj;
			System.err.println(fail);
			errors.println(fail);
			ctx.put(key, "");
			return false;
		}
		return true;
	}

	public void run(String inFile, final String outDir, final PrintWriter bad,
			final String auth, final String normalCat, final String suspectCat) {

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

						// $auth $cat $slug $id $media $caption $tags $date
						public void process(JsonObject obj) {
							Map<String, String> ctx = Maps.newHashMap();
							boolean skip = false;
							boolean fail = false;

							ctx.put("index", "" + count);

							ctx.put("auth", auth);
							ctx.put("cat", normalCat);

							String key = "id";
							fail = safePut(ctx, key, obj, count, bad); // Long

							// key = "slug";
							// fail = safePut(ctx, key, obj, count, bad);
							String slug = obj.get("slug").getAsString();

							String mappedTitle = titleMap.get("" + count);
							if (Strings.isNullOrEmpty(mappedTitle)
									|| mappedTitle.equals("?")) {
								mappedTitle = slug;
							}

							ctx.put("title", mappedTitle);

							key = "type";
							fail = safePut(ctx, key, obj, count, bad);

							key = "date";
							fail = safePut(ctx, key, obj, count, bad);

							key = "caption"; // optional for 'text'
							if (obj.has(key)) {
								fail = safePut(ctx, key, obj, count, bad);
							} else {
								ctx.put(key, "");
							}

							JsonArray tags = obj.get("tags").getAsJsonArray();
							key = "tags";
							ctx.put(key,
									Joiner.on(",").join(JsonUtils.toList(tags)));

							key = "media";
							String media = "";
							String objType = obj.get("type").getAsString();
							if (objType.equals("video")) {
								if (obj.has("video_url")) {
									String videoUrl = obj.get("video_url")
											.getAsString();

									media = "<video controls=\"controls\" width=\""
											+ VIDEO_W
											+ "\" height=\""
											+ VIDEO_H
											+ "\"><source src=\""
											+ videoUrl
											+ "\" type=\"video/mp4\" >Your browser does not support the video tag.</video>";
								} else if (obj.has("permalink_url")) {
									String permaLinkUrl = obj.get(
											"permalink_url").getAsString();
									media = "[tube]" + permaLinkUrl + "[/tube]";
								} else {
									String unknown = count
											+ "* SKIP UNKNOWN VIDEO TYPE "
											+ obj;
									System.err.println(unknown);
									bad.println(unknown);
									ctx.put("cat", suspectCat);
									media = "<p>unknown media url for " + count
											+ " </p>";
									skip = true;
								}
							} else if (objType.equals("photo")) {
								JsonArray photos = obj.get("photos")
										.getAsJsonArray();
								int pics = photos.size();
								for (int pic = 0; pic < pics; pic++) {
									JsonObject original = photos.get(pic)
											.getAsJsonObject()
											.get("original_size")
											.getAsJsonObject();

									String purl = original.get("url")
											.getAsString();
									int w = original.get("width").getAsInt();
									int h = original.get("height").getAsInt();

									media += "<img src=\"" + purl
											+ "\" height=\"" + h
											+ "\" width=\"" + w + "\" />";
								}
							} else if (objType.equals("text")) {
								// String title =
								// obj.get("title").getAsString();
								// ctx.put("slug", title);
								media = obj.get("body").getAsString();

							} else {
								String unknown = count + "* CAN'T HANDLE TYPE "
										+ objType + " of " + obj;
								System.err.println(unknown);
								bad.println(unknown);
								skip = true;
							}
							ctx.put(key, media);
							if (!skip) {
								VelocityContext vctx = new VelocityContext(ctx);
								String name = outDir + File.separator
										+ JsonUtils.paddedInt(count, 5)
										+ ".php";
								runVelo("post_contents.vm", vctx, name);
							}
							count++;
						}
					});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		Transform t = new Transform();
		t.loadTitles("out/titles.csv");
		PrintWriter badBoys = null; // new StringWriter();
		boolean goOn = true;
		try {
			badBoys = new PrintWriter(new OutputStreamWriter(
					new FileOutputStream(new File("out/bad.txt")),
					VeloAware.ENC));
		} catch (Exception e) {
			e.printStackTrace();
			goOn = false;
		}

		if (goOn) {
			t.run("out/blog.txt", "php", badBoys, AUTH, NORMAL_CAT, SUSPECT_CAT);
		}
		Closeables.closeQuietly(badBoys);
	}
}
