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
import {MultiInputKeyValue, MultiInputOutput} from "@valtimo/components";
import {BehaviorSubject, combineLatest, map, Observable, of, Subscription, switchMap, take} from "rxjs";
import {SyncToChildrenConfig} from "../../models";

@Component({
  standalone: false,
  selector: "valtimo-sync-to-children-configuration",
  templateUrl: "./sync-to-children-configuration.component.html",
})
export class SyncToChildrenConfigurationComponent implements FunctionConfigurationComponent, OnInit, OnDestroy {
  @Input() save$!: Observable<void>;
  @Input() disabled$!: Observable<boolean>;
  @Input() pluginId!: string;
  @Input() prefillConfiguration$!: Observable<SyncToChildrenConfig>;
  @Output() valid: EventEmitter<boolean> = new EventEmitter<boolean>();
  @Output() configuration: EventEmitter<FunctionConfigurationData> = new EventEmitter<FunctionConfigurationData>();

  public prefillMappings$!: Observable<MultiInputKeyValue[]>;

  private saveSubscription!: Subscription;
  private readonly formValue$ = new BehaviorSubject<SyncToChildrenConfig | null>(null);
  private readonly valid$ = new BehaviorSubject<boolean>(false);

  public ngOnInit(): void {
    this.prefillMappings$ = (this.prefillConfiguration$ ?? of(null)).pipe(
      map(prefill => (prefill?.mappings ?? []).map(mapping => ({key: mapping.source, value: mapping.target})))
    );
    this.openSaveSubscription();
  }

  public ngOnDestroy() {
    this.saveSubscription?.unsubscribe();
  }

  public mappingsChange(values: MultiInputOutput): void {
    const formValue: SyncToChildrenConfig = {
      mappings: (values as MultiInputKeyValue[]).map(value => ({source: value.key ?? "", target: value.value ?? ""})),
    };
    this.formValue$.next(formValue);
    this.handleValid(formValue);
  }

  private handleValid(formValue: SyncToChildrenConfig): void {
    const isPointer = (path: string) => path.startsWith("/");
    const valid =
      formValue.mappings.length > 0 &&
      formValue.mappings.every(mapping => isPointer(mapping.source) && isPointer(mapping.target));
    this.valid$.next(valid);
    this.valid.emit(valid);
  }

  private openSaveSubscription(): void {
    this.saveSubscription = this.save$
      ?.pipe(
        switchMap(() => combineLatest([this.formValue$, this.valid$]).pipe(take(1)))
      )
      .subscribe(([formValue, valid]) => {
        if (valid) {
          this.configuration.emit(formValue!);
        }
      });
  }
}
