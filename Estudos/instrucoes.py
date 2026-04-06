def fetch():
    print("--- FETCH ---") #JOGA O COMANDO NA MEMÓRIA
    print("\nMPC 0: [mar := pc; rd;]")
    print("  Datapath: PC -> Travas da ULA -> ULA (passagem) -> Barramento C -> MAR")  #manda o dado direto pro MAR passagem livre na ula
    print("  Sinal: RD ativado. RAM externa começa a procurar a instrução.")
    
    print("\nMPC 1: [pc := pc + 1; rd;]")
    print("  Datapath: PC -> Travas da ULA -> ULA (soma +1) -> Barramento C -> PC") # simplesmente aumenta o pc
    
    print("\nMPC 2: [ir := mbr; if n then goto 28;]")
    print("  Datapath Externo: RAM -> Barramento de Dados Externo -> MBR") # viaja do pente até a entrada da cpu / buffer (MBR)
    print("  Datapath Interno: MBR -> Travas da ULA -> ULA (passgem) -> Barramento C -> IR") # envia os bits do mbr pro IR

def decode_din(binary):
    print("\n--- DECODE ---") 
    # ULA e o MMUX para testam cada bit do opcode sequencialmente.
    # a instrução sofre um shift a cada etapa para colocar o próximo bit na Flag N.
    
    # 1xxx 
    if binary[0] == '1':
        print("  -> O 1º bit (bit 15) é 1! Flag N ativada no MPC 2.")
        print("  -> Salto GOTO 28 executado. Iniciando decodificação da Família 1xxx.")
        
        print("\nMPC 28: [tir := lshift(ir + ir); if n then goto 32;]")
        # Deslocamento na ULA
        if binary[1] == '1':
             print("  -> O 2º bit (bit 14) é 1! Salto GOTO 32 (Família 11xx - Pilha e Fluxo).")
             
             print("\nMPC 32: [tir := lshift(tir); if n then goto 47;]")
             if binary[2] == '1':
                 print("  -> O 3º bit (bit 13) é 1! Salto GOTO 47 (Família 111x - CALL/Estendidas).")
                 
                 print("\nMPC 47: [alu := tir; if n then goto 55;]")
                 if binary[3] == '1':
                     print("  -> O 4º bit (bit 12) é 1! Salto GOTO 55. Fim da ramificação de 4 bits.")
                     print("  -> Delegação do hardware para instruções estendidas (Família 1111 - PUSH, POP, etc).")
                     return 
                 else:
                     print("  -> O 4º bit (bit 12) é 0. Fim do Decode.")
                     print("  -> Roteamento concluído. Iniciando fase de Execução (Instrução CALL).")
                     return 
             else:
                 print("  -> O 3º bit (bit 13) é 0. Flag N desligada. Transição para MPC 33 (Família 110x).")
                 
                 print("\nMPC 33: [alu := tir; if n then goto 35;]")
                 if binary[3] == '1':
                     print("  -> O 4º bit (bit 12) é 1! Salto GOTO 35. Fim do Decode (Instrução JNZE).")
                     return
                 else:
                     print("  -> O 4º bit (bit 12) é 0. Fim do Decode. Transição para MPC 34 (Instrução JNEG).")
                     return
        else:
             print("  -> O 2º bit (bit 14) é 0. Flag N desligada. Transição para MPC 29 (Família 10xx).")
             
             print("\nMPC 29: [tir := lshift(tir); if n then goto 36;]")
             if binary[2] == '1':
                 print("  -> O 3º bit (bit 13) é 1! Salto GOTO 36 (Família 101x - ADDL e SUBL).")
                 
                 print("\nMPC 36: [alu := tir; if n then goto 44;]")
                 if binary[3] == '1':
                     print("  -> O 4º bit (bit 12) é 1! Salto GOTO 44. Fim do Decode (Instrução SUBL).")
                     return
                 else:
                     print("  -> O 4º bit (bit 12) é 0. Fim do Decode. Roteamento concluído (Instrução ADDL).")
                     return
             else:
                 print("  -> O 3º bit (bit 13) é 0. Flag N desligada. Transição para MPC 30 (Família 100x).")
                 
                 print("\nMPC 30: [alu := tir; if n then goto 38;]")
                 if binary[3] == '1':
                     print("  -> O 4º bit (bit 12) é 1! Salto GOTO 38. Fim do Decode (Instrução STOL).")
                     return
                 else:
                     print("  -> O 4º bit (bit 12) é 0. Fim do Decode. Transição para MPC 31 (Instrução LODL).")
                     return

    # 0xxx 

    else:
        print("  -> O 1º bit (bit 15) é 0. Flag N não ativada. Transição para MPC 3.")
        
        print("\nMPC 3: [tir := lshift(ir + ir); if n then goto 19;]")
        print("  Datapath: IR -> Travas da ULA -> ULA (soma) -> Shifter (deslocamento) -> Barramento C -> TIR")  
        if binary[1] == '1':
            print("  -> O 2º bit (bit 14) é 1! Salto GOTO 19 executado (Família 01xx - Saltos e Constantes).")
            
            print("\nMPC 19: [tir := lshift(tir); if n then goto 23;]")
            if binary[2] == '1':
                print("  -> O 3º bit (bit 13) é 1! Salto GOTO 23 (Família 011x - JUMP e LOCO).")
                
                print("\nMPC 23: [alu := tir; if n then goto 30;]")
                if binary[3] == '1':
                    print("  -> O 4º bit (bit 12) é 1! Salto GOTO 30. Fim do Decode (Instrução LOCO).")
                    return
                else:
                    print("  -> O 4º bit (bit 12) é 0. Fim do Decode. Transição para MPC 24 (Instrução JUMP).")
                    return
            else:
                print("  -> O 3º bit (bit 13) é 0. Flag N desligada. Transição para MPC 20 (Família 010x).")
                
                print("\nMPC 20: [alu := tir; if n then goto 25;]")
                if binary[3] == '1':
                    print("  -> O 4º bit (bit 12) é 1! Salto GOTO 25. Fim do Decode (Instrução JZER).")
                    return
                else:
                    print("  -> O 4º bit (bit 12) é 0. Fim do Decode. Transição para MPC 21 (Instrução JPOS).")
                    return
        else:
            print("  -> O 2º bit (bit 14) é 0. Flag N desligada. Transição para MPC 4 (Família 00xx).")
            
            print("\nMPC 4: [tir := lshift(tir); if n then goto 11;]")
            print("  Datapath: TIR -> Travas da ULA -> ULA (passagem livre) -> Shifter -> Barramento C -> TIR") 
            if binary[2] == '1':
                print("  -> O 3º bit (bit 13) é 1! Salto GOTO 11 (Família 001x - ADDD e SUBD).")
                
                print("\nMPC 11: [alu := tir; if n then goto 15;]")
                if binary[3] == '1':
                    print("  -> O 4º bit (bit 12) é 1! Salto GOTO 15. Fim do Decode (Instrução SUBD).")
                    return
                else:
                    print("  -> O 4º bit (bit 12) é 0. Fim do Decode. Transição para MPC 12 (Instrução ADDD).")
                    return
            else:
                print("  -> O 3º bit (bit 13) é 0. Flag N desligada. Transição para MPC 5 (Família 000x).")
                
                print("\nMPC 5: [alu := tir; if n then goto 9;]")
                print("  Datapath: TIR -> Travas -> ULA (sinaliza flags lógicas) -> Bloqueio de gravação no Barramento C") 
                if binary[3] == '1':
                    print("  -> O 4º bit (bit 12) é 1! Salto GOTO 9. Fim do Decode (Instrução STOD).")
                    return
                else:
                    print("  -> O 4º bit (bit 12) é 0. Fim do Decode. Transição para Execução primária (Instrução LODD).")
                    return

