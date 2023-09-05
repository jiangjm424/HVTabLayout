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

import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import androidx.annotation.Dimension;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.CollectionInfoCompat;
import androidx.viewpager.widget.ViewPager;
import java.util.ArrayList;
import jm.droid.lib.hvtablayout.R;
import jm.droid.lib.tab.internal.ViewUtils;

public class HTabLayout extends HorizontalScrollView implements ITabLayout {

  private static final int[] STYLEABLE_TAB_LAYOUT = R.styleable.HVTabLayout;
  private static final int ANIMATION_DURATION = 300;

  private static final String LOG_TAG = "TabLayout";

  @Override
  public int getIndicatorPosition() {
    return indicatorPosition;
  }

  @Override
  public void setIndicatorPosition(int indicatorPosition) {
    this.indicatorPosition = indicatorPosition;
  }

  // indicatorPosition keeps track of where the indicator is.
  private int indicatorPosition = C.INVALID_POSITION;

  private final ArrayList<TabHolder> tabs = new ArrayList<>();
  @Nullable private TabHolder selectedTab;

  @NonNull final SlidingTabIndicator slidingTabIndicator;

  private final TabLayoutAdapterDataObserver mObserver = new TabLayoutAdapterDataObserver(this);

  private final int contentInsetStart;

  @C.TabGravity int tabGravity;
  int tabIndicatorAnimationDuration;
  @C.Mode int mode;

  final TimeInterpolator tabIndicatorTimeInterpolator;

  @Override
  public TimeInterpolator getTabIndicatorTimeInterpolator() {
    return tabIndicatorTimeInterpolator;
  }

  private final ArrayList<OnTabSelectedListener> selectedListeners = new ArrayList<>();

  private ValueAnimator scrollAnimator;

  private TabAdapter mAdapter;
  private @Nullable TabIndicator tabIndicator;

  private int viewPagerScrollState;

  public HTabLayout(@NonNull Context context) {
    this(context, null);
  }

