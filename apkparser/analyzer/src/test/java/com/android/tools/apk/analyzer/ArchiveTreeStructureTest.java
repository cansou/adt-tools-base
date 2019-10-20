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

package com.android.tools.apk.analyzer;

import static com.google.common.truth.Truth.assertThat;

import com.android.annotations.NonNull;
import com.android.testutils.TestResources;
import com.android.utils.ILogger;
import com.android.utils.StdLogger;
import com.google.common.primitives.Longs;
import java.io.IOException;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ArchiveTreeStructureTest {
    private ILogger logger = new StdLogger(StdLogger.Level.VERBOSE);
    private ArchiveContext archiveContext;
    private ArchiveNode root;

    @Before
    public void setup() throws IOException {
        archiveContext = Archives.open(TestResources.getFile("/test.apk").toPath(), logger);
        root = ArchiveTreeStructure.create(archiveContext);
    }

    @After
    public void tearDown() throws IOException {
        archiveContext.close();
    }

    @Test
    public void create() {
        String actual = dumpTree(root, n -> n.getData().getFullPathString());
        String expected =
                "/\n"
                        + "/res/\n"
                        + "/res/anim/\n"
                        + "/res/anim/fade.xml\n"
                        + "/instant-run.zip\n"
                        + "/instant-run.zip/instant-run/\n"
                        + "/instant-run.zip/instant-run/classes1.dex\n"
                        + "/AndroidManifest.xml";
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void updateRawFileSizes() {
        ArchiveTreeStructure.updateRawFileSizes(root, ApkSizeCalculator.getDefault());
        String actual =
                dumpTree(
                        root,
                        n -> {
                            ArchiveEntry entry = n.getData();
                            return String.format(
                                    Locale.US,
                                    "%1$-10d %2$s",
                                    entry.getRawFileSize(),
                                    entry.getFullPathString());
                        });
        String expected =
                "170        /\n"
                        + "6          /res/\n"
                        + "6          /res/anim/\n"
                        + "6          /res/anim/fade.xml\n"
                        + "153        /instant-run.zip\n"
                        + "2          /instant-run.zip/instant-run/\n"
                        + "2          /instant-run.zip/instant-run/classes1.dex\n"
                        + "11         /AndroidManifest.xml";
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void updateDownloadFileSizes() {
        ArchiveTreeStructure.updateDownloadFileSizes(root, ApkSizeCalculator.getDefault());

        String actual =
                dumpTree(
                        root,
                        n -> {
                            ArchiveEntry entry = n.getData();
                            return String.format(
                                    Locale.US,
                                    "%1$-10d %2$s",
                                    entry.getDownloadFileSize(),
                                    entry.getFullPathString());
                        });
        String expected =
                "172        /\n"
                        + "8          /res/\n"
                        + "8          /res/anim/\n"
                        + "8          /res/anim/fade.xml\n"
                        + "153        /instant-run.zip\n"
                        + "4          /instant-run.zip/instant-run/\n"
                        + "4          /instant-run.zip/instant-run/classes1.dex\n"
                        + "11         /AndroidManifest.xml";
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void sort() {
        ArchiveTreeStructure.updateRawFileSizes(root, ApkSizeCalculator.getDefault());
        ArchiveTreeStructure.sort(
                root,
                (o1, o2) ->
                        Longs.compare(
                                o2.getData().getRawFileSize(), o1.getData().getRawFileSize()));
        String actual =
                dumpTree(
                        root,
                        n -> {
                            ArchiveEntry entry = n.getData();
                            return String.format(
                                    Locale.US,
                                    "%1$-10d %2$s",
                                    entry.getRawFileSize(),
                                    entry.getFullPathString());
                        });
        String expected =
                "170        /\n"
                        + "153        /instant-run.zip\n"
                        + "2          /instant-run.zip/instant-run/\n"
                        + "2          /instant-run.zip/instant-run/classes1.dex\n"
                        + "11         /AndroidManifest.xml\n"
                        + "6          /res/\n"
                        + "6          /res/anim/\n"
                        + "6          /res/anim/fade.xml";
        assertThat(actual).isEqualTo(expected);
    }

    private static String dumpTree(
            @NonNull ArchiveNode root, @NonNull Function<ArchiveNode, String> mapper) {
        return ArchiveTreeStream.preOrderStream(root).map(mapper).collect(Collectors.joining("\n"));
    }
}
