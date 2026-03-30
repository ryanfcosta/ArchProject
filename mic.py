#16 bits each comand
comando = str(input())

def lodd(binary):
    adress = binary[4:]
    print(f"Sending Adress {adress} from IR -> ALU -> CABLE C -> MAR ")
    print("Signal read")
    print("RAM receives adress")
    print("Searching DATA")
    print("Sending to processor (MBR)")
    print("Sending from MBR -> ALU ->  CABLE C -> AC")

    
hm = {
    "0000": lodd,
    "0001": "STOD",
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


print(comando[:4])

if comando[:7] in hm:
    inst = hm[comando[:7]]
    inst(comando)
elif comando[:4] in hm:
    inst = hm[comando[:4]]
    inst(comando)
else:
    print("Not comando")

