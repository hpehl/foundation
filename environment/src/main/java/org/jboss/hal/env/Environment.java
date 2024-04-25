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

@ApplicationScoped
public class Environment {

    private final String applicationId;
    private final String applicationName;
    private final Version applicationVersion;
    private final String base;
    private final BuildType buildType;
    private String instanceName;
    private String instanceOrganization;
    private Version instanceVersion;
    private Version managementVersion;
    private OperationMode operationMode;
    private boolean sso;

    Environment() {
        // final properties are defined as Closure defines in the POM
        this.applicationId = System.getProperty("environment.id");
        this.applicationName = System.getProperty("environment.name");
        this.applicationVersion = Version.parseVersion(System.getProperty("environment.version"));
        this.base = System.getProperty("environment.base");
        this.buildType = BuildType.parse(System.getProperty("environment.build"));
    }

    // init non-final properties as part of the bootstrap process
    public void init(String instanceName, String instanceOrganization, Version instanceVersion,
            Version managementVersion, OperationMode operationMode, boolean sso) {
        this.instanceName = instanceName;
        this.instanceOrganization = instanceOrganization;
        this.instanceVersion = instanceVersion;
        this.managementVersion = managementVersion;
        this.operationMode = operationMode;
        this.sso = sso;
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

    public Version instanceVersion() {
        return instanceVersion;
    }

    public Version managementVersion() {
        return managementVersion;
    }

    public OperationMode operationMode() {
        return operationMode;
    }

    public boolean sso() {
        return sso;
    }
}
