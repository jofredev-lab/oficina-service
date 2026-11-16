package br.com.oficina.domain.peca;

import br.com.oficina.domain.shared.RegraNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Agregado Peça/Insumo com controle de estoque.
 * O campo {@code versao} habilita lock otimista e evita baixas concorrentes inconsistentes.
 */
@Entity
@Table(name = "peca")
public class Peca {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String codigo;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal preco;

    @Column(name = "quantidade_estoque", nullable = false)
    private int quantidadeEstoque;

    @Column(name = "estoque_minimo", nullable = false)
    private int estoqueMinimo;

    @Version
    private Long versao;

    protected Peca() {
    }

    public Peca(String codigo, String nome, BigDecimal preco, int quantidadeEstoque, int estoqueMinimo) {
        preencher(codigo, nome, preco, quantidadeEstoque, estoqueMinimo);
    }

    public void atualizar(String codigo, String nome, BigDecimal preco, int quantidadeEstoque, int estoqueMinimo) {
        preencher(codigo, nome, preco, quantidadeEstoque, estoqueMinimo);
    }

    private void preencher(String codigo, String nome, BigDecimal preco, int quantidadeEstoque, int estoqueMinimo) {
        if (codigo == null || codigo.isBlank()) {
            throw new RegraNegocioException("Código da peça é obrigatório");
        }
        if (nome == null || nome.isBlank()) {
            throw new RegraNegocioException("Nome da peça é obrigatório");
        }
        if (preco == null || preco.signum() < 0) {
            throw new RegraNegocioException("Preço da peça não pode ser negativo");
        }
        if (quantidadeEstoque < 0 || estoqueMinimo < 0) {
            throw new RegraNegocioException("Estoque não pode ser negativo");
        }
        this.codigo = codigo.trim().toUpperCase();
        this.nome = nome.trim();
        this.preco = preco.setScale(2, RoundingMode.HALF_UP);
        this.quantidadeEstoque = quantidadeEstoque;
        this.estoqueMinimo = estoqueMinimo;
    }

    public void baixarEstoque(int quantidade) {
        validarQuantidade(quantidade);
        if (quantidade > quantidadeEstoque) {
            throw new RegraNegocioException("Estoque insuficiente da peça " + codigo
                    + ": disponível " + quantidadeEstoque + ", solicitado " + quantidade);
        }
        quantidadeEstoque -= quantidade;
    }

    public void reporEstoque(int quantidade) {
        validarQuantidade(quantidade);
        quantidadeEstoque += quantidade;
    }

    public boolean isEstoqueBaixo() {
        return quantidadeEstoque <= estoqueMinimo;
    }

    private void validarQuantidade(int quantidade) {
        if (quantidade <= 0) {
            throw new RegraNegocioException("Quantidade deve ser maior que zero");
        }
    }

    public Long getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNome() {
        return nome;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public int getQuantidadeEstoque() {
        return quantidadeEstoque;
    }

    public int getEstoqueMinimo() {
        return estoqueMinimo;
    }
}
