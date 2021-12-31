import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;
import java.util.regex.*;
import java.util.Arrays;

public class AppTest {

    String[] args = {"","https://stackoverflow.com/","https://www.youtube.com/watch?v=3PcIJKd1PKU", "https://www.youtube.com/watch?v=wjAdxAbmQNM&list=RDwjAdxAbmQNM&start_radio=1&rv=wjAdxAbmQNM&t=0"};

    @Test
    @DisplayName("get Url In Args test")
    void keepYoutubeUrlsOnly_Test() {
        assertEquals(args, App.keepYoutubeUrlsOnly(args));
    }

}
