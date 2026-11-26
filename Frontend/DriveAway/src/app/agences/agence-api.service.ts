import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import {
  AgenceFilterParams,
  AgenceHorairesPatch,
  AgenceProximiteResponse,
  AgenceRequest,
  AgenceResponse,
  OuvertureResponse,
  PointRetraitParametresPatch,
  PointRetraitRequest,
  PointRetraitResponse,
  ProblemDetail,
  ReseauStatistiquesResponse,
  RetourAutoriseResponse,
  StatutAgence,
} from './agence.models';

@Injectable({ providedIn: 'root' })
export class AgenceApiService {
  private readonly http = inject(HttpClient, { optional: true });
  private readonly baseUrl = '/api/agences';

  get isHttpAvailable(): boolean {
    return this.http !== null;
  }

  private client(): HttpClient {
    if (!this.http) {
      throw new Error('HttpClient indisponible');
    }
    return this.http;
  }

  findAll(filters: AgenceFilterParams = {}): Observable<AgenceResponse[]> {
    let params = new HttpParams();
    if (filters.ville?.trim()) params = params.set('ville', filters.ville.trim());
    if (filters.nom?.trim()) params = params.set('nom', filters.nom.trim());
    if (filters.statut) params = params.set('statut', filters.statut);
    if (filters.ouvert24h !== undefined) params = params.set('ouvert24h', String(filters.ouvert24h));
    if (filters.ouverteMaintenant !== undefined) params = params.set('ouverteMaintenant', String(filters.ouverteMaintenant));
    if (filters.typePoint) params = params.set('typePoint', filters.typePoint);
    if (filters.retraitAutorise !== undefined) params = params.set('retraitAutorise', String(filters.retraitAutorise));
    if (filters.retourAutorise !== undefined) params = params.set('retourAutorise', String(filters.retourAutorise));
    if (filters.autreVilleAutorise !== undefined) params = params.set('autreVilleAutorise', String(filters.autreVilleAutorise));
    if (filters.sortBy) params = params.set('sortBy', filters.sortBy);
    if (filters.direction) params = params.set('direction', filters.direction);

    return this.client().get<AgenceResponse[]>(this.baseUrl, { params });
  }

  createAgence(request: AgenceRequest): Observable<AgenceResponse> {
    return this.client().post<AgenceResponse>(this.baseUrl, request);
  }

  updateAgence(id: number, request: AgenceRequest): Observable<AgenceResponse> {
    return this.client().put<AgenceResponse>(`${this.baseUrl}/${id}`, request);
  }

  deleteAgence(id: number): Observable<void> {
    return this.client().delete<void>(`${this.baseUrl}/${id}`);
  }

  updateHoraires(id: number, patch: AgenceHorairesPatch): Observable<AgenceResponse> {
    return this.client().patch<AgenceResponse>(`${this.baseUrl}/${id}/horaires`, patch);
  }

  updateStatut(id: number, statut: StatutAgence): Observable<AgenceResponse> {
    return this.client().patch<AgenceResponse>(`${this.baseUrl}/${id}/statut`, { statut });
  }

  verifierOuverture(id: number, dateHeure?: string): Observable<OuvertureResponse> {
    let params = new HttpParams();
    if (dateHeure) params = params.set('dateHeure', dateHeure);
    return this.client().get<OuvertureResponse>(`${this.baseUrl}/${id}/ouverture`, { params });
  }

  verifierRetourAutorise(id: number, autreVille = false): Observable<RetourAutoriseResponse> {
    const params = new HttpParams().set('autreVille', String(autreVille));
    return this.client().get<RetourAutoriseResponse>(`${this.baseUrl}/${id}/retour-autorise`, { params });
  }

  findProximite(latitude: number, longitude: number, rayonKm = 25, activeUniquement = true): Observable<AgenceProximiteResponse[]> {
    const params = new HttpParams()
      .set('latitude', String(latitude))
      .set('longitude', String(longitude))
      .set('rayonKm', String(rayonKm))
      .set('activeUniquement', String(activeUniquement));
    return this.client().get<AgenceProximiteResponse[]>(`${this.baseUrl}/proximite`, { params });
  }

  getStatistiques(): Observable<ReseauStatistiquesResponse> {
    return this.client().get<ReseauStatistiquesResponse>(`${this.baseUrl}/statistiques`);
  }

  createPoint(agenceId: number, request: PointRetraitRequest): Observable<PointRetraitResponse> {
    return this.client().post<PointRetraitResponse>(`${this.baseUrl}/${agenceId}/points`, request);
  }

  updatePoint(agenceId: number, pointId: number, request: PointRetraitRequest): Observable<PointRetraitResponse> {
    return this.client().put<PointRetraitResponse>(`${this.baseUrl}/${agenceId}/points/${pointId}`, request);
  }

  updatePointParametres(agenceId: number, pointId: number, patch: PointRetraitParametresPatch): Observable<PointRetraitResponse> {
    return this.client().patch<PointRetraitResponse>(`${this.baseUrl}/${agenceId}/points/${pointId}/parametres`, patch);
  }

  deletePoint(agenceId: number, pointId: number): Observable<void> {
    return this.client().delete<void>(`${this.baseUrl}/${agenceId}/points/${pointId}`);
  }

  extractProblem(error: unknown): ProblemDetail {
    if (error instanceof HttpErrorResponse) {
      if (error.error && typeof error.error === 'object' && ('detail' in error.error || 'title' in error.error)) {
        return error.error as ProblemDetail;
      }
      if (error.status === 0 || error.status === 500 || error.status === 502 || error.status === 503 || error.status === 504) {
        return {
          status: 0,
          title: 'Service injoignable',
          detail: 'Vérifiez que eureka-server (:8761), api-gateway (:8090) et agence-service (:8085) sont démarrés.',
        };
      }
      return { status: error.status, title: error.statusText || 'Erreur HTTP', detail: error.message };
    }
    return { title: 'Erreur', detail: 'Une erreur inattendue est survenue.' };
  }

  unavailable(): Observable<never> {
    return throwError(() => new Error('API indisponible'));
  }
}
