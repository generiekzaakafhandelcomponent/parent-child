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

import com.fasterxml.jackson.core.JsonPointer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.document.domain.impl.JsonSchemaDocumentId
import com.ritense.document.domain.impl.relation.JsonSchemaDocumentRelation
import com.ritense.document.domain.relation.DocumentRelationType
import com.ritense.document.service.DocumentService
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate

private val logger = KotlinLogging.logger {}

@SkipComponentScan
@Service
class ParentChildService(
    private val documentService: DocumentService,
    transactionManager: PlatformTransactionManager,
) {
    private val childTransactionTemplate =
        TransactionTemplate(transactionManager).apply {
            propagationBehavior = TransactionDefinition.PROPAGATION_REQUIRES_NEW
        }

    /**
     * Links [childDocumentId] and [parentDocumentId] both ways: a PARENT relation is assigned to the child
     * document, and the reciprocal CHILD relation is assigned to the parent document.
     */
    @Transactional
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

    /**
     * Copies the parent nodes configured in [mappings] into every CHILD document of [parentDocumentId].
     * Fails fast when a source path does not exist in the parent, before any child is touched. Each child
     * is updated in its own transaction; children that fail do not block the others, and are reported
     * together afterwards.
     */
    fun syncToChildren(
        parentDocumentId: String,
        mappings: List<NodeMapping>,
    ) {
        require(mappings.isNotEmpty()) { "At least one mapping is required" }

        val parent = documentService.get(parentDocumentId)
        val parentContent = parent.content().asJson()
        val sourceNodes =
            mappings.associateWith { mapping ->
                parentContent.at(mapping.source).takeUnless { it.isMissingNode }
                    ?: throw IllegalStateException(
                        "Source path '${mapping.source}' not found in parent document $parentDocumentId",
                    )
            }

        val failedChildIds =
            parent
                .relations()
                .filter { it.relationType() == DocumentRelationType.CHILD }
                .mapNotNull { relation ->
                    val childId = relation.id().toString()
                    try {
                        childTransactionTemplate.executeWithoutResult { syncChild(childId, sourceNodes) }
                        null
                    } catch (e: RuntimeException) {
                        logger.error(e) {
                            "Failed to sync parent document $parentDocumentId to child document $childId"
                        }
                        childId
                    }
                }

        check(failedChildIds.isEmpty()) {
            "Failed to sync parent document $parentDocumentId to child documents $failedChildIds"
        }
    }

    private fun syncChild(
        childDocumentId: String,
        sourceNodes: Map<NodeMapping, JsonNode>,
    ) {
        val child = documentService.get(childDocumentId)
        val content = child.content().asJson().deepCopy<ObjectNode>()
        sourceNodes.forEach { (mapping, node) -> setAt(content, mapping.target, node.deepCopy()) }
        documentService.modifyDocument(child, content)
    }

    private fun setAt(
        root: ObjectNode,
        path: String,
        value: JsonNode,
    ) {
        val pointer = JsonPointer.compile(path)
        require(!pointer.matches()) { "Target path must not be the document root" }
        var parentNode = root
        var current = pointer
        while (!current.tail().matches()) {
            val name = current.matchingProperty
            parentNode = parentNode.get(name) as? ObjectNode ?: parentNode.putObject(name)
            current = current.tail()
        }
        parentNode.set<JsonNode>(current.matchingProperty, value)
    }
}
