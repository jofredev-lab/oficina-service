<<<<<<< HEAD
# Oficina Mecânica — Sistema Integrado de Atendimento e Execução de Serviços (MVP)

Tech Challenge · Fase 1 · SOAT — Software Architecture (Pós Tech)

## 1. Objetivo do projeto
Primeira versão (MVP) do back-end de uma oficina mecânica de médio porte, substituindo anotações manuais e planilhas por um sistema que organiza **ordens de serviço (OS)**, **clientes**, **veículos**, **serviços** e **peças/insumos (com estoque)**, e que permite ao cliente acompanhar o andamento e aprovar orçamentos via API.

## 2. Descrição resumida da solução
- A OS é aberta identificando o cliente por **CPF/CNPJ**; o veículo é cadastrado (ou reaproveitado pela placa) e os serviços/peças são incluídos.
- O **orçamento é calculado automaticamente** (serviços + peças × quantidade) e enviado ao cliente (`Aguardando aprovação`).
- O cliente consulta e **aprova/rejeita** pela API pública, usando o *código de acompanhamento* (UUID) da OS.
- A **aprovação baixa o estoque** das peças na mesma transação: se faltar estoque, nada é alterado (rollback).
- O status segue o fluxo **Recebida → Em diagnóstico → Aguardando aprovação → Em execução → Finalizada → Entregue**, com regras de transição no domínio.
- APIs administrativas protegidas por **JWT**; documentação em **Swagger/OpenAPI**.

### Regras de negócio relevantes
| Regra | Onde está |
|---|---|
| CPF/CNPJ e placa válidos (objetos de valor) | `Documento`, `Placa` |
| Transições de status permitidas | `StatusOS` + `OrdemServico` |
| Itens só podem ser alterados em *Recebida* e *Em diagnóstico* | `OrdemServico` |
| Não gera orçamento sem ao menos um serviço/peça | `OrdemServico.enviarOrcamento()` |
| Rejeição do orçamento → OS *Finalizada* sem execução (veículo pode ser entregue) | `OrdemServico.rejeitarOrcamento()` |
| Baixa de estoque na aprovação, com lock otimista (`@Version`) | `OrdemServicoService`, `Peca` |
| Preço dos itens "congelado" no momento da inclusão na OS | `ItemServico`, `ItemPeca` |
| Tempo médio de execução = aprovação → finalização | `OrdemServico.duracaoExecucao()` |

## 3. Arquitetura utilizada
**Monolito em camadas**, com organização por contexto de domínio (DDD tático):

```
interfaces/rest   →  application (casos de uso, DTOs)  →  domain (agregados, VOs, regras)
                              ↓
                     infrastructure (JPA/repositórios, segurança JWT, configs)
```

- **domain**: entidades/agregados (`OrdemServico`, `Cliente`, `Veiculo`, `Servico`, `Peca`), objetos de valor (`Documento`, `Placa`), enum de status e exceções de negócio.
- **application**: serviços transacionais (um método = um caso de uso/transação) e DTOs.
- **infrastructure**: repositórios Spring Data, segurança (JWT) e configuração.
- **interfaces/rest**: controllers REST e tratamento global de erros (`ProblemDetail`).

> Decisão pragmática de MVP: as entidades de domínio carregam anotações JPA (sem um segundo modelo de persistência) e a camada de aplicação usa os repositórios diretamente. Em uma próxima fase é possível isolar o domínio com portas/adaptadores.

Documentação DDD (Event Storming, linguagem ubíqua, diagramas): [`docs/ddd/`](docs/ddd/).

## 4. Tecnologias utilizadas
Java 21 · Spring Boot 3.3 (Web, Data JPA, Validation, Security) · PostgreSQL 16 · JWT (jjwt) · springdoc-openapi (Swagger UI) · JUnit 5 + MockMvc + H2 (testes) · JaCoCo (cobertura) · Docker / Docker Compose · Maven.

## 5. Decisão e justificativa do banco de dados
**PostgreSQL** (relacional):
- O domínio é fortemente relacional (cliente → veículo → OS → itens → peças) e exige **integridade referencial** e **unicidade** (CPF/CNPJ, placa, código da peça).
- A aprovação da OS altera OS e estoque no mesmo fluxo: precisamos de **transações ACID** (tudo ou nada) e de lock otimista para evitar baixas concorrentes inconsistentes.
- Consultas de gestão (listagem por status, tempo médio) são naturalmente expressas em SQL.
- Um banco de documentos (ex.: MongoDB) facilitaria dados sem esquema, mas dificultaria as garantias transacionais entre agregados que o estoque exige. Banco maduro, open source e com imagem oficial leve para Docker.

Para os testes automatizados é usado **H2 em memória** (modo PostgreSQL), sem dependência externa.

## 6. Pré-requisitos
- **Docker** e **Docker Compose** (execução completa), ou
- **JDK 21** + **Maven 3.9+** e um PostgreSQL local (execução sem Docker).

## 7. Instruções para execução

### Com Docker (recomendado)
```bash
docker compose up --build
```
- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html

### Sem Docker
```bash
# suba um PostgreSQL com banco/usuário/senha "oficina" (ou ajuste DB_URL, DB_USER, DB_PASSWORD)
mvn spring-boot:run
```

### Variáveis de ambiente (todas opcionais — há valores de demonstração)
| Variável | Padrão (demo) |
|---|---|
| `DB_URL` / `DB_USER` / `DB_PASSWORD` | `jdbc:postgresql://localhost:5432/oficina` / `oficina` / `oficina` |
| `JWT_SECRET` | segredo de demonstração (mín. 32 caracteres) — **troque em produção** |
| `ADMIN_USERNAME` / `ADMIN_PASSWORD` | `admin` / `admin123` |

