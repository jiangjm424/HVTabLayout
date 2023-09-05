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

import android.content.Context;
import android.database.Observable;
import android.view.ViewGroup;
import androidx.annotation.NonNull;

public abstract class TabAdapter<VH extends TabHolder> {
  private final AdapterDataObservable mObservable = new AdapterDataObservable();

  final VH createTabHolder(ViewGroup parent) {
    VH vh = onCreateTabHolder(parent);
    return vh;
  }

  final void bindTabHolder(VH holder, int position) {
    onBindTabHolder(holder, position);
  }

  final TabIndicator createTabIndicator(@NonNull Context context) {
    TabIndicator indicator = onCreateTabIndicator(context);
    return indicator;
  }

  protected final void notifyTabDataSetChanged() {
    mObservable.notifyChanged();
  }

  protected final void notifyTabChanged(int position) {
    mObservable.notifyTabChanged(position);
  }

  protected final void notifyTabInserted(int position) {
    mObservable.notifyTabInserted(position);
  }

  protected final void notifyTabRemoved(int position) {
    mObservable.notifyTabRemoved(position);
  }

  final void registerAdapterDataObserver(@NonNull AdapterDataObserver observer) {
    mObservable.registerObserver(observer);
  }

  final void unregisterAdapterDataObserver(@NonNull AdapterDataObserver observer) {
    mObservable.unregisterObserver(observer);
  }

  protected boolean interrupt(int position) {
    return false;
  }

  public abstract VH onCreateTabHolder(@NonNull ViewGroup parent);

  public abstract void onBindTabHolder(@NonNull VH holder, int position);

  public abstract TabIndicator onCreateTabIndicator(Context context);

  public abstract int getTabCount();

  /**
   * e用于修改Tab激活状态
   *
   * @param tab
   */
  public abstract void onTabActivatedChanged(@NonNull TabHolder tab);

  public void onDetachedFromTabLayout(@NonNull ITabLayout tabLayout) {}

  public void onAttachedToTabLayout(@NonNull ITabLayout tabLayout) {}

  static class AdapterDataObservable extends Observable<AdapterDataObserver> {
    public void notifyChanged() {
      for (int i = mObservers.size() - 1; i >= 0; i--) {
        mObservers.get(i).notifyChanged();
      }
    }

    public void notifyTabChanged(int position) {
      for (int i = mObservers.size() - 1; i >= 0; i--) {
        mObservers.get(i).notifyTabChanged(position);
      }
    }

    public void notifyTabInserted(int position) {
      for (int i = mObservers.size() - 1; i >= 0; i--) {
        mObservers.get(i).notifyTabInserted(position);
      }
    }

    public void notifyTabRemoved(int position) {
      for (int i = mObservers.size() - 1; i >= 0; i--) {
        mObservers.get(i).notifyTabRemoved(position);
      }
    }
  }

  abstract static class AdapterDataObserver {
    public void notifyTabRemoved(int position) {}

    public void notifyChanged() {}

    public void notifyTabInserted(int position) {}

    public void notifyTabChanged(int position) {}
  }
}
