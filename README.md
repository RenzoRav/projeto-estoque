# Gestão de Estoque

API REST para cadastro de categorias e produtos, consulta de estoque e cálculo do valor armazenado por produto. O sistema utiliza Java e Spring Boot, com separação entre apresentação, regras de negócio e persistência.

## Funcionalidades

- Cadastro, listagem, consulta individual, atualização e exclusão de categorias e produtos.
- Relacionamento de uma categoria para vários produtos.
- Consulta por trecho do nome, sem diferenciar maiúsculas e minúsculas.
- Filtro de produtos pela categoria, isolado ou combinado com o nome.
- Cálculo automático do valor de estoque de cada produto.
- Validação de entrada e tratamento centralizado de erros.
- Documentação interativa com Swagger/OpenAPI.
- Persistência local e testes de integração automatizados.

## Tecnologias

| Tecnologia | Versão ou finalidade |
| --- | --- |
| Java | 21 |
| Spring Boot | 3.5.7 |
| Spring Web | Endpoints REST e respostas HTTP |
| Spring Data JPA | Repositórios e persistência |
| Bean Validation | Validação dos contratos de entrada |
| H2 | Banco de dados relacional |
| Springdoc OpenAPI | 2.8.14 |
| Maven Wrapper | 3.9.9 |
| JUnit e MockMvc | Testes de integração |

## Pré-requisitos

- JDK 21 instalado e disponível no terminal.
- Variável `JAVA_HOME` apontando para a pasta do JDK, quando exigida pelo ambiente.
- Git para clonar o repositório.
- Acesso à internet na primeira execução para baixar o Maven e as dependências.

Não é necessário instalar Maven ou um servidor de banco de dados separadamente.

## Instalação e execução

Extraia o arquivo do projeto e abra um terminal na pasta que contém `gestao-estoque`.

### Windows

```powershell
cd gestao-estoque
java -version
.\mvnw.cmd spring-boot:run
```

Também é possível executar `iniciar.bat` na pasta do projeto. Para encerrar a aplicação, pressione `Ctrl+C` no terminal.

### Linux e macOS

```sh
cd gestao-estoque
chmod +x mvnw
./mvnw spring-boot:run
```

### Endereços locais

| Recurso | Endereço |
| --- | --- |
| API | http://localhost:8080/api |
| Swagger | http://localhost:8080/documentacao |
| Documento OpenAPI | http://localhost:8080/v3/api-docs |

O caminho `/api` é o prefixo das rotas, não uma página de apresentação. Para testar as operações no navegador, utilize o Swagger. Os endereços ficam disponíveis enquanto a aplicação estiver em execução.

### Alterar a porta

A porta padrão é `8080`. No PowerShell, é possível alterá-la pela variável de ambiente `PORT`:

```powershell
$env:PORT = '8081'
.\mvnw.cmd spring-boot:run
```

Nesse caso, os endereços locais passam a utilizar a porta `8081`.

## Banco de dados

A aplicação utiliza H2 em arquivo. O Hibernate cria ou atualiza as tabelas na inicialização.

| Configuração | Valor padrão |
| --- | --- |
| Conexão | `jdbc:h2:file:./dados/estoque` |
| Usuário | `sa` |
| Senha | Vazia |
| Estratégia de esquema | `spring.jpa.hibernate.ddl-auto=update` |
| Arquivo de configuração | `src/main/resources/application.properties` |

A pasta `dados` é criada no diretório de execução. Os registros permanecem disponíveis após reiniciar a aplicação, desde que ela seja iniciada no mesmo diretório. Os arquivos do banco não são versionados.

Para iniciar com um banco vazio, encerre a aplicação e mova a pasta `dados` para um local de backup antes de executar novamente.

Os testes utilizam H2 em memória, com criação e remoção do esquema, sem acessar o banco local da aplicação. A configuração de testes está em `src/test/resources/application.properties`.

## Organização do projeto

```text
src
├── main
│   ├── java/br/com/estoque
│   │   ├── controller
│   │   ├── dto
│   │   ├── entity
│   │   ├── exception
│   │   ├── repository
│   │   ├── service
│   │   └── GestaoEstoqueApplication.java
│   └── resources/application.properties
└── test
    ├── java/br/com/estoque/GestaoEstoqueIntegrationTest.java
    └── resources/application.properties
```

| Camada | Responsabilidade |
| --- | --- |
| `controller` | Receber requisições, validar entradas e controlar respostas com `ResponseEntity` |
| `dto` | Definir contratos de entrada, saída e mensagens de erro |
| `entity` | Mapear entidades e relacionamentos do banco |
| `exception` | Centralizar o tratamento de erros com `RestControllerAdvice` |
| `repository` | Acessar os dados com Spring Data JPA |
| `service` | Aplicar regras de negócio, controlar transações e converter entidades em respostas |

