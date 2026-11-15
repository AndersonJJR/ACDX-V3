package com.erp.brazil.fiscal.application.port;

import com.erp.brazil.fiscal.domain.model.NFE;

public interface SEFAZPort {
    EmissionResult emit(NFE nfe);

    ConsultResult consult(String chaveAcesso);

    CancellationResult cancel(String chaveAcesso, String protocolo, String justificativa);

    InutilizationResult inutilize(String cnpj, String serie, int numeroInicial, int numeroFinal, String justificativa);

    record EmissionResult(
            boolean success,
            String protocolo,
            String xmlAutorizado,
            String message
    ) {
        public static EmissionResult success(String protocolo, String xmlAutorizado) {
            return new EmissionResult(true, protocolo, xmlAutorizado, null);
        }

        public static EmissionResult failure(String message) {
            return new EmissionResult(false, null, null, message);
        }
    }

    record ConsultResult(
            boolean success,
            String status,
            String protocolo,
            String xml,
            String message
    ) {
        public static ConsultResult success(String status, String protocolo, String xml) {
            return new ConsultResult(true, status, protocolo, xml, null);
        }

        public static ConsultResult failure(String message) {
            return new ConsultResult(false, null, null, null, message);
        }
    }

    record CancellationResult(
            boolean success,
            String protocolo,
            String xml,
            String message
    ) {
        public static CancellationResult success(String protocolo, String xml) {
            return new CancellationResult(true, protocolo, xml, null);
        }

        public static CancellationResult failure(String message) {
            return new CancellationResult(false, null, null, message);
        }
    }

    record InutilizationResult(
            boolean success,
            String protocolo,
            String xml,
            String message
    ) {
        public static InutilizationResult success(String protocolo, String xml) {
            return new InutilizationResult(true, protocolo, xml, null);
        }

        public static InutilizationResult failure(String message) {
            return new InutilizationResult(false, null, null, message);
        }
    }
}