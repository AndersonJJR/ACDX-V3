package com.erp.brazil.hr.infrastructure.aop;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.Pattern;

@Service
@Slf4j
public class DataMaskingService {

    @Value("${app.audit.masking.enabled:true}")
    private boolean maskingEnabled;

    @Value("${app.audit.masking.fields:cpf,rg,email,phone,bank_account,pix_key,address,mother_name,father_name}")
    private String[] sensitiveFields;

    private static final Set<String> SENSITIVE_FIELD_NAMES = Set.of(
        "cpf", "rg", "email", "phone", "personalPhone", "corporatePhone",
        "bankAccount", "bankBranch", "bankCode", "pixKey",
        "address", "addressStreet", "addressNumber", "addressNeighborhood",
        "addressCity", "addressState", "addressCep",
        "motherName", "fatherName",
        "emergencyContactPhone", "emergencyContactName",
        "password", "token", "secret", "key"
    );

    private static final Pattern CPF_PATTERN = Pattern.compile("\\d{3}\\.?\\d{3}\\.?\\d{3}-?\\d{2}");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("[^@]+@[^@]+\\.[^@]+");
    private static final Pattern PHONE_PATTERN = Pattern.compile("\\+?\\d{10,15}");

    public Map<String, Object> maskSensitiveData(Map<String, Object> data) {
        if (!maskingEnabled || data == null) {
            return data;
        }

        Map<String, Object> masked = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            masked.put(entry.getKey(), maskValue(entry.getKey(), entry.getValue()));
        }
        return masked;
    }

    public List<String> getMaskedFields(Map<String, Object> data) {
        List<String> maskedFields = new ArrayList<>();
        if (!maskingEnabled || data == null) {
            return maskedFields;
        }

        for (String key : data.keySet()) {
            if (isSensitiveField(key)) {
                maskedFields.add(key);
            }
        }
        return maskedFields;
    }

    private Object maskValue(String fieldName, Object value) {
        if (value == null) return null;

        if (isSensitiveField(fieldName)) {
            return maskByType(fieldName, value.toString());
        }

        // Recursively mask nested objects
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> maskedMap = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                maskedMap.put(entry.getKey().toString(), maskValue(entry.getKey().toString(), entry.getValue()));
            }
            return maskedMap;
        }

        if (value instanceof Collection<?> collection) {
            List<Object> maskedList = new ArrayList<>();
            for (Object item : collection) {
                if (item instanceof Map<?, ?>) {
                    maskedList.add(maskValue("item", item));
                } else {
                    maskedList.add(item);
                }
            }
            return maskedList;
        }

        return value;
    }

    private boolean isSensitiveField(String fieldName) {
        String lower = fieldName.toLowerCase();
        for (String sensitive : SENSITIVE_FIELD_NAMES) {
            if (lower.contains(sensitive.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private String maskByType(String fieldName, String value) {
        String lower = fieldName.toLowerCase();

        if (lower.contains("cpf")) {
            return maskCpf(value);
        }
        if (lower.contains("email")) {
            return maskEmail(value);
        }
        if (lower.contains("phone") || lower.contains("celular") || lower.contains("telefone")) {
            return maskPhone(value);
        }
        if (lower.contains("bank") || lower.contains("conta") || lower.contains("agencia")) {
            return maskBankAccount(value);
        }
        if (lower.contains("pix")) {
            return maskPixKey(value);
        }
        if (lower.contains("address") || lower.contains("endereco")) {
            return maskAddress(value);
        }
        if (lower.contains("mother") || lower.contains("father") || lower.contains("mae") || lower.contains("pai")) {
            return maskName(value);
        }

        return "***MASKED***";
    }

    private String maskCpf(String cpf) {
        if (cpf == null || cpf.length() < 11) return "***";
        String digits = cpf.replaceAll("[^0-9]", "");
        if (digits.length() != 11) return "***";
        return digits.substring(0, 3) + "***.***" + digits.substring(9);
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "***";
        String[] parts = email.split("@");
        String user = parts[0];
        if (user.length() <= 2) return "***@" + parts[1];
        return user.substring(0, 2) + "***" + "@" + parts[1];
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) return "***";
        String digits = phone.replaceAll("[^0-9]", "");
        if (digits.length() < 4) return "***";
        return "***" + digits.substring(digits.length() - 4);
    }

    private String maskBankAccount(String account) {
        if (account == null || account.length() < 4) return "***";
        return "***" + account.substring(account.length() - 4);
    }

    private String maskPixKey(String pixKey) {
        if (pixKey == null || pixKey.length() < 4) return "***";
        if (EMAIL_PATTERN.matcher(pixKey).matches()) {
            return maskEmail(pixKey);
        }
        if (CPF_PATTERN.matcher(pixKey).matches()) {
            return maskCpf(pixKey);
        }
        if (PHONE_PATTERN.matcher(pixKey).matches()) {
            return maskPhone(pixKey);
        }
        return "***" + pixKey.substring(pixKey.length() - 4);
    }

    private String maskAddress(String address) {
        if (address == null || address.length() < 5) return "***";
        return address.substring(0, Math.min(10, address.length())) + "...";
    }

    private String maskName(String name) {
        if (name == null || name.length() < 3) return "***";
        String[] parts = name.split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, 1) + "***";
        }
        return parts[0] + " " + parts[parts.length - 1].substring(0, 1) + "***";
    }
}