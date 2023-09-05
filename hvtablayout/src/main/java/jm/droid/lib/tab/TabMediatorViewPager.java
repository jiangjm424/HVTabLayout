/*
 * Copyright 2023 The Jmdroid Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jm.droid.lib.tab;

import static androidx.viewpager.widget.ViewPager.SCROLL_STATE_DRAGGING;
import static androidx.viewpager.widget.ViewPager.SCROLL_STATE_IDLE;
import static androidx.viewpager.widget.ViewPager.SCROLL_STATE_SETTLING;

import android.database.DataSetObserver;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import java.lang.ref.WeakReference;

public abstract class TabMediatorViewPager<VH extends TabHolder> extends TabAdapter<VH> {
  private final @NonNull ViewPager viewPager;
  private final @NonNull ITabLayout tabLayout;

  private final boolean autoRefresh;
  private boolean attached;

  private @Nullable PagerAdapter pagerAdapter;
  private DataSetObserver pagerAdapterObserver;
  private TabLayoutOnPageChangeListener pageChangeListener;
  private OnTabSelectedListener onTabSelectedListener;
  private AdapterChangeListener adapterChangeListener;

  public TabMediatorViewPager(@NonNull ITabLayout tabLayout, @NonNull ViewPager viewPager) {
    this(tabLayout, viewPager, true);
  }

  public TabMediatorViewPager(
      @NonNull ITabLayout tabLayout, @NonNull ViewPager viewPager, boolean autoRefresh) {
    this.tabLayout = tabLayout;
    this.viewPager = viewPager;
    this.autoRefresh = autoRefresh;
  }

  @Override
  public final void onBindTabHolder(@NonNull VH holder, int position) {
    onBindTabHolder(holder, pagerAdapter, position);
  }

  public abstract void onBindTabHolder(
      @NonNull VH holder, @Nullable PagerAdapter adapter, int position);

  @Override
  public final int getTabCount() {
    PagerAdapter adapter = viewPager.getAdapter();
    if (adapter == null) return 0;
    return adapter.getCount();
  }

  public final TabMediatorViewPager bind() {
    if (attached) {
      throw new IllegalStateException("TabLayoutMediator is already bind");
    }
    if (tabLayout.getAdapter() != null) {
      throw new IllegalStateException(
          "TabLayout should be no adapter when AdapterMediatorViewPager2 bind ");
    }
    attached = true;
    pagerAdapter = viewPager.getAdapter();
    tabLayout.setAdapter(this);
    // Add our custom OnPageChangeListener to the ViewPager
    pageChangeListener = new TabLayoutOnPageChangeListener(tabLayout);
    viewPager.addOnPageChangeListener(pageChangeListener);
    // Now we'll add a tab selected listener to set ViewPager's current item
    onTabSelectedListener = new ViewPagerOnTabSelectedListener(viewPager);
    tabLayout.addOnTabSelectedListener(onTabSelectedListener);

    if (autoRefresh && pagerAdapter != null) {
      pagerAdapterObserver = new PagerAdapterObserver();
      pagerAdapter.registerDataSetObserver(pagerAdapterObserver);
    }

    // Add a listener so that we're notified of any adapter changes
    adapterChangeListener = new AdapterChangeListener();
    adapterChangeListener.setAutoRefresh(autoRefresh);
    viewPager.addOnAdapterChangeListener(adapterChangeListener);
    tabLayout.selectTab(tabLayout.getTabAt(viewPager.getCurrentItem()));
    return this;
  }

  @Override
  public void onAttachedToTabLayout(@NonNull ITabLayout tabLayout) {
    if (this.tabLayout != tabLayout) {
      throw new IllegalStateException(
          "This adapter should be set in " + this.tabLayout + ", but you set in " + tabLayout);
    }
  }

  @Override
  public void onDetachedFromTabLayout(@NonNull ITabLayout tabLayout) {
      if (autoRefresh && pagerAdapter != null) {
          pagerAdapter.unregisterDataSetObserver(pagerAdapterObserver);
      }
      viewPager.removeOnAdapterChangeListener(adapterChangeListener);
      viewPager.removeOnPageChangeListener(pageChangeListener);
      tabLayout.removeOnTabSelectedListener(onTabSelectedListener);
  }

  private void populateFromPagerAdapter() {
    if (pagerAdapter != null) {
      notifyTabDataSetChanged();
      if (getTabCount() > 0) {
        int last = tabLayout.getTabCount() - 1;
        int curr = Math.min(viewPager.getCurrentItem(), last);
        if (curr != tabLayout.getSelectedTabPosition()) {
          tabLayout.selectTab(tabLayout.getTabAt(curr));
        }
      }
    }
  }

  private static class TabLayoutOnPageChangeListener implements ViewPager.OnPageChangeListener {
    @NonNull private final WeakReference<ITabLayout> tabLayoutRef;
    private int previousScrollState;
    private int scrollState;

    public TabLayoutOnPageChangeListener(ITabLayout tabLayout) {
      tabLayoutRef = new WeakReference<>(tabLayout);
    }

    @Override
    public void onPageScrollStateChanged(final int state) {
      previousScrollState = scrollState;
      scrollState = state;
      ITabLayout tabLayout = tabLayoutRef.get();
      if (tabLayout != null) {
        tabLayout.updateViewPagerScrollState(scrollState);
      }
    }

    @Override
    public void onPageScrolled(
        final int position, final float positionOffset, final int positionOffsetPixels) {
      final ITabLayout tabLayout = tabLayoutRef.get();
      if (tabLayout != null) {
        // Only update the tab view selection if we're not settling, or we are settling after
        // being dragged
        final boolean updateSelectedTabView =
            scrollState != SCROLL_STATE_SETTLING || previousScrollState == SCROLL_STATE_DRAGGING;
        // Update the indicator if we're not settling after being idle. This is caused
        // from a setCurrentItem() call and will be handled by an animation from
        // onPageSelected() instead.
        final boolean updateIndicator =
            !(scrollState == SCROLL_STATE_SETTLING && previousScrollState == SCROLL_STATE_IDLE);
        tabLayout.setScrollPosition(
            position, positionOffset, updateSelectedTabView, updateIndicator, false);
      }
    }

    @Override
    public void onPageSelected(final int position) {
      final ITabLayout tabLayout = tabLayoutRef.get();
      if (tabLayout != null
          && tabLayout.getSelectedTabPosition() != position
          && position < tabLayout.getTabCount()) {
        // Select the tab, only updating the indicator if we're not being dragged/settled
        // (since onPageScrolled will handle that).
        final boolean updateIndicator =
            scrollState == SCROLL_STATE_IDLE
                || (scrollState == SCROLL_STATE_SETTLING
                    && previousScrollState == SCROLL_STATE_IDLE);
        tabLayout.selectTab(tabLayout.getTabAt(position), updateIndicator);
      }
    }

    void reset() {
      previousScrollState = scrollState = SCROLL_STATE_IDLE;
    }
  }

  private static class ViewPagerOnTabSelectedListener implements OnTabSelectedListener {
    private final ViewPager viewPager;

    public ViewPagerOnTabSelectedListener(ViewPager viewPager) {
      this.viewPager = viewPager;
    }

    @Override
    public void onTabSelected(@NonNull TabHolder tab) {
      viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(TabHolder tab) {
      // No-op
    }

    @Override
    public void onTabReselected(TabHolder tab) {
      // No-op
    }
  }

  private class PagerAdapterObserver extends DataSetObserver {
    PagerAdapterObserver() {}

    @Override
    public void onChanged() {
      populateFromPagerAdapter();
    }

    @Override
    public void onInvalidated() {
      populateFromPagerAdapter();
    }
  }

  private class AdapterChangeListener implements ViewPager.OnAdapterChangeListener {
    private boolean autoRefresh;

    AdapterChangeListener() {}

    @Override
    public void onAdapterChanged(
        @NonNull ViewPager viewPager,
        @Nullable PagerAdapter oldAdapter,
        @Nullable PagerAdapter newAdapter) {
      if (TabMediatorViewPager.this.viewPager == viewPager) {
        pagerAdapter = newAdapter;
        notifyTabDataSetChanged();
      }
    }

    void setAutoRefresh(boolean autoRefresh) {
      this.autoRefresh = autoRefresh;
    }
  }
}
