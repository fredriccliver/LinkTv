package linktv.linktv;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class User {
    public String fid = "";
    public String cookies = "";
    public String userAgent = "";

    public void setUserAgent(String user_agent) {
        userAgent = user_agent;
    }

    public void parseFBCookie(String cookie_str) {

        Log.d("tag", "Cookies string:  " + cookie_str);

        if (cookie_str.contains("datr=") &&
                cookie_str.contains("c_user=") &&
                cookie_str.contains("lu=") &&
                cookie_str.contains("fr=") &&
                cookie_str.contains("s=") &&
                cookie_str.contains("xs=")
                ) {

            try {
                fid = extractCookieValue(cookie_str, "c_user");
            } catch (Exception e) {
                Log.d("tag", "error : " + e.toString());
            }

            cookies = URLEncoder.encode(cookie_str);
            CookieSyncManager.getInstance().sync();
        }
    }

    private String extractCookieValue(String rawCookie, String cookie_name) throws Exception {

        Map<String, String> Cookies = new HashMap<String, String>();

        String[] rawCookieParams = rawCookie.split(";");

        for (int i = 0; i < rawCookieParams.length; i++) {
            String[] rawCookieNameAndValue = rawCookieParams[i].split("=");
            Cookies.put(rawCookieNameAndValue[0].trim(), rawCookieNameAndValue[1].trim());
        }

        String cookie_val = Cookies.get(cookie_name);

        return cookie_val;

    }

}
