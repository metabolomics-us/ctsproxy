import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatCardModule } from '@angular/material/card';
import { MatRadioModule } from '@angular/material/radio';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { ResultTableComponent } from '../result-table/result-table.component';
import { TranslationService } from '../../services/translation.service';
import { DownloadService, ExportStyle, ExportType, ResultsMap } from '../../services/download.service';

@Component({
  selector: 'app-single-conversion',
  imports: [
    FormsModule,
    MatFormFieldModule,
    MatSelectModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatCardModule,
    MatRadioModule,
    MatCheckboxModule,
    ResultTableComponent,
  ],
  templateUrl: './single-conversion.component.html',
  styleUrl: './single-conversion.component.scss',
})
export class SingleConversionComponent implements OnInit {
  fromValues = signal<string[]>([]);
  toValues = signal<string[]>([]);
  singleToValues = signal<string[]>([]);
  errors = signal<string[]>([]);
  inputError = signal<string | null>(null);
  loading = signal(false);
  results = signal<ResultsMap | null>(null);

  queryFrom = 'Chemical Name';
  queryTo = 'InChIKey';
  queryString = '';
  exportStyle: ExportStyle = 'table';
  exportType: ExportType = 'csv';
  topHit = true;

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
      this.singleToValues.set(this.filterIllegal(to, this.queryFrom));
    } catch (err: any) {
      this.errors.update((e) => [...e, err.message || String(err)]);
    }
  }

  onQueryStringChange(): void {
    this.queryString = this.queryString.trim();
    if (this.queryString.includes(';')) {
      this.inputError.set('Invalid input: chemical names must not contain ";". Please enter a single name without semicolons.');
    } else {
      this.inputError.set(null);
    }
  }

  onFromChange(): void {
    this.singleToValues.set(this.filterIllegal(this.toValues(), this.queryFrom));
    if (this.queryFrom !== 'InChIKey' && this.translation.getInChIKeyOnlyToValues().includes(this.queryTo)) {
      this.queryTo = 'InChIKey';
    }
  }

  async convertSingle() {
    if (!this.queryString || this.inputError()) return;
    this.loading.set(true);
    this.errors.set([]);
    try {
      const result = await this.translation.convert(this.queryFrom, this.queryTo, this.queryString);
      const results: ResultsMap = {};
      results[this.queryString] = {};
      results[this.queryString][this.queryTo] = result;
      this.results.set(results);
    } catch (err: any) {
      this.errors.update((e) => [...e, err.message || String(err)]);
    } finally {
      this.loading.set(false);
    }
  }

  download(): void {
    const r = this.results();
    if (!r) return;
    this.downloadService.export(
      { from: this.queryFrom, to: this.queryTo },
      [this.queryString],
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
