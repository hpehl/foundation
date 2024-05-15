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

import jakarta.enterprise.context.ApplicationScoped;

import static org.jboss.hal.env.AccessControlProvider.SIMPLE;
import static org.jboss.hal.env.OperationMode.STANDALONE;
import static org.jboss.hal.env.Version.EMPTY_VERSION;

@ApplicationScoped
public class Environment {

    private final String applicationId;
    private final String applicationName;
    private final Version applicationVersion;
    private final String base;
    private final BuildType buildType;
    private String instanceName;
    private String instanceOrganization;
    private String productName;
    private Version productVersion;
    private Version managementVersion;
    private OperationMode operationMode;
    private AccessControlProvider accessControlProvider;
    private boolean sso;

    Environment() {
        // final properties are defined as Closure defines in the POM
        this.applicationId = System.getProperty("environment.id");
        this.applicationName = System.getProperty("environment.name");
        this.applicationVersion = Version.parseVersion(System.getProperty("environment.version"));
        this.base = System.getProperty("environment.base");
        this.buildType = BuildType.parse(System.getProperty("environment.build"));

        // default values for the non-final properties
        this.instanceName = "undefined";
        this.instanceOrganization = "undefined";
        this.productName = "undefined";
        this.productVersion = EMPTY_VERSION;
        this.managementVersion = EMPTY_VERSION;
        this.operationMode = STANDALONE;
        this.accessControlProvider = SIMPLE;
        this.sso = false;
    }

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

    // update non-final properties as part of the bootstrap process
    public void update(AccessControlProvider accessControlProvider, boolean sso) {
        this.accessControlProvider = accessControlProvider;
        this.sso = sso;
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
                ", accessControlProvider=" + accessControlProvider +
                ", sso=" + sso +
                ')';
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

    public Version managementVersion() {
        return managementVersion;
    }

    public OperationMode operationMode() {
        return operationMode;
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
}
