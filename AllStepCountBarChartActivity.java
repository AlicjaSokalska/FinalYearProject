package com.example.testsample;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;
public class AllStepCountBarChartActivity extends AppCompatActivity {

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_step_count_bar_chart);

        WebView chartWebView = findViewById(R.id.chartWebView);
        WebSettings webSettings = chartWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        // Retrieve chart data from Intent
        String chartData = getIntent().getStringExtra("chartData");

        // Generate QuickChart URL
        String quickChartUrl = "https://quickchart.io/chart?c=" + chartData;

        // Load the QuickChart URL in the WebView
        chartWebView.loadUrl(quickChartUrl);

        // Enable zooming
        chartWebView.getSettings().setBuiltInZoomControls(true);
        chartWebView.getSettings().setDisplayZoomControls(false);

        // Enable JavaScript alerts
        chartWebView.setWebChromeClient(new WebChromeClient());

        // Set WebView client to resize the chart after page load is complete
        chartWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                view.setInitialScale(1); // Set initial scale
                view.getSettings().setLoadWithOverviewMode(true); // Load content with overview mode
                view.getSettings().setUseWideViewPort(true); // Use wide viewport
            }
        });
    }

    private void resizeChartToFit(WebView webView) {
        webView.loadUrl("javascript:(function() { " +
                "var chart = document.getElementsByTagName('canvas')[0];" +
                "var container = chart.parentNode;" +
                "chart.style.width = '" + webView.getWidth() + "px';" +
                "chart.style.height = '" + webView.getHeight() + "px';" +
                "})()");
    }
}
