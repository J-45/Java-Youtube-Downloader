import java.util.Arrays;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.*;
import org.apache.commons.lang.StringEscapeUtils; // https://mvnrepository.com/artifact/commons-lang/commons-lang/2.6

// https://stackoverflow.com/questions/6020384/create-array-of-regex-matches

public class App {

    public static void main(String[] urls) throws Exception, IOException {
        // System.out.println(Arrays.toString(urls));
        String youtubeUrlRegex = "https?://(www\\.)?(youtube\\.com|youtu\\.be)/watch\\?v=[\\w&=]+";
        String youtubePlaylistUrlRegex = "https?://(www\\.)?(youtube\\.com|youtu\\.be)/watch\\?v=\\w+&list=\\w+";
        String[] youtubeUrls = keepYoutubeUrlsOnly(youtubeUrlRegex, urls);
        String[] audioAndVideo = new String[2];

        for (int youtubeUrlsLoop = 0; youtubeUrlsLoop < youtubeUrls.length; youtubeUrlsLoop = youtubeUrlsLoop + 1) {
            // System.out.println("loop 1:" + youtubeUrlsLoop);
            String currentURl = youtubeUrls[youtubeUrlsLoop];
            if (currentURl.matches(youtubePlaylistUrlRegex)) {
                System.out.println("PLAYLIST: " + currentURl);
                String[] playlistUrls = getUrlsFromplaylist(currentURl);
                for (int playlistLoopCount = 0; playlistLoopCount < playlistUrls.length; playlistLoopCount++) {
                    // System.out.println("loop 2:" + playlistLoopCount);
                    String singlePlaylistUrl = playlistUrls[playlistLoopCount];
                    audioAndVideo = getDownloadUrl(singlePlaylistUrl);
                }
            } else {
                System.out.println("URL: " + currentURl);
                audioAndVideo = getDownloadUrl(currentURl);
            }
        }
    }

    public static String[] keepYoutubeUrlsOnly(String youtubeUrlRegex, String[] urls) {
        int numberOfValidUrls = countValidUrls(youtubeUrlRegex, urls);
        int addedUrl = 0;
        String[] youtubeUrls = new String[numberOfValidUrls];

        for (int index = 0; index < urls.length; ++index) {
            String url = urls[index];
            if (url.matches(youtubeUrlRegex)) {
                youtubeUrls[addedUrl] = url;
                addedUrl++;
            }
        }
        return youtubeUrls;
    }

    public static int countValidUrls(String youtubeUrlRegex, String[] args) {
        int goodUrlsCounter = 0;
        for (int index = 0; index < args.length; ++index) {
            String url = args[index];
            if (url.matches(youtubeUrlRegex)) {
                // System.out.println("urls[" + index + "]: " + url);
                goodUrlsCounter++;
            }
        }
        return goodUrlsCounter;
    }

    public static String fetchWebpage(String url) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .setHeader("Accept", "*/*")
                .setHeader("User-Agent", "curl/7.68.0")
                .setHeader("Accept-Encoding", "")
                .setHeader("Accept-Language", "fr,fr-FR;q=0.8,en-US;q=0.5,en;q=0.3")
                .setHeader("Content-Type", "")
                .GET() // GET is default
                .build();
        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    public static String getPlayslistId(String url) {
        String playlistId = "";
        Pattern pattern = Pattern.compile("watch\\?v=[\\w-]+&list=(\\w+)");
        Matcher matcher = pattern.matcher(url);
        // System.out.println(url); // Debug
        if (matcher.find()) {
            playlistId = matcher.group(1);

        } else {
            playlistId = "none";
        }
        return playlistId;
    }

    public static String[] getUrlsFromplaylist(String url) throws IOException, InterruptedException {
        List<String> allMatches = new ArrayList<String>();
        String playlisId = getPlayslistId(url);
        String htmlContent = fetchWebpage("https://www.youtube.com/playlist?list=" + playlisId);
        Matcher matcher = Pattern
                .compile("watchEndpoint\":\\{\"videoId\":\"([\\w-]+)\",\"playlistId\":\"\\w+\",\"index")
                .matcher(htmlContent);
        while (matcher.find()) {
            allMatches.add("https://www.youtube.com/watch?v=" + matcher.group(1) + "&list=" + playlisId);
        }
        return allMatches.toArray(new String[0]);
    }

    public static String[] getDownloadUrl(String url) throws IOException, InterruptedException 
    {
        System.out.println("DOWNLOAD:" + url);
        List<String> allMatches = new ArrayList<String>();
        String[] audioAndVideo = new String[2];
        String htmlContent = fetchWebpage(url);
        String dataRegex = "\"url\":\"([^\"]+)\",\"mimeType\":\"((?:[^,]+))\",\"bitrate\":\\d+,(?:\"width\":\\d+,\"height\":(\\d+),)?(?:(?:[^,]+),){5}\"contentLength\":\"(\\d+)\"";
        int videoLastContentLength = 0;
        int audioLastContentLength = 0;
        boolean needAudioFile = false;
        Matcher matcher = Pattern
                .compile(dataRegex)
                .matcher(htmlContent);

        while (matcher.find()) {
            String downloadUrl = StringEscapeUtils.unescapeJava(matcher.group(1));
            String mimeType = matcher.group(2).split(";")[0];
            String height = matcher.group(3);
            int contentLength = Integer.parseInt(matcher.group(4));
            if (Integer.parseInt(height) > 720){
                needAudioFile = true;
            }
            if(needAudioFile){
                if (height != null){
                    if(contentLength > videoLastContentLength){
                        videoLastContentLength = contentLength;
                        audioAndVideo[0] = downloadUrl;
                    }
                }else{
                    if(contentLength > audioLastContentLength){
                        audioLastContentLength = contentLength;
                        audioAndVideo[1] = downloadUrl;
                    }
                }
            }else{
                audioAndVideo[1] = "";
            }

            allMatches.add(downloadUrl+" - "+mimeType+" - "+height+" - "+contentLength);

            // System.out.println("url: "+StringEscapeUtils.unescapeJava(downloadUrl)+"\nmimeType: "+mimeType+"\nheight: "+height+"\ncontentLength: "+contentLength);
        }

        System.out.println(Arrays.toString(audioAndVideo));
        System.exit(0);
        return audioAndVideo;
    }
}