## 8. Autenticação e credenciais de demonstração
1. `POST /api/auth/login` com `{"username":"admin","password":"admin123"}` → retorna `token`.
2. Envie `Authorization: Bearer <token>` nas rotas `/api/admin/**`.
3. No Swagger UI: botão **Authorize** → cole o token.

São públicas apenas: `/api/auth/**`, `/api/public/**` (acompanhamento do cliente) e a documentação.

## 9. Instruções para execução dos testes
```bash
mvn test       # testes unitários e de integração
mvn verify     # + relatório JaCoCo e verificação de cobertura mínima de 80% (linhas) no domínio
```
Relatório de cobertura: `target/site/jacoco/index.html`.

- **Unitários** (`src/test/java/.../domain`): objetos de valor, regras de estoque, orçamento e transições de status.
- **Integração** (`src/test/java/.../integration`): API REST + JPA + segurança, incluindo o fluxo completo da OS, rollback por estoque insuficiente, rejeição de orçamento, validações e JWT.

## 10. Acesso ao Swagger / documentação da API
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI (JSON): `http://localhost:8080/v3/api-docs`

## 11. Exemplo rápido do fluxo (curl)
```bash
TOKEN=$(curl -s -X POST localhost:8080/api/auth/login -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"admin123"}' | sed 's/.*"token":"\([^"]*\)".*/\1/')
H="Authorization: Bearer $TOKEN"; J='Content-Type: application/json'

curl -X POST localhost:8080/api/admin/clientes  -H "$H" -H "$J" -d '{"nome":"João","documento":"529.982.247-25","email":"joao@email.com"}'
curl -X POST localhost:8080/api/admin/servicos  -H "$H" -H "$J" -d '{"nome":"Troca de óleo","preco":80.00}'
curl -X POST localhost:8080/api/admin/pecas     -H "$H" -H "$J" -d '{"codigo":"FO-1","nome":"Filtro de óleo","preco":25.50,"quantidadeEstoque":10,"estoqueMinimo":2}'

curl -X POST localhost:8080/api/admin/ordens-servico -H "$H" -H "$J" -d '{
  "documentoCliente":"529.982.247-25",
  "veiculo":{"placa":"ABC1D23","marca":"Fiat","modelo":"Uno","ano":2018},
  "servicosIds":[1], "pecas":[{"pecaId":1,"quantidade":2}]}'

curl -X POST localhost:8080/api/admin/ordens-servico/1/diagnostico      -H "$H"
curl -X POST localhost:8080/api/admin/ordens-servico/1/orcamento/enviar -H "$H"
# cliente (sem JWT), usando o codigoAcompanhamento retornado na abertura da OS:
curl localhost:8080/api/public/acompanhamento/<codigo>
curl -X POST localhost:8080/api/public/acompanhamento/<codigo>/orcamento/aprovar
curl -X POST localhost:8080/api/admin/ordens-servico/1/finalizar -H "$H"
curl -X POST localhost:8080/api/admin/ordens-servico/1/entregar  -H "$H"
curl localhost:8080/api/admin/metricas/tempo-medio-execucao -H "$H"
```

## 12. Principais endpoints
| Área | Rotas |
|---|---|
| Auth | `POST /api/auth/login` |
| CRUD | `/api/admin/clientes`, `/veiculos`, `/servicos`, `/pecas` (+ `/pecas/{id}/estoque/entrada\|saida`, `/pecas/estoque-baixo`) |
| OS (admin) | `POST/GET /api/admin/ordens-servico`, `GET /{id}`, `POST /{id}/itens\|diagnostico\|orcamento/enviar\|orcamento/aprovar\|orcamento/rejeitar\|finalizar\|entregar` |
| Cliente (público) | `GET /api/public/acompanhamento/{codigo}`, `POST .../orcamento/aprovar\|rejeitar` |
| Métricas | `GET /api/admin/metricas/tempo-medio-execucao` |

## 13. Estrutura dos principais diretórios
```
├── Dockerfile / docker-compose.yml
├── pom.xml
├── docs/
│   ├── ddd/                 # Event Storming, linguagem ubíqua, diagramas, mapeamento para o código
│   ├── seguranca/           # análise de vulnerabilidades
│   └── ENTREGA.md           # checklist do PDF de entrega
├── src/main/java/br/com/oficina/
│   ├── domain/              # shared, cliente, veiculo, servico, peca, ordemservico
│   ├── application/         # dto, service (casos de uso)
│   ├── infrastructure/      # persistence, security, config
│   └── interfaces/rest/     # controllers e tratamento de erros
└── src/test/java/br/com/oficina/   # domain (unitários) e integration
```

## 14. Links da entrega
- Documentação DDD (Miro): **<PREENCHER: link público do Miro>** — export em `docs/ddd/` (veja `docs/ddd/README.md`)
- Vídeo (YouTube, público, ≤ 15 min): **<PREENCHER>**
- Branch e hash do commit da entrega: **<PREENCHER>**
- Relatório de vulnerabilidades: [`docs/seguranca/`](docs/seguranca/)

## 15. Limitações conhecidas do MVP
- Schema gerenciado por `hibernate.ddl-auto=update` (migrar para Flyway).
- Um único usuário administrador, definido por variáveis de ambiente.
- "Envio" do orçamento = disponibilização via API (sem e-mail/SMS/push).
=======
# oficina-service
teste de sistema de oficina medio porte
>>>>>>> b7554ad8d71015363380a9c07c65fa96a934737b
