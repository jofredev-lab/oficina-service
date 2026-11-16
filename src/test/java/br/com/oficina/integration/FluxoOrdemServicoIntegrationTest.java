package br.com.oficina.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Testes de integração dos fluxos principais (API REST + JPA + segurança JWT, banco H2 em memória). */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FluxoOrdemServicoIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    // ---------- helpers ----------

    private String login() throws Exception {
        MvcResult r = postJson("/api/auth/login", null, Map.of("username", "admin", "password", "admin123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andReturn();
        return JsonPath.read(r.getResponse().getContentAsString(), "$.token");
    }

    private ResultActions postJson(String url, String token, Object body) throws Exception {
        MockHttpServletRequestBuilder req = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON);
        if (token != null) {
            req.header("Authorization", "Bearer " + token);
        }
        if (body != null) {
            req.content(mapper.writeValueAsString(body));
        }
        return mvc.perform(req);
    }

    private ResultActions getJson(String url, String token) throws Exception {
        MockHttpServletRequestBuilder req = get(url);
        if (token != null) {
            req.header("Authorization", "Bearer " + token);
        }
        return mvc.perform(req);
    }

    private JsonNode corpo(ResultActions acao) throws Exception {
        return mapper.readTree(acao.andReturn().getResponse().getContentAsString());
    }

    private long criarCliente(String token, String nome, String documento) throws Exception {
        return corpo(postJson("/api/admin/clientes", token,
                Map.of("nome", nome, "documento", documento, "email", "cliente@email.com", "telefone", "61999999999"))
                .andExpect(status().isCreated())).get("id").asLong();
    }

    private long criarServico(String token, String nome, double preco) throws Exception {
        return corpo(postJson("/api/admin/servicos", token,
                Map.of("nome", nome, "descricao", "descrição", "preco", preco))
                .andExpect(status().isCreated())).get("id").asLong();
    }

    private long criarPeca(String token, String codigo, double preco, int estoque) throws Exception {
        return corpo(postJson("/api/admin/pecas", token,
                Map.of("codigo", codigo, "nome", "Peça " + codigo, "preco", preco,
                        "quantidadeEstoque", estoque, "estoqueMinimo", 1))
                .andExpect(status().isCreated())).get("id").asLong();
    }

    private JsonNode abrirOs(String token, String documento, String placa, long servicoId, long pecaId, int quantidade)
            throws Exception {
        Map<String, Object> body = Map.of(
                "documentoCliente", documento,
                "veiculo", Map.of("placa", placa, "marca", "Fiat", "modelo", "Uno", "ano", 2018),
                "servicosIds", List.of(servicoId),
                "pecas", List.of(Map.of("pecaId", pecaId, "quantidade", quantidade)));
        return corpo(postJson("/api/admin/ordens-servico", token, body).andExpect(status().isCreated()));
    }

    // ---------- testes ----------

    @Test
    void fluxoCompletoDaCriacaoAteAEntregaComAprovacaoDoCliente() throws Exception {
        String token = login();
        String cpf = "529.982.247-25";
        criarClienteSeNecessario(token, cpf);
        long servicoId = criarServico(token, "Troca de óleo", 80.00);
        long pecaId = criarPeca(token, "FO-100", 25.50, 10);

        JsonNode os = abrirOs(token, cpf, "ABC1D23", servicoId, pecaId, 2);
        long osId = os.get("id").asLong();
        String codigo = os.get("codigoAcompanhamento").asText();
        assertEquals("RECEBIDA", os.get("status").asText());
        assertEquals("NAO_GERADO", os.get("orcamentoStatus").asText());
        assertEquals(131.0, os.get("valorTotal").asDouble());
        assertEquals("ABC1D23", os.get("veiculoPlaca").asText());

        postJson("/api/admin/ordens-servico/" + osId + "/diagnostico", token, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_DIAGNOSTICO"));

        postJson("/api/admin/ordens-servico/" + osId + "/orcamento/enviar", token, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("AGUARDANDO_APROVACAO"))
                .andExpect(jsonPath("$.orcamentoStatus").value("PENDENTE"));

        // Cliente consulta o andamento pela API pública (sem JWT)
        getJson("/api/public/acompanhamento/" + codigo, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("AGUARDANDO_APROVACAO"))
                .andExpect(jsonPath("$.valorTotal").value(131.0));

        // Cliente aprova o orçamento: inicia a execução e baixa o estoque
        postJson("/api/public/acompanhamento/" + codigo + "/orcamento/aprovar", null, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_EXECUCAO"))
                .andExpect(jsonPath("$.orcamentoStatus").value("APROVADO"));

        getJson("/api/admin/pecas/" + pecaId, token)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidadeEstoque").value(8));

        postJson("/api/admin/ordens-servico/" + osId + "/finalizar", token, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FINALIZADA"));

        postJson("/api/admin/ordens-servico/" + osId + "/entregar", token, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ENTREGUE"));

        getJson("/api/admin/ordens-servico/" + osId, token)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ENTREGUE"))
                .andExpect(jsonPath("$.servicos.length()").value(1))
                .andExpect(jsonPath("$.pecas.length()").value(1));

        getJson("/api/admin/ordens-servico?status=ENTREGUE", token)
                .andExpect(status().isOk());

        JsonNode metricas = corpo(getJson("/api/admin/metricas/tempo-medio-execucao", token)
                .andExpect(status().isOk()));
        assertTrue(metricas.get("quantidadeOrdens").asLong() >= 1);
    }

    @Test
    void aprovacaoComEstoqueInsuficienteFazRollbackECamposPermanecemInalterados() throws Exception {
        String token = login();
        String cnpj = "11.222.333/0001-81";
        criarClienteSeNecessario(token, cnpj);
        long servicoId = criarServico(token, "Alinhamento", 120.00);
        long pecaId = criarPeca(token, "PA-200", 10.00, 1);

        JsonNode os = abrirOs(token, cnpj, "DEF4567", servicoId, pecaId, 5);
        long osId = os.get("id").asLong();
        String codigo = os.get("codigoAcompanhamento").asText();

        postJson("/api/admin/ordens-servico/" + osId + "/diagnostico", token, null).andExpect(status().isOk());
        postJson("/api/admin/ordens-servico/" + osId + "/orcamento/enviar", token, null).andExpect(status().isOk());

        postJson("/api/public/acompanhamento/" + codigo + "/orcamento/aprovar", null, null)
                .andExpect(status().isUnprocessableEntity());

        // transação revertida: status e estoque não mudaram
        getJson("/api/admin/ordens-servico/" + osId, token)
                .andExpect(jsonPath("$.status").value("AGUARDANDO_APROVACAO"))
                .andExpect(jsonPath("$.orcamentoStatus").value("PENDENTE"));
        getJson("/api/admin/pecas/" + pecaId, token)
                .andExpect(jsonPath("$.quantidadeEstoque").value(1));
    }

    @Test
    void clientePodeRejeitarOrcamentoEAOsEhFinalizadaSemExecucao() throws Exception {
        String token = login();
        String cpf = "111.444.777-35";
        criarClienteSeNecessario(token, cpf);
        long servicoId = criarServico(token, "Revisão", 300.00);
        long pecaId = criarPeca(token, "PR-300", 15.00, 5);

        JsonNode os = abrirOs(token, cpf, "GHI1J23", servicoId, pecaId, 1);
        long osId = os.get("id").asLong();
        String codigo = os.get("codigoAcompanhamento").asText();

        postJson("/api/admin/ordens-servico/" + osId + "/diagnostico", token, null).andExpect(status().isOk());
        postJson("/api/admin/ordens-servico/" + osId + "/orcamento/enviar", token, null).andExpect(status().isOk());

        postJson("/api/public/acompanhamento/" + codigo + "/orcamento/rejeitar", null, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FINALIZADA"))
                .andExpect(jsonPath("$.orcamentoStatus").value("REJEITADO"));

        getJson("/api/admin/pecas/" + pecaId, token)
                .andExpect(jsonPath("$.quantidadeEstoque").value(5));
    }

    @Test
    void naoPermitePularStatus() throws Exception {
        String token = login();
        String cpf = "529.982.247-25";
        criarClienteSeNecessario(token, cpf);
        long servicoId = criarServico(token, "Troca de pastilhas", 150.00);
        long pecaId = criarPeca(token, "PP-400", 40.00, 4);

        JsonNode os = abrirOs(token, cpf, "JKL4M56", servicoId, pecaId, 1);
        long osId = os.get("id").asLong();

        postJson("/api/admin/ordens-servico/" + osId + "/finalizar", token, null)
                .andExpect(status().isUnprocessableEntity());
        postJson("/api/admin/ordens-servico/" + osId + "/orcamento/enviar", token, null)
                .andExpect(status().isUnprocessableEntity());
    }

    private long criarClienteSeNecessario(String token, String cpf) throws Exception {
        JsonNode clientes = corpo(getJson("/api/admin/clientes", token).andExpect(status().isOk()));
        String digitos = cpf.replaceAll("\\D", "");
        for (JsonNode c : clientes) {
            if (digitos.equals(c.get("documento").asText())) {
                return c.get("id").asLong();
            }
        }
        return criarCliente(token, "Cliente " + digitos, cpf);
    }

    @Test
    void rejeitaCpfInvalidoComRegraDeNegocio() throws Exception {
        String token = login();
        postJson("/api/admin/clientes", token,
                Map.of("nome", "Fulano", "documento", "111.111.111-11"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").value("CPF inválido"));
    }

    @Test
    void rejeitaPlacaInvalidaNaAberturaDaOs() throws Exception {
        String token = login();
        String cpf = "529.982.247-25";
        criarClienteSeNecessario(token, cpf);
        Map<String, Object> body = Map.of(
                "documentoCliente", cpf,
                "veiculo", Map.of("placa", "PLACA-INVALIDA", "marca", "Fiat", "modelo", "Uno", "ano", 2018));
        postJson("/api/admin/ordens-servico", token, body)
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void validaCamposObrigatoriosDoPayload() throws Exception {
        String token = login();
        postJson("/api/admin/clientes", token, Map.of("nome", ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros").exists());
    }

    @Test
    void duplicidadeDeCpfRetornaConflito() throws Exception {
        String token = login();
        String cpf = "390.533.447-05";
        criarCliente(token, "Primeiro", cpf);
        postJson("/api/admin/clientes", token, Map.of("nome", "Segundo", "documento", cpf))
                .andExpect(status().isConflict());
    }

    @Test
    void apisAdministrativasExigemJwt() throws Exception {
        getJson("/api/admin/clientes", null).andExpect(status().isUnauthorized());
        getJson("/api/admin/ordens-servico", "token.invalido.aqui").andExpect(status().isUnauthorized());
        postJson("/api/admin/pecas", null, Map.of("codigo", "X")).andExpect(status().isUnauthorized());
    }

    @Test
    void loginComCredenciaisInvalidasRetorna401() throws Exception {
        postJson("/api/auth/login", null, Map.of("username", "admin", "password", "senha-errada"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void consultaDeCodigoInexistenteRetorna404() throws Exception {
        getJson("/api/public/acompanhamento/codigo-inexistente", null).andExpect(status().isNotFound());
    }

    @Test
    void swaggerEstaDisponivelSemAutenticacao() throws Exception {
        getJson("/v3/api-docs", null).andExpect(status().isOk());
    }
}
