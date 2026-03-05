import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatCardModule } from '@angular/material/card';
import { MatRadioModule } from '@angular/material/radio';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatChipsModule } from '@angular/material/chips';
import { ResultTableComponent } from '../result-table/result-table.component';
import { TranslationService } from '../../services/translation.service';
import { DownloadService, ExportStyle, ExportType, ResultsMap } from '../../services/download.service';

@Component({
  selector: 'app-batch-conversion',
  imports: [
    FormsModule,
    MatFormFieldModule,
    MatSelectModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatProgressBarModule,
    MatCardModule,
    MatRadioModule,
    MatCheckboxModule,
    MatChipsModule,
    ResultTableComponent,
  ],
  templateUrl: './batch-conversion.component.html',
  styleUrl: './batch-conversion.component.scss',
})
export class BatchConversionComponent implements OnInit {
  fromValues = signal<string[]>([]);
  toValues = signal<string[]>([]);
  batchToValues = signal<string[]>([]);
  errors = signal<string[]>([]);
  loading = signal(false);
  loadingCounter = signal(0);
  loadingTotal = signal(0);
  batchResults = signal<ResultsMap | null>(null);
  queryStrings = signal<string[]>([]);

  queryFrom = 'Chemical Name';
  queryTo: string[] = ['InChIKey'];
  queryString = '';
  exportStyle: ExportStyle = 'table';
  exportType: ExportType = 'csv';
  topHit = true;

  private generation = 0;

  constructor(
    private translation: TranslationService,
    private downloadService: DownloadService
  ) {}

  async ngOnInit() {
    try {
      const [from, to] = await Promise.all([
        this.translation.getFromValues(),
        this.translation.getToValues(),
      ]);
      this.fromValues.set(this.filterIllegal(from, ''));
      this.toValues.set(to);
      this.batchToValues.set(this.filterIllegal(to, this.queryFrom));
    } catch (err: any) {
      this.errors.update((e) => [...e, err.message || String(err)]);
    }
  }

  onFromChange(): void {
    this.batchToValues.set(this.filterIllegal(this.toValues(), this.queryFrom));
    if (this.queryFrom !== 'InChIKey') {
      const illegal = this.translation.getInChIKeyOnlyToValues();
      this.queryTo = this.queryTo.filter((t) => !illegal.includes(t));
    }
  }

  onFileSelected(event: Event): void {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = () => {
        this.queryString = reader.result as string;
      };
      reader.readAsText(file);
    }
  }

  async convertBatch() {
    if (!this.queryString || this.queryTo.length === 0) return;

    this.generation++;
    const myGeneration = this.generation;
    this.loading.set(true);
    this.errors.set([]);
    this.loadingCounter.set(0);

    const strings = this.queryString.split('\n').filter(Boolean);
    this.queryStrings.set(strings);
    this.loadingTotal.set(strings.length * this.queryTo.length);

    const results: ResultsMap = {};
    for (const s of strings) {
      results[s] = {};
    }
    this.batchResults.set(results);

    for (const s of strings) {
      for (const to of this.queryTo) {
        if (this.generation !== myGeneration) return;

        try {
          const result = await this.translation.convert(this.queryFrom, to, s);
          results[s][to] = result;
        } catch (err: any) {
          results[s][to] = [];
          this.errors.update((e) => [...e, err.message || String(err)]);
        }

        if (this.generation === myGeneration) {
          this.loadingCounter.update((c) => c + 1);
          this.batchResults.set({ ...results });
        }
      }
    }

    this.loading.set(false);
  }

  clearResults(): void {
    this.batchResults.set(null);
    this.queryString = '';
  }

  download(): void {
    const r = this.batchResults();
    if (!r) return;
    this.downloadService.export(
      { from: this.queryFrom, to: this.queryTo },
      this.queryStrings(),
      r,
      this.exportStyle,
      this.topHit,
      this.exportType
    );
  }

  private filterIllegal(array: string[], from: string): string[] {
    if (from !== 'InChIKey') {
      const illegal = this.translation.getInChIKeyOnlyToValues();
      return array.filter((x) => !illegal.includes(x));
    }
    return array;
  }
}
