package com.nauman.vdownloader;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.JavascriptInterface;
import android.webkit.WebBackForwardList;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebViewClient;

import com.nauman.vdownloader.Tasks.GenerateLink;
import com.nauman.vdownloader.constants.iConstants;
import com.nauman.vdownloader.Helper.BottomNavigationViewHelper;
import com.nauman.vdownloader.utils.AdBlock;
import com.nauman.vdownloader.utils.IOUtils;
import com.nauman.vdownloader.utils.iUtils;

import java.io.ByteArrayInputStream;


public class MainActivity extends Activity implements iConstants {
    private WebView webView;
    private ProgressBar progressBar;
    private float m_downX;
    final Activity activity = this;
    FloatingActionButton fab;
    private boolean AdblockEnabled = true;
    private BottomNavigationView bottomNavigationView;
    private CountDownTimer timer;
    private Boolean SearchHasFocus = false;
    private View bottomSheet;
    private View webViewCon;
    SharedPreferences sharedPrefs;

    int count=0;

    public static final int REQUEST_PERMISSION_CODE = 1001;
    public static final String REQUEST_PERMISSION = android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = (WebView) findViewById(R.id.webView);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        bottomSheet = findViewById(R.id.design_bottom_sheet);
        webViewCon=(View)findViewById(R.id.webViewCon);

        bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation);
        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);
        OnPrepareBottomNav(bottomNavigationView.getMenu());
        sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this);

        fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setVisibility(View.VISIBLE);
        webView.loadUrl("http://youtube.com");

        AdblockEnabled=sharedPrefs.getBoolean("ADBLOCK",true);
        bottomNavigationView.setOnNavigationItemSelectedListener
                (new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                        switch (item.getItemId()) {

                            case R.id.action_back:
                                back();
                                break;
                            case R.id.action_download:
                                startActivity(new Intent("android.intent.action.VIEW_DOWNLOADS"));
                                break;
                            case R.id.action_rateus:
                                Uri uri = Uri.parse("market://details?id=" + MainActivity.this.getPackageName());
                                Intent openStore = new Intent(Intent.ACTION_VIEW, uri);
                                openStore.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                                        Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                                try {
                                    startActivity(openStore);
                                } catch (ActivityNotFoundException e) {
                                    startActivity(new Intent(Intent.ACTION_VIEW,
                                            Uri.parse("http://play.google.com/store/apps/details?id=" + MainActivity.this.getPackageName())));
                                }
                                break;
                            case R.id.action_forward:
                                forward();
                                break;
                        }
                        return true;
                    }
                });

        //WebView
        initWebView();

        isNeedGrantPermission();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GenerateLink.Start(MainActivity.this,webView.getUrl(),webView.getTitle());

            }
        });

        timer = new CountDownTimer(2000, 20) {

            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                try{

                }catch(Exception e){
                    Log.e("TimerError", "Error: " + e.toString());
                }
            }
        }.start();

    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void initWebView() {
        webView.setWebChromeClient(new MyWebChromeClient(this));
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(View.VISIBLE);
                OnPrepareBottomNav(bottomNavigationView.getMenu());

            }
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);

                OnPrepareBottomNav(bottomNavigationView.getMenu());
            }

            @Deprecated
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {

                if (AdblockEnabled && new AdBlock(MainActivity.this).isAd(url)) {
                    return new WebResourceResponse(
                            "text/plain",
                            "UTF-8",
                            new ByteArrayInputStream("".getBytes())
                    );
                }

                return super.shouldInterceptRequest(view, url);
            }

            @TargetApi(Build.VERSION_CODES.N)
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (AdblockEnabled &&  new  AdBlock(MainActivity.this).isAd(request.getUrl().toString())) {
                        return new WebResourceResponse(
                                "text/plain",
                                "UTF-8",
                                new ByteArrayInputStream("".getBytes())
                        );
                    }
                }

                return super.shouldInterceptRequest(view, request);
            }
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                progressBar.setVisibility(View.GONE);
                OnPrepareBottomNav(bottomNavigationView.getMenu());
            }
        });
        webView.clearCache(true);
        webView.clearHistory();
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setPluginState(WebSettings.PluginState.ON);
        webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        webView.setHorizontalScrollBarEnabled(false);
        WebSettings webSettings = webView.getSettings();
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setDomStorageEnabled(true);
        webView.addJavascriptInterface(new MyJavaScriptInterface(),
                "android");
        webView.setWebChromeClient(new WebChromeClient() {});

        webView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getPointerCount() > 1) {
                    //Multi touch detected
                    return true;
                }
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        // save the x
                        m_downX = event.getX();
                    }
                    break;

                    case MotionEvent.ACTION_MOVE:
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP: {
                        // set x so that it doesn't move
                        event.setLocation(m_downX, event.getY());
                    }
                    break;
                }

                return false;
            }
        });

    }
    class MyJavaScriptInterface {
        @JavascriptInterface
        public void onUrlChange(String url) {

        }
    }

    public boolean OnPrepareBottomNav(Menu menu){

        if (!webView.canGoBack()) {
            menu.getItem(0).setEnabled(false);
            menu.getItem(0).getIcon().setAlpha(130);
        } else {
            menu.getItem(0).setEnabled(true);
            menu.getItem(0).getIcon().setAlpha(255);
        }
        if (!webView.canGoForward()) {
            menu.getItem(3).setEnabled(false);
            menu.getItem(3).getIcon().setAlpha(130);
        } else {
            menu.getItem(3).setEnabled(true);
            menu.getItem(3).getIcon().setAlpha(255);
        }

        return true;
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

         return true;
    }

    private void back() {
        if (webView.canGoBack()) {
            WebBackForwardList mWebBackForwardList = webView.copyBackForwardList();

            String historyUrl = mWebBackForwardList.getItemAtIndex(mWebBackForwardList.getCurrentIndex()-1).getUrl();

            webView.goBack();
//            SearchText.setText(historyUrl);
            webView.loadUrl(historyUrl);

        }
    }
    private void forward() {
        if (webView.canGoForward()) {
            WebBackForwardList mWebBackForwardList = webView.copyBackForwardList();
            String historyUrl = mWebBackForwardList.getItemAtIndex(mWebBackForwardList.getCurrentIndex()+1).getUrl();
            webView.goForward();
//            SearchText.setText(webView.getUrl());
            webView.loadUrl(webView.getUrl());
        }
    }
    private class MyWebChromeClient extends WebChromeClient {
        Context context;

        public MyWebChromeClient(Context context) {
            super();
            this.context = context;
        }


    }

    private boolean isNeedGrantPermission() {
        try {
            if (IOUtils.hasMarsallow()) {
                if (ContextCompat.checkSelfPermission(this, REQUEST_PERMISSION) != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, REQUEST_PERMISSION)) {
                        final String msg = String.format(getString(R.string.format_request_permision), getString(R.string.app_name));

                        AlertDialog.Builder localBuilder = new AlertDialog.Builder(MainActivity.this);
                        localBuilder.setTitle("Permission Required!");
                        localBuilder
                                .setMessage(msg).setNeutralButton("Grant",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(
                                            DialogInterface paramAnonymousDialogInterface,
                                            int paramAnonymousInt) {
                                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{REQUEST_PERMISSION}, REQUEST_PERMISSION_CODE);
                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(
                                            DialogInterface paramAnonymousDialogInterface,
                                            int paramAnonymousInt) {

                                        paramAnonymousDialogInterface.dismiss();
                                        finish();
                                    }
                                });
                        localBuilder.show();

                    }
                    else {
                        ActivityCompat.requestPermissions(this, new String[]{REQUEST_PERMISSION}, REQUEST_PERMISSION_CODE);
                    }
                    return true;
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        try {
            if (requestCode == REQUEST_PERMISSION_CODE) {
                if (grantResults != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                }
                else {
                    iUtils.ShowToast(MainActivity.this,getString(R.string.info_permission_denied));

                    finish();
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            iUtils.ShowToast(MainActivity.this,getString(R.string.info_permission_denied));
            finish();
        }

    }

    @Override
    public void onResume(){
        super.onResume();
        // put your code here...
        AdblockEnabled=sharedPrefs.getBoolean("ADBLOCK",true);
        webView.onResume();
        webView.resumeTimers();
    }
    @Override
    public void onPause(){
        super.onPause();
        // put your code here...
        webView.onPause();
        webView.pauseTimers();

    }
    @Override
    public void onStop(){
        super.onStop();

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        webView.loadUrl("about:blank");
        webView.stopLoading();
        webView.setWebChromeClient(null);
        webView.setWebViewClient(null);
        webView.destroy();
        webView=null;
     }

    public void onBackPressed(){

    }

}