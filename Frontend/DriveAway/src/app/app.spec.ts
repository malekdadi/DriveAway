import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { App } from './app';
import { routes } from './app.routes';

describe('DriveAway', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({ imports: [App], providers: [provideRouter(routes)] });
  });

  it('shows the DriveAway brand', () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('header img').getAttribute('alt')).toBe('DriveAway');
  });

  it('applies night mode and remembers it for a new visit', () => {
    localStorage.removeItem('driveaway-theme');
    try {
      const fixture = TestBed.createComponent(App);
      fixture.detectChanges();
      const page = fixture.nativeElement as HTMLElement;
      const toggle = page.querySelector<HTMLButtonElement>('.theme-switch button[aria-label="Mode nuit"]')!;
      toggle.click();
      fixture.detectChanges();
      expect(page.classList.contains('night-mode')).toBe(true);
      expect(toggle.getAttribute('aria-pressed')).toBe('true');

      const nextVisit = TestBed.createComponent(App);
      nextVisit.detectChanges();
      expect(nextVisit.nativeElement.classList.contains('night-mode')).toBe(true);
      nextVisit.nativeElement.querySelector('.theme-switch button[aria-label="Mode jour"]').click();
      nextVisit.detectChanges();
      expect(nextVisit.nativeElement.classList.contains('night-mode')).toBe(false);
    } finally {
      localStorage.removeItem('driveaway-theme');
    }
  });

  it('links each header module to its page and marks the current module', async () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    const header = fixture.nativeElement as HTMLElement;
    const router = TestBed.inject(Router);
    const links = Array.from(header.querySelectorAll<HTMLAnchorElement>('header nav a'));

    expect(links.map(link => link.getAttribute('href'))).toEqual([
      '/utilisateurs', '/vehicules', '/reservations', '/facturation', '/agences', '/maintenance',
    ]);

    const menu = header.querySelector<HTMLButtonElement>('.menu-toggle')!;
    menu.click();
    fixture.detectChanges();
    expect(menu.getAttribute('aria-expanded')).toBe('true');
    links[0].click();
    await fixture.whenStable();
    fixture.detectChanges();
    expect(menu.getAttribute('aria-expanded')).toBe('false');

    for (const link of links) {
      await router.navigateByUrl(link.getAttribute('href')!);
      await fixture.whenStable();
      fixture.detectChanges();
      expect(link.getAttribute('aria-current')).toBe('page');
      expect(header.querySelector('main h1')?.textContent).toContain(
        link.querySelector('.nav-label')?.firstChild?.textContent?.trim(),
      );
    }
  });

  it('shows the Porsche Macan in the homepage campaign', async () => {
    const harness = await RouterTestingHarness.create('/');
    const campaign = harness.routeNativeElement!.querySelector('.editorial-visual')!;
    const heroImage = campaign.querySelector('img')!;
    expect(heroImage.getAttribute('src')).toMatch(/^data:image\/jpeg;base64,/);
    expect(heroImage.alt).toBe('Porsche Macan');
    expect(campaign.querySelector('figcaption a')?.getAttribute('href')).toBe('/reservations?voiture=macan');
  });

  it('opens the catalogue filtered from the fleet statistics', async () => {
    const harness = await RouterTestingHarness.create('/');
    const links = [...harness.routeNativeElement!.querySelectorAll<HTMLAnchorElement>('.fleet-breakdown a')];
    const suvLink = links.find(link => link.textContent?.includes('SUV'))!;

    await harness.navigateByUrl(suvLink.getAttribute('href')!);

    expect(harness.routeNativeElement!.querySelector('.filters [aria-pressed="true"]')?.textContent?.trim()).toBe('SUV');
    expect(harness.routeNativeElement!.querySelectorAll('.car')).toHaveLength(4);
  });

  it('filters the catalogue and carries the booking estimate into billing', async () => {
    const harness = await RouterTestingHarness.create('/vehicules');
    const buttons = Array.from(harness.routeNativeElement!.querySelectorAll<HTMLButtonElement>('.filters button'));
    expect(harness.routeNativeElement!.querySelectorAll('.car')).toHaveLength(7);
    buttons.find(button => button.textContent?.trim() === 'Prestige')!.click();
    harness.detectChanges();
    await harness.fixture.whenStable();
    expect(harness.routeNativeElement!.querySelectorAll('.car')).toHaveLength(4);

    const sort = harness.routeNativeElement!.querySelector<HTMLSelectElement>('.fleet-sort select')!;
    sort.value = 'price-desc';
    sort.dispatchEvent(new Event('change'));
    harness.detectChanges();
    expect(harness.routeNativeElement!.querySelector('.car h3')!.textContent).toBe('Range Rover Sport');

    buttons.find(button => button.textContent?.trim() === 'Berline')!.click();
    harness.detectChanges();

    expect(harness.routeNativeElement!.querySelectorAll('.car')).toHaveLength(1);
    expect(harness.routeNativeElement!.querySelector('.car h3')!.textContent).toBe('Mercedes-Benz Classe E');

    await harness.navigateByUrl('/reservations?voiture=classe-e&depart=2099-01-01&retour=2099-01-04');
    await harness.fixture.whenStable();
    harness.routeNativeElement!.querySelector('form')!.dispatchEvent(new Event('submit', { bubbles: true, cancelable: true }));
    harness.detectChanges();
    expect(harness.routeNativeElement!.querySelector('article')!.textContent).toContain('1470 DT');

    await harness.navigateByUrl('/facturation');
    expect(harness.routeNativeElement!.querySelector('article')!.textContent).toContain('1470 DT');
  });

  it('refreshes the reservation when its URL changes and rejects invalid calendar dates', async () => {
    const harness = await RouterTestingHarness.create(
      '/reservations?voiture=macan&depart=2099-02-30&retour=2099-03-04',
    );
    await harness.fixture.whenStable();
    const page = harness.routeNativeElement!;

    expect(page.querySelector<HTMLInputElement>('input[name="depart"]')!.value).toBe('');
    expect(page.querySelector('.estimate-total')).toBeNull();

    await harness.navigateByUrl('/reservations?voiture=x5&agence=Sousse&depart=2099-01-01&retour=2099-01-03');
    await harness.fixture.whenStable();
    expect(page.querySelector<HTMLImageElement>('.vehicle-preview img')!.alt).toBe('BMW X5');
    expect(page.querySelector<HTMLSelectElement>('select[name="agence"]')!.value).toBe('Sousse');
    expect(page.querySelector<HTMLInputElement>('input[name="retour"]')!.min).toBe('2099-01-02');
    expect(page.querySelector('.estimate-total')!.textContent).toContain('1380 DT');
  });
});
