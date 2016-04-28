package com.tonyjhuang.cheddar.ui.onboard;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by tonyjhuang on 2/9/16.
 */
public class OnboardPagerAdapter extends FragmentPagerAdapter {

    private Fragment[] fragments = {
            new OnboardActivity_.OnboardCheddarFragment_(),
            new OnboardActivity_.OnboardMatchFragment_(),
            new OnboardActivity_.OnboardGroupFragment_(),
            new OnboardActivity_.AlphaWarningFragment_()
    };

    public OnboardPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return fragments[position];
    }

    @Override
    public int getCount() {
        return fragments.length;
    }
}