package com.empresa.broker.util;

import com.empresa.broker.exception.ValidationException;

public final class CpfUtil {

    private CpfUtil() {
    }

    public static String normalizar(String cpf) {
        if (cpf == null) {
            return null;
        }
        return cpf.replaceAll("\\D", "");
    }

    public static boolean isValido(String cpf) {
        String normalizado = normalizar(cpf);
        if (normalizado == null || normalizado.length() != 11) {
            return false;
        }
        if (normalizado.chars().distinct().count() == 1) {
            return false;
        }
        int primeiroDigito = calcularDigito(normalizado, 9);
        int segundoDigito = calcularDigito(normalizado, 10);
        return Character.getNumericValue(normalizado.charAt(9)) == primeiroDigito
                && Character.getNumericValue(normalizado.charAt(10)) == segundoDigito;
    }

    public static String validarENormalizar(String cpf) {
        String normalizado = normalizar(cpf);
        if (!isValido(normalizado)) {
            throw new ValidationException("CPF invalido: " + cpf);
        }
        return normalizado;
    }

    private static int calcularDigito(String cpf, int tamanho) {
        int soma = 0;
        for (int i = 0; i < tamanho; i++) {
            soma += Character.getNumericValue(cpf.charAt(i)) * (tamanho + 1 - i);
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }
}
