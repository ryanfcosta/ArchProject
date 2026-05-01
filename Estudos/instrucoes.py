def fetch():
    print("--- FETCH ---") # JOGA O COMANDO NA MEMÓRIA
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
    # ULA e o MMUX testam cada bit do opcode sequencialmente.
    # a instrução sofre um shift a cada etapa para colocar o próximo bit na Flag N.
    
    # 1xxx 
    if binary[0] == '1':
        print("  -> O 1º bit (bit 15) é 1! Flag N ativada no MPC 2.")
        print("  -> Salto GOTO 28 executado. Iniciando decodificação da Família 1xxx.")
        
        print("\nMPC 28: [tir := lshift(ir + ir); if n then goto 40;]")
        if binary[1] == '1':
             print("  -> O 2º bit (bit 14) é 1! Salto GOTO 40 (Família 11xx).")
             
             print("\nMPC 40: [tir := lshift(tir); if n then goto 46;]")
             if binary[2] == '1':
                 print("  -> O 3º bit (bit 13) é 1! Salto GOTO 46 (Família 111x).")
                 
                 # call ou família extendida
                 print("\nMPC 46: [tir := lshift(tir); if n then goto 50;]")
                 if binary[3] == '1':
                     print("  -> O 4º bit (bit 12) é 1! Salto GOTO 50 (Início da Família 1111).")
                     
                     print("\nMPC 50: [tir := lshift(tir); if n then goto 65;]")
                     if binary[4] == '1':
                         print("  -> O 5º bit (bit 11) é 1! Salto GOTO 65 (RETN/SWAP/INSP/DESP).")
                         
                         print("\nMPC 65: [tir := lshift(tir); if n then goto 73;]")
                         if binary[5] == '1':
                             print("  -> O 6º bit (bit 10) é 1! Salto GOTO 73 (INSP/DESP).")
                             
                             print("\nMPC 73: [alu := tir; if n then goto 76;]")
                             print("  -> O 7º bit (bit 9) é 1! Salto GOTO 76. Fim do Decode (DESP)." )if binary[6] == '1' else "  -> O 7º bit (bit 9) é 0. Fim do Decode. Destino: MPC 74 (INSP)."
                             return
                         else:
                             print("  -> O 6º bit (bit 10) é 0. Transição para MPC 66.")
                             
                             print("\nMPC 66: [alu := tir; if n then goto 70;]")
                             print("  -> O 7º bit (bit 9) é 1! Salto GOTO 70. Fim do Decode (SWAP).") if binary[6] == '1' else "  -> O 7º bit (bit 9) é 0. Fim do Decode. Destino: MPC 67 (RETN)."
                             return
                     else:
                         print("  -> O 5º bit (bit 11) é 0. Transição para MPC 51.")
                         
                         print("\nMPC 51: [tir := lshift(tir); if n then goto 59;]")
                         if binary[5] == '1':
                             print("  -> O 6º bit (bit 10) é 1! Salto GOTO 59 (PUSH/POP).")
                             
                             print("\nMPC 59: [alu := tir; if n then goto 62;]")
                             if binary[6] == '1':
                                 print("  -> O 7º bit (bit 9) é 1! Salto GOTO 62. Fim do Decode (POP).")
                             else:
                                 print("  -> O 7º bit (bit 9) é 0. Fim do Decode. Destino: MPC 60 (PUSH).")
                             return
                         else:
                            print("  -> O 6º bit (bit 10) é 0. Transição para MPC 52.")
                             
                            print("\nMPC 52: [alu := tir; if n then goto 56;]")
                             
                            print("  -> O 7º bit (bit 9) é 1! Salto GOTO 56. Fim do Decode (POPI).") if binary[6] == '1' else "  -> O 7º bit (bit 9) é 0. Fim do Decode. Destino: MPC 53 (PSHI)."
                            return
                 else:
                     print("  -> O 4º bit (bit 12) é 0. Fim do Decode. Destino: MPC 47 (CALL).")
                     return 
             else:
                 print("  -> O 3º bit (bit 13) é 0. Flag N desligada. Transição para MPC 41.")
                 
                 print("\nMPC 41: [alu := tir; if n then goto 44;]")
                 if binary[3] == '1':
                     print("  -> O 4º bit (bit 12) é 1! Salto GOTO 44 (JNZE).")
                     return
                 else:
                     print("  -> O 4º bit (bit 12) é 0. Fim do Decode (JNEG).")
                     return
        else:
             print("  -> O 2º bit (bit 14) é 0. Flag N desligada. Transição para MPC 29 (Família 10xx).")
             
             print("\nMPC 29: [tir := lshift(tir); if n then goto 35;]")
             if binary[2] == '1':
                 print("  -> O 3º bit (bit 13) é 1! Salto GOTO 35.")
                 
                 print("\nMPC 35: [alu := tir; if n then goto 38;]")
                 if binary[3] == '1':
                     print("  -> O 4º bit (bit 12) é 1! Salto GOTO 38 (SUBL).")
                     return
                 else:
                     print("  -> O 4º bit (bit 12) é 0. Roteamento concluído (ADDL).")
                     return
             else:
                 print("  -> O 3º bit (bit 13) é 0. Flag N desligada. Transição para MPC 30.")
                 
                 print("\nMPC 30: [alu := tir; if n then goto 33;]")
                 if binary[3] == '1':
                     print("  -> O 4º bit (bit 12) é 1! Salto GOTO 33 (STOL).")
                     return
                 else:
                     print("  -> O 4º bit (bit 12) é 0. Transição para MPC 31 (LODL).")
                     return

    # 0xxx 
    else:
        print("  -> O 1º bit (bit 15) é 0. Flag N não ativada. Transição para MPC 3.")
        
        print("\nMPC 3: [tir := lshift(ir + ir); if n then goto 19;]")
        if binary[1] == '1':
            print("  -> O 2º bit (bit 14) é 1! Salto GOTO 19.")
            
            print("\nMPC 19: [tir := lshift(tir); if n then goto 25;]")
            if binary[2] == '1':
                print("  -> O 3º bit (bit 13) é 1! Salto GOTO 25.")
                
                print("\nMPC 25: [alu := tir; if n then goto 27;]")
                if binary[3] == '1':
                    print("  -> O 4º bit (bit 12) é 1! Salto GOTO 27 (LOCO).")
                    return
                else:
                    print("  -> O 4º bit (bit 12) é 0. Transição para MPC 26 (JUMP).")
                    return
            else:
                print("  -> O 3º bit (bit 13) é 0. Transição para MPC 20.")
                
                print("\nMPC 20: [alu := tir; if n then goto 23;]")
                if binary[3] == '1':
                    print("  -> O 4º bit (bit 12) é 1! Salto GOTO 23 (JZER).")
                    return
                else:
                    print("  -> O 4º bit (bit 12) é 0. Transição para MPC 21 (JPOS).")
                    return
        else:
            print("  -> O 2º bit (bit 14) é 0. Transição para MPC 4.")
            
            print("\nMPC 4: [tir := lshift(tir); if n then goto 11;]")
            if binary[2] == '1':
                print("  -> O 3º bit (bit 13) é 1! Salto GOTO 11.")
                
                print("\nMPC 11: [alu := tir; if n then goto 15;]")
                if binary[3] == '1':
                    print("  -> O 4º bit (bit 12) é 1! Salto GOTO 15 (SUBD).")
                    return
                else:
                    print("  -> O 4º bit (bit 12) é 0. Transição para MPC 12 (ADDD).")
                    return
            else:
                print("  -> O 3º bit (bit 13) é 0. Transição para MPC 5.")
                
                print("\nMPC 5: [alu := tir; if n then goto 9;]")
                if binary[3] == '1':
                    print("  -> O 4º bit (bit 12) é 1! Salto GOTO 9 (STOD).")
                    return
                else:
                    print("  -> O 4º bit (bit 12) é 0. Transição para MPC 6 (LODD).")
                    return

