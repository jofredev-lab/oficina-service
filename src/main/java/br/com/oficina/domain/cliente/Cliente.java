package br.com.oficina.domain.cliente;

import br.com.oficina.domain.shared.RegraNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Agregado Cliente, identificado de forma única por CPF/CNPJ. */
@Entity
@Table(name = "cliente")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Embedded
    private Documento documento;

    @Column(length = 150)
    private String email;

    @Column(length = 20)
    private String telefone;

    protected Cliente() {
    }

    public Cliente(String nome, Documento documento, String email, String telefone) {
        preencher(nome, documento, email, telefone);
    }

    public void atualizar(String nome, Documento documento, String email, String telefone) {
        preencher(nome, documento, email, telefone);
    }

    private void preencher(String nome, Documento documento, String email, String telefone) {
        if (nome == null || nome.isBlank()) {
            throw new RegraNegocioException("Nome do cliente é obrigatório");
        }
        if (documento == null) {
            throw new RegraNegocioException("CPF/CNPJ do cliente é obrigatório");
        }
        this.nome = nome.trim();
        this.documento = documento;
        this.email = email;
        this.telefone = telefone;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public Documento getDocumento() {
        return documento;
    }

    public String getEmail() {
        return email;
    }

    public String getTelefone() {
        return telefone;
    }
}
