# Mapeamento: documentação DDD → código

| Conceito DDD | Elemento | Classe / local |
|---|---|---|
| Agregado (raiz) | Ordem de Serviço | `domain/ordemservico/OrdemServico` |
| Agregado (raiz) | Cliente · Veículo · Serviço · Peça | `domain/cliente/Cliente` · `domain/veiculo/Veiculo` · `domain/servico/Servico` · `domain/peca/Peca` |
| Entidade interna | Itens da OS | `ItemServico`, `ItemPeca` |
| Objeto de valor | CPF/CNPJ · Placa | `Documento` · `Placa` (imutáveis, validação na criação) |
| Enum de domínio | Status da OS · Status do orçamento | `StatusOS` (regras de transição) · `OrcamentoStatus` |
| Comandos | Abrir OS, enviar orçamento, aprovar, finalizar, entregar… | métodos de `OrdemServicoService` e rotas de `OrdemServicoController` / `AcompanhamentoController` |
| Eventos de domínio | OS aberta, orçamento enviado/aprovado/rejeitado, execução finalizada, veículo entregue | transições de estado em `OrdemServico` (persistidas em `status`, `orcamentoStatus` e datas). *No MVP os eventos são implícitos — não há barramento de eventos.* |
| Políticas / regras | Transições, itens bloqueados, estoque suficiente, orçamento sem itens | `StatusOS`, `OrdemServico`, `Peca` |
| Serviço de aplicação | Casos de uso transacionais | `application/service/*` |
| Repositórios | Persistência dos agregados | `infrastructure/persistence/*` |
| Exceções de domínio | Regra violada · não encontrado · conflito | `RegraNegocioException` (422) · `RecursoNaoEncontradoException` (404) · `ConflitoException` (409) |
| Linguagem ubíqua | Nomes em português do domínio | classes, métodos, status e rotas |
