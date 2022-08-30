package com.example.finalyearproject.ui.instructions

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.webkit.WebViewClient
import com.example.finalyearproject.R
import kotlinx.android.synthetic.main.activity_instructions.*
import kotlinx.android.synthetic.main.activity_instructions.view.*

class InstructionsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_instructions)

        var actionBar = supportActionBar

        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_48);
            actionBar.setDisplayHomeAsUpEnabled(true)
        }

        val bundle = intent.extras

        var url = bundle!!.getString("url", "Default")

        if (!url.startsWith("http://") && !url.startsWith("https://")){
            url = "http://" + url;
        }

        webView.webViewClient = object : WebViewClient() {}
        if (url != null) {
            webView.loadUrl(url)
        }
    }

    //override button press for the action bar
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onContextItemSelected(item)
    }

}