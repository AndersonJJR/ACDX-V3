import { ValidatorFn, AbstractControl, ValidationErrors } from '@angular/forms';

export interface BrazilianValidationErrors {
  cpf?: { valid: boolean; message: string };
  cnpj?: { valid: boolean; message: string };
  cep?: { valid: boolean; message: string };
  phone?: { valid: boolean; message: string };
  stateRegistration?: { valid: boolean; message: string; state: string };
}

/**
 * Validador de CPF (Cadastro de Pessoas Físicas)
 * Formato: 000.000.000-00 ou 00000000000
 * Algoritmo: Validação dos dois dígitos verificadores
 */
export function cpfValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    if (!control.value) {
      return null;
    }

    const cpf = control.value.replace(/[^\d]/g, '');

    if (cpf.length !== 11) {
      return { cpf: { valid: false, message: 'CPF deve conter 11 dígitos' } };
    }

    // Verifica se todos os dígitos são iguais (CPF inválido)
    if (/^(\d)\1{10}$/.test(cpf)) {
      return { cpf: { valid: false, message: 'CPF inválido' } };
    }

    // Validação do primeiro dígito verificador
    let sum = 0;
    for (let i = 0; i < 9; i++) {
      sum += parseInt(cpf.charAt(i), 10) * (10 - i);
    }
    let remainder = (sum * 10) % 11;
    if (remainder === 10 || remainder === 11) {
      remainder = 0;
    }
    if (remainder !== parseInt(cpf.charAt(9), 10)) {
      return { cpf: { valid: false, message: 'CPF inválido' } };
    }

    // Validação do segundo dígito verificador
    sum = 0;
    for (let i = 0; i < 10; i++) {
      sum += parseInt(cpf.charAt(i), 10) * (11 - i);
    }
    remainder = (sum * 10) % 11;
    if (remainder === 10 || remainder === 11) {
      remainder = 0;
    }
    if (remainder !== parseInt(cpf.charAt(10), 10)) {
      return { cpf: { valid: false, message: 'CPF inválido' } };
    }

    return null;
  };
}

/**
 * Validador de CNPJ (Cadastro Nacional da Pessoa Jurídica)
 * Formato: 00.000.000/0000-00 ou 00000000000000
 * Algoritmo: Validação dos dois dígitos verificadores
 */
export function cnpjValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    if (!control.value) {
      return null;
    }

    const cnpj = control.value.replace(/[^\d]/g, '');

    if (cnpj.length !== 14) {
      return { cnpj: { valid: false, message: 'CNPJ deve conter 14 dígitos' } };
    }

    // Verifica se todos os dígitos são iguais (CNPJ inválido)
    if (/^(\d)\1{13}$/.test(cnpj)) {
      return { cnpj: { valid: false, message: 'CNPJ inválido' } };
    }

    // Validação do primeiro dígito verificador
    const weights1 = [5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2];
    let sum = 0;
    for (let i = 0; i < 12; i++) {
      sum += parseInt(cnpj.charAt(i), 10) * weights1[i];
    }
    let remainder = sum % 11;
    const digit1 = remainder < 2 ? 0 : 11 - remainder;
    if (digit1 !== parseInt(cnpj.charAt(12), 10)) {
      return { cnpj: { valid: false, message: 'CNPJ inválido' } };
    }

    // Validação do segundo dígito verificador
    const weights2 = [6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2];
    sum = 0;
    for (let i = 0; i < 13; i++) {
      sum += parseInt(cnpj.charAt(i), 10) * weights2[i];
    }
    remainder = sum % 11;
    const digit2 = remainder < 2 ? 0 : 11 - remainder;
    if (digit2 !== parseInt(cnpj.charAt(13), 10)) {
      return { cnpj: { valid: false, message: 'CNPJ inválido' } };
    }

    return null;
  };
}

/**
 * Validador de CEP (Código de Endereçamento Postal)
 * Formato: 00000-000 ou 00000000
 */
export function cepValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    if (!control.value) {
      return null;
    }

    const cep = control.value.replace(/[^\d]/g, '');

    if (cep.length !== 8) {
      return { cep: { valid: false, message: 'CEP deve conter 8 dígitos' } };
    }

    // Verifica se não é um CEP genérico inválido
    if (/^(\d)\1{7}$/.test(cep)) {
      return { cep: { valid: false, message: 'CEP inválido' } };
    }

    return null;
  };
}

/**
 * Validador de Telefone Brasileiro
 * Formatos aceitos:
 * - (XX) XXXXX-XXXX (celular com 9 dígitos)
 * - (XX) XXXX-XXXX (fixo com 8 dígitos)
 * - XX XXXXX-XXXX
 * - XX XXXX-XXXX
 */