As entidades não são retornadas diretamente pela API. As conversões são manuais, sem biblioteca adicional. As dependências são recebidas por construtor.

## Modelo de dados

Uma categoria pode possuir vários produtos, e cada produto pertence obrigatoriamente a uma categoria. O relacionamento utiliza `OneToMany` em `Categoria` e `ManyToOne` em `Produto`, com chave estrangeira `categoria_id`. Não há exclusão em cascata.

### Categoria

| Campo | Tipo | Regra |
| --- | --- | --- |
| `id` | Inteiro longo | Gerado automaticamente |
| `nome` | Texto | Obrigatório, único e com até 80 caracteres |

### Produto

| Campo | Tipo | Regra |
| --- | --- | --- |
| `id` | Inteiro longo | Gerado automaticamente |
| `nome` | Texto | Obrigatório, único e com até 100 caracteres |
| `preco` | Decimal | De 0.01 a 9999999999.99, com até duas casas decimais |
| `quantidade` | Inteiro | Entre 0 e 10000 |
| `categoriaId` | Inteiro longo | Id positivo de uma categoria existente, recebido na entrada |
| `valorEstoque` | Decimal | Calculado na resposta, não recebido na entrada |
| `categoria` | Objeto | Id e nome da categoria, retornados na resposta |

## Regras de negócio

1. Os nomes das categorias são únicos. O mesmo vale para os nomes dos produtos, independentemente da categoria.
2. Os nomes são normalizados em minúsculas e sem espaços nas extremidades. Assim, diferenças de capitalização ou espaços externos não permitem duplicações.
3. Categorias com produtos vinculados não podem ser excluídas.
4. Produtos com quantidade positiva não podem ser excluídos. Antes da exclusão, a quantidade deve ser atualizada para zero.
5. O valor de estoque é calculado por `preco × quantidade` em cada resposta de produto, usando `BigDecimal`.
6. O preço deve ser positivo, e a quantidade deve permanecer dentro do intervalo permitido.

As regras de duplicidade e exclusão ficam nos serviços. As restrições de formato, tamanho e intervalo também são verificadas por Bean Validation na entrada. As operações de escrita são transacionais.

## Endpoints

### Categorias

| Método | Caminho | Descrição | Sucesso |
| --- | --- | --- | --- |
| POST | `/api/categorias` | Cadastrar categoria | 201 |
| GET | `/api/categorias` | Listar e filtrar por nome | 200 |
| GET | `/api/categorias/{id}` | Consultar categoria | 200 |
| PUT | `/api/categorias/{id}` | Atualizar categoria | 200 |
| DELETE | `/api/categorias/{id}` | Excluir categoria sem produtos vinculados | 204 |

Corpo de cadastro e atualização:

```json
{
  "nome": "Perifericos"
}
```

Exemplo de resposta:

```json
{
  "id": 1,
  "nome": "perifericos"
}
```

### Produtos

| Método | Caminho | Descrição | Sucesso |
| --- | --- | --- | --- |
| POST | `/api/produtos` | Cadastrar produto | 201 |
| GET | `/api/produtos` | Listar e filtrar produtos | 200 |
| GET | `/api/produtos/{id}` | Consultar produto | 200 |
| PUT | `/api/produtos/{id}` | Atualizar produto | 200 |
| DELETE | `/api/produtos/{id}` | Excluir produto com quantidade zero | 204 |

Corpo de cadastro e atualização:

```json
{
  "nome": "Teclado",
  "preco": 125.50,
  "quantidade": 2,
  "categoriaId": 1
}
```

Exemplo de resposta:

```json
{
  "id": 1,
  "nome": "teclado",
  "preco": 125.50,
  "quantidade": 2,
  "valorEstoque": 251.00,
  "categoria": {
    "id": 1,
    "nome": "perifericos"
  }
}
```

O id da categoria deve corresponder a um registro existente. O `PUT` recebe todos os campos de entrada; ele não representa uma atualização parcial. Os cadastros retornam o cabeçalho `Location` com o endereço relativo do novo recurso. As exclusões retornam corpo vazio.

### Filtros

| Recurso | Parâmetro | Comportamento |
| --- | --- | --- |
| Categorias | `nome` | Procura um trecho do nome, ignorando maiúsculas e minúsculas |
| Produtos | `nome` | Procura um trecho do nome, ignorando maiúsculas e minúsculas |
| Produtos | `categoriaId` | Seleciona produtos da categoria informada |

