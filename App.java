import java.util.Arrays;
import java.util.regex.*;

public class App {

    public static void main(String[] args) throws Exception {
        String[] urls = keepYoutubeUrlsOnly(args);
        
        for (int index = 0; index < args.length; ++index)
        {
            System.out.println("args[" + index + "]: " + args[index]);
        }
        
    }

    public static String[] keepYoutubeUrlsOnly(String[] args) {
        System.out.println(Arrays.toString(args));
        return args;
    }
}
