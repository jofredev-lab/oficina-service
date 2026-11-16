package br.com.oficina.domain.veiculo;

import br.com.oficina.domain.shared.RegraNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

/** Objeto de valor: placa no padrão antigo (ABC1234) ou Mercosul (ABC1D23). */
@Embeddable
public class Placa {

    private static final Pattern PADRAO = Pattern.compile("^[A-Z]{3}[0-9][A-Z0-9][0-9]{2}$");

    @Column(name = "placa", nullable = false, length = 7, unique = true)
    private String numero;

    protected Placa() {
    }

    private Placa(String numero) {
        this.numero = numero;
    }

    public static Placa of(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new RegraNegocioException("Placa é obrigatória");
        }
        String normalizada = valor.replaceAll("[\\s-]", "").toUpperCase(Locale.ROOT);
        if (!PADRAO.matcher(normalizada).matches()) {
            throw new RegraNegocioException("Placa inválida: use o formato ABC1234 ou ABC1D23");
        }
        return new Placa(normalizada);
    }

    public String getNumero() {
        return numero;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Placa outra && Objects.equals(numero, outra.numero);
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