def lodd(binary): # pega o endereço, vai até a RAM externa, busca a variável e tira do buffer e salva no AC (Acumulador)
    adress = binary[4:]
    
    print(f"\n[LODD] Rastreando o Caminho de Dados (Datapath) para: {binary}")
    print("="*85)

    fetch()
    decode_din(binary)    

    print("\n--- EXECUTE")
    print(f"MPC 6: [mar := ir; rd;]")
    print("  Datapath: IR -> Travas da ULA -> ULA (passagem) -> Barramento C -> MAR") # joga até a RAM externa //igual MPC0 mas vem do IR
    print(f"  Sinal: RD ativado. RAM externa recebe o endereço da variável ({adress}).") # lê da RAM
    
    print("\nMPC 7: [rd;]")
    print("  Datapath: Ocioso. Barramentos internos livres aguardando a resposta elétrica da memória RAM.")
    
    print("\nMPC 8: [ac := mbr; goto 0;]")
    print("  Datapath Externo: RAM -> Barramento de Dados Externo -> MBR") # joga pro buffer
    print("  Datapath Interno: MBR -> Travas da ULA -> ULA (passagem) -> Barramento C -> AC (Acumulador)") # grava no acumulador
    print("="*85)
    print("Execução finalizada. Processador pronto para a próxima instrução.\n")

