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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ResourceAddress;

import static java.util.Collections.emptyList;
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
 * To get a fully qualified address from an address template use the method {@link #resolve(StatementContext)}.
 */
public final class AddressTemplate implements Iterable<Segment> {

    // ------------------------------------------------------ factory

    /** Creates a new address template from an encoded string template. */
    public static AddressTemplate root() {
        return new AddressTemplate(emptyList());
    }


    /** Creates a new address template from an encoded string template. */
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

    /** Creates a new address template from a list of segments. */
    public static AddressTemplate of(List<Segment> segments) {
        if (segments != null) {
            return new AddressTemplate(segments);
        } else {
            return new AddressTemplate(emptyList());
        }
    }

    // ------------------------------------------------------ instance

    private static final String[][] SPECIAL_CHARACTERS = new String[][]{
            new String[]{"/", "\\/"},
            new String[]{":", "\\:"},
            new String[]{"=", "\\="},
    };
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

    /** @return the string representation of this address template */
    @Override
    public String toString() {
        return template.isEmpty() ? "/" : template;
    }

    // ------------------------------------------------------ append / sub and parent

    /**
     * Appends the specified encoded template to this template and returns a new template. If the specified template does not
     * start with a slash, '/' is automatically appended.
     *
     * @param template the encoded template to append (makes no difference whether it starts with '/' or not)
     * @return a new template
     */
    public AddressTemplate append(String template) {
        String slashTemplate = template.startsWith("/") ? template : "/" + template;
        return AddressTemplate.of(this.template + slashTemplate);
    }

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
        return AddressTemplate.of(join(subSegments));
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

    public ResourceAddress resolve(TemplateResolver resolver) {
        if (isEmpty()) {
            return ResourceAddress.root();
        } else {
            ModelNode model = new ModelNode();
            AddressTemplate resolved = resolver.resolve(this);
            for (Segment segment : resolved) {
                if (segment.containsPlaceholder()) {
                    throw new ResolveException("Unable to resolve segment '" + segment + "' in template '" + this + "'");
                }
                model.add(segment.key, decodeValue(segment.value));
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

            StringTokenizer tok = new StringTokenizer(withSlash);
            while (tok.hasMoreTokens()) {
                String nextToken = tok.nextToken();
                int index = nextToken.indexOf('=');
                if (index != -1 && !escaped(nextToken, index)) {
                    String key = nextToken.substring(0, index);
                    String value = nextToken.substring(index + 1);
                    segments.add(new Segment(key, value));
                } else {
                    segments.add(new Segment(nextToken));
                }
            }
        }
        return segments;
    }

    private static String join(List<Segment> segments) {
        return segments.stream().map(Segment::toString).collect(joining("/"));
    }

    static String encodeValue(String value) {
        boolean encode = false;
        if (value != null) {
            for (String[] specialCharacter : SPECIAL_CHARACTERS) {
                if (value.contains(specialCharacter[0])) {
                    encode = true;
                    break;
                }
            }
            if (encode) {
                String localValue = value;
                for (String[] special : SPECIAL_CHARACTERS) {
                    localValue = localValue.replace(special[0], special[1]);
                }
                return localValue;
            }
        }
        return value;
    }

    private static String decodeValue(String value) {
        boolean decode = false;
        if (value != null) {
            for (String[] specialCharacter : SPECIAL_CHARACTERS) {
                if (value.contains(specialCharacter[1])) {
                    decode = true;
                    break;
                }
            }
            if (decode) {
                String localValue = value;
                for (String[] special : SPECIAL_CHARACTERS) {
                    localValue = localValue.replace(special[1], special[0]);
                }
                return localValue;
            }
        }
        return value;
    }

    private static boolean escaped(String string, int index) {
        if (index > 0 && index < string.length()) {
            return string.charAt(index - 1) == '\\';
        }
        return false;
    }

    private static class StringTokenizer {

        private final String delim;
        private final String s;
        private final int len;

        private int pos;
        private String next;

        StringTokenizer(String s) {
            this.s = s;
            this.delim = "/";
            len = s.length();
        }

        String nextToken() {
            if (!hasMoreTokens()) {
                throw new NoSuchElementException();
            }
            String result = next;
            next = null;
            return result;
        }

        boolean hasMoreTokens() {
            if (next != null) {
                return true;
            }
            // skip leading delimiters
            while (pos < len) {
                char ch = s.charAt(pos);
                if (delim.indexOf(ch) == -1) {break;}
                pos++;
            }

            if (pos >= len) {
                return false;
            }

            int index = pos++;
            while (pos < len) {
                char ch = s.charAt(pos);
                if (delim.indexOf(ch) != -1) {break;}
                pos++;
            }

            next = s.substring(index, pos++);
            return true;
        }
    }
}
