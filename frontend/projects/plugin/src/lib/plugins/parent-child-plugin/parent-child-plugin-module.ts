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

import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";
import {PluginTranslatePipeModule} from "@valtimo/plugin";
import {ButtonModule, CheckboxModule, InputModule as CarbonInputModule} from "carbon-components-angular";
import {CarbonMultiInputModule, FormModule, InputModule as ValtimoInputModule} from "@valtimo/components";
import {ParentChildPluginConfigurationComponent} from "./components/parent-child-plugin-configuration/parent-child-plugin-configuration.component";
import {ParentChildActionConfigurationComponent} from "./components/parent-child-action-configuration/parent-child-action-configuration.component";
import {SyncToChildrenConfigurationComponent} from "./components/sync-to-children-configuration/sync-to-children-configuration.component";
import {SyncToParentConfigurationComponent} from "./components/sync-to-parent-configuration/sync-to-parent-configuration.component";

@NgModule({
  declarations: [
    ParentChildPluginConfigurationComponent,
    ParentChildActionConfigurationComponent,
    SyncToChildrenConfigurationComponent,
    SyncToParentConfigurationComponent,
  ],
  imports: [CommonModule, PluginTranslatePipeModule, FormModule, ValtimoInputModule, CarbonMultiInputModule, ButtonModule, CheckboxModule, CarbonInputModule],
  exports: [
    ParentChildPluginConfigurationComponent,
    ParentChildActionConfigurationComponent,
    SyncToChildrenConfigurationComponent,
    SyncToParentConfigurationComponent,
  ],
})
export class ParentChildPluginModule {
}
