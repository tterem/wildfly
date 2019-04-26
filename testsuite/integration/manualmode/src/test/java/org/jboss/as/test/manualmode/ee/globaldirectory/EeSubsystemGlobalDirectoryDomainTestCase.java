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

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
//import java.net.URL;

/**
 * @author Vratislav Marek (vmarek@redhat.com)
 **/
@RunWith(Arquillian.class)
@RunAsClient
public class EeSubsystemGlobalDirectoryDomainTestCase extends EESubsystemGlobalDirectory {



/*
    Major functionality

    org.jboss.as.test.manualmode.ee.globaldirectory.EeSubsystemGlobalDirectoryDomainTestCase#testAppSharedLib

    Test prerequisites
        Create temporary directory and include test jars dependency of test deployment application to all domain servers
    Set logging level to DEBUG
    Define global-directory by CLI command
    Check if global-directory is registered properly
    Restart domain
    Check log file if contains all jars with alphabetical order
    Deploy test application deployment
    Call some method from global-directory in deployment and verify method output
 */

    @Test
    public void testAppSharedLib() throws IOException {

    }

}
