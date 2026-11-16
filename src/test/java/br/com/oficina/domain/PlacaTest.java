package br.com.oficina.domain;

import br.com.oficina.domain.shared.RegraNegocioException;
import br.com.oficina.domain.veiculo.Placa;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PlacaTest {

    @Test
    void aceitaPadraoAntigoComHifenENormaliza() {
        assertEquals("ABC1234", Placa.of("abc-1234").getNumero());
    }

    @Test
    void aceitaPadraoMercosul() {
        assertEquals("ABC1D23", Placa.of("ABC1D23").getNumero());
    }

    @ParameterizedTest
    @ValueSource(strings = {"AB12345", "ABCD123", "1234ABC", "ABC12", "ABC-12345"})
    void rejeitaPlacaInvalida(String valor) {
        assertThrows(RegraNegocioException.class, () -> Placa.of(valor));
    }

    @Test
    void rejeitaNuloOuEmBranco() {
        assertThrows(RegraNegocioException.class, () -> Placa.of(null));
        assertThrows(RegraNegocioException.class, () -> Placa.of(" "));
    }

    @Test
    void igualdadePorValor() {
        assertEquals(Placa.of("abc1d23"), Placa.of("ABC-1D23"));
        assertEquals(Placa.of("abc1d23").hashCode(), Placa.of("ABC1D23").hashCode());
        assertEquals("ABC1D23", Placa.of("abc1d23").toString());
    }
}
