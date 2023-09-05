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

package jm.droid.lib.tab.internal;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.animation.TimeInterpolator;
import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import androidx.annotation.Dimension;
import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import java.lang.reflect.Constructor;

/**
 * Utils class for custom views.
 *
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public class ViewUtils {

  private ViewUtils() {}

  public static float dpToPx(@NonNull Context context, @Dimension(unit = Dimension.DP) int dp) {
    Resources r = context.getResources();
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
  }

  public static TimeInterpolator parseTimeInterpolator(Context context, String name) {
    if (TextUtils.isEmpty(name)) {
      return null;
    }

    final String fullName;
    if (name.startsWith(".")) {
      // Relative to the app package. Prepend the app package name.
      fullName = context.getPackageName() + name;
    } else if (name.indexOf('.') >= 0) {
      // Fully qualified package name.
      fullName = name;
    } else {
      // Assume stock behavior in this package (if we have one)
      Log.w("ViewUtils", "Is that interpolator name right:" + name);
      fullName = name;
    }

    try {
      final Class<TimeInterpolator> clazz =
          (Class<TimeInterpolator>) Class.forName(fullName, false, context.getClassLoader());
      Constructor<TimeInterpolator> c = clazz.getConstructor();
      c.setAccessible(true);
      return c.newInstance();
    } catch (Exception e) {
      throw new RuntimeException("Could not inflate Behavior TimeInterpolator " + fullName, e);
    }
  }

  /** Linear interpolation between {@code startValue} and {@code endValue} by {@code fraction}. */
  public static float lerp(float startValue, float endValue, float fraction) {
    return startValue + (fraction * (endValue - startValue));
  }

  /** Linear interpolation between {@code startValue} and {@code endValue} by {@code fraction}. */
  public static int lerp(int startValue, int endValue, float fraction) {
    return startValue + Math.round(fraction * (endValue - startValue));
  }

  /**
   * Linear interpolation between {@code outputMin} and {@code outputMax} when {@code value} is
   * between {@code inputMin} and {@code inputMax}.
   *
   * <p>Note that {@code value} will be coerced into {@code inputMin} and {@code inputMax}.This
   * function can handle input and output ranges that span positive and negative numbers.
   */
  public static float lerp(
      float outputMin, float outputMax, float inputMin, float inputMax, float value) {
    if (value <= inputMin) {
      return outputMin;
    }
    if (value >= inputMax) {
      return outputMax;
    }

    return lerp(outputMin, outputMax, (value - inputMin) / (inputMax - inputMin));
  }
}
