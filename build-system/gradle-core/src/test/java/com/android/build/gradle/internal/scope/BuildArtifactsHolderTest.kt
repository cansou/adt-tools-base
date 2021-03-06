/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.android.build.gradle.internal.scope

import com.android.build.api.artifact.BuildArtifactType.JAVAC_CLASSES
import com.android.build.gradle.internal.api.artifact.BuildableArtifactImpl
import com.android.build.gradle.internal.api.dsl.DslScope
import com.android.build.gradle.internal.fixtures.FakeDeprecationReporter
import com.android.build.gradle.internal.fixtures.FakeEvalIssueReporter
import com.android.build.gradle.internal.fixtures.FakeObjectFactory
import com.android.build.gradle.internal.scope.BuildArtifactsHolder.OperationType
import com.android.build.gradle.internal.variant2.DslScopeImpl
import com.android.utils.FileUtils
import com.google.common.truth.Truth.assertThat
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import java.io.File

/**
 * Test for [BuildArtifactsHolder]
 */
class BuildArtifactsHolderTest {

    private lateinit var project : Project
    lateinit var root : File
    private val dslScope = DslScopeImpl(
            FakeEvalIssueReporter(throwOnError = true),
            FakeDeprecationReporter(),
            FakeObjectFactory())
    private lateinit var holder : BuildArtifactsHolder
    private lateinit var task0 : Task
    private lateinit var task1 : Task
    private lateinit var task2 : Task

    @Before
    fun setUp() {
        BuildableArtifactImpl.enableResolution()
        project = ProjectBuilder.builder().build()
        root = project.file("build")
        holder = VariantBuildArtifactsHolder(
            project,
            "debug",
            root,
            dslScope)
        task0 = project.tasks.create("task0")
        task1 = project.tasks.create("task1")
        task2 = project.tasks.create("task2")
    }

    /** Return the expected location of a generated file given the task name and file name. */
    private fun file(taskName : String, filename : String) =
            FileUtils.join(JAVAC_CLASSES.getOutputDir(root), "debug", taskName, filename)

    @Test
    fun replaceOutput() {
        val files1 = holder.createBuildableArtifact(JAVAC_CLASSES,
            OperationType.INITIAL,
            project.files(file("task1", "foo")).files, task1.name)
        assertThat(holder.getArtifactFiles(JAVAC_CLASSES)).isSameAs(files1)
        val files2 = holder.createBuildableArtifact(JAVAC_CLASSES,
            OperationType.TRANSFORM,
            project.files(file("task2", "bar")).files, task2.name)
        assertThat(holder.getArtifactFiles(JAVAC_CLASSES)).isSameAs(files2)
        holder.createDirectory(
            JAVAC_CLASSES,
            OperationType.APPEND,
            task0.name,
            "baz")

        assertThat(files1.single()).isEqualTo(file("task1", "foo"))
        // TaskDependency.getDependencies accepts null.
        @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
        assertThat(files1.buildDependencies.getDependencies(null)).containsExactly(task1)
        assertThat(files2.single()).isEqualTo(file("task2", "bar"))
        @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
        assertThat(files2.buildDependencies.getDependencies(null)).containsExactly(task2)

        val history = holder.getHistory(JAVAC_CLASSES)
        assertThat(history[0]).isSameAs(files1)
        assertThat(history[1]).isSameAs(files2)
    }

    @Test
    fun appendOutput() {
        holder.createDirectory(JAVAC_CLASSES, OperationType.INITIAL, task1.name, "foo")
        val files0 = holder.getFinalArtifactFiles(JAVAC_CLASSES)
        val files1 = holder.getArtifactFiles(JAVAC_CLASSES)
        assertThat(holder.getArtifactFiles(JAVAC_CLASSES)).isSameAs(files1)
        holder.createDirectory(JAVAC_CLASSES, OperationType.APPEND, task2.name, "bar")
        val files2 = holder.getArtifactFiles(JAVAC_CLASSES)
        assertThat(holder.getArtifactFiles(JAVAC_CLASSES)).isSameAs(files2)
        holder.createDirectory(JAVAC_CLASSES, OperationType.APPEND, task0.name, "baz")

        assertThat(files1).containsExactly(
                file("task1", "foo"))
        assertThat(files2).containsExactly(
                file("task1", "foo"),
                file("task2", "bar"))
        assertThat(files0).containsExactly(
            file("task1", "foo"),
            file("task2", "bar"),
            file("task0", "baz"))

        @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
        assertThat(files0.buildDependencies.getDependencies(null)).containsExactly(task0, task1, task2)
        @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
        assertThat(files1.buildDependencies.getDependencies(null)).containsExactly(task1)
        @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
        assertThat(files2.buildDependencies.getDependencies(null))
                .containsExactly(task1, task2)

        val history = holder.getHistory(JAVAC_CLASSES)
        assertThat(history[0]).isSameAs(files1)
        assertThat(history[1]).isSameAs(files2)
        assertThat(history[2]).isSameAs(holder.getArtifactFiles(JAVAC_CLASSES))
    }

