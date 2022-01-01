import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;
// import java.util.regex.*;
// import java.util.Arrays;

public class AppTest {

    String[] testUrls = {"","https://stackoverflow.com/","https://www.youtube.com/watch?v=3PcIJKd1PKU", "https://www.youtube.com/watch?v=wjAdxAbmQNM&list=RDwjAdxAbmQNM","https://youtu.be/watch?v=3PcIJKd1PKU","http://www.youtube.com/watch?v=wjAdxAbmQNM&list=RDwjAdxAbmQNM&=8ds1v887s8d7f8d"};

    String[] goodUrls = {"https://www.youtube.com/watch?v=3PcIJKd1PKU", "https://www.youtube.com/watch?v=wjAdxAbmQNM&list=RDwjAdxAbmQNM","https://youtu.be/watch?v=3PcIJKd1PKU","http://www.youtube.com/watch?v=wjAdxAbmQNM&list=RDwjAdxAbmQNM&=8ds1v887s8d7f8d"};

    String youtubeUrlRegex = "https?://(www\\.)?(youtube\\.com|youtu\\.be)/watch\\?v=[\\w&=]+";

    @Test
    @DisplayName("get Url In Arg")
    void keepYoutubeUrlsOnly_Test() {
        assertArrayEquals(goodUrls, App.keepYoutubeUrlsOnly(youtubeUrlRegex, testUrls));
    }

    @Test
    @DisplayName("count valid url in args with regex")
    void countValidUrls_Test() {
        assertEquals(4, App.countValidUrls(youtubeUrlRegex, testUrls));
    }

}
