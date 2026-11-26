export type StatutAgence = 'ACTIVE' | 'FERMEE_TEMPORAIREMENT' | 'FERMEE_DEFINITIVEMENT';
export type TypePointRetrait = 'AEROPORT' | 'CENTRE_VILLE' | 'GARE' | 'PORT' | 'HOTEL';

export interface PointRetraitResponse {
  id: number;
  agenceId: number;
  agenceNom: string;
  agenceVille: string;
  nom: string;
  adresse: string;
  typePoint: TypePointRetrait;
  retraitAutorise: boolean;
  retourAutorise: boolean;
  autreVilleAutorise: boolean;
  actif: boolean;
  capaciteMax: number;
  instructionsAcces: string | null;
  latitude: number | null;
  longitude: number | null;
}

export interface PointRetraitRequest {
  nom: string;
  adresse: string;
  typePoint: TypePointRetrait;
  retraitAutorise: boolean;
  retourAutorise: boolean;
  autreVilleAutorise: boolean;
  actif: boolean;
  capaciteMax: number;
  instructionsAcces: string | null;
  latitude: number | null;
  longitude: number | null;
}

export interface PointRetraitParametresPatch {
  retraitAutorise?: boolean;
  retourAutorise?: boolean;
  autreVilleAutorise?: boolean;
  actif?: boolean;
  capaciteMax?: number;
}

export interface AgenceResponse {
  id: number;
  nom: string;
  adresse: string;
  ville: string;
  telephone: string;
  heureOuverture: string;
  heureFermeture: string;
  ouvert24h: boolean;
  joursOuverture: string;
  statut: StatutAgence;
  ouverteMaintenant: boolean;
  latitude: number | null;
  longitude: number | null;
  nombrePointsRetrait: number;
  pointsRetrait: PointRetraitResponse[];
}

export interface AgenceRequest {
  nom: string;
  adresse: string;
  ville: string;
  telephone: string;
  heureOuverture: string;
  heureFermeture: string;
  ouvert24h: boolean;
  joursOuverture: string;
  statut: StatutAgence;
  latitude: number | null;
  longitude: number | null;
}

export interface AgenceHorairesPatch {
  heureOuverture?: string;
  heureFermeture?: string;
  ouvert24h?: boolean;
  joursOuverture?: string;
}

export interface OuvertureResponse {
  agenceId: number;
  agenceNom: string;
  statut: StatutAgence;
  dateHeureVerifiee: string;
  ouverte: boolean;
  raison: string;
  heureOuverture: string;
  heureFermeture: string;
  ouvert24h: boolean;
  joursOuverture: string;
}

export interface RetourAutoriseResponse {
  agenceId: number;
  agenceNom: string;
  ville: string;
  agenceActive: boolean;
  retourAutorise: boolean;
  autreVilleAutorise: boolean;
  nombrePointsCompatibles: number;
  raison: string;
  pointsCompatibles: PointRetraitResponse[];
}

export interface AgenceProximiteResponse {
  distanceKm: number;
  agence: AgenceResponse;
}

export interface ReseauStatistiquesResponse {
  totalAgences: number;
  agencesActives: number;
  agencesFermeesTemporairement: number;
  agencesFermeesDefinitivement: number;
  totalPointsRetrait: number;
  pointsActifs: number;
  pointsRetraitAutorise: number;
  pointsRetourAutorise: number;
  pointsAutreVilleAutorise: number;
  capaciteTotaleActive: number;
  agencesParVille: Record<string, number>;
  pointsParType: Record<string, number>;
}

export interface ProblemDetail {
  type?: string;
  title?: string;
  status?: number;
  detail?: string;
  instance?: string;
  errors?: Record<string, string>;
}

export interface AgenceFilterParams {
  ville?: string;
  nom?: string;
  statut?: StatutAgence | '';
  ouvert24h?: boolean;
  ouverteMaintenant?: boolean;
  typePoint?: TypePointRetrait | '';
  retraitAutorise?: boolean;
  retourAutorise?: boolean;
  autreVilleAutorise?: boolean;
  sortBy?: string;
  direction?: 'asc' | 'desc';
}
