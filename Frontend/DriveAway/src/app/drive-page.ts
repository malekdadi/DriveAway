import { NgTemplateOutlet } from '@angular/common';
import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import macanStudio from '../../public/media/macan-studio.jpg';
import { AgencesModule } from './agences/agences-module';

export const services = [
  { path: 'utilisateurs', icon: 'person', title: 'Espace client', description: 'Créez votre compte client.' },
  { path: 'vehicules', icon: 'directions_car', title: 'Notre flotte', description: 'Comparez les modèles et les tarifs à la journée.' },
  { path: 'reservations', icon: 'calendar_month', title: 'Mes réservations', description: 'Choisissez vos dates et préparez votre contrat.' },
  { path: 'facturation', icon: 'receipt_long', title: 'Mes factures', description: 'Retrouvez le récapitulatif de votre réservation.' },
  { path: 'agences', icon: 'location_on', title: 'Nos agences', description: 'Choisissez votre point de retrait en Tunisie.' },
  { path: 'maintenance', icon: 'build', title: 'Service entretien', description: 'Planifiez un rendez-vous pour votre véhicule.' },
];

const featuredVehicle = { name: 'Porsche Macan', category: 'SUV', price: 850, image: 'macan', transmission: 'Automatique', seats: 5, premium: true };

const vehicles = [
  { name: 'Renault Clio', category: 'Citadine', price: 145, image: 'clio', transmission: 'Manuelle', seats: 5, premium: false },
  { name: 'Peugeot 3008', category: 'SUV', price: 285, image: '3008', transmission: 'Automatique', seats: 5, premium: false },
  { name: 'Volkswagen Golf', category: 'Compacte', price: 215, image: 'golf', transmission: 'Automatique', seats: 5, premium: false },
  { name: 'Mercedes-Benz Classe E', category: 'Berline', price: 490, image: 'classe-e', transmission: 'Automatique', seats: 5, premium: true },
  { name: 'BMW X5', category: 'SUV', price: 690, image: 'x5', transmission: 'Automatique', seats: 5, premium: true },
  featuredVehicle,
  { name: 'Range Rover Sport', category: 'SUV', price: 990, image: 'range-rover', transmission: 'Automatique', seats: 5, premium: true },
];

const agencies = [
  { name: 'Tunis-Carthage', area: 'Aéroport · Grand Tunis', icon: 'flight' },
  { name: 'La Marsa', area: 'Centre-ville · Grand Tunis', icon: 'location_city' },
  { name: 'Sousse', area: 'Centre-ville · Sahel', icon: 'location_on' },
];

interface Booking {
  vehicle: string;
  agency: string;
  start: string;
  end: string;
  days: number;
  total: number;
  reference: string;
}

let demoBooking: Booking | undefined;

const dayInMilliseconds = 86_400_000;

function parseRentalDate(value: string): number {
  if (!/^\d{4}-\d{2}-\d{2}$/.test(value)) return NaN;
  const date = Date.parse(value);
  return Number.isFinite(date) && new Date(date).toISOString().slice(0, 10) === value ? date : NaN;
}

@Component({
  imports: [RouterLink, FormsModule, NgTemplateOutlet, AgencesModule],
  templateUrl: './drive-page.html',
})
export class DrivePage {
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  protected readonly page = this.route.snapshot.data['page'] as string;
  protected readonly service = services.find(item => item.path === this.page);
  protected readonly vehicles = vehicles;
  protected readonly featuredVehicle = featuredVehicle;
  protected readonly agencies = agencies;
  protected get studioPhoto() {
    return macanStudio;
  }
  protected readonly categories = ['Toutes', 'Prestige', ...new Set(vehicles.map(vehicle => vehicle.category))];
  protected readonly prestigeCount = vehicles.filter(vehicle => vehicle.premium).length;
  protected readonly automaticCount = vehicles.filter(vehicle => vehicle.transmission === 'Automatique').length;
  protected readonly fleetByCategory = this.categories.slice(2).map(category => ({
    category,
    count: vehicles.filter(vehicle => vehicle.category === category).length,
  })).sort((a, b) => b.count - a.count);
  protected category = 'Toutes';
  protected sort = 'selection';
  protected agency = agencies[0].name;
  protected vehicle = vehicles[0].image;
  protected start = '';
  protected end = '';
  protected readonly today = new Intl.DateTimeFormat('fr-CA', { timeZone: 'Africa/Tunis', year: 'numeric', month: '2-digit', day: '2-digit' }).format(new Date());
  protected notice = '';
  protected booking = demoBooking;

  constructor() {
    const changeDetector = inject(ChangeDetectorRef);
    this.route.queryParamMap.pipe(takeUntilDestroyed()).subscribe(params => {
      if (this.page === 'vehicules') {
        this.category = this.categories.find(item => item === params.get('categorie')) ?? this.categories[0];
      }
      this.agency = agencies.find(item => item.name === params.get('agence'))?.name ?? agencies[0].name;
      this.vehicle = vehicles.find(item => item.image === params.get('voiture'))?.image ?? vehicles[0].image;
      const start = params.get('depart') ?? '';
      const end = params.get('retour') ?? '';
      this.start = Number.isFinite(parseRentalDate(start)) ? start : '';
      this.end = Number.isFinite(parseRentalDate(end)) ? end : '';
      this.notice = '';
      changeDetector.markForCheck();
    });
  }

  protected get filteredVehicles() {
    const matching = vehicles.filter(vehicle => this.category === 'Toutes'
      || (this.category === 'Prestige' ? vehicle.premium : vehicle.category === this.category));

    if (this.sort === 'price-asc') return matching.sort((a, b) => a.price - b.price);
    if (this.sort === 'price-desc') return matching.sort((a, b) => b.price - a.price);
    return matching.sort((a, b) => Number(Boolean(b.premium)) - Number(Boolean(a.premium)));
  }

  protected vehiclePhoto(vehicle: (typeof vehicles)[number]) {
    return 'media/' + vehicle.image + '.jpg';
  }

  protected get days() {
    return (parseRentalDate(this.end) - parseRentalDate(this.start)) / dayInMilliseconds;
  }

  protected get earliestReturn() {
    const start = parseRentalDate(this.start);
    if (!Number.isFinite(start) || this.start < this.today) return this.today;
    return new Date(start + dayInMilliseconds).toISOString().slice(0, 10);
  }

  protected get total() {
    return this.start >= this.today && this.days > 0 ? this.days * this.selectedVehicle.price : 0;
  }

  protected get selectedVehicle() {
    return vehicles.find(vehicle => vehicle.image === this.vehicle) ?? vehicles[0];
  }

  protected prepareBooking() {
    return this.router.navigate(['/reservations'], {
      queryParams: { agence: this.agency, depart: this.start, retour: this.end },
    });
  }

  protected reserve() {
    if (this.start < this.today) {
      this.notice = 'Choisissez une date de départ à partir d’aujourd’hui.';
      return;
    }
    if (!(this.days > 0)) {
      this.notice = 'Choisissez une date de retour après la date de départ.';
      return;
    }
    demoBooking = {
      vehicle: this.selectedVehicle.name, agency: this.agency, start: this.start, end: this.end,
      days: this.days, total: this.total, reference: 'DA-' + Date.now().toString(36).toUpperCase(),
    };
    this.booking = demoBooking;
    this.notice = '';
  }
}
