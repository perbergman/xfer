package pb;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class Pull {

	private Method getMethod = null;

	public Pull() {
		try {
			getMethod = JsonObject.class.getDeclaredMethod("get",
					new Class[] { String.class });
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String tumblrURL(String url, String type, String args) {
		return MessageFormat.format(url, type, args);
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

	public void run(String blog, int max, boolean isPretty) {
		JsonElement info = JsonUtils.loadURL(this.tumblrURL(blog, "info", ""));

		if (max < 1) {
			max = Integer.MAX_VALUE;
		}

		BufferedWriter wr = null;
		// Writer wr = new PrintWriter(System.out);
		try {
			wr = Files.newWriter(new File("out/blog.txt"), Charsets.UTF_8);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		System.out.println("LOADED " + JsonUtils.pretty(info));

		int postCount = jpath(info, Arrays.asList("response", "blog", "posts"))
				.getAsInt();
		System.out.println("POSTS FOUND " + postCount);

		int offset = 0;
		int got = -1;
		int total = 0;
		JsonElement reply = null;
		do {
			reply = JsonUtils.loadURL(this.tumblrURL(blog, "posts", "&limit=" + 20
					+ "&offset=" + offset));
			JsonArray posts = jpath(reply, Arrays.asList("response", "posts"))
					.getAsJsonArray();
			got = posts.size();
			System.out.println("OFFSET " + offset + " GOT " + got);
			offset += got;
			for (int i = 0; i < got; i++) {
				System.out.println(++total + " " + posts.get(i));
				try {
					wr.write(posts.get(i).toString());
					wr.write('\n');
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if (isPretty) {
				System.out.println(JsonUtils.pretty(posts));
			}
		} while (got > 0 && offset < max);
		try {
			wr.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
