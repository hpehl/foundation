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

import static org.jboss.hal.dmr.ModelDescriptionConstants.RESOURCE_DESCRIPTION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SECURITY_CONTEXT;

public class Metadata extends ModelNode {

    public static Metadata undefined() {
        return new Metadata();
    }

    public static Metadata metadata(ResourceDescription resourceDescription, SecurityContext securityContext) {
        return new Metadata(resourceDescription, securityContext);
    }

    private final ResourceDescription resourceDescription;
    private final SecurityContext securityContext;

    Metadata() {
        super();
        this.resourceDescription = new ResourceDescription();
        this.securityContext = new SecurityContext();
    }

    Metadata(ResourceDescription resourceDescription, SecurityContext securityContext) {
        super();
        this.resourceDescription = resourceDescription;
        this.securityContext = securityContext;
        get(RESOURCE_DESCRIPTION).set(resourceDescription);
        get(SECURITY_CONTEXT).set(securityContext);
    }

    public ResourceDescription resourceDescription() {
        return resourceDescription;
    }

    public SecurityContext securityContext() {
        return securityContext;
    }
}
