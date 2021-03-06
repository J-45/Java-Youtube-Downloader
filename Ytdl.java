package eu.j45.youtube;

// import java.util.Arrays;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringEscapeUtils; // https://mvnrepository.com/artifact/commons-lang/commons-lang/2.6

// https://stackoverflow.com/questions/6020384/create-array-of-regex-matches
// https://www.amitph.com/java-download-file-from-url/#Using_Plain_Java_IO
// https://stackoverflow.com/questions/44238554/how-to-run-ffmpeg-commandwindows-in-java

public class Ytdl {
    public static void main(String[] urls) throws Exception, IOException {
        // System.out.println(Arrays.toString(urls));
        System.out.println("Working Directory = " + System.getProperty("user.dir"));
        String youtubeUrlRegex = "https?://(www\\.)?(youtube\\.com|youtu\\.be)/watch\\?v=[\\w&=]+";
        String youtubePlaylistUrlRegex = "https?://(www\\.)?(youtube\\.com|youtu\\.be)/watch\\?v=\\w+&list=\\w+";
        String[] youtubeUrls = keepYoutubeUrlsOnly(youtubeUrlRegex, urls);
        String[] data = new String[5];
        String[] playlistUrls;
        String videoUrl;
        String audioUrl;
        String title;
        int videoContentLength;
        int audioContentLength;
        
        for (String currentURl : youtubeUrls) {
            if (currentURl.matches(youtubePlaylistUrlRegex)) {
                System.out.println("PLAYLIST: " + currentURl);
                playlistUrls = getUrlsFromplaylist(currentURl);
                for (String singlePlaylistUrl: playlistUrls) {
                    data = getData(singlePlaylistUrl);
                }
            } else {
                System.out.println("URL: " + currentURl);
                data = getData(currentURl);
                videoUrl = data[0];
                audioUrl = data[1];
                title = data[2];
                videoContentLength = Integer.parseInt(data[3]);
                audioContentLength = Integer.parseInt(data[4]);
                // Files.delete(Paths.get("video.mp4"));
                // Files.delete(Paths.get("audio.mp3"));
                download(videoUrl,"video.mp4",videoContentLength);
                download(audioUrl,"audio.mp3",audioContentLength);
                if (audioUrl != ""){
                    System.out.println("JOIN");
                    join(title+".mp4");
                }
            }
        }
    }

    public static String[] keepYoutubeUrlsOnly(String youtubeUrlRegex, String[] urls) {
        int numberOfValidUrls = countValidUrls(youtubeUrlRegex, urls);
        int addedUrl = 0;
        String[] youtubeUrls = new String[numberOfValidUrls];
        
        for (String url: urls) {
            if (url.matches(youtubeUrlRegex)) {
                youtubeUrls[addedUrl] = url;
                addedUrl++;
            }
        }
        return youtubeUrls;
    }

    public static int countValidUrls(String youtubeUrlRegex, String[] args) {
        int goodUrlsCounter = 0;

        for (String url: args) {
            if (url.matches(youtubeUrlRegex)) {
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

    public static String[] getData(String url) throws IOException, InterruptedException 
    {
        System.out.println("DOWNLOAD:" + url);
        List<String> allMatches = new ArrayList<String>();
        String[] data = new String[5];
        String htmlContent = fetchWebpage(url);

        Pattern patternTitle = Pattern.compile("<title>(.+) - YouTube</title>");
        Matcher matcherTitle = patternTitle.matcher(htmlContent);
        // System.out.println(url); // Debug
        if (matcherTitle.find()) {
            data[2] = matcherTitle.group(1);

        } else {
            data[2] = "no title";
        }

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
                    data[0] = downloadUrl;
                }
            }else{
                if(contentLength > audioLastContentLength){
                    audioLastContentLength = contentLength;
                    data[1] = downloadUrl;
                }
            }

            if(!needAudioFile)
            {
                data[1] = "";
            }

            allMatches.add(downloadUrl+" - "+mimeType+" - "+height+" - "+contentLength);

            // System.out.println("url: "+StringEscapeUtils.unescapeJava(downloadUrl)+"\nmimeType: "+mimeType+"\nheight: "+height+"\ncontentLength: "+contentLength);
        }
        data[3] = Long.toString(videoLastContentLength);
        data[4] = Long.toString(audioLastContentLength);
        // System.out.println(data[3] + " - " + data[4]);
        // System.out.println(Arrays.toString(audioAndVideo));
        return data;
    }

    public static boolean download(String dlLink, String filename,int contentLength) throws IOException{
        System.out.println(dlLink);
//        String result = null;
        try (InputStream inputStream = Runtime.getRuntime().exec("curl -L -C - --speed-limit 1024 --speed-time 5 --retry 7 --http3 "+dlLink+" -o /home/groot/Documents/yt_dl/src/"+filename).getInputStream();
                Scanner s = new Scanner(inputStream).useDelimiter("\\A")) {
//            result = s.hasNext() ? s.next() : null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        File file = new File("/home/groot/Documents/yt_dl/src/"+filename);
        // System.out.println(file.length() + " - " +contentLength);
        

        if(file.length() == contentLength){
            return true;
        }else{
            return false;
        }
    }

    public static boolean join(String filename) throws IOException, InterruptedException {
        filename = filename.replace('/', '-');
        File outputFile = new File("/home/groot/Documents/yt_dl/src/"+filename);
        String[] cmd = {"ffmpeg","-y","-i","/home/groot/Documents/yt_dl/src/video.mp4","-i","/home/groot/Documents/yt_dl/src/audio.mp3","-c:v","copy","-c:a","copy",outputFile.toString()};
        
        Process process = Runtime.getRuntime().exec(cmd);                    
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));                                          
        String s;                                                                
        while ((s = reader.readLine()) != null) {                                
            System.out.println("Script output: " + s);                             
        }

        System.out.println(cmd);  
        System.out.println(outputFile+" exists:"+ outputFile.exists());
        return outputFile.exists();
        }
}
