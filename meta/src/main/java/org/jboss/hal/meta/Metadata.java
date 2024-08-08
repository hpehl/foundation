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

public class Metadata {

    public static Metadata empty() {
        return new Metadata(AddressTemplate.root(),
                new ResourceDescription(new ModelNode()),
                new SecurityContext(new ModelNode()));
    }

    public static Metadata metadata(AddressTemplate template,
            ResourceDescription resourceDescription,
            SecurityContext securityContext) {
        return new Metadata(template, resourceDescription, securityContext);
    }

    public final boolean empty;
    /**
     * The template that was used in {@link MetadataRepository#lookup(AddressTemplate)}
     */
    public final AddressTemplate template;
    public final ResourceDescription resourceDescription;
    public final SecurityContext securityContext;

    Metadata(AddressTemplate template, ResourceDescription resourceDescription, SecurityContext securityContext) {
        this.template = template;
        this.resourceDescription = resourceDescription;
        this.securityContext = securityContext;
        this.empty = !this.resourceDescription.isDefined() && !this.securityContext.isDefined();
    }
}
