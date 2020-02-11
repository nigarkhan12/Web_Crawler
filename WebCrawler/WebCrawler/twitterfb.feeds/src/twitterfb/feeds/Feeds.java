package twitterfb.feeds;
import com.restfb.FacebookClient;

import com.restfb.types.Post;
import com.restfb.Connection;
import com.restfb.types.Group;
import com.restfb.types.User;
import com.restfb.DefaultFacebookClient;

import java.util.List;
import java.util.HashMap;

import twitter4j.*;
import twitter4j.auth.OAuth2Token;
import twitter4j.conf.ConfigurationBuilder;

import java.util.Map;


public class  Feeds {
    /* accesstoken, fbClient, User for Facebook  */
    private static String accesstoken = "xxxxxxxxxx";
    private static FacebookClient fbClient = new DefaultFacebookClient(accesstoken);
    private static User me = fbClient.fetchObject("me", User.class);

    /* CONSUMER_KEY, CONSUMER_SECRET for Twitter */
    private static final String CONSUMER_KEY		= "xxxxxxxxxxxx";
    private static final String CONSUMER_SECRET 	= "xxxxxxxxxxxx";

    //	How many tweets to retrieve in every call to Twitter. 100 is the maximum allowed in the API
    private static final int TWEETS_PER_QUERY		= 2;


    private static HashMap<String, String> map= new HashMap<>();
    private static JSONArray arr= new JSONArray();
    private static JSONArray ImpData;

    /*
    	This controls how many queries, maximum, we will make of Twitter before cutting off the results.
    	We will retrieve up to MAX_QUERIES*TWEETS_PER_QUERY tweets.

    */
    private static final int MAX_QUERIES			= 5;

    //	What we want to search for in this program.  india news always returns as many results as we could
    //	ever want, so it's safe to assume we'll get multiple pages back...
    private static final String SEARCH_TERM			= "#india news";


    private static String cleanText(String text)
    {
        text = text.replace("\n", "\\n");
        text = text.replace("\t", "\\t");

        return text;
    }


    private static OAuth2Token getOAuth2Token()
    {
        OAuth2Token token = null;
        ConfigurationBuilder cb;

        cb = new ConfigurationBuilder();
        cb.setApplicationOnlyAuthEnabled(true);

        cb.setOAuthConsumerKey(CONSUMER_KEY).setOAuthConsumerSecret(CONSUMER_SECRET);

        try
        {
            token = new TwitterFactory(cb.build()).getInstance().getOAuth2Token();
        }
        catch (Exception e)
        {
            System.out.println("Could not get OAuth2 token");
            e.printStackTrace();
            System.exit(0);
        }

        return token;
    }


/*
      Get a fully application-authenticated Twitter object useful for making subsequent calls.

      @return	Twitter4J Twitter object that's ready for API calls
*/

    private static Twitter getTwitterFeeds()
    {
        OAuth2Token token;

        //	First step, get a "bearer" token that can be used for our requests
        token = getOAuth2Token();

        /*
        	Now, configure our new Twitter object to use application authentication and provide it with
        	our CONSUMER key and secret and the bearer token we got back from Twitter
        */
        ConfigurationBuilder cb = new ConfigurationBuilder();

        cb.setApplicationOnlyAuthEnabled(true);

        cb.setOAuthConsumerKey(CONSUMER_KEY);
        cb.setOAuthConsumerSecret(CONSUMER_SECRET);

        cb.setOAuth2TokenType(token.getTokenType());
        cb.setOAuth2AccessToken(token.getAccessToken());

        //	And create the Twitter object!
        return new TwitterFactory(cb.build()).getInstance();
    }

