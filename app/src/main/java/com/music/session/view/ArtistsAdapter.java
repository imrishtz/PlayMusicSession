package com.music.session.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.music.session.R;
import com.music.session.model.SongIndexing;
import com.music.session.model.SongsList;

import java.util.ArrayList;

public class ArtistsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {
    private final Context mContext;
    private ArrayList<SongIndexing> mSongsData;
    private final static String TAG = "SongsAdapter";
    private ArrayList<Object> showList;
    private static final int SONG_HOLDER_TYPE = 396;
    private static final int ARTIST_HOLDER_TYPE = 397;


    public static class SongViewHolder extends RecyclerView.ViewHolder {
        private View ViewRec;
        private TextView textView2;
        private TextView textView3;
        private ImageView image_clipart;
        private SongViewHolder(View v) {
            super(v);
            ViewRec = v;
            textView2 = v.findViewById(R.id.text1);
            textView3 = v.findViewById(R.id.text2);
            image_clipart = v.findViewById(R.id.image_clipart);
        }
    }

    public static class ArtistViewHolder extends RecyclerView.ViewHolder {
        private TextView artistName;
        private ArtistViewHolder(View v) {
            super((v));
            artistName = v.findViewById(R.id.artist_header);
            v.setClickable(false);
        }
    }
    public ArtistsAdapter(ArrayList<SongIndexing> songsData, Context context) {
        Log.i(TAG, "imri ArtistsAdapter");
        mSongsData = songsData;
        fillArtistList();
        mContext = context;
    }
    @Override
    public int getItemViewType(int position) {
        int type;
        Object object = showList.get(position);
        if (object instanceof SongIndexing) {
            type = SONG_HOLDER_TYPE;
        } else {
            type = ARTIST_HOLDER_TYPE;
        }
        return type;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                      int viewType) {
        View vh;
        if (viewType == ARTIST_HOLDER_TYPE) {
            vh = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.artist_recycler_item, parent, false);
            return new ArtistsAdapter.ArtistViewHolder(vh);
        } else { //SONG_HOLDER_TYPE:
            vh =  LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.songs_recycler_item_constraint, parent, false);
            return new ArtistsAdapter.SongViewHolder(vh);
        }
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder.getItemViewType() == SONG_HOLDER_TYPE) {
            SongViewHolder songViewHolder = (SongViewHolder) holder;
            SongIndexing songIndexing = (SongIndexing) showList.get(position);
            String albumName = songIndexing.getAlbum();
            String songTitle = songIndexing.getTitle();
            Bitmap bitMapClipArt = songIndexing.getClipArt();
            songViewHolder.textView2.setText(songTitle);
            songViewHolder.textView3.setText(albumName);
            if (bitMapClipArt != null) {
                songViewHolder.image_clipart.setImageBitmap(bitMapClipArt);
            } else {
                songViewHolder.image_clipart.setImageDrawable(mContext.getDrawable(R.drawable.ic_no_art));
            }

            songViewHolder.ViewRec.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                }
            });
            songViewHolder.ViewRec.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    return true;
                }
            });
        } else { // ARTIST_HOLDER_TYPE
            ArtistViewHolder artistViewHolder = (ArtistViewHolder) holder;
            String artistName = (String)showList.get(position);
            artistViewHolder.artistName.setText(artistName);
        }

    }

    @Override
    public int getItemCount() {
        return showList.size();
    }

    private void fillArtistList() {
        showList = new ArrayList<>();
        String lastArtist = "-1";
        for (int i = 0; i < mSongsData.size(); ++i) {
            int relevantPosition = SongsList.getRealIndexFromArtist(i);
            String artist = mSongsData.get(relevantPosition).getArtist();
            if (!artist.equals(lastArtist)) {
                showList.add(artist);
                lastArtist = artist;
            }
            showList.add(mSongsData.get(relevantPosition));

        }
    }
    public int getSongToPlay(int position) {
        Object o = showList.get(position);
        if (o instanceof SongIndexing) {
            return ((SongIndexing)o).getRealIndex();
        } else {
            return -1;
        }
    }
}
