package com.akshat.earthquako.UI;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.akshat.earthquako.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class CustomInfoWindow implements GoogleMap.InfoWindowAdapter {
    private View view;
    private LayoutInflater layoutInflater;
    private Context context;
    TextView title,mag;

    public CustomInfoWindow(Context context) {
        this.context=context;
        layoutInflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view=layoutInflater.inflate(R.layout.custom_info_window,null);
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        title=(TextView) view.findViewById(R.id.winTitle);
        title.setText(marker.getTitle());
        mag=(TextView) view.findViewById(R.id.magnitude);
        mag.setText(marker.getSnippet());
        return view;
    }
}
