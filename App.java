import java.util.Arrays;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringEscapeUtils; // https://mvnrepository.com/artifact/commons-lang/commons-lang/2.6

// https://stackoverflow.com/questions/6020384/create-array-of-regex-matches
// https://www.amitph.com/java-download-file-from-url/#Using_Plain_Java_IO

public class App {
    public static void main(String[] urls) throws Exception, IOException {
        // System.out.println(Arrays.toString(urls));
        System.out.println("Working Directory = " + System.getProperty("user.dir"));
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
                String videoUrl = audioAndVideo[0];
                String audioUrl = audioAndVideo[1];
                
                // Files.delete(Paths.get("video.mp4"));
                // Files.delete(Paths.get("audio.mp3"));
                download(videoUrl,"video.mp4");
                download(audioUrl,"audio.mp3");
                if (audioUrl != ""){
                    System.out.println("Joining...");
                    join();
                }
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
        // HttpClient client = HttpClient.newHttpClient();
        // HttpRequest request = HttpRequest.newBuilder()
        //         .uri(URI.create(url))
        //         .setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
        //         .setHeader("Accept-Encoding", "gzip, deflate")
        //         .setHeader("User-Agent", "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:95.0) Gecko/20100101 Firefox/95.0")
        //         .setHeader("Accept-Encoding", "")
        //         .setHeader("Accept-Language", "fr,fr-FR;q=0.8,en-US;q=0.5,en;q=0.3")
        //         .setHeader("Content-Type", "")
        //         .GET() // GET is default
        //         .build();
        // HttpResponse<String> response = client.send(request,
        //         HttpResponse.BodyHandlers.ofString());
        // return response.body();

        String result = null;
        try (InputStream inputStream = Runtime.getRuntime().exec("curl -s --http2 "+url+" -H 'User-Agent: Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:95.0) Gecko/20100101 Firefox/95.0' -H 'Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8' -H 'Accept-Language: fr,fr-FR;q=0.8,en-US;q=0.5,en;q=0.3' -H 'Accept-Encoding: gzip, deflate, br' -H 'DNT: 1' -H 'Connection: keep-alive' -H 'Upgrade-Insecure-Requests: 1' -H 'Sec-Fetch-Dest: document' -H 'Sec-Fetch-Mode: navigate' -H 'Sec-Fetch-Site: none' -H 'Sec-Fetch-User: ?1' -H 'Pragma: no-cache' -H 'Cache-Control: no-cache' -H 'TE: trailers' --compressed").getInputStream();
                Scanner s = new Scanner(inputStream).useDelimiter("\\A")) {
            result = s.hasNext() ? s.next() : null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
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
        long videoLastContentLength = 0;
        long audioLastContentLength = 0;
        boolean needAudioFile = false;
        Matcher matcher = Pattern
                .compile(dataRegex)
                .matcher(htmlContent);

        while (matcher.find()) {
            String downloadUrl = StringEscapeUtils.unescapeJava(matcher.group(1));
            String mimeType = matcher.group(2).split(";")[0];
            String height = matcher.group(3);
            long contentLength = Integer.parseInt(matcher.group(4));



            if (height != null){
                if (Integer.parseInt(height) > 720) {
                    needAudioFile = true;
                }

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

            if(!needAudioFile)
            {
                audioAndVideo[1] = "";
            }

            allMatches.add(downloadUrl+" - "+mimeType+" - "+height+" - "+contentLength);

            // System.out.println("url: "+StringEscapeUtils.unescapeJava(downloadUrl)+"\nmimeType: "+mimeType+"\nheight: "+height+"\ncontentLength: "+contentLength);
        }

        // System.out.println(Arrays.toString(audioAndVideo));
        return audioAndVideo;
    }

    public static void download(String dlLink, String filename){
        System.out.println(dlLink);
        String result = null;
        try (InputStream inputStream = Runtime.getRuntime().exec("curl -L --http3 "+dlLink+" -o /home/groot/Documents/yt_dl/src/"+filename+"").getInputStream();
                Scanner s = new Scanner(inputStream).useDelimiter("\\A")) {
            result = s.hasNext() ? s.next() : null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(result);
    }

    public static void join() {
        String result = null;
        try (InputStream inputStream = Runtime.getRuntime().exec("ffmpeg -i /home/groot/Documents/yt_dl/src/video.mp4 -i /home/groot/Documents/yt_dl/src/audio.mp3 -c:v copy -c:a copy /home/groot/Documents/yt_dl/src/output.mp4").getInputStream();
                Scanner s = new Scanner(inputStream).useDelimiter("\\A")) {
            result = s.hasNext() ? s.next() : null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}