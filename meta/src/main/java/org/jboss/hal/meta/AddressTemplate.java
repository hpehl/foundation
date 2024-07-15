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
package org.jboss.hal.meta;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.ValueEncoder;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.joining;

/**
 * Template for a DMR address which can contain variable parts.
 * <p>
 * An address template can be defined using the following EBNF:
 * <pre>
 * AddressTemplate = "/" | Segment ;
 * Segment         = Tuple | Segment "/" Tuple ;
 * Tuple           = Placeholder | Key "=" Value ;
 * Placeholder     = "{" Alphanumeric "}" ;
 * Key             = Alphanumeric ;
 * Value           = Placeholder | Alphanumeric | "*" ;
 * </pre>
 * <p>
 * Examples for valid address templates are
 * <pre>
 * /
 * subsystem=io
 * {selected.server}
 * {selected.server}/deployment=foo
 * subsystem=logging/logger={selection}
 * </pre>
 * <p>
 * <strong>Resolving</strong><br/>
 * To get a fully qualified {@link ResourceAddress} from an address template use one of the <code>resolve()</code> methods and a
 * {@link TemplateResolver}. In general, you prefer address templates over {@linkplain ResourceAddress resource addresses}.
 * <p>
 * <strong>Encoding</strong><br/>
 * Some characters in values must be encoded using the backslash character:
 * <ul>
 *     <li><code>/</code> → <code>\/</code></li>
 *     <li><code>=</code> → <code>\=</code></li>
 *     <li><code>:</code> → <code>\:</code></li>
 * </ul>
 * When creating a template from a {@linkplain AddressTemplate#of(String) string},
 * or {@link AddressTemplate#append(String) appending} a string, you must take care of the encoding.
 * If you create a template from {@linkplain AddressTemplate#of(List) segments}, the encoding is done for you.
 */
public final class AddressTemplate implements Iterable<Segment> {

    // ------------------------------------------------------ factory

    /**
     * Creates a new root address template, which represents the empty address.
     */
    public static AddressTemplate root() {
        return new AddressTemplate(emptyList());
    }

    /**
     * Creates a new address template from an <strong>encoded</strong> string template. Special characters in the template must
     * be encoded.
     */
    public static AddressTemplate of(String template) {
        return new AddressTemplate(parse(template));
    }

    /** Creates a new address template from a placeholder. */
    public static AddressTemplate of(Placeholder placeholder) {
        if (placeholder != null) {
            return new AddressTemplate(parse("/" + placeholder.expression()));
        } else {
            return new AddressTemplate(emptyList());
        }
    }

    /**
     * Creates a new address template from a list of segments. Special characters in segment values must not be encoded.
     */
    public static AddressTemplate of(List<Segment> segments) {
        if (segments != null) {
            return new AddressTemplate(segments);
        } else {
            return new AddressTemplate(emptyList());
        }
    }

    // ------------------------------------------------------ instance

    /** The string representation of this address template. If the template contains special characters, they're encoded. */
    public final String template;
    private final LinkedList<Segment> segments;

