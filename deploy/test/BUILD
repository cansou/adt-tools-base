load("//tools/base/bazel:android.bzl", "dex_library")
load("//tools/base/fakeandroid:fakeandroid.bzl", "fake_android_test")
load("//tools/base/bazel:kotlin.bzl", "kotlin_library")

package(default_testonly = True)

fake_android_test(
    name = "BasicTest",
    size = "medium",
    srcs = glob(["java/com/android/tools/deploy/swapper/*.java"]),
    data = [
        ":test-app",
        "//tools/base/deploy/agent/native:agent_server",
        "//tools/base/deploy/agent/native:libswap.so",
        "//tools/base/deploy/test/apk1:apk",
        "//tools/base/deploy/test/apk2:apk",
    ],
    jvm_flags = [
        # Location of the inital test app.
        "-Dapp.dex.location=$(location :test-app)",

        # Location of the dex files to be swapped in.
        "-Dapp.swap.dex.location=$(location :test-app-swap)",

        # JVMTI Agent for the host.
        "-Dswap.agent.location=$(location //tools/base/deploy/agent/native:libswap.so)",

        # Agent server for communcation with the agent.
        "-Dswap.server.location=$(location //tools/base/deploy/agent/native:agent_server)",

        # APKs for testing the DexArchiveComparator
        "-Dapk1.location=$(location //tools/base/deploy/test/apk1:apk)",
        "-Dapk2.location=$(location //tools/base/deploy/test/apk2:apk)",
    ],
    deps = [
        ":test-app",
        ":test-app-swap",
        "//prebuilts/tools/common/m2/repository/com/google/protobuf/protobuf-java/3.4.0:jar",
        "//tools/base/bazel:langtools",
        "//tools/base/deploy/deployer:tools.deployer",
        "//tools/base/deploy/proto:deploy_java_proto",
        "//tools/base/deploy/test/apk1:apk",
        "//tools/base/deploy/test/apk2:apk",
        "//tools/base/fakeandroid",
        "//tools/idea/.idea/libraries:JUnit4",
    ],
)

dex_library(
    name = "test-app",
    dexer = "D8",
    # Test dex compiles with a non-release build.
    # Also make it desugar as much as possible with API 23.
    flags = [
        "--debug",
        "--min-api 23",
    ],
    jars = [":test-app_java_deploy.jar"],
)

java_binary(
    name = "test-app_java",
    srcs = glob(["java/com/android/tools/deploy/swapper/testapp/**/*.java"]),
    create_executable = 0,
    deps = [
        ":test-app-kotlin-lib_java",
        "//tools/base/fakeandroid:android-mock",
    ],
)

dex_library(
    name = "test-app-swap",
    dexer = "D8",
    flags = [
        "--file-per-class",
        "--debug",
        "--min-api 23",
    ],
    jars = [":test-app-swap_java_deploy.jar"],
)

java_binary(
    name = "test-app-swap_java",
    srcs =
        glob(["test_swap/com/android/tools/deploy/swapper/testapp/**/*.java"]) + [
            "java/com/android/tools/deploy/swapper/testapp/TestActivity.java",
        ],
    create_executable = 0,
    deps = [
        ":test-app-kotlin-lib-swap_java",
        "//tools/base/fakeandroid:android-mock",
    ],
)

kotlin_library(
    name = "test-app-kotlin-lib_java",
    srcs = ["kotlin"],
)

kotlin_library(
    name = "test-app-kotlin-lib-swap_java",
    srcs = ["kotlin_swap"],
)