    @Test
    fun obtainFinalOutput() {
        val finalVersion = holder.getFinalArtifactFiles(JAVAC_CLASSES)
        holder.createBuildableArtifact(JAVAC_CLASSES,
            OperationType.TRANSFORM,
            project.files(file("task1", "task1File")).files,
            task1.name)
        val files1 = holder.getArtifactFiles(JAVAC_CLASSES)

        assertThat(files1.single()).isEqualTo(file("task1", "task1File"))
        @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
        assertThat(files1.buildDependencies.getDependencies(null)).containsExactly(task1)

        // Now add some more files to this artifact type using all appendArtifact methods
        holder.createBuildableArtifact(JAVAC_CLASSES, OperationType.APPEND,
            project.files(file("task0", "task0File")).files,
            task0.name)
        val task0Files = holder.getArtifactFiles(JAVAC_CLASSES)
        holder.createBuildableArtifact(JAVAC_CLASSES,
            OperationType.APPEND, project.files("single_file"))
        holder.createBuildableArtifact(JAVAC_CLASSES,
            OperationType.APPEND,
            project.files("element1", "element2"))

        @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
        assertThat(task0Files.buildDependencies.getDependencies(null)).containsExactly(task0, task1)

        // assert that the current buildableArtifact has all the files
        assertThat(holder.getArtifactFiles(JAVAC_CLASSES).files).containsExactly(
            file("task1", "task1File"),
            file("task0", "task0File"),
            project.file("element1"),
            project.file("element2"),
            project.file("single_file"))
        // as well as the "finalVersion"
        assertThat(finalVersion.files).containsExactly(
            file("task1", "task1File"),
            file("task0", "task0File"),
            project.file("element1"),
            project.file("element2"),
            project.file("single_file"))
    }

    @Test
    fun earlyFinalOutput() {
        val finalVersion = holder.getFinalArtifactFiles(JAVAC_CLASSES)
        // no-one appends or replaces, we should be empty files if resolved.
        assertThat(finalVersion.files).isEmpty()
    }

    @Test
    fun lateFinalOutput() {
        holder.createBuildableArtifact(JAVAC_CLASSES,
            OperationType.INITIAL,
            project.files(file("task1", "task1File")),
            task1.name)
        val files1 = holder.getArtifactFiles(JAVAC_CLASSES)

        assertThat(files1.single()).isEqualTo(file("task1", "task1File"))
        @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
        assertThat(files1.buildDependencies.getDependencies(null)).containsExactly(task1)

        // now get final version.
        val finalVersion = holder.getFinalArtifactFiles(JAVAC_CLASSES)
        assertThat(finalVersion.files).hasSize(1)
        @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
        assertThat(finalVersion.buildDependencies.getDependencies(null)).containsExactly(task1)
    }

    @Test
    fun addBuildableArtifact() {
        holder.createBuildableArtifact(
            JAVAC_CLASSES,
            OperationType.INITIAL,
            project.files(file("task1", "task1File")).files,
            task1.name)
        val javaClasses = holder.getArtifactFiles(JAVAC_CLASSES)

        // register the buildable artifact under a different type.
        val newHolder = TestBuildArtifactsHolder(project, { root }, dslScope)
        newHolder.createBuildableArtifact(
            JAVAC_CLASSES,
            OperationType.INITIAL,
            javaClasses)
        // and verify that files and dependencies are carried over.
        val newJavaClasses = newHolder.getArtifactFiles(JAVAC_CLASSES)
        assertThat(newJavaClasses.single()).isEqualTo(file("task1", "task1File"))
        @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
        assertThat(newJavaClasses.buildDependencies.getDependencies(null)).containsExactly(task1)
    }

    @Test
    fun finalBuildableFileLocation() {
        holder.createArtifactFile(InternalArtifactType.BUNDLE, OperationType.INITIAL, task1.name, "finalFile")
        val finalArtifactFiles = holder.getFinalArtifactFiles(InternalArtifactType.BUNDLE)
        assertThat(finalArtifactFiles.files).hasSize(1)
        val outputFile = finalArtifactFiles.files.elementAt(0)
        val relativeFile = outputFile.relativeTo(project.buildDir)
        assertThat(relativeFile.path).isEqualTo(
            FileUtils.join(
                InternalArtifactType.Category.OUTPUTS.name.toLowerCase(),
                InternalArtifactType.BUNDLE.name.toLowerCase(),
                "debug",
                "finalFile"))
    }

