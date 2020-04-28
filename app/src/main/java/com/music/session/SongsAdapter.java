package com.music.session;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.MyViewHolder> {
    private final Context mContext;
    private List<Audio> mSongsData;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
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
            image_clipart = v.findViewById(R.id.image_clipart);
        }
    }

    public SongsAdapter(List<Audio> songsData, Context context) {
        mSongsData = songsData;
        mContext = context;
    }

    @Override
    public SongsAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                        int viewType) {
        View v =  LayoutInflater.from(parent.getContext())
                .inflate(R.layout.songs_recycler_item_constraint, parent, false);
        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        String artistName = mSongsData.get(position).getArtist();
        boolean isNoArtistName = android.text.TextUtils.isDigitsOnly(artistName);
        String songTitle = mSongsData.get(position).getTitle();
        Bitmap bitMapClipArt = mSongsData.get(position).getClipArt();
        holder.textView2.setText(songTitle);
        holder.textView3.setText(artistName);
        if (bitMapClipArt != null) {
            holder.image_clipart.setImageBitmap(bitMapClipArt);
            Log.v("imri", "imri exc if");
        } else {
            holder.image_clipart.setImageDrawable(mContext.getDrawable(R.drawable.ic_action_name));
            Log.v("imri", "imri exc else");
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
        return mSongsData.size();
    }
}
