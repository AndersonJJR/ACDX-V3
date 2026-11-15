package com.erp.brazil.common.domain.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * Brazilian document and data validators.
 * Provides validation for CPF, CNPJ, CEP, Phone numbers, and State Registration (IE).
 */
public final class BrazilianValidators {

    private static final Pattern CPF_PATTERN = Pattern.compile("^\\d{3}\\.?\\d{3}\\.?\\d{3}-?\\d{2}$");
    private static final Pattern CNPJ_PATTERN = Pattern.compile("^\\d{2}\\.?\\d{3}\\.?\\d{3}/?\\d{4}-?\\d{2}$");
    private static final Pattern CEP_PATTERN = Pattern.compile("^\\d{5}-?\\d{3}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\(?\\d{2}\\)?\\s?9?\\d{4}-?\\d{4}$");
    private static final Pattern PHONE_LANDLINE_PATTERN = Pattern.compile("^\\(?\\d{2}\\)?\\s?\\d{4}-?\\d{4}$");

    private BrazilianValidators() {
    }

    /**
     * Validates a CPF (Cadastro de Pessoas Físicas).
     *
     * @param cpf the CPF to validate (with or without formatting)
     * @return true if valid, false otherwise
     */
    public static boolean isValidCpf(String cpf) {
        if (cpf == null) {
            return false;
        }
        String digits = cpf.replaceAll("[^0-9]", "");
        if (digits.length() != 11) {
            return false;
        }
        if (isAllDigitsSame(digits)) {
            return false;
        }
        return calculateCpfCheckDigits(digits).equals(digits.substring(9));
    }

    /**
     * Validates a CNPJ (Cadastro Nacional da Pessoa Jurídica).
     *
     * @param cnpj the CNPJ to validate (with or without formatting)
     * @return true if valid, false otherwise
     */
    public static boolean isValidCnpj(String cnpj) {
        if (cnpj == null) {
            return false;
        }
        String digits = cnpj.replaceAll("[^0-9]", "");
        if (digits.length() != 14) {
            return false;
        }
        if (isAllDigitsSame(digits)) {
            return false;
        }
        return calculateCnpjCheckDigits(digits).equals(digits.substring(12));
    }

    /**
     * Validates a CEP (Código de Endereçamento Postal).
     *
     * @param cep the CEP to validate (with or without formatting)
     * @return true if valid, false otherwise
     */
    public static boolean isValidCep(String cep) {
        if (cep == null) {
            return false;
        }
        String digits = cep.replaceAll("[^0-9]", "");
        return digits.length() == 8;
    }

    /**
     * Validates a Brazilian phone number (mobile or landline).
     *
     * @param phone the phone number to validate (with or without formatting)
     * @return true if valid, false otherwise
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null) {
            return false;
        }
        String digits = phone.replaceAll("[^0-9]", "");
        if (digits.length() < 10 || digits.length() > 11) {
            return false;
        }
        // Check DDD (area code) - valid range 11-99
        int ddd = Integer.parseInt(digits.substring(0, 2));
        return ddd >= 11 && ddd <= 99;
    }

    /**
     * Validates a Brazilian mobile phone number (9 digits after DDD).
     *
     * @param phone the mobile phone number to validate
     * @return true if valid mobile, false otherwise
     */
    public static boolean isValidMobilePhone(String phone) {
        if (phone == null) {
            return false;
        }
        String digits = phone.replaceAll("[^0-9]", "");
        if (digits.length() != 11) {
            return false;
        }
        // Mobile numbers have 9 as the first digit after DDD
        return digits.charAt(2) == '9';
    }

