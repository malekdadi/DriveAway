import { CommonModule } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AgenceApiService } from './agence-api.service';
import {
  AgenceFilterParams,
  AgenceRequest,
  AgenceResponse,
  OuvertureResponse,
  PointRetraitRequest,
  PointRetraitResponse,
  ProblemDetail,
  ReseauStatistiquesResponse,
  RetourAutoriseResponse,
  StatutAgence,
  TypePointRetrait,
} from './agence.models';

const FALLBACK_AGENCES: AgenceResponse[] = [
  {
    id: 1,
    nom: 'Tunis-Carthage',
    adresse: 'Aéroport International Tunis-Carthage, Zone Arrivées',
    ville: 'Tunis',
    telephone: '+216 71 755 000',
    heureOuverture: '00:00:00',
    heureFermeture: '23:59:59',
    ouvert24h: true,
    joursOuverture: 'LUN,MAR,MER,JEU,VEN,SAM,DIM',
    statut: 'ACTIVE',
    ouverteMaintenant: true,
    latitude: 36.851,
    longitude: 10.2272,
    nombrePointsRetrait: 1,
    pointsRetrait: [
      {
        id: 1,
        agenceId: 1,
        agenceNom: 'Tunis-Carthage',
        agenceVille: 'Tunis',
        nom: 'Terminal 1 — Hall Arrivées',
        adresse: 'Aéroport Tunis-Carthage, Terminal 1, Guichet n°4',
        typePoint: 'AEROPORT',
        retraitAutorise: true,
        retourAutorise: true,
        autreVilleAutorise: true,
        actif: true,
        capaciteMax: 40,
        instructionsAcces: 'À droite après la sortie douane, parking P1.',
        latitude: 36.8512,
        longitude: 10.2275,
      },
    ],
  },
  {
    id: 2,
    nom: 'La Marsa',
    adresse: '14 Avenue Habib Bourguiba, La Marsa',
    ville: 'Tunis',
    telephone: '+216 71 742 310',
    heureOuverture: '08:00:00',
    heureFermeture: '20:00:00',
    ouvert24h: false,
    joursOuverture: 'LUN,MAR,MER,JEU,VEN,SAM',
    statut: 'ACTIVE',
    ouverteMaintenant: true,
    latitude: 36.8784,
    longitude: 10.3247,
    nombrePointsRetrait: 1,
    pointsRetrait: [
      {
        id: 3,
        agenceId: 2,
        agenceNom: 'La Marsa',
        agenceVille: 'Tunis',
        nom: 'Agence Corniche La Marsa',
        adresse: '14 Avenue Habib Bourguiba, La Marsa Plage',
        typePoint: 'CENTRE_VILLE',
        retraitAutorise: true,
        retourAutorise: true,
        autreVilleAutorise: true,
        actif: true,
        capaciteMax: 18,
        instructionsAcces: 'Stationnement réservé devant l’agence.',
        latitude: 36.8784,
        longitude: 10.3247,
      },
    ],
  },
  {
    id: 3,
    nom: 'Sousse',
    adresse: 'Avenue de la Corniche, Boujaafar',
    ville: 'Sousse',
    telephone: '+216 73 228 450',
    heureOuverture: '08:00:00',
    heureFermeture: '19:00:00',
    ouvert24h: false,
    joursOuverture: 'LUN,MAR,MER,JEU,VEN,SAM',
    statut: 'ACTIVE',
    ouverteMaintenant: true,
    latitude: 35.8256,
    longitude: 10.6369,
    nombrePointsRetrait: 1,
    pointsRetrait: [
      {
        id: 5,
        agenceId: 3,
        agenceNom: 'Sousse',
        agenceVille: 'Sousse',
        nom: 'Corniche Boujaafar',
        adresse: 'Avenue de la Corniche, face hôtel Riadh Palms',
        typePoint: 'CENTRE_VILLE',
        retraitAutorise: true,
        retourAutorise: true,
        autreVilleAutorise: true,
        actif: true,
        capaciteMax: 22,
        instructionsAcces: 'Guichet principal au rez-de-chaussée.',
        latitude: 35.8256,
        longitude: 10.6369,
      },
    ],
  },
];

