package com.music.session.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.music.session.Constants;
import com.music.session.R;
import com.music.session.model.Audio;
import com.music.session.model.SongsListSongs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.MyViewHolder>  {
    private final Context mContext;
    private final static String TAG = "SongsAdapter";
    private SongsListSongs mSongsListInstance;

    public Audio getAudio(int position) {
        return mSongsListInstance.getSong(position);
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        private View ViewRec;
        private TextView textView2;
        private TextView textView3;
        private ImageView image_clipart;
        private MyViewHolder(View v) {
            super(v);
            ViewRec = v;
            textView2 = v.findViewById(R.id.text1);
            textView3 = v.findViewById(R.id.text2);
            Log.i(TAG, "MyViewHolder: imri screen=" + Constants.screenWidth);
            if (Constants.screenWidth < 600) {
                InputFilter[] fArray = new InputFilter[1];
                fArray[0] = new InputFilter.LengthFilter(45);
                textView2.setFilters(fArray);
                textView3.setFilters(fArray);
                ViewGroup.LayoutParams paramSong = textView2.getLayoutParams();
                paramSong.width = 360;
                textView2.setLayoutParams(paramSong);
                ViewGroup.LayoutParams paramArtist = textView3.getLayoutParams();
                paramArtist.width = 360;
                textView3.setLayoutParams(paramArtist);
                textView2.setTextSize(14);
                textView3.setTextSize(12);
            } else if (Constants.screenWidth < 700) {
                textView2.setTextSize(16);
                textView3.setTextSize(14);

            } else if (Constants.screenWidth > 1800) {
                InputFilter[] fArray = new InputFilter[1];
                fArray[0] = new InputFilter.LengthFilter(80);
                textView2.setFilters(fArray);
                textView2.setEllipsize(TextUtils.TruncateAt.END);
                ViewGroup.LayoutParams paramSong = textView2.getLayoutParams();
                paramSong.width = 1600;
                textView2.setLayoutParams(paramSong);
                textView2.setTextSize(21);
                textView3.setTextSize(19);
            } else if (Constants.screenWidth > 1200) {
                ViewGroup.LayoutParams paramSong = textView2.getLayoutParams();
                paramSong.width = 1100;
                textView2.setLayoutParams(paramSong);
                textView2.setTextSize(20);
                textView3.setTextSize(17);
            }
            image_clipart = v.findViewById(R.id.image_clipart);
        }
    }

    SongsAdapter(Context context) {
        mContext = context;
        mSongsListInstance = new SongsListSongs(mContext);
    }

    @Override
    public SongsAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                        int viewType) {
        View v =  LayoutInflater.from(parent.getContext())
                .inflate(R.layout.songs_recycler_item_constraint, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        Audio audio = mSongsListInstance.getSong(position);
        String artistName = audio.getArtist();
        String songTitle = audio.getTitle();
        Bitmap bitMapClipArt = audio.getClipArt();
        holder.textView2.setText(songTitle);
        holder.textView3.setText(artistName);
        if (bitMapClipArt != null) {
            holder.image_clipart.setImageBitmap(bitMapClipArt);
        } else {
            holder.image_clipart.setImageDrawable(mContext.getDrawable(R.drawable.ic_no_art));
        }

        holder.ViewRec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });
        holder.ViewRec.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                return true;
            }
        });

    }

    @Override
    public int getItemCount() {
        return mSongsListInstance.getSize();
    }
}
