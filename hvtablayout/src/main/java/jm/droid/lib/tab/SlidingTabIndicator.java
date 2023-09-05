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

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.annotation.Dimension;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.core.view.ViewCompat;
import jm.droid.lib.tab.internal.ViewUtils;

@SuppressLint("ViewConstructor")
@RestrictTo(RestrictTo.Scope.LIBRARY)
class SlidingTabIndicator extends LinearLayout {
  @Dimension(unit = Dimension.DP)
  private static final int FIXED_WRAP_GUTTER_MIN = 16;

  private final ITabLayout tabLayout;
  ValueAnimator indicatorAnimator;

  private int layoutDirection = -1;
  private final IndicatorElement indicatorElement = new IndicatorElement();

  SlidingTabIndicator(Context context, @NonNull ITabLayout layout) {
    super(context);
    setWillNotDraw(false);
    tabLayout = layout;
  }

  boolean childrenNeedLayout() {
    for (int i = 0, z = getChildCount(); i < z; i++) {
      final View child = getChildAt(i);
      if (child.getWidth() <= 0) {
        return true;
      }
    }
    return false;
  }

  /**
   * Set the indicator position based on an offset between two adjacent tabs.
   *
   * @param position Position index of the first tab (with less index) currently being displayed.
   *     Tab position+1 will be visible if positionOffset is nonzero.
   * @param positionOffset Value from [0, 1) indicating the offset from the tab at position.
   */
  void setIndicatorPositionFromTabPosition(int position, float positionOffset) {
    // Since we are tweening the indicator in between the position and position+positionOffset,
    // we set the indicator position to whichever is closer.
    tabLayout.setIndicatorPosition(Math.round(position + positionOffset));
    if (indicatorAnimator != null && indicatorAnimator.isRunning()) {
      indicatorAnimator.cancel();
    }

    // The title view refers to the one indicated when offset is 0.
    final View firstTitle = getChildAt(position);
    // The title view refers to the one indicated when offset is 1.
    final View nextTitle = getChildAt(position + 1);

    tweenIndicatorPosition(firstTitle, nextTitle, positionOffset);
  }

