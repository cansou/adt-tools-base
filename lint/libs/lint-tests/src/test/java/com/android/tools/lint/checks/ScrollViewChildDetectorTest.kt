/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.android.tools.lint.checks

import com.android.tools.lint.detector.api.Detector

class ScrollViewChildDetectorTest : AbstractCheckTest() {
    override fun getDetector(): Detector {
        return ScrollViewChildDetector()
    }

    fun testScrollView() {
        lint().files(
            xml(
                "res/layout/wrong_dimension.xml",
                """
                <HorizontalScrollView
                    xmlns:android="http://schemas.android.com/apk/res/android"

                    android:layout_width="match_parent"
                    android:layout_height="match_parent">

                    <LinearLayout
                        android:layout_width="match_parent"
                        android:layout_height="match_parent" />

                </HorizontalScrollView>
                """
            ).indented()
        ).run().expect(
            """
            res/layout/wrong_dimension.xml:8: Warning: This LinearLayout should use android:layout_width="wrap_content" [ScrollViewSize]
                    android:layout_width="match_parent"
                    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            0 errors, 1 warnings
            """
        )
    }
}
