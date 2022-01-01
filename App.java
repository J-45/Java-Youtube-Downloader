// import java.util.Arrays;
// import java.util.regex.*;

public class App {

    public static void main(String[] urls) throws Exception {
        // System.out.println(Arrays.toString(urls));
        
        String youtubeUrlRegex = "https?://(www\\.)?(youtube\\.com|youtu\\.be)/watch\\?v=[\\w&=]+";
        String[] youtubeUrls = keepYoutubeUrlsOnly(youtubeUrlRegex, urls);

        for (int youtubeUrlsLoop = 0; youtubeUrlsLoop < youtubeUrls.length; ++youtubeUrlsLoop)
        {   
            downloadVideo(youtubeUrls[youtubeUrlsLoop]);
        }
    }

    public static String[] keepYoutubeUrlsOnly(String youtubeUrlRegex, String[] urls) {
        int numberOfValidUrls = countValidUrls(youtubeUrlRegex, urls);
        int addedUrl = 0;
        String[] youtubeUrls = new String[numberOfValidUrls];

        for (int index = 0; index < urls.length; ++index)
        {   
            String url = urls[index];
            if (url.matches(youtubeUrlRegex))
            {
                youtubeUrls[addedUrl] = url;
                addedUrl++;
            }
        }
        return youtubeUrls;
    }

    public static int countValidUrls(String youtubeUrlRegex,String[] args) {
        int goodUrlsCounter = 0;
        for (int index = 0; index < args.length; ++index)
        {   
            String url = args[index];
            if (url.matches(youtubeUrlRegex)){
                // System.out.println("urls[" + index + "]: " + url);
                goodUrlsCounter++;
            }
        }
        return goodUrlsCounter;
    }

    public static String[] getUrlsFromplaylist(String url) {
        String[] foundUrls = {""};
        return foundUrls;
    }

    public static boolean downloadVideo(String url) {
        String youtubePlaylistUrlRegex = "https?://(www\\.)?(youtube\\.com|youtu\\.be)/watch\\?v=\\w+&list=\\w+";

        if (url.matches(youtubePlaylistUrlRegex)){
            System.out.println("PLAYLIST: " + url);
            String[] playlistUrls = getUrlsFromplaylist(url);
            for (int playlistLoopCount = 0; playlistLoopCount < playlistUrls.length; ++playlistLoopCount)
            {   
                String singlePlaylistUrl = playlistUrls[playlistLoopCount];
                downloadVideo(singlePlaylistUrl);
            }
        }
        else{
            System.out.println("URL: " + url);
        }
        return true;
    }
}