    /*
     To get the FB feeds, the username, the group Information
     with their feeds.
     @return JSONArray
    */
    public static JSONArray getFBFeeds() {
        JSONArray fb= new JSONArray();
        HashMap<String, String> map= new HashMap<>();;
        System.out.println(me.getName());
        Connection<Post> result = fbClient.fetchConnection("me/feed",Post.class);

        int counter = 0; // set the counter to 0
        try {
            map.put("user", me.getName()); // get the User Name
            fb.put(map);
            for (List<Post> page : result) {
                for (Post aPost : page) {
                    System.out.println(aPost.getMessage());
                    System.out.println("fb.com/" + aPost.getId());
                    map.put("postId", aPost.getId()); // get the postId
                    map.put("message", aPost.getMessage()); // get the message
                    fb.put(map);
                    counter++; // Increment the counter for every Post
                }
            }

            // Get posts from Facebook's Groups
            Connection<Group> groupsFeed = fbClient.fetchConnection("me/groups", Group.class);
            for (List<Group> page : groupsFeed) {
                for (Group aGroup : page) {
                    System.out.println(aGroup.getName());
                    map.put("groupId", aGroup.getId()); // get the groupId
                    map.put("groupName", aGroup.getName()); // get the groupName
                    fb.put(map);
                    System.out.println("fb.com/" + aGroup.getId());
                    counter++; // Increment the counter for every Group
                }
                fb.put(map); // put them into the JSONArray
            }
        }catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("ERROR in parsing Facebook data. " + e);
        }
        return fb;
    }
    /*
     Organise the data in the the respective key value pair
     @return the JSONArray for the Twitter.
    */
    private static JSONArray getIMPData(Status data) {
        JSONObject json = new JSONObject(data);
        try {
            for(int i = 0 ; i < 20 ; i++) {
                map.put("user", data.getUser().getScreenName()); // get the User Name
                map.put("text", data.getText());
                map.put("created_at", data.getCreatedAt().toString()); // get the feed created date
                if (json.has("user"))    // check if data exists or not!!!!
                    map.put("user", data.getUser().getScreenName());
                if (json.has("text"))
                    map.put("text", data.getText());
                if (json.has("created_at"))
                    map.put("created_at", data.getCreatedAt().toString());
                arr.put(map); // add them to arr
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("ERROR in parsing Twitter data. " + e);
        }
        return arr;
    }

    public static JSONArray getTwitterData(){
        int	totalTweets = 0;

        /*
        	This variable is the key to our retrieving multiple blocks of tweets.
        */
        long maxID = -1;

        Twitter twitter = getTwitterFeeds();

        
        try
        {
            /*
            	This returns all the various rate limits in effect for us with the Twitter API
            */

            Map<String, RateLimitStatus> rateLimitStatus = twitter.getRateLimitStatus("search");

            //	This finds the rate limit specifically for doing the search API call we use in this program
            RateLimitStatus searchTweetsRateLimit = rateLimitStatus.get("/search/tweets");


            //	Always nice to see these things when debugging code...
            System.out.printf("You have %d calls remaining out of %d, Limit resets in %d seconds\n",
                    searchTweetsRateLimit.getRemaining(),
                    searchTweetsRateLimit.getLimit(),
                    searchTweetsRateLimit.getSecondsUntilReset());


            //	This is the loop that retrieve multiple blocks of tweets from Twitter
            for (int queryNumber=0;queryNumber < MAX_QUERIES; queryNumber++)
            {
                System.out.printf("\n\n!!! Starting loop %d\n\n", queryNumber);

                if (searchTweetsRateLimit.getRemaining() == 0)
                {
                    
                    System.out.printf("!!! Sleeping for %d seconds due to rate limits\n", searchTweetsRateLimit.getSecondsUntilReset());

                    /*
                     	Adding two seconds seems to do the trick. Even just adding one second still triggers a
                    	rate limit exception more often than not.
                    */
                    Thread.sleep((searchTweetsRateLimit.getSecondsUntilReset()+2) * 1000l);
                }

                Query q = new Query(SEARCH_TERM);			// Search for tweets that contains this term
                q.setCount(TWEETS_PER_QUERY);				// How many tweets, max, to retrieve
                q.setLang("en");							// English language tweets, please

        
                if (maxID != -1)
                {
                    q.setMaxId(maxID - 1);
                }

                //	This actually does the search on Twitter and makes the call across the network
                QueryResult r = twitter.search(q);

                
                if (r.getTweets().isEmpty())
                {
                    break;			// Nothing? We must be done
                }


                /*
                	loop through all the tweets and process them.
                */

                for (Status s: r.getTweets())
                {
                    //	Increment our count of tweets retrieved
                    totalTweets++;

                    /*
                    	Keep track of the lowest tweet ID.  If we do not do this, we cannot retrieve multiple
                    	blocks of tweets...
                    */
                    if (maxID == -1 || s.getId() < maxID)
                    {
                        maxID = s.getId();
                    }

                    //	Do something with the tweet....
                    System.out.printf("At %s, @%-20s said:  %s\n",
                            s.getCreatedAt().toString(),
                            s.getUser().getScreenName(),
                            cleanText(s.getText()));

                    ImpData = getIMPData(s); // call the getIMPData

                }

                searchTweetsRateLimit = r.getRateLimitStatus();
            }

        }
        catch (InterruptedException | TwitterException e)
        {
            //	Catch all -- we're going to read the stack trace and figure out what needs to be done to fix it
            System.out.println("That didn't work well...wonder why?");
            e.printStackTrace();
        }

        System.out.printf("\n\nA total of %d tweets retrieved\n", totalTweets);
        System.out.println("\n" +
                " \n");

        return (JSONArray) ImpData;
    }



    public static void main(String[] args)
    {
        /*
         Call the getTwitterData and getFBFeeds Methods
         and handle the Exception in the catch block
        */
        try {
            getTwitterData();
            Feeds.getFBFeeds();
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("ERROR in getting Data. " + e);
        }
    }
}
