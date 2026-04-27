from instrucoes import *

def main():
    #16 bits each comand
    comando = str(input("Digite o comando binário: ")).strip()
    
    hm = {
        # buffer x ac
        "0000": lodd,           # 00: memória -> ac 
        "0001": stod,           # 01: ac -> memória 
        "0010": addd,           # 02: ac = ac + memória 
        "0011": subd,           # 03: ac = ac - memória
        
        # pula endereço
        "0100": jpos,           # 04: if AC for >= 0 pula para o endereço
        "0101": jzer,           # 05: if AC for == 0 pula para o endereço
        "0110": jump,           # 06: pula para o endereço
        "1100": jneg,          # 12:  if AC for < 0 pula para o endereço
        "1101": jnze,          # 13:  if AC for != 0 pula para o endereço

        "0111": loco,           # 7: salva uma constante no acumulador

        # var local pilha
        "1000": lodl,           # 08: acha endereço usando offset, valor -> memória -> ac
        "1001": stol,           # 09: acha endereço usando offset, valor -> ac -> memória
        "1010": addl,           # 10: acha endereço usando offset, valor -> memória, ac = ac + memória
        "1011": subl,           # 11: acha endereço usando offset, valor -> memória, ac = ac - memória
        "1110": call,           # 14: salva PC no topo da pilha e pula para endereço
        
        # funções 7 bits
        "1111000": pshi,        # 15: troca endereço do topo pelo seu valor
        "1111001": popi,        # 16: escreve dado do topo para o endereço embaixo
        "1111010": push,        # 17: ac -> topo da pilha
        "1111011": pop,         # 18: topo da pilha -> ac
        "1111100": retn,        # 19: topo da pilha -> pc (retorna do call)
        "1111101": swap,        # 20: troca ac com topo da pilha
        "1111110": insp,        # 21: sp = sp + constante (aloca espaço local)
    }

    print(comando[:4])

    if comando[:7] in hm:
        inst = hm[comando[:7]]
        if callable(inst):
            inst(comando)
        else:
            print(f"Instrução {inst} mapeada, mas ainda não implementada no código.")
    elif comando[:4] in hm:
        inst = hm[comando[:4]]
        if callable(inst):
            inst(comando)
        else:
            print(f"Instrução {inst} mapeada, mas ainda não implementada no código.")
    else:
        print("Não há essa instrução!")

if __name__ == "__main__":
    main()