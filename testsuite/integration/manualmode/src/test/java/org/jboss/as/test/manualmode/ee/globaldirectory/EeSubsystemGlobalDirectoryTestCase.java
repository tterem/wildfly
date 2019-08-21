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

import org.jboss.arquillian.container.spi.client.container.DeploymentException;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.as.arquillian.container.ManagementClient;
import org.jboss.as.test.manualmode.ee.globaldirectory.deployments.GlobalDirectoryDeployment;
import org.jboss.as.test.manualmode.ee.globaldirectory.deployments.GlobalDirectoryDeployment2;
import org.jboss.as.test.manualmode.ee.globaldirectory.deployments.GlobalDirectoryDeployment3;
import org.jboss.as.test.manualmode.ee.globaldirectory.libraries.GlobalDirectoryLibrary;
import org.jboss.as.test.manualmode.ee.globaldirectory.libraries.GlobalDirectoryLibraryImpl;
import org.jboss.as.test.manualmode.ee.globaldirectory.libraries.GlobalDirectoryLibraryImpl2;
import org.jboss.as.test.manualmode.ee.globaldirectory.util.MavenUtil;
import org.jboss.as.test.shared.TestSuiteEnvironment;
import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

import static org.junit.Assert.fail;

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

    @Deployment(name = DEPLOYMENT2, managed = false, testable = false)
    @TargetsContainer(CONTAINER)
    public static WebArchive createDeployment2() {
        WebArchive war = ShrinkWrap.create(WebArchive.class, DEPLOYMENT2 + ".war");
        war.addClass(GlobalDirectoryDeployment2.class);
        war.addAsWebInfResource(new StringAsset("<?xml version=\"1.0\" encoding=\"UTF-8\"?><web-app><servlet-mapping>\n" +
              "        <servlet-name>javax.ws.rs.core.Application</servlet-name>\n" +
              "        <url-pattern>/*</url-pattern>\n" +
              "    </servlet-mapping></web-app>"),"web.xml");
        return war;
    }

    @Deployment(name = DEPLOYMENT3, managed = false, testable = false)
    @TargetsContainer(CONTAINER)
    public static WebArchive createDeployment3() {
        WebArchive war = ShrinkWrap.create(WebArchive.class, DEPLOYMENT3 + ".war");
        war.addClass(GlobalDirectoryDeployment3.class);
        war.addAsWebInfResource(new StringAsset("<?xml version=\"1.0\" encoding=\"UTF-8\"?><web-app><servlet-mapping>\n" +
              "        <servlet-name>javax.ws.rs.core.Application</servlet-name>\n" +
              "        <url-pattern>/*</url-pattern>\n" +
              "    </servlet-mapping></web-app>"),"web.xml");
        return war;
    }

    @Test
    public void testModifyDependencySharedLibs() throws IOException, InterruptedException {
        ManagementClient managementClient = new ManagementClient(TestSuiteEnvironment.getModelControllerClient(),
              TestSuiteEnvironment.getServerAddress(), TestSuiteEnvironment.getServerPort(), "remote+http");
        URL url = new URL(managementClient.getWebUri().toURL(), '/' + DEPLOYMENT + "/");

        createLibrary(GlobalDirectoryLibrary.class.getSimpleName(), GlobalDirectoryLibrary.class);
        createLibrary(GlobalDirectoryLibraryImpl.class.getSimpleName(), GlobalDirectoryLibraryImpl.class);

        copyLibraryToGlobalDirectory(GlobalDirectoryLibrary.class.getSimpleName());
        copyLibraryToGlobalDirectory(GlobalDirectoryLibraryImpl.class.getSimpleName());

        register(GLOBAL_DIRECTORY_NAME);
        // verifyProperlyRegistered(GLOBAL_DIRECTORY_NAME, GLOBAL_DIRECTORY_PATH.toString());
        restartServer();

        deployer.deploy(DEPLOYMENT);


        Response response = client.target(url + "global-directory/library").request().get();
        String result = response.readEntity(String.class);
        Assert.assertEquals("HELLO WORLD", result);

        restartServer();

        deployer.undeploy(DEPLOYMENT);
    }

    @Test
    public void testJBossModulesFoundCorruptedJarInSharedLibs() throws IOException, InterruptedException {
        createCorruptedLibrary("corrupted", Arrays.asList("hello world"));
        copyLibraryToGlobalDirectory("corrupted");
        register(GLOBAL_DIRECTORY_NAME);
        // verifyProperlyRegistered(GLOBAL_DIRECTORY_NAME, GLOBAL_DIRECTORY_PATH.toString());
        restartServer();

        try {
            deployer.deploy(DEPLOYMENT);
            fail("Exception should have been thrown.");
        } catch (Exception e) {
            Assert.assertEquals(DeploymentException.class, e.getClass());
        }
        logContains("WFLYSRV0276: There is an error in opening zip file " + GLOBAL_DIRECTORY_PATH.toFile().toPath().toAbsolutePath().toString() + "/corrupted.jar");
    }

    @Test
    public void testVerifyLoadingOrderSharedLibs() throws IOException, InterruptedException {
        ManagementClient managementClient = new ManagementClient(TestSuiteEnvironment.getModelControllerClient(),
              TestSuiteEnvironment.getServerAddress(), TestSuiteEnvironment.getServerPort(), "remote+http");
        URL url = new URL(managementClient.getWebUri().toURL(), '/' + DEPLOYMENT + "/");

        MavenUtil mavenUtil;
        mavenUtil = MavenUtil.create(true);

        // File library1;
        File library2;
        File dependency;
        try {
            // library1 = mavenUtil.createMavenGavFile("org.eclipse.microprofile.config:microprofile-config-api:1.3");
            library2 = mavenUtil.createMavenGavFile("org.eclipse.microprofile.config:microprofile-config-api:1.2.1");
            dependency = mavenUtil.createMavenGavFile("org.osgi:org.osgi.annotation.versioning:1.0.0");
        } catch (Exception e) {
            throw new RuntimeException("Unable to get artifacts from maven via Aether library", e);
        }

        // copyLibraryToGlobalDirectory(library1.getName(), library1.toPath().toAbsolutePath());
        copyLibraryToGlobalDirectory(library2.getName(), library2.toPath().toAbsolutePath());
        copyLibraryToGlobalDirectory(dependency.getName(), dependency.toPath().toAbsolutePath());



        createLibrary(GlobalDirectoryLibrary.class.getSimpleName(), GlobalDirectoryLibrary.class);
        createLibrary(GlobalDirectoryLibraryImpl.class.getSimpleName(), GlobalDirectoryLibraryImpl.class);

        copyLibraryToGlobalDirectory(GlobalDirectoryLibrary.class.getSimpleName());
        copyLibraryToGlobalDirectory(GlobalDirectoryLibraryImpl.class.getSimpleName());



        register(GLOBAL_DIRECTORY_NAME);
        // verifyProperlyRegistered(GLOBAL_DIRECTORY_NAME, GLOBAL_DIRECTORY_PATH.toString());
        restartServer();

        deployer.deploy(DEPLOYMENT);

        Response response = client.target(url + "global-directory/library").request().get();
        String result = response.readEntity(String.class);
        Assert.assertEquals("100", result);

        restartServer();

        deployer.undeploy(DEPLOYMENT);

    }

    @Test
    public void testReadPropertyFilesSharedLibs() throws IOException, InterruptedException {
        ManagementClient managementClient = new ManagementClient(TestSuiteEnvironment.getModelControllerClient(),
              TestSuiteEnvironment.getServerAddress(), TestSuiteEnvironment.getServerPort(), "remote+http");
        URL url = new URL(managementClient.getWebUri().toURL(), '/' + DEPLOYMENT2 + "/");

        createLibrary(GlobalDirectoryLibrary.class.getSimpleName(), GlobalDirectoryLibrary.class);
        createLibrary(GlobalDirectoryLibraryImpl2.class.getSimpleName(), GlobalDirectoryLibraryImpl2.class);

        String propertyFileName = "properties";
        String propertyFileString = "PROPERTY FILE";

        createTextFile(propertyFileName, Arrays.asList(propertyFileString ));

        copyLibraryToGlobalDirectory(GlobalDirectoryLibrary.class.getSimpleName());
        copyLibraryToGlobalDirectory(GlobalDirectoryLibraryImpl2.class.getSimpleName());
        copyTextFileToGlobalDirectory(propertyFileName);

        register(GLOBAL_DIRECTORY_NAME);
        // verifyProperlyRegistered(GLOBAL_DIRECTORY_NAME, GLOBAL_DIRECTORY_PATH.toString());
        restartServer();

        deployer.deploy(DEPLOYMENT2);

        Response response = client.target(url + "global-directory/library2").request().get();
        String result = response.readEntity(String.class);
        Assert.assertEquals(propertyFileString, result);

        restartServer();

        deployer.undeploy(DEPLOYMENT2);
    }
}
