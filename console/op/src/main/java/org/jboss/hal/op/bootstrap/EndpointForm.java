package org.jboss.hal.op.bootstrap;

import org.jboss.elemento.IsElement;
import org.patternfly.component.form.Form;
import org.patternfly.component.form.FormGroupControl;
import org.patternfly.component.form.TextInput;
import org.patternfly.component.help.HelperText;
import org.patternfly.component.inputgroup.InputGroupText;
import org.patternfly.component.switch_.Switch;
import org.patternfly.core.ObservableValue;
import org.patternfly.icon.IconSets;

import elemental2.dom.HTMLElement;
import elemental2.dom.URL;

import static org.patternfly.component.ValidationStatus.default_;
import static org.patternfly.component.ValidationStatus.error;
import static org.patternfly.component.ValidationStatus.success;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.form.Form.form;
import static org.patternfly.component.form.FormGroup.formGroup;
import static org.patternfly.component.form.FormGroupControl.formGroupControl;
import static org.patternfly.component.form.FormGroupLabel.formGroupLabel;
import static org.patternfly.component.form.TextInput.textInput;
import static org.patternfly.component.form.TextInputType.number;
import static org.patternfly.component.help.HelperText.helperText;
import static org.patternfly.component.inputgroup.InputGroup.inputGroup;
import static org.patternfly.component.inputgroup.InputGroupItem.inputGroupItem;
import static org.patternfly.component.inputgroup.InputGroupText.inputGroupText;
import static org.patternfly.component.switch_.Switch.switch_;
import static org.patternfly.core.ObservableValue.ov;
import static org.patternfly.icon.IconSets.fas.link;

class EndpointForm implements IsElement<HTMLElement> {

    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_PORT = "9990";
    @SuppressWarnings("HttpUrlsUsage")
    private static final String DEFAULT_URL = "http://" + DEFAULT_HOST + ":" + DEFAULT_PORT;

    private final Form form;
    private final FormGroupControl nameControl;
    private final TextInput nameInput;
    private final Switch secureSwitch;
    private final TextInput hostInput;
    private final TextInput portInput;
    private final FormGroupControl urlControl;
    private final TextInput urlInput;
    private final ObservableValue<String> url;

    EndpointForm() {
        this.form = form().horizontal()
                .addGroup(formGroup().fieldId("mi-name").required()
                        .addLabel(formGroupLabel("Name"))
                        .addControl(nameControl = formGroupControl()
                                .addControl(nameInput = textInput("mi-name"))))
                .addGroup(formGroup().fieldId("mi-protocol").required()
                        .addLabel(formGroupLabel("Protocol").noPaddingTop())
                        .addControl(formGroupControl()
                                .add(secureSwitch = switch_("mi-protocol", "mi-protocol", false)
                                        .label("HTTPS", "HTTP"))))
                .addGroup(formGroup().fieldId("mi-host").required()
                        .addLabel(formGroupLabel("Host"))
                        .addControl(formGroupControl()
                                .addControl(hostInput = textInput("mi-host")
                                        .placeholder(DEFAULT_HOST))))
                .addGroup(formGroup().fieldId("mi-port").required()
                        .addLabel(formGroupLabel("Port"))
                        .addControl(formGroupControl()
                                .addControl(portInput = textInput(number, "mi-port")
                                        .applyTo(input -> {
                                            input.min(0);
                                            input.max(65535);
                                        })
                                        .placeholder(DEFAULT_PORT))))
                .addGroup(formGroup().fieldId("mi-url")
                        .addLabel(formGroupLabel("URL"))
                        .addControl(urlControl = formGroupControl()
                                .add(inputGroup()
                                        .addText(inputGroupText().icon(link()))
                                        .addItem(inputGroupItem().fill()
                                                .addFormControl(urlInput = textInput("mi-url")
                                                        .readonly()))
                                        .addItem(inputGroupItem()
                                                .addButton(button("Ping").control()
                                                        .onClick((event, component) -> ping()))))));

        url = ov(DEFAULT_URL).subscribe((current, previous) -> urlInput.value(current));
        secureSwitch.onChange((event, component, value) -> url.set(buildUrl(value, hostInput.value(), portInput.value())));
        hostInput.onChange((event, component, value) -> url.set(buildUrl(secureSwitch.value(), value, portInput.value())));
        portInput.onChange((event, component, value) -> url.set(buildUrl(secureSwitch.value(), hostInput.value(), value)));
    }

    @Override
    public HTMLElement element() {
        return form.element();
    }

    boolean isValid() {
        nameControl.removeHelperText();
        if (nameInput.value() == null || nameInput.value().isEmpty()) {
            nameControl.addHelperText(helperText("Must not be empty", error));
            nameInput.validated(error);
            return false;
        } else {
            nameInput.validated(default_);
            return true;
        }
    }

    Endpoint endpoint() {
        return Endpoint.endpoint(nameInput.value(), url.get());
    }

    void reset() {
        nameControl.removeHelperText();
        nameInput.validated(default_);
        urlControl.removeHelperText();
        urlInput.validated(default_);
    }

    void show(Endpoint endpoint) {
        if (endpoint == null) {
            nameInput.value("", false);
            secureSwitch.value(false, false);
            hostInput.value("", false);
            portInput.value("", false);
            url.change(DEFAULT_URL);
        } else {
            URL u = new URL(endpoint.url);
            nameInput.value(endpoint.name, false);
            secureSwitch.value(u.protocol.equals("https:"), false);
            hostInput.value(u.hostname, false);
            portInput.value(u.port, false);
            url.change(endpoint.url);
        }
    }

    private String buildUrl(boolean secure, String host, String port) {
        StringBuilder builder = new StringBuilder();
        builder.append("http");
        if (secure) {
            builder.append("s");
        }
        builder.append("://");
        if (host == null || host.isEmpty()) {
            builder.append(DEFAULT_HOST);
        } else {
            builder.append(host);
        }
        builder.append(":");
        if (port == null || port.isEmpty()) {
            builder.append(DEFAULT_PORT);
        } else {
            builder.append(port);
        }
        return builder.toString();
    }

    private void ping() {
        String urlValue = url.get();
        String failSafeUrl = urlValue.endsWith("/") ? urlValue.substring(0, urlValue.length() - 1) : urlValue;
        Endpoint.ping(failSafeUrl)
                .then(valid -> {
                    if (valid) {
                        urlInput.validated(success);
                        urlControl.removeHelperText();
                    } else {
                        urlInput.validated(error);
                        urlControl.removeHelperText();
                        urlControl.addHelperText(helperText("Not a valid management interface", error));
                    }
                    return null;
                })
                .catch_(__ -> {
                    urlInput.validated(error);
                    urlControl.removeHelperText();
                    urlControl.addHelperText(helperText("Not a valid management interface", error));
                    return null;
                });
    }
}