    private AddressTemplate(List<Segment> segments) {
        this.segments = new LinkedList<>(segments);
        this.template = join(this.segments);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AddressTemplate)) {
            return false;
        }

        AddressTemplate that = (AddressTemplate) o;
        return template.equals(that.template);

    }

    @Override
    public int hashCode() {
        int result = template.hashCode();
        result = 31 * result;
        return result;
    }

    /**
     * @return the string representation of this address template. If the template contains special characters, they're encoded.
     */
    @Override
    public String toString() {
        return template.isEmpty() ? "/" : template;
    }

    // ------------------------------------------------------ append / sub and parent

    /**
     * Appends the specified <strong>encoded</strong> template to this template and returns a new template. Special characters
     * in the value must not be be encoded.
     *
     * @param key   the key to append to the template
     * @param value the value to append to the template
     * @return a new template
     */
    public AddressTemplate append(String key, String value) {
        return append(key + "=" + ValueEncoder.encode(value));
    }

    /**
     * Appends the specified <strong>encoded</strong> template to this template and returns a new template. Special characters
     * in the template must be encoded. If the specified template does not start with a slash, '/' is automatically appended.
     *
     * @param template the <strong>encoded</strong> template to append (makes no difference whether it starts with '/' or not)
     * @return a new template
     */
    public AddressTemplate append(String template) {
        String slashTemplate = template.startsWith("/") ? template : "/" + template;
        return AddressTemplate.of(this.template + slashTemplate);
    }

    /**
     * Appends the specified template to this template and returns a new template.
     *
     * @return a new template
     */
    public AddressTemplate append(AddressTemplate template) {
        return append(template.toString());
    }

    /**
     * Works like {@link List#subList(int, int)} over the tokens of this template and throws the same exceptions.
     *
     * @param fromIndex low endpoint (inclusive) of the sub template
     * @param toIndex   high endpoint (exclusive) of the sub template
     * @return a new address template containing the specified tokens.
     * @throws IndexOutOfBoundsException for an illegal endpoint index value (<tt>fromIndex &lt; 0 || toIndex &gt; size ||
     *                                   fromIndex &gt; toIndex</tt>)
     */
    public AddressTemplate subTemplate(int fromIndex, int toIndex) {
        LinkedList<Segment> subSegments = new LinkedList<>(this.segments.subList(fromIndex, toIndex));
        return new AddressTemplate(subSegments);
    }

    /** @return the parent address template or the root template */
    public AddressTemplate parent() {
        if (isEmpty() || size() == 1) {
            return AddressTemplate.of("/");
        } else {
            return subTemplate(0, size() - 1);
        }
    }

    // ------------------------------------------------------ properties

    /** @return the first segment or null if this address template is empty. */
    public Segment first() {
        if (!segments.isEmpty()) {
            return segments.getFirst();
        }
        return null;
    }

    /** @return the last segment or null if this address template is empty. */
    public Segment last() {
        if (!segments.isEmpty()) {
            return segments.getLast();
        }
        return null;
    }

    /** @return true if this template contains no tokens, false otherwise */
    public boolean isEmpty() {
        return segments.isEmpty();
    }

    /** @return the number of tokens */
    public int size() {
        return segments.size();
    }

    public List<Segment> segments() {
        return unmodifiableList(segments);
    }

    @Override
    public Iterator<Segment> iterator() {
        return segments.iterator();
    }

    // ------------------------------------------------------ resolve

    /** Resolve this template using the {@link NoopResolver} */
    public ResourceAddress resolve() {
        return resolve(new NoopResolver());
    }

    /** Resolve this template using the {@link WildcardResolver} */
    public ResourceAddress resolve(String first, String... more) {
        return resolve(new WildcardResolver(first, more));
    }

    /** Resolve this template using the {@link StatementContextResolver} */
    public ResourceAddress resolve(StatementContext context) {
        return resolve(new StatementContextResolver(context));
    }

    /**
     * Resolves the given template using the provided resolver.
     *
     * @param resolver the resolver used to resolve the template
     * @return the resolved ResourceAddress
     */
    public ResourceAddress resolve(TemplateResolver resolver) {
        if (isEmpty()) {
            return ResourceAddress.root();
        } else {
            ModelNode model = new ModelNode();
            AddressTemplate resolved = resolver.resolve(this);
            for (Segment segment : resolved) {
                // Do *not* encode values: the model node will be encoded as DMR!
                model.add(segment.key, segment.value);
            }
            return new ResourceAddress(model);
        }
    }

    // ------------------------------------------------------ internal

    private static LinkedList<Segment> parse(String template) {
        LinkedList<Segment> segments = new LinkedList<>();

        if (template != null && !template.trim().isEmpty()) {
            String trimmed = template.trim();
            String withSlash = trimmed.startsWith("/") ? trimmed : "/" + trimmed;
            if (withSlash.equals("/")) {
                return segments;
            }

            // split template by '/'
            int current = 0;
            boolean backslash = false;
            List<String> unparsedSegments = new ArrayList<>();
            String withoutSlash = withSlash.substring(1);
            for (int i = 0; i < withoutSlash.length(); i++) {
                char c = withoutSlash.charAt(i);
                if (c == '\\') {
                    backslash = true;
                } else if (c == '/') {
                    if (!backslash) {
                        unparsedSegments.add(withoutSlash.substring(current, i));
                        current = i + 1;
                    }
                    backslash = false;
                } else {
                    backslash = false;
                }
            }
            unparsedSegments.add(withoutSlash.substring(current));

            // split segments by '='
            for (String unparsedSegment : unparsedSegments) {
                String key = null;
                String value;
                backslash = false;
                for (int i = 0; i < unparsedSegment.length(); i++) {
                    char c = unparsedSegment.charAt(i);
                    if (c == '\\') {
                        backslash = true;
                    } else if (c == '=') {
                        if (!backslash) {
                            key = unparsedSegment.substring(0, i);
                            value = unparsedSegment.substring(i + 1);
                            segments.add(new Segment(key, ValueEncoder.decode(value)));
                        }
                        backslash = false;
                    } else {
                        backslash = false;
                    }
                }
                if (key == null) {
                    segments.add(new Segment(unparsedSegment));
                }
            }
        }
        return segments;
    }

    private static String join(List<Segment> segments) {
        return segments.stream().map(Segment::toString).collect(joining("/"));
    }
}
