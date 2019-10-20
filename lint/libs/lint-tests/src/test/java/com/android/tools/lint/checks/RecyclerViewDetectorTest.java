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

package com.android.tools.lint.checks;

import com.android.tools.lint.detector.api.Detector;

@SuppressWarnings({"ClassNameDiffersFromFileName", "MethodMayBeStatic", "SpellCheckingInspection"})
public class RecyclerViewDetectorTest extends AbstractCheckTest {
    public void testFixedPosition() throws Exception {
        assertEquals(
                ""
                        + "src/test/pkg/RecyclerViewTest.java:69: Error: Do not treat position as fixed; only use immediately and call holder.getAdapterPosition() to look it up later [RecyclerView]\n"
                        + "        public void onBindViewHolder(ViewHolder holder, int position) {\n"
                        + "                                                        ~~~~~~~~~~~~\n"
                        + "src/test/pkg/RecyclerViewTest.java:82: Error: Do not treat position as fixed; only use immediately and call holder.getAdapterPosition() to look it up later [RecyclerView]\n"
                        + "        public void onBindViewHolder(ViewHolder holder, final int position) {\n"
                        + "                                                        ~~~~~~~~~~~~~~~~~~\n"
                        + "src/test/pkg/RecyclerViewTest.java:102: Error: Do not treat position as fixed; only use immediately and call holder.getAdapterPosition() to look it up later [RecyclerView]\n"
                        + "        public void onBindViewHolder(ViewHolder holder, final int position) {\n"
                        + "                                                        ~~~~~~~~~~~~~~~~~~\n"
                        + "src/test/pkg/RecyclerViewTest.java:111: Error: Do not treat position as fixed; only use immediately and call holder.getAdapterPosition() to look it up later [RecyclerView]\n"
                        + "        public void onBindViewHolder(ViewHolder holder, final int position, List<Object> payloads) {\n"
                        + "                                                        ~~~~~~~~~~~~~~~~~~\n"
                        + "4 errors, 0 warnings\n",
                lintProject(
                        java(
                                "src/test/pkg/RecyclerViewTest.java",
                                ""
                                        + "package test.pkg;\n"
                                        + "\n"
                                        + "import android.support.v7.widget.RecyclerView;\n"
                                        + "import android.view.View;\n"
                                        + "import android.widget.TextView;\n"
                                        + "\n"
                                        + "import java.util.List;\n"
                                        + "\n"
                                        + "@SuppressWarnings({\"ClassNameDiffersFromFileName\", \"unused\"})\n"
                                        + "public class RecyclerViewTest {\n"
                                        + "    // From https://developer.android.com/training/material/lists-cards.html\n"
                                        + "    public abstract static class Test1 extends RecyclerView.Adapter<Test1.ViewHolder> {\n"
                                        + "        private String[] mDataset;\n"
                                        + "        public static class ViewHolder extends RecyclerView.ViewHolder {\n"
                                        + "            public TextView mTextView;\n"
                                        + "            public ViewHolder(TextView v) {\n"
                                        + "                super(v);\n"
                                        + "                mTextView = v;\n"
                                        + "            }\n"
                                        + "        }\n"
                                        + "\n"
                                        + "        public Test1(String[] myDataset) {\n"
                                        + "            mDataset = myDataset;\n"
                                        + "        }\n"
                                        + "\n"
                                        + "        @Override\n"
                                        + "        public void onBindViewHolder(ViewHolder holder, int position) {\n"
                                        + "            holder.mTextView.setText(mDataset[position]); // OK\n"
                                        + "        }\n"
                                        + "    }\n"
                                        + "\n"
                                        + "    public abstract static class Test2 extends RecyclerView.Adapter<Test2.ViewHolder> {\n"
                                        + "        public static class ViewHolder extends RecyclerView.ViewHolder {\n"
                                        + "            public ViewHolder(View v) {\n"
                                        + "                super(v);\n"
                                        + "            }\n"
                                        + "        }\n"
                                        + "\n"
                                        + "        @Override\n"
                                        + "        public void onBindViewHolder(ViewHolder holder, int position) {\n"
                                        + "            // OK\n"
                                        + "        }\n"
                                        + "    }\n"
                                        + "\n"
                                        + "    public abstract static class Test3 extends RecyclerView.Adapter<Test3.ViewHolder> {\n"
                                        + "        public static class ViewHolder extends RecyclerView.ViewHolder {\n"
                                        + "            public ViewHolder(View v) {\n"
                                        + "                super(v);\n"
                                        + "            }\n"
                                        + "        }\n"
                                        + "\n"
                                        + "        @Override\n"
                                        + "        public void onBindViewHolder(ViewHolder holder, final int position) {\n"
                                        + "            // OK - final, but not referenced\n"
                                        + "\n"
                                        + "        }\n"
                                        + "    }\n"
                                        + "\n"
                                        + "    public abstract static class Test4 extends RecyclerView.Adapter<Test4.ViewHolder> {\n"
                                        + "        private int myCachedPosition;\n"
                                        + "\n"
                                        + "        public static class ViewHolder extends RecyclerView.ViewHolder {\n"
                                        + "            public ViewHolder(View v) {\n"
                                        + "                super(v);\n"
                                        + "            }\n"
                                        + "        }\n"
                                        + "\n"
                                        + "        @Override\n"
                                        + "        public void onBindViewHolder(ViewHolder holder, int position) {\n"
                                        + "            myCachedPosition = position; // ERROR: escapes\n"
                                        + "        }\n"
                                        + "    }\n"
                                        + "\n"
                                        + "    public abstract static class Test5 extends RecyclerView.Adapter<Test5.ViewHolder> {\n"
                                        + "        public static class ViewHolder extends RecyclerView.ViewHolder {\n"
                                        + "            public ViewHolder(View v) {\n"
                                        + "                super(v);\n"
                                        + "            }\n"
                                        + "        }\n"
                                        + "\n"
                                        + "        @Override\n"
                                        + "        public void onBindViewHolder(ViewHolder holder, final int position) {\n"
                                        + "            new Runnable() {\n"
                                        + "                @Override public void run() {\n"
                                        + "                    System.out.println(position); // ERROR: escapes\n"
                                        + "                }\n"
                                        + "            }.run();\n"
                                        + "        }\n"
                                        + "    }\n"
                                        + "\n"
                                        + "    // https://code.google.com/p/android/issues/detail?id=172335\n"
                                        + "    public abstract static class Test6 extends RecyclerView.Adapter<Test6.ViewHolder> {\n"
                                        + "        List<String> myData;\n"
                                        + "        public static class ViewHolder extends RecyclerView.ViewHolder {\n"
                                        + "            private View itemView;\n"
                                        + "            public ViewHolder(View v) {\n"
                                        + "                super(v);\n"
                                        + "            }\n"
                                        + "        }\n"
                                        + "\n"
                                        + "        @Override\n"
                                        + "        public void onBindViewHolder(ViewHolder holder, final int position) {\n"
                                        + "            holder.itemView.setOnClickListener(new View.OnClickListener() {\n"
                                        + "                public void onClick(View view) {\n"
                                        + "                    myData.get(position); // ERROR\n"
                                        + "                }\n"
                                        + "            });\n"
                                        + "        }\n"
                                        + "\n"
                                        + "        @Override\n"
                                        + "        public void onBindViewHolder(ViewHolder holder, final int position, List<Object> payloads) {\n"
                                        + "            holder.itemView.setOnClickListener(new View.OnClickListener() {\n"
                                        + "                public void onClick(View view) {\n"
                                        + "                    myData.get(position); // ERROR\n"
                                        + "                }\n"
                                        + "            });\n"
                                        + "        }\n"
                                        + "    }\n"
                                        + "}\n"),
                        mRecyclerViewStub));
    }

