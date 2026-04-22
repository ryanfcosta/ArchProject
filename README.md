# ArchProject
## Proposta
A proposta do projeto é fazer um simulador que explique, de forma visual e educacional, o funcionamento da microarquitetura MIC-1.

## Estudos
Foram utilizados livros sobre arquiteturas, websites e arquivos sobre MIC 1 e um simulador base de MIC 1 para estudos e testes sobre os datapaths e as instruções utilizadas no projeto.

## Implementação
O simulador está sendo inteiramente programado na linguagem Java
De maneira mais especifica, utilizando:
 - A plataforma *javaFX* para criar a interface 
 - A ferramenta *Maven* para criar a estrutura do projeto e gerir as dependências

Os registradores e espaços na memória são simulados utilizando classes .java (CPU.java e Memoria.java), com a implementação, até então de:
- 7 registradores (2 de pilha)
- 2 flags
- Status do barramento de controle da RAM
- 14 instruções do MIC-1
- MPCs e suas explicações
- Inputs para executar instruções, gravando-as na memória

Tudo isso é exibido utilizando a dupla 
- *primary.fxml*: Basicamente um arquivo de marcação e estilo, com o design da aplicação
- *PrimaryController.java*: Atualiza labels, cores e gerencia inputs e botões


## Perspectiva de Features
Temos o objetivo de, durante o desenvolvimento do projeto  
- Integrar dois processadores MIC-1 em paralelo
- Simplificar a linguagem de comandos e instruções
