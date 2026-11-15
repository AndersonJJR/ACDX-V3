import { Injectable, signal, computed } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class LoadingService {
  private readonly _requestCount = signal(0);
  private readonly _globalLoading = signal(false);

  readonly isLoading = computed(() => this._requestCount() > 0 || this._globalLoading());
  readonly requestCount = computed(() => this._requestCount());

  start(): void {
    this._requestCount.update((count) => count + 1);
  }

  stop(): void {
    this._requestCount.update((count) => Math.max(0, count - 1));
  }

  setGlobalLoading(loading: boolean): void {
    this._globalLoading.set(loading);
  }

  reset(): void {
    this._requestCount.set(0);
    this._globalLoading.set(false);
  }
}