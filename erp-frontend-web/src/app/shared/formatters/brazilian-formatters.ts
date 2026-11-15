import { formatDate, parseISO, isValid } from 'date-fns';
import { ptBR } from 'date-fns/locale';

/**
 * Formata valor numérico para moeda brasileira (BRL)
 * Exemplo: 1234.56 -> "R$ 1.234,56"
 */
export function formatBRL(value: number | string | null | undefined): string {
  if (value === null || value === undefined || value === '') {
    return 'R$ 0,00';
  }

  const num = typeof value === 'string' ? parseFloat(value.replace(/[^\d,-]/g, '').replace(',', '.')) : value;

  if (isNaN(num)) {
    return 'R$ 0,00';
  }

  return new Intl.NumberFormat('pt-BR', {
    style: 'currency',
    currency: 'BRL',
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  }).format(num);
}

/**
 * Converte string formatada em BRL para número
 * Exemplo: "R$ 1.234,56" -> 1234.56
 */
export function parseBRL(value: string): number {
  if (!value) return 0;

  // Remove R$, espaços, pontos de milhar e substitui vírgula por ponto
  const cleaned = value
    .replace(/[R$\s]/g, '')
    .replace(/\./g, '')
    .replace(',', '.');

  const num = parseFloat(cleaned);
  return isNaN(num) ? 0 : num;
}

/**
 * Formata data para padrão brasileiro (dd/MM/yyyy)
 * Exemplo: new Date('2024-01-15') -> "15/01/2024"
 */
export function formatDateBR(date: Date | string | number | null | undefined): string {
  if (!date) return '';

  let d: Date;
  if (date instanceof Date) {
    d = date;
  } else if (typeof date === 'string') {
    // Tenta parsear ISO string primeiro
    d = parseISO(date);
    if (!isValid(d)) {
      // Tenta parsear formato brasileiro
      const parts = date.split('/');
      if (parts.length === 3) {
        d = new Date(parseInt(parts[2], 10), parseInt(parts[1], 10) - 1, parseInt(parts[0], 10));
      } else {
        return '';
      }
    }
  } else {
    d = new Date(date);
  }

  if (!isValid(d)) return '';

  return formatDate(d, 'dd/MM/yyyy', { locale: ptBR });
}

/**
 * Formata data e hora para padrão brasileiro (dd/MM/yyyy HH:mm)
 */
export function formatDateTimeBR(date: Date | string | number | null | undefined): string {
  if (!date) return '';

  let d: Date;
  if (date instanceof Date) {
    d = date;
  } else if (typeof date === 'string') {
    d = parseISO(date);
    if (!isValid(d)) {
      return '';
    }
  } else {
    d = new Date(date);
  }

  if (!isValid(d)) return '';

  return formatDate(d, 'dd/MM/yyyy HH:mm', { locale: ptBR });
}

/**
 * Formata apenas hora (HH:mm)
 */
export function formatTimeBR(date: Date | string | number | null | undefined): string {
  if (!date) return '';

  let d: Date;
  if (date instanceof Date) {
    d = date;
  } else if (typeof date === 'string') {
    d = parseISO(date);
    if (!isValid(d)) return '';
  } else {
    d = new Date(date);
  }

  if (!isValid(d)) return '';

  return formatDate(d, 'HH:mm', { locale: ptBR });
}

/**
 * Converte string no formato brasileiro (dd/MM/yyyy) para Date
 */
export function parseDateBR(value: string): Date | null {
  if (!value) return null;

  const parts = value.split('/');
  if (parts.length !== 3) return null;

  const day = parseInt(parts[0], 10);
  const month = parseInt(parts[1], 10) - 1; // Mês é 0-indexed
  const year = parseInt(parts[2], 10);

  if (isNaN(day) || isNaN(month) || isNaN(year)) return null;

  const date = new Date(year, month, day);
  if (date.getDate() !== day || date.getMonth() !== month || date.getFullYear() !== year) {
    return null; // Data inválida (ex: 31/02)
  }

  return date;
}

/**
 * Converte string no formato brasileiro (dd/MM/yyyy HH:mm) para Date
 */
export function parseDateTimeBR(value: string): Date | null {
  if (!value) return null;

  const [datePart, timePart] = value.split(' ');
  const date = parseDateBR(datePart);
  if (!date) return null;

  if (timePart) {
    const [hours, minutes] = timePart.split(':').map(Number);
    if (!isNaN(hours) && !isNaN(minutes)) {
      date.setHours(hours, minutes, 0, 0);
    }
  }

  return date;
}

/**
 * Formata telefone brasileiro
 * Entrada: 11999999999 ou 1199999999
 * Saída: (11) 99999-9999 ou (11) 9999-9999
 */
