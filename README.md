# ArchProject
## Proposta
A proposta do projeto é fazer um simulador que explique, de forma visual, o funcionamento da microarquitetura MIC-1, conforme a literatura de Tanenbaum.

## Estudos
Foram utilizados livros sobre arquitetura de comp, websites e foram criados arqiovos sobre MIC 1, além de um simulador base de MIC 1 para estudos e testes sobre os datapaths e as instruções utilizadas no projeto.

## Implementação
O simulador está sendo inteiramente programado na linguagem Java
De maneira mais especifica, utilizando:
 - A plataforma *javaFX* para criar a interface 
 - A ferramenta *Maven* para criar a estrutura do projeto e gerir as dependências

Os registradores e espaços na memória são simulados utilizando classes .java (CPU.java e Memoria.java), com a implementação, até então de:
- 16 básicos + MAR e MBR
- 2 flags
- Barramentos A, B e C
- Interpretação das macroinstruções do MIC-1
- Execução de todos os MPCs em 4 subciclos
- Inputs para executar instruções, gravando-as na memóri

Tudo isso é exibido utilizando a dupla 
- *primary.fxml*: Basicamente um arquivo de marcação e estilo, com o design da aplicação
- *PrimaryController.java*: Atualiza labels, cores e gerencia inputs e botões

## Perspectiva de Features
Temos o objetivo de, durante o desenvolvimento do projeto  
- Integrar dois processadores MIC-1 em paralelo
- Implementar macroprogramação em console em vez de memória
- Implementar memória cache e pipeline  