export function phoneValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    if (!control.value) {
      return null;
    }

    const phone = control.value.replace(/[^\d]/g, '');

    // Deve ter 10 ou 11 dígitos (DDD + número)
    if (phone.length !== 10 && phone.length !== 11) {
      return { phone: { valid: false, message: 'Telefone deve conter 10 ou 11 dígitos (com DDD)' } };
    }

    // DDD válido (11 a 99)
    const ddd = parseInt(phone.substring(0, 2), 10);
    if (ddd < 11 || ddd > 99) {
      return { phone: { valid: false, message: 'DDD inválido' } };
    }

    // Para celular (11 dígitos), o 9º dígito deve ser 9
    if (phone.length === 11 && phone.charAt(2) !== '9') {
      return { phone: { valid: false, message: 'Celular deve ter 9 como primeiro dígito após o DDD' } };
    }

    return null;
  };
}

/**
 * Validador de Inscrição Estadual (IE) por Estado
 * Cada estado tem seu próprio algoritmo de validação
 */
export function stateRegistrationValidator(state: string): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    if (!control.value) {
      return null;
    }

    const ie = control.value.replace(/[^\d]/g, '');
    const uf = state.toUpperCase();

    const validators: Record<string, (ie: string) => boolean> = {
      'SP': validateIESaoPaulo,
      'RJ': validateIERioDeJaneiro,
      'MG': validateIEMinasGerais,
      'RS': validateIERioGrandeDoSul,
      'PR': validateIEParana,
      'SC': validateIESantaCatarina,
      'BA': validateIEBahia,
      'GO': validateIEGoiás,
      'ES': validateIEEspiritoSanto,
      'PE': validateIEPernambuco,
      'CE': validateIECeara,
      'PA': validateIEPara,
      'MA': validateIEMaranhao,
      'MT': validateIEMatoGrosso,
      'MS': validateIEMatoGrossoDoSul,
      'DF': validateIEDistritoFederal,
    };

    const validator = validators[uf];
    if (!validator) {
      // Se não há validador específico, aceita qualquer valor numérico
      return null;
    }

    if (!validator(ie)) {
      return {
        stateRegistration: {
          valid: false,
          message: `Inscrição Estadual inválida para o estado ${uf}`,
          state: uf,
        },
      };
    }

    return null;
  };
}

// Implementações específicas por estado

function validateIESaoPaulo(ie: string): boolean {
  if (ie.length !== 12) return false;
  if (/^(\d)\1{11}$/.test(ie)) return false;

  const weights1 = [1, 3, 4, 5, 6, 7, 8, 10, 10];
  const weights2 = [3, 4, 5, 6, 7, 8, 10, 10];

  let sum = 0;
  for (let i = 0; i < 9; i++) {
    sum += parseInt(ie.charAt(i), 10) * weights1[i];
  }
  let remainder = sum % 11;
  const digit1 = remainder === 10 ? 0 : remainder;
  if (digit1 !== parseInt(ie.charAt(9), 10)) return false;

  sum = 0;
  for (let i = 0; i < 8; i++) {
    sum += parseInt(ie.charAt(i), 10) * weights2[i];
  }
  sum += digit1 * weights2[8];
  remainder = sum % 11;
  const digit2 = remainder === 10 ? 0 : remainder;
  if (digit2 !== parseInt(ie.charAt(10), 10)) return false;

  // 12º dígito é sempre 0 para SP
  return ie.charAt(11) === '0';
}

function validateIERioDeJaneiro(ie: string): boolean {
  if (ie.length !== 8) return false;
  if (/^(\d)\1{7}$/.test(ie)) return false;

  const weights = [2, 7, 6, 5, 4, 3, 2];
  let sum = 0;
  for (let i = 0; i < 7; i++) {
    sum += parseInt(ie.charAt(i), 10) * weights[i];
  }
  const remainder = sum % 11;
  const digit = remainder < 2 ? 0 : 11 - remainder;
  return digit === parseInt(ie.charAt(7), 10);
}

function validateIEMinasGerais(ie: string): boolean {
  if (ie.length !== 13) return false;

  const weights1 = [1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2];
  let sum = 0;
  for (let i = 0; i < 12; i++) {
    let product = parseInt(ie.charAt(i), 10) * weights1[i];
    if (product > 9) product = Math.floor(product / 10) + (product % 10);
    sum += product;
  }
  const remainder = sum % 10;
  const digit = remainder === 0 ? 0 : 10 - remainder;
  return digit === parseInt(ie.charAt(12), 10);
}

