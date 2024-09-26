/*
 *  Copyright 2024 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.ui.modelbrowser;

import java.util.Stack;

class History<T> {

    private final Stack<T> backStack;
    private final Stack<T> forwardStack;
    private boolean back;
    private boolean forward;
    private T current;

    History() {
        backStack = new Stack<>();
        forwardStack = new Stack<>();
        back = false;
        forward = false;
        current = null;
    }

    void navigate(T item) {
        if (current != null) {
            backStack.push(current);
        }
        current = item;
        forwardStack.clear();
        back = !backStack.isEmpty();
        forward = false;
    }

    boolean canGoBack() {
        return back;
    }

    T back() {
        if (!backStack.isEmpty()) {
            if (current != null) {
                forwardStack.push(current);
            }
            current = backStack.pop();
            back = !backStack.isEmpty();
            forward = true;
        } else {
            back = false;
            forward = !forwardStack.isEmpty();
        }
        return current;
    }

    T peekBack() {
        if (!backStack.isEmpty()) {
            return backStack.peek();
        }
        return current;
    }

    boolean canGoForward() {
        return forward;
    }

    T forward() {
        if (!forwardStack.isEmpty()) {
            if (current != null) {
                backStack.push(current);
            }
            current = forwardStack.pop();
            back = true;
            forward = !forwardStack.isEmpty();
        } else {
            back = !backStack.isEmpty();
            forward = false;
        }
        return current;
    }

    T peekForward() {
        if (!forwardStack.isEmpty()) {
            return forwardStack.peek();
        }
        return current;
    }

    T current() {
        return current;
    }
}
