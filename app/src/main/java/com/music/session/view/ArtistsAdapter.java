package com.music.session.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.music.session.R;
import com.music.session.model.Audio;
import com.music.session.model.SongsListArtists;

import java.util.ArrayList;

public class ArtistsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements HeaderItemDecoration.StickyHeaderInterface {
    private final Context mContext;
    private ArrayList<Object> showList;
    private static final int SONG_HOLDER_TYPE = 396;
    private static final int ARTIST_HOLDER_TYPE = 397;
    private static final int DIVIDER_HOLDER_TYPE = 398;
    private SongsListArtists mArtistsListInstance;

    ArtistsAdapter(Context context) {
        mContext = context;
        mArtistsListInstance = new SongsListArtists(mContext);
        fillArtistList();
    }
    @Override
    public int getHeaderPositionForItem(int itemPosition) {
        int headerPosition = 0;
        do {
            if (this.isHeader(itemPosition)) {
                headerPosition = itemPosition;
                break;
            }
            itemPosition -= 1;
        } while (itemPosition >= 0);
        return headerPosition;
    }

    @Override
    public int getHeaderLayout(int headerPosition) {
        return R.layout.artist_recycler_item;
    }

    @Override
    public void bindHeaderData(View header, int headerPosition) {
        String child = (String)showList.get(headerPosition);
        TextView tvName = header.findViewById(R.id.artist_header);
        tvName.setText(child);
    }

    @Override
    public boolean isHeader(int itemPosition) {
        Object o = showList.get(itemPosition);
        return (o instanceof String);
    }


    private static class SongViewHolder extends RecyclerView.ViewHolder {
        private TextView textView2;
        private TextView textView3;
        private ImageView image_clipart;
        private SongViewHolder(View v) {
            super(v);
            textView2 = v.findViewById(R.id.text1);
            textView3 = v.findViewById(R.id.text2);
            image_clipart = v.findViewById(R.id.image_clipart);
        }
    }

    private static class ArtistViewHolder extends RecyclerView.ViewHolder {
        private TextView artistName;
        private ArtistViewHolder(View v) {
            super((v));
            artistName = v.findViewById(R.id.artist_header);
            v.setClickable(false);
        }
    }
    private static class DividerViewHolder extends RecyclerView.ViewHolder {
        private DividerViewHolder(View v) {
            super((v));
            v.setClickable(false);
        }
    }

    @Override
    public int getItemViewType(int position) {
        int type;
        Object object = showList.get(position);
        if (object instanceof ArtistAudio) {
            type = SONG_HOLDER_TYPE;
        } else if (object instanceof  String) {
            type = ARTIST_HOLDER_TYPE;
        } else {
            type = DIVIDER_HOLDER_TYPE;
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
        } else if (viewType == SONG_HOLDER_TYPE) { //SONG_HOLDER_TYPE:
            vh =  LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.songs_recycler_item_constraint, parent, false);
            return new ArtistsAdapter.SongViewHolder(vh);
        } else { // DIVIDER_HOLDER_TYPE
            vh =  LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.divider, parent, false);
            return new  DividerViewHolder(vh);
        }
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder.getItemViewType() == SONG_HOLDER_TYPE) {
            SongViewHolder songViewHolder = (SongViewHolder) holder;
            Audio audio = ((ArtistAudio) showList.get(position)).mAudio;
            String albumName = audio.getAlbum();
            String songTitle = audio.getTitle();
            Bitmap bitMapClipArt = audio.getClipArt();
            songViewHolder.textView2.setText(songTitle);
            songViewHolder.textView3.setText(albumName);
            if (bitMapClipArt != null) {
                songViewHolder.image_clipart.setImageBitmap(bitMapClipArt);
            } else {
                songViewHolder.image_clipart.setImageDrawable(mContext.getDrawable(R.drawable.ic_no_art));
            }
        } else if (holder.getItemViewType() == ARTIST_HOLDER_TYPE) {
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
        int counter = 0;
        for (int i = 0; i < mArtistsListInstance.getSize(); ++i) {
            Audio audio = mArtistsListInstance.getSong(i);
            String artist = audio.getArtist();
            if (!artist.equals(lastArtist)) {
                showList.add(artist);
                lastArtist = artist;
                ++counter;
            }
            showList.add(new ArtistAudio(counter, audio, i));
            showList.add(true); // Divider
        }
    }
    int getSongToPlay(int position) {

        Object o = showList.get(position);
        if (o instanceof ArtistAudio) {
            return position - ((ArtistAudio) o).artistsToCount - ((ArtistAudio) o).dividerToCount;
        } else {
            return -1;
        }
    }
    class ArtistAudio {
        ArtistAudio(int counter,Audio audio, int dividerCount) {
            this.mAudio = audio;
            this.artistsToCount = counter;
            this.dividerToCount = dividerCount;
        }
        int artistsToCount;
        int dividerToCount;
        Audio mAudio;
    }

}
