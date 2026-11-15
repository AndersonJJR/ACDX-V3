export const environment = {
  production: true,
  apiUrl: 'https://api.acdx.com.br/api/v1',
  appName: 'ACDX ERP',
  version: '1.0.0',
  defaultLanguage: 'pt-BR',
  timezone: 'America/Sao_Paulo',
  currency: 'BRL',
  dateFormat: 'dd/MM/yyyy',
  dateTimeFormat: 'dd/MM/yyyy HH:mm',
  pagination: {
    defaultPageSize: 20,
    pageSizeOptions: [10, 20, 50, 100],
  },
  toast: {
    defaultDuration: 5000,
    maxVisible: 5,
  },
};