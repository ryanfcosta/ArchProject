#16 bits each comand
comando = str(input()).strip()

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
    print("\n--- DECODE ---") # IDENTIFICA INSTRUÇÃO (joga bit pra esquerda até encontrar 1)
    
    # isso acontece no final de MPC 2
    if binary[0] == '1':
        print("  -> O 1º bit (bit 15) é 1! Flag N ativada no MPC 2.")
        print("  -> Salto GOTO 28 executado. Iniciando decodificação de variáveis locais (1xxx).")
        return 

    # se bit 15 for 0, cai para MPC 3
    print("\nMPC 3: [tir := lshift(ir + ir); if n then goto 19;]")
    print("  Datapath: IR -> Travas da ULA -> ULA (soma) -> Shifter (desloca 1 bit) -> Barramento C -> TIR")  # desloca bits para a esquerda e guarda no temporário
    if binary[1] == '1':
        print("  -> O 2º bit (bit 14) é 1! A ULA acende a Flag N.")
        print("  -> MPC detecta a flag e realiza o salto GOTO 19 (Família 01xx - Saltos e Constantes).")
        
        # --- 01xx ---
        print("\nMPC 19: [tir := lshift(tir); if n then goto 23;]")
        if binary[2] == '1':
            print("  -> O 3º bit (bit 13) é 1! A ULA acende a Flag N.")
            print("  -> MPC realiza o salto GOTO 23 (Família 011x - JUMP e LOCO).")
            
            print("\nMPC 23: [alu := tir; if n then goto 30;]")
            if binary[3] == '1':
                print("  -> O 4º bit (bit 12) é 1! A ULA acende a Flag N.")
                print("  -> MPC realiza o salto GOTO 30. Fim do Decode. (Instrução LOCO)")
                return
            else:
                print("  -> O 4º bit (bit 12) é 0. Flag N desligada. Não salta.")
                print("  -> Fim do Decode, caindo para MPC 24. (Instrução JUMP)")
                return
        else:
            print("  -> O 3º bit (bit 13) é 0. Flag N desligada. Não salta. Cai para MPC 20.")
            
            print("\nMPC 20: [alu := tir; if n then goto 25;]")
            if binary[3] == '1':
                print("  -> O 4º bit (bit 12) é 1! A ULA acende a Flag N.")
                print("  -> MPC realiza o salto GOTO 25. Fim do Decode. (Instrução JZER)")
                return
            else:
                print("  -> O 4º bit (bit 12) é 0. Flag N desligada. Não salta.")
                print("  -> Fim do Decode, caindo para MPC 21. (Instrução JPOS)")
                return
    else:
        print("  -> O 2º bit (bit 14) é 0. Flag N desligada. Não salta. Cai para MPC 4 (Família 00xx).")
        
        # --- 00xx ---
        print("\nMPC 4: [tir := lshift(tir); if n then goto 11;]")
        print("  Datapath: TIR -> Travas da ULA -> ULA (passagem) -> Shifter (desloca 1 bit) -> Barramento C -> TIR") # desloca mais uma vez (lshift) 
        if binary[2] == '1':
            print("  -> O 3º bit (bit 13) é 1! A ULA acende a Flag N.")
            print("  -> MPC realiza o salto GOTO 11 (Família 001x - ADDD e SUBD).")
            
            print("\nMPC 11: [alu := tir; if n then goto 15;]")
            if binary[3] == '1':
                print("  -> O 4º bit (bit 12) é 1! A ULA acende a Flag N.")
                print("  -> MPC realiza o salto GOTO 15. Fim do Decode. (Instrução SUBD)")
                return
            else:
                print("  -> O 4º bit (bit 12) é 0. Flag N desligada. Não salta.")
                print("  -> Fim do Decode, caindo para MPC 12. (Instrução ADDD)")
                return
        else:
            print("  -> O 3º bit (bit 13) é 0. Flag N desligada. Não salta. Cai para MPC 5.")
            
            print("\nMPC 5: [alu := tir; if n then goto 9;]")
            print("  Datapath: TIR -> Travas da ULA -> ULA (testa flags N/Z) -> (Nenhum registrador recebe no Barramento C)") # identifica instrução
            if binary[3] == '1':
                print("  -> O 4º bit (bit 12) é 1! A ULA acende a Flag N.")
                print("  -> MPC detecta a flag e realiza o salto GOTO 9. Fim do Decode. (Instrução STOD)")
                return
            else:
                print("  -> O 4º bit (bit 12) é 0. Flag N desligada. Não salta.")
                print("  -> Todos os bits de teste são 0. Fim do Decode, caindo para MPC 6. (Instrução LODD)")
                return


