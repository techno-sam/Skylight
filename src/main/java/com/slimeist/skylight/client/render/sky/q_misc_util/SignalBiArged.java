package com.slimeist.skylight.client.render.sky.q_misc_util;

/*
   Copyright 2020 qouteall

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

Modified by Slimeist to only render the sky
 */

import org.apache.logging.log4j.util.BiConsumer;
import org.apache.logging.log4j.util.TriConsumer;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class SignalBiArged<A, B> {
    private List<BiConsumer<A, B>> funcList = new ArrayList<>();
    private boolean isEmitting = false;

    public void emit(A a, B b) {
        isEmitting = true;
        try {
            funcList.forEach(runnable -> runnable.accept(a, b));
        }
        finally {
            isEmitting = false;
        }
    }

    public static class SimpleBox<T> {
        public T obj;

        public SimpleBox(T obj) {
            this.obj = obj;
        }
    }

    //NOTE the func should not capture owner
    public <T> void connectWithWeakRef(T owner, TriConsumer<T, A, B> func) {
        //NOTE using weak hash map was a mistake
        //https://stackoverflow.com/questions/8051912/will-a-weakhashmaps-entry-be-collected-if-the-value-contains-the-only-strong-re

        WeakReference<T> weakRef = new WeakReference<>(owner);
        SimpleBox<BiConsumer<A, B>> boxOfRunnable = new SimpleBox<>(null);
        boxOfRunnable.obj = (a, b) -> {
            T currentTarget = weakRef.get();
            if (currentTarget != null) {
                func.accept(currentTarget, a, b);
            }
            else {
                disconnect(boxOfRunnable.obj);
            }
        };
        connect(boxOfRunnable.obj);
    }

    public void connect(BiConsumer<A, B> func) {
        copyDataWhenEmitting();
        funcList.add(func);
    }

    public void disconnect(BiConsumer<A, B> func) {
        copyDataWhenEmitting();
        boolean removed = funcList.remove(func);
        assert removed;
    }

    private void copyDataWhenEmitting() {
        if (isEmitting) {
            funcList = new ArrayList<>(funcList);
        }
    }
}