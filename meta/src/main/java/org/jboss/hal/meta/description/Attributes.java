package org.jboss.hal.meta.description;

import java.util.Iterator;
import java.util.LinkedHashMap;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

/** Wrapper around the attributes of a {@link ResourceDescription} */
public class Attributes implements Iterable<Attribute> {

    private final LinkedHashMap<String, Attribute> attributes;

    public Attributes(ModelNode modelNode) {
        this.attributes = modelNode.asPropertyList()
                .stream()
                .map(property -> new Attribute(this, property))
                .collect(toMap(NamedNode::name, identity(), (existing, replacement) -> replacement, LinkedHashMap::new));
    }

    @Override
    public Iterator<Attribute> iterator() {
        return attributes.values().iterator();
    }

    public Attribute get(String name) {
        return attributes.get(name);
    }
}