  public HTabLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, -1);
  }

  public HTabLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    // Ensure we are using the correctly themed context rather than the context that was passed in.
    context = getContext();

    // Disable the Scroll Bar
    setHorizontalScrollBarEnabled(false);

    // Add the TabStrip
    slidingTabIndicator = new SlidingTabIndicator(context, this);
    super.addView(
        slidingTabIndicator,
        0,
        new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));

    TypedArray a = context.obtainStyledAttributes(attrs, STYLEABLE_TAB_LAYOUT);
    tabIndicatorAnimationDuration =
        a.getInt(R.styleable.HVTabLayout_hvTabIndicatorAnimationDuration, ANIMATION_DURATION);

    if (a.hasValue(R.styleable.HVTabLayout_hvTabIndicatorTimeInterpolator)) {
      tabIndicatorTimeInterpolator =
          ViewUtils.parseTimeInterpolator(
              context, a.getString(R.styleable.HVTabLayout_hvTabIndicatorTimeInterpolator));
    } else {
      tabIndicatorTimeInterpolator = new AccelerateDecelerateInterpolator();
    }
    contentInsetStart = a.getDimensionPixelSize(R.styleable.HVTabLayout_hvTabContentStart, 0);
    // noinspection WrongConstant
    mode = a.getInt(R.styleable.HVTabLayout_hvTabMode, C.MODE_FIXED);
    tabGravity = a.getInt(R.styleable.HVTabLayout_hvTabGravity, C.GRAVITY_FILL);
    a.recycle();
    // Now apply the tab mode and gravity
    applyModeAndGravity();
  }

  @Override
  public void draw(@NonNull Canvas canvas, @NonNull IndicatorElement element) {
    if (tabIndicator != null) tabIndicator.draw(canvas, element);
  }

  /**
   * Set the scroll position of the {@link HTabLayout}.
   *
   * @param position Position of the tab to scroll.
   * @param positionOffset Value from [0, 1) indicating the offset from {@code position}.
   * @param updateSelectedTabView Whether to draw the tab at the specified position + positionOffset
   *     as selected.
   *     <p>Note that calling the method with {@code updateSelectedTabView = true} <em>does not</em>
   *     select a tab at the specified position, but only <em>draws it as selected</em>. This can be
   *     useful for when the TabLayout behavior needs to be linked to another view, such as {@link
   *     ViewPager}.
   * @see #setScrollPosition(int, float, boolean, boolean)
   */
  public void setScrollPosition(int position, float positionOffset, boolean updateSelectedTabView) {
    setScrollPosition(position, positionOffset, updateSelectedTabView, true);
  }

  /**
   * Set the scroll position of the {@link HTabLayout}.
   *
   * @param position Position of the tab to scroll.
   * @param positionOffset Value from [0, 1) indicating the offset from {@code position}.
   * @param updateSelectedTabView Whether to draw the tab at the specified position + positionOffset
   *     as selected.
   *     <p>Note that calling the method with {@code updateSelectedTabView = true} <em>does not</em>
   *     select a tab at the specified position, but only <em>draws it as selected</em>. This can be
   *     useful for when the TabLayout behavior needs to be linked to another view, such as {@link
   *     ViewPager}.
   * @param updateIndicatorPosition Whether to set the indicator to the specified position and
   *     offset.
   *     <p>Note that calling the method with {@code updateIndicatorPosition = true} <em>does
   *     not</em> select a tab at the specified position, but only updates the indicator position.
   *     This can be useful for when the TabLayout behavior needs to be linked to another view, such
   *     as {@link ViewPager}.
   * @see #setScrollPosition(int, float, boolean)
   */
  public void setScrollPosition(
      int position,
      float positionOffset,
      boolean updateSelectedTabView,
      boolean updateIndicatorPosition) {
    setScrollPosition(
        position,
        positionOffset,
        updateSelectedTabView,
        updateIndicatorPosition,
        /* alwaysScroll= */ true);
  }

  @Override
  public void setScrollPosition(
      int position,
      float positionOffset,
      boolean updateSelectedTabView,
      boolean updateIndicatorPosition,
      boolean alwaysScroll) {
    final int roundedPosition = Math.round(position + positionOffset);
    if (roundedPosition < 0 || roundedPosition >= slidingTabIndicator.getChildCount()) {
      return;
    }

    // Set the indicator position, if enabled
    if (updateIndicatorPosition) {
      slidingTabIndicator.setIndicatorPositionFromTabPosition(position, positionOffset);
    }

    // Now update the scroll position, canceling any running animation
    if (scrollAnimator != null && scrollAnimator.isRunning()) {
      scrollAnimator.cancel();
    }
    int scrollXForPosition = calculateScrollXForTab(position, positionOffset);
    int scrollX = getScrollX();
    // If the position is smaller than the selected tab position, the position is getting larger
    // to reach the selected tab position so scrollX is increasing.
    // We only want to update the scroll position if the new scroll position is greater than
    // the current scroll position.
    // Conversely if the position is greater than the selected tab position, the position is
    // getting smaller to reach the selected tab position so scrollX is decreasing.
    // We only update the scroll position if the new scroll position is less than the current
    // scroll position.
    // Lastly if the position is equal to the selected position, we want to set the scroll
    // position which also updates the selected tab view and the indicator.
    boolean toMove =
        (position < getSelectedTabPosition() && scrollXForPosition >= scrollX)
            || (position > getSelectedTabPosition() && scrollXForPosition <= scrollX)
            || (position == getSelectedTabPosition());
    // If the layout direction is RTL, the scrollXForPosition and scrollX comparisons are
    // reversed since scrollX values remain the same in RTL but tab positions go RTL.
    if (ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL) {
      toMove =
          (position < getSelectedTabPosition() && scrollXForPosition <= scrollX)
              || (position > getSelectedTabPosition() && scrollXForPosition >= scrollX)
              || (position == getSelectedTabPosition());
    }
    // We want to scroll if alwaysScroll is true, the viewpager is being dragged, or if we should
    // scroll by the rules above.
    if (toMove || viewPagerScrollState == ViewPager.SCROLL_STATE_DRAGGING || alwaysScroll) {
      scrollTo(position < 0 ? 0 : scrollXForPosition, 0);
    }

    // Update the 'selected state' view as we scroll, if enabled
    if (updateSelectedTabView) {
      setSelectedTabView(roundedPosition);
    }
  }

  @Nullable
  @Override
  public TabAdapter getAdapter() {
    return mAdapter;
  }

  @Nullable
  @Override
  public void setAdapter(@Nullable TabAdapter adapter) {
    if (mAdapter != null) {
      mAdapter.unregisterAdapterDataObserver(mObserver);
      mAdapter.onDetachedFromTabLayout(this);
      tabs.clear();
      selectedTab = null;
      tabIndicator = null;
      removeAllTabs();
    }
    if (adapter == null) {
      return;
    }
    mAdapter = adapter;
    mAdapter.onAttachedToTabLayout(this);
    mAdapter.registerAdapterDataObserver(mObserver);
    tabIndicator = mAdapter.createTabIndicator(getContext());
    for (int i = 0; i < mAdapter.getTabCount(); i++) {
      TabHolder tab = mAdapter.createTabHolder(slidingTabIndicator);
      tab.parent = this;
      mAdapter.bindTabHolder(tab, i);
      addTab(tab);
    }
  }

  /**
   * Add a tab to this layout. The tab will be added at the end of the list. If this is the first
   * tab to be added it will become the selected tab.
   *
   * @param tab Tab to add
   */
  final void addTab(@NonNull TabHolder tab) {
    addTab(tab, tabs.isEmpty());
  }

  /**
   * Add a tab to this layout. The tab will be inserted at <code>position</code>. If this is the
   * first tab to be added it will become the selected tab.
   *
   * @param tab The tab to add
   * @param position The new position of the tab
   */
  final void addTab(@NonNull TabHolder tab, int position) {
    addTab(tab, position, tabs.isEmpty());
  }

  /**
   * Add a tab to this layout. The tab will be added at the end of the list.
   *
   * @param tab Tab to add
   * @param setSelected True if the added tab should become the selected tab.
   */
  final void addTab(@NonNull TabHolder tab, boolean setSelected) {
    addTab(tab, tabs.size(), setSelected);
  }

  /**
   * Add a tab to this layout. The tab will be inserted at <code>position</code>.
   *
   * @param tab The tab to add
   * @param position The new position of the tab
   * @param setSelected True if the added tab should become the selected tab.
   */
  final void addTab(@NonNull TabHolder tab, int position, boolean setSelected) {
    if (tab.parent != this) {
      throw new IllegalArgumentException("Tab belongs to a different TabLayout.");
    }
    configureTab(tab, position);
    addTabView(tab);

    if (setSelected) {
      tab.select();
    }
  }

  private boolean isScrollingEnabled() {
    return getTabMode() == C.MODE_SCROLLABLE || getTabMode() == C.MODE_AUTO;
  }

  @Override
  public boolean onInterceptTouchEvent(MotionEvent event) {
    // When a touch event is intercepted and the tab mode is fixed, do not continue to process the
    // touch event. This will prevent unexpected scrolling from occurring in corner cases (i.e. a
    // layout in fixed mode that has padding should not scroll for the width of the padding).
    return isScrollingEnabled() && super.onInterceptTouchEvent(event);
  }

  @SuppressLint("ClickableViewAccessibility")
  @Override
  public boolean onTouchEvent(MotionEvent event) {
    if (event.getActionMasked() == MotionEvent.ACTION_SCROLL && !isScrollingEnabled()) {
      return false;
    }
    return super.onTouchEvent(event);
  }

  /**
   * Add a {@link OnTabSelectedListener} that will be invoked when tab selection changes.
   *
   * <p>Components that add a listener should take care to remove it when finished via {@link
   * #removeOnTabSelectedListener(OnTabSelectedListener)}.
   *
   * @param listener listener to add
   */
  @Override
  public void addOnTabSelectedListener(@NonNull OnTabSelectedListener listener) {
    if (!selectedListeners.contains(listener)) {
      selectedListeners.add(listener);
    }
  }

  /**
   * Remove the given {@link OnTabSelectedListener} that was previously added via {@link
   * #addOnTabSelectedListener(OnTabSelectedListener)}.
   *
   * @param listener listener to remove
   */
  @Override
  public void removeOnTabSelectedListener(@NonNull OnTabSelectedListener listener) {
    selectedListeners.remove(listener);
  }

  /** Remove all previously added {@link OnTabSelectedListener}s. */
  public void clearOnTabSelectedListeners() {
    selectedListeners.clear();
  }

  /**
   * Returns the number of tabs currently registered with the tab layout.
   *
   * @return Tab count
   */
  @Override
  public int getTabCount() {
    return tabs.size();
  }

  /** Returns the tab at the specified index. */
  @Nullable
  @Override
  public TabHolder getTabAt(int index) {
    return (index < 0 || index >= getTabCount()) ? null : tabs.get(index);
  }

  /**
   * Returns the position of the current selected tab.
   *
   * @return selected tab position, or {@code -1} if there isn't a selected tab.
   */
  @Override
  public int getSelectedTabPosition() {
    return selectedTab != null ? selectedTab.getPosition() : C.INVALID_POSITION;
  }

  /**
   * Remove a tab from the layout. If the removed tab was selected it will be deselected and another
   * tab will be selected if present.
   *
   * @param tab The tab to remove
   */
  final void removeTab(@NonNull TabHolder tab) {
    if (tab.parent != this) {
      throw new IllegalArgumentException("Tab does not belong to this TabLayout.");
    }

    removeTabAt(tab.getPosition());
  }

  /**
   * Remove a tab from the layout. If the removed tab was selected it will be deselected and another
   * tab will be selected if present.
   *
   * @param position Position of the tab to remove
   */
  @Override
  public final void removeTabAt(int position) {
    final int selectedTabPosition = selectedTab != null ? selectedTab.getPosition() : 0;
    removeTabViewAt(position);
    tabs.remove(position);
    final int newTabCount = tabs.size();
    int newIndicatorPosition = -1;
    for (int i = position; i < newTabCount; i++) {
      // If the current tab position is the indicator position, mark its new position as the new
      // indicator position.
      if (tabs.get(i).getPosition() == indicatorPosition) {
        newIndicatorPosition = i;
      }
      tabs.get(i).setPosition(i);
    }
    // Update the indicator position to the correct selected tab after refreshing tab positions.
    indicatorPosition = newIndicatorPosition;

    if (selectedTabPosition == position) {
      selectTab(tabs.isEmpty() ? null : tabs.get(Math.max(0, position - 1)));
    }
  }

  /** Remove all tabs from the tab layout and deselect the current tab. */
  @Override
  public final void removeAllTabs() {
    // Remove all the tab views
    slidingTabIndicator.removeAllViews();
    // Remove all the tab holder

    tabs.clear();

    selectedTab = null;
  }

  public void setTabMode(@C.Mode int mode) {
    if (mode != this.mode) {
      this.mode = mode;
      applyModeAndGravity();
    }
  }

  @C.Mode
  @Override
  public int getTabMode() {
    return mode;
  }

  @Override
  public void setTabGravity(@C.TabGravity int gravity) {
    if (tabGravity != gravity) {
      tabGravity = gravity;
      applyModeAndGravity();
    }
  }

  @Override
  @C.TabGravity
  public int getTabGravity() {
    return tabGravity;
  }

  @Override
  public void updateViewPagerScrollState(int scrollState) {
    this.viewPagerScrollState = scrollState;
  }

  @Override
  public boolean shouldDelayChildPressedState() {
    // Only delay the pressed state if the tabs can scroll
    return getTabScrollRange() > 0;
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
  }

  protected int getTabScrollRange() { // todo:
    return Math.max(
        0, slidingTabIndicator.getWidth() - getWidth() - getPaddingLeft() - getPaddingRight());
  }

  private void configureTab(@NonNull TabHolder tab, int position) {
    tab.setPosition(position);
    tabs.add(position, tab);

    final int count = tabs.size();
    int newIndicatorPosition = -1;
    for (int i = position + 1; i < count; i++) {
      // If the current tab position is the indicator position, mark its new position as the new
      // indicator position.
      if (tabs.get(i).getPosition() == indicatorPosition) {
        newIndicatorPosition = i;
      }
      tabs.get(i).setPosition(i);
    }
    indicatorPosition = newIndicatorPosition;
  }

  private void addTabView(@NonNull TabHolder tab) {
    final View tabView = tab.itemView;
    tabView.setOnClickListener(
        v -> {
          if (mAdapter.interrupt(tab.getPosition())) {
            return;
          }
          tab.select();
        });
    TabLayoutParams lp = createLayoutParamsForTabs();
    lp.tab = tab;
    slidingTabIndicator.addView(tabView, tab.getPosition(), lp);
  }

  @Override
  public void addView(View child) {
    addViewInternal(child);
  }

  @Override
  public void addView(View child, int index) {
    addViewInternal(child);
  }

  @Override
  public void addView(View child, ViewGroup.LayoutParams params) {
    addViewInternal(child);
  }

  @Override
  public void addView(View child, int index, ViewGroup.LayoutParams params) {
    addViewInternal(child);
  }

  private void addViewInternal(final View child) {}

  @NonNull
  private TabLayoutParams createLayoutParamsForTabs() {
    final TabLayoutParams lp =
        new TabLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
    updateTabViewLayoutParams(lp);
    return lp;
  }

  private void updateTabViewLayoutParams(@NonNull LinearLayout.LayoutParams lp) {
    if (mode == C.MODE_FIXED && tabGravity == C.GRAVITY_FILL) {
      lp.width = 0;
      lp.weight = 1;
    } else {
      lp.width = LinearLayout.LayoutParams.WRAP_CONTENT;
      lp.weight = 0;
    }
  }

  @Override
  public void onInitializeAccessibilityNodeInfo(@NonNull AccessibilityNodeInfo info) {
    super.onInitializeAccessibilityNodeInfo(info);
    AccessibilityNodeInfoCompat infoCompat = AccessibilityNodeInfoCompat.wrap(info);
    infoCompat.setCollectionInfo(
        CollectionInfoCompat.obtain(
            /* rowCount= */ 1,
            /* columnCount= */ getTabCount(),
            /* hierarchical= */ false,
            /* selectionMode= */ CollectionInfoCompat.SELECTION_MODE_SINGLE));
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    // If we have a MeasureSpec which allows us to decide our height, try and use the default
    // height
    final int idealHeight = Math.round(ViewUtils.dpToPx(getContext(), getDefaultHeight()));
    switch (MeasureSpec.getMode(heightMeasureSpec)) {
      case MeasureSpec.AT_MOST:
        if (getChildCount() == 1 && MeasureSpec.getSize(heightMeasureSpec) >= idealHeight) {
          getChildAt(0).setMinimumHeight(idealHeight);
        }
        break;
      case MeasureSpec.UNSPECIFIED:
        heightMeasureSpec =
            MeasureSpec.makeMeasureSpec(
                idealHeight + getPaddingTop() + getPaddingBottom(), MeasureSpec.EXACTLY);
        break;
      default:
        break;
    }

    // Now super measure itself using the (possibly) modified height spec
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    if (getChildCount() == 1) {
      // If we're in fixed mode then we need to make sure the tab strip is the same width as us
      // so we don't scroll
      final View child = getChildAt(0);
      boolean remeasure = false;

      switch (mode) {
        case C.MODE_AUTO:
        case C.MODE_SCROLLABLE:
          // We only need to resize the child if it's smaller than us. This is similar
          // to fillViewport
          remeasure = child.getMeasuredWidth() < getMeasuredWidth();
          break;
        case C.MODE_FIXED:
          // Resize the child so that it doesn't scroll
          remeasure = child.getMeasuredWidth() != getMeasuredWidth();
          break;
      }

      if (remeasure) {
        // Re-measure the child with a widthSpec set to be exactly our measure width
        int childHeightMeasureSpec =
            getChildMeasureSpec(
                heightMeasureSpec,
                getPaddingTop() + getPaddingBottom(),
                child.getLayoutParams().height);

        int childWidthMeasureSpec =
            MeasureSpec.makeMeasureSpec(getMeasuredWidth(), MeasureSpec.EXACTLY);
        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
      }
    }
  }

  private void removeTabViewAt(int position) {
    slidingTabIndicator.removeViewAt(position);
    requestLayout();
  }

  private void animateToTab(int newPosition) {
    if (newPosition == C.INVALID_POSITION) {
      return;
    }

    if (getWindowToken() == null
        || !ViewCompat.isLaidOut(this)
        || slidingTabIndicator.childrenNeedLayout()) {
      // If we don't have a window token, or we haven't been laid out yet just draw the new
      // position now
      setScrollPosition(newPosition, 0f, true);
      return;
    }

    final int startScrollX = getScrollX();
    final int targetScrollX = calculateScrollXForTab(newPosition, 0);

    if (startScrollX != targetScrollX) {
      ensureScrollAnimator();

      scrollAnimator.setIntValues(startScrollX, targetScrollX);
      scrollAnimator.start();
    }

    // Now animate the indicator
    slidingTabIndicator.animateIndicatorToPosition(newPosition, tabIndicatorAnimationDuration);
  }

  private void ensureScrollAnimator() {
    if (scrollAnimator == null) {
      scrollAnimator = new ValueAnimator();
      scrollAnimator.setInterpolator(tabIndicatorTimeInterpolator);
      scrollAnimator.setDuration(tabIndicatorAnimationDuration);
      scrollAnimator.addUpdateListener(animator -> scrollTo((int) animator.getAnimatedValue(), 0));
    }
  }

  void setScrollAnimatorListener(ValueAnimator.AnimatorListener listener) {
    ensureScrollAnimator();
    scrollAnimator.addListener(listener);
  }

  /**
   * Called when a tab is selected. Unselects all other tabs in the TabLayout.
   *
   * @param position Position of the selected tab.
   */
  private void setSelectedTabView(int position) {
    final int tabCount = slidingTabIndicator.getChildCount();
    if (position < tabCount) {
      for (int i = 0; i < tabCount; i++) {
        final View child = slidingTabIndicator.getChildAt(i);
        final TabLayoutParams lp = (TabLayoutParams) child.getLayoutParams();
        final TabHolder tab = lp.getTab();
        // Update the tab view if it needs to be updated (eg. it's newly selected and it is not
        // yet selected, or it is selected and something else was selected).
        if ((i == position && !tab.isActivated()) || (i != position && tab.isActivated())) {
          tab.activated = i == position;
          mAdapter.onTabActivatedChanged(tab);
        }
      }
    }
  }

  public void selectTab(int index) {
    if (index > -1 && index < tabs.size()) {
      selectTab(getTabAt(index));
    }
  }

  /**
   * Selects the given tab.
   *
   * @param tab The tab to select, or {@code null} to select none.
   * @see #selectTab(TabHolder, boolean)
   */
  @Override
  public void selectTab(@Nullable TabHolder tab) {
    selectTab(tab, true);
  }

  /**
   * Selects the given tab. Will always animate to the selected tab if the current tab is
   * reselected, regardless of the value of {@code updateIndicator}.
   *
   * @param tab The tab to select, or {@code null} to select none.
   * @param updateIndicator Whether to update the indicator.
   * @see #selectTab(TabHolder)
   */
  @Override
  public void selectTab(@Nullable final TabHolder tab, boolean updateIndicator) {
    final TabHolder currentTab = selectedTab;

    if (currentTab == tab) {
      if (currentTab != null) {
        dispatchTabReselected(tab);
        animateToTab(tab.getPosition());
      }
    } else {
      final int newPosition = tab != null ? tab.getPosition() : C.INVALID_POSITION;
      if (updateIndicator) {
        if ((currentTab == null || currentTab.getPosition() == C.INVALID_POSITION)
            && newPosition != C.INVALID_POSITION) {
          // If we don't currently have a tab, just draw the indicator
          setScrollPosition(newPosition, 0f, true);
        } else {
          animateToTab(newPosition);
        }
        if (newPosition != C.INVALID_POSITION) {
          setSelectedTabView(newPosition);
        }
      }
      // Setting selectedTab before dispatching 'tab unselected' events, so that currentTab's state
      // will be interpreted as unselected
      selectedTab = tab;
      // If the current tab is still attached to the TabLayout.
      if (currentTab != null && currentTab.parent != null) {
        dispatchTabUnselected(currentTab);
      }
      if (tab != null) {
        dispatchTabSelected(tab);
      }
    }
  }

  private void dispatchTabSelected(@NonNull final TabHolder tab) {
    for (int i = selectedListeners.size() - 1; i >= 0; i--) {
      selectedListeners.get(i).onTabSelected(tab);
    }
  }

  private void dispatchTabUnselected(@NonNull final TabHolder tab) {
    for (int i = selectedListeners.size() - 1; i >= 0; i--) {
      selectedListeners.get(i).onTabUnselected(tab);
    }
  }

  private void dispatchTabReselected(@NonNull final TabHolder tab) {
    for (int i = selectedListeners.size() - 1; i >= 0; i--) {
      selectedListeners.get(i).onTabReselected(tab);
    }
  }

  private int calculateScrollXForTab(int position, float positionOffset) {
    if (mode == C.MODE_SCROLLABLE || mode == C.MODE_AUTO) {
      final View selectedChild = slidingTabIndicator.getChildAt(position);
      if (selectedChild == null) {
        return 0;
      }
      final View nextChild =
          position + 1 < slidingTabIndicator.getChildCount()
              ? slidingTabIndicator.getChildAt(position + 1)
              : null;
      final int selectedWidth = selectedChild.getWidth();
      final int nextWidth = nextChild != null ? nextChild.getWidth() : 0;

      // base scroll amount: places center of tab in center of parent
      int scrollBase = selectedChild.getLeft() + (selectedWidth / 2) - (getWidth() / 2);
      // offset amount: fraction of the distance between centers of tabs
      int scrollOffset = (int) ((selectedWidth + nextWidth) * 0.5f * positionOffset);

      return (ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_LTR)
          ? scrollBase + scrollOffset
          : scrollBase - scrollOffset;
    }
    return 0;
  }

  private void applyModeAndGravity() {
    int paddingStart = 0;
    if (mode == C.MODE_SCROLLABLE || mode == C.MODE_AUTO) {
      // If we're scrollable, or fixed at start, inset using padding
      paddingStart = Math.max(0, contentInsetStart);
    }
    ViewCompat.setPaddingRelative(slidingTabIndicator, paddingStart, 0, 0, 0);

    switch (mode) {
      case C.MODE_AUTO:
      case C.MODE_FIXED:
        if (tabGravity == C.GRAVITY_START) {
          Log.w(
              LOG_TAG,
              "GRAVITY_START is not supported with the current tab mode, GRAVITY_CENTER will be"
                  + " used instead");
        }
        slidingTabIndicator.setGravity(Gravity.CENTER_HORIZONTAL);
        break;
      case C.MODE_SCROLLABLE:
        applyGravityForModeScrollable(tabGravity);
        break;
    }

    updateTabViews(true);
  }

  private void applyGravityForModeScrollable(int tabGravity) {
    switch (tabGravity) {
      case C.GRAVITY_CENTER:
        slidingTabIndicator.setGravity(Gravity.CENTER_HORIZONTAL);
        break;
      case C.GRAVITY_FILL:
        Log.w(
            LOG_TAG,
            "MODE_SCROLLABLE + GRAVITY_FILL is not supported, GRAVITY_START will be used"
                + " instead");
        // Fall through
      case C.GRAVITY_START:
        slidingTabIndicator.setGravity(GravityCompat.START);
        break;
      default:
        break;
    }
  }

  @Override
  public void updateTabViews(final boolean requestLayout) {
    for (int i = 0; i < slidingTabIndicator.getChildCount(); i++) {
      View child = slidingTabIndicator.getChildAt(i);
      child.setMinimumWidth(getTabMinWidth());
      updateTabViewLayoutParams((LinearLayout.LayoutParams) child.getLayoutParams());
      if (requestLayout) {
        child.requestLayout();
      }
    }
  }

  @NonNull
  private static ColorStateList createColorStateList(int defaultColor, int selectedColor) {
    final int[][] states = new int[2][];
    final int[] colors = new int[2];
    int i = 0;

    states[i] = SELECTED_STATE_SET;
    colors[i] = selectedColor;
    i++;

    // Default enabled state
    states[i] = EMPTY_STATE_SET;
    colors[i] = defaultColor;
    i++;

    return new ColorStateList(states, colors);
  }

  @Dimension(unit = Dimension.DP)
  private int getDefaultHeight() {
    return 10;
  }

  private int getTabMinWidth() {
    //        if (requestedTabMinWidth != INVALID_WIDTH) {
    // If we have been given a min width, use it
    //            return requestedTabMinWidth;
    //        }
    // Else, we'll use the default value
    //        return (mode == MODE_SCROLLABLE || mode == MODE_AUTO) ? scrollableTabMinWidth : 0;
    return 0;
  }

  @Override
  public LayoutParams generateLayoutParams(AttributeSet attrs) {
    // We don't care about the layout params of any views added to us, since we don't actually
    // add them. The only view we add is the SlidingTabStrip, which is done manually.
    // We return the default layout params so that we don't blow up if we're given a TabItem
    // without android:layout_* values.
    return generateDefaultLayoutParams();
  }

  @Override
  public final void notifyChanged() {
    removeAllTabs();
    indicatorPosition = C.INVALID_POSITION;
    if (mAdapter != null) {
      final int N = mAdapter.getTabCount();
      for (int i = 0; i < N; i++) {
        TabHolder tab = mAdapter.createTabHolder(slidingTabIndicator);
        tab.parent = HTabLayout.this;
        mAdapter.bindTabHolder(tab, i);
        addTab(tab, false);
      }
    }
  }

  @Override
  public final void insertTabAt(int position) {
    Log.i("jiang-tab-size", "notifyTabInserted :" + position);
    TabHolder tab = mAdapter.createTabHolder(slidingTabIndicator);
    tab.parent = HTabLayout.this;
    mAdapter.bindTabHolder(tab, position);
    addTab(tab);
  }

  @Override
  public final void updateTabAt(int position) {
    final TabHolder tab = tabs.get(position);
    mAdapter.bindTabHolder(tab, position);
  }
}
