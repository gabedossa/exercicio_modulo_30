# Cadastro Cliente / Produto / Pedido

Projeto de estudo (curso EBAC) para praticar acesso a banco de dados com **JDBC puro** em Java, aplicando o padrão **DAO** (Data Access Object) sobre um schema PostgreSQL com relacionamento N:N entre pedidos e produtos.

## Sumário

- [Visão geral](#visão-geral)
- [Tecnologias](#tecnologias)
- [Modelo de dados](#modelo-de-dados)
- [Estrutura do projeto](#estrutura-do-projeto)
- [Arquitetura (camadas)](#arquitetura-camadas)
- [Configuração do banco de dados](#configuração-do-banco-de-dados)
- [Como rodar](#como-rodar)
- [Testes](#testes)
- [Observações e limitações conhecidas](#observações-e-limitações-conhecidas)

## Visão geral

O projeto implementa o CRUD de quatro entidades:

- **Cliente** — cadastro simples de clientes.
- **Produto** — cadastro de produtos com preço e estoque.
- **Pedido** — um pedido de venda, com data e valor total.
- **ItemPedido** — tabela associativa que resolve o relacionamento N:N entre `Pedido` e `Produto` (um pedido tem vários produtos, um produto aparece em vários pedidos), guardando a quantidade e o preço praticado no momento da venda.

Cada entidade possui uma classe de modelo (POJO) em `model`, uma interface DAO e uma implementação JDBC em `dao`.

## Tecnologias

- **Java 25** (JRE_CONTAINER do `.classpath`)
- **JDBC** (sem ORM/framework) via driver `postgresql-42.7.13`
- **PostgreSQL**
- **JUnit 5** (Jupiter) para os testes
- **Mockito 5** (mock maker inline) para os testes unitários dos DAOs
- Projeto **Eclipse** (`.project` / `.classpath`), sem Maven/Gradle — as dependências são referenciadas diretamente como `.jar` no classpath

## Modelo de dados

```sql
-- Cliente
CREATE TABLE cliente (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(50) NOT NULL UNIQUE,
    nome VARCHAR(100) NOT NULL
);

-- Produto
CREATE TABLE produto (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(50) NOT NULL UNIQUE,
    nome VARCHAR(100) NOT NULL,
    preco NUMERIC(10,2) NOT NULL,
    quantidade INT NOT NULL DEFAULT 0
);

-- Pedido (lado "1" do relacionamento com os itens)
CREATE TABLE pedido (
    id BIGSERIAL PRIMARY KEY,
    data_pedido TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total NUMERIC(10,2) NOT NULL DEFAULT 0
);

-- Item_pedido: tabela intermediária, resolve o N:N entre pedido e produto
CREATE TABLE item_pedido (
    id BIGSERIAL PRIMARY KEY,
    pedido_id BIGINT NOT NULL REFERENCES pedido(id) ON DELETE CASCADE,
    produto_id BIGINT NOT NULL REFERENCES produto(id) ON DELETE RESTRICT,
    quantidade INT NOT NULL CHECK (quantidade > 0),
    preco_unitario NUMERIC(10,2) NOT NULL,
    UNIQUE (pedido_id, produto_id)
);
```

Pontos importantes do schema:

- `pedido.data_pedido` é preenchida automaticamente pelo banco (`DEFAULT CURRENT_TIMESTAMP`); o DAO nunca envia esse valor no `INSERT`.
- Excluir um `Pedido` remove seus itens em cascata (`ON DELETE CASCADE` em `item_pedido.pedido_id`).
- Excluir um `Produto` que já foi vendido é bloqueado pelo banco (`ON DELETE RESTRICT` em `item_pedido.produto_id`).
- `UNIQUE (pedido_id, produto_id)` garante que o mesmo produto não apareça duas vezes no mesmo pedido — para trocar quantidade/preço de um item já existente, atualiza-se a linha; para trocar o produto, é preciso excluir o item e inserir um novo.
- `item_pedido.preco_unitario` guarda o preço no momento da venda, já que `produto.preco` pode mudar depois.

## Estrutura do projeto

```
src/main/java/
├── model/
│   ├── Cliente.java
│   ├── Produto.java
│   ├── Pedido.java
│   └── ItemPedido.java
├── dao/
│   ├── ClienteDAO.java        (interface)
│   ├── ClienteDaoImpl.java
│   ├── ProdutoDAO.java        (interface)
│   ├── ProdutoDaoImpl.java
│   ├── PedidoDAO.java         (interface)
│   ├── PedidoDaoImpl.java
│   ├── ItemPedidoDAO.java     (interface)
│   └── ItemPedidoDaoImpl.java
└── factory/
    └── ConnectionFactory.java

src/test/java/dao/
├── ClienteDaoImplTest.java          (integração)
├── ClienteDaoImplUnitTest.java      (unitário, com mocks)
├── ProdutoDaoImplTest.java          (integração)
├── ProdutoDaoImplUnitTest.java      (unitário, com mocks)
├── PedidoDaoImplTest.java           (integração)
├── PedidoDaoImplUnitTest.java       (unitário, com mocks)
├── ItemPedidoDaoImplTest.java       (integração)
└── ItemPedidoDaoImplUnitTest.java   (unitário, com mocks)

src/test/resources/
└── mockito-extensions/
    └── org.mockito.plugins.MockMaker   (ativa o inline mock maker do Mockito)
```

## Arquitetura (camadas)

- **model** — classes simples (getters/setters) espelhando as colunas das tabelas.
- **dao** — uma interface por entidade + uma implementação JDBC (`*DaoImpl`) que abre conexão, monta o `PreparedStatement`, executa e fecha os recursos.
- **factory.ConnectionFactory** — fábrica estática de conexões (`DriverManager.getConnection`), sem pool: cada operação de DAO abre e fecha sua própria conexão.

Métodos expostos por cada DAO:

| DAO | Métodos |
|---|---|
| `ClienteDAO` | `cadastrar`, `atualizar`, `buscaCliente(codigo)`, `buscatodos`, `excluir` |
| `ProdutoDAO` | `cadastrar`, `atualizar`, `buscaProduto(codigo)`, `buscatodos`, `excluir` |
| `PedidoDAO` | `cadastrar`, `atualizar`, `buscaPedido(id)`, `buscatodos`, `excluir` |
| `ItemPedidoDAO` | `cadastrar`, `atualizar`, `buscaPorPedido(pedidoId)`, `buscatodos`, `excluir` |

`Cliente` e `Produto` têm código de negócio único (`codigo`), por isso a busca pontual é por código. `Pedido` e `ItemPedido` não têm código natural, por isso a busca é por `id` (ou, no caso de `ItemPedido`, pelo `pedidoId`, que é o caso de uso mais comum: listar os itens de um pedido).

## Configuração do banco de dados

A conexão é resolvida em [`ConnectionFactory`](src/main/java/factory/ConnectionFactory.java) a partir de variáveis de ambiente, com valores padrão para desenvolvimento local:

| Variável | Padrão |
|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:15432/postgres` |
| `DB_USER` | `postgres` |
| `DB_PASSWORD` | `Adm1234` |

Para usar outro banco, defina as variáveis de ambiente antes de rodar a aplicação/testes, por exemplo:

```powershell
$env:DB_URL = "jdbc:postgresql://localhost:5432/meubanco"
$env:DB_USER = "meu_usuario"
$env:DB_PASSWORD = "minha_senha"
```

Antes de usar, crie as tabelas no banco executando o SQL da seção [Modelo de dados](#modelo-de-dados).

## Como rodar

O projeto foi criado no Eclipse (sem Maven/Gradle). O classpath (`.classpath`) referencia os `.jar` do driver PostgreSQL e do JUnit 5 diretamente pelo caminho local da máquina onde foi criado — ajuste esses caminhos (ou importe o projeto e adicione as bibliotecas pela IDE) caso rode em outra máquina:

- Driver JDBC: `postgresql-42.7.13.jar`
- JUnit 5: `junit-jupiter-api`, `junit-jupiter-engine`, `junit-platform-commons`, `junit-platform-engine`, `junit-platform-launcher`, `org.apiguardian.api`, `org.opentest4j`
- Mockito 5 (só para os testes unitários): `mockito-core`, `mockito-junit-jupiter`, `byte-buddy`, `byte-buddy-agent`, `objenesis`

Para importar:

1. Abra o Eclipse → `File > Open Projects from File System...` → selecione a pasta do projeto.
2. Garanta que um JDK 25 está configurado (é o que o `.classpath` declara; ajuste o `JRE_CONTAINER` se sua máquina só tiver outra versão instalada).
3. Ajuste as bibliotecas externas do `.classpath` se os `.jar` não estiverem nos mesmos caminhos.

## Testes

Cada DAO tem dois tipos de teste, lado a lado em `src/test/java/dao`:

- **Testes de integração** (`ClienteDaoImplTest`, `ProdutoDaoImplTest`, `PedidoDaoImplTest`, `ItemPedidoDaoImplTest`) — se conectam a um banco PostgreSQL real (via `ConnectionFactory`), cadastram registros de teste (com códigos únicos via `UUID` quando aplicável) e os removem no `@AfterEach`. **Exigem** um banco PostgreSQL acessível com as tabelas já criadas (seção [Modelo de dados](#modelo-de-dados)). `ItemPedidoDaoImplTest` também cria/remove os `Pedido`/`Produto` necessários para satisfazer as chaves estrangeiras, e tem um teste que comprova que o banco bloqueia a exclusão de um produto referenciado por um item (`ON DELETE RESTRICT`).
- **Testes unitários** (`ClienteDaoImplUnitTest`, `ProdutoDaoImplUnitTest`, `PedidoDaoImplUnitTest`, `ItemPedidoDaoImplUnitTest`) — **não tocam em banco nenhum**. Usam Mockito para mockar `Connection`/`PreparedStatement`/`ResultSet` e o método estático `ConnectionFactory.getConnection()` (via `Mockito.mockStatic`), verificando isoladamente: o SQL exato gerado, a ordem/valor dos parâmetros do `PreparedStatement`, o mapeamento do `ResultSet` para o model, e que `PreparedStatement`/`Connection` são fechados mesmo quando `executeUpdate()`/`executeQuery()` lança exceção.

O `mockStatic` exige o *inline mock maker* do Mockito, habilitado pelo arquivo [`src/test/resources/mockito-extensions/org.mockito.plugins.MockMaker`](src/test/resources/mockito-extensions/org.mockito.plugins.MockMaker) (conteúdo: `mock-maker-inline`). Sem essa pasta de resources no classpath de teste, os testes unitários falham com `MockitoException`.

Rode pelos utilitários de teste do Eclipse (clique direito na classe/pacote → `Run As > JUnit Test`) ou via linha de comando com `javac`/`java` apontando para os `.jar` listados acima e para o JUnit Platform Launcher.

## Observações e limitações conhecidas

- `ConnectionFactory` não usa pool de conexões — cada chamada de DAO abre e fecha sua própria `Connection`, o que é aceitável para fins didáticos mas não é recomendado em produção.
- As credenciais padrão (`postgres` / `Adm1234`) servem apenas para desenvolvimento local; sempre sobrescreva via variáveis de ambiente em qualquer outro ambiente.
- Os testes unitários usam mocking de método estático (`mockStatic`), o que acopla o teste à assinatura de `ConnectionFactory.getConnection()`; é um trade-off aceito aqui para testar os DAOs sem alterar o design de produção (não há injeção de dependência no projeto).
