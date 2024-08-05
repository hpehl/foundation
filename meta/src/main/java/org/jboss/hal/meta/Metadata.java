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
package org.jboss.hal.meta;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.description.ResourceDescription;
import org.jboss.hal.meta.security.SecurityContext;

import static org.jboss.hal.dmr.ModelDescriptionConstants.HAL_RECURSIVE;

public class Metadata {

    public static Metadata empty() {
        return new Metadata(new ResourceDescription(new ModelNode()), new SecurityContext(new ModelNode()));
    }

    public final boolean empty;
    public final boolean recursive;
    public final ResourceDescription resourceDescription;
    public final SecurityContext securityContext;

    public Metadata(ResourceDescription resourceDescription, SecurityContext securityContext) {
        this.resourceDescription = resourceDescription == null ? new ResourceDescription(new ModelNode()) : resourceDescription;
        this.securityContext = securityContext == null ? new SecurityContext(new ModelNode()) : securityContext;
        this.recursive = this.resourceDescription.get(HAL_RECURSIVE).asBoolean(false) &&
                this.securityContext.get(HAL_RECURSIVE).asBoolean(false);
        this.empty = !this.resourceDescription.isDefined() && !this.securityContext.isDefined();
    }
}
