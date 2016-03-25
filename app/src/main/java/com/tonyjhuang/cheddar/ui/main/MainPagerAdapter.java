package com.tonyjhuang.cheddar.ui.main;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by tonyjhuang on 2/9/16.
 */
public class MainPagerAdapter extends FragmentPagerAdapter {

    private Fragment[] fragments = {
            new MainActivity2_.OnboardCheddarFragment_(),
            new MainActivity2_.OnboardMatchFragment_(),
            new MainActivity2_.OnboardGroupFragment_(),
            new MainActivity2_.AlphaWarningFragment_()
    };

    public MainPagerAdapter(FragmentManager fm) {
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