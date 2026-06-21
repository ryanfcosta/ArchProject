# ArchProject
## Proposta
A proposta do projeto é fazer um simulador que explique, de forma visual, o funcionamento da microarquitetura MIC-1, conforme a literatura de Tanenbaum.

## Estudos
Foram utilizados livros sobre arquitetura de com, websites e foram criados arqiovos sobre MIC 1, além de um simulador base de MIC 1 para estudos e testes sobre os datapaths e as instruções utilizadas no projeto.

## Implementação
O simulador está sendo inteiramente programado na linguagem Java
De maneira mais especifica, utilizando:
 - A plataforma *javaFX* para criar a interface 
 - A ferramenta *Maven* para criar a estrutura do projeto e gerir as dependências

Estruturas Implementadas:
- 16 registradores básicos + MAR e MBR
- 2 flags de desvio e 0 status da RAM (idle/read/write)
- Barramentos A, B e C
- Caches L1 e L2 com Mapeamento Direto e política Write Through
- Interpretação das macroinstruções do MIC-1
- Execução de todos os MPCs em 4 subciclos

Tudo isso é exibido utilizando 
- Arquivos fxml para a diagramação das páginas (main, ram-view e cache-view)
- Arquivos controllers para a interação com essas páginas e atualização visual

## Perspectiva de Features
Temos o objetivo de
- Integrar dois processadores MIC-1 em paralelo
- Implementar pipeline  
