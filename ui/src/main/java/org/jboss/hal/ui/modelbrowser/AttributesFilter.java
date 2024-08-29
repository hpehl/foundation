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

import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.meta.description.AttributeDescription;
import org.jboss.hal.meta.filter.AccessTypeFilterValue;
import org.jboss.hal.meta.filter.DeprecatedFilterValue;
import org.jboss.hal.meta.filter.Filter;
import org.jboss.hal.meta.filter.NameFilterValue;
import org.jboss.hal.meta.filter.StorageFilterValue;
import org.jboss.hal.meta.filter.TypesFilterValue;

public class AttributesFilter extends Filter<AttributeDescription> {

    public AttributesFilter() {
        add(new NameFilterValue<>(NamedNode::name));
        add(new TypesFilterValue<>(ad -> ad));
        add(new DeprecatedFilterValue<>(ad -> ad));
        add(new StorageFilterValue<>(ad -> ad));
        add(new AccessTypeFilterValue<>(ad -> ad));
    }
}
