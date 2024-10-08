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

import org.junit.jupiter.api.Test;

import static java.lang.System.arraycopy;
import static org.jboss.hal.env.Stability.COMMUNITY;
import static org.jboss.hal.env.Stability.DEFAULT;
import static org.jboss.hal.env.Stability.EXPERIMENTAL;
import static org.jboss.hal.env.Stability.PREVIEW;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnvironmentTest {

    @Test
    void highlight() {
        assertFalse(environment(DEFAULT, DEFAULT).highlightStability());
        assertFalse(environment(DEFAULT, COMMUNITY).highlightStability());
        assertTrue(environment(DEFAULT, PREVIEW).highlightStability());
        assertTrue(environment(DEFAULT, EXPERIMENTAL).highlightStability());

        assertFalse(environment(COMMUNITY, DEFAULT).highlightStability());
        assertFalse(environment(COMMUNITY, COMMUNITY).highlightStability());
        assertTrue(environment(COMMUNITY, PREVIEW).highlightStability());
        assertTrue(environment(COMMUNITY, EXPERIMENTAL).highlightStability());

        assertFalse(environment(PREVIEW, DEFAULT).highlightStability());
        assertFalse(environment(PREVIEW, COMMUNITY).highlightStability());
        assertTrue(environment(PREVIEW, PREVIEW).highlightStability());
        assertTrue(environment(PREVIEW, EXPERIMENTAL).highlightStability());

        assertFalse(environment(EXPERIMENTAL, DEFAULT).highlightStability());
        assertFalse(environment(EXPERIMENTAL, COMMUNITY).highlightStability());
        assertFalse(environment(EXPERIMENTAL, PREVIEW).highlightStability());
        assertTrue(environment(EXPERIMENTAL, EXPERIMENTAL).highlightStability());
    }

    @Test
    void highlightStability() {
        // built-in stability must not be null,
        // but we pass null to state that it doesn't matter for this unit test
        Environment defaultEnvironment = environment(null, DEFAULT);
        assertFalse(defaultEnvironment.highlightStability(DEFAULT));
        assertFalse(defaultEnvironment.highlightStability(COMMUNITY));
        assertTrue(defaultEnvironment.highlightStability(PREVIEW));
        assertTrue(defaultEnvironment.highlightStability(EXPERIMENTAL));

        Environment communityEnvironment = environment(null, COMMUNITY);
        assertFalse(communityEnvironment.highlightStability(DEFAULT));
        assertFalse(communityEnvironment.highlightStability(COMMUNITY));
        assertTrue(communityEnvironment.highlightStability(PREVIEW));
        assertTrue(communityEnvironment.highlightStability(EXPERIMENTAL));

        Environment previewEnvironment = environment(null, PREVIEW);
        assertFalse(previewEnvironment.highlightStability(DEFAULT));
        assertFalse(previewEnvironment.highlightStability(COMMUNITY));
        assertTrue(previewEnvironment.highlightStability(PREVIEW));
        assertTrue(previewEnvironment.highlightStability(EXPERIMENTAL));

        Environment experimentalEnvironment = environment(null, EXPERIMENTAL);
        assertFalse(experimentalEnvironment.highlightStability(DEFAULT));
        assertFalse(experimentalEnvironment.highlightStability(COMMUNITY));
        assertFalse(experimentalEnvironment.highlightStability(PREVIEW));
        assertTrue(experimentalEnvironment.highlightStability(EXPERIMENTAL));
    }

    @Test
    void highlightDependentStability() {

        // ------------------------------------------------------ DEFAULT
        // built-in stability must not be null,
        // but we pass null to state that it doesn't matter for this unit test
        Environment defaultEnvironment = environment(null, DEFAULT);

        assertFalse(defaultEnvironment.highlightStability(DEFAULT, DEFAULT));
        assertFalse(defaultEnvironment.highlightStability(DEFAULT, DEFAULT, DEFAULT));
        assertFalse(defaultEnvironment.highlightStability(DEFAULT, DEFAULT, COMMUNITY));
        assertTrue(defaultEnvironment.highlightStability(DEFAULT, DEFAULT, PREVIEW));
        assertTrue(defaultEnvironment.highlightStability(DEFAULT, DEFAULT, EXPERIMENTAL));

        assertFalse(defaultEnvironment.highlightStability(DEFAULT, COMMUNITY));
        assertFalse(defaultEnvironment.highlightStability(DEFAULT, COMMUNITY, DEFAULT));
        assertFalse(defaultEnvironment.highlightStability(DEFAULT, COMMUNITY, COMMUNITY));
        assertTrue(defaultEnvironment.highlightStability(DEFAULT, COMMUNITY, PREVIEW));
        assertTrue(defaultEnvironment.highlightStability(DEFAULT, COMMUNITY, EXPERIMENTAL));

        assertTrue(defaultEnvironment.highlightStability(DEFAULT, PREVIEW));
        assertFalse(defaultEnvironment.highlightStability(DEFAULT, PREVIEW, DEFAULT));
        assertFalse(defaultEnvironment.highlightStability(DEFAULT, PREVIEW, COMMUNITY));
        assertTrue(defaultEnvironment.highlightStability(DEFAULT, PREVIEW, PREVIEW));
        assertTrue(defaultEnvironment.highlightStability(DEFAULT, PREVIEW, EXPERIMENTAL));

        assertTrue(defaultEnvironment.highlightStability(DEFAULT, EXPERIMENTAL));
        assertFalse(defaultEnvironment.highlightStability(DEFAULT, EXPERIMENTAL, DEFAULT));
        assertFalse(defaultEnvironment.highlightStability(DEFAULT, EXPERIMENTAL, COMMUNITY));
        assertFalse(defaultEnvironment.highlightStability(DEFAULT, EXPERIMENTAL, PREVIEW));
        assertTrue(defaultEnvironment.highlightStability(DEFAULT, EXPERIMENTAL, EXPERIMENTAL));

        assertFalse(defaultEnvironment.highlightStability(COMMUNITY, DEFAULT));
        assertFalse(defaultEnvironment.highlightStability(COMMUNITY, DEFAULT, DEFAULT));
        assertFalse(defaultEnvironment.highlightStability(COMMUNITY, DEFAULT, COMMUNITY));
        assertTrue(defaultEnvironment.highlightStability(COMMUNITY, DEFAULT, PREVIEW));
        assertTrue(defaultEnvironment.highlightStability(COMMUNITY, DEFAULT, EXPERIMENTAL));

        assertFalse(defaultEnvironment.highlightStability(COMMUNITY, COMMUNITY));
        assertFalse(defaultEnvironment.highlightStability(COMMUNITY, COMMUNITY, DEFAULT));
        assertFalse(defaultEnvironment.highlightStability(COMMUNITY, COMMUNITY, COMMUNITY));
        assertTrue(defaultEnvironment.highlightStability(COMMUNITY, COMMUNITY, PREVIEW));
        assertTrue(defaultEnvironment.highlightStability(COMMUNITY, COMMUNITY, EXPERIMENTAL));

        assertTrue(defaultEnvironment.highlightStability(COMMUNITY, PREVIEW));
        assertFalse(defaultEnvironment.highlightStability(COMMUNITY, PREVIEW, DEFAULT));
        assertFalse(defaultEnvironment.highlightStability(COMMUNITY, PREVIEW, COMMUNITY));
        assertTrue(defaultEnvironment.highlightStability(COMMUNITY, PREVIEW, PREVIEW));
        assertTrue(defaultEnvironment.highlightStability(COMMUNITY, PREVIEW, EXPERIMENTAL));

        assertTrue(defaultEnvironment.highlightStability(COMMUNITY, EXPERIMENTAL));
        assertFalse(defaultEnvironment.highlightStability(COMMUNITY, EXPERIMENTAL, DEFAULT));
        assertFalse(defaultEnvironment.highlightStability(COMMUNITY, EXPERIMENTAL, COMMUNITY));
        assertFalse(defaultEnvironment.highlightStability(COMMUNITY, EXPERIMENTAL, PREVIEW));
        assertTrue(defaultEnvironment.highlightStability(COMMUNITY, EXPERIMENTAL, EXPERIMENTAL));

        assertFalse(defaultEnvironment.highlightStability(PREVIEW, DEFAULT));
        assertFalse(defaultEnvironment.highlightStability(PREVIEW, DEFAULT, DEFAULT));
        assertFalse(defaultEnvironment.highlightStability(PREVIEW, DEFAULT, COMMUNITY));
        assertTrue(defaultEnvironment.highlightStability(PREVIEW, DEFAULT, PREVIEW));
        assertTrue(defaultEnvironment.highlightStability(PREVIEW, DEFAULT, EXPERIMENTAL));

        assertFalse(defaultEnvironment.highlightStability(PREVIEW, COMMUNITY));
        assertFalse(defaultEnvironment.highlightStability(PREVIEW, COMMUNITY, DEFAULT));
        assertFalse(defaultEnvironment.highlightStability(PREVIEW, COMMUNITY, COMMUNITY));
        assertTrue(defaultEnvironment.highlightStability(PREVIEW, COMMUNITY, PREVIEW));
        assertTrue(defaultEnvironment.highlightStability(PREVIEW, COMMUNITY, EXPERIMENTAL));

        assertTrue(defaultEnvironment.highlightStability(PREVIEW, PREVIEW));
        assertFalse(defaultEnvironment.highlightStability(PREVIEW, PREVIEW, DEFAULT));
        assertFalse(defaultEnvironment.highlightStability(PREVIEW, PREVIEW, COMMUNITY));
        assertTrue(defaultEnvironment.highlightStability(PREVIEW, PREVIEW, PREVIEW));
        assertTrue(defaultEnvironment.highlightStability(PREVIEW, PREVIEW, EXPERIMENTAL));

        assertTrue(defaultEnvironment.highlightStability(PREVIEW, EXPERIMENTAL));
        assertFalse(defaultEnvironment.highlightStability(PREVIEW, EXPERIMENTAL, DEFAULT));
        assertFalse(defaultEnvironment.highlightStability(PREVIEW, EXPERIMENTAL, COMMUNITY));
        assertFalse(defaultEnvironment.highlightStability(PREVIEW, EXPERIMENTAL, PREVIEW));
        assertTrue(defaultEnvironment.highlightStability(PREVIEW, EXPERIMENTAL, EXPERIMENTAL));

        assertFalse(defaultEnvironment.highlightStability(EXPERIMENTAL, DEFAULT));
        assertFalse(defaultEnvironment.highlightStability(EXPERIMENTAL, DEFAULT, DEFAULT));
        assertFalse(defaultEnvironment.highlightStability(EXPERIMENTAL, DEFAULT, COMMUNITY));
        assertFalse(defaultEnvironment.highlightStability(EXPERIMENTAL, DEFAULT, PREVIEW));
        assertTrue(defaultEnvironment.highlightStability(EXPERIMENTAL, DEFAULT, EXPERIMENTAL));

        assertFalse(defaultEnvironment.highlightStability(EXPERIMENTAL, COMMUNITY));
        assertFalse(defaultEnvironment.highlightStability(EXPERIMENTAL, COMMUNITY, DEFAULT));
        assertFalse(defaultEnvironment.highlightStability(EXPERIMENTAL, COMMUNITY, COMMUNITY));
        assertFalse(defaultEnvironment.highlightStability(EXPERIMENTAL, COMMUNITY, PREVIEW));
        assertTrue(defaultEnvironment.highlightStability(EXPERIMENTAL, COMMUNITY, EXPERIMENTAL));

        assertFalse(defaultEnvironment.highlightStability(EXPERIMENTAL, PREVIEW));
        assertFalse(defaultEnvironment.highlightStability(EXPERIMENTAL, PREVIEW, DEFAULT));
        assertFalse(defaultEnvironment.highlightStability(EXPERIMENTAL, PREVIEW, COMMUNITY));
        assertFalse(defaultEnvironment.highlightStability(EXPERIMENTAL, PREVIEW, PREVIEW));
        assertTrue(defaultEnvironment.highlightStability(EXPERIMENTAL, PREVIEW, EXPERIMENTAL));

        assertTrue(defaultEnvironment.highlightStability(EXPERIMENTAL, EXPERIMENTAL));
        assertFalse(defaultEnvironment.highlightStability(EXPERIMENTAL, EXPERIMENTAL, DEFAULT));
        assertFalse(defaultEnvironment.highlightStability(EXPERIMENTAL, EXPERIMENTAL, COMMUNITY));
        assertFalse(defaultEnvironment.highlightStability(EXPERIMENTAL, EXPERIMENTAL, PREVIEW));
        assertTrue(defaultEnvironment.highlightStability(EXPERIMENTAL, EXPERIMENTAL, EXPERIMENTAL));

        // ------------------------------------------------------ COMMUNITY
        // built-in stability must not be null,
        // but we pass null to state that it doesn't matter for this unit test
        Environment communityEnvironment = environment(null, COMMUNITY);

        assertFalse(communityEnvironment.highlightStability(DEFAULT, DEFAULT));
        assertFalse(communityEnvironment.highlightStability(DEFAULT, DEFAULT, DEFAULT));
        assertFalse(communityEnvironment.highlightStability(DEFAULT, DEFAULT, COMMUNITY));
        assertTrue(communityEnvironment.highlightStability(DEFAULT, DEFAULT, PREVIEW));
        assertTrue(communityEnvironment.highlightStability(DEFAULT, DEFAULT, EXPERIMENTAL));

        assertFalse(communityEnvironment.highlightStability(DEFAULT, COMMUNITY));
        assertFalse(communityEnvironment.highlightStability(DEFAULT, COMMUNITY, DEFAULT));
        assertFalse(communityEnvironment.highlightStability(DEFAULT, COMMUNITY, COMMUNITY));
        assertTrue(communityEnvironment.highlightStability(DEFAULT, COMMUNITY, PREVIEW));
        assertTrue(communityEnvironment.highlightStability(DEFAULT, COMMUNITY, EXPERIMENTAL));

        assertTrue(communityEnvironment.highlightStability(DEFAULT, PREVIEW));
        assertFalse(communityEnvironment.highlightStability(DEFAULT, PREVIEW, DEFAULT));
        assertFalse(communityEnvironment.highlightStability(DEFAULT, PREVIEW, COMMUNITY));
        assertTrue(communityEnvironment.highlightStability(DEFAULT, PREVIEW, PREVIEW));
        assertTrue(communityEnvironment.highlightStability(DEFAULT, PREVIEW, EXPERIMENTAL));

        assertTrue(communityEnvironment.highlightStability(DEFAULT, EXPERIMENTAL));
        assertFalse(communityEnvironment.highlightStability(DEFAULT, EXPERIMENTAL, DEFAULT));
        assertFalse(communityEnvironment.highlightStability(DEFAULT, EXPERIMENTAL, COMMUNITY));
        assertFalse(communityEnvironment.highlightStability(DEFAULT, EXPERIMENTAL, PREVIEW));
        assertTrue(communityEnvironment.highlightStability(DEFAULT, EXPERIMENTAL, EXPERIMENTAL));

        assertFalse(communityEnvironment.highlightStability(COMMUNITY, DEFAULT));
        assertFalse(communityEnvironment.highlightStability(COMMUNITY, DEFAULT, DEFAULT));
        assertFalse(communityEnvironment.highlightStability(COMMUNITY, DEFAULT, COMMUNITY));
        assertTrue(communityEnvironment.highlightStability(COMMUNITY, DEFAULT, PREVIEW));
        assertTrue(communityEnvironment.highlightStability(COMMUNITY, DEFAULT, EXPERIMENTAL));

        assertFalse(communityEnvironment.highlightStability(COMMUNITY, COMMUNITY));
        assertFalse(communityEnvironment.highlightStability(COMMUNITY, COMMUNITY, DEFAULT));
        assertFalse(communityEnvironment.highlightStability(COMMUNITY, COMMUNITY, COMMUNITY));
        assertTrue(communityEnvironment.highlightStability(COMMUNITY, COMMUNITY, PREVIEW));
        assertTrue(communityEnvironment.highlightStability(COMMUNITY, COMMUNITY, EXPERIMENTAL));

        assertTrue(communityEnvironment.highlightStability(COMMUNITY, PREVIEW));
        assertFalse(communityEnvironment.highlightStability(COMMUNITY, PREVIEW, DEFAULT));
        assertFalse(communityEnvironment.highlightStability(COMMUNITY, PREVIEW, COMMUNITY));
        assertTrue(communityEnvironment.highlightStability(COMMUNITY, PREVIEW, PREVIEW));
        assertTrue(communityEnvironment.highlightStability(COMMUNITY, PREVIEW, EXPERIMENTAL));

        assertTrue(communityEnvironment.highlightStability(COMMUNITY, EXPERIMENTAL));
        assertFalse(communityEnvironment.highlightStability(COMMUNITY, EXPERIMENTAL, DEFAULT));
        assertFalse(communityEnvironment.highlightStability(COMMUNITY, EXPERIMENTAL, COMMUNITY));
        assertFalse(communityEnvironment.highlightStability(COMMUNITY, EXPERIMENTAL, PREVIEW));
        assertTrue(communityEnvironment.highlightStability(COMMUNITY, EXPERIMENTAL, EXPERIMENTAL));

        assertFalse(communityEnvironment.highlightStability(PREVIEW, DEFAULT));
        assertFalse(communityEnvironment.highlightStability(PREVIEW, DEFAULT, DEFAULT));
        assertFalse(communityEnvironment.highlightStability(PREVIEW, DEFAULT, COMMUNITY));
        assertTrue(communityEnvironment.highlightStability(PREVIEW, DEFAULT, PREVIEW));
        assertTrue(communityEnvironment.highlightStability(PREVIEW, DEFAULT, EXPERIMENTAL));

        assertFalse(communityEnvironment.highlightStability(PREVIEW, COMMUNITY));
        assertFalse(communityEnvironment.highlightStability(PREVIEW, COMMUNITY, DEFAULT));
        assertFalse(communityEnvironment.highlightStability(PREVIEW, COMMUNITY, COMMUNITY));
        assertTrue(communityEnvironment.highlightStability(PREVIEW, COMMUNITY, PREVIEW));
        assertTrue(communityEnvironment.highlightStability(PREVIEW, COMMUNITY, EXPERIMENTAL));

        assertTrue(communityEnvironment.highlightStability(PREVIEW, PREVIEW));
        assertFalse(communityEnvironment.highlightStability(PREVIEW, PREVIEW, DEFAULT));
        assertFalse(communityEnvironment.highlightStability(PREVIEW, PREVIEW, COMMUNITY));
        assertTrue(communityEnvironment.highlightStability(PREVIEW, PREVIEW, PREVIEW));
        assertTrue(communityEnvironment.highlightStability(PREVIEW, PREVIEW, EXPERIMENTAL));

        assertTrue(communityEnvironment.highlightStability(PREVIEW, EXPERIMENTAL));
        assertFalse(communityEnvironment.highlightStability(PREVIEW, EXPERIMENTAL, DEFAULT));
        assertFalse(communityEnvironment.highlightStability(PREVIEW, EXPERIMENTAL, COMMUNITY));
        assertFalse(communityEnvironment.highlightStability(PREVIEW, EXPERIMENTAL, PREVIEW));
        assertTrue(communityEnvironment.highlightStability(PREVIEW, EXPERIMENTAL, EXPERIMENTAL));

        assertFalse(communityEnvironment.highlightStability(EXPERIMENTAL, DEFAULT));
        assertFalse(communityEnvironment.highlightStability(EXPERIMENTAL, DEFAULT, DEFAULT));
        assertFalse(communityEnvironment.highlightStability(EXPERIMENTAL, DEFAULT, COMMUNITY));
        assertFalse(communityEnvironment.highlightStability(EXPERIMENTAL, DEFAULT, PREVIEW));
        assertTrue(communityEnvironment.highlightStability(EXPERIMENTAL, DEFAULT, EXPERIMENTAL));

        assertFalse(communityEnvironment.highlightStability(EXPERIMENTAL, COMMUNITY));
        assertFalse(communityEnvironment.highlightStability(EXPERIMENTAL, COMMUNITY, DEFAULT));
        assertFalse(communityEnvironment.highlightStability(EXPERIMENTAL, COMMUNITY, COMMUNITY));
        assertFalse(communityEnvironment.highlightStability(EXPERIMENTAL, COMMUNITY, PREVIEW));
        assertTrue(communityEnvironment.highlightStability(EXPERIMENTAL, COMMUNITY, EXPERIMENTAL));

        assertFalse(communityEnvironment.highlightStability(EXPERIMENTAL, PREVIEW));
        assertFalse(communityEnvironment.highlightStability(EXPERIMENTAL, PREVIEW, DEFAULT));
        assertFalse(communityEnvironment.highlightStability(EXPERIMENTAL, PREVIEW, COMMUNITY));
        assertFalse(communityEnvironment.highlightStability(EXPERIMENTAL, PREVIEW, PREVIEW));
        assertTrue(communityEnvironment.highlightStability(EXPERIMENTAL, PREVIEW, EXPERIMENTAL));

        assertTrue(communityEnvironment.highlightStability(EXPERIMENTAL, EXPERIMENTAL));
        assertFalse(communityEnvironment.highlightStability(EXPERIMENTAL, EXPERIMENTAL, DEFAULT));
        assertFalse(communityEnvironment.highlightStability(EXPERIMENTAL, EXPERIMENTAL, COMMUNITY));
        assertFalse(communityEnvironment.highlightStability(EXPERIMENTAL, EXPERIMENTAL, PREVIEW));
        assertTrue(communityEnvironment.highlightStability(EXPERIMENTAL, EXPERIMENTAL, EXPERIMENTAL));

        // ------------------------------------------------------ PREVIEW
        // built-in stability must not be null,
        // but we pass null to state that it doesn't matter for this unit test
        Environment previewEnvironment = environment(null, PREVIEW);

        assertFalse(previewEnvironment.highlightStability(DEFAULT, DEFAULT));
        assertFalse(previewEnvironment.highlightStability(DEFAULT, DEFAULT, DEFAULT));
        assertFalse(previewEnvironment.highlightStability(DEFAULT, DEFAULT, COMMUNITY));
        assertTrue(previewEnvironment.highlightStability(DEFAULT, DEFAULT, PREVIEW));
        assertTrue(previewEnvironment.highlightStability(DEFAULT, DEFAULT, EXPERIMENTAL));

        assertFalse(previewEnvironment.highlightStability(DEFAULT, COMMUNITY));
        assertFalse(previewEnvironment.highlightStability(DEFAULT, COMMUNITY, DEFAULT));
        assertFalse(previewEnvironment.highlightStability(DEFAULT, COMMUNITY, COMMUNITY));
        assertTrue(previewEnvironment.highlightStability(DEFAULT, COMMUNITY, PREVIEW));
        assertTrue(previewEnvironment.highlightStability(DEFAULT, COMMUNITY, EXPERIMENTAL));

        assertTrue(previewEnvironment.highlightStability(DEFAULT, PREVIEW));
        assertFalse(previewEnvironment.highlightStability(DEFAULT, PREVIEW, DEFAULT));
        assertFalse(previewEnvironment.highlightStability(DEFAULT, PREVIEW, COMMUNITY));
        assertTrue(previewEnvironment.highlightStability(DEFAULT, PREVIEW, PREVIEW));
        assertTrue(previewEnvironment.highlightStability(DEFAULT, PREVIEW, EXPERIMENTAL));

        assertTrue(previewEnvironment.highlightStability(DEFAULT, EXPERIMENTAL));
        assertFalse(previewEnvironment.highlightStability(DEFAULT, EXPERIMENTAL, DEFAULT));
        assertFalse(previewEnvironment.highlightStability(DEFAULT, EXPERIMENTAL, COMMUNITY));
        assertFalse(previewEnvironment.highlightStability(DEFAULT, EXPERIMENTAL, PREVIEW));
        assertTrue(previewEnvironment.highlightStability(DEFAULT, EXPERIMENTAL, EXPERIMENTAL));

        assertFalse(previewEnvironment.highlightStability(COMMUNITY, DEFAULT));
        assertFalse(previewEnvironment.highlightStability(COMMUNITY, DEFAULT, DEFAULT));
        assertFalse(previewEnvironment.highlightStability(COMMUNITY, DEFAULT, COMMUNITY));
        assertTrue(previewEnvironment.highlightStability(COMMUNITY, DEFAULT, PREVIEW));
        assertTrue(previewEnvironment.highlightStability(COMMUNITY, DEFAULT, EXPERIMENTAL));

        assertFalse(previewEnvironment.highlightStability(COMMUNITY, COMMUNITY));
        assertFalse(previewEnvironment.highlightStability(COMMUNITY, COMMUNITY, DEFAULT));
        assertFalse(previewEnvironment.highlightStability(COMMUNITY, COMMUNITY, COMMUNITY));
        assertTrue(previewEnvironment.highlightStability(COMMUNITY, COMMUNITY, PREVIEW));
        assertTrue(previewEnvironment.highlightStability(COMMUNITY, COMMUNITY, EXPERIMENTAL));

        assertTrue(previewEnvironment.highlightStability(COMMUNITY, PREVIEW));
        assertFalse(previewEnvironment.highlightStability(COMMUNITY, PREVIEW, DEFAULT));
        assertFalse(previewEnvironment.highlightStability(COMMUNITY, PREVIEW, COMMUNITY));
        assertTrue(previewEnvironment.highlightStability(COMMUNITY, PREVIEW, PREVIEW));
        assertTrue(previewEnvironment.highlightStability(COMMUNITY, PREVIEW, EXPERIMENTAL));

        assertTrue(previewEnvironment.highlightStability(COMMUNITY, EXPERIMENTAL));
        assertFalse(previewEnvironment.highlightStability(COMMUNITY, EXPERIMENTAL, DEFAULT));
        assertFalse(previewEnvironment.highlightStability(COMMUNITY, EXPERIMENTAL, COMMUNITY));
        assertFalse(previewEnvironment.highlightStability(COMMUNITY, EXPERIMENTAL, PREVIEW));
        assertTrue(previewEnvironment.highlightStability(COMMUNITY, EXPERIMENTAL, EXPERIMENTAL));

        assertFalse(previewEnvironment.highlightStability(PREVIEW, DEFAULT));
        assertFalse(previewEnvironment.highlightStability(PREVIEW, DEFAULT, DEFAULT));
        assertFalse(previewEnvironment.highlightStability(PREVIEW, DEFAULT, COMMUNITY));
        assertTrue(previewEnvironment.highlightStability(PREVIEW, DEFAULT, PREVIEW));
        assertTrue(previewEnvironment.highlightStability(PREVIEW, DEFAULT, EXPERIMENTAL));

        assertFalse(previewEnvironment.highlightStability(PREVIEW, COMMUNITY));
        assertFalse(previewEnvironment.highlightStability(PREVIEW, COMMUNITY, DEFAULT));
        assertFalse(previewEnvironment.highlightStability(PREVIEW, COMMUNITY, COMMUNITY));
        assertTrue(previewEnvironment.highlightStability(PREVIEW, COMMUNITY, PREVIEW));
        assertTrue(previewEnvironment.highlightStability(PREVIEW, COMMUNITY, EXPERIMENTAL));

        assertTrue(previewEnvironment.highlightStability(PREVIEW, PREVIEW));
        assertFalse(previewEnvironment.highlightStability(PREVIEW, PREVIEW, DEFAULT));
        assertFalse(previewEnvironment.highlightStability(PREVIEW, PREVIEW, COMMUNITY));
        assertTrue(previewEnvironment.highlightStability(PREVIEW, PREVIEW, PREVIEW));
        assertTrue(previewEnvironment.highlightStability(PREVIEW, PREVIEW, EXPERIMENTAL));

        assertTrue(previewEnvironment.highlightStability(PREVIEW, EXPERIMENTAL));
        assertFalse(previewEnvironment.highlightStability(PREVIEW, EXPERIMENTAL, DEFAULT));
        assertFalse(previewEnvironment.highlightStability(PREVIEW, EXPERIMENTAL, COMMUNITY));
        assertFalse(previewEnvironment.highlightStability(PREVIEW, EXPERIMENTAL, PREVIEW));
        assertTrue(previewEnvironment.highlightStability(PREVIEW, EXPERIMENTAL, EXPERIMENTAL));

        assertFalse(previewEnvironment.highlightStability(EXPERIMENTAL, DEFAULT));
        assertFalse(previewEnvironment.highlightStability(EXPERIMENTAL, DEFAULT, DEFAULT));
        assertFalse(previewEnvironment.highlightStability(EXPERIMENTAL, DEFAULT, COMMUNITY));
        assertFalse(previewEnvironment.highlightStability(EXPERIMENTAL, DEFAULT, PREVIEW));
        assertTrue(previewEnvironment.highlightStability(EXPERIMENTAL, DEFAULT, EXPERIMENTAL));

        assertFalse(previewEnvironment.highlightStability(EXPERIMENTAL, COMMUNITY));
        assertFalse(previewEnvironment.highlightStability(EXPERIMENTAL, COMMUNITY, DEFAULT));
        assertFalse(previewEnvironment.highlightStability(EXPERIMENTAL, COMMUNITY, COMMUNITY));
        assertFalse(previewEnvironment.highlightStability(EXPERIMENTAL, COMMUNITY, PREVIEW));
        assertTrue(previewEnvironment.highlightStability(EXPERIMENTAL, COMMUNITY, EXPERIMENTAL));

        assertFalse(previewEnvironment.highlightStability(EXPERIMENTAL, PREVIEW));
        assertFalse(previewEnvironment.highlightStability(EXPERIMENTAL, PREVIEW, DEFAULT));
        assertFalse(previewEnvironment.highlightStability(EXPERIMENTAL, PREVIEW, COMMUNITY));
        assertFalse(previewEnvironment.highlightStability(EXPERIMENTAL, PREVIEW, PREVIEW));
        assertTrue(previewEnvironment.highlightStability(EXPERIMENTAL, PREVIEW, EXPERIMENTAL));

        assertTrue(previewEnvironment.highlightStability(EXPERIMENTAL, EXPERIMENTAL));
        assertFalse(previewEnvironment.highlightStability(EXPERIMENTAL, EXPERIMENTAL, DEFAULT));
        assertFalse(previewEnvironment.highlightStability(EXPERIMENTAL, EXPERIMENTAL, COMMUNITY));
        assertFalse(previewEnvironment.highlightStability(EXPERIMENTAL, EXPERIMENTAL, PREVIEW));
        assertTrue(previewEnvironment.highlightStability(EXPERIMENTAL, EXPERIMENTAL, EXPERIMENTAL));

        // ------------------------------------------------------ EXPERIMENTAL
        // built-in stability must not be null,
        // but we pass null to state that it doesn't matter for this unit test
        Environment experimentalEnvironment = environment(null, EXPERIMENTAL);

        assertFalse(experimentalEnvironment.highlightStability(DEFAULT, DEFAULT));
        assertFalse(experimentalEnvironment.highlightStability(DEFAULT, DEFAULT, DEFAULT));
        assertFalse(experimentalEnvironment.highlightStability(DEFAULT, DEFAULT, COMMUNITY));
        assertFalse(experimentalEnvironment.highlightStability(DEFAULT, DEFAULT, PREVIEW));
        assertTrue(experimentalEnvironment.highlightStability(DEFAULT, DEFAULT, EXPERIMENTAL));

        assertFalse(experimentalEnvironment.highlightStability(DEFAULT, COMMUNITY));
        assertFalse(experimentalEnvironment.highlightStability(DEFAULT, COMMUNITY, DEFAULT));
        assertFalse(experimentalEnvironment.highlightStability(DEFAULT, COMMUNITY, COMMUNITY));
        assertFalse(experimentalEnvironment.highlightStability(DEFAULT, COMMUNITY, PREVIEW));
        assertTrue(experimentalEnvironment.highlightStability(DEFAULT, COMMUNITY, EXPERIMENTAL));

        assertFalse(experimentalEnvironment.highlightStability(DEFAULT, PREVIEW));
        assertFalse(experimentalEnvironment.highlightStability(DEFAULT, PREVIEW, DEFAULT));
        assertFalse(experimentalEnvironment.highlightStability(DEFAULT, PREVIEW, COMMUNITY));
        assertFalse(experimentalEnvironment.highlightStability(DEFAULT, PREVIEW, PREVIEW));
        assertTrue(experimentalEnvironment.highlightStability(DEFAULT, PREVIEW, EXPERIMENTAL));

        assertTrue(experimentalEnvironment.highlightStability(DEFAULT, EXPERIMENTAL));
        assertFalse(experimentalEnvironment.highlightStability(DEFAULT, EXPERIMENTAL, DEFAULT));
        assertFalse(experimentalEnvironment.highlightStability(DEFAULT, EXPERIMENTAL, COMMUNITY));
        assertFalse(experimentalEnvironment.highlightStability(DEFAULT, EXPERIMENTAL, PREVIEW));
        assertTrue(experimentalEnvironment.highlightStability(DEFAULT, EXPERIMENTAL, EXPERIMENTAL));

        assertFalse(experimentalEnvironment.highlightStability(COMMUNITY, DEFAULT));
        assertFalse(experimentalEnvironment.highlightStability(COMMUNITY, DEFAULT, DEFAULT));
        assertFalse(experimentalEnvironment.highlightStability(COMMUNITY, DEFAULT, COMMUNITY));
        assertFalse(experimentalEnvironment.highlightStability(COMMUNITY, DEFAULT, PREVIEW));
        assertTrue(experimentalEnvironment.highlightStability(COMMUNITY, DEFAULT, EXPERIMENTAL));

        assertFalse(experimentalEnvironment.highlightStability(COMMUNITY, COMMUNITY));
        assertFalse(experimentalEnvironment.highlightStability(COMMUNITY, COMMUNITY, DEFAULT));
        assertFalse(experimentalEnvironment.highlightStability(COMMUNITY, COMMUNITY, COMMUNITY));
        assertFalse(experimentalEnvironment.highlightStability(COMMUNITY, COMMUNITY, PREVIEW));
        assertTrue(experimentalEnvironment.highlightStability(COMMUNITY, COMMUNITY, EXPERIMENTAL));

        assertFalse(experimentalEnvironment.highlightStability(COMMUNITY, PREVIEW));
        assertFalse(experimentalEnvironment.highlightStability(COMMUNITY, PREVIEW, DEFAULT));
        assertFalse(experimentalEnvironment.highlightStability(COMMUNITY, PREVIEW, COMMUNITY));
        assertFalse(experimentalEnvironment.highlightStability(COMMUNITY, PREVIEW, PREVIEW));
        assertTrue(experimentalEnvironment.highlightStability(COMMUNITY, PREVIEW, EXPERIMENTAL));

        assertTrue(experimentalEnvironment.highlightStability(COMMUNITY, EXPERIMENTAL));
        assertFalse(experimentalEnvironment.highlightStability(COMMUNITY, EXPERIMENTAL, DEFAULT));
        assertFalse(experimentalEnvironment.highlightStability(COMMUNITY, EXPERIMENTAL, COMMUNITY));
        assertFalse(experimentalEnvironment.highlightStability(COMMUNITY, EXPERIMENTAL, PREVIEW));
        assertTrue(experimentalEnvironment.highlightStability(COMMUNITY, EXPERIMENTAL, EXPERIMENTAL));

        assertFalse(experimentalEnvironment.highlightStability(PREVIEW, DEFAULT));
        assertFalse(experimentalEnvironment.highlightStability(PREVIEW, DEFAULT, DEFAULT));
        assertFalse(experimentalEnvironment.highlightStability(PREVIEW, DEFAULT, COMMUNITY));
        assertFalse(experimentalEnvironment.highlightStability(PREVIEW, DEFAULT, PREVIEW));
        assertTrue(experimentalEnvironment.highlightStability(PREVIEW, DEFAULT, EXPERIMENTAL));

        assertFalse(experimentalEnvironment.highlightStability(PREVIEW, COMMUNITY));
        assertFalse(experimentalEnvironment.highlightStability(PREVIEW, COMMUNITY, DEFAULT));
        assertFalse(experimentalEnvironment.highlightStability(PREVIEW, COMMUNITY, COMMUNITY));
        assertFalse(experimentalEnvironment.highlightStability(PREVIEW, COMMUNITY, PREVIEW));
        assertTrue(experimentalEnvironment.highlightStability(PREVIEW, COMMUNITY, EXPERIMENTAL));

        assertFalse(experimentalEnvironment.highlightStability(PREVIEW, PREVIEW));
        assertFalse(experimentalEnvironment.highlightStability(PREVIEW, PREVIEW, DEFAULT));
        assertFalse(experimentalEnvironment.highlightStability(PREVIEW, PREVIEW, COMMUNITY));
        assertFalse(experimentalEnvironment.highlightStability(PREVIEW, PREVIEW, PREVIEW));
        assertTrue(experimentalEnvironment.highlightStability(PREVIEW, PREVIEW, EXPERIMENTAL));

        assertTrue(experimentalEnvironment.highlightStability(PREVIEW, EXPERIMENTAL));
        assertFalse(experimentalEnvironment.highlightStability(PREVIEW, EXPERIMENTAL, DEFAULT));
        assertFalse(experimentalEnvironment.highlightStability(PREVIEW, EXPERIMENTAL, COMMUNITY));
        assertFalse(experimentalEnvironment.highlightStability(PREVIEW, EXPERIMENTAL, PREVIEW));
        assertTrue(experimentalEnvironment.highlightStability(PREVIEW, EXPERIMENTAL, EXPERIMENTAL));

        assertFalse(experimentalEnvironment.highlightStability(EXPERIMENTAL, DEFAULT));
        assertFalse(experimentalEnvironment.highlightStability(EXPERIMENTAL, DEFAULT, DEFAULT));
        assertFalse(experimentalEnvironment.highlightStability(EXPERIMENTAL, DEFAULT, COMMUNITY));
        assertFalse(experimentalEnvironment.highlightStability(EXPERIMENTAL, DEFAULT, PREVIEW));
        assertTrue(experimentalEnvironment.highlightStability(EXPERIMENTAL, DEFAULT, EXPERIMENTAL));

        assertFalse(experimentalEnvironment.highlightStability(EXPERIMENTAL, COMMUNITY));
        assertFalse(experimentalEnvironment.highlightStability(EXPERIMENTAL, COMMUNITY, DEFAULT));
        assertFalse(experimentalEnvironment.highlightStability(EXPERIMENTAL, COMMUNITY, COMMUNITY));
        assertFalse(experimentalEnvironment.highlightStability(EXPERIMENTAL, COMMUNITY, PREVIEW));
        assertTrue(experimentalEnvironment.highlightStability(EXPERIMENTAL, COMMUNITY, EXPERIMENTAL));

        assertFalse(experimentalEnvironment.highlightStability(EXPERIMENTAL, PREVIEW));
        assertFalse(experimentalEnvironment.highlightStability(EXPERIMENTAL, PREVIEW, DEFAULT));
        assertFalse(experimentalEnvironment.highlightStability(EXPERIMENTAL, PREVIEW, COMMUNITY));
        assertFalse(experimentalEnvironment.highlightStability(EXPERIMENTAL, PREVIEW, PREVIEW));
        assertTrue(experimentalEnvironment.highlightStability(EXPERIMENTAL, PREVIEW, EXPERIMENTAL));

        assertTrue(experimentalEnvironment.highlightStability(EXPERIMENTAL, EXPERIMENTAL));
        assertFalse(experimentalEnvironment.highlightStability(EXPERIMENTAL, EXPERIMENTAL, DEFAULT));
        assertFalse(experimentalEnvironment.highlightStability(EXPERIMENTAL, EXPERIMENTAL, COMMUNITY));
        assertFalse(experimentalEnvironment.highlightStability(EXPERIMENTAL, EXPERIMENTAL, PREVIEW));
        assertTrue(experimentalEnvironment.highlightStability(EXPERIMENTAL, EXPERIMENTAL, EXPERIMENTAL));
    }

    private Environment environment(Stability builtinStability, Stability serverStability) {
        Environment environment = new Environment(
                "hal-test",
                "HAL Test Console",
                Version.parseVersion("1.0.0"), "/",
                BuildType.DEVELOPMENT,
                builtinStability);

        int length = Stability.values().length;
        Stability[] permissibleStabilityLevels = new Stability[length];
        arraycopy(Stability.values(), 0, permissibleStabilityLevels, 0, length);
        environment.update(serverStability, permissibleStabilityLevels);
        return environment;
    }
}