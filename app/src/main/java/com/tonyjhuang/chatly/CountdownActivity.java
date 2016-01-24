package com.tonyjhuang.chatly;

import android.os.Handler;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.CardView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.ViewsById;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

@EActivity(R.layout.activity_countdown)
public class CountdownActivity extends CheddarActivity {

    private static final float saveDisabled = 0.25f;

    // Mon Feb 15 2016 12:00:00 GMT-0500 (EST)
    private static final long releaseDate = 1455555600000l;

    private static Timer timer = new Timer();

    @ViewById
    TextView cheddar;

    @ViewById(R.id.coming_soon)
    TextView comingSoon;

    @ViewById(R.id.cardview)
    CardView cardView;

    @ViewById(R.id.email_layout)
    TextInputLayout emailLayout;

    @ViewById(R.id.email)
    EditText emailInput;

    @ViewById(R.id.save)
    ImageView save;

    @ViewById(R.id.days)
    TextSwitcher days;

    @ViewById(R.id.hours)
    TextSwitcher hours;

    @ViewById(R.id.minutes)
    TextSwitcher minutes;

    @ViewById(R.id.seconds)
    TextSwitcher seconds;

    @ViewsById({R.id.days_container, R.id.hours_container, R.id.minutes_container, R.id.seconds_container})
    List<ViewGroup> countdownContainers;

    @Pref
    CheddarPrefs_ prefs;

    private Handler animationHandler = new Handler();

    private Pattern regex = Pattern.compile("[a-zA-Z0-9\\.]+@husky\\.neu\\.edu$");

    private ViewSwitcher.ViewFactory countdownViewFactory = new ViewSwitcher.ViewFactory() {
        @Override
        public View makeView() {
            return LayoutInflater.from(CountdownActivity.this)
                    .inflate(R.layout.stub_countdown_number, null);
        }
    };

    @AfterViews
    public void updateViews() {
        if (!prefs.emailAddress().get().equals("")) {
            emailInput.setText(prefs.emailAddress().get());
            emailInput.setEnabled(false);
            save.setAlpha(saveDisabled);
        } else {
            emailInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        onSave();
                    }
                    return true;
                }
            });
        }

        days.setFactory(countdownViewFactory);
        hours.setFactory(countdownViewFactory);
        minutes.setFactory(countdownViewFactory);
        seconds.setFactory(countdownViewFactory);

        updateCountdown();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateCountdown();
                    }
                });
            }
        }, TimeHelper.SECOND, TimeHelper.SECOND);

        playAnimation();
    }

    private void updateCountdown() {
        long timeToRelease = releaseDate - System.currentTimeMillis();
        days.setText(TimeHelper.daysLeft(timeToRelease) + "");
        hours.setText(TimeHelper.hoursLeft(timeToRelease) + "");
        minutes.setText(TimeHelper.minutesLeft(timeToRelease) + "");
        seconds.setText(TimeHelper.secondsLeft(timeToRelease) + "");
    }

    private void playAnimation() {
        animationHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                animateCountdown();
            }
        }, 250);
    }

    // time: 250
    private void animateCountdown() {
        for (int i = 0; i < countdownContainers.size(); i++) {
            ViewGroup container = countdownContainers.get(i);
            container.setTranslationY(container.getHeight());
            container.animate()
                    .setStartDelay(i * 100)
                    .translationYBy(-container.getHeight())
                    .setInterpolator(new OvershootInterpolator())
                    .alpha(1f)
                    .setDuration(500);
        }
        // finishes at time: 700.

        animationHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                animateComingSoon();
            }
        }, 700);
    }

    // time: 950
    private void animateComingSoon() {
        comingSoon.animate().alpha(1f).setDuration(1500);
        animationHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                animateCheddar();
            }
        }, 1200);
    }

    // time: 1150
    private void animateCheddar() {
        cheddar.animate().alpha(1f).setDuration(1500);
        animationHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                animateCard();
            }
        }, 1750);
    }

    // time: 1400
    private void animateCard() {
        YoYo.with(Techniques.FadeInUp).duration(1000).playOn(cardView);
    }

    @Click(R.id.cheddar)
    public void onCheddarClick() {
        for (ViewGroup container : countdownContainers) {
            container.setAlpha(0);
        }
        cheddar.setAlpha(0);
        comingSoon.setAlpha(0);
        cardView.setAlpha(0);
        playAnimation();
    }

    @Click(R.id.save)
    public void onSaveClick(View v) {
        onSave();
    }

    public void onSave() {
        if (regex.matcher(emailInput.getText()).matches()) {
            emailInput.setEnabled(false);
            prefs.emailAddress().put(emailInput.getText().toString());
            save.setClickable(false);
            save.animate().alpha(saveDisabled);
        } else {
            YoYo.with(Techniques.Shake).duration(500).playOn(emailInput);
        }
    }

    @Override
    public void onBackPressed() {
        if (CheddarApplication.DEBUG && !prefs.emailAddress().get().equals("")) {
            prefs.emailAddress().put("");
            emailInput.setText("");
            emailInput.setEnabled(true);
            save.setAlpha(1f);
            save.setClickable(true);
        } else {
            super.onBackPressed();
        }
    }
}
