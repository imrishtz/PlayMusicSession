package com.music.session;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.music.session.MainActivity;
import com.music.session.R;

import java.util.List;

public class AllSongs extends Fragment {
    AlertDialog.Builder songInfoDialog;
    String TAG = "AllSongs";
    View view;
    private float[] lastTouchDownXY = new float[2];
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mSongsAdapter;
    private RecyclerView.LayoutManager layoutManager;
    public List<Audio> mSongList;
    SongsList mSongListInstance = SongsList.getInstance();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
// Inflate the layout for this fragment
        final Context context = getContext();
        view = inflater.inflate(R.layout.all_songs, container, false);
        mSongList = mSongListInstance.getAllSongs(context);

        //  mSongList = getAllAudioFromDevice(context);
        recyclerView = (RecyclerView) view.findViewById(R.id.my_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);

        mSongsAdapter = new SongsAdapter(mSongList);
        recyclerView.setAdapter(mSongsAdapter);

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(context,
                recyclerView, new ClickListener() {
            @Override
            public void onClick(View view, final int position) {
                ((MainActivity)getActivity()).playAudio(position);

            }

            @Override
            public void onLongClick(View view, int position) {
                //SongInfoBox songInfoBox = new SongInfoBox(context);
                float x = lastTouchDownXY[0];
             //   songInfoBox.show(mSongList.get(position), x);

            }

        }));
        return view;
    }
    public List<Audio> getAllSongs() {
        return mSongList;
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

