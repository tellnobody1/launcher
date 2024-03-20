/*
 * Copyright 2018 The Android Open Source Project
 * Copyright 2024 tellnobody1
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package github.tellnobody1.launcher;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;

class SwipeView extends View {

    private Animation.AnimationListener mListener;

    SwipeView(Context context) {
        super(context);
    }

    public void setAnimationListener(Animation.AnimationListener listener) {
        mListener = listener;
    }

    @Override
    public void onAnimationStart() {
        super.onAnimationStart();
        if (mListener != null)
            mListener.onAnimationStart(getAnimation());
    }

    @Override
    public void onAnimationEnd() {
        super.onAnimationEnd();
        if (mListener != null)
            mListener.onAnimationEnd(getAnimation());
    }
}