def stod(binary): # pega endereço do acumulador e joga no buffer
    adress = binary[4:]

    print(f"\n[STOD] Rastreando o Caminho de Dados (Datapath) para: {binary}")
    print("="*85)

    fetch()
    decode_din(binary)

    print("\n--- EXECUTE ---")
    print("MPC 9: [mar := ir;]")
    print("  Datapath: IR -> Travas da ULA -> ULA (passagem) -> Barramento C -> MAR") # isola do ir e joga no MAR
    print(f"  -> O endereço de destino ({adress}) é isolado e enviado para o MAR.")
    
    print("\nMPC 10: [mbr := ac; wr; goto 0;]")
    print("  Datapath: AC -> Travas da ULA -> ULA (passagem) -> Barramento C -> MBR") # pega do AC e joga no buffer
    print("  Sinal: WR (Write) ativado. A memória RAM externa recebe a ordem para gravar.") # escreve na RAM
    print("  -> O dado do Acumulador chega ao MBR e é consolidado na RAM.")
    print("="*85)
    print("Execução finalizada. Processador pronto para a próxima instrução.\n")

def addd(binary): # joga endereço no buffer e soma com o que tem no acumulador
    adress = binary[4:]

    print(f"\n[ADDD] Rastreando o Caminho de Dados (Datapath) para: {binary}")
    print("="*85)

    fetch()
    decode_din(binary)

    print("\n--- EXECUTE ---")
    print("MPC 12: [mar := ir; rd;]")
    print("  Datapath: IR -> Travas da ULA -> ULA (passagem) -> Barramento C -> MAR") # acessa o MAR e liga o read
    print(f"  Sinal: RD ativado. Buscando valor na RAM no endereço ({adress}).")
    
    print("\nMPC 13: [rd;]")
    print("  Datapath: Ocioso. Aguardando dado da RAM chegar ao MBR.")
    
    print("\nMPC 14: [ac := ac + mbr; goto 0;]")
    print("  Datapath Interno (A Soma):")
    print("    1. Valor do AC -> Barramento B -> B-LATCH") # acumulador na trava B
    print("    2. Valor do MBR -> AMUX -> Entrada A da ULA") # joga valor do buffer na ULA
    print("    3. ULA: Realiza a operação SOMA entre as duas entradas.")
    print("    4. Resultado -> Barramento C -> AC (Acumulador)") # soma e joga pro ac
    print("  -> Operação aritmética concluída. MPC zerado.")
    print("="*85)
    print("Execução finalizada. Processador pronto para a próxima instrução.\n")

def subd(binary):   # joga endereço no buffer e subtrai ele do que tem no acumulador
    adress = binary[4:]

    print(f"\n[SUBD] Rastreando o Caminho de Dados (Datapath) para: {binary}")
    print("="*85)

    fetch()
    decode_din(binary)

    print("\n--- EXECUTE ---")
    print("MPC 15: [mar := ir; rd;]")
    print("  Datapath: IR -> Travas da ULA -> ULA (passagem) -> Barramento C -> MAR") #acessa o MAR e liga o read
    print(f"  Sinal: RD ativado. Buscando valor na RAM no endereço ({adress}) para a subtração.")
    
    print("\nMPC 16: [rd;]")
    print("  Datapath: Ocioso. Aguardando dado da RAM chegar ao MBR.")
    
    print("\nMPC 17: [ac := ac - mbr; goto 0;]") 
    print("  Datapath Interno (A Subtração):")
    print("    1. Valor do AC -> Barramento B -> B-LATCH") # acumuludor na trava B
    print("    2. Valor do MBR -> AMUX -> Entrada A da ULA") # joga valor do buffer na ULA
    print("    3. ULA: A Unidade de Controle altera os sinais lógicos da ULA para realizar a SUBTRAÇÃO.")
    print("    4. Resultado -> Barramento C -> AC (Acumulador)") #subtrai e joga para o AC
    print("  -> Operação matemática concluída. MPC zerado.")
    print("="*85)
    print("Execução finalizada. Processador pronto para a próxima instrução.\n")