def lodd(binary): # pega o endereço, vai até a RAM externa, busca a variável e tira do buffer e salva no AC (Acumulador)
    adress = binary[4:]
    
    print(f"\n[LODD] Rastreando o Caminho de Dados (Datapath) para: {binary}")
    print("="*85)

    fetch()
    decode_din(binary)    

    print("\n--- EXECUTE")
    print(f"MPC 6: [mar := ir; rd;]")
    print("  Datapath: IR -> Travas da ULA -> ULA (passagem livre) -> Barramento C -> MAR") # joga até a RAM externa //igual MPC0 mas vem do IR
    print(f"  Sinal: RD ativado. RAM externa recebe o endereço da variável ({adress}).") # lê da RAM
    
    print("\nMPC 7: [rd;]")
    print("  Datapath: Ocioso. Barramentos internos livres aguardando a resposta elétrica da memória RAM.")
    
    print("\nMPC 8: [ac := mbr; goto 0;]")
    print("  Datapath Externo: RAM -> Barramento de Dados Externo -> MBR") # joga pro buffer
    print("  Datapath Interno: MBR -> Travas da ULA -> ULA (passagem livre) -> Barramento C -> AC (Acumulador)") # grava no acumulador
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
    print("  Datapath: IR -> Travas da ULA -> ULA (passagem livre) -> Barramento C -> MAR") # isola do ir e joga no MAR
    print(f"  -> O endereço de destino ({adress}) é isolado e enviado para o MAR.")
    
    print("\nMPC 10: [mbr := ac; wr; goto 0;]")
    print("  Datapath: AC -> Travas da ULA -> ULA (passagem livre) -> Barramento C -> MBR") # pega do AC e joga no buffer
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

def subd(binary):   #joga endereço no buffer e subtrai ele do que tem no acumulador
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

def jpos(binary): #se o dado AC for >= 0 ele pula para o endereço
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
    print("    3. O Barramento C não recebe ordem de escrita para nenhum registo.")
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
    print("    3. O Barramento C não recebe ordem de escrita para nenhum registo.")
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


hm = {
    "0000": lodd,
    "0001": stod,
    "0010": addd,
    "0011": subd,
    "0100": jpos,
    "0101": jzer,   
    "0110": jump,
    "0111":"LOCO",
    "1000":"LODL",
    "1001":"STOL",
    "1010":"ADDL",
    "1011":"SUBL",
    "1100":"JNEG",
    "1101":"JNZE",
    "1110":"CALL",
    "1111":"PSHI",
    "1111001":"POPI",
    "1111010":"PUSH",  
    "1111011":"POP",
    "1111100":"RETN",
    "1111101":"SWAP",
    "1111110":"INSP",
    "1111111":"DESP"
}

"""
REGISTERS

possível ler e escrever o mesmo registrador em um único ciclo, ou seja, pode fazer A = A AND B ou A = A + B

MAR:    REGISTRADOR DE ENDEREÇO DE MEMÓRIA (BARR 32 BITS)
↑
MDR:    REGISTRADOR DE DADOS DE MEMÓRIA (BARR 32 BITS)
↑
PC:     CONTADOR DE PROGRAMA (BARR 8 BITS)
↑
MBR:    REGISTRADOR DE DADOS DE MEMÓRIA (BARR 8 BITS) | BUFFER REGISTER

SP:     PONTEIRO DE STACK
↑
LV:     POINTEIRO PARA BASE DE VARIÁVEIS LOCAIS (NA PILHA)
↑
CPP:    APONTA PARA POOL DE CONSTANTES E POINTEIRO PARA OUTRAS ÁREAS DA MEMÓRIA
↑
TOS:    REGISTRADOR TEMPORÁRIO
↑
OPC:    REGISTRADOR TEMPORÁRIO

H:      ACUMULADOR

OUTROS COMPONENTES 

ULA:                SOMA, SUBTRAI E RESOLVE OPERAÇÕES BOOLEANAS
A-LATCH/B-LATCH:    TRAVAS DA ULA QUE ARMAZENAM OS DADOS PARA A ULA (REGISTRADORES)
RD:                 PINO DE LEITURA EXTERNA
IR:                 REGISTRADOR DE INSTRUÇÃO (GUARDA A INSTRUÇÃO ATUAL DE 16 BITS)
TIR:                REGISTRADOR DE INSTRUÇÃO TEMPORÁRIO (USADO NO DECODE)
"""



print(comando[:4])

if comando[:7] in hm:
    inst = hm[comando[:7]]
    inst(comando)
elif comando[:4] in hm:
    inst = hm[comando[:4]]
    inst(comando)
else:
    print("Não há essa instrução!")


"""
FUNCIONAMENTO POR INSTRUÇÃO:
FETCH -> DECODE -> EXEC

FETCH (3 CICLOS): GRAVA NA MEMÓRIA O COMANDO
DECODE (4 CICLOS): IDENTIFICA  A INSTRUÇÃO A PARTIR DO COMANDO GRAVADO // divide um ciclo com tech
EXEC: ENCAMINHADO A PARTIR DO DECODE (DIFERE A PARTIR DA INSTRUÇÃO)


PADRÕES POR DATAPATH
PARA ACESSAR DADO NO BUFFER, DEVE TER PASSADO ANTES NO MAR
"""