export function formatPhoneBR(value: string): string {
  if (!value) return '';

  const numbers = value.replace(/[^\d]/g, '');

  if (numbers.length === 11) {
    // Celular: (XX) XXXXX-XXXX
    return `(${numbers.slice(0, 2)}) ${numbers.slice(2, 7)}-${numbers.slice(7, 11)}`;
  } else if (numbers.length === 10) {
    // Fixo: (XX) XXXX-XXXX
    return `(${numbers.slice(0, 2)}) ${numbers.slice(2, 6)}-${numbers.slice(6, 10)}`;
  }

  // Retorna parcialmente formatado se tamanho inesperado
  if (numbers.length > 2) {
    if (numbers.length <= 7) {
      return `(${numbers.slice(0, 2)}) ${numbers.slice(2)}`;
    } else if (numbers.length <= 11) {
      return `(${numbers.slice(0, 2)}) ${numbers.slice(2, 7)}-${numbers.slice(7)}`;
    }
  }

  return value;
}

/**
 * Formata CEP brasileiro
 * Entrada: 01234567
 * Saída: 01234-567
 */
export function formatCepBR(value: string): string {
  if (!value) return '';

  const numbers = value.replace(/[^\d]/g, '');

  if (numbers.length <= 5) return numbers;
  return `${numbers.slice(0, 5)}-${numbers.slice(5, 8)}`;
}

/**
 * Formata CPF ou CNPJ automaticamente
 * CPF: 000.000.000-00
 * CNPJ: 00.000.000/0000-00
 */
export function formatCpfCnpjBR(value: string): string {
  if (!value) return '';

  const numbers = value.replace(/[^\d]/g, '');

  if (numbers.length <= 11) {
    // CPF
    if (numbers.length <= 3) return numbers;
    if (numbers.length <= 6) return `${numbers.slice(0, 3)}.${numbers.slice(3)}`;
    if (numbers.length <= 9) return `${numbers.slice(0, 3)}.${numbers.slice(3, 6)}.${numbers.slice(6)}`;
    return `${numbers.slice(0, 3)}.${numbers.slice(3, 6)}.${numbers.slice(6, 9)}-${numbers.slice(9, 11)}`;
  } else {
    // CNPJ
    if (numbers.length <= 2) return numbers;
    if (numbers.length <= 5) return `${numbers.slice(0, 2)}.${numbers.slice(2)}`;
    if (numbers.length <= 8) return `${numbers.slice(0, 2)}.${numbers.slice(2, 5)}.${numbers.slice(5)}`;
    if (numbers.length <= 12) return `${numbers.slice(0, 2)}.${numbers.slice(2, 5)}.${numbers.slice(5, 8)}/${numbers.slice(8)}`;
    return `${numbers.slice(0, 2)}.${numbers.slice(2, 5)}.${numbers.slice(5, 8)}/${numbers.slice(8, 12)}-${numbers.slice(12, 14)}`;
  }
}

/**
 * Formata percentual brasileiro
 * Exemplo: 0.1525 -> "15,25%"
 */
export function formatPercentBR(value: number | string | null | undefined, decimals: number = 2): string {
  if (value === null || value === undefined || value === '') return '0,00%';

  const num = typeof value === 'string' ? parseFloat(value.replace(',', '.')) : value;
  if (isNaN(num)) return '0,00%';

  return new Intl.NumberFormat('pt-BR', {
    style: 'percent',
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals,
  }).format(num / 100);
}

/**
 * Formata número com separadores brasileiros
 * Exemplo: 1234567.89 -> "1.234.567,89"
 */
export function formatNumberBR(value: number | string | null | undefined, decimals: number = 2): string {
  if (value === null || value === undefined || value === '') return '0,00';

  const num = typeof value === 'string' ? parseFloat(value.replace(/[^\d,-]/g, '').replace(',', '.')) : value;
  if (isNaN(num)) return '0,00';

  return new Intl.NumberFormat('pt-BR', {
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals,
  }).format(num);
}

/**
 * Formata quantidade (inteiros)
 * Exemplo: 1234 -> "1.234"
 */
export function formatQuantityBR(value: number | string | null | undefined): string {
  return formatNumberBR(value, 0);
}

/**
 * Formata peso (kg) com 3 casas decimais
 * Exemplo: 1234.567 -> "1.234,567 kg"
 */
export function formatWeightBR(value: number | string | null | undefined): string {
  const formatted = formatNumberBR(value, 3);
  return `${formatted} kg`;
}

/**
 * Formata volume (m³) com 3 casas decimais
 */
export function formatVolumeBR(value: number | string | null | undefined): string {
  const formatted = formatNumberBR(value, 3);
  return `${formatted} m³`;
}

/**
 * Remove formatação brasileira e retorna número puro
 */
export function unformatBR(value: string): number {
  if (!value) return 0;
  const cleaned = value.replace(/[^\d,-]/g, '').replace(/\./g, '').replace(',', '.');
  return parseFloat(cleaned) || 0;
}

/**
 * Formata CNPJ apenas com números para envio à API
 */
export function unformatCpfCnpj(value: string): string {
  return value.replace(/[^\d]/g, '');
}

/**
 * Formata CEP apenas com números para envio à API
 */
export function unformatCep(value: string): string {
  return value.replace(/[^\d]/g, '');
}

/**
 * Formata telefone apenas com números para envio à API
 */
export function unformatPhone(value: string): string {
  return value.replace(/[^\d]/g, '');
}