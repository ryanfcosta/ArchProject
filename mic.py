#16 bits each comand
comando = str(input())

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
        print("  -> Salto GOTO 28 executado. Decodificação principal ignorada.")
        return

    print("\nMPC 3: [tir := lshift(ir + ir); if n then goto 19;]")
    print("  Datapath: IR -> Travas da ULA -> ULA (soma) -> Shifter (desloca 1 bit) -> Barramento C -> TIR")  # desloca bits para a esquerda e guarda no temporário
    if binary[1] == '1':
        print("  -> O 2º bit (bit 14) é 1! A ULA acende a Flag N.")
        print("  -> MPC detecta a flag e realiza o salto GOTO 19.")
        return
    else:
        print("  -> O 2º bit (bit 14) é 0. Flag N desligada. Não salta.")

    print("\nMPC 4: [tir := lshift(tir); if n then goto 11;]")
    print("  Datapath: TIR -> Travas da ULA -> ULA (passagem) -> Shifter (desloca 1 bit) -> Barramento C -> TIR") # desloca mais uma vez (lshift) 
    if binary[2] == '1':
        print("  -> O 3º bit (bit 13) é 1! A ULA acende a Flag N.")
        print("  -> MPC detecta a flag e realiza o salto GOTO 11.")
        return
    else:
        print("  -> O 3º bit (bit 13) é 0. Flag N desligada. Não salta.")

    print("\nMPC 5: [alu := tir; if n then goto 9;]")
    print("  Datapath: TIR -> Travas da ULA -> ULA (testa flags N/Z) -> (Nenhum registrador recebe no Barramento C)") # identifica instrução
    if binary[3] == '1':
        print("  -> O 4º bit (bit 12) é 1! A ULA acende a Flag N.")
        print("  -> MPC detecta a flag e realiza o salto GOTO 9.")
        return
    else:
        print("  -> O 4º bit (bit 12) é 0. Flag N desligada. Não salta.")
        print("  -> Todos os bits de teste são 0. Fim do Decode, caindo para a Execução principal.")


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

def stod(binary): #pega endereço do acumulador e joga no buffer
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


hm = {
    "0000": lodd,
    "0001": stod,
    "0010": "ADDD",
    "0011": "SUBD",
    "0100": "JPOS",
    "0101": "JZER",
    "0110": "JUMP",
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