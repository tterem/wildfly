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
package org.jboss.as.test.manualmode.ee.globaldirectory;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.as.test.manualmode.ee.globaldirectory.deployments.GlobalDirectoryDeployment;
import org.jboss.as.test.manualmode.ee.globaldirectory.libraries.GlobalDirectoryLibrary;
import org.jboss.as.test.manualmode.ee.globaldirectory.libraries.GlobalDirectoryLibraryImpl;
import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URL;

/**
 * @author Vratislav Marek (vmarek@redhat.com)
 * @author Tomas Terem (tterem@redhat.com)
 **/
@RunWith(Arquillian.class)
@RunAsClient
public class EeSubsystemGlobalDirectoryTestCase extends EESubsystemGlobalDirectory {

    private static Logger LOGGER = Logger.getLogger(EeSubsystemGlobalDirectoryTestCase.class);

    @Before
    public void setup() throws IOException, InterruptedException {
        LOGGER.error("???????????????????????????????????????????????????????????????????????????????");
        createLibrary(GlobalDirectoryLibrary.class.getSimpleName(), GlobalDirectoryLibrary.class);
        createLibrary(GlobalDirectoryLibraryImpl.class.getSimpleName(), GlobalDirectoryLibraryImpl.class);
        copyJarToGlobalDirectory(GlobalDirectoryLibrary.class.getSimpleName());
        copyJarToGlobalDirectory(GlobalDirectoryLibraryImpl.class.getSimpleName());
        register(GLOBAL_DIRECTORY_NAME);
        // verifyProperlyRegistered(GLOBAL_DIRECTORY_NAME, GLOBAL_DIRECTORY_PATH.toString());
        restartServer();
    }

    @Deployment(name = DEPLOYMENT)
    @TargetsContainer(CONTAINER)
    public static WebArchive createDeployment() {
        LOGGER.error("WWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWw");
        WebArchive war = ShrinkWrap.create(WebArchive.class, DEPLOYMENT + ".war");
        war.addClass(GlobalDirectoryDeployment.class);
        return war;
    }

    /*
        Scenario 1 - major functionality

        While we pre-check testing this feature, we found instability while restart server.
        That’s why it is required several restart of server.
        Purpose of third restart is verify if the feature is stable.

        org.jboss.as.test.manualmode.ee.globaldirectory.EeSubsystemGlobalDirectoryTestCase#testModifyDependencySharedLibs

        Test prerequisites

        Create temporary directory and include test jars dependency of test deployment application
        Define global-directory by CLI command
        Check if global-directory are registered properly and verify his attributes
        Restart server
        Deploy test application deployment



        Call some method from global-directory in deployment and verify method output


        Change the test jars dependency of test deployment application in temporary directory
        Restart server
        Verify in log if application deployment service is loaded correctly
        Call some method from global-directory in deployment and verify method output
        Restart server
        Verify in log if application deployment service is loaded correctly
        Call some method from global-directory in deployment and verify method output
    */
    @Test
    public void testModifyDependencySharedLibs(@ArquillianResource URL url) throws IOException, InterruptedException {
        LOGGER.error(url);
        //deployApplication();

        Response response = client.target(url + "global-directory/library").request().get();
        String result = response.readEntity(String.class);
        LOGGER.error(result);

        restartServer();


    }

    /*
        Scenario 2 - negative reaction to corrupted jar

        org.jboss.as.test.manualmode.ee.globaldirectory.EeSubsystemGlobalDirectoryTestCase#testJBossModulesFoundCorruptedJarInSharedLibs

        Test prerequisites
            Create temporary directory and include corrupted test jar
        Define global-directory by CLI command
        Check if global-directory are registered properly and verify his attributes
        Restart server
        Deploy test application deployment
        Call some method from global-directory in deployment and verify JBoss modules throw correct error message about corrupted file
    */
    @Test
    public void testJBossModulesFoundCorruptedJarInSharedLibs() {

    }

    /*
        Scenario 3 - verify load order

        org.jboss.as.test.manualmode.ee.globaldirectory.EeSubsystemGlobalDirectoryTestCase#testVerifyLoadingOrderSharedLibs

        Test prerequisites
            Create temporary directory and include test jars with duplicities dependency of test deployment application
            Set logging level to DEBUG
        Define global-directory by CLI command
        Check if global-directory are registered properly and verify his attributes
        Restart server
        Check log file if contains all jars with alphabetical order
        Check if global-directory is registered properly
        Deploy test application deployment
        Call some method from global-directory in deployment
        Verify result to contains expected output from jar loaded by alphabetical order first
    */
    @Test
    public void testVerifyLoadingOrderSharedLibs() {

    }

    /*
        Scenario 4 - load any resource

        org.jboss.as.test.manualmode.ee.globaldirectory.EeSubsystemGlobalDirectoryTestCase#testReadPropertyFilesSharedLibs

        Test prerequisites
           Create temporary directory
           Include test jars with dependency of test deployment application
        Include test property files expected by test deployment application from library
        Define global-directory by CLI command
        Check if global-directory are registered properly and verify his attributes
        Restart server
        Deploy test application deployment
        Call some method from global-directory in deployment
        Verify result to contains expected output with property variables
    */
    @Test
    public void testReadPropertyFilesSharedLibs() {

    }

}
