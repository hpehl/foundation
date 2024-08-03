package org.jboss.hal.meta.security;

import org.jboss.hal.meta.AddressTemplate;
import org.junit.jupiter.api.Test;

import static org.jboss.hal.meta.StatementContextFactory.domainStatementContext;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SecurityContextResolverTest {

    @Test
    public void serverGroup() {
        String[][] fixtures = new String[][]{
                new String[]{"{domain.controller}", "host=primary"},
                new String[]{"{selected.host}", "host=secondary"},
                new String[]{"{selected.host}/{selected.server}", "host=secondary/server=server1"},
                new String[]{"{selected.host}/{selected.server-config}", "host=secondary/server-config=*"},
                new String[]{"{selected.profile}", "profile=*"},
                new String[]{"{selected.server-group}", "server-group=main-server-group"},
                new String[]{"{selected.deployment}", "deployment=*"},
                new String[]{"subsystem=logging/console-handler={selected.resource}", "subsystem=logging/console-handler=*"},

                new String[]{"{selected.server-group}/jvm=*", "server-group=*/jvm=*"},
                new String[]{"{selected.server-group}/jvm=jvm1", "server-group=*/jvm=jvm1"},
                new String[]{"server-group=*", "server-group=*"},
                new String[]{"server-group=*/jvm=*", "server-group=*/jvm=*"},
                new String[]{"server-group=*/jvm=jvm1", "server-group=*/jvm=jvm1"},
                new String[]{"server-group=main-server-group", "server-group=*"},
                new String[]{"server-group=main-server-group/jvm=*", "server-group=*/jvm=*"},
                new String[]{"server-group=main-server-group/jvm=jvm1", "server-group=*/jvm=jvm1"},
        };
        SecurityContextResolver resolver = new SecurityContextResolver(domainStatementContext());
        for (int i = 0; i < fixtures.length; i++) {
            String[] fixture = fixtures[i];
            assertEquals(fixture[1], resolver.resolve(AddressTemplate.of(fixture[0])).template,
                    String.format("fixtures[%d]", i));
        }
    }
}