@Component({
  selector: 'app-agences-module',
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './agences-module.html',
  styleUrl: './agences-module.css',
})
export class AgencesModule implements OnInit {
  private readonly api = inject(AgenceApiService);

  protected readonly agences = signal<AgenceResponse[]>(FALLBACK_AGENCES);
  protected readonly stats = signal<ReseauStatistiquesResponse | null>(null);
  protected readonly backendConnected = signal(false);
  protected readonly loading = signal(false);
  protected readonly activeTab = signal<'client' | 'admin'>('client');

  protected readonly problem = signal<ProblemDetail | null>(null);
  protected readonly successMessage = signal('');

  // Filtres client (query strings)
  protected filterVille = '';
  protected filterNom = '';
  protected filterStatut: StatutAgence | '' = '';
  protected filterTypePoint: TypePointRetrait | '' = '';
  protected filterOuverteMaintenant = false;
  protected filterAutreVille = false;
  protected sortBy = 'nom';
  protected sortDirection: 'asc' | 'desc' = 'asc';

  // Proximite GPS (Haversine)
  protected proxLat = 36.851;
  protected proxLng = 10.2272;
  protected proxRayonKm = 25;
  protected readonly distancesByAgenceId = signal<Record<number, number>>({});
  protected readonly proximiteActive = signal(false);

  // Verifications metiers simples par agence
  protected checkDateHeure = '2026-11-25T14:30';
  protected readonly ouvertureResults = signal<Record<number, OuvertureResponse>>({});
  protected readonly retourResults = signal<Record<number, RetourAutoriseResponse>>({});

  // Formulaire CRUD Agence
  protected editingAgenceId: number | null = null;
  protected agenceForm: AgenceRequest = this.emptyAgenceForm();

  // Formulaire PATCH Horaires
  protected horairesTargetId: number | null = null;
  protected horairesForm = {
    heureOuverture: '08:00',
    heureFermeture: '19:00',
    ouvert24h: false,
    joursOuverture: 'LUN,MAR,MER,JEU,VEN,SAM',
  };

  // Formulaire CRUD Point de retrait
  protected pointAgenceId: number | null = null;
  protected editingPointId: number | null = null;
  protected pointForm: PointRetraitRequest = this.emptyPointForm();

  protected readonly statuts: StatutAgence[] = ['ACTIVE', 'FERMEE_TEMPORAIREMENT', 'FERMEE_DEFINITIVEMENT'];
  protected readonly typesPoint: TypePointRetrait[] = ['AEROPORT', 'CENTRE_VILLE', 'GARE', 'PORT', 'HOTEL'];

  protected readonly problemErrorsList = computed(() => {
    const p = this.problem();
    if (!p?.errors) return [];
    return Object.entries(p.errors);
  });

  ngOnInit(): void {
    if (this.api.isHttpAvailable) {
      this.refreshAll();
    }
  }

  protected refreshAll(): void {
    this.clearMessages();
    this.loadAgences();
    this.loadStatistiques();
  }

  protected loadAgences(): void {
    if (!this.api.isHttpAvailable) return;
    this.loading.set(true);
    this.proximiteActive.set(false);
    this.distancesByAgenceId.set({});

    const params: AgenceFilterParams = {
      ville: this.filterVille,
      nom: this.filterNom,
      statut: this.filterStatut,
      typePoint: this.filterTypePoint,
      ouverteMaintenant: this.filterOuverteMaintenant ? true : undefined,
      autreVilleAutorise: this.filterAutreVille ? true : undefined,
      sortBy: this.sortBy,
      direction: this.sortDirection,
    };

    this.api.findAll(params).subscribe({
      next: list => {
        this.agences.set(list);
        this.backendConnected.set(true);
        this.loading.set(false);
        if (this.pointAgenceId === null && list.length > 0) {
          this.pointAgenceId = list[0].id;
        }
      },
      error: err => {
        this.loading.set(false);
        this.backendConnected.set(false);
        this.agences.set(this.filterFallback());
        const pd = this.api.extractProblem(err);
        if (pd.status !== 0) {
          this.problem.set(pd);
        }
      },
    });
  }

