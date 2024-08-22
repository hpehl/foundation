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
package org.jboss.hal.ui.resource;

import org.jboss.hal.model.filter.Filter;
import org.jboss.hal.model.filter.FilterValue;

import static org.jboss.hal.dmr.ModelDescriptionConstants.ACCESS_TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.STORAGE;

class ResourceFilter extends Filter<ResourceAttribute> {

    static final String NAME_FILTER = "name-filter";
    static final String UNDEFINED_FILTER = "undefined-filter";
    static final String DEPRECATED_FILTER = "deprecated-filter";
    static final String STORAGE_FILTER = "storage-filter";
    static final String ACCESS_TYPE_FILTER = "access-type-filter";

    ResourceFilter() {
        add(new FilterValue<>(NAME_FILTER, "",
                (ra, name) -> ra.name.toLowerCase().contains(name.toLowerCase())));
        add(new FilterValue<>(UNDEFINED_FILTER, (Boolean) null,
                (ra, defined) -> defined == null || defined == ra.value.isDefined()));
        add(new FilterValue<>(DEPRECATED_FILTER, (Boolean) null,
                (ra, deprecated) -> deprecated == null || deprecated == ra.description.deprecation().isDefined()));
        add(new FilterValue<>(STORAGE_FILTER, "",
                (ra, storage) -> storage.equals(ra.description.get(STORAGE).asString())));
        add(new FilterValue<>(ACCESS_TYPE_FILTER, "",
                (ra, accessType) -> accessType.equals(ra.description.get(ACCESS_TYPE).asString())));
    }
}
