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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import java.lang.ref.WeakReference;
import java.util.Objects;

public abstract class TabMediatorViewPager2<VH extends TabHolder, A extends RecyclerView.Adapter<?>>
    extends TabAdapter<VH> {
  private @NonNull final ViewPager2 viewPager;
  private @Nullable A rvAdapter;
  private ITabLayout tabLayout;

  private final boolean autoRefresh;
  private final boolean smoothScroll;
  private boolean attached;
  @Nullable private TabLayoutOnPageChangeCallback onPageChangeCallback;
  @Nullable private OnTabSelectedListener onTabSelectedListener;
  @Nullable private RecyclerView.AdapterDataObserver pagerAdapterObserver;

  public TabMediatorViewPager2(@NonNull ITabLayout tabLayout, @NonNull ViewPager2 viewPager) {
    this(tabLayout, viewPager, true);
  }

  public TabMediatorViewPager2(
      @NonNull ITabLayout tabLayout, @NonNull ViewPager2 viewPager, boolean autoRefresh) {
    this(tabLayout, viewPager, autoRefresh, true);
  }

  public TabMediatorViewPager2(
      @NonNull ITabLayout tabLayout,
      @NonNull ViewPager2 viewPager,
      boolean autoRefresh,
      boolean smoothScroll) {
    this.tabLayout = tabLayout;
    this.viewPager = viewPager;
    this.autoRefresh = autoRefresh;
    this.smoothScroll = smoothScroll;
  }

  @Override
  public final void onBindTabHolder(@NonNull VH holder, int position) {
    onBindTabHolder(holder, rvAdapter, position);
  }

  public abstract void onBindTabHolder(@NonNull VH holder, @NonNull A adapter, int position);

  @Override
  public final int getTabCount() {
    return Objects.requireNonNull(viewPager.getAdapter()).getItemCount();
  }

  public final TabMediatorViewPager2 bind() {
    if (attached) {
      throw new IllegalStateException("TabLayoutMediator is already attached");
    }
    rvAdapter = (A) viewPager.getAdapter();
    if (rvAdapter == null) {
      throw new IllegalStateException(
          "AdapterMediatorViewPager2 attached before ViewPager2 has an " + "adapter");
    }
    if (tabLayout.getAdapter() != null) {
      throw new IllegalStateException(
          "TabLayout should be no adapter when AdapterMediatorViewPager2 attached ");
    }
    attached = true;
    tabLayout.setAdapter(this);
    // Add our custom OnPageChangeCallback to the ViewPager
    onPageChangeCallback = new TabLayoutOnPageChangeCallback(tabLayout);
    viewPager.registerOnPageChangeCallback(onPageChangeCallback);

    // Now we'll add a tab selected listener to set ViewPager's current item
    onTabSelectedListener = new ViewPagerOnTabSelectedListener(viewPager, smoothScroll);
    tabLayout.addOnTabSelectedListener(onTabSelectedListener);

    // Now we'll populate ourselves from the pager adapter, adding an observer if
    // autoRefresh is enabled
    if (autoRefresh) {
      // Register our observer on the new adapter
      pagerAdapterObserver = new PagerAdapterObserver();
      rvAdapter.registerAdapterDataObserver(pagerAdapterObserver);
    }
    //        populateTabsFromPagerAdapter();
    //        setDefaultPosition(viewPager.getCurrentItem());
    // Now update the scroll position to match the ViewPager's current item
    tabLayout.selectTab(tabLayout.getTabAt(viewPager.getCurrentItem()));
    tabLayout.setScrollPosition(viewPager.getCurrentItem(), 0f, true, true, true);
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
      if (autoRefresh && rvAdapter != null) {
          rvAdapter.unregisterAdapterDataObserver(pagerAdapterObserver);
          pagerAdapterObserver = null;
      }
      tabLayout.removeOnTabSelectedListener(onTabSelectedListener);
      viewPager.unregisterOnPageChangeCallback(onPageChangeCallback);
      onTabSelectedListener = null;
      onPageChangeCallback = null;
      rvAdapter = null;
      attached = false;
  }

  private void populateTabsFromPagerAdapter() {
    if (rvAdapter != null) {
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

  private static class TabLayoutOnPageChangeCallback extends ViewPager2.OnPageChangeCallback {
    @NonNull private final WeakReference<ITabLayout> tabLayoutRef;
    private int previousScrollState;
    private int scrollState;

    TabLayoutOnPageChangeCallback(ITabLayout tabLayout) {
      tabLayoutRef = new WeakReference<>(tabLayout);
      reset();
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
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
      ITabLayout tabLayout = tabLayoutRef.get();
      if (tabLayout != null) {
        // Only update the tab view selection if we're not settling, or we are settling after
        // being dragged
        boolean updateSelectedTabView =
            scrollState != ViewPager2.SCROLL_STATE_SETTLING
                || previousScrollState == ViewPager2.SCROLL_STATE_DRAGGING;
        // Update the indicator if we're not settling after being idle. This is caused
        // from a setCurrentItem() call and will be handled by an animation from
        // onPageSelected() instead.
        boolean updateIndicator =
            !(scrollState == ViewPager2.SCROLL_STATE_SETTLING
                && previousScrollState == ViewPager2.SCROLL_STATE_IDLE);
        tabLayout.setScrollPosition(
            position, positionOffset, updateSelectedTabView, updateIndicator, false);
      }
    }

    @Override
    public void onPageSelected(final int position) {
      ITabLayout tabLayout = tabLayoutRef.get();
      if (tabLayout != null
          && tabLayout.getSelectedTabPosition() != position
          && position < tabLayout.getTabCount()) {
        // Select the tab, only updating the indicator if we're not being dragged/settled
        // (since onPageScrolled will handle that).
        boolean updateIndicator =
            scrollState == ViewPager2.SCROLL_STATE_IDLE
                || (scrollState == ViewPager2.SCROLL_STATE_SETTLING
                    && previousScrollState == ViewPager2.SCROLL_STATE_IDLE);
        tabLayout.selectTab(tabLayout.getTabAt(position), updateIndicator);
      }
    }

    void reset() {
      previousScrollState = scrollState = ViewPager2.SCROLL_STATE_IDLE;
    }
  }

  private static class ViewPagerOnTabSelectedListener implements OnTabSelectedListener {
    private final ViewPager2 viewPager;
    private final boolean smoothScroll;

    ViewPagerOnTabSelectedListener(ViewPager2 viewPager, boolean smoothScroll) {
      this.viewPager = viewPager;
      this.smoothScroll = smoothScroll;
    }

    @Override
    public void onTabSelected(@NonNull TabHolder tab) {
      viewPager.setCurrentItem(tab.getPosition(), smoothScroll);
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

  private class PagerAdapterObserver extends RecyclerView.AdapterDataObserver {
    PagerAdapterObserver() {}

    @Override
    public void onChanged() {
      populateTabsFromPagerAdapter();
    }

    @Override
    public void onItemRangeChanged(int positionStart, int itemCount) {
      populateTabsFromPagerAdapter();
    }

    @Override
    public void onItemRangeChanged(int positionStart, int itemCount, @Nullable Object payload) {
      populateTabsFromPagerAdapter();
    }

    @Override
    public void onItemRangeInserted(int positionStart, int itemCount) {
      populateTabsFromPagerAdapter();
    }

    @Override
    public void onItemRangeRemoved(int positionStart, int itemCount) {
      populateTabsFromPagerAdapter();
    }

    @Override
    public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
      populateTabsFromPagerAdapter();
    }
  }
}
