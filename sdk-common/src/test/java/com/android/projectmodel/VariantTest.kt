/*
 * Copyright (C) 2018 The Android Open Source Project
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

package com.android.projectmodel

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class VariantTest {
    val artMain  = Artifact(name = ARTIFACT_NAME_MAIN)
    val artTest = Artifact(name = ARTIFACT_NAME_UNIT_TEST)
    val artAndroidTest = Artifact(name = ARTIFACT_NAME_ANDROID_TEST)
    val artExtra1 = Artifact(name = "extra1")
    val artExtra2 = Artifact(name = "extra2")
    val artAndroidExtra1 = Artifact(name = "androidExtra1")
    val artAndroidExtra2 = Artifact(name = "androidExtra2")

    val configPath = matchAllArtifacts()

    val mainOnly = Variant(configPath, listOf(artMain))
    val mainAndTest = Variant(configPath, listOf(artMain, artTest))
    val mainAndAndroidTest = Variant(configPath, listOf(artMain, artAndroidTest))
    val mainAndExtras = Variant(configPath, listOf(artMain, artExtra1, artExtra2))
    val mainAndJavaExtras = Variant("mainAndExtras", mainArtifact = artMain, extraJavaArtifacts = listOf(artAndroidExtra1, artAndroidExtra2))
    val allArtifacts = Variant("mainAndExtras", mainArtifact = artMain, unitTestArtifact = artTest, androidTestArtifact = artAndroidTest, extraArtifacts = listOf(artExtra1, artExtra2), extraJavaArtifacts = listOf(artAndroidExtra1, artAndroidExtra2))

    @Test
    fun testArtifactsProperty() {
        assertThat(mainOnly.artifacts).containsExactly(artMain)
        assertThat(mainAndTest.artifacts).containsExactly(artMain, artTest)
        assertThat(mainAndAndroidTest.artifacts).containsExactly(artMain, artAndroidTest)
        assertThat(mainAndExtras.artifacts).containsExactly(artMain, artExtra1, artExtra2)
        assertThat(mainAndJavaExtras.artifacts).containsExactly(artMain, artAndroidExtra1, artAndroidExtra2)
        assertThat(allArtifacts.artifacts).containsExactly(artMain, artTest, artAndroidTest, artExtra1, artExtra2, artAndroidExtra1, artAndroidExtra2)
    }

    @Test
    fun testArtifactNamed() {
        assertThat(allArtifacts.artifactNamed(ARTIFACT_NAME_MAIN)).isEqualTo(artMain)
        assertThat(allArtifacts.artifactNamed("nonexistent name")).isNull()
        assertThat(allArtifacts.artifactNamed(ARTIFACT_NAME_UNIT_TEST)).isEqualTo(artTest)
        assertThat(allArtifacts.artifactNamed(ARTIFACT_NAME_ANDROID_TEST)).isEqualTo(artAndroidTest)
        assertThat(allArtifacts.artifactNamed("extra1")).isEqualTo(artExtra1)
        assertThat(allArtifacts.artifactNamed("extra2")).isEqualTo(artExtra2)
        assertThat(allArtifacts.artifactNamed("androidExtra1")).isEqualTo(artAndroidExtra1)
        assertThat(allArtifacts.artifactNamed("androidExtra2")).isEqualTo(artAndroidExtra2)
    }

    @Test
    fun testExtras() {
        assertThat(mainOnly.extraArtifacts).isEmpty()
        assertThat(mainAndTest.extraArtifacts).isEmpty()
        // Ensure that we don't waste memory on empty list instances
        assertThat(mainAndTest.extraArtifacts == mainOnly.extraArtifacts).isTrue()
        assertThat(mainAndExtras.extraArtifacts).containsExactly(artExtra1, artExtra2)
    }
}
