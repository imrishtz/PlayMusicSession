package com.music.session.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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

public class AllSongs extends Fragment {

    private SongsAdapter mSongsAdapter;

    AllSongs(Context context) {
        mSongsAdapter = new SongsAdapter(context);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
// Inflate the layout for this fragment
        final Context context = getContext();
        View view = inflater.inflate(R.layout.all_songs, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.my_recycler_view);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
        DividerItemDecoration horizontalDecoration = new DividerItemDecoration(recyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        Drawable horizontalDivider = ContextCompat.getDrawable(context, R.drawable.horizontal_divider);
        horizontalDecoration.setDrawable(horizontalDivider);
        recyclerView.addItemDecoration(horizontalDecoration);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        recyclerView.setAdapter(mSongsAdapter);
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(context,
                recyclerView, new ClickListener() {
            @Override
            public void onClick(View view, final int position) {
                if (getActivity() != null) {
                    ((MainActivity) getActivity()).playAudio(position, true);
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
        public void onTouchEvent(RecyclerView rv, MotionEvent event) {}

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {}
    }
    @Override
    public void onResume() {
        super.onResume();
    }
}