function validateIERioGrandeDoSul(ie: string): boolean {
  if (ie.length !== 10) return false;

  const weights = [2, 9, 8, 7, 6, 5, 4, 3, 2];
  let sum = 0;
  for (let i = 0; i < 9; i++) {
    sum += parseInt(ie.charAt(i), 10) * weights[i];
  }
  const remainder = sum % 11;
  const digit = remainder < 2 ? 0 : 11 - remainder;
  return digit === parseInt(ie.charAt(9), 10);
}

function validateIEParana(ie: string): boolean {
  if (ie.length !== 10) return false;

  const weights1 = [3, 2, 7, 6, 5, 4, 3, 2];
  let sum = 0;
  for (let i = 0; i < 8; i++) {
    sum += parseInt(ie.charAt(i), 10) * weights1[i];
  }
  let remainder = sum % 11;
  let digit = remainder < 2 ? 0 : 11 - remainder;
  if (digit !== parseInt(ie.charAt(8), 10)) return false;

  const weights2 = [4, 3, 2, 7, 6, 5, 4, 3, 2];
  sum = 0;
  for (let i = 0; i < 9; i++) {
    sum += parseInt(ie.charAt(i), 10) * weights2[i];
  }
  remainder = sum % 11;
  digit = remainder < 2 ? 0 : 11 - remainder;
  return digit === parseInt(ie.charAt(9), 10);
}

function validateIESantaCatarina(ie: string): boolean {
  if (ie.length !== 9) return false;

  const weights = [9, 8, 7, 6, 5, 4, 3, 2];
  let sum = 0;
  for (let i = 0; i < 8; i++) {
    sum += parseInt(ie.charAt(i), 10) * weights[i];
  }
  const remainder = sum % 11;
  const digit = remainder < 2 ? 0 : 11 - remainder;
  return digit === parseInt(ie.charAt(8), 10);
}

function validateIEBahia(ie: string): boolean {
  if (ie.length !== 8 && ie.length !== 9) return false;

  const weights = [7, 6, 5, 4, 3, 2, 9, 8];
  let sum = 0;
  const length = ie.length === 8 ? 7 : 8;
  for (let i = 0; i < length; i++) {
    sum += parseInt(ie.charAt(i), 10) * weights[weights.length - length + i];
  }
  const remainder = sum % 11;
  const digit = remainder < 2 ? 0 : 11 - remainder;
  return digit === parseInt(ie.charAt(ie.length - 1), 10);
}

function validateIEGoiás(ie: string): boolean {
  if (ie.length !== 9) return false;

  const weights1 = [9, 8, 7, 6, 5, 4, 3, 2];
  let sum = 0;
  for (let i = 0; i < 8; i++) {
    sum += parseInt(ie.charAt(i), 10) * weights1[i];
  }
  let remainder = sum % 11;
  let digit = remainder < 2 ? 0 : 11 - remainder;
  if (digit !== parseInt(ie.charAt(8), 10)) return false;

  return true;
}

function validateIEEspiritoSanto(ie: string): boolean {
  if (ie.length !== 9) return false;

  const weights = [9, 8, 7, 6, 5, 4, 3, 2];
  let sum = 0;
  for (let i = 0; i < 8; i++) {
    sum += parseInt(ie.charAt(i), 10) * weights[i];
  }
  const remainder = sum % 11;
  const digit = remainder < 2 ? 0 : 11 - remainder;
  return digit === parseInt(ie.charAt(8), 10);
}

function validateIEPernambuco(ie: string): boolean {
  if (ie.length !== 9 && ie.length !== 14) return false;

  if (ie.length === 9) {
    const weights = [8, 7, 6, 5, 4, 3, 2];
    let sum = 0;
    for (let i = 0; i < 7; i++) {
      sum += parseInt(ie.charAt(i), 10) * weights[i];
    }
    sum += parseInt(ie.charAt(7), 10) * 9;
    const remainder = sum % 11;
    const digit = remainder < 2 ? 0 : 11 - remainder;
    return digit === parseInt(ie.charAt(8), 10);
  }

  return true;
}

function validateIECeara(ie: string): boolean {
  if (ie.length !== 9) return false;

  const weights = [9, 8, 7, 6, 5, 4, 3, 2];
  let sum = 0;
  for (let i = 0; i < 8; i++) {
    sum += parseInt(ie.charAt(i), 10) * weights[i];
  }
  const remainder = sum % 11;
  const digit = remainder < 2 ? 0 : 11 - remainder;
  return digit === parseInt(ie.charAt(8), 10);
}

