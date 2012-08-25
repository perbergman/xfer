package pb;

import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

public class Pull {

	private static String annaUrl = "http://api.tumblr.com/v2/blog/pinkspiderweb.tumblr.com/{0}?api_key=D1DvsHgqW7HPufGprUH9DsrsT3g5EDBTu0g20F8Oow92VBewvg{1}";
	private static String perUrl = "http://api.tumblr.com/v2/blog/nondualist.tumblr.com/{0}?api_key=D1DvsHgqW7HPufGprUH9DsrsT3g5EDBTu0g20F8Oow92VBewvg{1}";

	private Method getMethod = null;

	public Pull() {
		try {
			getMethod = JsonObject.class.getDeclaredMethod("get",
					new Class[] { String.class });
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String pretty(JsonElement je) {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String jsonOutput = gson.toJson(je);
		return jsonOutput;
	}

	private JsonElement parse(Reader rdr) {
		return new JsonParser().parse(new JsonReader(rdr));
	}

	public JsonElement load(String value) {
		return this.parse(new StringReader(value));
	}

	public JsonElement load(String url, String type, String args) {
		JsonElement ret = null;

		String query = MessageFormat.format(url, type, args);
		// System.out.println(query);
		DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(query);

		HttpResponse response = null;
		try {
			response = httpclient.execute(httpGet);
			// System.out.println(response.getStatusLine());
			HttpEntity entity = response.getEntity();
			ret = this.parse(new InputStreamReader(entity.getContent()));
			EntityUtils.consume(entity);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			httpGet.releaseConnection();
		}

		return ret;
	}

	/**
	 * 
	 * @param parts
	 *            - [name]
	 * @return
	 */
	private JsonElement jpath(JsonElement elt, List<String> parts) {
		JsonObject current = elt.getAsJsonObject();
		for (int i = 0; i < (parts.size() - 1); i++) {
			String name = parts.get(i);
			try {
				current = (JsonObject) getMethod.invoke(current,
						new Object[] { name });
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		String last = parts.get(parts.size() - 1);
		return current.getAsJsonObject().get(last);
	}

	public void run(String blog, boolean isPretty) {
		JsonElement info = load(blog, "info", "");
		System.out.println("LOADED " + pretty(info));

		int postCount = jpath(info, Arrays.asList("response", "blog", "posts"))
				.getAsInt();
		System.out.println("POSTS FOUND " + postCount);

		int offset = 0;
		int got = -1;
		JsonElement posts = null;
		do {
			posts = load(blog, "posts", "&limit=" + 20 + "&offset=" + offset);
			got = jpath(posts, Arrays.asList("response", "posts"))
					.getAsJsonArray().size();
			System.out.println("OFFSET " + offset + " GOT " + got);
			offset += got;
			if (isPretty) {
				System.out.println(pretty(posts));
			}
		} while (got > 0);
	}

	public static void main(String[] args) {
		Pull p = new Pull();
		p.run(perUrl, false);
	}

}