def inicia_trace(nome, binary):
    print(f"\n[{nome}] Rastreando o Caminho de Dados (Datapath) para: {binary}")
    print("="*85)
    fetch()
    decode_din(binary)
    print("\n--- EXECUTE ---")

def fim_trace(mensagem="Execução finalizada. Processador pronto para a próxima instrução."):
    print("="*85)
    print(mensagem + "\n")

def lodd(binary): # pega o endereço, vai até a RAM externa, busca a variável e tira do buffer e salva no AC (Acumulador)
    adress = binary[4:]
    inicia_trace("LODD", binary)
    
    print(f"MPC 6: [mar := ir; rd;]")
    print("  Datapath: IR -> Travas da ULA -> ULA (passagem) -> Barramento C -> MAR") # joga até a RAM externa //igual MPC0 mas vem do IR
    print(f"  Sinal: RD ativado. RA M externa recebe o endereço da variável ({adress}).") # lê da RAM
    
    print("\nMPC 7: [rd;]")
    print("  Datapath: Ocioso. Barramentos internos livres aguardando a resposta elétrica da memória RAM.")
    
    print("\nMPC 8: [ac := mbr; goto 0;]")
    print("  Datapath Externo: RAM -> Barramento de Dados Externo -> MBR") # joga pro buffer
    print("  Datapath Interno: MBR -> Travas da ULA -> ULA (passagem) -> Barramento C -> AC (Acumulador)") # grava no acumulador
    
    fim_trace()

