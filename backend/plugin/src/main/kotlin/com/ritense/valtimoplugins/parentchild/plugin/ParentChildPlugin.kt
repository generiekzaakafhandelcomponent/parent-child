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

package com.ritense.valtimoplugins.parentchild.plugin

import com.ritense.plugin.annotation.Plugin
import com.ritense.plugin.annotation.PluginAction
import com.ritense.plugin.annotation.PluginActionProperty
import com.ritense.processlink.domain.ActivityTypeWithEventName.SERVICE_TASK_START
import com.ritense.valtimoplugins.parentchild.client.ParentChildService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.operaton.bpm.engine.delegate.DelegateExecution

private val logger = KotlinLogging.logger {}

/**
 * Parent Child plugin demonstrating a simple action that interacts with an API endpoint.
 * Note that the key in the @Plugin annotation must be unique, and
 * should be equal to the pluginId in the plugin's frontend configuration.
 */
@Plugin(
    key = "parent-child-plugin",
    title = "Parent Child Plugin",
    description = "This is a parent-child plugin demonstrating an API call action.",
)
open class ParentChildPlugin(
    private val parentChildService: ParentChildService,
) {
    /**
     * Links the document the process is running for to a parent document, by assigning a PARENT
     * DocumentRelation to it.
     */
    @PluginAction(
        key = "connect-parent",
        title = "connect parent",
        description = "Links the current document to a parent document by adding a PARENT relation.",
        activityTypes = [SERVICE_TASK_START],
    )
    open fun connectParentDocument(
        execution: DelegateExecution,
        @PluginActionProperty parentDocumentId: String,
    ) {
        parentChildService.connectParentDocument(
            childDocumentId = execution.processBusinessKey,
            parentDocumentId = parentDocumentId,
        )
    }
}
