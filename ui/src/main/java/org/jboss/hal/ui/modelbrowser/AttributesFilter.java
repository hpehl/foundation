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

import java.util.List;

import org.jboss.hal.dmr.ModelType;
import org.jboss.hal.meta.description.AttributeDescription;
import org.jboss.hal.model.filter.Filter;
import org.jboss.hal.model.filter.FilterValue;

import static java.util.Collections.emptyList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ACCESS_TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.STORAGE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TYPE;

class AttributesFilter extends Filter<AttributeDescription> {

    static final String NAME_FILTER = "name-filter";
    static final String TYPE_FILTER = "type-filter";
    static final String DEPRECATED_FILTER = "deprecated-filter";
    static final String STORAGE_FILTER = "storage-filter";
    static final String ACCESS_TYPE_FILTER = "access-type-filter";

    AttributesFilter() {
        add(new FilterValue<>(NAME_FILTER, "", (ad, name) -> ad.name().toLowerCase().contains(name.toLowerCase())));
        add(new FilterValue<AttributeDescription, List<ModelType>>(TYPE_FILTER, emptyList(), (ad, types) -> {
            if (!types.isEmpty()) {
                ModelType attributeType = ad.get(TYPE).asType();
                return types.contains(attributeType);
            }
            return true;
        }));
        add(new FilterValue<>(DEPRECATED_FILTER, (Boolean) null,
                (ad, deprecated) -> deprecated == null || deprecated == ad.deprecation().isDefined()));
        add(new FilterValue<>(STORAGE_FILTER, "", (ad, storage) -> storage.equals(ad.get(STORAGE).asString())));
        add(new FilterValue<>(ACCESS_TYPE_FILTER, "", (ad, accessType) -> accessType.equals(ad.get(ACCESS_TYPE).asString())));
    }
}
