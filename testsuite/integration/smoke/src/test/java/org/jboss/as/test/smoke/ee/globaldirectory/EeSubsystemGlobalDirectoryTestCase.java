/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2019, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.as.test.smoke.ee.globaldirectory;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.as.arquillian.api.ContainerResource;
import org.jboss.as.arquillian.container.ManagementClient;
import org.jboss.dmr.ModelNode;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.FAILED;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.FAILURE_DESCRIPTION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.INCLUDE_RUNTIME;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OUTCOME;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.PATH;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.REMOVE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.RESULT;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUCCESS;

/**
 * @author Vratislav Marek (vmarek@redhat.com)
 **/
@RunWith(Arquillian.class)
@RunAsClient
public class EeSubsystemGlobalDirectoryTestCase {

    private static final String SUBSYSTEM_EE = "ee";
    private static final String GLOBAL_DIRECTORY = "global-directory";
    private static final String DUPLICATE_ERROR_GLOBAL_DIRECTORY_CODE = "WFLYEE0121";

    private static final String FIRST_GLOBAL_DIRECTORY_NAME = "sharelib1";
    // Path is int validated, can be added non-existed path
    private static final String FIRST_GLOBAL_DIRECTORY_PATH = "libs/lib1";

    private static final String SECOND_GLOBAL_DIRECTORY_NAME = "sharelib2";
    // Path is int validated, can be added non-existed path
    private static final String SECOND_GLOBAL_DIRECTORY_PATH = "libs/lib2";

    @ContainerResource
    private ManagementClient managementClient;

    /**
     * Test checking if exist global directory command
     */
    @Test
    public void testAddAndRemoveSharedLib() throws IOException {
        register(FIRST_GLOBAL_DIRECTORY_NAME, FIRST_GLOBAL_DIRECTORY_PATH);
        verifyProperlyRegistered(FIRST_GLOBAL_DIRECTORY_NAME, FIRST_GLOBAL_DIRECTORY_PATH);
        remove(FIRST_GLOBAL_DIRECTORY_NAME);
        verifyNonExist(FIRST_GLOBAL_DIRECTORY_NAME);
    }

    /**
     * Test checking rule for only one global directory
     */
    @Test
    public void testRejectSecondSharedLib() throws IOException {
        register(FIRST_GLOBAL_DIRECTORY_NAME, FIRST_GLOBAL_DIRECTORY_PATH);
        verifyProperlyRegistered(FIRST_GLOBAL_DIRECTORY_NAME, FIRST_GLOBAL_DIRECTORY_PATH);

        final ModelNode response = register(SECOND_GLOBAL_DIRECTORY_NAME, SECOND_GLOBAL_DIRECTORY_PATH, false);
        ModelNode outcome = response.get(OUTCOME);
        assertThat("Registration of global directory failure!", outcome.asString(), is(FAILED));
        final ModelNode failureDescription = response.get(FAILURE_DESCRIPTION);
        assertThat("Error message doesn't contains information about duplicate global directory",
                failureDescription.asString(), containsString(DUPLICATE_ERROR_GLOBAL_DIRECTORY_CODE));

        verifyNonExist(SECOND_GLOBAL_DIRECTORY_NAME);
    }

    /**
     * Register global directory
     * Verify the response for success
     *
     * @param name Name of new global directory
     * @param path Directory path to the libraries for the new global directory.
     */
    private ModelNode register(String name, String path) throws IOException {
        return register(name, path, true);
    }

    /**
     * Register global directory
     *
     * @param name          Name of new global directory
     * @param path          Directory path to the libraries for the new global directory.
     * @param expectSuccess If is true verify the response for success, If is false only return operation result
     */
    private ModelNode register(String name, String path, boolean expectSuccess) throws IOException {
        // /subsystem=ee/global-directory=<<name>>:add(path=<<path>>)
        final ModelNode address = new ModelNode();
        address.add(SUBSYSTEM, SUBSYSTEM_EE)
                .add(GLOBAL_DIRECTORY, name)
                .protect();
        final ModelNode operation = new ModelNode();
        operation.get(OP).set(ADD);
        operation.get(INCLUDE_RUNTIME).set(true);
        operation.get(OP_ADDR).set(address);
        operation.get(PATH).set(path);

        ModelNode response = execute(operation);
        ModelNode outcome = response.get(OUTCOME);
        if (expectSuccess) {
            assertThat("Registration of global directory " + name + " failure!", outcome.asString(), is(SUCCESS));
        }
        return response;
    }

    /**
     * Remove global directory
     *
     * @param name Name of global directory for removing
     */
    private ModelNode remove(String name) throws IOException {
        // /subsystem=ee/global-directory=<<name>>:remove
        final ModelNode address = new ModelNode();
        address.add(SUBSYSTEM, SUBSYSTEM_EE)
                .add(GLOBAL_DIRECTORY, name)
                .protect();
        final ModelNode operation = new ModelNode();
        operation.get(OP).set(REMOVE);
        operation.get(INCLUDE_RUNTIME).set(true);
        operation.get(OP_ADDR).set(address);

        ModelNode response = execute(operation);
        ModelNode outcome = response.get(OUTCOME);
        assertThat("Remove of global directory " + name + "  failure!", outcome.asString(), is(SUCCESS));
        return response;
    }

    /**
     * Verify if is global directory is registered and contains right path
     *
     * @param name Name of global directory for verify
     * @param path Expected set path for current global directory
     */
    private ModelNode verifyProperlyRegistered(String name, String path) throws IOException {
        // /subsystem=ee/global-directory=<<name>>:read-resource
        final ModelNode address = new ModelNode();
        address.add(SUBSYSTEM, SUBSYSTEM_EE)
                .add(GLOBAL_DIRECTORY, name)
                .protect();
        final ModelNode operation = new ModelNode();
        operation.get(OP).set(READ_RESOURCE_OPERATION);
        operation.get(INCLUDE_RUNTIME).set(true);
        operation.get(OP_ADDR).set(address);

        ModelNode response = execute(operation);
        ModelNode outcome = response.get(OUTCOME);
        assertThat("Read resource of global directory " + name + " failure!", outcome.asString(), is(SUCCESS));

        final ModelNode result = response.get(RESULT);
        assertThat("Global directory " + name + " have set wrong path!", result.get(PATH).asString(), is(path));
        return response;
    }

    /**
     * Verify global directory isn't exist
     *
     * @param name Name of global directory for verify
     */
    private ModelNode verifyNonExist(String name) throws IOException {
        // /subsystem=ee/global-directory=<<name>>:read-resource
        final ModelNode address = new ModelNode();
        address.add(SUBSYSTEM, SUBSYSTEM_EE)
                .add(GLOBAL_DIRECTORY, name)
                .protect();
        final ModelNode operation = new ModelNode();
        operation.get(OP).set(READ_RESOURCE_OPERATION);
        operation.get(INCLUDE_RUNTIME).set(true);
        operation.get(OP_ADDR).set(address);

        ModelNode response = execute(operation);
        ModelNode outcome = response.get(OUTCOME);
        assertThat("Global directory " + name + " still exist!", outcome.asString(), not(SUCCESS));
        return response;
    }

    /**
     * Execute operation in wildfly
     *
     * @param operation Cli command represent in ModelNode interpretation
     */
    private ModelNode execute(final ModelNode operation) throws
            IOException {
        return managementClient.getControllerClient().execute(operation);
    }
}
