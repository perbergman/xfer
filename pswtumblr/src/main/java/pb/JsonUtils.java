package pb;

import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.text.DecimalFormat;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

public class JsonUtils {

	public static String pretty(JsonElement je) {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String jsonOutput = gson.toJson(je);
		return jsonOutput;
	}

	private static JsonElement parse(Reader rdr) {
		return new JsonParser().parse(new JsonReader(rdr));
	}

	public static JsonElement loadString(String value) {
		return parse(new StringReader(value));
	}

	public static JsonElement loadURL(String query) {
		JsonElement ret = null;

		// System.out.println(query);
		DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(query);

		HttpResponse response = null;
		try {
			response = httpclient.execute(httpGet);
			// System.out.println(response.getStatusLine());
			HttpEntity entity = response.getEntity();
			ret = parse(new InputStreamReader(entity.getContent()));
			EntityUtils.consume(entity);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			httpGet.releaseConnection();
		}

		return ret;
	}

	public static List<String> toList(JsonArray array) {
		List<String> ret = Lists.newArrayListWithCapacity(array.size());
		for (int i = 0; i < array.size(); i++) {
			String value = array.get(i).toString();
			value = value.substring(1, value.length() - 1);
			ret.add(value.trim());
		}
		return ret;
	}

	public static String paddedInt(int value, int pad) {
		DecimalFormat myFormatter = new DecimalFormat(Strings.repeat("0", pad));
		return myFormatter.format(value);
	}

}
