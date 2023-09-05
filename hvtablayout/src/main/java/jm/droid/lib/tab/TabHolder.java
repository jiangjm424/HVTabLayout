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

import android.view.View;
import androidx.annotation.NonNull;

public class TabHolder {
  ITabLayout parent;
  @NonNull final View itemView;
  private int position = C.INVALID_POSITION;

  public boolean isActivated() {
    return activated;
  }

  boolean activated = false;

  public TabHolder(@NonNull View view) {
    itemView = view;
  }

  void setPosition(int position) {
    this.position = position;
  }

  public int getPosition() {
    return position;
  }

  @NonNull
  public View getItemView() {
    return itemView;
  }

  public void select() {
    if (parent == null) {
      throw new IllegalArgumentException("Tab not attached to a TabLayout");
    }
    parent.selectTab(this);
  }
}
