from instrucoes import *

def main():
    #16 bits each comand
    comando = str(input("Digite o comando binário: ")).strip()
    
    hm = {
        # buffer x ac
        "0000": lodd,           # 00: buffer -> ac
        "0001": stod,           # 01: ac -> buffer
        "0010": addd,           # 02: ac = ac + buffer
        "0011": subd,           # 03: ac = ac - buffer
        
        # pula endereço
        "0100": jpos,           # 04: if AC for >= 0 pula para o endereço
        "0101": jzer,           # 05: if AC for == 0 pula para o endereço
        "0110": jump,           # 06: pula para o endereço
        "1100":"JNEG",          # 12:  if AC for < 0 pula para o endereço
        "1101":"JNZE",          # 13:  if AC for != 0 pula para o endereço

        "0111": loco,           # 7: salva uma constante no acumulador

        # var local pilha
        "1000": lodl,           # 08: acha endereço usando offset, valor -> buffer -> ac
        "1001": stol,           # 09: acha endereço usando offset, valor -> ac -> buffer
        "1010": addl,           # 10: acha endereço usando offset, valor -> buffer, ac = ac + buffer
        "1011": subl,            # 11:
        "1110": call,          # 14:
        "1111": pshi,          # 15:
        "1111001": popi,       # 16:
        "1111010":push,       # 17:
        "1111011": pop,        # 18:
        "1111100": retn,       # 19:
        "1111101": swap,       # 20:
        "1111110": insp,       # 21: 
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