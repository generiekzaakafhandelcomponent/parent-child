/*
 * Copyright 2026 Ritense BV, the Netherlands.
 *
 * Licensed under EUPL, Version 1.2 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import {PluginSpecification} from "@valtimo/plugin";
import {ParentChildPluginConfigurationComponent} from "./components/parent-child-plugin-configuration/parent-child-plugin-configuration.component";
import {PARENT_CHILD_PLUGIN_LOGO_BASE64} from "./assets";
import {ParentChildActionConfigurationComponent} from "./components/parent-child-action-configuration/parent-child-action-configuration.component";
import {SyncToChildrenConfigurationComponent} from "./components/sync-to-children-configuration/sync-to-children-configuration.component";

const parentChildPluginSpecification: PluginSpecification = {
  pluginId: "parent-child-plugin",
  pluginConfigurationComponent: ParentChildPluginConfigurationComponent,
  pluginLogoBase64: PARENT_CHILD_PLUGIN_LOGO_BASE64,
  functionConfigurationComponents: {
    "connect-parent": ParentChildActionConfigurationComponent,
    "sync-to-children": SyncToChildrenConfigurationComponent,
  },
  pluginTranslations: {
    nl: {
      title: "Parent Child Plugin",
      "connect-parent": "Koppel bovenliggend document",
      description: "Dit is een voorbeeld plugin die beschikt over een API call action.",
      configurationTitle: "Configuratienaam",
      actionDescription: "Deze actie koppelt het huidige document aan een bovenliggend document: het huidige document krijgt een PARENT relatie en het bovenliggend document krijgt een CHILD relatie.",
      parentDocumentId: "Document ID van bovenliggend document",
      "sync-to-children": "Synchroniseer naar onderliggende documenten",
      syncToChildrenDescription: "Deze actie kopieert de geconfigureerde onderdelen van het huidige document naar alle onderliggende (CHILD) documenten. Paden zijn JSON pointers, bijvoorbeeld /destination.",
      mappings: "Mapping",
      mappingSource: "Pad in bovenliggend document",
      mappingTarget: "Pad in onderliggend document",
    },
    en: {
      title: "Parent Child Plugin",
      "connect-parent": "Connect parent document",
      description: "This is a parent-child plugin demonstrating an API call action.",
      configurationTitle: "Configuration Name",
      actionDescription: "This action links the current document to a parent document: the current document gets a PARENT relation and the parent document gets a CHILD relation.",
      parentDocumentId: "Parent document ID",
      "sync-to-children": "Sync to child documents",
      syncToChildrenDescription: "This action copies the configured nodes of the current document into all of its child documents. Paths are JSON pointers, for example /destination.",
      mappings: "Mapping",
      mappingSource: "Path in parent document",
      mappingTarget: "Path in child document",
    },
  },
};

export {parentChildPluginSpecification};
