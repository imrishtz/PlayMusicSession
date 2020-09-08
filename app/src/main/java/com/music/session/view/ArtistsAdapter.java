package com.music.session.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.music.session.Constants;
import com.music.session.R;
import com.music.session.model.Audio;
import com.music.session.model.SongsListArtists;

import java.util.ArrayList;

public class ArtistsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements HeaderItemDecoration.StickyHeaderInterface {
    private ArrayList<Object> showList;
    private static final int SONG_HOLDER_TYPE = 396;
    private static final int ARTIST_HOLDER_TYPE = 397;
    private static final int DIVIDER_HOLDER_TYPE = 398;
    private SongsListArtists mArtistsListInstance;

    ArtistsAdapter(Context context) {
        mArtistsListInstance = new SongsListArtists(context);
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
        setSizeByScreenWidth(tvName);
        tvName.setText(child);
    }
    private static void setSizeByScreenWidth(TextView tv) {
        if (Constants.screenWidth < 600) {
            tv.setTextSize(22);
        } else if (Constants.screenWidth < 800) {
            tv.setTextSize(25);
        }
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
                textView2.setTextSize(15);
                textView3.setTextSize(12);
            } else if (Constants.screenWidth < 700) {
                textView2.setTextSize(16);
                textView3.setTextSize(14);
            } else if (Constants.screenWidth > 1800) {
                InputFilter[] fArray = new InputFilter[1];
                fArray[0] = new InputFilter.LengthFilter(80);
                textView2.setFilters(fArray);
                textView2.setEllipsize(TextUtils.TruncateAt.END);
                textView3.setFilters(fArray);
                ViewGroup.LayoutParams paramSong = textView2.getLayoutParams();
                paramSong.width = 1600;
                textView2.setLayoutParams(paramSong);
                ViewGroup.LayoutParams paramArtist = textView3.getLayoutParams();
                paramArtist.width = 1600;
                textView3.setLayoutParams(paramArtist);
                textView2.setTextSize(21);
                textView3.setTextSize(18);
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

    private static class ArtistViewHolder extends RecyclerView.ViewHolder {
        private TextView artistName;
        private ArtistViewHolder(View v) {
            super((v));
            artistName = v.findViewById(R.id.artist_header);
            setSizeByScreenWidth(artistName);
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
                songViewHolder.image_clipart.setImageDrawable(songViewHolder.image_clipart.getContext().getDrawable(R.drawable.ic_no_art));
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
