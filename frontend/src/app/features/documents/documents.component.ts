import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs';
import { DocumentStatus, DocumentType, EntityType, FleetDocument } from '../../core/models/fleet.model';
import { DocumentsService } from '../../core/services/documents.service';

@Component({
  selector: 'app-documents',
  imports: [ReactiveFormsModule],
  template: `
    <section class="page-heading">
      <span class="section-kicker">Dossier flotte</span>
      <h1>Documents</h1>
      <p>Deposez, recherchez et telechargez les documents lies aux clients, vehicules et paiements.</p>
    </section>

    <div class="module-grid">
      <section class="table-panel">
        <div class="panel-header">
          <div>
            <span class="section-kicker">Recherche</span>
            <h2>{{ documents().length }} document(s)</h2>
          </div>
        </div>

        <form class="toolbar-form" [formGroup]="searchForm" (ngSubmit)="search()">
          <select formControlName="entityType">
            @for (type of entityTypes; track type) {
              <option [value]="type">{{ type }}</option>
            }
          </select>
          <input formControlName="entityId" placeholder="ID client, vehicule ou paiement" />
          <select formControlName="documentType">
            <option value="">Tous les types</option>
            @for (type of documentTypes; track type) {
              <option [value]="type">{{ type }}</option>
            }
          </select>
          <button class="primary-button" type="submit" [disabled]="searchForm.invalid">Rechercher</button>
        </form>

        @if (message()) {
          <p class="success-message">{{ message() }}</p>
        }
        @if (error()) {
          <p class="error-message">{{ error() }}</p>
        }

        <div class="table-scroll">
          <table>
            <thead>
              <tr>
                <th>Fichier</th>
                <th>Type</th>
                <th>Statut</th>
                <th>Expiration</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              @for (document of documents(); track document.id) {
                <tr>
                  <td>
                    <strong>{{ document.originalName || document.fileName }}</strong>
                    <small>{{ document.entityType }} - {{ document.entityId }}</small>
                  </td>
                  <td>{{ document.documentType }}</td>
                  <td><span class="status-pill" [class]="statusClass(document.status)">{{ document.status }}</span></td>
                  <td>{{ document.expiryDate || '-' }}</td>
                  <td>
                    <button class="small-button" type="button" (click)="download(document)">Telecharger</button>
                  </td>
                </tr>
              } @empty {
                <tr>
                  <td colspan="5" class="empty-cell">Lancez une recherche pour afficher les documents.</td>
                </tr>
              }
            </tbody>
          </table>
        </div>
      </section>

      <section class="form-panel">
        <span class="section-kicker">Depot</span>
        <h2>Ajouter un document</h2>
        <form class="stack-form" [formGroup]="uploadForm" (ngSubmit)="upload()">
          <label>
            Fichier
            <input type="file" (change)="selectFile($event)" />
          </label>
          <label>
            Entite
            <select formControlName="entityType">
              @for (type of entityTypes; track type) {
                <option [value]="type">{{ type }}</option>
              }
            </select>
          </label>
          <label>ID associe <input formControlName="entityId" /></label>
          <label>
            Type de document
            <select formControlName="documentType">
              @for (type of documentTypes; track type) {
                <option [value]="type">{{ type }}</option>
              }
            </select>
          </label>
          <label>Date d'expiration <input type="date" formControlName="expiryDate" /></label>
          <button class="primary-button" type="submit" [disabled]="uploadForm.invalid || !selectedFile() || saving()">
            {{ saving() ? 'Depot...' : 'Deposer le document' }}
          </button>
        </form>
      </section>
    </div>

    <section class="table-panel">
      <div class="panel-header">
        <div>
          <span class="section-kicker">A surveiller</span>
          <h2>Documents bientot expires</h2>
        </div>
        <button class="ghost-button" type="button" (click)="loadExpiring()">Actualiser</button>
      </div>
      <div class="data-grid">
        @for (document of expiring(); track document.id) {
          <article class="mini-card">
            <strong>{{ document.originalName || document.fileName }}</strong>
            <span>{{ document.documentType }} - expiration {{ document.expiryDate || '-' }}</span>
          </article>
        } @empty {
          <p class="muted-text">Aucun document proche de l'expiration.</p>
        }
      </div>
    </section>
  `,
})
export class DocumentsComponent {
  private readonly fb = inject(FormBuilder);
  private readonly documentsService = inject(DocumentsService);

  readonly entityTypes: EntityType[] = ['CUSTOMER', 'VEHICLE', 'PAYMENT'];
  readonly documentTypes: DocumentType[] = ['INSURANCE', 'LICENSE', 'CONTRACT', 'INVOICE', 'VEHICLE_PHOTO', 'TECHNICAL_REPORT', 'REGISTRATION_CERTIFICATE', 'OTHER'];
  readonly documents = signal<FleetDocument[]>([]);
  readonly expiring = signal<FleetDocument[]>([]);
  readonly selectedFile = signal<File | null>(null);
  readonly saving = signal(false);
  readonly error = signal<string | null>(null);
  readonly message = signal<string | null>(null);

  readonly searchForm = this.fb.nonNullable.group({
    entityType: ['CUSTOMER' as EntityType, Validators.required],
    entityId: ['', Validators.required],
    documentType: [''],
  });

  readonly uploadForm = this.fb.nonNullable.group({
    entityType: ['CUSTOMER' as EntityType, Validators.required],
    entityId: ['', Validators.required],
    documentType: ['OTHER' as DocumentType, Validators.required],
    expiryDate: [''],
  });

  constructor() {
    this.loadExpiring();
  }

  search(): void {
    if (this.searchForm.invalid) {
      return;
    }
    const value = this.searchForm.getRawValue();
    this.documentsService.search(value.entityType, value.entityId, value.documentType as DocumentType | '').subscribe({
      next: (page) => this.documents.set(page.content ?? []),
      error: (error) => this.error.set(error?.error?.message ?? 'Recherche impossible.'),
    });
  }

  selectFile(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.selectedFile.set(input.files?.[0] ?? null);
  }

  upload(): void {
    const file = this.selectedFile();
    if (!file || this.uploadForm.invalid) {
      return;
    }
    const value = this.uploadForm.getRawValue();
    this.saving.set(true);
    this.documentsService
      .upload(file, value.entityType, value.entityId, value.documentType, value.expiryDate || undefined)
      .pipe(finalize(() => this.saving.set(false)))
      .subscribe({
        next: () => {
          this.message.set('Document depose.');
          this.selectedFile.set(null);
          this.searchForm.patchValue({ entityType: value.entityType, entityId: value.entityId, documentType: value.documentType });
          this.search();
          this.loadExpiring();
        },
        error: (error) => this.error.set(error?.error?.message ?? 'Depot du document impossible.'),
      });
  }

  loadExpiring(): void {
    this.documentsService.expiring().subscribe({
      next: (items) => this.expiring.set(items ?? []),
      error: () => this.expiring.set([]),
    });
  }

  download(document: FleetDocument): void {
    window.open(this.documentsService.downloadUrl(document.id), '_blank');
  }

  statusClass(status: string): string {
    return `status-${status.toLowerCase()}`;
  }
}