    /**
     * Validates a State Registration (Inscrição Estadual) for a given state.
     *
     * @param ie   the state registration number
     * @param uf   the state abbreviation (e.g., "SP", "RJ", "MG")
     * @return true if valid, false otherwise
     */
    public static boolean isValidStateRegistration(String ie, String uf) {
        if (ie == null || uf == null) {
            return false;
        }
        String digits = ie.replaceAll("[^0-9]", "");
        String state = uf.toUpperCase();

        return switch (state) {
            case "SP" -> validateIeSP(digits);
            case "RJ" -> validateIeRJ(digits);
            case "MG" -> validateIeMG(digits);
            case "RS" -> validateIeRS(digits);
            case "PR" -> validateIePR(digits);
            case "SC" -> validateIeSC(digits);
            case "BA" -> validateIeBA(digits);
            case "GO" -> validateIeGO(digits);
            case "PE" -> validateIePE(digits);
            case "CE" -> validateIeCE(digits);
            case "PA" -> validateIePA(digits);
            case "ES" -> validateIeES(digits);
            case "MA" -> validateIeMA(digits);
            case "MT" -> validateIeMT(digits);
            case "MS" -> validateIeMS(digits);
            case "DF" -> validateIeDF(digits);
            default -> digits.length() >= 8 && digits.length() <= 14;
        };
    }

    /**
     * Formats a CPF with standard formatting (XXX.XXX.XXX-XX).
     *
     * @param cpf the CPF digits only
     * @return formatted CPF
     */
    public static String formatCpf(String cpf) {
        if (cpf == null) {
            return null;
        }
        String digits = cpf.replaceAll("[^0-9]", "");
        if (digits.length() != 11) {
            return cpf;
        }
        return digits.substring(0, 3) + "." +
                digits.substring(3, 6) + "." +
                digits.substring(6, 9) + "-" +
                digits.substring(9);
    }

    /**
     * Formats a CNPJ with standard formatting (XX.XXX.XXX/XXXX-XX).
     *
     * @param cnpj the CNPJ digits only
     * @return formatted CNPJ
     */
    public static String formatCnpj(String cnpj) {
        if (cnpj == null) {
            return null;
        }
        String digits = cnpj.replaceAll("[^0-9]", "");
        if (digits.length() != 14) {
            return cnpj;
        }
        return digits.substring(0, 2) + "." +
                digits.substring(2, 5) + "." +
                digits.substring(5, 8) + "/" +
                digits.substring(8, 12) + "-" +
                digits.substring(12);
    }

    /**
     * Formats a CEP with standard formatting (XXXXX-XXX).
     *
     * @param cep the CEP digits only
     * @return formatted CEP
     */
    public static String formatCep(String cep) {
        if (cep == null) {
            return null;
        }
        String digits = cep.replaceAll("[^0-9]", "");
        if (digits.length() != 8) {
            return cep;
        }
        return digits.substring(0, 5) + "-" + digits.substring(5);
    }

    /**
     * Formats a phone number with standard formatting ((XX) 9XXXX-XXXX or (XX) XXXX-XXXX).
     *
     * @param phone the phone digits only
     * @return formatted phone
     */
    public static String formatPhone(String phone) {
        if (phone == null) {
            return null;
        }
        String digits = phone.replaceAll("[^0-9]", "");
        if (digits.length() == 11) {
            return "(" + digits.substring(0, 2) + ") " +
                    digits.substring(2, 7) + "-" +
                    digits.substring(7);
        } else if (digits.length() == 10) {
            return "(" + digits.substring(0, 2) + ") " +
                    digits.substring(2, 6) + "-" +
                    digits.substring(6);
        }
        return phone;
    }

    /**
     * Calculates CPF check digits.
     */
    private static String calculateCpfCheckDigits(String digits) {
        int sum = 0;
        for (int i = 0; i < 9; i++) {
            sum += Character.getNumericValue(digits.charAt(i)) * (10 - i);
        }
        int firstDigit = 11 - (sum % 11);
        if (firstDigit >= 10) {
            firstDigit = 0;
        }

        sum = 0;
        for (int i = 0; i < 10; i++) {
            sum += Character.getNumericValue(digits.charAt(i)) * (11 - i);
        }
        int secondDigit = 11 - (sum % 11);
        if (secondDigit >= 10) {
            secondDigit = 0;
        }

        return String.valueOf(firstDigit) + secondDigit;
    }

