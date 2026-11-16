package br.com.oficina.domain.ordemservico;

import br.com.oficina.domain.peca.Peca;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/** Entidade interna do agregado OrdemServico: peça/insumo incluído, com preço "congelado" no momento da inclusão. */
@Entity
@Table(name = "item_peca")
public class ItemPeca {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "peca_id")
    private Peca peca;

    @Column(name = "nome_peca", nullable = false, length = 120)
    private String nomePeca;

    @Column(name = "preco_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal precoUnitario;

    @Column(nullable = false)
    private int quantidade;

    protected ItemPeca() {
    }

    ItemPeca(Peca peca, int quantidade) {
        this.peca = peca;
        this.nomePeca = peca.getNome();
        this.precoUnitario = peca.getPreco();
        this.quantidade = quantidade;
    }

    public BigDecimal subtotal() {
        return precoUnitario.multiply(BigDecimal.valueOf(quantidade));
    }

    public Long getId() {
        return id;
    }

    public Peca getPeca() {
        return peca;
    }

    public String getNomePeca() {
        return nomePeca;
    }

    public BigDecimal getPrecoUnitario() {
        return precoUnitario;
    }

    public int getQuantidade() {
        return quantidade;
    }
}
