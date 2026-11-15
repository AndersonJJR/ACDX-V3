import { Component, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { BtnComponent } from '../ui/btn/btn.component';
import { CardComponent } from '../ui/card/card.component';

@Component({
  selector: 'app-placeholder',
  standalone: true,
  imports: [CommonModule, RouterLink, BtnComponent, CardComponent],
  templateUrl: './placeholder.component.html',
  styleUrl: './placeholder.component.scss',
})
export class PlaceholderComponent {
  title = input('Funcionalidade');
  description = input('Esta funcionalidade está em desenvolvimento.');
  icon = input('M13 10V3L4 14h7v7l9-11h-7z');
  actionLabel = input('Voltar');
  actionRoute = input('/dashboard');
}