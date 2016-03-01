package com.tonyjhuang.cheddar.ui.main;

import android.support.v4.app.Fragment;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.widget.TextView;

import com.tonyjhuang.cheddar.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import de.greenrobot.event.EventBus;

/**
 * Created by tonyjhuang on 2/4/16.
 * Shows the initial screen with onboarding text.
 */

@EFragment(R.layout.fragment_get_started)
public class GetStartedFragment extends Fragment {

    @ViewById
    TextView description;

    @AfterViews
    void boldDescriptionText() {
        boldSubstring(description, "anonymous");
    }

    private void boldSubstring(TextView textView, String substring) {
        String text = textView.getText().toString();
        int index = text.indexOf(substring);
        final SpannableStringBuilder builder = new SpannableStringBuilder(text);
        builder.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                index, index + substring.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(builder);
    }

    @Click(R.id.lets_go)
    void onLetsGoButtonClick() {
        EventBus.getDefault().post(new NextClickEvent());
    }

    public static class NextClickEvent {
    }
}
