package com.luka.androidpuzzle;

import android.content.ClipData;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        List<LinearLayout> linearLayouts = new ArrayList<>();
        List<Integer> imageResourceIds = new ArrayList<>();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Puzzle Game");

        for (int i = 0; i <= 3; i++) {
            for (int j = 0; j <= 2; j++) {
                int layoutId = getResources().getIdentifier("layout" + i + j, "id", getPackageName());
                linearLayouts.add(findViewById(layoutId));

                int imageId = getResources().getIdentifier("image" + i + j, "id", getPackageName());
                imageResourceIds.add(imageId);

                ImageView imageView = findViewById(imageId);
                if (imageView != null) imageView.setOnTouchListener(new MyTouchListener());

                findViewById(layoutId).setOnDragListener(new MyDragListener());
            }
        }

        Collections.shuffle(imageResourceIds);

        for (int i = 0; i < linearLayouts.size(); i++) {
            ImageView imageView = findViewById(imageResourceIds.get(i));

            if (imageView == null) continue;

            ViewParent parent = imageView.getParent();
            if (parent instanceof ViewGroup) ((ViewGroup) parent).removeView(imageView);

            linearLayouts.get(i).addView(imageView);
        }
    }

    private final class MyTouchListener implements View.OnTouchListener {
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (motionEvent.getAction() != MotionEvent.ACTION_DOWN) return false;

            ClipData data = ClipData.newPlainText("", "");
            View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(
                    view);
            view.startDragAndDrop(data, shadowBuilder, view, 0);

            return true;
        }
    }

    class MyDragListener implements View.OnDragListener {
        private final Drawable enterShape = getResources().getDrawable(R.drawable.shape_droptarget);
        private final Drawable normalShape = getResources().getDrawable(R.drawable.shape);

        private final GridLayout gridLayout = findViewById(R.id.gridLayout);

        @Override
        public boolean onDrag(View v, DragEvent event) {
            int action = event.getAction();
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_ENTERED:
                    v.setBackground(enterShape);
                    break;
                case DragEvent.ACTION_DRAG_EXITED:
                case DragEvent.ACTION_DRAG_ENDED:
                    v.setBackground(normalShape);
                    break;
                case DragEvent.ACTION_DROP:
                    if (event == null || event.getLocalState() == null) break;

                    View view = (View) event.getLocalState();
                    ViewGroup owner = (ViewGroup) view.getParent();

                    if (owner == null || owner == v) break;

                    owner.removeView(view);

                    if (v instanceof LinearLayout) {
                        LinearLayout container = (LinearLayout) v;

                        if (container.getChildCount() > 0) {
                            View oldView = container.getChildAt(0);
                            container.removeViewAt(0);

                            owner.addView(oldView);
                        }

                        container.addView(view);

                        if (checkAllImagesInPlace()) {
                            gridLayout.removeAllViews();
                            showCompletedImage();
                        }
                    }
                    break;
                default:
                    break;
            }
            return true;
        }

        private boolean checkAllImagesInPlace() {

            for (int i = 0; i < gridLayout.getChildCount(); i++) {
                View childView = gridLayout.getChildAt(i);

                if (!(childView instanceof LinearLayout)) continue;

                LinearLayout linearLayout = (LinearLayout) childView;

                if (linearLayout.getChildCount() != 1) continue;

                View innerChildView = linearLayout.getChildAt(0);

                if (!(innerChildView instanceof ImageView)) continue;

                ImageView imageView = (ImageView) innerChildView;

                Drawable currentDrawable = imageView.getDrawable();

                int expectedResourceId = getResources().getIdentifier(
                        "android" + (i / 3) + (i % 3), "drawable", getPackageName());
                Drawable expectedDrawable = getResources().getDrawable(expectedResourceId);

                if (!areDrawablesEqual(currentDrawable, expectedDrawable)) {
                    return false;
                }
            }

            return true;
        }

        private boolean areDrawablesEqual(Drawable drawable1, Drawable drawable2) {
            if (drawable1 == null && drawable2 == null) return true;
            if (drawable1 == null || drawable2 == null) return false;

            Bitmap bitmap1 = ((BitmapDrawable) drawable1).getBitmap();
            Bitmap bitmap2 = ((BitmapDrawable) drawable2).getBitmap();

            return bitmap1.sameAs(bitmap2);
        }

        private void showCompletedImage() {
            gridLayout.setRowCount(1);
            gridLayout.setColumnCount(1);

            ImageView completedImageView = new ImageView(MainActivity.this);

            completedImageView.setImageResource(R.drawable.android);

            gridLayout.addView(completedImageView);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.height = GridLayout.LayoutParams.MATCH_PARENT;
            params.width = GridLayout.LayoutParams.MATCH_PARENT;
            completedImageView.setLayoutParams(params);

            Toast.makeText(MainActivity.this, "🎉 Congratulations! You solved the puzzle! 🧩", Toast.LENGTH_LONG).show();
        }
    }
}