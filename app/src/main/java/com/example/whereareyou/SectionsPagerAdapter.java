package com.example.whereareyou;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.whereareyou.map.MapFragment;
import com.example.whereareyou.record.RecordFragment;

/**
 * Returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {
  private static RecordFragment recordFragment;
  private static MapFragment mapFragment;

  @StringRes
  private static final int[] TAB_TITLES = new int[]{R.string.tab_text_1, R.string.tab_text_2};
  private final Context mContext;

  SectionsPagerAdapter(Context context, FragmentManager fm) {
    super(fm);
    mContext = context;
  }

  @NonNull
  @Override
  public Fragment getItem(int position) {
    // getItem is called to instantiate the fragment for the given page.
    switch (position) {
      case 0:
        return new RecordFragment();

      case 1:
        return new MapFragment();
    }
    throw new RuntimeException("Can not get item.");
  }

  @NonNull
  @Override
  public Object instantiateItem(@NonNull ViewGroup container, int position) {
    Fragment createdFragment = (Fragment) super.instantiateItem(container, position);
    // save the appropriate reference depending on position
    switch (position) {
      case 0:
        recordFragment = (RecordFragment) createdFragment;
        break;
      case 1:
        mapFragment = (MapFragment) createdFragment;
        break;
    }
    return createdFragment;
  }

  @Nullable
  @Override
  public CharSequence getPageTitle(int position) {
    return mContext.getResources().getString(TAB_TITLES[position]);
  }

  @Override
  public int getCount() {
    // Show 2 total pages.
    return 2;
  }
}