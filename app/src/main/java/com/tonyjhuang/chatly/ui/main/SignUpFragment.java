package com.tonyjhuang.chatly.ui.main;

import android.support.v4.app.Fragment;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.tonyjhuang.chatly.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.ColorRes;

import de.greenrobot.event.EventBus;

/**
 * Created by tonyjhuang on 2/9/16.
 * Fragment for sign up UI.
 */
@EFragment(R.layout.fragment_sign_up)
public class SignUpFragment extends Fragment {

    @ViewById(R.id.year_spinner)
    Spinner yearPicker;

    @ViewById(R.id.school_spinner)
    Spinner schoolPicker;

    @ViewById(R.id.male)
    Button maleSelector;

    @ViewById(R.id.female)
    Button femaleSelector;

    @ColorRes(R.color.colorAccent)
    int accentColor;

    @ColorRes(android.R.color.secondary_text_light)
    int defaultTextColor;

    private boolean isMale;

    @AfterViews
    void updateViews() {
        ArrayAdapter<CharSequence> yearAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.school_year, R.layout.simple_spinner_item);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearPicker.setAdapter(yearAdapter);

        ArrayAdapter<CharSequence> schoolAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.college, R.layout.simple_spinner_item);
        schoolAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        schoolPicker.setAdapter(schoolAdapter);

        setIsMale(true);
    }

    private void setIsMale(boolean isMale) {
        this.isMale = isMale;
        maleSelector.setTextColor(isMale ? accentColor : defaultTextColor);
        femaleSelector.setTextColor(isMale ? defaultTextColor : accentColor);
    }

    @Click(R.id.male)
    public void onMaleSelectorClicked() {
        setIsMale(true);
    }

    @Click(R.id.female)
    public void onFemaleSelectorClicked() {
        setIsMale(false);
    }

    @Click(R.id.submit)
    public void onSubmitClicked() {
        EventBus.getDefault().post(new SubmitClickEvent());
    }

    public static class SubmitClickEvent {
    }
}