    /**
     * Calculates CNPJ check digits.
     */
    private static String calculateCnpjCheckDigits(String digits) {
        int[] weights1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int[] weights2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

        int sum = 0;
        for (int i = 0; i < 12; i++) {
            sum += Character.getNumericValue(digits.charAt(i)) * weights1[i];
        }
        int firstDigit = 11 - (sum % 11);
        if (firstDigit >= 10) {
            firstDigit = 0;
        }

        sum = 0;
        for (int i = 0; i < 13; i++) {
            sum += Character.getNumericValue(digits.charAt(i)) * weights2[i];
        }
        int secondDigit = 11 - (sum % 11);
        if (secondDigit >= 10) {
            secondDigit = 0;
        }

        return String.valueOf(firstDigit) + secondDigit;
    }

    private static boolean isAllDigitsSame(String digits) {
        char first = digits.charAt(0);
        for (int i = 1; i < digits.length(); i++) {
            if (digits.charAt(i) != first) {
                return false;
            }
        }
        return true;
    }

    // State-specific IE validation methods
    private static boolean validateIeSP(String ie) {
        if (ie.length() != 12) return false;
        // SP validation algorithm
        int[] weights = {1, 3, 4, 5, 6, 7, 8, 10};
        int sum = 0;
        for (int i = 0; i < 8; i++) {
            sum += Character.getNumericValue(ie.charAt(i)) * weights[i];
        }
        int digit = sum % 11;
        if (digit == 10) digit = 0;
        return digit == Character.getNumericValue(ie.charAt(8));
    }

    private static boolean validateIeRJ(String ie) {
        return ie.length() == 8;
    }

    private static boolean validateIeMG(String ie) {
        return ie.length() == 13;
    }

    private static boolean validateIeRS(String ie) {
        return ie.length() == 10;
    }

    private static boolean validateIePR(String ie) {
        return ie.length() == 10;
    }

    private static boolean validateIeSC(String ie) {
        return ie.length() == 9;
    }

    private static boolean validateIeBA(String ie) {
        return ie.length() == 8 || ie.length() == 9;
    }

    private static boolean validateIeGO(String ie) {
        return ie.length() == 9;
    }

    private static boolean validateIePE(String ie) {
        return ie.length() == 9 || ie.length() == 14;
    }

    private static boolean validateIeCE(String ie) {
        return ie.length() == 9;
    }

    private static boolean validateIePA(String ie) {
        return ie.length() == 9;
    }

    private static boolean validateIeES(String ie) {
        return ie.length() == 9;
    }

    private static boolean validateIeMA(String ie) {
        return ie.length() == 9;
    }

    private static boolean validateIeMT(String ie) {
        return ie.length() == 11;
    }

    private static boolean validateIeMS(String ie) {
        return ie.length() == 11;
    }

    private static boolean validateIeDF(String ie) {
        return ie.length() == 13;
    }

    /**
     * Constraint validator for CPF.
     */
    public static class CpfValidator implements ConstraintValidator<ValidCpf, String> {
        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            return value == null || isValidCpf(value);
        }
    }

    /**
     * Constraint validator for CNPJ.
     */
    public static class CnpjValidator implements ConstraintValidator<ValidCnpj, String> {
        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            return value == null || isValidCnpj(value);
        }
    }

    /**
     * Constraint validator for CEP.
     */
    public static class CepValidator implements ConstraintValidator<ValidCep, String> {
        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            return value == null || isValidCep(value);
        }
    }

    /**
     * Constraint validator for Phone.
     */
    public static class PhoneValidator implements ConstraintValidator<ValidPhone, String> {
        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            return value == null || isValidPhone(value);
        }
    }

    /**
     * Constraint validator for State Registration.
     */
    public static class StateRegistrationValidator implements ConstraintValidator<ValidStateRegistration, String> {
        private String uf;

        @Override
        public void initialize(ValidStateRegistration constraintAnnotation) {
            this.uf = constraintAnnotation.uf();
        }

        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            return value == null || isValidStateRegistration(value, uf);
        }
    }
}