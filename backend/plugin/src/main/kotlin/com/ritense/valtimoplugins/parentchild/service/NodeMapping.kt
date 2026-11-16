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

package com.ritense.valtimoplugins.parentchild.service

/**
 * Copies the node at JSON pointer [source] in the source document to JSON pointer [target] in the target document.
 * With [merge], an array source is added to an existing array target without duplicates instead of replacing it
 * (only used by sync-to-parent).
 */
data class NodeMapping(
    val source: String,
    val target: String,
    val merge: Boolean = false,
)