def jpos(binary): # se o dado AC for >= 0 ele pula para o endereço
    adress = binary[4:]

    print(f"\n[JPOS] Rastreando o Caminho de Dados (Datapath) para: {binary}")
    print("="*85)

    fetch()
    decode_din(binary)

    print("\n--- EXECUTE ---")
    print("MPC 21: [alu := ac; if n then goto 0;]") # conferir se positivo
    print("  Datapath Interno (O Teste Lógico):")
    print("    1. O valor do AC viaja pelo Barramento B até à Trava B.") # acumulador para trava B da ULA
    print("    2. A ULA avalia o valor apenas para testar a Flag N (Negativo).")
    print("    3. O Barramento C não recebe ordem de escrita para nenhum registrador.")
    print("  -> Se AC for negativo (N=1): O hardware executa o salto para MPC 0 (o desvio é abortado).") # flag desvia
    print("  -> Se AC for positivo ou zero (N=0): A Flag N fica desligada, o fluxo cai para a próxima linha.")
    
    print("\nMPC 22: [pc := ir; goto 0;]")
    print("  Datapath Interno (A Atualização do PC):")
    print("    1. O endereço de destino isolado no IR passa livremente pela ULA.")
    print("    2. Sobe pelo Barramento C.")
    print(f"    3. É gravado diretamente no PC. O próximo 'Fetch' lerá da gaveta {adress}.") # endereço gravado no pc
    print("  -> Salto executado com sucesso! O MPC é zerado (goto 0).")
    print("="*85)
    print("Execução finalizada. Processador pronto para ler do novo endereço.\n") # fetch vai guardar novo endereço na memória


def jzer(binary): # se o dado AC for == 0 ele pula para o endereço
    adress = binary[4:]

    print(f"\n[JZER] Rastreando o Caminho de Dados (Datapath) para: {binary}")
    print("="*85)

    fetch()
    decode_din(binary)

    print("\n--- EXECUTE ---")
    print("MPC 25: [alu := ac; if z then goto 27;]")
    print("  Datapath Interno (O Teste Lógico da Flag Z):") #flag Z (ver se é zero)
    print("    1. O valor do AC viaja pelo Barramento B até à Trava B.")    
    print("    2. A ULA avalia o valor apenas para testar a Flag Z (Zero).")
    print("    3. O Barramento C não recebe ordem de escrita para nenhum registrador.")
    print("  -> Se AC for igual a zero (Z=1): A ULA acende a Flag Z e o hardware salta para MPC 27.") # desvia para MPC 27
    print("  -> Se AC for diferente de zero (Z=0): A Flag Z fica desligada, o fluxo cai para a próxima linha.")

    print("\nMPC 26: [goto 0;]")
    print("  -> O salto falhou (AC != 0). O processador aborta o desvio e volta para o ciclo Fetch inicial.") # não é zero, volta a buscar

    print("\nMPC 27: [pc := ir; goto 0;]")
    print("  Datapath Interno (A Atualização do PC):")
    print("    1. O endereço de destino isolado no IR passa livremente pela ULA.") 
    print("    2. Sobe pelo Barramento C.")
    print(f"    3. É gravado diretamente no PC. O próximo 'Fetch' lerá da gaveta {adress}.") #endereço gravado no pc
    print("  -> Salto executado com sucesso! O MPC é zerado.")
    print("="*85)
    print("Execução finalizada. Processador pronto para ler do próximo endereço.\n") # fetch vai guardar novo endereço na memória

def jump(binary): # pula para o endereço
    adress = binary[4:]

    print(f"\n[JUMP] Rastreando o Caminho de Dados (Datapath) para: {binary}")
    print("="*85)

    fetch()
    decode_din(binary)

    print("\n--- EXECUTE ---")
    print("MPC 24: [pc := ir; goto 0;]")
    print("  Datapath Interno (A Atualização Direta do PC):")
    print("    1. O endereço de destino isolado no IR passa livremente pela ULA.") 
    print("    2. Não há testes de Flags. O endereço sobe direto pelo Barramento C.")
    print(f"    3. É gravado diretamente no PC. O fluxo de execução foi alterado definitivamente para a gaveta {adress}.") #endereço gravado no pc
    print("  -> Salto executado com sucesso! O MPC é zerado (goto 0).")
    print("="*85)
    print("Execução finalizada. Processador pronto para ler do novo endereço.\n") # fetch vai guardar novo endereço na memória

