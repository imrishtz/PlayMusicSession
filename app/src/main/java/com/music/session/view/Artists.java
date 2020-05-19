package com.music.session.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.music.session.R;
import com.music.session.model.MediaPlayerService;
import com.music.session.model.SongIndexing;
import com.music.session.model.SongsList;

import java.util.ArrayList;

public class Artists extends Fragment {
    AlertDialog.Builder songInfoDialog;
    String TAG = "Artists";
    View view;
    private float[] lastTouchDownXY = new float[2];
    private RecyclerView recyclerView;
    private ArtistsAdapter mArtistsAdapter;
    private RecyclerView.LayoutManager layoutManager;
    public ArrayList<SongIndexing> mArtistsList;
    SongsList mArtistsListInstance = SongsList.getInstance();

    public Artists(Context context) {
        mArtistsList = mArtistsListInstance.getAllSongs();
        mArtistsAdapter = new ArtistsAdapter(mArtistsList, context);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.v(TAG, "imri onCreateView");
// Inflate the layout for this fragment
        final Context context = getContext();
        view = inflater.inflate(R.layout.all_artists, container, false);


        //  mArtistsList = getAllAudioFromDevice(context);
        recyclerView = (RecyclerView) view.findViewById(R.id.artists_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        // use a linear layout manager
        layoutManager = new LinearLayoutManager(context);
        DividerItemDecoration horizontalDecoration = new DividerItemDecoration(recyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        Drawable horizontalDivider = ContextCompat.getDrawable(context, R.drawable.horizontal_divider);
        horizontalDecoration.setDrawable(horizontalDivider);
        recyclerView.addItemDecoration(horizontalDecoration);
        recyclerView.setLayoutManager(layoutManager);


        recyclerView.setAdapter(mArtistsAdapter);
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(context,
                recyclerView, new ClickListener() {
            @Override
            public void onClick(View view, final int position) {
                //((MainActivity)getActivity()).playAudio(SongsList.getRealIndexFromArtist(position), false);
                int index = mArtistsAdapter.getSongToPlay(position);
                if (index != -1) {
                    ((MainActivity) getActivity()).playAudio(index);
                }
            }

            @Override
            public void onLongClick(View view, int position) {
                //SongInfoBox songInfoBox = new SongInfoBox(context);
                float x = lastTouchDownXY[0];
                //   songInfoBox.show(mArtistsList.get(position), x);

            }

        }));
        return view;
    }
    public ArrayList<SongIndexing> getAllSongs() {
        return mArtistsList;
    }
    int IdSongs = 1;

    class RecyclerTouchListener implements RecyclerView.OnItemTouchListener{

        private ClickListener clicklistener;
        private GestureDetector gestureDetector;

        public RecyclerTouchListener(Context context, final RecyclerView recycleView, final ClickListener clicklistener){

            this.clicklistener=clicklistener;
            gestureDetector=new GestureDetector(context,new GestureDetector.SimpleOnGestureListener(){
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child=recycleView.findChildViewUnder(e.getX(),e.getY());
                    if(child!=null && clicklistener!=null){
                        clicklistener.onLongClick(child,recycleView.getChildAdapterPosition(child));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            View child=rv.findChildViewUnder(e.getX(),e.getY());
            if(child!=null && clicklistener!=null && gestureDetector.onTouchEvent(e)){
                clicklistener.onClick(child,rv.getChildAdapterPosition(child));
            }
            lastTouchDownXY[0] = e.getY();

            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent event) {

        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }


    @Override
    public void onResume() {
        super.onResume();
    }
}

