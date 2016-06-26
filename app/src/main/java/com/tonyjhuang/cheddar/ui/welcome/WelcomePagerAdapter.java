package com.tonyjhuang.cheddar.ui.welcome;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Supplies fragments to the Onboard viewpager.
 */
public class WelcomePagerAdapter extends FragmentPagerAdapter {

    private final List<Fragment> fragments;

    public WelcomePagerAdapter(FragmentManager fm, boolean showOnboard) {
        super(fm);
        fragments = new ArrayList<>();
        if (showOnboard) {
            fragments.add(WelcomeActivity_.OnboardCheddarFragment_.builder().build());
            fragments.add(WelcomeActivity_.OnboardMatchFragment_.builder().build());
            fragments.add(WelcomeActivity_.OnboardGroupFragment_.builder().build());
        }
        fragments.add(WelcomeFragment_.builder().build());
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }
}