def stod(binary): # pega endereço do acumulador e joga no buffer
    adress = binary[4:]
    inicia_trace("STOD", binary)

    print("MPC 9: [mar := ir; mbr := ac; wr;]")
    print("  Datapath: IR -> MAR e AC -> MBR ao mesmo tempo. Sinal WR ativado.") # isola do ir e joga no MAR
    print(f"  -> O endereço de destino ({adress}) é isolado e enviado para o MAR.")
    
    print("\nMPC 10: [wr; goto 0;]")
    print("  Sinal: WR (Write) ativado novamente.") # escreve na RAM
    print("  -> O dado do Acumulador chega ao MBR e é consolidado na RAM.")
    
    fim_trace()

def addd(binary): # joga endereço no buffer e soma com o que tem no acumulador
    adress = binary[4:]
    inicia_trace("ADDD", binary)

    print("MPC 12: [mar := ir; rd;]")
    print("  Datapath: IR -> Travas da ULA -> ULA (passagem) -> Barramento C -> MAR") # acessa o MAR e liga o read
    print(f"  Sinal: RD ativado. Buscando valor na RAM no endereço ({adress}).")
    
    print("\nMPC 13: [rd;]")
    print("  Datapath: Ocioso. Aguardando dado da RAM chegar ao MBR.")
    
    print("\nMPC 14: [ac := mbr + ac; goto 0;]")
    print("  Datapath Interno (A Soma):")
    print("    1. Valor do AC -> Barramento B -> B-LATCH") # acumulador na trava B
    print("    2. Valor do MBR -> AMUX -> Entrada A da ULA") # joga valor do buffer na ULA
    print("    3. ULA: Realiza a operação SOMA entre as duas entradas.")
    print("    4. Resultado -> Barramento C -> AC (Acumulador)") # soma e joga pro ac
    print("  -> Operação aritmética concluída. MPC zerado.")
    
    fim_trace()

def subd(binary):   # joga endereço no buffer e subtrai ele do que tem no acumulador
    adress = binary[4:]
    inicia_trace("SUBD", binary)

    print("MPC 15: [mar := ir; rd;]")
    print("  Datapath: IR -> Travas da ULA -> ULA (passagem) -> Barramento C -> MAR") #acessa o MAR e liga o read
    print(f"  Sinal: RD ativado. Buscando valor na RAM no endereço ({adress}) para a subtração.")
    
    print("\nMPC 16: [ac := ac + 1; rd;]")
    print("  Datapath Interno: AC -> ULA (Soma +1) -> AC. Inicia o complemento de 2.")
    
    print("\nMPC 17: [a := inv(mbr);]")
    print("  Datapath Interno: Inverte os bits da variável no MBR.")
    
    print("\nMPC 18: [ac := ac + a; goto 0;]")
    print("  Datapath Interno (A Subtração Matemática via Complemento de 2):") # subtrai e coloca no ac
    print("    1. MBR invertido somado ao AC (que já foi incrementado).")
    print("  -> Operação matemática concluída. MPC zerado.")
    
    fim_trace()

