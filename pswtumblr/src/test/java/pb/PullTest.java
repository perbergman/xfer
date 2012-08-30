package pb;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.junit.Test;

public class PullTest {

	@Test
	public void test01() {
		Pull p = new Pull();
		Properties urls = new Properties();
		try {
			InputStream is = new FileInputStream("app.properties");
			urls.load(is);
		} catch (IOException e) {
			e.printStackTrace();
		}
		String url = urls.getProperty("url1");
		p.run(url, -100, false);
	}

}
