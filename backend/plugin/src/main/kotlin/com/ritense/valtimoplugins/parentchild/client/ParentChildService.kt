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

package com.ritense.valtimoplugins.parentchild.client

import com.ritense.document.domain.impl.JsonSchemaDocumentId
import com.ritense.document.domain.impl.relation.JsonSchemaDocumentRelation
import com.ritense.document.domain.relation.DocumentRelationType
import com.ritense.document.service.DocumentService
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import org.springframework.stereotype.Service

@SkipComponentScan
@Service
class ParentChildService(
    private val documentService: DocumentService,
) {
    /**
     * Links [childDocumentId] and [parentDocumentId] both ways: a PARENT relation is assigned to the child
     * document, and the reciprocal CHILD relation is assigned to the parent document.
     */

    //TODO add transactional
    fun connectParentDocument(childDocumentId: String, parentDocumentId: String) {
        val childId = JsonSchemaDocumentId.existingId(childDocumentId)
        val parentId = JsonSchemaDocumentId.existingId(parentDocumentId)

        documentService.assignDocumentRelation(
            childId,
            JsonSchemaDocumentRelation(parentId, DocumentRelationType.PARENT),
        )
        documentService.assignDocumentRelation(
            parentId,
            JsonSchemaDocumentRelation(childId, DocumentRelationType.CHILD),
        )
    }
}