def jpos(binary): # se o dado AC for >= 0 ele pula para o endereço
    adress = binary[4:]
    inicia_trace("JPOS", binary)

    print("MPC 21: [alu := ac; if n then goto 0;]") # conferir se positivo
    print("  Datapath Interno (O Teste Lógico):")
    print("    1. O valor do AC viaja pelo Barramento B até à Trava B.") # acumulador para trava B da ULA
    print("    2. A ULA avalia o valor apenas para testar a Flag N (Negativo).")
    print("    3. O Barramento C não recebe ordem de escrita para nenhum registrador.")
    print("  -> Se AC for negativo (N=1): O hardware executa o salto para MPC 0 (o desvio é abortado).") # flag desvia
    print("  -> Se AC for positivo ou zero (N=0): A Flag N fica desligada, o fluxo cai para a próxima linha.")
    
    print("\nMPC 22: [pc := band(ir, amask); goto 0;]")
    print("  Datapath Interno (A Atualização do PC via Máscara):")
    print("    1. A máscara amask extrai apenas o endereço de destino do IR.")
    print(f"    2. É gravado diretamente no PC. O próximo 'Fetch' lerá da gaveta {adress}.") # endereço gravado no pc
    print("  -> Salto executado com sucesso! O MPC é zerado (goto 0).")
    
    fim_trace("Execução finalizada. Processador pronto para ler do próximo endereço.")

def jzer(binary): # se o dado AC for == 0 ele pula para o endereço
    adress = binary[4:]
    inicia_trace("JZER", binary)

    print("MPC 23: [alu := ac; if z then goto 22;]")
    print("  Datapath Interno (O Teste Lógico da Flag Z):") #flag Z (ver se é zero)
    print("    1. O valor do AC viaja pelo Barramento B até à Trava B.")    
    print("    2. A ULA avalia o valor apenas para testar a Flag Z (Zero).")
    print("  -> Se AC for igual a zero (Z=1): A ULA acende a Flag Z e o hardware salta para MPC 22.") # desvia para MPC 22
    
    print("\nMPC 24: [goto 0;]")
    print("  -> O salto falhou (AC != 0). O processador aborta o desvio e volta para o ciclo Fetch inicial.") # não é zero, volta a buscar

    print("\nMPC 22: [pc := band(ir, amask); goto 0;]")
    print("  Datapath Interno (A Atualização do PC via Máscara):")
    print(f"  -> É gravado diretamente no PC. O próximo 'Fetch' lerá da gaveta {adress}.") #endereço gravado no pc
    print("  -> Salto executado com sucesso! O MPC é zerado.")
    
    fim_trace("Execução finalizada. Processador pronto para ler do próximo endereço.")

def jump(binary): # pula para o endereço
    adress = binary[4:]
    inicia_trace("JUMP", binary)

    print("MPC 26: [pc := band(ir, amask); goto 0;]")
    print("  Datapath Interno (A Atualização Direta do PC):")
    print("    1. O endereço de destino isolado no IR via amask.") 
    print("    2. Não há testes de Flags.")
    print(f"    3. É gravado diretamente no PC. O fluxo de execução foi alterado definitivamente para a gaveta {adress}.") #endereço gravado no pc
    print("  -> Salto executado com sucesso! O MPC é zerado (goto 0).")
    
    fim_trace("Execução finalizada. Processador pronto para ler do próximo endereço.")

def jneg(binary): # if AC for < 0 pula para o endereço
    adress = binary[4:]
    inicia_trace("JNEG", binary)

    print("MPC 42: [alu := ac; if n then goto 22;]")
    print("  Datapath Interno (O Teste Lógico da Flag N):")
    print("    1. O valor do AC viaja para a ULA.")
    print("    2. A ULA avalia o valor para testar a Flag N (Negativo).")
    print("  -> Se AC for negativo (N=1): A ULA acende a Flag N e o hardware salta para MPC 22.")
    
    print("\nMPC 43: [goto 0;]")
    print("  -> O salto falhou (AC >= 0). O processador aborta o desvio.")

    print("\nMPC 22: [pc := band(ir, amask); goto 0;]")
    print("  Datapath Interno (A Atualização do PC via Máscara):")
    print(f"  -> É gravado diretamente no PC. O próximo 'Fetch' lerá da gaveta {adress}.")
    print("  -> Salto executado com sucesso! O MPC é zerado.")
    
    fim_trace("Execução finalizada. Processador pronto para ler do próximo endereço.")

