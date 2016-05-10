package com.tonyjhuang.cheddar.ui.onboard;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.tonyjhuang.cheddar.ui.login.RegisterFragment_;

/**
 * Supplies fragments to the Onboard viewpager.
 */
public class OnboardPagerAdapter extends FragmentPagerAdapter {

    private Fragment[] fragments = {
            OnboardActivity_.OnboardCheddarFragment_.builder().build(),
            OnboardActivity_.OnboardMatchFragment_.builder().build(),
            OnboardActivity_.OnboardGroupFragment_.builder().build(),
            RegisterFragment_.builder().build()
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