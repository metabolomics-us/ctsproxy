import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

export interface ConversionResultItem {
  value: string;
  score?: number | string;
}

@Injectable({ providedIn: 'root' })
export class TranslationService {
  private readonly inchikeyOnlyConversions = ['PubChem CID', 'Pubchem SID'];
  private readonly hiddenFromValues = ['chemspider', 'smiles'];
  private readonly hiddenToValues = ['chemspider'];

  private readonly additionalInChIKeyToValues: Record<string, string> = {
    'Exact Mass': 'exactmass',
    'Molecular Formula': 'formula',
    'Molecular Weight': 'molweight',
  };

  constructor(private http: HttpClient) {}

  async getToValues(): Promise<string[]> {
    const data = await firstValueFrom(this.http.get<string[]>('/rest/toValues'));
    const filtered = data.filter((v) => !this.hiddenToValues.includes(v.toLowerCase()));
    return [...filtered, ...this.getAdditionalInChIKeyToValues()];
  }

  async getFromValues(): Promise<string[]> {
    const data = await firstValueFrom(this.http.get<string[]>('/rest/fromValues'));
    return data.filter((v) => !this.hiddenFromValues.includes(v.toLowerCase()));
  }

  getAdditionalInChIKeyToValues(): string[] {
    return Object.keys(this.additionalInChIKeyToValues);
  }

  getInChIKeyOnlyToValues(): string[] {
    return [...this.getAdditionalInChIKeyToValues(), ...this.inchikeyOnlyConversions];
  }

  async convert(from: string, to: string, searchTerm: string): Promise<ConversionResultItem[]> {
    if (to === 'InChIKey') {
      return this.convertWithScoring(from, searchTerm);
    } else if (from === 'InChIKey' && this.getAdditionalInChIKeyToValues().includes(to)) {
      return this.convertCompoundProperty(searchTerm, to);
    } else {
      return this.convertGeneral(from, to, searchTerm);
    }
  }

  private async convertWithScoring(from: string, searchTerm: string): Promise<ConversionResultItem[]> {
    const url = `/rest/score/${encodeURIComponent(from)}/${searchTerm}/biological`;
    const response = await firstValueFrom(this.http.get<any>(url));

    if (response?.result?.length > 0 && response.result[0].value !== 'no scoring done') {
      return response.result.map((res: any) => ({
        value: res.InChIKey,
        score: res.score,
      }));
    }
    return [{ value: 'No result', score: 'N/A' }];
  }

  private async convertCompoundProperty(inchikey: string, to: string): Promise<ConversionResultItem[]> {
    const response = await firstValueFrom(this.http.get<any>(`/service/compound/${inchikey}`));
    const prop = this.additionalInChIKeyToValues[to];

    if (response?.[prop] !== undefined) {
      return [{ value: response[prop] }];
    }
    return [{ value: 'No result' }];
  }

  private async convertGeneral(from: string, to: string, searchTerm: string): Promise<ConversionResultItem[]> {
    const url = `/rest/convert/${encodeURIComponent(from)}/${encodeURIComponent(to)}/${searchTerm}`;
    const response = await firstValueFrom(this.http.get<any[]>(url));

    if (response?.[0]?.results?.length > 0) {
      return response[0].results.map((res: string) => ({ value: res }));
    }
    return [{ value: 'No result' }];
  }
}
