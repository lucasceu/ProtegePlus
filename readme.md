# PROTEGE+ 🏥

**PROTEGE+** é um aplicativo Android nativo desenvolvido para auxiliar usuários na identificação de sintomas, consulta de protocolos de primeiros socorros (SBV), acesso rápido a telefones de emergência e testes de conhecimento na área da saúde.

O projeto utiliza uma arquitetura baseada em **SQLite** local, garantindo funcionamento offline e alta performance.

---

## 📱 Funcionalidades Principais

### 1. Verificador de Sintomas 🩺
* **Busca Inteligente:** O usuário seleciona sintomas através de uma lista categorizada por sistemas do corpo humano (ícones e expansão visual).
* **Algoritmo de Ranking:** O sistema cruza os sintomas selecionados com o banco de dados de enfermidades e retorna um ranking das doenças mais prováveis, indicando a "força" da compatibilidade (ex: "Compatível com 3 de 5 sintomas").
* **Detalhes:** Exibição amigável de sintomas inclusos, exclusivos e definições.

### 2. Suporte Básico de Vida (SBV) 🚑
* **Protocolos de Acidentes:** Consulta rápida a procedimentos de emergência (ex: Queimaduras, Fraturas, Intoxicações).
* **Navegação:** Lista agrupada por categorias de acidentes com setas indicativas.
* **Leitura:** Interface otimizada (`NestedScrollView`) para leitura de textos longos e procedimentos técnicos.

### 3. Serviços de Emergência ☎️
* **Discagem Rápida:** Lista de telefones úteis (SAMU, Bombeiros, Hospitais) com filtro de busca em tempo real.
* **Integração:** Clique no item para abrir o discador do Android automaticamente.

### 4. Teste de Conhecimento (Quiz) 🧠
* **Gamificação:** Perguntas de múltipla escolha sobre saúde e primeiros socorros.
* **Lógica de Jogo:** Sorteio aleatório de questões sem repetição (técnica de embaralhamento).
* **Feedback Imediato:** Indicação visual (Verde/Vermelho) ao responder e pontuação final.

### 5. Área Administrativa (CRUD) ⚙️
Acesso restrito para gerenciamento do conteúdo do aplicativo:
* **Gerenciar Pessoas:** Cadastro, edição e exclusão de usuários e administradores.
* **Gerenciar Telefones:** Adicionar ou remover números da lista pública de emergência.
* **Gerenciar Questionários:** Criação completa de novas perguntas e alternativas para o Quiz.

---

## 🛠 Tecnologias Utilizadas

* **Linguagem:** Kotlin (Principal) e Java (Legado/Compatibilidade).
* **Interface (UI):** XML Layouts com Material Design.
    * Uso extensivo de `CardView`, `RecyclerView` e `ConstraintLayout`.
    * Estilização customizada (Botões arredondados, Seletores de Checkbox, Fontes Nunito Sans).
* **Banco de Dados:** SQLite (Nativo).
    * **DatabaseHelper:** Gerenciador robusto de versão e cópia do banco pré-populado da pasta `assets`.
    * **Versão Atual do DB:** v11.

---

## 📂 Estrutura do Banco de Dados

O aplicativo utiliza um banco pré-existente (`BD_Protege_v12.db`) copiado para o dispositivo. As principais tabelas são:

* `tb_ciap`: Enfermidades, códigos CIAP-2 e sintomas associados.
* `tb_sintomas`: Lista de sintomas para seleção.
* `sbv_acidentes`: Conteúdo textual dos protocolos de socorro.
* `telefonesuteis`: Números de emergência.
* `questao_questionario` / `resposta_questionario`: Dados para o Quiz (1:N).
* `pessoa`: Dados de usuários e login.

---

## 🚀 Como Executar o Projeto

1.  **Pré-requisitos:** Android Studio instalado (versão recente).
2.  **Clonar/Baixar:** Baixe o código fonte para sua máquina.
3.  **Banco de Dados:**
    * Certifique-se de que o arquivo `BD_Protege_v12.db` esteja na pasta `app/src/main/assets/`.
4.  **Compilar:**
    * Abra o projeto no Android Studio.
    * Aguarde a sincronização do Gradle.
    * Execute em um Emulador ou Dispositivo Físico.

---

## 🎨 Identidade Visual

O projeto segue um padrão de cores forte e consistente:
* **Primária:** Vermelho Protege (`#D32F2F` / `@color/protegeRed`)
* **Secundária/Detalhes:** Teal (`@color/protegeTeal`)
* **Tipografia:** Família Nunito Sans (Regular, SemiBold, Bold).

---

## 📝 Status do Desenvolvimento

* [x] Login e Cadastro de Usuários.
* [x] Integração SQLite (DatabaseHelper).
* [x] Módulo de Sintomas (Busca e Ranking).
* [x] Módulo SBV (Navegação e Detalhes).
* [x] Módulo Telefones (Lista Pública e Admin).
* [x] Módulo Quiz (Jogo e Admin).
* [x] Polimento de UI/UX (Ícones, Espaçamentos, Cards).

---

Desenvolvido com 💻 e ☕.