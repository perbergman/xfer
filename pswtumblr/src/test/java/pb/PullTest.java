package pb;

import org.junit.Test;

public class PullTest {

	private static String annaUrl = "http://api.tumblr.com/v2/blog/pinkspiderweb.tumblr.com/{0}?api_key=D1DvsHgqW7HPufGprUH9DsrsT3g5EDBTu0g20F8Oow92VBewvg{1}";
	private static String perUrl = "http://api.tumblr.com/v2/blog/nondualist.tumblr.com/{0}?api_key=D1DvsHgqW7HPufGprUH9DsrsT3g5EDBTu0g20F8Oow92VBewvg{1}";

	@Test
	public void test01() {
		Pull p = new Pull();
		p.run(annaUrl, 100, false);
	}

}
