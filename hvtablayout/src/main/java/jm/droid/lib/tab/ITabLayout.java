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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface ITabLayout extends TabIndicator {
  int getIndicatorPosition();

  void setIndicatorPosition(int position);

  @C.Mode
  int getTabMode();

  @C.TabGravity
  int getTabGravity();

  void setTabGravity(@C.TabGravity int gravity);

  void updateTabViews(boolean b);

  int getSelectedTabPosition();

  TimeInterpolator getTabIndicatorTimeInterpolator();

  void selectTab(TabHolder tabHolder);

  void selectTab(TabHolder tabHolder, boolean updateIndicator);

  TabHolder getTabAt(int position);

  int getTabCount();

  void removeAllTabs();

  void addOnTabSelectedListener(OnTabSelectedListener onTabSelectedListener);

  void removeOnTabSelectedListener(OnTabSelectedListener onTabSelectedListener);

  void updateViewPagerScrollState(int scrollState);

  void setScrollPosition(
      int position,
      float positionOffset,
      boolean updateSelectedTabView,
      boolean updateIndicator,
      boolean alwaysScroll);

  void removeTabAt(int position);

  void notifyChanged();

  void insertTabAt(int position);

  void updateTabAt(int position);

  void setAdapter(@NonNull TabAdapter adapter);

  @Nullable
  TabAdapter getAdapter();
}