    @SuppressWarnings("all")
    public void testExecuteBindings() throws Exception {
        assertEquals(
                ""
                        + "src/test/pkg/RecyclerViewTest2.java:32: Error: You must call holder.dataBinder.executePendingBindings() before the onBind method exits, otherwise, the DataBinding library will update the UI in the next animation frame causing a delayed update & potential jumps if the item resizes. [PendingBindings]\n"
                        + "            holder.dataBinder.someMethod(); // ERROR - no pending call\n"
                        + "            ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                        + "src/test/pkg/RecyclerViewTest2.java:40: Error: You must call holder.dataBinder.executePendingBindings() before the onBind method exits, otherwise, the DataBinding library will update the UI in the next animation frame causing a delayed update & potential jumps if the item resizes. [PendingBindings]\n"
                        + "            holder.dataBinder.someMethod(); // ERROR: After call\n"
                        + "            ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                        + "src/test/pkg/RecyclerViewTest2.java:48: Error: You must call holder.dataBinder.executePendingBindings() before the onBind method exits, otherwise, the DataBinding library will update the UI in the next animation frame causing a delayed update & potential jumps if the item resizes. [PendingBindings]\n"
                        + "                holder.dataBinder.someMethod(); // ERROR: can't reach pending\n"
                        + "                ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                        + "src/test/pkg/RecyclerViewTest2.java:116: Error: You must call holder.dataBinder.executePendingBindings() before the onBind method exits, otherwise, the DataBinding library will update the UI in the next animation frame causing a delayed update & potential jumps if the item resizes. [PendingBindings]\n"
                        + "                holder.dataBinder.someMethod(); // ERROR\n"
                        + "                ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                        + "src/test/pkg/RecyclerViewTest2.java:139: Error: You must call holder.dataBinder.executePendingBindings() before the onBind method exits, otherwise, the DataBinding library will update the UI in the next animation frame causing a delayed update & potential jumps if the item resizes. [PendingBindings]\n"
                        + "                    holder.dataBinder.someMethod(); // ERROR: no fallthrough\n"
                        + "                    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                        + "5 errors, 0 warnings\n",
                lintProject(
                        java(
                                "src/test/pkg/RecyclerViewTest2.java",
                                ""
                                        + "package test.pkg;\n"
                                        + "\n"
                                        + "import android.support.v7.widget.RecyclerView;\n"
                                        + "import android.view.View;\n"
                                        + "import android.widget.TextView;\n"
                                        + "\n"
                                        + "@SuppressWarnings({\"unused\", \"ConstantIfStatement\", \"ConstantConditions\", \"StatementWithEmptyBody\"})\n"
                                        + "public class RecyclerViewTest2 {\n"
                                        + "    // From https://developer.android.com/training/material/lists-cards.html\n"
                                        + "    public abstract static class AbstractTest extends RecyclerView.Adapter<AbstractTest.ViewHolder> {\n"
                                        + "        public static class ViewHolder extends RecyclerView.ViewHolder {\n"
                                        + "            public TextView mTextView;\n"
                                        + "            public ViewDataBinding dataBinder;\n"
                                        + "            public ViewHolder(TextView v) {\n"
                                        + "                super(v);\n"
                                        + "                mTextView = v;\n"
                                        + "            }\n"
                                        + "        }\n"
                                        + "    }\n"
                                        + "\n"
                                        + "    public abstract static class Test1 extends AbstractTest {\n"
                                        + "        @Override\n"
                                        + "        public void onBindViewHolder(ViewHolder holder, int position) {\n"
                                        + "            holder.dataBinder.someMethod(); // OK\n"
                                        + "            holder.dataBinder.executePendingBindings();\n"
                                        + "        }\n"
                                        + "    }\n"
                                        + "\n"
                                        + "    public abstract static class Test2 extends AbstractTest {\n"
                                        + "        @Override\n"
                                        + "        public void onBindViewHolder(ViewHolder holder, int position) {\n"
                                        + "            holder.dataBinder.someMethod(); // ERROR - no pending call\n"
                                        + "        }\n"
                                        + "    }\n"
                                        + "\n"
                                        + "    public abstract static class Test3 extends AbstractTest {\n"
                                        + "        @Override\n"
                                        + "        public void onBindViewHolder(ViewHolder holder, int position) {\n"
                                        + "            holder.dataBinder.executePendingBindings();\n"
                                        + "            holder.dataBinder.someMethod(); // ERROR: After call\n"
                                        + "        }\n"
                                        + "    }\n"
                                        + "\n"
                                        + "    public abstract static class Test4 extends AbstractTest {\n"
                                        + "        @Override\n"
                                        + "        public void onBindViewHolder(ViewHolder holder, int position) {\n"
                                        + "            if (true) {\n"
                                        + "                holder.dataBinder.someMethod(); // ERROR: can't reach pending\n"
                                        + "            } else {\n"
                                        + "                holder.dataBinder.executePendingBindings();\n"
                                        + "            }\n"
                                        + "        }\n"
                                        + "    }\n"
                                        + "\n"
                                        + "    public abstract static class Test5 extends AbstractTest {\n"
                                        + "        @Override\n"
                                        + "        public void onBindViewHolder(ViewHolder holder, int position) {\n"
                                        + "            holder.dataBinder.someMethod(); // OK\n"
                                        + "            if (true) {\n"
                                        + "                if (true) {\n"
                                        + "                    if (false) {\n"
                                        + "\n"
                                        + "                    } else {\n"
                                        + "                        holder.dataBinder.executePendingBindings();\n"
                                        + "                    }\n"
                                        + "                }\n"
                                        + "            }\n"
                                        + "        }\n"
                                        + "    }\n"
                                        + "\n"
                                        + "    /* We don't yet track variable reassignment to compute equivalent data binders\n"
                                        + "    public abstract static class Test6 extends AbstractTest {\n"
                                        + "        @Override\n"
                                        + "        public void onBindViewHolder(ViewHolder holder, int position) {\n"
                                        + "            holder.dataBinder.someMethod(); // OK\n"
                                        + "            ViewDataBinding dataBinder = holder.dataBinder;\n"
                                        + "            dataBinder.executePendingBindings();\n"
                                        + "        }\n"
                                        + "    }\n"
                                        + "    */\n"
                                        + "\n"
                                        + "    public abstract static class Test7 extends AbstractTest {\n"
                                        + "        @Override\n"
                                        + "        public void onBindViewHolder(ViewHolder holder, int position) {\n"
                                        + "            if (true) {\n"
                                        + "                holder.dataBinder.someMethod(); // OK\n"
                                        + "            }\n"
                                        + "            holder.dataBinder.executePendingBindings();\n"
                                        + "        }\n"
                                        + "    }\n"
                                        + "\n"
                                        + "    public abstract static class Test8 extends AbstractTest {\n"
                                        + "        @Override\n"
                                        + "        public void onBindViewHolder(ViewHolder holder, int position) {\n"
                                        + "            holder.dataBinder.someMethod(); // OK\n"
                                        + "            synchronized (this) {\n"
                                        + "                holder.dataBinder.executePendingBindings();\n"
                                        + "            }\n"
                                        + "        }\n"
                                        + "    }\n"
                                        + "\n"
                                        + "    public abstract static class Test9 extends AbstractTest {\n"
                                        + "        @SuppressWarnings(\"UnusedLabel\")\n"
                                        + "        @Override\n"
                                        + "        public void onBindViewHolder(ViewHolder holder, int position) {\n"
                                        + "            holder.dataBinder.someMethod(); // OK\n"
                                        + "        myLabel:\n"
                                        + "            holder.dataBinder.executePendingBindings();\n"
                                        + "        }\n"
                                        + "    }\n"
                                        + "\n"
                                        + "    public abstract static class Test10 extends AbstractTest {\n"
                                        + "        @Override\n"
                                        + "        public void onBindViewHolder(ViewHolder holder, int position) {\n"
                                        + "            if (true) {\n"
                                        + "                holder.dataBinder.someMethod(); // ERROR\n"
                                        + "                return;\n"
                                        + "            }\n"
                                        + "            holder.dataBinder.executePendingBindings();\n"
                                        + "        }\n"
                                        + "    }\n"
                                        + "\n"
                                        + "    public abstract static class Test11 extends AbstractTest {\n"
                                        + "        @Override\n"
                                        + "        public void onBindViewHolder(ViewHolder holder, int position) {\n"
                                        + "            switch (position) {\n"
                                        + "                case 1: holder.dataBinder.someMethod(); // OK: fallthrough\n"
                                        + "                case 2: holder.dataBinder.executePendingBindings();\n"
                                        + "            }\n"
                                        + "        }\n"
                                        + "    }\n"
                                        + "\n"
                                        + "    public abstract static class Test12 extends AbstractTest {\n"
                                        + "        @Override\n"
                                        + "        public void onBindViewHolder(ViewHolder holder, int position) {\n"
                                        + "            switch (position) {\n"
                                        + "                case 1:\n"
                                        + "                    holder.dataBinder.someMethod(); // Not last: don't flag\n"
                                        + "                    holder.dataBinder.someMethod(); // ERROR: no fallthrough\n"
                                        + "                    break;\n"
                                        + "                case 2:\n"
                                        + "                    holder.dataBinder.executePendingBindings();\n"
                                        + "            }\n"
                                        + "        }\n"
                                        + "    }\n"
                                        + "\n"
                                        + "    public abstract static class Test13 extends AbstractTest {\n"
                                        + "        @Override\n"
                                        + "        public void onBindViewHolder(ViewHolder holder, int position) {\n"
                                        + "            do {\n"
                                        + "                holder.dataBinder.someMethod(); // OK\n"
                                        + "                holder.dataBinder.executePendingBindings();\n"
                                        + "            } while (position-- >= 0);\n"
                                        + "        }\n"
                                        + "    }\n"
                                        + "\n"
                                        + "    public static class ViewDataBinding {\n"
                                        + "        private View root;\n"
                                        + "\n"
                                        + "        public void someMethod() {\n"
                                        + "        }\n"
                                        + "        public void executePendingBindings() {\n"
                                        + "        }\n"
                                        + "\n"
                                        + "        public View getRoot() {\n"
                                        + "            return root;\n"
                                        + "        }\n"
                                        + "    }\n"
                                        + "}\n"),
                        mRecyclerViewStub));
    }

    @Override
    protected Detector getDetector() {
        return new RecyclerViewDetector();
    }

    private final TestFile mRecyclerViewStub =
            java(
                    "src/android/support/v7/widget/RecyclerView.java",
                    ""
                            + "package android.support.v7.widget;\n"
                            + "\n"
                            + "import android.content.Context;\n"
                            + "import android.util.AttributeSet;\n"
                            + "import android.view.View;\n"
                            + "import java.util.List;\n"
                            + "\n"
                            + "// Just a stub for lint unit tests\n"
                            + "public class RecyclerView extends View {\n"
                            + "    public RecyclerView(Context context, AttributeSet attrs) {\n"
                            + "        super(context, attrs);\n"
                            + "    }\n"
                            + "\n"
                            + "    public abstract static class ViewHolder {\n"
                            + "        public ViewHolder(View itemView) {\n"
                            + "        }\n"
                            + "    }\n"
                            + "\n"
                            + "    public abstract static class Adapter<VH extends ViewHolder> {\n"
                            + "        public abstract void onBindViewHolder(VH holder, int position);\n"
                            + "        public void onBindViewHolder(VH holder, int position, List<Object> payloads) {\n"
                            + "        }\n"
                            + "    }\n"
                            + "}\n");
}
