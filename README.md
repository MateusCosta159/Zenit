# 🪴 Zenit: Rastreador Solar e Térmico

### 🎓 IFSP - Campus Araraquara
**Curso:** Análise e Desenvolvimento de Sistemas  
**Disciplina:** ARQDMO2 - Dispositivos Móveis 2  
**Professor:** Henrique Galati

---

### 👥 Integrantes da Equipe
* **Nome do Aluno 1:** Luiz Gustavo Monico
* **Nome do Aluno 2:** Mateus Costa da Silva

---

## 📝 Visão Geral do Produto
O **Zenit** é um assistente inteligente voltado para a jardinagem urbana e monitoramento climático residencial. O aplicativo permite que entusiastas de botânica e usuários domésticos monitorem as condições ideais para o crescimento de plantas e o conforto térmico de ambientes. Utilizando os sensores físicos do dispositivo móvel e conectividade em nuvem, o Zenit transforma dados ambientais complexos em insights práticos para o dia a dia.

---

## 🛠️ Requisitos Funcionais (RF)
Para garantir o cumprimento integral do edital e alcançar a pontuação máxima, o aplicativo implementa os seguintes requisitos:

* **RF01 -- Recursos Multimídia e Sensores:** O aplicativo explora três recursos de hardware distintos:
  * *Sensor de Ambiente (Luz e Temperatura/Pressão):* Coleta periódica da luminosidade local para verificar a exposição solar ideal para as plantas.
  * *Sensor de Posição (Magnetômetro):* Funciona como uma bússola integrada para auxiliar o usuário a identificar a orientação geográfica (ex: face Norte) de sua varanda ou quintal.
  * *Câmera:* Captura de fotos das plantas para a criação de um diário e histórico visual de crescimento.
* **RF02 -- Interface e Navegação:** O aplicativo possui uma arquitetura composta por, no mínimo, 8 telas (Activities/Fragments) com transição fluida e navegação consistente.
* **RF03 -- Feedback Multissensorial:** O sistema emite notificações push para alertas críticos (ex: calor excessivo) e efeitos sonoros para confirmar medições bem-sucedidas.
* **RF04 -- Persistência de Dados:** Todos os registros de sensores e dados das plantas são sincronizados e persistidos em nuvem utilizando o **Firebase Firestore**.
* **RF05 -- Autenticação:** O controle de acesso do usuário é realizado através de e-mail/senha com integração ao Firebase Auth ou por meio de autenticação biométrica local.

---

## 📱 Arquitetura de Telas (Mínimo de 8 Telas)
O fluxo do usuário foi projetado para cobrir todas as necessidades do MVP de forma intuitiva, cumprindo a meta de navegação do projeto:

1. **Login:** Interface de acesso com campos para credenciais (e-mail e senha) e botão de acesso via biometria.
2. **Cadastro:** Tela para a criação de novas contas de usuário diretamente integradas ao Firebase.
3. **Dashboard (Home):** Visão geral centralizada exibindo a listagem das plantas cadastradas e o resumo do clima atual capturado.
4. **Bússola:** Tela dedicada à orientação geográfica usando o magnetômetro para mapeamento do sol.
5. **Sensor de Ambiente:** Tela dedicada à amostragem e coleta em tempo real da luminosidade local utilizando o sensor de luz.
6. **Adicionar Planta/Ambiente:** Formulário para catalogar novas plantas, incluindo o acionamento da Câmera nativa do dispositivo para registrar a foto do perfil do vaso.
7. **Detalhes da Planta:** Exibição expandida contendo o histórico de dados numéricos coletados pelos sensores e uma linha do tempo com as fotos salvas na nuvem.
8. **Configurações / Perfil:** Painel de gerenciamento para ajustes de alertas, limites de notificação térmica e opção de desconexão da conta (logout).
