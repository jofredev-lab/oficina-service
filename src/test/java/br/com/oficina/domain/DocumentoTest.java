package br.com.oficina.domain;

import br.com.oficina.domain.cliente.Documento;
import br.com.oficina.domain.cliente.TipoDocumento;
import br.com.oficina.domain.shared.RegraNegocioException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DocumentoTest {

    @Test
    void aceitaCpfValidoComMascaraENormaliza() {
        Documento d = Documento.of("529.982.247-25");
        assertEquals("52998224725", d.getNumero());
        assertEquals(TipoDocumento.CPF, d.getTipo());
    }

    @Test
    void aceitaCnpjValidoComMascara() {
        Documento d = Documento.of("11.222.333/0001-81");
        assertEquals("11222333000181", d.getNumero());
        assertEquals(TipoDocumento.CNPJ, d.getTipo());
    }

    @ParameterizedTest
    @ValueSource(strings = {"529.982.247-24", "111.111.111-11", "000.000.000-00"})
    void rejeitaCpfInvalido(String cpf) {
        RegraNegocioException ex = assertThrows(RegraNegocioException.class, () -> Documento.of(cpf));
        assertEquals("CPF inválido", ex.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"11.222.333/0001-82", "00.000.000/0000-00"})
    void rejeitaCnpjInvalido(String cnpj) {
        RegraNegocioException ex = assertThrows(RegraNegocioException.class, () -> Documento.of(cnpj));
        assertEquals("CNPJ inválido", ex.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"123", "abc", "123456789012"})
    void rejeitaQuantidadeDeDigitosInvalida(String valor) {
        assertThrows(RegraNegocioException.class, () -> Documento.of(valor));
    }

    @Test
    void rejeitaNuloOuEmBranco() {
        assertThrows(RegraNegocioException.class, () -> Documento.of(null));
        assertThrows(RegraNegocioException.class, () -> Documento.of("  "));
    }

    @Test
    void igualdadePorValor() {
        assertEquals(Documento.of("529.982.247-25"), Documento.of("52998224725"));
        assertEquals(Documento.of("529.982.247-25").hashCode(), Documento.of("52998224725").hashCode());
        assertEquals("52998224725", Documento.of("529.982.247-25").toString());
    }
}
