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
import org.jboss.hal.meta.description.OperationDescription;
import org.jboss.hal.ui.filter.DeprecatedFilterAttribute;
import org.jboss.hal.ui.filter.GlobalOperationsFilterAttribute;
import org.jboss.hal.ui.filter.NameFilterAttribute;
import org.jboss.hal.ui.filter.ParametersFilterAttribute;
import org.jboss.hal.ui.filter.ReturnValueFilterAttribute;
import org.patternfly.filter.Filter;
import org.patternfly.filter.FilterOperator;

public class OperationsFilter extends Filter<OperationDescription> {

    public OperationsFilter() {
        super(FilterOperator.AND);
        add(new NameFilterAttribute<>(NamedNode::name));
        add(new ParametersFilterAttribute<>());
        add(new ReturnValueFilterAttribute<>());
        add(new DeprecatedFilterAttribute<>(od -> od));
        add(new GlobalOperationsFilterAttribute<>());
    }
}