def jnze(binary): # if AC for != 0 pula para o endereço
    adress = binary[4:]
    inicia_trace("JNZE", binary)

    print("MPC 44: [alu := ac; if z then goto 0;]")
    print("  Datapath Interno (O Teste Lógico da Flag Z):")
    print("    1. O valor do AC viaja para a ULA.")
    print("    2. A ULA avalia o valor para testar a Flag Z (Zero).")
    print("  -> Se AC for igual a zero (Z=1): O hardware salta para MPC 0 (aborta o salto).")
    
    print("\nMPC 45: [goto 22;]")
    print("  -> O salto prossegue (AC != 0). Direcionando para o MPC de gravação do PC.")

    print("\nMPC 22: [pc := band(ir, amask); goto 0;]")
    print(f"  -> O endereço {adress} é isolado via máscara e gravado no PC. MPC zerado.")
    
    fim_trace("Execução finalizada. Processador pronto para ler do próximo endereço.")

def loco(binary): # salva uma constante no acumulador
    constante = binary[4:]
    inicia_trace("LOCO", binary)

    print("MPC 27: [ac := band(ir, amask); goto 0;]")
    print("  Datapath Interno (Isolamento da Constante via Máscara):")
    print("    1. Valor do IR (16 bits) mascarado pela ULA com AMASK (4095).")
    print("    2. Apaga os 4 bits iniciais e passa os 12 finais.") # transforma o "endereço" em constante
    print("    3. Resultado -> Barramento C -> AC (Acumulador)") # salva no acumulador
    print(f"  -> A constante ({constante}) foi extraída e salva no AC! MPC zerado (goto 0).")
    
    fim_trace()

def lodl(binary): # joga uma variável local da pilha para o ac
    offset = binary[4:]
    inicia_trace("LODL", binary)

    print("MPC 31: [a := ir + sp;]")
    print("  Datapath Interno (Cálculo do Ponteiro na ULA):")
    print("    1. Valor do IR (12 bits de offset) somado ao Stack Pointer (SP).") # offset vai pra trava B
    print("    2. Nota: Nesta arquitetura, usamos o SP como base para o cálculo de locais.")
    
    print("\nMPC 32: [mar := a; rd; goto 7;]")
    print("  Datapath: O resultado (endereço absoluto) vai para o MAR.")
    print(f"  Sinal: RD ativado. A RAM começa a buscar o dado. Salta para o fluxo do LODD.")
    
    print("\nMPC 7: [rd;]")
    print("  Datapath: Ocioso. Barramentos internos livres aguardando a resposta elétrica da memória RAM.")
    
    print("\nMPC 8: [ac := mbr; goto 0;]")
    print("  Datapath Interno: MBR -> Travas da ULA -> Barramento C -> AC (Acumulador)") # buffer -> ac
    
    fim_trace()

def stol(binary): # joga o valor em ac para a variável da pilha
    offset = binary[4:]
    inicia_trace("STOL", binary)

    print("MPC 33: [a := ir + sp;]")
    print("  Datapath Interno (Cálculo do Ponteiro na ULA usando SP):")
    print("    1. IR e SP somados na ULA para achar o endereço absoluto.") #offset vai para a trava b
    
    print("\nMPC 34: [mar := a; mbr := ac; wr; goto 10;]")
    print("  Datapath Interno: O endereço calculado vai para MAR. O AC vai para o MBR simultaneamente.") # escreve no buffer
    print("  Sinal: WR (Write) ativado. A memória RAM recebe a ordem para gravar o valor. Salto para o fluxo do STOD.")
    
    print("\nMPC 10: [wr; goto 0;]")
    print("  Sinal: WR (Write) ativado novamente.") # escreve na RAM
    print("  -> O dado do Acumulador chega ao MBR e é consolidado na RAM. MPC zerado.")
    
    fim_trace()

