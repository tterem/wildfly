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
import org.jboss.as.arquillian.container.ManagementClient;
import org.jboss.as.test.manualmode.ee.globaldirectory.deployments.GlobalDirectoryDeployment;
import org.jboss.as.test.manualmode.ee.globaldirectory.libraries.GlobalDirectoryLibrary;
import org.jboss.as.test.manualmode.ee.globaldirectory.libraries.GlobalDirectoryLibraryImpl;
import org.jboss.as.test.shared.TestSuiteEnvironment;
import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
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
        createLibrary(GlobalDirectoryLibrary.class.getSimpleName(), GlobalDirectoryLibrary.class);
        createLibrary(GlobalDirectoryLibraryImpl.class.getSimpleName(), GlobalDirectoryLibraryImpl.class);
        copyJarToGlobalDirectory(GlobalDirectoryLibrary.class.getSimpleName());
        copyJarToGlobalDirectory(GlobalDirectoryLibraryImpl.class.getSimpleName());
        register(GLOBAL_DIRECTORY_NAME);
        // verifyProperlyRegistered(GLOBAL_DIRECTORY_NAME, GLOBAL_DIRECTORY_PATH.toString());
        restartServer();
    }

    @Deployment(name = DEPLOYMENT, managed = false, testable = false)
    @TargetsContainer(CONTAINER)
    public static WebArchive createDeployment() {
        WebArchive war = ShrinkWrap.create(WebArchive.class, DEPLOYMENT + ".war");
        war.addClass(GlobalDirectoryDeployment.class);
        war.addAsWebInfResource(new StringAsset("<?xml version=\"1.0\" encoding=\"UTF-8\"?><web-app><servlet-mapping>\n" +
                "        <servlet-name>javax.ws.rs.core.Application</servlet-name>\n" +
                "        <url-pattern>/*</url-pattern>\n" +
                "    </servlet-mapping></web-app>"),"web.xml");
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
    public void testModifyDependencySharedLibs() throws IOException, InterruptedException {
        ManagementClient managementClient = new ManagementClient(TestSuiteEnvironment.getModelControllerClient(),
        TestSuiteEnvironment.getServerAddress(), TestSuiteEnvironment.getServerPort(), "remote+http");
        URL url = new URL(managementClient.getWebUri().toURL(), '/' + DEPLOYMENT + "/");

        deployer.deploy(DEPLOYMENT);

        LOGGER.error(url);
        //deployApplication();

        Response response = client.target(url + "global-directory/library").request().get();
        String result = response.readEntity(String.class);
        LOGGER.error(result);

        restartServer();


    }

}
