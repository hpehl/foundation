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
package org.jboss.hal.env;

import java.util.Arrays;

import jakarta.enterprise.context.ApplicationScoped;

import static org.jboss.hal.env.AccessControlProvider.SIMPLE;
import static org.jboss.hal.env.OperationMode.STANDALONE;
import static org.jboss.hal.env.Stability.COMMUNITY;
import static org.jboss.hal.env.Version.EMPTY_VERSION;

@ApplicationScoped
public class Environment {

    private final String applicationId;
    private final String applicationName;
    private final Version applicationVersion;
    private final String base;
    private final BuildType buildType;
    private final Stability builtInStability;
    private String instanceName;
    private String instanceOrganization;
    private String productName;
    private Version productVersion;
    private Version managementVersion;
    private OperationMode operationMode;
    private Stability stability;
    private Stability[] permissibleStabilityLevels;
    private AccessControlProvider accessControlProvider;
    private boolean sso;
    private String domainController;

    public Environment() {
        // final properties are defined as Closure defines in the POM
        this.applicationId = System.getProperty("environment.id");
        this.applicationName = System.getProperty("environment.name");
        this.applicationVersion = Version.parseVersion(System.getProperty("environment.version"));
        this.base = System.getProperty("environment.base");
        this.buildType = BuildType.parse(System.getProperty("environment.build"));
        this.builtInStability = Stability.parse(System.getProperty("environment.stability"));

        // default values for the non-final properties
        this.instanceName = "undefined";
        this.instanceOrganization = "undefined";
        this.productName = "undefined";
        this.productVersion = EMPTY_VERSION;
        this.managementVersion = EMPTY_VERSION;
        this.operationMode = STANDALONE;
        this.stability = COMMUNITY;
        this.permissibleStabilityLevels = new Stability[0];
        this.accessControlProvider = SIMPLE;
        this.sso = false;
        this.domainController = "undefined";
    }

    @Override
    public String toString() {
        return "Environment(" +
                "applicationId='" + applicationId + '\'' +
                ", applicationName='" + applicationName + '\'' +
                ", applicationVersion=" + applicationVersion +
                ", base='" + base + '\'' +
                ", buildType=" + buildType +
                ", instanceName='" + instanceName + '\'' +
                ", instanceOrganization='" + instanceOrganization + '\'' +
                ", productName='" + productName + '\'' +
                ", productVersion=" + productVersion +
                ", managementVersion=" + managementVersion +
                ", operationMode=" + operationMode +
                ", stability=" + stability +
                ", permissibleStabilityLevels=" + Arrays.toString(permissibleStabilityLevels) +
                ", accessControlProvider=" + accessControlProvider +
                ", sso=" + sso +
                ", domainController=" + domainController +
                ')';
    }

    // ------------------------------------------------------ update

    // update non-final properties as part of the bootstrap process
    public void update(String instanceName, String instanceOrganization, String productName, Version productVersion,
            Version managementVersion, OperationMode operationMode) {
        this.instanceName = instanceName;
        this.instanceOrganization = instanceOrganization;
        this.productName = productName;
        this.productVersion = productVersion;
        this.managementVersion = managementVersion;
        this.operationMode = operationMode;
    }

    public void update(String domainController) {
        this.domainController = domainController;
    }

    public void update(AccessControlProvider accessControlProvider, boolean sso) {
        this.accessControlProvider = accessControlProvider;
        this.sso = sso;
    }

    public void update(Stability stability, Stability[] permissibleStabilityLevels) {
        this.stability = stability;
        this.permissibleStabilityLevels = permissibleStabilityLevels;
    }

    // ------------------------------------------------------ getter

    public String applicationId() {
        return applicationId;
    }

    public String applicationName() {
        return applicationName;
    }

    public Version applicationVersion() {
        return applicationVersion;
    }

    public String base() {
        return base;
    }

    public BuildType buildType() {
        return buildType;
    }

    public String instanceName() {
        return instanceName;
    }

    public String instanceOrganization() {
        return instanceOrganization;
    }

    public String productName() {
        return productName;
    }

    public Version productVersion() {
        return productVersion;
    }

    /**
     * Retrieves the product version to be used in links.
     * <p>
     * The product version link is a representation of the product version in the format of "major.minor" if the minor version
     * is not 0, otherwise it only includes the major version. The product version is obtained from the
     * {@link #productVersion()} method.
     *
     * @return the product version link as a string
     * @see #productVersion()
     */
    public String productVersionLink() {
        return productVersion().minor() == 0
                ? String.valueOf(productVersion().major())
                : productVersion().major() + "." + productVersion().minor();
    }

    public Version managementVersion() {
        return managementVersion;
    }

    public OperationMode operationMode() {
        return operationMode;
    }

    public Stability stability() {
        return stability;
    }

    public Stability builtInStability() {
        return builtInStability;
    }

    /**
     * Determines if the stability level of the environment is greater than the built-in stability level.
     *
     * @return true if the stability level of the environment is greater than the built-in stability level, false otherwise.
     */
    public boolean highlightStabilityLevel() {
        return stability.order > builtInStability.order;
    }

    /**
     * Determines if the stability level of the given stability is greater than the stability level of the environment.
     *
     * @param stability the stability level to compare with the environment stability level
     * @return true if the stability level of the given stability is greater than the stability level of the environment, false
     * otherwise.
     */
    public boolean highlightStabilityLevel(Stability stability) {
        return stability != null && stability.order > this.stability.order;
    }

    public boolean standalone() {
        return operationMode == OperationMode.STANDALONE;
    }

    public AccessControlProvider accessControlProvider() {
        return accessControlProvider;
    }

    public boolean sso() {
        return sso;
    }

    public String domainController() {
        return domainController;
    }
}