  protected loadStatistiques(): void {
    if (!this.api.isHttpAvailable) return;
    this.api.getStatistiques().subscribe({
      next: s => this.stats.set(s),
      error: () => this.stats.set(null),
    });
  }

  protected resetFilters(): void {
    this.filterVille = '';
    this.filterNom = '';
    this.filterStatut = '';
    this.filterTypePoint = '';
    this.filterOuverteMaintenant = false;
    this.filterAutreVille = false;
    this.sortBy = 'nom';
    this.sortDirection = 'asc';
    this.loadAgences();
  }

  protected searchProximite(): void {
    if (!this.api.isHttpAvailable) return;
    this.clearMessages();
    this.loading.set(true);
    this.api.findProximite(this.proxLat, this.proxLng, this.proxRayonKm, true).subscribe({
      next: items => {
        const map: Record<number, number> = {};
        for (const item of items) {
          map[item.agence.id] = item.distanceKm;
        }
        this.distancesByAgenceId.set(map);
        this.agences.set(items.map(i => i.agence));
        this.proximiteActive.set(true);
        this.backendConnected.set(true);
        this.loading.set(false);
      },
      error: err => {
        this.loading.set(false);
        this.problem.set(this.api.extractProblem(err));
      },
    });
  }

  protected useBrowserLocation(): void {
    if (typeof navigator === 'undefined' || !navigator.geolocation) {
      this.problem.set({ title: 'Géolocalisation indisponible', detail: 'Utilisez les coordonnées manuelles ci-dessous.' });
      return;
    }
    navigator.geolocation.getCurrentPosition(
      pos => {
        this.proxLat = Number(pos.coords.latitude.toFixed(4));
        this.proxLng = Number(pos.coords.longitude.toFixed(4));
        this.searchProximite();
      },
      () => this.problem.set({ title: 'Position refusée', detail: 'Saisissez une latitude/longitude ou utilisez Tunis-Carthage par défaut.' }),
    );
  }

  protected checkOuverture(agenceId: number): void {
    if (!this.api.isHttpAvailable) return;
    this.clearMessages();
    const iso = this.checkDateHeure ? `${this.checkDateHeure}:00` : undefined;
    this.api.verifierOuverture(agenceId, iso).subscribe({
      next: res => this.ouvertureResults.update(prev => ({ ...prev, [agenceId]: res })),
      error: err => this.problem.set(this.api.extractProblem(err)),
    });
  }

  protected checkRetour(agenceId: number, autreVille: boolean): void {
    if (!this.api.isHttpAvailable) return;
    this.clearMessages();
    this.api.verifierRetourAutorise(agenceId, autreVille).subscribe({
      next: res => this.retourResults.update(prev => ({ ...prev, [agenceId]: res })),
      error: err => this.problem.set(this.api.extractProblem(err)),
    });
  }

  // ==================== CRUD Agences ====================

  protected startEditAgence(agence: AgenceResponse): void {
    this.clearMessages();
    this.editingAgenceId = agence.id;
    this.agenceForm = {
      nom: agence.nom,
      adresse: agence.adresse,
      ville: agence.ville,
      telephone: agence.telephone,
      heureOuverture: this.toTimeInput(agence.heureOuverture),
      heureFermeture: this.toTimeInput(agence.heureFermeture),
      ouvert24h: agence.ouvert24h,
      joursOuverture: agence.joursOuverture,
      statut: agence.statut,
      latitude: agence.latitude,
      longitude: agence.longitude,
    };
    this.activeTab.set('admin');
  }

  protected cancelEditAgence(): void {
    this.editingAgenceId = null;
    this.agenceForm = this.emptyAgenceForm();
  }