def loco(binary): # salva uma constante no acumulador
    constante = binary[4:]

    print(f"\n[LOCO] Rastreando o Caminho de Dados (Datapath) para: {binary}")
    print("="*85)

    fetch()
    decode_din(binary)

    print("\n--- EXECUTE ---")
    print("MPC 30: [ac := ir and amask; goto 0;]")
    print("  Datapath Interno (Isolamento da Constante via Máscara):")
    print("    1. Valor do IR (16 bits) -> Barramento B -> B-LATCH")
    print("    2. Valor interno do AMASK (4095) -> Barramento A -> A-LATCH")
    print("    3. ULA: Executa a operação lógica AND. Apaga os 4 bits iniciais e passa os 12 finais.") # transforma o "endereço" em constante
    print("    4. Resultado -> Barramento C -> AC (Acumulador)") # salva no acumulador
    print(f"  -> A constante ({constante}) foi extraída e salva no AC! MPC zerado (goto 0).")
    print("="*85)
    print("Execução finalizada. Processador pronto para a próxima instrução.\n")

def lodl(binary): # joga uma variável local da pilha para o ac
    offset = binary[4:]
    print(f"\n[LODL] Rastreando o Caminho de Dados (Datapath) para: {binary}")
    print("="*85)

    fetch()
    decode_din(binary)

    print("\n--- EXECUTE ---")
    print("MPC 31: [mar := ir + lv; rd;]")
    print("  Datapath Interno (Cálculo do Ponteiro na ULA):")
    print("    1. Valor do IR (12 bits de offset) -> Barramento B -> B-LATCH") # offset vai pra trava B
    print("    2. Valor do registrador LV (Local Variables) -> Barramento A -> A-LATCH") # variavel local vai pra trava A
    print("    3. ULA: Executa a operação de SOMA para encontrar o endereço absoluto na memória.") # endereço absoluto é a soma
    print("    4. Resultado -> Barramento C -> MAR")
    print(f"  Sinal: RD ativado. A RAM começa a buscar o dado na gaveta calculada (LV + {offset}).")
    
    print("\nMPC 32: [rd;]")
    print("  Datapath: Ocioso. Barramentos internos livres aguardando a resposta elétrica da memória RAM.")
    
    print("\nMPC 33: [ac := mbr; goto 0;]")
    print("  Datapath Externo: RAM -> Barramento de Dados Externo -> MBR") # joga no buffer
    print("  Datapath Interno: MBR -> Travas da ULA -> ULA (passagem) -> Barramento C -> AC (Acumulador)") # buffer -> ac
    print("="*85)
    print("Execução finalizada. Processador pronto para a próxima instrução.\n")

def stol(binary): # joga o valor em ac para a variável da pilha
    offset = binary[4:]

    print(f"\n[STOL] Rastreando o Caminho de Dados (Datapath) para: {binary}")
    print("="*85)

    fetch()
    decode_din(binary)

    print("\n--- EXECUTE ---")
    print("MPC 38: [mar := ir + lv;]")
    print("  Datapath Interno (Cálculo do Ponteiro na ULA):")
    print("    1. Valor do IR (12 bits de offset) -> Barramento B -> B-LATCH") #offset vai para a trava b
    print("    2. Valor do registrador LV (Local Variables) -> Barramento A -> A-LATCH") # variavel local vai pra trava A
    print("    3. ULA: Executa a operação de SOMA para encontrar o endereço absoluto na memória.") # endereço absoluto é a soma
    print("    4. Resultado -> Barramento C -> MAR")
    print(f"  -> A RAM externa é notificada de que a gaveta alvo é (LV + {offset}).")
    
    print("\nMPC 39: [mbr := ac; wr; goto 0;]")
    print("  Datapath Interno: AC -> Travas da ULA -> ULA (passagem) -> Barramento C -> MBR") # escreve no buffer
    print("  Sinal: WR (Write) ativado. A memória RAM recebe a ordem para gravar o valor.")
    print("  -> O dado do Acumulador chega ao MBR e é consolidado na RAM. MPC zerado.")
    print("="*85)
    print("Execução finalizada. Processador pronto para a próxima instrução.\n")

