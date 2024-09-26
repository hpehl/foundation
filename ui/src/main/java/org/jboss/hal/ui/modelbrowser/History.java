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
