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

package com.android.build.gradle.internal.cxx.configure

import com.android.build.gradle.internal.core.Abi
import com.android.build.gradle.internal.fixtures.FakeEvalIssueReporter
import com.android.build.gradle.internal.ndk.NdkHandler
import com.android.builder.errors.EvalIssueReporter
import com.google.common.collect.Sets
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AbiConfiguratorTest {
    companion object {
        val ALL_ABI = Abi.getDefaultValues().toList()
        val ALL_ABI_AS_STRING = ALL_ABI.map(Abi::getName)
        val ALL_ABI_COMMA_STRING = ALL_ABI_AS_STRING.sorted().joinToString(", ")
    }

    private val evalIssueReporter = FakeEvalIssueReporter()

    fun configure(
            evalIssueReporter: EvalIssueReporter,
            variantName: String = "MyVariant",
            ndkHandlerSupportedAbis: Collection<Abi> = AbiConfiguratorTest.ALL_ABI,
            ndkHandlerDefaultAbis: Collection<Abi> = AbiConfiguratorTest.ALL_ABI,
            externalNativeBuildAbiFilters: Set<String> = setOf(),
            ndkConfigAbiFilters: Set<String> = setOf(),
            splitsFilterAbis: Set<String> = NdkHandler
                    .getDefaultAbiList()
                    .map { abi : Abi -> abi.getName() }
                    .toSet(),
            ideBuildOnlyTargetAbi: Boolean = false,
            ideBuildTargetAbi: String? = null): AbiConfigurator {
        return AbiConfigurator(evalIssueReporter,
                variantName,
                ndkHandlerSupportedAbis,
                ndkHandlerDefaultAbis,
                externalNativeBuildAbiFilters,
                ndkConfigAbiFilters,
                splitsFilterAbis,
                ideBuildOnlyTargetAbi,
                ideBuildTargetAbi)
    }

    @Test
    fun testBaseline() {
        val configurator = configure(evalIssueReporter)
        assertThat(evalIssueReporter.messages).isEmpty()
        // Should be no messages reported
        assertThat(configurator.validAbis).containsExactlyElementsIn(ALL_ABI)
        assertThat(configurator.allAbis).containsExactlyElementsIn(ALL_ABI_AS_STRING)
    }

    @Test
    fun testValidAbiInBuildGradleDsl() {
        val configurator = configure(
                evalIssueReporter,
                externalNativeBuildAbiFilters = setOf("x86"))
        assertThat(evalIssueReporter.messages).isEmpty()
        // Should be no messages reported
        assertThat(configurator.validAbis).containsExactly(Abi.X86)
        assertThat(configurator.allAbis).containsExactly("x86")
    }

    // User typed a wrong ABI into build.gradle:externalNativeBuild.cmake.abiFilters
    @Test
    fun testInvalidAbiInBuildGradleDsl() {
        val configurator = configure(
                evalIssueReporter,
                externalNativeBuildAbiFilters = setOf("x87"))
        assertThat(evalIssueReporter.messages.first()).isEqualTo(
                "ABIs [x87] are not supported for platform. Supported ABIs " +
                "are [$ALL_ABI_COMMA_STRING].")
        assertThat(configurator.validAbis).isEmpty()
        assertThat(configurator.allAbis).containsExactlyElementsIn(Sets.newHashSet("x87"))
    }

    @Test
    fun testSplitsEnabled() {
        val configurator = configure(
                evalIssueReporter,
                splitsFilterAbis = setOf("x86"))
        assertThat(evalIssueReporter.messages).isEmpty()
        // Should be no messages reported
        assertThat(configurator.validAbis).containsExactly(Abi.X86)
        assertThat(configurator.allAbis).containsExactly("x86")
    }

    @Test
    fun testSplitsEnabledInvalidAbi() {
        val configurator = configure(
                evalIssueReporter,
                splitsFilterAbis = setOf("x87"))
        assertThat(evalIssueReporter.messages).containsExactly(
                "ABIs [x87] are not supported for platform. Supported ABIs are "
                + "[$ALL_ABI_COMMA_STRING].")
        assertThat(configurator.validAbis).isEmpty()
        assertThat(configurator.allAbis).containsExactly("x87")
    }

    @Test
    fun testValidAbiThatIsNotInNdk() {
        val configurator = configure(
                evalIssueReporter,
                splitsFilterAbis = setOf(),
                ndkHandlerSupportedAbis = listOf(Abi.X86_64),
                externalNativeBuildAbiFilters = setOf("x86"))
        assertThat(evalIssueReporter.messages).containsExactly(
                "ABIs [x86] are not supported for platform. " +
                "Supported ABIs are [x86_64].")
        assertThat(configurator.validAbis).containsExactly(Abi.X86)
        assertThat(configurator.allAbis).containsExactly("x86")
    }

    @Test
    fun testExternalNativeBuildAbiFiltersAndNdkAbiFiltersAreTheSame() {
        val configurator = configure(
                evalIssueReporter,
                ndkConfigAbiFilters = setOf("x86"),
                externalNativeBuildAbiFilters = setOf("x86"))
        assertThat(evalIssueReporter.messages).isEmpty()
        // Should be no messages reported
        assertThat(configurator.validAbis).containsExactly(Abi.X86)
        assertThat(configurator.allAbis).containsExactly("x86")
    }

    @Test
    fun testExternalNativeBuildAbiFiltersAndNdkAbiFiltersAreNonIntersecting() {
        val configurator = configure(
                evalIssueReporter,
                ndkConfigAbiFilters = setOf("x86"),
                externalNativeBuildAbiFilters = setOf("x86_64"))
        assertThat(configurator.validAbis).isEmpty()
        assertThat(configurator.allAbis).isEmpty()
    }

    @Test
    fun testValidInjectedAbi() {
        val configurator = configure(
                evalIssueReporter,
                ideBuildOnlyTargetAbi = true,
                ideBuildTargetAbi = "x86")
        assertThat(evalIssueReporter.messages).isEmpty()
        // Should be no messages reported
        assertThat(configurator.validAbis).containsExactly(Abi.X86)
        assertThat(configurator.allAbis).containsExactlyElementsIn(ALL_ABI_AS_STRING)
    }

    @Test
    fun testValidBogusAndValidInjectedAbi() {
        val configurator = configure(
                evalIssueReporter,
                ideBuildOnlyTargetAbi = true,
                ideBuildTargetAbi = "bogus,x86")

        assertThat(evalIssueReporter.warnings).containsExactly(
            "ABIs [bogus,x86] set by 'android.injected.build.abi' gradle flag contained " +
                    "'bogus' which is invalid.")
        assertThat(evalIssueReporter.errors).isEmpty()
        assertThat(configurator.validAbis).containsExactly(Abi.X86)
        assertThat(configurator.allAbis).containsExactlyElementsIn(ALL_ABI_AS_STRING)

    }

    @Test
    fun testBogusInjectedAbi() {
        val configurator = configure(
                evalIssueReporter,
                ideBuildOnlyTargetAbi = true,
                ideBuildTargetAbi = "bogus")
        assertThat(evalIssueReporter.errors).containsExactly(
                "ABIs [bogus] set by 'android.injected.build.abi' gradle " +
                "flag is not supported. Supported ABIs are " +
                "[$ALL_ABI_COMMA_STRING].")
        assertThat(configurator.validAbis).containsExactlyElementsIn(ALL_ABI)
        assertThat(configurator.allAbis).containsExactlyElementsIn(ALL_ABI_AS_STRING)
    }

    @Test
    fun testValidEmptyInjectedAbi() {
        // Empty list should not error
        val configurator = configure(
                evalIssueReporter,
                ideBuildOnlyTargetAbi = true,
                ideBuildTargetAbi = "")
        assertThat(configurator.validAbis).containsExactlyElementsIn(ALL_ABI)
        assertThat(configurator.allAbis).containsExactlyElementsIn(ALL_ABI_AS_STRING)
    }

    @Test
    fun testValidNullInjectedAbi() {
        // Empty list should not error
        val configurator = configure(
                evalIssueReporter,
                ideBuildOnlyTargetAbi = true,
                ideBuildTargetAbi = null)
        assertThat(configurator.validAbis).containsExactlyElementsIn(ALL_ABI)
        assertThat(configurator.allAbis).containsExactlyElementsIn(ALL_ABI_AS_STRING)
    }

    @Test
    fun testAbiSplitsLookDefaulted() {
        // Empty list should not error
        val configurator = configure(
                evalIssueReporter,
                ideBuildOnlyTargetAbi = true,
                ideBuildTargetAbi = null)
        assertThat(configurator.validAbis).containsExactlyElementsIn(ALL_ABI)
        assertThat(configurator.allAbis).containsExactlyElementsIn(ALL_ABI_AS_STRING)
    }

    @Test
    fun testPeopleCanSpecifyMipsIfTheyReallyWantTo() {
        // Empty list should not error
        val configurator = configure(
                evalIssueReporter,
                splitsFilterAbis = setOf("mips"),
                ideBuildOnlyTargetAbi = true,
                ideBuildTargetAbi = null)
        assertThat(configurator.validAbis).containsExactly(Abi.MIPS)
        assertThat(configurator.allAbis).containsExactly("mips")
    }

    @Test
    fun testMisspelledMips() {
        // Empty list should not error
        val configurator = configure(
                evalIssueReporter,
                splitsFilterAbis = setOf("misp"),
                ideBuildOnlyTargetAbi = true,
                ideBuildTargetAbi = null)
        assertThat(evalIssueReporter.errors).containsExactly(
            "ABIs [misp] are not supported for platform. Supported ABIs are [arm64-v8a, " +
                    "armeabi-v7a, x86, x86_64].")
        assertThat(evalIssueReporter.warnings).isEmpty()
        assertThat(configurator.validAbis).isEmpty()
        assertThat(configurator.allAbis).containsExactly("misp")
    }

    // Related to: http://b/74173612
    @Test
    fun testIdeSelectedAbiDoesntIntersectWithNdkConfigAbiFilters() {
        val configurator = configure(
            evalIssueReporter,
            ndkConfigAbiFilters = setOf("arm64-v8a", "x86_64"),
            ideBuildOnlyTargetAbi = true,
            ideBuildTargetAbi = "armeabi-v7a,armeabi")
        assertThat(evalIssueReporter.errors).isEmpty()
        assertThat(evalIssueReporter.warnings).containsExactly(
            "ABIs [armeabi-v7a,armeabi] set by 'android.injected.build.abi' gradle flag " +
                    "contained 'ARMEABI, ARMEABI_V7A' not targeted by this project.")
        assertThat(configurator.validAbis).containsExactly()
    }

    // Related to: http://b/74173612
    @Test
    fun testIdeSelectedAbiDoesntIntersectWithExternalNativeBuildAbiFilters() {
        val configurator = configure(
            evalIssueReporter,
            externalNativeBuildAbiFilters = setOf("arm64-v8a", "x86_64"),
            ideBuildOnlyTargetAbi = true,
            ideBuildTargetAbi = "armeabi-v7a,armeabi")
        assertThat(evalIssueReporter.errors).isEmpty()
        assertThat(evalIssueReporter.warnings).containsExactly(
            "ABIs [armeabi-v7a,armeabi] set by 'android.injected.build.abi' gradle flag " +
                    "contained 'ARMEABI, ARMEABI_V7A' not targeted by this project.")
        assertThat(configurator.validAbis).containsExactly()
    }

    // Related to: http://b/74173612
    // This test contains one ideBuildTargetAbi that is not targeted by this project and one that
    // is targeted by this project. There should be a warning and the ABI targeted by this project
    // should be retained.
    @Test
    fun testIdeSelectedAbiDoesntIntersectWithSplitsFilterAbis() {
        val configurator = configure(
            evalIssueReporter,
            splitsFilterAbis = setOf("arm64-v8a", "x86_64"),
            ideBuildOnlyTargetAbi = true,
            ideBuildTargetAbi = "armeabi-v7a,x86_64")
        assertThat(evalIssueReporter.errors).isEmpty()
        assertThat(evalIssueReporter.warnings).containsExactly(
            "ABIs [armeabi-v7a,x86_64] set by 'android.injected.build.abi' gradle flag " +
                    "contained 'ARMEABI_V7A' not targeted by this project.")
        assertThat(configurator.validAbis).containsExactly(Abi.X86_64)
    }

    @Test
    fun testAllowSpaceInInjectedAbi() {
        val configurator = configure(
            evalIssueReporter,
            ideBuildOnlyTargetAbi = true,
            ideBuildTargetAbi = " x86, x86_64 ")
        assertThat(evalIssueReporter.errors).isEmpty()
        assertThat(evalIssueReporter.warnings).isEmpty()
        assertThat(configurator.validAbis).containsExactly(Abi.X86, Abi.X86_64)
    }
}
