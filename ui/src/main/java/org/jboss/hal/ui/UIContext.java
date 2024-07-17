package org.jboss.hal.ui;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.MetadataLookup;
import org.jboss.hal.meta.StatementContext;

/** Holds common classes needed by UI elements. Use this class to keep the method signatures clean and tidy. */
@ApplicationScoped
public class UIContext {

    public final Dispatcher dispatcher;
    public final MetadataLookup metadataLookup;
    public final StatementContext statementContext;

    @Inject
    public UIContext(Dispatcher dispatcher, MetadataLookup metadataLookup, StatementContext statementContext) {
        this.dispatcher = dispatcher;
        this.metadataLookup = metadataLookup;
        this.statementContext = statementContext;
    }
}
