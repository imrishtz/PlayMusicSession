package com.music.session;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.MyViewHolder> {
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

    public SongsAdapter(List<Audio> songsData) {
        mSongsData = songsData;
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
        holder.image_clipart.setImageBitmap(bitMapClipArt);

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