  protected submitAgence(): void {
    if (!this.api.isHttpAvailable) return;
    this.clearMessages();
    const payload: AgenceRequest = {
      ...this.agenceForm,
      heureOuverture: this.normalizeTime(this.agenceForm.heureOuverture),
      heureFermeture: this.normalizeTime(this.agenceForm.heureFermeture),
      latitude: this.toNullableNumber(this.agenceForm.latitude),
      longitude: this.toNullableNumber(this.agenceForm.longitude),
    };

    const obs = this.editingAgenceId !== null
      ? this.api.updateAgence(this.editingAgenceId, payload)
      : this.api.createAgence(payload);

    obs.subscribe({
      next: saved => {
        this.successMessage.set(
          this.editingAgenceId !== null
            ? `Agence « ${saved.nom} » mise à jour (200 OK).`
            : `Agence « ${saved.nom} » créée avec succès (201 Created).`,
        );
        this.cancelEditAgence();
        this.loadAgences();
        this.loadStatistiques();
      },
      error: err => this.problem.set(this.api.extractProblem(err)),
    });
  }

  protected changeStatut(agence: AgenceResponse, statut: StatutAgence): void {
    if (!this.api.isHttpAvailable) return;
    this.clearMessages();
    this.api.updateStatut(agence.id, statut).subscribe({
      next: updated => {
        this.successMessage.set(`Statut de « ${updated.nom} » passé à ${updated.statut} (PATCH 200 OK).`);
        this.loadAgences();
        this.loadStatistiques();
      },
      error: err => this.problem.set(this.api.extractProblem(err)),
    });
  }

  protected openHorairesEditor(agence: AgenceResponse): void {
    this.horairesTargetId = agence.id;
    this.horairesForm = {
      heureOuverture: this.toTimeInput(agence.heureOuverture),
      heureFermeture: this.toTimeInput(agence.heureFermeture),
      ouvert24h: agence.ouvert24h,
      joursOuverture: agence.joursOuverture,
    };
  }

  protected submitHorairesPatch(): void {
    if (!this.api.isHttpAvailable || this.horairesTargetId === null) return;
    this.clearMessages();
    this.api.updateHoraires(this.horairesTargetId, {
      heureOuverture: this.normalizeTime(this.horairesForm.heureOuverture),
      heureFermeture: this.normalizeTime(this.horairesForm.heureFermeture),
      ouvert24h: this.horairesForm.ouvert24h,
      joursOuverture: this.horairesForm.joursOuverture,
    }).subscribe({
      next: updated => {
        this.successMessage.set(`Horaires de « ${updated.nom} » mis à jour (PATCH 200 OK).`);
        this.horairesTargetId = null;
        this.loadAgences();
      },
      error: err => this.problem.set(this.api.extractProblem(err)),
    });
  }

  protected removeAgence(agence: AgenceResponse): void {
    if (!this.api.isHttpAvailable) return;
    this.clearMessages();
    this.api.deleteAgence(agence.id).subscribe({
      next: () => {
        this.successMessage.set(`Agence « ${agence.nom} » supprimée (204 No Content).`);
        this.loadAgences();
        this.loadStatistiques();
      },
      error: err => this.problem.set(this.api.extractProblem(err)),
    });
  }

  // ==================== CRUD Points de retrait ====================

  protected startEditPoint(point: PointRetraitResponse): void {
    this.clearMessages();
    this.pointAgenceId = point.agenceId;
    this.editingPointId = point.id;
    this.pointForm = {
      nom: point.nom,
      adresse: point.adresse,
      typePoint: point.typePoint,
      retraitAutorise: point.retraitAutorise,
      retourAutorise: point.retourAutorise,
      autreVilleAutorise: point.autreVilleAutorise,
      actif: point.actif,
      capaciteMax: point.capaciteMax,
      instructionsAcces: point.instructionsAcces,
      latitude: point.latitude,
      longitude: point.longitude,
    };
  }

  protected cancelEditPoint(): void {
    this.editingPointId = null;
    this.pointForm = this.emptyPointForm();
  }

  protected submitPoint(): void {
    if (!this.api.isHttpAvailable || this.pointAgenceId === null) return;
    this.clearMessages();
    const payload: PointRetraitRequest = {
      ...this.pointForm,
      latitude: this.toNullableNumber(this.pointForm.latitude),
      longitude: this.toNullableNumber(this.pointForm.longitude),
    };

    const obs = this.editingPointId !== null
      ? this.api.updatePoint(this.pointAgenceId, this.editingPointId, payload)
      : this.api.createPoint(this.pointAgenceId, payload);

    obs.subscribe({
      next: saved => {
        this.successMessage.set(
          this.editingPointId !== null
            ? `Point « ${saved.nom} » mis à jour (200 OK).`
            : `Point « ${saved.nom} » ajouté à l’agence (201 Created).`,
        );
        this.cancelEditPoint();
        this.loadAgences();
        this.loadStatistiques();
      },
      error: err => this.problem.set(this.api.extractProblem(err)),
    });
  }

