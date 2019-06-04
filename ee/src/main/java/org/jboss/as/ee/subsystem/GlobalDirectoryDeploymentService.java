/*
 * Copyright 2019 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.as.ee.subsystem;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

import org.jboss.as.ee.logging.EeLogger;
import org.jboss.as.ee.subsystem.GlobalDirectoryResourceDefinition.GlobalDirectory;
import org.jboss.as.server.deployment.module.ModuleDependency;
import org.jboss.as.server.deployment.module.ModuleSpecification;
import org.jboss.as.server.moduleservice.ExternalModuleService;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoader;
import org.jboss.msc.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

/**
 * This service do the required steps to add a directory as a deployment dependency.
 * Basically is used as an intermediate step in GlobalDirectoryDependencyProcessor to retrieve the global directory
 * configured as a resource under EE subsystem.
 *
 * @author Yeray Borges
 */
public class GlobalDirectoryDeploymentService implements Service {
    private List<Supplier<GlobalDirectory>> globalDirectories;
    private final ExternalModuleService externalModuleService;
    private final ModuleSpecification moduleSpecification;
    private final ModuleLoader moduleLoader;
    private final Object lock;

    public GlobalDirectoryDeploymentService(List<Supplier<GlobalDirectory>> globalDirectories, final ExternalModuleService externalModuleService, final ModuleSpecification moduleSpecification, final ModuleLoader moduleLoader, final Object lock ) {
        this.globalDirectories = globalDirectories;
        this.moduleSpecification = moduleSpecification;
        this.externalModuleService = externalModuleService;
        this.moduleLoader = moduleLoader;
        this.lock = lock;
    }

    @Override
    public void start(StartContext context) throws StartException {
        final List<GlobalDirectory> dataSorted = new ArrayList<>();
        for (Supplier<GlobalDirectory> dataSupplier : globalDirectories) {
            GlobalDirectory data = dataSupplier.get();
            dataSorted.add(data);
        }

        //validate all exists
        for(GlobalDirectory globalDirectory : dataSorted) {
            if (!Files.exists(globalDirectory.getResolvedPath())) {
                throw EeLogger.ROOT_LOGGER.globalDirectoryDoNotExist(globalDirectory.getResolvedPath().toString(), globalDirectory.getName());
            }
        }

        //Possible optimization, do not load modules if there is a sub directory
//        Iterator<GlobalDirectory> itCurrent = dataSorted.iterator();
//        for(; itCurrent.hasNext(); ) {
//            Path current = itCurrent.next().getResolvedPath().toPath();
//            for(int j=1; j< dataSorted.size(); j++ ) {
//                Path next = dataSorted.get(j).getResolvedPath().toPath();
//
//                if (current.getNameCount() > next.getNameCount() && current.startsWith(next)) {
//                    itCurrent.remove();
//                }
//            }
//        }

        //sort by name, it will allow setting the final deployment module dependencies in a deterministic way
        Collections.sort(dataSorted, Comparator.comparing(GlobalDirectory::getModuleName));

        synchronized (lock) {
            for (GlobalDirectory data : dataSorted) {
                Path resolvedPath = data.getResolvedPath();
                String moduleName = data.getModuleName();
                ModuleIdentifier moduleIdentifier = externalModuleService.addExternalModule(moduleName, resolvedPath.toString());
                moduleSpecification.addSystemDependency(new ModuleDependency(moduleLoader, moduleIdentifier, false, true, true, false));
            }
        }
    }

    @Override
    public void stop(StopContext context) {

    }
}
