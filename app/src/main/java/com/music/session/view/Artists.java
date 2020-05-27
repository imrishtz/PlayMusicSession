package com.music.session.view;

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

import com.music.session.R;

public class Artists extends Fragment {
    private ArtistsAdapter mArtistsAdapter;
    Artists(Context context) {
        mArtistsAdapter = new ArtistsAdapter(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final Context context = getContext();
        View view = inflater.inflate(R.layout.all_artists, container, false);
        RecyclerView recyclerView =  view.findViewById(R.id.artists_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(mArtistsAdapter);
        recyclerView.addItemDecoration(new HeaderItemDecoration(recyclerView, mArtistsAdapter));

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(context,
                recyclerView, new ClickListener() {
            @Override
            public void onClick(View view, final int position) {
                int index = mArtistsAdapter.getSongToPlay(position);
                if (index != -1 && (getActivity() != null)) {
                    ((MainActivity) getActivity()).playAudio(index, false);
                }
            }

            @Override
            public void onLongClick(View view, int position) {}
        }));
        return view;
    }

    class RecyclerTouchListener implements RecyclerView.OnItemTouchListener{
        private ClickListener clicklistener;
        private GestureDetector gestureDetector;

        RecyclerTouchListener(Context context, final RecyclerView recycleView, final ClickListener clicklistener){

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

