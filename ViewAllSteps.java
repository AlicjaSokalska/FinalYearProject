package com.example.testsample;

import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

public class ViewAllSteps extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_all_steps);

        // Dummy step count data
        int dailyStepCount = 1000; // Dummy data for daily step count
        int weeklyStepCount = 7000; // Dummy data for weekly step count
        int allStepCount = 30000; // Dummy data for all step count

        // Load daily step count chart into WebView
        WebView webViewDaily = findViewById(R.id.webViewDailyStep);
        loadChartIntoWebView(webViewDaily, "Daily Step Count", dailyStepCount);

        // Load weekly step count chart into WebView
        WebView webViewWeekly = findViewById(R.id.webViewWeeklyStep);
        loadChartIntoWebView(webViewWeekly, "Weekly Step Count", weeklyStepCount);

        // Load all step count chart into WebView
        WebView webViewAll = findViewById(R.id.webViewAllStep);
        loadChartIntoWebView(webViewAll, "All Step Count", allStepCount);
    }

    private void loadChartIntoWebView(WebView webView, String label, int stepCount) {
        String chartUrl = "https://quickchart.io/chart?c=" + generateChartData(label, stepCount);
        webView.loadUrl(chartUrl);

        // Configure WebView settings
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true); // Enable JavaScript
        webSettings.setBuiltInZoomControls(true); // Enable zoom controls
        webSettings.setDisplayZoomControls(false); // Hide zoom controls
        webView.setWebChromeClient(new WebChromeClient()); // Enable JavaScript alerts
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                view.setInitialScale(1); // Set initial scale
                view.getSettings().setLoadWithOverviewMode(true); // Load content with overview mode
                view.getSettings().setUseWideViewPort(true); // Use wide viewport
            }
        });
    }

    private String generateChartData(String label, int stepCount) {
        return "{\"type\":\"bar\",\"data\":{\"labels\":[\"" + label + "\"],\"datasets\":[{\"label\":\"Steps\",\"data\":[" + stepCount + "]}]},\"options\":{\"scales\":{\"xAxes\":[{\"scaleLabel\":{\"display\":true,\"labelString\":\"Steps\"}}]}}}";
    }
}