  @Override
  public void onRtlPropertiesChanged(int layoutDirection) {
    super.onRtlPropertiesChanged(layoutDirection);

    // Workaround for a bug before Android M where LinearLayout did not re-layout itself when
    // layout direction changed
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
      if (this.layoutDirection != layoutDirection) {
        requestLayout();
        this.layoutDirection = layoutDirection;
      }
    }
  }

  @Override
  protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    if (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY) {
      // HorizontalScrollView will first measure use with UNSPECIFIED, and then with
      // EXACTLY. Ignore the first call since anything we do will be overwritten anyway
      return;
    }

    // GRAVITY_CENTER will make all tabs the same width as the largest tab, and center them in the
    // SlidingTabIndicator's width (with a "gutter" of padding on either side). If the Tabs do not
    // fit in the SlidingTabIndicator, then fall back to GRAVITY_FILL behavior.
    if ((tabLayout.getTabGravity() == C.GRAVITY_CENTER) || tabLayout.getTabMode() == C.MODE_AUTO) {
      boolean remeasure = false;
      if (getOrientation() == LinearLayout.HORIZONTAL) {
        remeasure = measureH();
      } else {
        remeasure = measureV();
      }
      if (remeasure) {
        // Now re-measure after our changes
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
      }
    }
  }

  private boolean measureH() {
    final int count = getChildCount();
    boolean remeasure = false;
    // First we'll find the widest tab
    int largestTabWidth = 0;
    for (int i = 0, z = count; i < z; i++) {
      View child = getChildAt(i);
      if (child.getVisibility() == VISIBLE) {
        largestTabWidth = Math.max(largestTabWidth, child.getMeasuredWidth());
      }
    }

    if (largestTabWidth <= 0) {
      // If we don't have a largest child yet, skip until the next measure pass
      return false;
    }

    final int gutter = (int) ViewUtils.dpToPx(getContext(), FIXED_WRAP_GUTTER_MIN);

    if (largestTabWidth * count <= getMeasuredWidth() - gutter * 2) {
      // If the tabs fit within our width minus gutters, we will set all tabs to have
      // the same width
      for (int i = 0; i < count; i++) {
        final LayoutParams lp = (LayoutParams) getChildAt(i).getLayoutParams();
        if (lp.width != largestTabWidth || lp.weight != 0) {
          lp.width = largestTabWidth;
          lp.weight = 0;
          remeasure = true;
        }
      }
    } else {
      // If the tabs will wrap to be larger than the width minus gutters, we need
      // to switch to GRAVITY_FILL.
      // TODO (b/129799806): This overrides the user TabGravity setting.
      tabLayout.setTabGravity(C.GRAVITY_FILL);
      tabLayout.updateTabViews(false);
      remeasure = true;
    }
    return remeasure;
  }

  private boolean measureV() {
    final int count = getChildCount();
    boolean remeasure = false;
    // First we'll find the widest tab
    int largestTabHeight = 0;
    for (int i = 0, z = count; i < z; i++) {
      View child = getChildAt(i);
      if (child.getVisibility() == VISIBLE) {
        largestTabHeight = Math.max(largestTabHeight, child.getMeasuredHeight());
      }
    }

    if (largestTabHeight <= 0) {
      // If we don't have a largest child yet, skip until the next measure pass
      return false;
    }

    final int gutter = (int) ViewUtils.dpToPx(getContext(), FIXED_WRAP_GUTTER_MIN);

    if (largestTabHeight * count <= getMeasuredHeight() - gutter * 2) {
      // If the tabs fit within our width minus gutters, we will set all tabs to have
      // the same width
      for (int i = 0; i < count; i++) {
        final LayoutParams lp = (LayoutParams) getChildAt(i).getLayoutParams();
        if (lp.height != largestTabHeight || lp.weight != 0) {
          lp.height = largestTabHeight;
          lp.weight = 0;
          remeasure = true;
        }
      }
    } else {
      // If the tabs will wrap to be larger than the width minus gutters, we need
      // to switch to GRAVITY_FILL.
      // TODO (b/129799806): This overrides the user TabGravity setting.
      tabLayout.setTabGravity(C.GRAVITY_FILL);
      tabLayout.updateTabViews(false);
      remeasure = true;
    }
    return remeasure;
  }

  @Override
  protected void onLayout(boolean changed, int l, int t, int r, int b) {
    super.onLayout(changed, l, t, r, b);

    if (indicatorAnimator != null && indicatorAnimator.isRunning()) {
      // It's possible that the tabs' layout is modified while the indicator is animating (ex. a
      // new tab is added, or a tab is removed in onTabSelected). This would change the target end
      // position of the indicator, since the tab widths are different. We need to modify the
      // animation's updateListener to pick up the new target positions.
      updateOrRecreateIndicatorAnimation(
          /* recreateAnimation= */ false, tabLayout.getSelectedTabPosition(), /* duration= */ -1);
    } else {
      // If we've been laid out, update the indicator position
      jumpIndicatorToIndicatorPosition();
    }
  }

  /**
   * Immediately update the indicator position to the specified position, unless we are mid-scroll
   * in a viewpager.
   */
  private void jumpIndicatorToPosition(int position) {
    // Don't update the indicator position if the scroll state is not idle, and the indicator
    // is drawn.
    if (position == C.INVALID_POSITION) {
      indicatorElement.start = indicatorElement.end = null;
    } else {
      final ViewGroup.LayoutParams lp = getChildAt(position).getLayoutParams();
      if (lp instanceof TabLayoutParams) {
        indicatorElement.start = ((TabLayoutParams) lp).getTab();
      }
    }
    tabLayout.setIndicatorPosition(position);
  }

  /** Immediately update the indicator position to the currently selected position. */
  private void jumpIndicatorToSelectedPosition() {
    jumpIndicatorToPosition(tabLayout.getSelectedTabPosition());
  }

  /** Immediately update the indicator position to the current indicator position. */
  private void jumpIndicatorToIndicatorPosition() {
    // If indicator position has not yet been set, set indicator to the selected tab position.
    if (tabLayout.getIndicatorPosition() == C.INVALID_POSITION) {
      tabLayout.setIndicatorPosition(tabLayout.getSelectedTabPosition());
    }
    jumpIndicatorToPosition(tabLayout.getIndicatorPosition());
  }

  /**
   * Update the position of the indicator by tweening between the currently selected tab and the
   * destination tab.
   *
   * <p>This method is called for each frame when either animating the indicator between
   * destinations or driving an animation through gesture, such as with a viewpager.
   *
   * @param startTitle The tab which should be selected (as marked by the indicator), when fraction
   *     is 0.0.
   * @param endTitle The tab which should be selected (as marked by the indicator), when fraction is
   *     1.0.
   * @param fraction A value between 0.0 and 1.0 that indicates how far between currentTitle and
   *     endTitle the indicator should be drawn. e.g. If a viewpager attached to this TabLayout is
   *     currently half way slid between page 0 and page 1, fraction will be 0.5.
   */
  private void tweenIndicatorPosition(
      @Nullable View startTitle, @Nullable View endTitle, float fraction) {
    boolean hasVisibleTitle = startTitle != null && startTitle.getWidth() > 0;
    if (hasVisibleTitle) {
      indicatorElement.start = ((TabLayoutParams) startTitle.getLayoutParams()).getTab();
      if (endTitle != null) {
        indicatorElement.end = ((TabLayoutParams) endTitle.getLayoutParams()).getTab();
      } else {
        indicatorElement.end = indicatorElement.start;
      }
      indicatorElement.fraction = fraction;
    } else {
      indicatorElement.start = indicatorElement.end = null;
      indicatorElement.fraction = 0f;
    }
    Log.i("jiang", "start:" + startTitle);
    Log.i("jiang", "endTitle:" + endTitle);
    Log.i("jiang", "fraction:" + fraction);

    ViewCompat.postInvalidateOnAnimation(this);
  }

  /**
   * Animate the position of the indicator from its current position to a new position.
   *
   * <p>This is typically used when a tab destination is tapped. If the indicator should be moved as
   * a result of a gesture, see {@link #setIndicatorPositionFromTabPosition(int, float)}.
   *
   * @param position The new position to animate the indicator to.
   * @param duration The duration over which the animation should take place.
   */
  void animateIndicatorToPosition(final int position, int duration) {
    if (indicatorAnimator != null
        && indicatorAnimator.isRunning()
        && tabLayout.getIndicatorPosition() != position) {
      indicatorAnimator.cancel();
    }

    updateOrRecreateIndicatorAnimation(/* recreateAnimation= */ true, position, duration);
  }

  /**
   * Animate the position of the indicator from its current position to a new position.
   *
   * @param recreateAnimation Whether a currently running animator should be re-targeted to move the
   *     indicator to it's new position.
   * @param position The new position to animate the indicator to.
   * @param duration The duration over which the animation should take place.
   */
  private void updateOrRecreateIndicatorAnimation(
      boolean recreateAnimation, final int position, int duration) {
    // If the indicator position is already the target position, we don't need to update the
    // indicator animation because nothing has changed.
    if (tabLayout.getIndicatorPosition() == position) {
      return;
    }
    final View currentView = getChildAt(tabLayout.getSelectedTabPosition());
    final View targetView = getChildAt(position);
    if (targetView == null) {
      // If we don't have a view, just update the position now and return
      jumpIndicatorToSelectedPosition();
      return;
    }
    tabLayout.setIndicatorPosition(position);

    // Create the update listener with the new target indicator positions. If we're not recreating
    // then animationStartLeft/Right will be the same as when the previous animator was created.
    ValueAnimator.AnimatorUpdateListener updateListener =
        valueAnimator ->
            tweenIndicatorPosition(currentView, targetView, valueAnimator.getAnimatedFraction());

    if (recreateAnimation) {
      // Create & start a new indicatorAnimator.
      ValueAnimator animator = indicatorAnimator = new ValueAnimator();
      animator.setInterpolator(tabLayout.getTabIndicatorTimeInterpolator());
      animator.setDuration(duration);
      animator.setFloatValues(0F, 1F);
      animator.addUpdateListener(updateListener);
      animator.start();
    } else {
      // Reuse the existing animator. Updating the listener only modifies the target positions.
      indicatorAnimator.removeAllUpdateListeners();
      indicatorAnimator.addUpdateListener(updateListener);
    }
  }

  @Override
  public void draw(@NonNull Canvas canvas) {
    canvas.save();
    tabLayout.draw(canvas, indicatorElement);
    canvas.restore();
    // Draw the tab item contents (icon and label) on top of the background + indicator layers
    super.draw(canvas);
  }
}