    @Test
    fun finalBuildableDirectoryLocation() {
        val taskProvider = Mockito.mock(TaskProvider::class.java)
        Mockito.`when`(taskProvider.get()).thenReturn(task1)
        holder.createDirectory(InternalArtifactType.MERGED_MANIFESTS,
            task1.name,
            "finalFolder")
        val finalArtifactFiles = holder.getFinalArtifactFiles(InternalArtifactType.MERGED_MANIFESTS)
        assertThat(finalArtifactFiles.files).hasSize(1)
        val outputFile = finalArtifactFiles.files.elementAt(0)
        val relativeFile = outputFile.relativeTo(project.buildDir)
        assertThat(relativeFile.path).isEqualTo(
            FileUtils.join(
                InternalArtifactType.Category.INTERMEDIATES.name.toLowerCase(),
                InternalArtifactType.MERGED_MANIFESTS.name.toLowerCase(),
                "debug",
                "finalFolder"))
    }

    @Test
    fun finalReplacedBuildableFileLocation() {
        val task1Output = holder.createArtifactFile(
            InternalArtifactType.BUNDLE, OperationType.INITIAL, task1.name, "finalFile")
        val task2Output = holder.createArtifactFile(
            InternalArtifactType.BUNDLE, OperationType.TRANSFORM, task2.name, "replacingFile")
        val finalArtifactFiles = holder.getFinalArtifactFiles(InternalArtifactType.BUNDLE)
        assertThat(finalArtifactFiles.files).hasSize(1)
        val outputFile = finalArtifactFiles.files.elementAt(0)
        // check that our output file 
        assertThat(task2Output.get().asFile.path).isEqualTo(outputFile.path)
        val relativeFile1 = task1Output.get().asFile.relativeTo(project.buildDir)
        assertThat(relativeFile1.path).isEqualTo(
            FileUtils.join(
                InternalArtifactType.Category.INTERMEDIATES.outputPath,
                InternalArtifactType.BUNDLE.name.toLowerCase(),
                "debug",
                "task1",
                "finalFile"))
        val relativeFile2 = task2Output.get().asFile.relativeTo(project.buildDir)
        assertThat(relativeFile2.path).isEqualTo(
            FileUtils.join(
                InternalArtifactType.Category.OUTPUTS.name.toLowerCase(),
                InternalArtifactType.BUNDLE.name.toLowerCase(),
                "debug",
                "replacingFile")
        )
    }

    @Test
    fun finalAppendedBuildableDirectoryLocation() {
        val task1Provider = Mockito.mock(TaskProvider::class.java)
        Mockito.`when`(task1Provider.get()).thenReturn(task1)
        val task2Provider = Mockito.mock(TaskProvider::class.java)
        Mockito.`when`(task2Provider.get()).thenReturn(task2)

        val finalArtifactFiles = holder.getFinalArtifactFiles(InternalArtifactType.MERGED_MANIFESTS)

        val task1Output = holder.createDirectory(
            InternalArtifactType.MERGED_MANIFESTS, task1.name, "originalFolder")
        assertThat(task1Output.get().asFile.path).isEqualTo(finalArtifactFiles.files.elementAt(0).path)

        val task2Output = holder.createDirectory(
            InternalArtifactType.MERGED_MANIFESTS, task2.name, "addedFolder")
        assertThat(finalArtifactFiles.files).hasSize(2)
        // check that our output file
        assertThat(task2Output.get().asFile.path).isEqualTo(finalArtifactFiles.files.elementAt(1).path)
        val relativeFile1 = task1Output.get().asFile.relativeTo(project.buildDir)
        assertThat(relativeFile1.path).isEqualTo(
            FileUtils.join(
                InternalArtifactType.Category.INTERMEDIATES.outputPath,
                InternalArtifactType.MERGED_MANIFESTS.name.toLowerCase(),
                "debug",
                "task1",
                "originalFolder"))
        val relativeFile2 = task2Output.get().asFile.relativeTo(project.buildDir)
        assertThat(relativeFile2.path).isEqualTo(
            FileUtils.join(
                InternalArtifactType.Category.INTERMEDIATES.name.toLowerCase(),
                InternalArtifactType.MERGED_MANIFESTS.name.toLowerCase(),
                "debug",
                "task2",
                "addedFolder")
        )
    }

    private class TestBuildArtifactsHolder(
        project: Project,
        rootOutputDir: () -> File,
        dslScope: DslScope) : BuildArtifactsHolder(project, rootOutputDir, dslScope) {

        override fun getIdentifier() = "test"
    }
}