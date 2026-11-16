package br.com.oficina.domain.servico;

import br.com.oficina.domain.shared.RegraNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Serviço do catálogo da oficina (troca de óleo, alinhamento etc.). */
@Entity
@Table(name = "servico")
public class Servico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(length = 500)
    private String descricao;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal preco;

    protected Servico() {
    }

    public Servico(String nome, String descricao, BigDecimal preco) {
        preencher(nome, descricao, preco);
    }

    public void atualizar(String nome, String descricao, BigDecimal preco) {
        preencher(nome, descricao, preco);
    }

    private void preencher(String nome, String descricao, BigDecimal preco) {
        if (nome == null || nome.isBlank()) {
            throw new RegraNegocioException("Nome do serviço é obrigatório");
        }
        if (preco == null || preco.signum() < 0) {
            throw new RegraNegocioException("Preço do serviço não pode ser negativo");
        }
        this.nome = nome.trim();
        this.descricao = descricao;
        this.preco = preco.setScale(2, RoundingMode.HALF_UP);
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public BigDecimal getPreco() {
        return preco;
    }
}
