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

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.parsing.ParseUtils.missingRequired;
import static org.jboss.as.controller.parsing.ParseUtils.requireNoAttributes;
import static org.jboss.as.controller.parsing.ParseUtils.requireNoContent;
import static org.jboss.as.controller.parsing.ParseUtils.requireNoNamespaceAttribute;
import static org.jboss.as.controller.parsing.ParseUtils.unexpectedAttribute;
import static org.jboss.as.controller.parsing.ParseUtils.unexpectedElement;

import java.util.EnumSet;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import org.jboss.staxmapper.XMLExtendedStreamReader;

/**
 * EE Subsystem parser for 5.0 schema version.
 * <p>
 * It extends the previous schema version adding support to global directories.
 *
 * @author Yeray Borges
 */
public class EESubsystemParser50 extends EESubsystemParser40{

    EESubsystemParser50() {

    }

    @Override
    public void readElement(XMLExtendedStreamReader reader, List<ModelNode> list) throws XMLStreamException {
        // EE subsystem doesn't have any attributes, so make sure that the xml doesn't have any
        requireNoAttributes(reader);
        final PathAddress subsystemPathAddress = PathAddress.pathAddress(EeExtension.PATH_SUBSYSTEM);
        final ModelNode eeSubSystem = Util.createAddOperation(subsystemPathAddress);
        // add the subsystem to the ModelNode(s)
        list.add(eeSubSystem);

        // elements
        final EnumSet<Element> encountered = EnumSet.noneOf(Element.class);
        while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
            switch (Namespace.forUri(reader.getNamespaceURI())) {
                case EE_5_0: {
                    final Element element = Element.forName(reader.getLocalName());
                    if (!encountered.add(element)) {
                        throw unexpectedElement(reader);
                    }
                    switch (element) {
                        case GLOBAL_MODULES: {
                            final ModelNode model = parseGlobalModules(reader);
                            eeSubSystem.get(GlobalModulesDefinition.GLOBAL_MODULES).set(model);
                            break;
                        }
                        case GLOBAL_DIRECTORIES: {
                            parseGlobalDirectories(reader, list, subsystemPathAddress);
                            break;
                        }
                        case EAR_SUBDEPLOYMENTS_ISOLATED: {
                            final String earSubDeploymentsIsolated = parseEarSubDeploymentsIsolatedElement(reader);
                            // set the ear subdeployment isolation on the subsystem operation
                            EeSubsystemRootResource.EAR_SUBDEPLOYMENTS_ISOLATED.parseAndSetParameter(earSubDeploymentsIsolated, eeSubSystem, reader);
                            break;
                        }
                        case SPEC_DESCRIPTOR_PROPERTY_REPLACEMENT: {
                            final String enabled = parseSpecDescriptorPropertyReplacement(reader);
                            EeSubsystemRootResource.SPEC_DESCRIPTOR_PROPERTY_REPLACEMENT.parseAndSetParameter(enabled, eeSubSystem, reader);
                            break;
                        }
                        case JBOSS_DESCRIPTOR_PROPERTY_REPLACEMENT: {
                            final String enabled = parseJBossDescriptorPropertyReplacement(reader);
                            EeSubsystemRootResource.JBOSS_DESCRIPTOR_PROPERTY_REPLACEMENT.parseAndSetParameter(enabled, eeSubSystem, reader);
                            break;
                        }
                        case ANNOTATION_PROPERTY_REPLACEMENT: {
                            final String enabled = parseEJBAnnotationPropertyReplacement(reader);
                            EeSubsystemRootResource.ANNOTATION_PROPERTY_REPLACEMENT.parseAndSetParameter(enabled, eeSubSystem, reader);
                            break;
                        }
                        case CONCURRENT: {
                            parseConcurrent(reader, list, subsystemPathAddress);
                            break;
                        }
                        case DEFAULT_BINDINGS: {
                            parseDefaultBindings(reader, list, subsystemPathAddress);
                            break;
                        }
                        default: {
                            throw unexpectedElement(reader);
                        }
                    }
                    break;
                }
                default: {
                    throw unexpectedElement(reader);
                }
            }
        }
    }

    static void parseGlobalDirectories(XMLExtendedStreamReader reader, List<ModelNode> operations, PathAddress subsystemPathAddress) throws XMLStreamException {
        requireNoAttributes(reader);
        boolean empty = true;
        while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
            switch (Element.forName(reader.getLocalName())) {
                case DIRECTORY: {
                    empty = false;
                    parseDirectory(reader, operations, subsystemPathAddress);
                    break;
                }
                default: {
                    throw unexpectedElement(reader);
                }
            }
        }
        if (empty) {
            throw missingRequired(reader, EnumSet.of(Element.DIRECTORY));
        }
    }

    static void parseDirectory(XMLExtendedStreamReader reader, List<ModelNode> operations, PathAddress subsystemPathAddress) throws XMLStreamException {
        final ModelNode addOperation = Util.createAddOperation();
        final int count = reader.getAttributeCount();
        String name = null;
        final EnumSet<Attribute> required = EnumSet.of(Attribute.NAME, Attribute.PATH);
        for (int i = 0; i < count; i++) {
            requireNoNamespaceAttribute(reader, i);
            final String value = reader.getAttributeValue(i);
            final Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
            required.remove(attribute);
            switch (attribute) {
                case NAME:
                    name = value.trim();
                    break;
                case PATH:
                    GlobalDirectoryResourceDefinition.PATH.parseAndSetParameter(value, addOperation, reader);
                    break;
                case RELATIVE_TO:
                    GlobalDirectoryResourceDefinition.RELATIVE_TO.parseAndSetParameter(value, addOperation, reader);
                    break;
                default:
                    throw unexpectedAttribute(reader, i);
            }
        }
        if (!required.isEmpty()) {
            throw missingRequired(reader, required);
        }
        requireNoContent(reader);
        final PathAddress address = subsystemPathAddress.append(EESubsystemModel.GLOBAL_DIRECTORY, name);
        addOperation.get(OP_ADDR).set(address.toModelNode());
        operations.add(addOperation);
    }
}
