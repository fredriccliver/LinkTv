package linktv.linktv;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends Activity {

    public WebView wv;
    int visit_cnt = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        wv = (WebView)findViewById(R.id.wv);
        wv.setWebViewClient(new WebViewClient(){

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                try
                {
                    String get_host = new URL(url).getHost();
                    Log.d("linktv",get_host);
                    if(!get_host.equals("alinktv.org")){
                        //visit_add();
                    }
                }
                catch (MalformedURLException e)
                {
                    // do something
                }
                return false;
            }
        });

        // 웹뷰에서 자바스크립트실행가능
        wv.getSettings().setJavaScriptEnabled(true);
        // 구글홈페이지 지정
        wv.loadUrl("http://alinktv.org/");
        // WebViewClient 지정
        // 각종 알림 및 요청을 받게되는 WebViewClient를 설정합니다. - option

//        Intent mLoginIntent = new Intent(this, LoginIntent.class);
//        this.startActivity(mLoginIntent);

    }

//    public void visit_add(){
//        visit_cnt = visit_cnt+1;
//
//        // 2회째가 아니고 바로 뜨도록 변경함 >1 에서 >=1 로 변경. 정원석 160726
//        if(visit_cnt>=1){
//            if(getSharedPreferences("pref", MODE_PRIVATE).getString("login", "no").equals("no")){
//                Intent mLoginIntent = new Intent(this, LoginIntent.class);
//                this.startActivity(mLoginIntent);
//
//            }else{
//
//            }
//
//            Log.d("linktv",String.valueOf(visit_cnt));
//
//        }
//    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK) {
            if(wv.canGoBack()) {
                wv.goBack();
            }else{
                finish();   //엑티비티종료
            }

        }
        return false;
    }
}
