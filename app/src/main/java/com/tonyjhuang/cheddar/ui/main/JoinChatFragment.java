package com.tonyjhuang.cheddar.ui.main;

import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.animation.AnticipateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;

import com.tonyjhuang.cheddar.R;
import com.tonyjhuang.cheddar.ui.utils.DisplayHelper;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ItemSelect;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.ColorRes;
import org.androidannotations.annotations.res.StringArrayRes;

import de.greenrobot.event.EventBus;

/**
 * Created by tonyjhuang on 2/4/16.
 * Show
 */
@EFragment(R.layout.fragment_join_chat)
public class JoinChatFragment extends Fragment {

    private static final int SINGLE_CHAT_OCCUPANCY = 2;
    private static final int GROUP_CHAT_OCCUPANCY = 5;

    @ViewById(R.id.cardview)
    CardView cardView;

    @ViewById(R.id.hall_spinner)
    Spinner residenceHallPicker;

    @ViewById(R.id.single_item)
    FrameLayout singleItem;

    @ViewById(R.id.single_image)
    ImageView singleItemImage;

    @ViewById(R.id.single_image_selected)
    ImageView singleItemSelectedImage;

    @ViewById(R.id.group_image)
    ImageView groupItemImage;

    @ViewById(R.id.group_image_selected)
    ImageView groupItemSelectedImage;

    @ViewById(R.id.group_item)
    FrameLayout groupItem;

    @StringArrayRes(R.array.residence_halls)
    String[] residenceHalls;

    @ColorRes(R.color.matchItemSelected)
    int matchItemSelectedColor;

    @ColorRes(R.color.matchItemUnselected)
    int matchItemUnselectedColor;

    // Which option do we match for, single or group? Default to Single.
    private boolean isSingle = true;

    @AfterViews
    void updateViews() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.residence_halls, R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        residenceHallPicker.setAdapter(adapter);
        setIsSingle(true);
    }

    private void setIsSingle(boolean isSingle) {
        this.isSingle = isSingle;
        singleItem.setBackgroundColor(isSingle ? matchItemSelectedColor : matchItemUnselectedColor);
        singleItemImage.setAlpha(isSingle ? 0f : .6f);
        singleItemSelectedImage.setAlpha(isSingle ? 1f : 0f);
        groupItem.setBackgroundColor(isSingle ? matchItemUnselectedColor : matchItemSelectedColor);
        groupItemImage.setAlpha(isSingle ? .6f : 0f);
        groupItemSelectedImage.setAlpha(isSingle ? 0f : 1f);
    }

    @ItemSelect(R.id.hall_spinner)
    void onResidenceHallItemClicked(boolean selected, int position) {
        setIsSingle(true);
    }

    @Click(R.id.match)
    void onMatchButtonClick() {
        if (isSingle) {
            String hall = String.valueOf(residenceHallPicker.getSelectedItem());
            EventBus.getDefault().post(new MatchClickEvent(SINGLE_CHAT_OCCUPANCY,
                    hall.equals(residenceHalls[0]) ? null : hall));
        } else {
            EventBus.getDefault().post(new MatchClickEvent(GROUP_CHAT_OCCUPANCY, null));
        }
        animateCardView();
    }

    private void animateCardView() {
        cardView.animate()
                .y(DisplayHelper.getScreenHeight(getContext()))
                .setInterpolator(new AnticipateInterpolator(1.5f))
                .setDuration(500);
    }

    @Click(R.id.single_click_target)
    void onSingleItemClick() {
        setIsSingle(true);
    }

    @Click(R.id.group_click_target)
    void onGroupItemClick() {
        setIsSingle(false);
    }

    public static class MatchClickEvent {
        // Can be null to denote the user did not pick a dorm. Is null if isSingle is false.
        public final String residenceHall;
        public final int maxOccupancy;

        public MatchClickEvent(int maxOccupancy, String residenceHall) {
            this.maxOccupancy = maxOccupancy;
            this.residenceHall = residenceHall;
        }
    }
}
