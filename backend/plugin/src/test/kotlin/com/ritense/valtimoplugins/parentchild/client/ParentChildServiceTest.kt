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

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.document.domain.Document
import com.ritense.document.domain.DocumentContent
import com.ritense.document.domain.impl.JsonSchemaDocumentId
import com.ritense.document.domain.impl.relation.JsonSchemaDocumentRelation
import com.ritense.document.domain.relation.DocumentRelationType
import com.ritense.document.service.DocumentService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.transaction.PlatformTransactionManager
import kotlin.test.assertEquals

class ParentChildServiceTest {

    private val documentService: DocumentService = mock()
    private val transactionManager: PlatformTransactionManager = mock()
    private val parentChildService = ParentChildService(documentService, transactionManager)
    private val objectMapper = ObjectMapper()

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

    @Test
    fun `should copy the mapped parent nodes into every child document`() {
        val parent = document(PARENT_ID, """{"destination":"Rome","activities":["hiking"]}""", CHILD_1_ID, CHILD_2_ID)
        val child1 = document(CHILD_1_ID, """{"name":"Anna"}""")
        val child2 = document(CHILD_2_ID, """{"name":"Bob","trip":{"days":3}}""")

        parentChildService.syncToChildren(
            PARENT_ID,
            listOf(NodeMapping("/destination", "/trip/destination"), NodeMapping("/activities", "/activities")),
        )

        val contentCaptor = argumentCaptor<JsonNode>()
        verify(documentService).modifyDocument(eq(child1), contentCaptor.capture())
        verify(documentService).modifyDocument(eq(child2), contentCaptor.capture())
        assertEquals(
            json("""{"name":"Anna","trip":{"destination":"Rome"},"activities":["hiking"]}"""),
            contentCaptor.firstValue,
        )
        assertEquals(
            json("""{"name":"Bob","trip":{"days":3,"destination":"Rome"},"activities":["hiking"]}"""),
            contentCaptor.secondValue,
        )
        verify(documentService, never()).modifyDocument(eq(parent), any<JsonNode>())
    }

    @Test
    fun `should fail fast without touching children when a source path is missing in the parent`() {
        document(PARENT_ID, """{"destination":"Rome"}""", CHILD_1_ID)

        assertThrows<IllegalStateException> {
            parentChildService.syncToChildren(PARENT_ID, listOf(NodeMapping("/hotel", "/hotel")))
        }

        verify(documentService, never()).get(CHILD_1_ID)
    }

    @Test
    fun `should sync the remaining children and report the failed ones`() {
        document(PARENT_ID, """{"destination":"Rome"}""", CHILD_1_ID, CHILD_2_ID)
        val child1 = document(CHILD_1_ID, """{}""")
        val child2 = document(CHILD_2_ID, """{}""")
        doThrow(IllegalArgumentException("schema violation"))
            .whenever(documentService)
            .modifyDocument(eq(child1), any<JsonNode>())

        val exception =
            assertThrows<IllegalStateException> {
                parentChildService.syncToChildren(PARENT_ID, listOf(NodeMapping("/destination", "/destination")))
            }

        verify(documentService).modifyDocument(eq(child2), any<JsonNode>())
        assertEquals(true, exception.message!!.contains(CHILD_1_ID))
        assertEquals(false, exception.message!!.contains(CHILD_2_ID))
    }

    @Test
    fun `should reject an empty mapping`() {
        assertThrows<IllegalArgumentException> {
            parentChildService.syncToChildren(PARENT_ID, emptyList())
        }
    }

    private fun document(
        id: String,
        content: String,
        vararg childIds: String,
    ): Document {
        val documentContent: DocumentContent = mock { on { asJson() } doReturn json(content) }
        val relations =
            childIds
                .map { JsonSchemaDocumentRelation(JsonSchemaDocumentId.existingId(it), DocumentRelationType.CHILD) }
                .toSet()
        val document: Document =
            mock {
                on { content() } doReturn documentContent
                on { relations() } doReturn relations
            }
        whenever(documentService.get(id)).thenReturn(document)
        return document
    }

    private fun json(content: String): JsonNode = objectMapper.readTree(content)

    companion object {
        private const val PARENT_ID = "f3c1a1a0-2222-4a1a-8a1a-000000000002"
        private const val CHILD_1_ID = "f3c1a1a0-3333-4a1a-8a1a-000000000003"
        private const val CHILD_2_ID = "f3c1a1a0-4444-4a1a-8a1a-000000000004"
    }
}
