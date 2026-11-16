package br.com.oficina.domain.ordemservico;

import br.com.oficina.domain.servico.Servico;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/** Entidade interna do agregado OrdemServico: serviço incluído, com preço "congelado" no momento da inclusão. */
@Entity
@Table(name = "item_servico")
public class ItemServico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "servico_id")
    private Servico servico;

    @Column(name = "nome_servico", nullable = false, length = 120)
    private String nomeServico;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal preco;

    protected ItemServico() {
    }

    ItemServico(Servico servico) {
        this.servico = servico;
        this.nomeServico = servico.getNome();
        this.preco = servico.getPreco();
    }

    public Long getId() {
        return id;
    }

    public Servico getServico() {
        return servico;
    }

    public String getNomeServico() {
        return nomeServico;
    }

    public BigDecimal getPreco() {
        return preco;
    }
}
