/*
 * Copyright (C) 2015 The Android Open Source Project
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
apply from: "../commonHeader.gradle"
buildscript { apply from: "../commonBuildScript.gradle" }
apply plugin: 'com.android.library'

apply from: "../commonLocalRepo.gradle"

android {
    compileSdkVersion rootProject.latestCompileSdk
    buildToolsVersion = rootProject.buildToolsVersion
    defaultConfig.minSdkVersion rootProject.supportLibMinSdk
    dataBinding {
        enabled = true
        addDefaultAdapters = false
    }
}

dependencies {
    def useAndroidX = project.findProperty("android.useAndroidX")
    if (useAndroidX == null) {
        throw new IllegalArgumentException("must provide androidX property")
    } else if (useAndroidX == "true") {
        implementation "androidx.databinding:databinding-runtime:${rootProject.buildVersion}"
    } else {
        implementation "com.android.databinding:library:${rootProject.buildVersion}"
    }
}
