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

/** Callback interface invoked when a tab's selection state changes. */
public interface OnTabSelectedListener {
  /**
   * Called when a tab enters the selected state.
   *
   * @param tab The tab that was selected
   */
  public void onTabSelected(TabHolder tab);

  /**
   * Called when a tab exits the selected state.
   *
   * @param tab The tab that was unselected
   */
  public void onTabUnselected(TabHolder tab);

  /**
   * Called when a tab that is already selected is chosen again by the user. Some applications may
   * use this action to return to the top level of a category.
   *
   * @param tab The tab that was reselected.
   */
  public void onTabReselected(TabHolder tab);
}
