package com.erp.brazil.fiscal.infrastructure.certificado;

import com.erp.brazil.fiscal.domain.model.CertificadoDigitalA1;
import com.erp.brazil.fiscal.domain.repository.CertificadoDigitalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CertificadoDigitalServiceTest {

    @Mock
    private CertificadoDigitalRepository certificadoRepository;

    @Mock
    private CertificadoSSLService sslService;

    private CertificadoDigitalService certificadoService;

    @BeforeEach
    void setUp() {
        certificadoService = new CertificadoDigitalService(certificadoRepository, sslService);
    }

    @Test
    void registerCertificate_EncryptsPfxAndPassword() {
        // Given
        String pfxBase64 = "base64encodedpfxcontent";
        String password = "senha123";
        String companyId = "company-1";

        CertificadoSSLService.CertificateInfo certInfo = new CertificadoSSLService.CertificateInfo(
                "CN=Teste", "CN=CA", "123456", "ABCDEF123456",
                LocalDate.now(), LocalDate.now().plusYears(1),
                "12345678000195"
        );

        when(sslService.extractCertificateInfo(pfxBase64, password)).thenReturn(certInfo);

        ArgumentCaptor<CertificadoDigitalA1> captor = ArgumentCaptor.forClass(CertificadoDigitalA1.class);

        // When
        CertificadoDigitalA1 result = certificadoService.registerCertificate(
                companyId, "alias-1", pfxBase64, password,
                "12345678000195", "Empresa Teste", 30
        );

        // Then
        assertNotNull(result);
        assertEquals(companyId, result.companyId());
        assertEquals("alias-1", result.alias());
        assertEquals("12345678000195", result.cnpj());
        assertEquals("Empresa Teste", result.companyName());
        assertEquals(CertificadoDigitalA1.CertificateStatus.ACTIVE, result.status());
        assertEquals(30, result.renewalAlertDays());

        // Verify encryption - stored values should be different from input
        verify(certificadoRepository).save(captor.capture());
        CertificadoDigitalA1 saved = captor.getValue();
        assertNotEquals(pfxBase64, saved.pfxEncrypted());
        assertNotEquals(password, saved.passwordEncrypted());
    }

    @Test
    void getActiveCertificate_ReturnsActiveCertificate() {
        // Given
        String companyId = "company-1";
        CertificadoDigitalA1 cert = createCertificate(companyId, CertificadoDigitalA1.CertificateStatus.ACTIVE);

        when(certificadoRepository.findByCompanyIdAndStatus(companyId, CertificadoDigitalA1.CertificateStatus.ACTIVE))
                .thenReturn(List.of(cert));

        // When
        Optional<CertificadoDigitalA1> result = certificadoService.getActiveCertificate(companyId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(cert.id(), result.get().id());
    }

    @Test
    void getActiveCertificate_ReturnsEmptyWhenNoActive() {
        // Given
        String companyId = "company-1";

        when(certificadoRepository.findByCompanyIdAndStatus(companyId, CertificadoDigitalA1.CertificateStatus.ACTIVE))
                .thenReturn(List.of());

        // When
        Optional<CertificadoDigitalA1> result = certificadoService.getActiveCertificate(companyId);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void getCertificateById_ReturnsCertificate() {
        // Given
        String id = "cert-1";
        String companyId = "company-1";
        CertificadoDigitalA1 cert = createCertificate(companyId, CertificadoDigitalA1.CertificateStatus.ACTIVE);

        when(certificadoRepository.findByIdAndCompanyId(id, companyId)).thenReturn(Optional.of(cert));

        // When
        Optional<CertificadoDigitalA1> result = certificadoService.getCertificateById(id, companyId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(id, result.get().id());
    }

    @Test
    void updateCertificate_UpdatesFields() {
        // Given
        String id = "cert-1";
        String companyId = "company-1";
        CertificadoDigitalA1 existing = createCertificate(companyId, CertificadoDigitalA1.CertificateStatus.ACTIVE);

        when(certificadoRepository.findByIdAndCompanyId(id, companyId)).thenReturn(Optional.of(existing));
        when(certificadoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // When
        certificadoService.updateCertificate(id, companyId, "new-alias", null, null, 60);

        // Then
        verify(certificadoRepository).save(argThat(c ->
                "new-alias".equals(c.alias()) && 60 == c.renewalAlertDays()
        ));
    }

    @Test
    void updateCertificate_WithNewPfx_EncryptsNewPfx() {
        // Given
        String id = "cert-1";
        String companyId = "company-1";
        CertificadoDigitalA1 existing = createCertificate(companyId, CertificadoDigitalA1.CertificateStatus.ACTIVE);

        when(certificadoRepository.findByIdAndCompanyId(id, companyId)).thenReturn(Optional.of(existing));
        when(certificadoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        String newPfxBase64 = "newbase64pfx";
        String newPassword = "newsenha123";

        // When
        certificadoService.updateCertificate(id, companyId, null, newPfxBase64, newPassword, -1);

        // Then
        verify(certificadoRepository).save(argThat(c ->
                !newPfxBase64.equals(c.pfxEncrypted()) &&
                !newPassword.equals(c.passwordEncrypted())
        ));
    }

    @Test
    void deleteCertificate_RemovesCertificate() {
        // Given
        String id = "cert-1";
        String companyId = "company-1";
        CertificadoDigitalA1 existing = createCertificate(companyId, CertificadoDigitalA1.CertificateStatus.ACTIVE);

        when(certificadoRepository.findByIdAndCompanyId(id, companyId)).thenReturn(Optional.of(existing));

        // When
        certificadoService.deleteCertificate(id, companyId);

        // Then
        verify(certificadoRepository).delete(existing);
    }

    @Test
    void decryptPfx_ReturnsOriginalPfx() {
        // Given
        String originalPfx = "original-pfx-content";
        String encrypted = certificadoService.encrypt(originalPfx); // Uses internal encryption

        // When
        String decrypted = certificadoService.decryptPfx(encrypted);

        // Then
        assertEquals(originalPfx, decrypted);
    }

    @Test
    void decryptPassword_ReturnsOriginalPassword() {
        // Given
        String originalPassword = "senha123";
        String encrypted = certificadoService.encrypt(originalPassword);

        // When
        String decrypted = certificadoService.decryptPassword(encrypted);

        // Then
        assertEquals(originalPassword, decrypted);
    }

    @Test
    void encryptDecrypt_RoundTrip() {
        // Given
        String original = "test-data-" + UUID.randomUUID();

        // When
        String encrypted = certificadoService.encrypt(original);
        String decrypted = certificadoService.decrypt(encrypted);

        // Then
        assertEquals(original, decrypted);
    }

    private CertificadoDigitalA1 createCertificate(String companyId, CertificadoDigitalA1.CertificateStatus status) {
        return new CertificadoDigitalA1(
                UUID.randomUUID().toString(),
                companyId,
                "alias-1",
                "encrypted-pfx",
                "encrypted-password",
                "12345678000195",
                "Empresa Teste",
                LocalDate.now().plusYears(1),
                status,
                "123456", "CN=CA", "CN=Teste", "ABCDEF123456",
                LocalDateTime.now(), LocalDateTime.now(), null, 30
        );
    }
}