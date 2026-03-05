import { Routes } from '@angular/router';
import { SingleConversionComponent } from './components/single-conversion/single-conversion.component';
import { BatchConversionComponent } from './components/batch-conversion/batch-conversion.component';
import { ServicesInfoComponent } from './components/services-info/services-info.component';

export const routes: Routes = [
  { path: '', component: SingleConversionComponent, title: 'Simple Conversion' },
  { path: 'batch', component: BatchConversionComponent, title: 'Batch Conversion' },
  { path: 'services', component: ServicesInfoComponent, title: 'REST Services' },
  { path: '**', redirectTo: '' },
];
