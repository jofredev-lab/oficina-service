package br.com.oficina.domain;

import br.com.oficina.domain.cliente.Cliente;
import br.com.oficina.domain.cliente.Documento;
import br.com.oficina.domain.peca.Peca;
import br.com.oficina.domain.servico.Servico;
import br.com.oficina.domain.shared.RegraNegocioException;
import br.com.oficina.domain.veiculo.Placa;
import br.com.oficina.domain.veiculo.Veiculo;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CadastrosTest {

    private final Documento cpf = Documento.of("529.982.247-25");

    @Test
    void clienteValidaCamposObrigatorios() {
        assertThrows(RegraNegocioException.class, () -> new Cliente(" ", cpf, null, null));
        assertThrows(RegraNegocioException.class, () -> new Cliente("João", null, null, null));
        Cliente c = new Cliente("  João  ", cpf, "joao@email.com", "61999999999");
        assertEquals("João", c.getNome());
        assertEquals("joao@email.com", c.getEmail());
        assertEquals("61999999999", c.getTelefone());
        c.atualizar("João da Silva", cpf, null, null);
        assertEquals("João da Silva", c.getNome());
    }

    @Test
    void veiculoValidaCamposObrigatorios() {
        Cliente c = new Cliente("João", cpf, null, null);
        Placa placa = Placa.of("ABC1D23");
        assertThrows(RegraNegocioException.class, () -> new Veiculo(null, "Fiat", "Uno", 2015, c));
        assertThrows(RegraNegocioException.class, () -> new Veiculo(placa, " ", "Uno", 2015, c));
        assertThrows(RegraNegocioException.class, () -> new Veiculo(placa, "Fiat", "", 2015, c));
        assertThrows(RegraNegocioException.class, () -> new Veiculo(placa, "Fiat", "Uno", 1800, c));
        assertThrows(RegraNegocioException.class, () -> new Veiculo(placa, "Fiat", "Uno", null, c));
        assertThrows(RegraNegocioException.class, () -> new Veiculo(placa, "Fiat", "Uno", 2015, null));
        Veiculo v = new Veiculo(placa, "Fiat", "Uno", 2015, c);
        assertEquals("Fiat", v.getMarca());
        assertEquals("Uno", v.getModelo());
        assertEquals(2015, v.getAno());
        assertEquals(c, v.getCliente());
        assertEquals(placa, v.getPlaca());
        v.atualizar(Placa.of("DEF4567"), "VW", "Gol", 2020, c);
        assertEquals("Gol", v.getModelo());
    }

    @Test
    void servicoValidaCamposObrigatorios() {
        assertThrows(RegraNegocioException.class, () -> new Servico("", "d", BigDecimal.ONE));
        assertThrows(RegraNegocioException.class, () -> new Servico("Troca", "d", new BigDecimal("-1")));
        assertThrows(RegraNegocioException.class, () -> new Servico("Troca", "d", null));
        Servico s = new Servico("Troca de óleo", "desc", new BigDecimal("80"));
        assertEquals(new BigDecimal("80.00"), s.getPreco());
        assertEquals("desc", s.getDescricao());
        s.atualizar("Alinhamento", "novo", new BigDecimal("120.5"));
        assertEquals("Alinhamento", s.getNome());
        assertEquals(new BigDecimal("120.50"), s.getPreco());
    }

    @Test
    void pecaValidaCamposObrigatorios() {
        assertThrows(RegraNegocioException.class, () -> new Peca(" ", "Filtro", BigDecimal.ONE, 1, 1));
        assertThrows(RegraNegocioException.class, () -> new Peca("F1", "", BigDecimal.ONE, 1, 1));
        assertThrows(RegraNegocioException.class, () -> new Peca("F1", "Filtro", new BigDecimal("-2"), 1, 1));
        assertThrows(RegraNegocioException.class, () -> new Peca("F1", "Filtro", null, 1, 1));
        assertThrows(RegraNegocioException.class, () -> new Peca("F1", "Filtro", BigDecimal.ONE, -1, 1));
        assertThrows(RegraNegocioException.class, () -> new Peca("F1", "Filtro", BigDecimal.ONE, 1, -1));
        Peca p = new Peca(" f1 ", "Filtro", new BigDecimal("25.5"), 10, 2);
        assertEquals("F1", p.getCodigo());
        assertEquals(new BigDecimal("25.50"), p.getPreco());
        assertEquals(2, p.getEstoqueMinimo());
        p.atualizar("F2", "Filtro de ar", BigDecimal.TEN, 4, 1);
        assertEquals("F2", p.getCodigo());
        assertEquals(4, p.getQuantidadeEstoque());
    }

    @Test
    void controleDeEstoque() {
        Peca p = new Peca("F1", "Filtro", BigDecimal.TEN, 10, 3);
        p.baixarEstoque(4);
        assertEquals(6, p.getQuantidadeEstoque());
        assertFalse(p.isEstoqueBaixo());
        p.baixarEstoque(3);
        assertTrue(p.isEstoqueBaixo());
        p.reporEstoque(10);
        assertEquals(13, p.getQuantidadeEstoque());
        assertFalse(p.isEstoqueBaixo());
    }

    @Test
    void naoPermiteBaixaAcimaDoEstoqueNemQuantidadeInvalida() {
        Peca p = new Peca("F1", "Filtro", BigDecimal.TEN, 2, 1);
        RegraNegocioException ex = assertThrows(RegraNegocioException.class, () -> p.baixarEstoque(3));
        assertTrue(ex.getMessage().contains("Estoque insuficiente"));
        assertEquals(2, p.getQuantidadeEstoque());
        assertThrows(RegraNegocioException.class, () -> p.baixarEstoque(0));
        assertThrows(RegraNegocioException.class, () -> p.reporEstoque(-1));
    }
}
