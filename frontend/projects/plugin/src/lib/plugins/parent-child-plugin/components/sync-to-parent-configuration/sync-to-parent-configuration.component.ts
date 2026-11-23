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

import {Component, EventEmitter, Input, OnDestroy, OnInit, Output} from "@angular/core";
import {FunctionConfigurationComponent, FunctionConfigurationData} from "@valtimo/plugin";
import {Observable, of, Subscription, take} from "rxjs";
import {NodeMapping, SyncToParentConfig} from "../../models";

// Own mapping rows instead of valtimo-carbon-multi-input, which has no checkbox column for merge.
@Component({
  standalone: false,
  selector: "valtimo-sync-to-parent-configuration",
  templateUrl: "./sync-to-parent-configuration.component.html",
})
export class SyncToParentConfigurationComponent implements FunctionConfigurationComponent, OnInit, OnDestroy {
  @Input() save$!: Observable<void>;
  @Input() disabled$!: Observable<boolean>;
  @Input() pluginId!: string;
  @Input() prefillConfiguration$!: Observable<SyncToParentConfig>;
  @Output() valid: EventEmitter<boolean> = new EventEmitter<boolean>();
  @Output() configuration: EventEmitter<FunctionConfigurationData> = new EventEmitter<FunctionConfigurationData>();

  public mappings: NodeMapping[] = [this.emptyMapping()];

  private readonly subscriptions = new Subscription();

  public ngOnInit(): void {
    this.subscriptions.add(
      (this.prefillConfiguration$ ?? of(null)).pipe(take(1)).subscribe(prefill => {
        if (prefill?.mappings?.length) {
          this.mappings = prefill.mappings.map(mapping => ({...mapping, merge: !!mapping.merge}));
        }
        this.emitValid();
      })
    );
    this.subscriptions.add(
      this.save$?.subscribe(() => {
        if (this.isValid()) {
          this.configuration.emit({mappings: this.mappings});
        }
      })
    );
  }

  public ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  public change(index: number, change: Partial<NodeMapping>): void {
    this.mappings[index] = {...this.mappings[index], ...change};
    this.emitValid();
  }

  public add(): void {
    this.mappings = [...this.mappings, this.emptyMapping()];
    this.emitValid();
  }

  public remove(index: number): void {
    this.mappings = this.mappings.filter((_, i) => i !== index);
    this.emitValid();
  }

  private emitValid(): void {
    this.valid.emit(this.isValid());
  }

  private isValid(): boolean {
    const isPointer = (path: string) => path.startsWith("/");
    return (
      this.mappings.length > 0 &&
      this.mappings.every(mapping => isPointer(mapping.source) && isPointer(mapping.target))
    );
  }

  private emptyMapping(): NodeMapping {
    return {source: "", target: "", merge: false};
  }
}