def addl(binary): # soma a variável local com o acumulador
    offset = binary[4:]
    inicia_trace("ADDL", binary)

    print("MPC 36: [a := ir + sp;]")
    print("  Datapath Interno (Cálculo do Ponteiro na ULA):")
    print("    1. Valor do IR (offset) somado ao Stack Pointer (SP).") # offset na trava b
    
    print("\nMPC 37: [mar := a; rd; goto 13;]")
    print("  Datapath: Endereço para o MAR. Sinal RD ativado. Salto para o fluxo do ADDD.") # escreve no buffer
    
    print("\nMPC 13: [rd;]")
    print("  Datapath: Ocioso. Aguardando dado da RAM chegar ao MBR.")
    
    print("\nMPC 14: [ac := mbr + ac; goto 0;]")
    print("  Datapath Interno (A Soma Matemática):")
    print("    1. Realiza a SOMA entre MBR e AC.") # soma e coloca no acumulador
    print("    2. Resultado -> Barramento C -> AC (Acumulador)")
    print("  -> Operação concluída e Acumulador atualizado! MPC zerado (goto 0).")
    
    fim_trace()

def subl(binary): # soma a variável local do acumulador
    offset = binary[4:]
    inicia_trace("SUBL", binary)

    print("MPC 38: [a := ir + sp;]")
    print("  Datapath Interno (Cálculo do Ponteiro na ULA usando SP):")
    print("    1. Valor do IR (offset) somado ao Stack Pointer (SP).")  # offset na trava b

    print("\nMPC 39: [mar := a; rd; goto 16;]")
    print("  Sinal: RD ativado. Pede à RAM a variável local. Salto para o fluxo do SUBD.") # jogar no buffer
    
    print("\nMPC 16: [ac := ac + 1; rd;]")
    print("  Datapath Interno: AC -> ULA (Soma +1) -> AC. Inicia o complemento de 2.")
    
    print("\nMPC 17: [a := inv(mbr);]")
    print("  Datapath Interno: Inverte os bits da variável local no MBR.")
    
    print("\nMPC 18: [ac := ac + a; goto 0;]")
    print("  Datapath Interno (A Subtração Matemática via Complemento de 2):") # subtrai e coloca no ac
    print("    1. MBR invertido somado ao AC (já incrementado).")
    print("  -> Operação concluída e Acumulador atualizado! MPC zerado (goto 0).")
    
    fim_trace()

def call(binary):
    adress = binary[4:]
    inicia_trace("CALL", binary)
    
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
    
    fim_trace("Execução finalizada. Processador pronto para executar a nova função.")

def pshi(binary): # troca endereço do topo pelo seu valor
    inicia_trace("PSHI", binary)
    print("MPC 70: [mar := sp; rd;]")
    print("  Datapath Interno: SP -> MAR")
    print("  Sinal: RD ativado. Vai ler o PONTEIRO que está no topo da pilha.")
    
    print("\nMPC 71: [rd;]")
    print("  Datapath: Ocioso. Aguardando o ponteiro chegar da RAM.")
    
    print("\nMPC 72: [mar := mbr; rd;]")
    print("  Datapath Interno: MBR (que agora tem o ponteiro) -> MAR")
    print("  Sinal: RD ativado de novo! Agora vai buscar o VALOR REAL que está nesse endereço.")
    
    print("\nMPC 73: [rd;]")
    print("  Datapath: Ocioso. Aguardando o valor real chegar da RAM.")
    
    print("\nMPC 74: [mar := sp;]")
    print("  Datapath Interno: SP -> MAR. Prepara para sobrescrever o topo da pilha.")
    
    print("\nMPC 75: [wr; goto 0;]")
    print("  Datapath: O MBR já tem o valor real. Sinal WR ativado.")
    print("  -> O ponteiro foi esmagado e substituído pelo seu valor! MPC zerado.")
    fim_trace()

def popi(binary): # escreve dado do topo para o endereço embaixo
    inicia_trace("POPI", binary)
    print("MPC 80: [mar := sp; rd;]")
    print("  Sinal: RD ativado. Lendo o DADO que está no topo da pilha.")
    
    print("\nMPC 81: [rd;]")
    print("  Datapath: Ocioso. Aguardando o DADO chegar.")
    
    print("\nMPC 82: [ac := mbr;]")
    print("  Datapath Interno: MBR -> AC. O DADO fica salvo temporariamente no Acumulador.")
    
    print("\nMPC 83: [sp := sp - 1;]")
    print("  Datapath Interno: SP -> ULA (-1) -> SP. Desce a pilha para achar o PONTEIRO.")
    
    print("\nMPC 84: [mar := sp; rd;]")
    print("  Sinal: RD ativado. Lendo o PONTEIRO de destino.")
    
    print("\nMPC 85: [rd;]")
    print("  Datapath: Ocioso. Aguardando o PONTEIRO chegar.")
    
    print("\nMPC 86: [mar := mbr;]")
    print("  Datapath Interno: MBR (ponteiro) -> MAR. Aponta para a gaveta final na RAM.")
    
    print("\nMPC 87: [mbr := ac; wr;]")
    print("  Datapath Interno: AC (dado salvo) -> MBR. Sinal WR ativado. Gravando o dado no destino!")
    
    print("\nMPC 88: [sp := sp - 1; goto 0;]")
    print("  Datapath Interno: SP -> ULA (-1) -> SP.")
    print("  -> Pop duplo concluído. Dado gravado remotamente. MPC zerado.")
    fim_trace()

