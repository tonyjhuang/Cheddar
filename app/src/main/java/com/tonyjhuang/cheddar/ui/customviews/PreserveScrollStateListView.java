package com.tonyjhuang.cheddar.ui.customviews;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListView;

/**
 * Keeps track of the scroll state when adding
 * new elements to the top.
 */
public class PreserveScrollStateListView extends ListView {

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
        if (!isInEditMode())
            getViewTreeObserver().addOnPreDrawListener(() -> shouldDraw);
    }

    public void pauseDrawing() {
        shouldDraw = false;
    }

    public void saveScrollStateAndPauseDrawing() {
        pauseDrawing();
        if (getAdapter().getCount() - getHeaderViewsCount() == 0) return;

        savedNumberOfItems = getAdapter().getCount();

        int headerViewsCount = getHeaderViewsCount();
        savedFirstVisiblePosition = getFirstVisiblePosition();
        View firstVisibleNonHeaderChild;
        if(savedFirstVisiblePosition < headerViewsCount) {
            firstVisibleNonHeaderChild = getChildAt(headerViewsCount - savedFirstVisiblePosition);
            savedFirstVisiblePosition = headerViewsCount;
        } else {
            firstVisibleNonHeaderChild = getChildAt(0);
        }

        if (firstVisibleNonHeaderChild != null) {
            savedFirstVisibleChildHeight = firstVisibleNonHeaderChild.getHeight();
            savedFirstVisibleChildTop = firstVisibleNonHeaderChild.getTop();
        } else {
            savedFirstVisibleChildHeight = getMeasuredHeightOfChild(savedFirstVisiblePosition);
            savedFirstVisibleChildTop = 0;
        }
    }

    public void resumeDrawing() {
        shouldDraw = true;
    }

    public void restoreScrollStateAndResumeDrawing() {
        if (getAdapter() == null) {
            resumeDrawing();
            return;
        }
        int numberOfAddedChildren = getCount() - savedNumberOfItems;
        if (savedNumberOfItems == 0 || numberOfAddedChildren == 0) {
            resumeDrawing();
            return;
        }

        int restoreToIndex = numberOfAddedChildren + savedFirstVisiblePosition;
        int newChildHeight = getMeasuredHeightOfChild(restoreToIndex);
        int heightDifference = newChildHeight - savedFirstVisibleChildHeight;

        int restoreToTop = savedFirstVisibleChildTop - heightDifference;
        setSelectionFromTop(restoreToIndex, restoreToTop);
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
