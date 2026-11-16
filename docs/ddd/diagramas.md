# Diagramas

## 1. Mapa de contextos
```mermaid
flowchart LR
  subgraph Core[Atendimento e Execução - core]
    OS[OrdemServico]
  end
  subgraph Cad[Cadastros]
    CL[Cliente]
    VE[Veiculo]
  end
  subgraph Est[Catálogo e Estoque]
    SV[Servico]
    PC[Peca]
  end
  OS -- referencia --> CL
  OS -- referencia --> VE
  OS -- referencia + snapshot de preço --> SV
  OS -- referencia + snapshot de preço; baixa de estoque --> PC
  VE -- pertence a --> CL
```

## 2. Modelo de domínio (agregados, entidades e objetos de valor)
```mermaid
classDiagram
  class OrdemServico {
    <<Aggregate Root>>
    +Long id
    +String codigoAcompanhamento
    +StatusOS status
    +OrcamentoStatus orcamentoStatus
    +abrir(cliente, veiculo)
    +adicionarServico(servico)
    +adicionarPeca(peca, qtd)
    +iniciarDiagnostico()
    +enviarOrcamento()
    +aprovarOrcamento()
    +rejeitarOrcamento()
    +finalizar()
    +entregar()
    +valorTotal()
    +duracaoExecucao()
  }
  class ItemServico {
    <<Entity>>
    nomeServico
    preco
  }
  class ItemPeca {
    <<Entity>>
    nomePeca
    precoUnitario
    quantidade
    subtotal()
  }
  class Cliente {
    <<Aggregate Root>>
    nome
    email
    telefone
  }
  class Documento {
    <<Value Object>>
    numero
    tipo
  }
  class Veiculo {
    <<Aggregate Root>>
    marca
    modelo
    ano
  }
  class Placa {
    <<Value Object>>
    numero
  }
  class Servico {
    <<Aggregate Root>>
    nome
    descricao
    preco
  }
  class Peca {
    <<Aggregate Root>>
    codigo
    nome
    preco
    quantidadeEstoque
    estoqueMinimo
    baixarEstoque()
    reporEstoque()
  }
  OrdemServico "1" *-- "0..*" ItemServico
  OrdemServico "1" *-- "0..*" ItemPeca
  OrdemServico --> Cliente
  OrdemServico --> Veiculo
  ItemServico --> Servico
  ItemPeca --> Peca
  Cliente *-- Documento
  Veiculo *-- Placa
  Veiculo --> Cliente
```

## 3. Ciclo de vida da Ordem de Serviço
```mermaid
stateDiagram-v2
  [*] --> RECEBIDA: abrir OS
  RECEBIDA --> EM_DIAGNOSTICO: iniciarDiagnostico
  EM_DIAGNOSTICO --> AGUARDANDO_APROVACAO: enviarOrcamento
  AGUARDANDO_APROVACAO --> EM_EXECUCAO: aprovarOrcamento (baixa estoque)
  AGUARDANDO_APROVACAO --> FINALIZADA: rejeitarOrcamento
  EM_EXECUCAO --> FINALIZADA: finalizar
  FINALIZADA --> ENTREGUE: entregar
  ENTREGUE --> [*]
```

## 4. Sequência: aprovação do orçamento pelo cliente
```mermaid
sequenceDiagram
  actor C as Cliente
  participant API as AcompanhamentoController
  participant S as OrdemServicoService (@Transactional)
  participant OS as OrdemServico
  participant P as Peca
  C->>API: POST /api/public/acompanhamento/{codigo}/orcamento/aprovar
  API->>S: aprovarPorCodigo(codigo)
  S->>OS: aprovarOrcamento()
  OS-->>S: status = EM_EXECUCAO
  loop cada item de peça
    S->>P: baixarEstoque(qtd)
    P-->>S: ok / RegraNegocioException
  end
  S-->>API: commit (ou rollback se faltar estoque)
  API-->>C: 200 (ou 422)
```
