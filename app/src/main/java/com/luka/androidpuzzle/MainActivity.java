package com.luka.androidpuzzle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.ClipData;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Puzzle Game");

        List<LinearLayout> linearLayouts = new ArrayList<>();
        List<Integer> imageResourceIds = new ArrayList<>();

        for (int i = 0; i <= 3; i++) {
            for (int j = 0; j <= 2; j++) {
                int layoutId = getResources().getIdentifier("layout" + i + j, "id", getPackageName());
                linearLayouts.add(findViewById(layoutId));

                int imageId = getResources().getIdentifier("image" + i + j, "id", getPackageName());
                imageResourceIds.add(imageId);

                // Set TouchListener for each ImageView
                ImageView imageView = findViewById(imageId);
                if (imageView != null) {
                    imageView.setOnTouchListener(new MyTouchListener());
                }

                // Set DragListener for each LinearLayout
                findViewById(layoutId).setOnDragListener(new MyDragListener());
            }
        }

        // Shuffle the Image resource IDs
        Collections.shuffle(imageResourceIds);

        // Assign shuffled Image resource IDs to LinearLayouts
        for (int i = 0; i < linearLayouts.size(); i++) {
            ImageView imageView = findViewById(imageResourceIds.get(i));

            // Ensure the ImageView is not null before attempting to manipulate it
            if (imageView != null) {
                // Remove the ImageView from its current parent, if any
                ViewParent parent = imageView.getParent();
                if (parent instanceof ViewGroup) {
                    ((ViewGroup) parent).removeView(imageView);
                }

                // Add the ImageView to the new parent (LinearLayout)
                linearLayouts.get(i).addView(imageView);
            }
        }
    }

    private final class MyTouchListener implements View.OnTouchListener {
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                ClipData data = ClipData.newPlainText("", "");
                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(
                        view);
                view.startDragAndDrop(data, shadowBuilder, view, 0);
                return true;
            } else {
                return false;
            }
        }
    }

    class MyDragListener implements View.OnDragListener {
        Drawable enterShape = getResources().getDrawable(
                R.drawable.shape_droptarget);
        Drawable normalShape = getResources().getDrawable(R.drawable.shape);

        @Override
        public boolean onDrag(View v, DragEvent event) {
            int action = event.getAction();
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    // do nothing
                    break;
                case DragEvent.ACTION_DRAG_ENTERED:
                    v.setBackgroundDrawable(enterShape);
                    break;
                case DragEvent.ACTION_DRAG_EXITED:
                    v.setBackgroundDrawable(normalShape);
                    break;
                case DragEvent.ACTION_DROP:
                    if(event == null || event.getLocalState() == null) break;

                    View view = (View) event.getLocalState();
                    ViewGroup owner = (ViewGroup) view.getParent();

                    if(owner == null || owner == v) break;

                    owner.removeView(view);

                    if (v instanceof LinearLayout) {
                        LinearLayout container = (LinearLayout) v;

                        if (container.getChildCount() > 0) {
                            View oldView = container.getChildAt(0);
                            container.removeViewAt(0);

                            owner.addView(oldView);
                        }

                        container.addView(view);
                        view.setVisibility(View.VISIBLE);
                    }
                    break;
                case DragEvent.ACTION_DRAG_ENDED:
                    v.setBackgroundDrawable(normalShape);
                default:
                    break;
            }
            return true;
        }
    }
}