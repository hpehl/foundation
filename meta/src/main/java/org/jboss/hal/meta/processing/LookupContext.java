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
package org.jboss.hal.meta.processing;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jboss.elemento.flow.FlowContext;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.description.ResourceDescription;
import org.jboss.hal.meta.security.SecurityContext;

class LookupContext extends FlowContext {

    final boolean recursive;
    final LookupResult lookupResult;
    final Map<ResourceAddress, ResourceDescription> toResourceDescription;
    final Map<ResourceAddress, SecurityContext> toSecurityContext;

    // for unit testing only!
    LookupContext(LookupResult lookupResult) {
        this.recursive = false;
        this.lookupResult = lookupResult;
        this.toResourceDescription = new HashMap<>();
        this.toSecurityContext = new HashMap<>();
    }

    LookupContext(Set<AddressTemplate> template, boolean recursive) {
        this.recursive = recursive;
        this.lookupResult = new LookupResult(template);
        this.toResourceDescription = new HashMap<>();
        this.toSecurityContext = new HashMap<>();
    }

    boolean shouldUpdate() {
        return !toResourceDescription.isEmpty() || !toSecurityContext.isEmpty();
    }
}
