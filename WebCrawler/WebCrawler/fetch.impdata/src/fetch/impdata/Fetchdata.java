package fetch.impdata;

import twitter4j.*;
import twitterfb.feeds.Feeds;

import com.restfb.exception.FacebookException;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Fetchdata {
    /*
     Function to fetch the JSONArray as input parameter
     Iterate it and return it into corresponding key value pair
    */
    private static void getdata(JSONArray arr){
        try{
            for (int i = 0; i < arr.length(); i++) {
                JSONObject objects = arr.getJSONObject(i);
                Iterator key = objects.keys();
                while (key.hasNext()) {
                    String k = key.next().toString();
                    System.out.println("Key : " + k + ", value : "
                            + objects.getString(k));
                }
                System.out.println("-----------"); // for separate the data in each iteration
            }
        } catch (JSONException e) {
            //	Catch all -- we're going to read the stack trace and figure out what needs to be done to fix it
            e.printStackTrace();
            throw new RuntimeException("ERROR in getting Facebook data. " + e);
        }
    }
    public static void main(String[] args) throws RuntimeException {
        try {
            System.out.println("############## Facebook Feeds ###############");
            System.out.println("Previous data of Module 1:-");

            /* create the object as JSONArray for Feed and call getFBFeeds */

            JSONArray fbdata  = Feeds.getFBFeeds();
            System.out.println("Important data from Module 1:-");
            System.out.println("Important Data Facebook");
            getdata(fbdata); // call the getdata
            System.out.println("\n");
        } catch (FacebookException e) {
            e.printStackTrace();
            throw new RuntimeException("ERROR in getting FB graph data. " + e);
        }
        try {
            System.out.println("############## Twitter Feeds ################");
            System.out.println("Previous data of Module 1:-");

            /* create the object as JSONArray for Feed and call getTwitterData */

            JSONArray twitterdata  = Feeds.getTwitterData();
            System.out.println("Important data from Module 1:-");
            System.out.println("Important Data Twitter");
            getdata(twitterdata); // call the getdata
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("ERROR in getting Twitter data. " + e);
        }
    }
}
// Todo extract the link from the Feeds
class HTMLLinkExtractor {

    private Pattern patternTag, patternLink;
    private Matcher matcherTag, matcherLink;

    private static final String HTML_A_TAG_PATTERN = "(?i)<a([^>]+)>(.+?)</a>";
    private static final String HTML_A_HREF_TAG_PATTERN = "\\s*(?i)href\\s*=\\s*(\"([^\"]*\")|'[^']*'|([^'\">\\s]+))";

    public HTMLLinkExtractor() {
        patternTag = Pattern.compile(HTML_A_TAG_PATTERN);
        patternLink = Pattern.compile(HTML_A_HREF_TAG_PATTERN);
    }

    /**
     * Extract href String with regular expression
     *
     */
    public Set<String> grabHTMLLinks(final String html) {
        Set<String> result = new HashSet<String>();
        matcherTag = patternTag.matcher(html);
        while (matcherTag.find()) {
            String href = matcherTag.group(1); // href
            matcherLink = patternLink.matcher(href);
            while (matcherLink.find()) {
                String link = matcherLink.group(1); // link
                link = link.replaceAll("'", "");
                link = link.replaceAll("\"", "");
                result.add(link);
            }
        }
        return result;
    }

}