def addl(binary): # soma a variável local com o acumulador
    offset = binary[4:]

    print(f"\n[ADDL] Rastreando o Caminho de Dados (Datapath) para: {binary}")
    print("="*85)

    fetch()
    decode_din(binary)

    print("\n--- EXECUTE ---")
    print("MPC 41: [mar := ir + lv; rd;]")
    print("  Datapath Interno (Cálculo do Ponteiro na ULA):")
    print("    1. Valor do IR (offset) -> Barramento B -> B-LATCH") # offset na trava b
    print("    2. Valor do registrador LV -> Barramento A -> A-LATCH") # variavel local na trava a
    print("    3. ULA: Executa a operação de SOMA para encontrar o endereço absoluto.") # endereço absoluto é a soma
    print("    4. Resultado -> Barramento C -> MAR")
    print(f"  Sinal: RD ativado. A RAM começa a buscar o dado na gaveta calculada (LV + {offset}).")
    
    print("\nMPC 42: [rd;]")
    print("  Datapath: Ocioso. Barramentos internos livres aguardando o dado da RAM chegar ao MBR.") # escreve no buffer
    
    print("\nMPC 43: [ac := ac + mbr; goto 0;]")
    print("  Datapath Interno (A Soma Matemática):")
    print("    1. Valor do AC atual -> Barramento B -> B-LATCH") # offset na trava b
    print("    2. Valor do MBR (que acabou de chegar) -> AMUX -> Entrada A da ULA") # variavel local na emtrada a
    print("    3. ULA: Realiza a SOMA entre as duas entradas.") # soma e coloca no acumulador
    print("    4. Resultado -> Barramento C -> AC (Acumulador)")
    print("  -> Operação concluída e Acumulador atualizado! MPC zerado (goto 0).")
    print("="*85)
    print("Execução finalizada. Processador pronto para a próxima instrução.\n")

def subl(binary): # soma a variável local do acumulador
    offset = binary[4:]

    print(f"\n[SUBL] Rastreando o Caminho de Dados (Datapath) para: {binary}")
    print("="*85)

    fetch()
    decode_din(binary)

    print("\n--- EXECUTE ---")
    print("MPC 44: [mar := ir + lv; rd;]")
    print("  Datapath Interno (Cálculo do Ponteiro na ULA):")
    print("    1. Valor do IR (offset) -> Barramento B -> B-LATCH")  # offset na trava b
    print("    2. Valor do registrador LV -> Barramento A -> A-LATCH") # variavel local na trava a
    print("    3. ULA: Executa a operação de SOMA para encontrar o endereço absoluto.") # endereço absoluto é a soma

    print("    4. Resultado -> Barramento C -> MAR")
    print(f"  Sinal: RD ativado. A RAM começa a buscar o dado na gaveta calculada (LV + {offset}).")
    
    print("\nMPC 45: [rd;]")
    print("  Datapath: Ocioso. Barramentos internos livres aguardando o dado da RAM chegar ao MBR.") # jogar no buffer
    
    print("\nMPC 46: [ac := ac - mbr; goto 0;]")
    print("  Datapath Interno (A Subtração Matemática):")
    print("    1. Valor do AC atual -> Barramento B -> B-LATCH") # ac na trava b
    print("    2. Valor do MBR (que acabou de chegar) -> AMUX -> Entrada A da ULA") # variavel local na entrada a
    print("    3. ULA: Realiza a SUBTRAÇÃO entre as duas entradas.")  # subtrai e coloca no ac
    print("    4. Resultado -> Barramento C -> AC (Acumulador)")
    print("  -> Operação concluída e Acumulador atualizado! MPC zerado (goto 0).")
    print("="*85)
    print("Execução finalizada. Processador pronto para a próxima instrução.\n")

def call(binary):
    adress = binary[4:]
    
    print(f"\n[CALL] Rastreando o Caminho de Dados (Datapath) para: {binary}")
    print("="*85)
    
    fetch()
    decode_din(binary)
    
    print("\n--- EXECUTE ---")
    print("MPC 48: [sp := sp + 1;]")
    print("  Datapath Interno: SP -> Travas da ULA -> ULA (soma +1) -> Barramento C -> SP")
    print("  -> O Stack Pointer (SP) é incrementado para abrir um novo espaço na pilha.")
    
    print("\nMPC 49: [mar := sp;]")
    print("  Datapath Interno: SP -> Travas da ULA -> ULA (passagem) -> Barramento C -> MAR")
    print("  -> O endereço do topo atualizado da pilha vai para o MAR.")
    
    print("\nMPC 50: [mbr := pc; wr;]")
    print("  Datapath Interno: PC -> Travas da ULA -> ULA (passagem) -> Barramento C -> MBR")
    print("  Sinal: WR (Write) ativado. A memória RAM grava o endereço de retorno no topo da pilha.")
    
    print("\nMPC 51: [pc := ir; goto 0;]")
    print("  Datapath Interno: IR -> Travas da ULA -> ULA (passagem) -> Barramento C -> PC")
    print(f"  -> O PC recebe o endereço da função ({adress}). Salto executado! MPC zerado (goto 0).")
    print("="*85)
    print("Execução finalizada. Processador pronto para executar a nova função.\n")