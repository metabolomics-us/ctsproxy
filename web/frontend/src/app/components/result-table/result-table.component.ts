import { Component, input } from '@angular/core';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { ConversionResultItem } from '../../services/translation.service';
import { ResultsMap } from '../../services/download.service';

@Component({
  selector: 'app-result-table',
  imports: [MatTableModule, MatPaginatorModule, MatIconModule, MatButtonModule],
  templateUrl: './result-table.component.html',
  styleUrl: './result-table.component.scss',
})
export class ResultTableComponent {
  sourceColumn = input.required<string>();
  columns = input.required<string[]>();
  ordering = input.required<string[]>();
  results = input.required<ResultsMap>();

  pageSize = 10;
  pageIndex = 0;
  private expanded = new Set<string>();

  pagedTerms(): string[] {
    const start = this.pageIndex * this.pageSize;
    return this.ordering().slice(start, start + this.pageSize);
  }

  getResults(term: string, col: string): ConversionResultItem[] {
    return this.results()?.[term]?.[col] || [];
  }

  isExpanded(term: string, col: string): boolean {
    return this.expanded.has(`${term}::${col}`);
  }

  toggleExpand(term: string, col: string): void {
    const key = `${term}::${col}`;
    if (this.expanded.has(key)) {
      this.expanded.delete(key);
    } else {
      this.expanded.add(key);
    }
  }

  onPage(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
  }

  onResizeStart(event: MouseEvent): void {
    event.preventDefault();
    const handle = event.target as HTMLElement;
    const th = handle.parentElement as HTMLTableCellElement;
    const startX = event.pageX;
    const startWidth = th.offsetWidth;

    handle.classList.add('resizing');

    const onMouseMove = (e: MouseEvent) => {
      const newWidth = Math.max(50, startWidth + (e.pageX - startX));
      th.style.width = `${newWidth}px`;
    };

    const onMouseUp = () => {
      handle.classList.remove('resizing');
      document.removeEventListener('mousemove', onMouseMove);
      document.removeEventListener('mouseup', onMouseUp);
    };

    document.addEventListener('mousemove', onMouseMove);
    document.addEventListener('mouseup', onMouseUp);
  }
}
