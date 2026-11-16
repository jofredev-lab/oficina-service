package br.com.oficina.domain.cliente;

import br.com.oficina.domain.shared.RegraNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

/** Objeto de valor: CPF ou CNPJ válido, armazenado somente com dígitos. */
@Embeddable
public class Documento {

    private static final int[] PESOS_CNPJ_1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
    private static final int[] PESOS_CNPJ_2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

    @Column(name = "documento", nullable = false, length = 14, unique = true)
    private String numero;

    protected Documento() {
    }

    private Documento(String numero) {
        this.numero = numero;
    }

    public static Documento of(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new RegraNegocioException("CPF/CNPJ é obrigatório");
        }
        String digitos = valor.replaceAll("\\D", "");
        switch (digitos.length()) {
            case 11 -> {
                if (!cpfValido(digitos)) {
                    throw new RegraNegocioException("CPF inválido");
                }
            }
            case 14 -> {
                if (!cnpjValido(digitos)) {
                    throw new RegraNegocioException("CNPJ inválido");
                }
            }
            default -> throw new RegraNegocioException("CPF/CNPJ deve conter 11 (CPF) ou 14 (CNPJ) dígitos");
        }
        return new Documento(digitos);
    }

    public String getNumero() {
        return numero;
    }

    public TipoDocumento getTipo() {
        return numero.length() == 11 ? TipoDocumento.CPF : TipoDocumento.CNPJ;
    }

    private static boolean cpfValido(String c) {
        if (todosIguais(c)) {
            return false;
        }
        return digitoCpf(c, 9, 10) == c.charAt(9) - '0'
                && digitoCpf(c, 10, 11) == c.charAt(10) - '0';
    }

    private static int digitoCpf(String c, int tamanho, int pesoInicial) {
        int soma = 0;
        for (int i = 0; i < tamanho; i++) {
            soma += (c.charAt(i) - '0') * (pesoInicial - i);
        }
        int resto = (soma * 10) % 11;
        return resto == 10 ? 0 : resto;
    }

    private static boolean cnpjValido(String c) {
        if (todosIguais(c)) {
            return false;
        }
        return digitoCnpj(c, PESOS_CNPJ_1) == c.charAt(12) - '0'
                && digitoCnpj(c, PESOS_CNPJ_2) == c.charAt(13) - '0';
    }

    private static int digitoCnpj(String c, int[] pesos) {
        int soma = 0;
        for (int i = 0; i < pesos.length; i++) {
            soma += (c.charAt(i) - '0') * pesos[i];
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }

    private static boolean todosIguais(String c) {
        return c.chars().distinct().count() == 1;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Documento outro && Objects.equals(numero, outro.numero);
    }

    @Override
    public int hashCode() {
        return Objects.hash(numero);
    }

    @Override
    public String toString() {
        return numero;
    }
}
