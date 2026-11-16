package br.com.oficina.domain.veiculo;

import br.com.oficina.domain.cliente.Cliente;
import br.com.oficina.domain.shared.RegraNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Year;

/** Agregado Veículo, sempre vinculado a um cliente. */
@Entity
@Table(name = "veiculo")
public class Veiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private Placa placa;

    @Column(nullable = false, length = 60)
    private String marca;

    @Column(nullable = false, length = 60)
    private String modelo;

    @Column(nullable = false)
    private Integer ano;

    @ManyToOne(optional = false)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    protected Veiculo() {
    }

    public Veiculo(Placa placa, String marca, String modelo, Integer ano, Cliente cliente) {
        preencher(placa, marca, modelo, ano, cliente);
    }

    public void atualizar(Placa placa, String marca, String modelo, Integer ano, Cliente cliente) {
        preencher(placa, marca, modelo, ano, cliente);
    }

    private void preencher(Placa placa, String marca, String modelo, Integer ano, Cliente cliente) {
        if (placa == null) {
            throw new RegraNegocioException("Placa é obrigatória");
        }
        if (marca == null || marca.isBlank()) {
            throw new RegraNegocioException("Marca do veículo é obrigatória");
        }
        if (modelo == null || modelo.isBlank()) {
            throw new RegraNegocioException("Modelo do veículo é obrigatório");
        }
        if (ano == null || ano < 1900 || ano > Year.now().getValue() + 1) {
            throw new RegraNegocioException("Ano do veículo inválido");
        }
        if (cliente == null) {
            throw new RegraNegocioException("Veículo deve pertencer a um cliente");
        }
        this.placa = placa;
        this.marca = marca.trim();
        this.modelo = modelo.trim();
        this.ano = ano;
        this.cliente = cliente;
    }

    public Long getId() {
        return id;
    }

    public Placa getPlaca() {
        return placa;
    }

    public String getMarca() {
        return marca;
    }

    public String getModelo() {
        return modelo;
    }

    public Integer getAno() {
        return ano;
    }

    public Cliente getCliente() {
        return cliente;
    }
}
