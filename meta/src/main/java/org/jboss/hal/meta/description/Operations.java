package org.jboss.hal.meta.description;

import java.util.Iterator;
import java.util.LinkedHashMap;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

/** Wrapper around the operations of a {@link ResourceDescription} */
public class Operations implements Iterable<Operation> {

    private final LinkedHashMap<String, Operation> operations;

    public Operations(ModelNode modelNode) {
        this.operations = modelNode.asPropertyList()
                .stream()
                .map(property -> new Operation(this, property))
                .collect(toMap(NamedNode::name, identity(), (existing, replacement) -> replacement, LinkedHashMap::new));
    }

    @Override
    public Iterator<Operation> iterator() {
        return operations.values().iterator();
    }

    public Operation get(String name) {
        return operations.get(name);
    }
}
