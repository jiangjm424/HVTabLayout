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

import android.util.Log;
import java.lang.ref.WeakReference;

class TabLayoutAdapterDataObserver extends TabAdapter.AdapterDataObserver {

  private final WeakReference<ITabLayout> tabLayoutRef;

  TabLayoutAdapterDataObserver(ITabLayout tabLayout) {
    tabLayoutRef = new WeakReference<>(tabLayout);
  }

  @Override
  public void notifyTabRemoved(int position) {
    final ITabLayout tabLayout = tabLayoutRef.get();
    if (tabLayout != null) {
      tabLayout.removeTabAt(position);
    }
  }

  @Override
  public void notifyChanged() {
    final ITabLayout tabLayout = tabLayoutRef.get();
    if (tabLayout != null) {
      tabLayout.notifyChanged();
    }
  }

  @Override
  public void notifyTabInserted(int position) {
    final ITabLayout tabLayout = tabLayoutRef.get();
    if (tabLayout != null) {
      tabLayout.insertTabAt(position);
    }
  }

  @Override
  public void notifyTabChanged(int position) {
    final ITabLayout tabLayout = tabLayoutRef.get();
    if (tabLayout != null) {
      tabLayout.updateTabAt(position);
    }
  }
}
