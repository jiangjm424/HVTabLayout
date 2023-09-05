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

import androidx.annotation.IntDef;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class C {

  private C() {}

  public static final int INVALID_POSITION = -1;

  public static final int MODE_SCROLLABLE = 0;
  public static final int MODE_FIXED = 1;
  public static final int MODE_AUTO = 2;

  @IntDef(value = {MODE_SCROLLABLE, MODE_FIXED, MODE_AUTO})
  @Retention(RetentionPolicy.SOURCE)
  @interface Mode {}

  public static final int GRAVITY_FILL = 0;
  public static final int GRAVITY_CENTER = 1;
  public static final int GRAVITY_START = 1 << 1;

  @IntDef(
      flag = true,
      value = {GRAVITY_FILL, GRAVITY_CENTER, GRAVITY_START})
  @Retention(RetentionPolicy.SOURCE)
  @interface TabGravity {}
}
