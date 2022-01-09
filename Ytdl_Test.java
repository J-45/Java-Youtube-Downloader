package eu.j45.youtube;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.Assert;
import org.junit.jupiter.api.*;
import java.io.IOException;
// import java.util.regex.*;
// import java.util.Arrays;

public class Ytdl_Test {

    String[] testUrls = {"","https://stackoverflow.com/", "https://www.youtube.com/watch?v=_SnGxw13QxU", "https://www.youtube.com/watch?v=KJgsSFOSQv0&list=PLWKjhJtqVAbmUE5IqyfGYEYjrZBYzaT4m","https://youtu.be/watch?v=3PcIJKd1PKU","http://www.youtube.com/watch?v=wjAdxAbmQNM&list=RDwjAdxAbmQNM&=8ds1v887s8d7f8d"};
    String[] goodUrls = { "https://www.youtube.com/watch?v=_SnGxw13QxU", "https://www.youtube.com/watch?v=KJgsSFOSQv0&list=PLWKjhJtqVAbmUE5IqyfGYEYjrZBYzaT4m","http://www.youtube.com/watch?v=wjAdxAbmQNM&list=RDwjAdxAbmQNM&=8ds1v887s8d7f8d"};
    String[] goodPlaylistUrls = {"https://www.youtube.com/watch?v=wfWxdh-_k_4&list=PLWKjhJtqVAbkq5Oh8ERRJ1aPZK2NKBSRx","https://www.youtube.com/watch?v=C5cnZ-gZy2I&list=PLWKjhJtqVAbkq5Oh8ERRJ1aPZK2NKBSRx"};
    String youtubeUrlRegex = "https?://(www\\.)?(youtube\\.com|youtu\\.be)/watch\\?v=[\\w&=]+";

    @Test
    @DisplayName("test main")
    void main_Test()  throws Exception{
        Ytdl.main(testUrls);
    }

    @Test
    @DisplayName("get Url In Arg")
    void keepYoutubeUrlsOnly_Test() {
        assertArrayEquals(goodUrls, Ytdl.keepYoutubeUrlsOnly(youtubeUrlRegex, testUrls));
    }

    @Test
    @DisplayName("count valid url in args with regex")
    void countValidUrls_Test() {
        assertEquals(testUrls.length - 2, Ytdl.countValidUrls(youtubeUrlRegex, testUrls));
    }

    @Test
    @DisplayName("get html content of page url")
    void fetchWebpage_Test() throws IOException, InterruptedException {
        assertEquals("200 OK", Ytdl.fetchWebpage("https://httpstat.us/200"));
    }

    @Test
    @DisplayName("get urls from html content")
    void getUrlsFromplaylist_Test() throws IOException, InterruptedException {
        assertArrayEquals(goodPlaylistUrls, Ytdl.getUrlsFromplaylist("https://www.youtube.com/watch?v=wfWxdh-_k_4&list=PLWKjhJtqVAbkq5Oh8ERRJ1aPZK2NKBSRx"));
    }

    @Test
    @DisplayName("get playlist id from playlist url")
    void getPlayslistId_Test() {
        assertEquals("PLWKjhJtqVAbkq5Oh8ERRJ1aPZK2NKBSRx", Ytdl.getPlayslistId("https://www.youtube.com/watch?v=wfWxdh-_k_4&list=PLWKjhJtqVAbkq5Oh8ERRJ1aPZK2NKBSRx"));
    }

    @Test
    @DisplayName("get download url")
    void getData_Test() throws IOException, Exception {
        String[] data = new String[2];
        data = Ytdl.getData("https://www.youtube.com/watch?v=iKBs9l8jS6Q");
        String videoLink = data[0];
        String audioLink = data[1];
        String title = data[2];
        Assert.assertTrue(videoLink.contains("videoplayback"));
        Assert.assertTrue(audioLink.contains("videoplayback"));
        Assert.assertEquals("Top 100 3D Renders from the Internet&#39;s Largest CG Challenge | Alternate Realities", title);
    }

    @Test
    @DisplayName("download url")
    void download_Test() throws IOException, Exception {
        String[] audioAndVideo = new String[2];
        audioAndVideo = Ytdl.getData("https://www.youtube.com/watch?v=3PcIJKd1PKU");
        String videoLink = audioAndVideo[0];
        String audioLink = audioAndVideo[1];
        Assert.assertTrue(Ytdl.download(videoLink,"video.mp4"));
        Assert.assertTrue(Ytdl.download(audioLink,"audio.mp3"));
    }
}