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

import java.util.List;

import org.jboss.hal.model.filter.Filter;
import org.jboss.hal.model.filter.FilterAttribute;

import static org.jboss.hal.model.filter.FilterCondition.booleanEquals;
import static org.jboss.hal.model.filter.FilterCondition.containsIgnoreCase;
import static org.jboss.hal.model.filter.FilterCondition.equalsIgnoreCase;

public class ResourceFilter extends Filter {

    public static final String NAME = "filterName";
    public static final String UNDEFINED = "filterUndefined";
    public static final String STORAGE = "filterStorage";
    public static final String ACCESS_TYPE = "filterAccessType";

    ResourceFilter() {
        super(List.of(
                new FilterAttribute(NAME, "", containsIgnoreCase()),
                new FilterAttribute(UNDEFINED, "", booleanEquals()),
                new FilterAttribute(STORAGE, "", equalsIgnoreCase()),
                new FilterAttribute(ACCESS_TYPE, "", equalsIgnoreCase())
        ));
    }
}