  protected togglePointFlag(point: PointRetraitResponse, field: 'retraitAutorise' | 'retourAutorise' | 'autreVilleAutorise' | 'actif'): void {
    if (!this.api.isHttpAvailable) return;
    this.clearMessages();
    this.api.updatePointParametres(point.agenceId, point.id, { [field]: !point[field] }).subscribe({
      next: updated => {
        this.successMessage.set(`Paramètre « ${field} » mis à jour sur « ${updated.nom} » (PATCH 200 OK).`);
        this.loadAgences();
        this.loadStatistiques();
      },
      error: err => this.problem.set(this.api.extractProblem(err)),
    });
  }

  protected removePoint(point: PointRetraitResponse): void {
    if (!this.api.isHttpAvailable) return;
    this.clearMessages();
    this.api.deletePoint(point.agenceId, point.id).subscribe({
      next: () => {
        this.successMessage.set(`Point « ${point.nom} » supprimé (204 No Content).`);
        this.loadAgences();
        this.loadStatistiques();
      },
      error: err => this.problem.set(this.api.extractProblem(err)),
    });
  }

  // ==================== Helpers d'affichage ====================

  protected iconForAgence(agence: AgenceResponse): string {
    const firstType = agence.pointsRetrait[0]?.typePoint;
    if (firstType === 'AEROPORT') return 'flight';
    if (firstType === 'PORT') return 'directions_boat';
    if (firstType === 'GARE') return 'train';
    if (firstType === 'HOTEL') return 'hotel';
    return 'location_city';
  }

  protected formatHours(agence: AgenceResponse): string {
    if (agence.ouvert24h) return 'Ouverte 24h/24';
    return `${agence.heureOuverture.slice(0, 5)} – ${agence.heureFermeture.slice(0, 5)}`;
  }

  protected statusLabel(statut: StatutAgence): string {
    if (statut === 'ACTIVE') return 'Active';
    if (statut === 'FERMEE_TEMPORAIREMENT') return 'Fermée temporairement';
    return 'Fermée définitivement';
  }

  private clearMessages(): void {
    this.problem.set(null);
    this.successMessage.set('');
  }

  private filterFallback(): AgenceResponse[] {
    return FALLBACK_AGENCES.filter(a => {
      if (this.filterVille && !a.ville.toLowerCase().includes(this.filterVille.toLowerCase())) return false;
      if (this.filterNom && !a.nom.toLowerCase().includes(this.filterNom.toLowerCase())) return false;
      if (this.filterStatut && a.statut !== this.filterStatut) return false;
      return true;
    });
  }

  private emptyAgenceForm(): AgenceRequest {
    return {
      nom: '',
      adresse: '',
      ville: '',
      telephone: '+216 ',
      heureOuverture: '08:00',
      heureFermeture: '19:00',
      ouvert24h: false,
      joursOuverture: 'LUN,MAR,MER,JEU,VEN,SAM',
      statut: 'ACTIVE',
      latitude: null,
      longitude: null,
    };
  }

  private emptyPointForm(): PointRetraitRequest {
    return {
      nom: '',
      adresse: '',
      typePoint: 'CENTRE_VILLE',
      retraitAutorise: true,
      retourAutorise: true,
      autreVilleAutorise: false,
      actif: true,
      capaciteMax: 20,
      instructionsAcces: '',
      latitude: null,
      longitude: null,
    };
  }

  private toTimeInput(value: string): string {
    return value ? value.slice(0, 5) : '08:00';
  }

  private normalizeTime(value: string): string {
    if (!value) return '08:00:00';
    return value.length === 5 ? `${value}:00` : value;
  }

  private toNullableNumber(value: unknown): number | null {
    if (value === '' || value === null || value === undefined) return null;
    const n = Number(value);
    return Number.isFinite(n) ? n : null;
  }
}
