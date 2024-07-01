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
package org.jboss.hal.env;

public enum Stability {

    EXPERIMENTAL(300, "E", "experimental"),

    PREVIEW(200, "P", "preview"),

    COMMUNITY(100, "C", "community"),

    DEFAULT(0, "D", "default");

    public static Stability parse(String value) {
        Stability stability = COMMUNITY;
        if (value != null) {
            try {
                stability = Stability.valueOf(value.toUpperCase());
            } catch (IllegalArgumentException ignore) {
                // ignore
            }
        }
        return stability;
    }

    public final int order;
    public final String letter;
    public final String label;

    Stability(int order, String letter, String label) {
        this.order = order;
        this.letter = letter;
        this.label = label;
    }
}