function validateIEPara(ie: string): boolean {
  if (ie.length !== 9) return false;

  const weights = [9, 8, 7, 6, 5, 4, 3, 2];
  let sum = 0;
  for (let i = 0; i < 8; i++) {
    sum += parseInt(ie.charAt(i), 10) * weights[i];
  }
  const remainder = sum % 11;
  const digit = remainder < 2 ? 0 : 11 - remainder;
  return digit === parseInt(ie.charAt(8), 10);
}

function validateIEMaranhao(ie: string): boolean {
  if (ie.length !== 9) return false;

  const weights = [9, 8, 7, 6, 5, 4, 3, 2];
  let sum = 0;
  for (let i = 0; i < 8; i++) {
    sum += parseInt(ie.charAt(i), 10) * weights[i];
  }
  const remainder = sum % 11;
  const digit = remainder < 2 ? 0 : 11 - remainder;
  return digit === parseInt(ie.charAt(8), 10);
}

function validateIEMatoGrosso(ie: string): boolean {
  if (ie.length !== 11) return false;

  const weights = [3, 2, 9, 8, 7, 6, 5, 4, 3, 2];
  let sum = 0;
  for (let i = 0; i < 10; i++) {
    sum += parseInt(ie.charAt(i), 10) * weights[i];
  }
  const remainder = sum % 11;
  const digit = remainder < 2 ? 0 : 11 - remainder;
  return digit === parseInt(ie.charAt(10), 10);
}

function validateIEMatoGrossoDoSul(ie: string): boolean {
  if (ie.length !== 11) return false;

  const weights = [2, 3, 4, 5, 6, 7, 8, 9, 2, 3];
  let sum = 0;
  for (let i = 0; i < 10; i++) {
    sum += parseInt(ie.charAt(i), 10) * weights[i];
  }
  const remainder = sum % 11;
  const digit = remainder < 2 ? 0 : 11 - remainder;
  return digit === parseInt(ie.charAt(10), 10);
}

function validateIEDistritoFederal(ie: string): boolean {
  if (ie.length !== 13) return false;

  const weights = [4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2];
  let sum = 0;
  for (let i = 0; i < 11; i++) {
    sum += parseInt(ie.charAt(i), 10) * weights[i];
  }
  const remainder = sum % 11;
  const digit = remainder < 2 ? 0 : 11 - remainder;
  return digit === parseInt(ie.charAt(11), 10) && ie.charAt(12) === '0';
}

/**
 * Validador combinado para CPF ou CNPJ
 * Aceita tanto CPF (11 dígitos) quanto CNPJ (14 dígitos)
 */
export function cpfCnpjValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    if (!control.value) {
      return null;
    }

    const value = control.value.replace(/[^\d]/g, '');

    if (value.length === 11) {
      return cpfValidator()(control);
    } else if (value.length === 14) {
      return cnpjValidator()(control);
    }

    return { cpfCnpj: { valid: false, message: 'Deve ser um CPF (11 dígitos) ou CNPJ (14 dígitos) válido' } };
  };
}

/**
 * Formata CPF para exibição: 000.000.000-00
 */
export function formatCpf(value: string): string {
  const numbers = value.replace(/[^\d]/g, '');
  if (numbers.length <= 3) return numbers;
  if (numbers.length <= 6) return `${numbers.slice(0, 3)}.${numbers.slice(3)}`;
  if (numbers.length <= 9) return `${numbers.slice(0, 3)}.${numbers.slice(3, 6)}.${numbers.slice(6)}`;
  return `${numbers.slice(0, 3)}.${numbers.slice(3, 6)}.${numbers.slice(6, 9)}-${numbers.slice(9, 11)}`;
}

/**
 * Formata CNPJ para exibição: 00.000.000/0000-00
 */
export function formatCnpj(value: string): string {
  const numbers = value.replace(/[^\d]/g, '');
  if (numbers.length <= 2) return numbers;
  if (numbers.length <= 5) return `${numbers.slice(0, 2)}.${numbers.slice(2)}`;
  if (numbers.length <= 8) return `${numbers.slice(0, 2)}.${numbers.slice(2, 5)}.${numbers.slice(5)}`;
  if (numbers.length <= 12) return `${numbers.slice(0, 2)}.${numbers.slice(2, 5)}.${numbers.slice(5, 8)}/${numbers.slice(8)}`;
  return `${numbers.slice(0, 2)}.${numbers.slice(2, 5)}.${numbers.slice(5, 8)}/${numbers.slice(8, 12)}-${numbers.slice(12, 14)}`;
}

/**
 * Formata CPF ou CNPJ automaticamente baseado no tamanho
 */
export function formatCpfCnpj(value: string): string {
  const numbers = value.replace(/[^\d]/g, '');
  if (numbers.length <= 11) {
    return formatCpf(numbers);
  }
  return formatCnpj(numbers);
}