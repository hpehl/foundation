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
package org.jboss.hal.meta.tree;

/**
 * Enum representing the types of resources to be included during a traversal of a management model tree. The types determine
 * the nature of the resources to be traversed, allowing for filtering based on specific criteria.
 */
public enum TraverseType {

    /**
     * Represents a specific type of resource traversal where the traversal includes singleton resources that do not yet exist.
     */
    NON_EXISTING_SINGLETONS,

    /**
     * Represents a type of resource traversal where the traversal includes wildcard resources like
     * {@code /subsystem=datasources/data-source=*}.
     */
    WILDCARD_RESOURCES,
}
