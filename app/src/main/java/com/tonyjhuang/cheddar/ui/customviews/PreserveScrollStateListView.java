package com.tonyjhuang.cheddar.ui.customviews;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * Keeps track of the scroll state when adding
 * new elements to the top.
 */
public class PreserveScrollStateListView extends ListView {

    private static final String TAG = PreserveScrollStateListView.class.getSimpleName();

    private int savedFirstVisiblePosition;
    private int savedFirstVisibleChildHeight;
    private int savedFirstVisibleChildTop;
    private int savedNumberOfItems;
    private boolean shouldDraw = true;

    public PreserveScrollStateListView(Context context) {
        this(context, null);
    }

    public PreserveScrollStateListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PreserveScrollStateListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if(!isInEditMode())
        getViewTreeObserver().addOnPreDrawListener(() -> shouldDraw);
    }

    public void pauseDrawing() {
        shouldDraw = false;
    }

    public void saveScrollStateAndPauseDrawing() {
        pauseDrawing();

        savedFirstVisiblePosition = getFirstVisiblePosition();
        savedNumberOfItems = getAdapter().getCount();
        View firstVisibleChild = getChildAt(savedFirstVisiblePosition);
        if (firstVisibleChild != null) {
            savedFirstVisibleChildHeight = firstVisibleChild.getHeight();
            savedFirstVisibleChildTop = firstVisibleChild.getTop();
        }
    }

    public void resumeDrawing() {
        shouldDraw = true;
    }

    public void restoreScrollStateAndResumeDrawing() {
        ListAdapter adapter = getAdapter();
        if (savedNumberOfItems == 0 || adapter == null) {
            resumeDrawing();
            return;
        }

        int numberOfAddedChildren = getAdapter().getCount() - savedNumberOfItems;
        int restoreToIndex = numberOfAddedChildren + savedFirstVisiblePosition;
        int newChildHeight = getMeasuredHeightOfChild(restoreToIndex);
        int heightDifference = newChildHeight - savedFirstVisibleChildHeight;

        int restoreToTop = heightDifference + savedFirstVisibleChildTop;

        if (restoreToTop > 0) {
            setSelectionFromTop(restoreToIndex, restoreToTop);
        } else {
            while (--restoreToIndex >= 0 && restoreToTop < 0) {
                int childHeight = getMeasuredHeightOfChild(restoreToIndex);
                restoreToTop += childHeight;
            }
            setSelectionFromTop(Math.max(restoreToIndex, 0), Math.max(restoreToTop, 0));
        }

        post(this::resumeDrawing);
    }

    private int getMeasuredHeightOfChild(int position) {
        View view = getAdapter().getView(position, null, this);
        view.measure(0, 0);
        return view.getMeasuredHeight();
    }

    @Override
    public void setSelectionFromTop(int position, int y) {
        post(() -> super.setSelectionFromTop(position, y));
        super.setSelectionFromTop(position, y);
    }
}
