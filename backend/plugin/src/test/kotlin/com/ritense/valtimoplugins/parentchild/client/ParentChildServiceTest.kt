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

import com.ritense.document.domain.Document
import com.ritense.document.domain.impl.JsonSchemaDocumentId
import com.ritense.document.domain.impl.relation.JsonSchemaDocumentRelation
import com.ritense.document.domain.relation.DocumentRelationType
import com.ritense.document.service.DocumentService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import kotlin.test.assertEquals

class ParentChildServiceTest {

    private val documentService: DocumentService = mock()
    private val parentChildService = ParentChildService(documentService)

    @Test
    fun `should assign a PARENT relation to the child document and the reciprocal CHILD relation to the parent document`() {
        val childDocumentId = "f3c1a1a0-1111-4a1a-8a1a-000000000001"
        val parentDocumentId = "f3c1a1a0-2222-4a1a-8a1a-000000000002"

        parentChildService.connectParentDocument(childDocumentId, parentDocumentId)

        val documentIdCaptor = argumentCaptor<Document.Id>()
        val relationCaptor = argumentCaptor<JsonSchemaDocumentRelation>()
        verify(documentService, times(2)).assignDocumentRelation(documentIdCaptor.capture(), relationCaptor.capture())

        assertEquals(JsonSchemaDocumentId.existingId(childDocumentId), documentIdCaptor.firstValue)
        assertEquals(JsonSchemaDocumentId.existingId(parentDocumentId), relationCaptor.firstValue.id())
        assertEquals(DocumentRelationType.PARENT, relationCaptor.firstValue.relationType())

        assertEquals(JsonSchemaDocumentId.existingId(parentDocumentId), documentIdCaptor.secondValue)
        assertEquals(JsonSchemaDocumentId.existingId(childDocumentId), relationCaptor.secondValue.id())
        assertEquals(DocumentRelationType.CHILD, relationCaptor.secondValue.relationType())
    }
}
