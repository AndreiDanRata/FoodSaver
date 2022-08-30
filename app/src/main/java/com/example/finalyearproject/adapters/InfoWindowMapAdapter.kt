package com.example.finalyearproject.adapters

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.example.finalyearproject.R
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

/**
 * Class for map fragment: creates the marker with the firebase data
 */
class InfoWindowMapAdapter(mContext: Context) : GoogleMap.InfoWindowAdapter {
    var mWindow: View = LayoutInflater.from(mContext).inflate(R.layout.info_window_maps, null)

    private fun setInfoWindowText(marker: Marker) {
        val title = marker.title
        val snippet = marker.snippet
        val tvTitle = mWindow.findViewById<TextView>(R.id.title_textView)
        val tvSnippet = mWindow.findViewById<TextView>(R.id.snippet_textView)

        if (!TextUtils.isEmpty(title)) {
            tvTitle.text = title
            tvTitle.setTextColor(Color.BLACK)
            tvTitle.setTypeface(null, Typeface.BOLD)
        }

        if (!TextUtils.isEmpty(snippet)) {
            tvSnippet.text = marker.snippet
            tvSnippet.setTextColor(Color.BLUE)
        }
    }

    override fun getInfoWindow(p0: Marker): View {
        setInfoWindowText(p0)
        return mWindow
    }

    override fun getInfoContents(p0: Marker): View {
        setInfoWindowText(p0)
        return mWindow
    }
}