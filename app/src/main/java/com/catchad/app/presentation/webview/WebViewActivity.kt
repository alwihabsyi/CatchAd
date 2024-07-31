package com.catchad.app.presentation.webview

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.catchad.app.databinding.ActivityWebViewBinding

class WebViewActivity : AppCompatActivity() {

    private var _binding: ActivityWebViewBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityWebViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setView()
    }

    private fun setView() {
        intent.getStringExtra("contentUrl")?.let {
            loadContent(it)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun loadContent(url: String) {
        val contentHtml = """
            <html>
            <head>
                <style>
                    body {
                        margin: 0;
                        padding: 0;
                        display: flex;
                        justify-content: center;
                        align-items: center;
                        height: 100vh;
                    }
                    img, video {
                        max-width: 100%;
                        max-height: 100%;
                        object-fit: contain;
                    }
                </style>
            </head>
            <body>
                <img src="$url" />
            </body>
            </html>
        """

        binding.webView.apply {
            settings.javaScriptEnabled = true
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true
            loadDataWithBaseURL(null, contentHtml, "text/html", "UTF-8", null)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}