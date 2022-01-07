import static org.junit.jupiter.api.Assertions.*;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import java.io.IOException;
// import java.util.regex.*;
// import java.util.Arrays;

public class AppTest {

    String[] testUrls = {"","https://stackoverflow.com/","https://www.youtube.com/watch?v=iKBs9l8jS6Q", "https://www.youtube.com/watch?v=_SnGxw13QxU", "https://www.youtube.com/watch?v=KJgsSFOSQv0&list=PLWKjhJtqVAbmUE5IqyfGYEYjrZBYzaT4m","https://www.youtube.com/watch?v=3PcIJKd1PKU","https://youtu.be/watch?v=3PcIJKd1PKU","http://www.youtube.com/watch?v=wjAdxAbmQNM&list=RDwjAdxAbmQNM&=8ds1v887s8d7f8d"};
    String[] goodUrls = {"https://www.youtube.com/watch?v=iKBs9l8jS6Q", "https://www.youtube.com/watch?v=_SnGxw13QxU", "https://www.youtube.com/watch?v=KJgsSFOSQv0&list=PLWKjhJtqVAbmUE5IqyfGYEYjrZBYzaT4m","https://www.youtube.com/watch?v=3PcIJKd1PKU","https://youtu.be/watch?v=3PcIJKd1PKU","http://www.youtube.com/watch?v=wjAdxAbmQNM&list=RDwjAdxAbmQNM&=8ds1v887s8d7f8d"};
    String[] goodPlaylistUrls = {"https://www.youtube.com/watch?v=wfWxdh-_k_4&list=PLWKjhJtqVAbkq5Oh8ERRJ1aPZK2NKBSRx","https://www.youtube.com/watch?v=C5cnZ-gZy2I&list=PLWKjhJtqVAbkq5Oh8ERRJ1aPZK2NKBSRx"};
    String youtubeUrlRegex = "https?://(www\\.)?(youtube\\.com|youtu\\.be)/watch\\?v=[\\w&=]+";

    @Test
    @DisplayName("test main")
    void main_Test()  throws Exception{
        App.main(testUrls);
    }

    @Test
    @DisplayName("get Url In Arg")
    void keepYoutubeUrlsOnly_Test() {
        assertArrayEquals(goodUrls, App.keepYoutubeUrlsOnly(youtubeUrlRegex, testUrls));
    }

    @Test
    @DisplayName("count valid url in args with regex")
    void countValidUrls_Test() {
        assertEquals(5, App.countValidUrls(youtubeUrlRegex, testUrls));
    }

    @Test
    @DisplayName("get html content of page url")
    void fetchWebpage_Test() throws IOException, InterruptedException {
        assertEquals("200 OK", App.fetchWebpage("https://httpstat.us/200"));
    }

    @Test
    @DisplayName("get urls from html content")
    void getUrlsFromplaylist_Test() throws IOException, InterruptedException {
        assertArrayEquals(goodPlaylistUrls, App.getUrlsFromplaylist("https://www.youtube.com/watch?v=wfWxdh-_k_4&list=PLWKjhJtqVAbkq5Oh8ERRJ1aPZK2NKBSRx"));
    }

    @Test
    @DisplayName("get playlist id from playlist url")
    void getPlayslistId_Test() {
        assertEquals("PLWKjhJtqVAbkq5Oh8ERRJ1aPZK2NKBSRx", App.getPlayslistId("https://www.youtube.com/watch?v=wfWxdh-_k_4&list=PLWKjhJtqVAbkq5Oh8ERRJ1aPZK2NKBSRx"));
    }

    @Test
    @DisplayName("get download url")
    void getDownloadUrl_Test() throws IOException, Exception {
        String[] audioAndVideo = new String[2];
        audioAndVideo = App.getDownloadUrl("https://www.youtube.com/watch?v=iKBs9l8jS6Q");
        Assert.assertTrue(audioAndVideo[0].contains("videoplayback"));
        Assert.assertTrue(audioAndVideo[0].contains("videoplayback"));
    }

    @Test
    @DisplayName("download url")
    void download_Test() throws IOException, Exception {
        String[] audioAndVideo = new String[2];
        audioAndVideo = App.getDownloadUrl("https://www.youtube.com/watch?v=eQ3aHHOE_vY");
        App.download(audioAndVideo[0],"video.mp4");
    }
}
