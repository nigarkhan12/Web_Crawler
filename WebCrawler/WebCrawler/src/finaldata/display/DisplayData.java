package finaldata.display;

import twitter4j.JSONObject;
import twitter4j.JSONArray;
import twitterfb.feeds.Feeds;
import twitter4j.JSONException;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import javax.servlet.http.HttpServlet;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@WebServlet(name = "DisplayData")
public class DisplayData extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();

        /*
         create the object of Feeds and
         call it according to the requirement
         i.e. getFBFeeds() and  getTwitterData() methods.
        */

        JSONArray fbdata  = Feeds.getFBFeeds();
        JSONArray twitterdata  = Feeds.getTwitterData();

        /*
         Set the page With the Background color Aqua
         and display the values
        */

        out.println("<html>");
        out.println("<head></head>");
        out.println("<body bgcolor='Aqua'>");
        out.println("<br/>");

        /*
         Fetch the data from the Module fetch.impdata
         and stored it in JSONArray in the corresponding
         objects i.e. fbdata and twitterdata.
         Iterate it and display the data accordingly.
        */
        try{
            for (int i = 0; i < fbdata.length(); i++) {
                JSONObject objects = fbdata.getJSONObject(i);
                Iterator key = objects.keys();
                while (key.hasNext()) {
                    String k = key.next().toString();
                    out.println("User:" +objects.getString("user"));
                    out.println("had posted");
                    out.println("<br/>");
                    out.println("postId:" +objects.getString("postId"));
                    out.println("<br/>");
                    out.println("message:" +objects.getString("message"));
                    out.println("<br/>");
                    out.println("The group "+i);
                    out.println("groupId:" +objects.getString("groupId"));
                    out.println("<br/>");
                    out.println("Groupname");
                    out.println("groupName:" +objects.getString("groupName"));
                    out.println("<br/>");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            throw new RuntimeException(
                    "ERROR: Didn't get Data.");
        }
        try{
            for (int i = 0; i < twitterdata.length(); i++) {
                JSONObject objects = twitterdata.getJSONObject(i);
                Iterator key = objects.keys();
                while (key.hasNext()) {
                    String k = key.next().toString();
                    out.println("user:" +objects.getString("user"));
                    out.println("<br/>");
                    out.println("text:" +objects.getString("text"));
                    out.println("<br/>");
                    out.println("created_at:" +objects.getString("created_at"));
                    out.println("<br/>");
                    out.println("link:" +objects.getString("link"));
                    out.println("<br/>");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            throw new RuntimeException(
                    "ERROR: Didn't get Data.");
        }

        /* closing the body and html tag */
        out.println("</body></html>");

    }
    }