def push(binary): # ac -> topo da pilha
    inicia_trace("PUSH", binary)
    print("MPC 56: [sp := sp + 1;]")
    print("  Datapath Interno: SP -> ULA (+1) -> SP (Abre espaço na pilha)")
    
    print("\nMPC 57: [mar := sp;]")
    print("  Datapath Interno: SP -> MAR (Aponta para o novo topo)")
    
    print("\nMPC 58: [mbr := ac; wr;]")
    print("  Datapath Interno: AC -> MBR. Sinal WR ativado. Grava Acumulador na RAM.")
    
    print("\nMPC 59: [goto 0;]")
    print("  -> Valor consolidado na pilha. MPC zerado (goto 0).")
    fim_trace()

def pop(binary): # topo da pilha -> ac
    inicia_trace("POP", binary)
    print("MPC 60: [mar := sp; rd;]")
    print("  Sinal: RD ativado. Pede o valor do topo da pilha à RAM.")
    
    print("\nMPC 61: [rd;]")
    print("  Datapath: Ocioso. Aguardando a resposta da RAM.")
    
    print("\nMPC 62: [ac := mbr;]")
    print("  Datapath Interno: MBR -> AC (Salva no Acumulador)")
    
    print("\nMPC 63: [sp := sp - 1; goto 0;]")
    print("  Datapath Interno: SP -> ULA (-1) -> SP (Fecha o espaço na pilha). MPC zerado.")
    fim_trace()

def retn(binary): # topo da pilha -> pc (retorna do call)
    inicia_trace("RETN", binary)
    print("MPC 64: [mar := sp; rd;]")
    print("  Sinal: RD ativado. Lendo endereço de retorno salvo pelo CALL.")
    
    print("\nMPC 65: [rd;]")
    print("  Datapath: Ocioso. Aguardando a RAM.")
    
    print("\nMPC 66: [pc := mbr;]")
    print("  Datapath Interno: MBR -> PC. (O PC volta para o ponto onde a função foi chamada)")
    
    print("\nMPC 67: [sp := sp - 1; goto 0;]")
    print("  Datapath Interno: SP -> ULA (-1) -> SP. Retorno concluído. MPC zerado.")
    fim_trace()

def swap(binary): # troca ac com topo da pilha
    inicia_trace("SWAP", binary)
    print("MPC 90: [mar := sp; rd;]")
    print("  Sinal: RD ativado. Lendo o valor do topo da pilha.")
    print("\nMPC 91: [rd;]")
    print("  Datapath: Ocioso. Aguardando a RAM.")
    print("\nMPC 92: [tir := mbr;]")
    print("  Datapath Interno: MBR -> TIR. Salva o topo da pilha temporariamente no TIR.")
    print("\nMPC 93: [mbr := ac; wr;]")
    print("  Datapath Interno: AC -> MBR. Sinal WR ativado. Grava o Acumulador na pilha.")
    print("\nMPC 94: [ac := tir; goto 0;]")
    print("  Datapath Interno: TIR -> AC. O Acumulador recebe o antigo topo da pilha. MPC zerado.")
    fim_trace()

def insp(binary): # sp = sp + constante (aloca espaço local)
    offset = binary[4:]
    inicia_trace("INSP", binary)
    print(f"  Constante extraída do binário: {offset}")
    print("MPC 68: [sp := sp + offset; goto 0;]")
    print("  Datapath Interno: SP (Barramento A) e Constante (Barramento B) -> ULA (SOMA) -> SP")
    print("  -> Stack Pointer ajustado para alocar/liberar variáveis locais. MPC zerado.")
    fim_trace()
