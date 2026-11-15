import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { ToastComponent } from './toast.component';
import { ToastService } from './toast.service';
import { By } from '@angular/platform-browser';

describe('ToastComponent', () => {
  let component: ToastComponent;
  let fixture: ComponentFixture<ToastComponent>;
  let toastService: ToastService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ToastComponent],
      providers: [ToastService],
    }).compileComponents();

    fixture = TestBed.createComponent(ToastComponent);
    component = fixture.componentInstance;
    toastService = TestBed.inject(ToastService);
  });

  it('should create', () => {
    fixture.componentRef.setInput('toast', {
      id: 'test-1',
      type: 'success',
      title: 'Sucesso!',
      message: 'Operação realizada com sucesso',
    });
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('should display title and message', () => {
    fixture.componentRef.setInput('toast', {
      id: 'test-1',
      type: 'success',
      title: 'Sucesso!',
      message: 'Operação realizada com sucesso',
    });
    fixture.detectChanges();

    const title = fixture.debugElement.query(By.css('h4')).nativeElement;
    const message = fixture.debugElement.query(By.css('p')).nativeElement;

    expect(title.textContent).toContain('Sucesso!');
    expect(message.textContent).toContain('Operação realizada com sucesso');
  });

  it('should show correct icon for success type', () => {
    fixture.componentRef.setInput('toast', {
      id: 'test-1',
      type: 'success',
      title: 'Sucesso!',
    });
    fixture.detectChanges();

    const svg = fixture.debugElement.query(By.css('svg'));
    expect(svg).toBeTruthy();
  });

  it('should show close button when closable is true', () => {
    fixture.componentRef.setInput('toast', {
      id: 'test-1',
      type: 'success',
      title: 'Sucesso!',
      closable: true,
    });
    fixture.detectChanges();

    const closeBtn = fixture.debugElement.query(By.css('button[aria-label="Fechar notificação"]'));
    expect(closeBtn).toBeTruthy();
  });

  it('should not show close button when closable is false', () => {
    fixture.componentRef.setInput('toast', {
      id: 'test-1',
      type: 'success',
      title: 'Sucesso!',
      closable: false,
    });
    fixture.detectChanges();

    const closeBtn = fixture.debugElement.query(By.css('button[aria-label="Fechar notificação"]'));
    expect(closeBtn).toBeFalsy();
  });

  it('should call toastService.remove when dismiss is called', fakeAsync(() => {
    spyOn(toastService, 'remove');
    fixture.componentRef.setInput('toast', {
      id: 'test-1',
      type: 'success',
      title: 'Sucesso!',
    });
    fixture.detectChanges();

    component.dismiss();
    tick(300);

    expect(toastService.remove).toHaveBeenCalledWith('test-1');
  }));

  it('should execute action callback when action button clicked', () => {
    const callback = jasmine.createSpy('callback');
    fixture.componentRef.setInput('toast', {
      id: 'test-1',
      type: 'success',
      title: 'Sucesso!',
      action: { label: 'Desfazer', callback },
    });
    fixture.detectChanges();

    const actionBtn = fixture.debugElement.query(By.css('button:not([aria-label])'));
    actionBtn.nativeElement.click();

    expect(callback).toHaveBeenCalled();
  });

  it('should apply correct classes for error type', () => {
    fixture.componentRef.setInput('toast', {
      id: 'test-1',
      type: 'error',
      title: 'Erro!',
    });
    fixture.detectChanges();

    const container = fixture.debugElement.query(By.css('div.flex.items-start')).nativeElement;
    expect(container.classList).toContain('bg-danger-50');
    expect(container.classList).toContain('border-danger-200');
  });

  it('should apply correct classes for warning type', () => {
    fixture.componentRef.setInput('toast', {
      id: 'test-1',
      type: 'warning',
      title: 'Atenção!',
    });
    fixture.detectChanges();

    const container = fixture.debugElement.query(By.css('div.flex.items-start')).nativeElement;
    expect(container.classList).toContain('bg-warning-50');
    expect(container.classList).toContain('border-warning-200');
  });

  it('should apply correct classes for info type', () => {
    fixture.componentRef.setInput('toast', {
      id: 'test-1',
      type: 'info',
      title: 'Informação!',
    });
    fixture.detectChanges();

    const container = fixture.debugElement.query(By.css('div.flex.items-start')).nativeElement;
    expect(container.classList).toContain('bg-primary-50');
    expect(container.classList).toContain('border-primary-200');
  });
});