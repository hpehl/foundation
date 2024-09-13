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

import org.jboss.hal.ui.filter.AccessTypeFilterAttribute;
import org.jboss.hal.ui.filter.DefinedFilterAttribute;
import org.jboss.hal.ui.filter.DeprecatedFilterAttribute;
import org.jboss.hal.ui.filter.NameFilterAttribute;
import org.jboss.hal.ui.filter.StorageFilterAttribute;
import org.patternfly.filter.Filter;
import org.patternfly.filter.FilterOperator;

class ResourceFilter extends Filter<ResourceAttribute> {

    ResourceFilter() {
        super(FilterOperator.AND);
        add(new NameFilterAttribute<>(ra -> ra.name));
        add(new DefinedFilterAttribute<>(ra -> ra.value));
        add(new DeprecatedFilterAttribute<>(ra -> ra.description));
        add(new StorageFilterAttribute<>(ra -> ra.description));
        add(new AccessTypeFilterAttribute<>(ra -> ra.description));
    }
}
