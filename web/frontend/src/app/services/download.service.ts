import { Injectable } from '@angular/core';
import { ConversionResultItem } from './translation.service';

export type ExportStyle = 'table' | 'list';
export type ExportType = 'csv' | 'tsv';

export interface QueryLike {
  from: string;
  to: string | string[];
}

export type ResultsMap = Record<string, Record<string, ConversionResultItem[]>>;

@Injectable({ providedIn: 'root' })
export class DownloadService {
  export(
    query: QueryLike,
    queryStrings: string[],
    results: ResultsMap,
    style: ExportStyle,
    topHit: boolean,
    type: ExportType
  ): void {
    const data =
      style === 'list'
        ? this.processList(query, queryStrings, results, topHit)
        : this.processTable(query, queryStrings, results, topHit);

    const now = new Date();
    const pad2 = (n: number) => (n < 10 ? '0' : '') + n;
    const date = `${now.getFullYear()}${pad2(now.getMonth() + 1)}${pad2(now.getDate())}${pad2(now.getHours())}${pad2(now.getMinutes())}${pad2(now.getSeconds())}`;

    const blob = new Blob([data], { type: `text/${type}` });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `cts-${date}.${type}`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  }

  private processList(query: QueryLike, searchTerms: string[], results: ResultsMap, topHit: boolean): string {
    let header = 'From,To,Term,Result';
    let body = '';
    let scored = false;

    for (const searchTerm of searchTerms) {
      for (const target of Object.keys(results[searchTerm] || {})) {
        const resultList = results[searchTerm][target];
        if (target === 'InChIKey') scored = true;

        body += '\n';
        if (topHit) {
          body +=
            target === 'InChIKey'
              ? `${query.from},${target},"${searchTerm}","${resultList[0].value}",${resultList[0].score}`
              : `${query.from},${target},"${searchTerm}","${resultList[0].value}"`;
        } else {
          body += resultList
            .map((r) =>
              target === 'InChIKey'
                ? `${query.from},${target},"${searchTerm}","${r.value}",${r.score}`
                : `${query.from},${target},"${searchTerm}","${r.value}"`
            )
            .join('\n');
        }
      }
    }

    if (scored) header += ',Score';
    return header + body;
  }

  private processTable(query: QueryLike, searchTerms: string[], results: ResultsMap, topHit: boolean): string {
    const targets = typeof query.to === 'string' ? [query.to] : query.to;
    let data = `${query.from},`;

    data += targets.map((t) => (t === 'InChIKey' ? `${t},Score` : t)).join(',') + '\n';

    data += searchTerms
      .map((searchTerm) => {
        return (
          `"${searchTerm}",` +
          targets
            .map((target) => {
              const resultList = results[searchTerm]?.[target] || [];
              const values = resultList.map((r) => r.value);
              const scores = resultList.filter((r) => r.score !== undefined).map((r) => r.score);

              let text = topHit ? `"${values[0] || ''}"` : `"${values.join('\n')}"`;
              if (scores.length > 0) {
                text += topHit ? `,"${scores[0]}"` : `,"${scores.join('\n')}"`;
              }
              return text;
            })
            .join(',')
        );
      })
      .join('\n');

    return data;
  }
}
