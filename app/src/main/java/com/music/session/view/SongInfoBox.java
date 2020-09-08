package com.music.session.view;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.music.session.R;
import com.music.session.model.Audio;


class SongInfoBox extends PopupWindow {

    private Context context;
    private View layout;
    private TextView songInfoName;
    private ImageView clipartImg;

    SongInfoBox (Context context) {
        super(context);
        this.context = context;
    }

    void show(Audio audio, float yPosition) {

        if (context == null)
            return;

        LayoutInflater layoutInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layout = layoutInflater.inflate(R.layout.song_info_dialog, null);
        songInfoName = layout.findViewById(R.id.song_info_name);
        songInfoName.setText(("Title: " + audio.getTitle() +
                              "\nArtist: " + audio.getArtist() +
                                "\nAlbum: " + audio.getAlbum()));
        clipartImg = layout.findViewById(R.id.clip_art);
        if (audio.getClipArt() != null) {
            clipartImg.setImageBitmap(audio.getClipArt());
        } else {
            clipartImg.setVisibility(View.GONE);
        }
        setContentView(layout);
        setFocusable(true);

        Log.v("imri","x = "  + " round = " + Math.round(yPosition));
        showAtLocation(layout, Gravity.NO_GRAVITY, 100, Math.round(yPosition) + 20);

    }

}