Exemplos:

```text
GET /api/categorias?nome=perif
GET /api/produtos?nome=tecla
GET /api/produtos?categoriaId=1
GET /api/produtos?nome=tecla&categoriaId=1
```

Os filtros são opcionais. Quando combinados, os dois critérios devem ser atendidos. As listas são ordenadas por nome em ordem crescente. Consultas sem resultados retornam `200` com `[]`, inclusive quando o filtro indica uma categoria sem registros correspondentes. Não há paginação.

## Respostas HTTP e erros

| Status | Significado |
| --- | --- |
| 200 | Consulta ou atualização concluída |
| 201 | Cadastro concluído, com `Location` |
| 204 | Exclusão concluída, sem corpo |
| 400 | Campo inválido, corpo malformado ou tipo incompatível |
| 404 | Registro consultado ou categoria necessária ao cadastro/atualização não encontrado |
| 409 | Nome duplicado, vínculo existente ou estoque impedindo exclusão |

Exemplo de validação:

```json
{
  "momento": "2026-10-29T12:00:00Z",
  "status": 400,
  "mensagem": "Existem campos inválidos.",
  "campos": {
    "nome": "não deve estar em branco"
  }
}
```

A data acima é ilustrativa. O instante real é gerado no atendimento da requisição. As mensagens por campo são fornecidas pelo mecanismo de validação e podem variar conforme o idioma do ambiente. Nos erros de negócio, o objeto `campos` é vazio e `mensagem` descreve o conflito.

## Exemplo completo no PowerShell

Com a aplicação em execução, execute os comandos abaixo em outro terminal. Utilize nomes ainda não cadastrados para evitar conflitos de duplicidade.

```powershell
$endereco = 'http://localhost:8080/api'
$categoria = Invoke-RestMethod "$endereco/categorias" -Method Post -ContentType 'application/json' -Body '{"nome":"Perifericos"}'

$entrada = @{
    nome = 'Teclado'
    preco = 125.50
    quantidade = 2
    categoriaId = $categoria.id
} | ConvertTo-Json

$produto = Invoke-RestMethod "$endereco/produtos" -Method Post -ContentType 'application/json' -Body $entrada
Invoke-RestMethod "$endereco/produtos/$($produto.id)"
Invoke-RestMethod "$endereco/produtos?nome=tecla&categoriaId=$($categoria.id)"

$atualizacao = @{
    nome = 'Teclado'
    preco = 125.50
    quantidade = 0
    categoriaId = $categoria.id
} | ConvertTo-Json

Invoke-RestMethod "$endereco/produtos/$($produto.id)" -Method Put -ContentType 'application/json' -Body $atualizacao
Invoke-RestMethod "$endereco/produtos/$($produto.id)" -Method Delete
Invoke-RestMethod "$endereco/categorias/$($categoria.id)" -Method Delete
```

## Testes

No Windows:

```powershell
.\mvnw.cmd clean verify
```

No Linux e macOS:

```sh
./mvnw clean verify
```

Também é possível executar `testar.bat` no Windows. A suíte contém oito testes de integração com Spring Boot e MockMvc, cobrindo:

- Operações completas de categoria.
- Operações completas de produto e cálculo do valor de estoque.
- Combinação dos filtros de nome e categoria.
- Duplicidade de categorias e produtos.
- Bloqueio de exclusão por dependência ou estoque.
- Validação de campos e formatos de requisição.
- Rejeição de categoria inexistente.
- Disponibilidade da documentação OpenAPI e redirecionamento do Swagger.

Os relatórios são gerados em `target/surefire-reports`. Os testes usam transações revertidas ao final de cada caso para isolar os registros.

## Empacotamento

```powershell
.\mvnw.cmd clean package
java -jar target/gestao-estoque-1.0.0.jar
```

No Linux e macOS, substitua `.\mvnw.cmd` por `./mvnw`. O arquivo executável inclui o servidor embarcado. Os artefatos gerados em `target` não são versionados; são produzidos durante a compilação.

## Decisões e limites da implementação

- H2 em arquivo simplifica a execução local e mantém os dados entre reinicializações.
- A criação do esquema é gerenciada pelo Hibernate, sem ferramenta de migrações.
- A aplicação não inclui autenticação, autorização, interface frontend ou implantação em hospedagem.
- O estoque é atualizado diretamente no cadastro ou na atualização do produto; não há histórico de movimentações.
- Não há controle específico de concorrência para alterações simultâneas de quantidade.
- Os nomes são devolvidos normalizados, não no formato original digitado.
- A listagem utiliza ordenação fixa por nome e não oferece paginação.

