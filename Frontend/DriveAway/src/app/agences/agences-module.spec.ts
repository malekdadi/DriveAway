import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { AgencesModule } from './agences-module';
import { AgenceResponse, ReseauStatistiquesResponse } from './agence.models';

const MOCK_AGENCES: AgenceResponse[] = [
  {
    id: 1,
    nom: 'Tunis-Carthage',
    adresse: 'Aéroport International Tunis-Carthage',
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
        nom: 'Terminal 1',
        adresse: 'Hall Arrivées',
        typePoint: 'AEROPORT',
        retraitAutorise: true,
        retourAutorise: true,
        autreVilleAutorise: true,
        actif: true,
        capaciteMax: 40,
        instructionsAcces: 'Guichet n°4',
        latitude: 36.8512,
        longitude: 10.2275,
      },
    ],
  },
];

const MOCK_STATS: ReseauStatistiquesResponse = {
  totalAgences: 1,
  agencesActives: 1,
  agencesFermeesTemporairement: 0,
  agencesFermeesDefinitivement: 0,
  totalPointsRetrait: 1,
  pointsActifs: 1,
  pointsRetraitAutorise: 1,
  pointsRetourAutorise: 1,
  pointsAutreVilleAutorise: 1,
  capaciteTotaleActive: 40,
  agencesParVille: { Tunis: 1 },
  pointsParType: { AEROPORT: 1 },
};

describe('AgencesModule', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [AgencesModule],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
  });

  it('loads agencies and statistics from agence-service API and displays RFC 9457 conflict errors', () => {
    const httpMock = TestBed.inject(HttpTestingController);
    const fixture = TestBed.createComponent(AgencesModule);
    fixture.detectChanges();

    const agencesReq = httpMock.expectOne(req => req.url === '/api/agences');
    expect(agencesReq.request.method).toBe('GET');
    agencesReq.flush(MOCK_AGENCES);

    const statsReq = httpMock.expectOne('/api/agences/statistiques');
    expect(statsReq.request.method).toBe('GET');
    statsReq.flush(MOCK_STATS);

    fixture.detectChanges();
    const el = fixture.nativeElement as HTMLElement;
    expect(el.textContent).toContain('API connectée');
    expect(el.textContent).toContain('Tunis-Carthage');
    expect(el.textContent).toContain('Terminal 1');
    expect(el.textContent).toContain('40 places');

    // Switch to Admin tab and trigger delete -> mock 409 Conflict ProblemDetail
    const tabs = Array.from(el.querySelectorAll<HTMLButtonElement>('[role="tab"]'));
    tabs[1].click();
    fixture.detectChanges();

    const deleteBtn = Array.from(el.querySelectorAll<HTMLButtonElement>('button')).find(
      b => b.textContent?.trim() === 'Supprimer',
    )!;
    deleteBtn.click();

    const delReq = httpMock.expectOne('/api/agences/1');
    expect(delReq.request.method).toBe('DELETE');
    delReq.flush(
      {
        type: 'about:blank',
        title: 'Conflict',
        status: 409,
        detail: "Impossible de supprimer l'agence car elle possède encore 1 point(s) de retrait.",
        instance: '/api/agences/1',
      },
      { status: 409, statusText: 'Conflict' },
    );

    fixture.detectChanges();
    expect(el.textContent).toContain('HTTP 409');
    expect(el.textContent).toContain('point(s) de retrait');
    httpMock.verify();
  });
});
