# Análise de vulnerabilidades

> **Preencher com o resultado real** antes da entrega (rode os comandos abaixo e anexe/cole o resultado).

## Dependências (OWASP Dependency-Check)
```bash
export NVD_API_KEY=<sua chave gratuita do NVD>
mvn -Psecurity verify
# relatório em target/dependency-check-report.html  → copie para docs/seguranca/
```

## Dependências, configuração e Dockerfile (Trivy)
```bash
trivy fs --severity HIGH,CRITICAL . > docs/seguranca/trivy-fs.txt
trivy image oficina-service:latest > docs/seguranca/trivy-image.txt   # após docker build
```
O workflow `.github/workflows/ci.yml` também executa o Trivy e publica o relatório como artefato.

## Controles de segurança implementados
| Controle | Implementação |
|---|---|
| Autenticação JWT (HS256) nas APIs administrativas | `JwtService`, `JwtAuthenticationFilter`, `SecurityConfig` |
| Senha do admin em hash BCrypt em memória; segredos por variável de ambiente | `AuthService`, `application.properties` |
| Sessão stateless, CSRF desabilitado (API com token) | `SecurityConfig` |
| Rotas não mapeadas negadas por padrão (`denyAll`) | `SecurityConfig` |
| Validação de entrada (Bean Validation) e dados sensíveis (CPF/CNPJ, placa) | DTOs, `Documento`, `Placa` |
| SQL parametrizado (JPA/Spring Data), sem concatenação de SQL | repositórios |
| Acompanhamento público por UUID aleatório; sem dados pessoais na resposta | `AcompanhamentoResponse` |
| Container executa com usuário não-root | `Dockerfile` |

## Resultado da análise
| Ferramenta | Data | Resultado | Ação |
|---|---|---|---|
| OWASP Dependency-Check | __ | __ | __ |
| Trivy | __ | __ | __ |
