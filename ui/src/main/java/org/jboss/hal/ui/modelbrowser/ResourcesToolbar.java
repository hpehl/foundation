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

import org.jboss.elemento.By;
import org.jboss.elemento.Id;
import org.jboss.elemento.IsElement;
import org.patternfly.component.toolbar.Toolbar;
import org.patternfly.component.toolbar.ToolbarItem;
import org.patternfly.core.ObservableValue;
import org.patternfly.filter.Filter;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.setVisible;
import static org.jboss.hal.ui.filter.ItemCount.itemCount;
import static org.jboss.hal.ui.filter.NameFilterTextInputGroup.nameFilterTextInputGroup;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.toolbar.Toolbar.toolbar;
import static org.patternfly.component.toolbar.ToolbarContent.toolbarContent;
import static org.patternfly.component.toolbar.ToolbarGroup.toolbarGroup;
import static org.patternfly.component.toolbar.ToolbarGroupType.iconButtonGroup;
import static org.patternfly.component.toolbar.ToolbarItem.toolbarItem;
import static org.patternfly.component.toolbar.ToolbarItemType.searchFilter;
import static org.patternfly.component.tooltip.Tooltip.tooltip;
import static org.patternfly.icon.IconSets.fas.plus;
import static org.patternfly.icon.IconSets.fas.sync;
import static org.patternfly.popper.Placement.auto;
import static org.patternfly.style.Classes.modifier;

class ResourcesToolbar implements IsElement<HTMLElement> {

    // ------------------------------------------------------ factory

    static ResourcesToolbar resourcesToolbar(ResourcesElement resourcesElement, Filter<ModelBrowserNode> filter,
            ObservableValue<Integer> visible, ObservableValue<Integer> total) {
        return new ResourcesToolbar(resourcesElement, filter, visible, total);
    }

    // ------------------------------------------------------ instance

    private final Toolbar toolbar;
    private final ToolbarItem addItem;

    private ResourcesToolbar(ResourcesElement resourcesElement, Filter<ModelBrowserNode> filter,
            ObservableValue<Integer> visible, ObservableValue<Integer> total) {
        // TODO RBAC
        String addId = Id.unique("add");
        addItem = toolbarItem()
                .add(button().id(addId).plain().icon(plus()).onClick((e, b) -> resourcesElement.add()))
                .add(tooltip(By.id(addId), "Add").placement(auto));
        String refreshId = Id.unique("refresh");
        ToolbarItem refreshItem = toolbarItem()
                .add(button().id(refreshId).plain().icon(sync()).onClick((e, b) -> resourcesElement.refresh()))
                .add(tooltip(By.id(refreshId), "Refresh").placement(auto));
        toolbar = toolbar()
                .addContent(toolbarContent()
                        .addItem(toolbarItem(searchFilter).add(nameFilterTextInputGroup(filter)))
                        .addItem(toolbarItem()
                                .style("align-self", "center")
                                .add(itemCount(visible, total, "resource", "resources")))
                        .addGroup(toolbarGroup(iconButtonGroup).css(modifier("align-right"))
                                .addItem(addItem)
                                .addItem(refreshItem)));
        setVisible(addItem, false);
    }

    void toggleAddButton(boolean supportsAdd) {
        setVisible(addItem, supportsAdd);
    }

    @Override
    public HTMLElement element() {
        return toolbar.element();
    }
}
