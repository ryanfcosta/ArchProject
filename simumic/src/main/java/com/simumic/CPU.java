package com.simumic;

public class CPU {
    private Memoria ram;  
    
    // Registradores
    private int pc = 0, ac = 0, ir = 0, mar = 0, mbr = 0, mpc = 0; 
    
    // Registradores da stack 
    private int lv = 0, sp = 0;

    // Status e flag
    private String ctrlSign = "IDLE";
    private boolean flagN = false, flagZ = true;  

    // Mascara endereço
    private final int AMASK = 0xFFF;
    private int opcodeAtual = 0;
    private int subOpcode = 0;
    private int enderecoAtual = 0;

    // Strings
    private String msgMPC = "";
    private String statusCiclo = "";

    private final String[] mcpStrings = {
        "mar:=pc; rd;",
        "pc:=pc + 1; rd;",
        "ir:=mbr; if n then goto 28;",
        "tir:=lshift(ir + ir); if n then goto 19;",
        "tir:=lshift(tir); if n then goto 11;",
        "alu:=tir; if n then goto 9;",
        "mar:=ir; rd;",
        "rd;",
        "ac:=mbr; goto 0;",
        "mar:=ir; mbr:=ac; wr;", 
        "wr; goto 0;",
        "alu:=tir; if n then goto 15;",
        "mar:=ir; rd;",
        "rd;",
        "ac:=mbr + ac; goto 0;",
        "mar:=ir; rd;",
        "ac:=ac + 1; rd;",
        "a:=inv(mbr);",
        "ac:=ac + a; goto 0;",
        "tir:=lshift(tir); if n then goto 25;",
        "alu:=tir; if n then goto 23;",
        "alu:=ac; if n then goto 0;", 
        "pc:=band(ir,amask); goto 0;",
        "alu:=ac; if z then goto 22;",
        "goto 0;",
        "alu:=tir; if n then goto 27;",
        "pc:=band(ir,amask); goto 0;",
        "ac:=band(ir,amask); goto 0; ",
        "tir:=lshift(ir + ir); if n then goto 40;",
        "tir:=lshift(tir); if n then goto 35;",
        "alu:=tir; if n then goto 33;",
        "a:=ir + sp; {1000 = LODL}",
        "mar:=a; rd; goto 7;",
        "a:=ir + sp; {1001 = STOL}",
        "mar:=a; mbr:=ac; wr; goto 10;",
        "alu:=tir; if n then goto 38;",
        "a:=ir + sp;",
        "mar:=a; rd; goto 13;",
        "a:=ir + sp;",
        "mar:=a; rd; goto 16 ;",
        "tir:=lshift(tir); if n then goto 46;",
        "alu:=tir; if n then goto 44;",
        "alu:=ac; if n then goto 22; {1100 = JNEG}",
        "goto 0;",
        "alu:=ac; if z then goto 0; {1101 = JNZE}",
        "pc:=band(ir,amask); goto 0;",
        "tir:=lshift(tir); if n then goto",
        "sp:=sp + (-1);",
        "mar:=sp; mbr:=pc; wr;",
        "pc:=band(ir,amask); wr; goto 0;",
        "tir:=lshift(tir); if n then goto 65;",
        "tir:=lshift(tir); if n then goto 59;",
        "alu:=tir; if n then goto 56;",
        "mar:=ac; rd;",
        "sp:=sp + (-1); rd;",
        "mar:=sp; wr; goto 10;",
        "mar:=sp; sp:=sp + 1; rd;",
        "rd;",
        "mar:=ac; wr; goto 10;",
        "alu:=tir; if n then goto 62;",
        "sp:=sp + (-1);",
        "mar:=sp; mbr:=ac; wr; goto 10;",
        "mar:=sp; sp:=sp + 1; rd;",
        "rd;",
        "ac:=mbr; goto 0;",
        "tir:=lshift(tir); if n then goto 73;",
        "alu:=tir; if n then goto 70;",
        "mar:=sp; sp:=sp + 1; rd; ",
        "rd;",
        "pc:=mbr; goto 0;",
        "a:=ac;",
        "ac:=sp;",
        "sp:=a; goto 0;",
        "alu:=tir; if n then goto 76;",
        "a:=band(ir,smask);",
        "sp:=sp + a; goto 0;",
        "a:=band(ir, smask);",
        "a:=inv(a);",
        "a:=a + 1; goto 75;"
    };


    public CPU(Memoria memoria) {
        this.ram = memoria;
    }

    public void attMPC(int n){
        this.mpc = n;
        this.msgMPC += "\nMPC " + n + ": [" + mcpStrings[n] + "]";
    }

    public void reset() {
        this.pc = 0; this.ac = 0; this.ir = 0; this.mar = 0; this.mbr = 0;
        this.lv = 0; this.sp = 0;
        this.flagN = false; this.flagZ = true; this.ctrlSign = "IDLE";
        this.statusCiclo = "---"; this.msgMPC = "---";
        this.opcodeAtual = 0; this.subOpcode = 0; this.enderecoAtual = 0;
    }

    public void fetch(){
        //MPC 0,1,2
        
        
        this.statusCiclo = "BUSCA";
        attMPC(0);
        // Datapath: PC -> Travas da ULA -> ULA (passagem) -> Barramento C -> MAR";
        this.mar = this.pc;
        
        attMPC(1);
        // Datapath: PC -> Travas da ULA -> ULA (soma +1) -> Barramento C -> PC"; 
        this.pc++; 
        this.ctrlSign = "READ";

        attMPC(2);
        // Datapath Externo: RAM -> MBR\n  Datapath Interno: MBR -> IR"; 
        this.mbr = ram.read(this.mar);
        this.ir = this.mbr;
    }

    public void decode(){
        this.statusCiclo = "DECODE";

        this.opcodeAtual = (this.ir >> 12) & 0xF; 
        this.subOpcode = (this.ir >> 9) & 0x7;
        this.enderecoAtual = this.ir & AMASK; 

        int primeiroBit = (this.ir >> 15) & 1;
        if (primeiroBit == 1) {
            this.flagN = true; 
            //O 1º bit (bit 15) é 1! Flag N ativada. Salto GOTO 28 (Família 1xxx)";
        } else {
            this.flagN = false; 
            //O 1º bit (bit 15) é 0. Flag N não ativada. Transição para MPC 3 (Família 0xxx)";
        }
        
        this.ctrlSign = "IDLE"; 
    }


    private void directRead(int mpc1, int mpc2) {
        this.mar = this.enderecoAtual;             
        this.ctrlSign = "READ"; 
        this.mbr = ram.read(this.mar);  
        attMPC(mpc1);
        attMPC(mpc2);
        //Sinal: RD ativado. Buscando valor na RAM no endereço (" + this.mar + ").\n  Datapath: IR -> MAR\n  Ocioso. Aguardando dado.";
    }

    private void localRead(int mpc1, int mpc2) {
        this.mar = this.enderecoAtual + this.lv;
        this.ctrlSign = "READ";
        this.mbr = ram.read(this.mar);
        attMPC(mpc1);
        attMPC(mpc2);
        //Datapath Interno: Cálculo do Ponteiro na ULA (IR + LV = " + this.mar + ")\n  Sinal: RD ativado.";
    }

    public void memoria(){
        this.statusCiclo = "MEMÓRIA";

        switch (this.opcodeAtual) {
            case 0: directRead(10, 11); break; // LOD D
            case 2: directRead(15, 16); break; // ADD D
            case 3: directRead(18, 19); break; // SUB D
            case 8: localRead(31, 32); break; // LOD L
            case 10: localRead(41, 42); break; // ADD L
            case 11: localRead(44, 45); break; // SUB L
            case 1: // STO D diferente pois é write
                this.mar = this.enderecoAtual;             
                this.mbr = this.ac;             
                this.ctrlSign = "WRITE"; 
                ram.write(this.mar, this.mbr);  
                
                attMPC(9); attMPC(10);
                // Sinal: WR (Write) ativado. RAM recebe ordem de gravar.";
                break;
            case 9: // STO L
                this.mar = this.enderecoAtual + this.lv;
                this.mbr = this.ac;
                this.ctrlSign = "WRITE";
                ram.write(this.mar, this.mbr);
                attMPC(38); attMPC(39);
                // Datapath Interno: Cálculo do Ponteiro (LV + " + this.enderecoAtual + ")\n  Sinal: WR ativado.";
                break;
            case 14: // CALL
                this.sp++; 
                this.mar = this.sp; 
                this.mbr = this.pc; 
                this.ctrlSign = "WRITE"; 
                ram.write(this.mar, this.mbr);

                attMPC(48); attMPC(49); attMPC(50);

                // Stack Pointer incrementado. RAM grava endereço de retorno.";
                break;
            case 15: // Opcodes maiores
                switch (this.subOpcode) {
                    case 2: // PUSH 
                        this.sp++; this.mar = this.sp; this.mbr = this.ac;
                        this.ctrlSign = "WRITE"; ram.write(this.mar, this.mbr);
                        this.msgMPC += "\n[sp := sp + 1;]\n[mar := sp;]\n[mbr := ac; wr;]\n  Sinal: WR ativado. Guardando AC na pilha.";
                        break;
                    case 3: case 4: // POP RETN
                        this.mar = this.sp; this.ctrlSign = "READ";
                        this.mbr = ram.read(this.mar);
                        this.msgMPC += "\n[mar := sp; rd;]\n  Sinal: RD ativado. Lendo do topo da pilha.";
                        break;
                    default:
                        this.ctrlSign = "IDLE";
                        this.msgMPC += "\n[MEMÓRIA] Ocioso. Instrução estendida sem acesso à RAM neste ciclo.";
                        break;
                }
                break;
            case 4: case 5: case 6: case 7: case 12: case 13: // Saltos e LOCO
                this.ctrlSign = "IDLE";
                // [MEMÓRIA] Ocioso. Esta instrução não realiza acesso de leitura/escrita externa neste ciclo.";
                break;
            default:
                this.ctrlSign = "IDLE";
                break;
        }
    }

    public void executeULA(){

        switch (this.opcodeAtual) {
            case 0: // LODD
                this.statusCiclo = "LODD " + this.enderecoAtual;
                this.ac = this.mbr; 
                attMPC(12);
                //Datapath: MBR -> ULA -> AC\n  Operação concluída. MPC zerado.";           
                break;
            case 1: case 9: // STOD STOL
                this.statusCiclo = (this.opcodeAtual == 1 ? "STOD " : "STOL ") + this.enderecoAtual;
                //[ULA] Ociosa. Operação de escrita finalizada no ciclo anterior. goto 0;";
                break;
            case 2: // ADDD
                this.statusCiclo = "ADDD " + this.enderecoAtual;
                this.ac += this.mbr;
                
                attMPC(17);
                //Datapath: AC + MBR -> ULA (SOMA) -> AC\n  Operação concluída.";
                break;
            case 3: // SUBD
                this.statusCiclo = "SUBD " + this.enderecoAtual;
                this.ac -= this.mbr;
                
                attMPC(20);
                //Datapath: AC - MBR -> ULA (SUBTRAÇÃO) -> AC\n  Operação concluída.";
                break;
            case 4: // JPOS (if AC >= 0)
                this.statusCiclo = "JPOS " + this.enderecoAtual;
                attMPC(21);
                //Datapath: AC avaliado pela ULA.";
                if (this.ac >= 0) {
                    this.pc = this.enderecoAtual;
                    attMPC(22);
                    // -> Salto executado com sucesso!";
                } else {
                    // -> Se AC for negativo (N=1): Salto abortado. goto 0;";
                }
                break;
            case 5: // JZER (if AC == 0)
                this.statusCiclo = "JZER " + this.enderecoAtual;
                attMPC(25);
                // Datapath: ULA testa Flag Z.";
                if (this.ac == 0) {
                    this.pc = this.enderecoAtual;
                    
                    attMPC(27); // -> Salto executado com sucesso!";
                } else {
                    attMPC(26); // -> O salto falhou (AC != 0). Aborta desvio.";
                }
                break;
            case 6: // JUMP
                this.statusCiclo = "JUMP " + this.enderecoAtual;
                this.pc = this.enderecoAtual;
                
                attMPC(24); // Datapath Interno: IR -> PC\n  -> Salto direto executado!";
                break;
            case 7: // LOCO
                this.statusCiclo = "LOCO " + this.enderecoAtual;
                this.ac = this.enderecoAtual; // Equivale a (ir and amask)

                attMPC(30); // Datapath: IR (16b) AND AMASK (4095) -> AC\n  -> Constante extraída e salva no AC!";
                break;
            case 8: // LODL
                this.statusCiclo = "LODL " + this.enderecoAtual;
                this.ac = this.mbr;
                attMPC(33);
                break;
            case 10: // ADDL
                this.statusCiclo = "ADDL " + this.enderecoAtual;
                this.ac += this.mbr;
                attMPC(43);                 
                break;
            case 11: // SUBL
                this.statusCiclo = "SUBL " + this.enderecoAtual;
                this.ac -= this.mbr;
                attMPC(46);
                break;
            case 12: // JNEG (if AC < 0)
                this.statusCiclo = "JNEG " + this.enderecoAtual;
                attMPC(34);
                if (this.ac < 0) {
                    this.pc = this.enderecoAtual;
                    attMPC(27);
                    // -> Salto executado!";
                } else {
                    // -> O salto falhou. Aborta desvio.";
                }
                break;
            case 13: // JNZE (if AC != 0)
                this.statusCiclo = "JNZE " + this.enderecoAtual;
                attMPC(35);
                if (this.ac != 0) {
                    this.pc = this.enderecoAtual;
                    attMPC(27); // -> Salto executado!";
                } else {
                    // -> O salto falhou (Z=1). Aborta desvio.";
                }
                break;
            case 14: // CALL
                this.statusCiclo = "CALL " + this.enderecoAtual;
                this.pc = this.enderecoAtual;
                attMPC(51);
                // Datapath Interno: IR -> PC\n  -> O PC recebe o endereço da função. Salto executado!";
                break;
            case 15: 
                switch (this.subOpcode) {
                    case 0: // PSHI
                        this.statusCiclo = "PSHI";
                        this.msgMPC += "\n[ULA] Ociosa.";
                        break;
                    case 1: // POPI
                        this.statusCiclo = "POPI";
                        this.msgMPC += "\n[ULA] Ociosa.";
                        break;
                    case 2: // PUSH
                        this.statusCiclo = "PUSH";
                        this.msgMPC += "\n[ULA] Ociosa. Valor consolidado na pilha. goto 0;";
                        break;
                    case 3: // POP
                        this.statusCiclo = "POP";
                        this.ac = this.mbr; this.sp--;
                        this.msgMPC += "\n[ac := mbr;]\n[sp := sp - 1; goto 0;]\n  Valor retirado da pilha para o AC.";
                        break;
                    case 4: // RETN
                        this.statusCiclo = "RETN";
                        this.pc = this.mbr; this.sp--;
                        this.msgMPC += "\n[pc := mbr;]\n[sp := sp - 1; goto 0;]\n  Retorno de sub-rotina concluído.";
                        break;
                    case 5: // SWAP
                        this.statusCiclo = "SWAP";
                        this.msgMPC += "\n[ULA] SWAP aguardando implementação de hardware.";
                        break;
                    case 6: // INSP
                        this.statusCiclo = "INSP " + this.enderecoAtual;
                        this.sp += this.enderecoAtual;
                        this.msgMPC += "\n[sp := sp + offset; goto 0;]\n  Stack Pointer ajustado.";
                        break;
                    default:
                        this.statusCiclo = "UNKNOWN 1111";
                        break;
                }
                break;
            default:
                this.statusCiclo = "UNKNOWN";
                break;
        }

        this.flagN = (this.ac < 0);
        this.flagZ = (this.ac == 0); 
        this.ctrlSign = "IDLE"; 
    }

    public int getPC() { return pc; }
    public int getAC() { return ac; }
    public int getIR() { return ir; }
    public int getMAR() { return mar; }
    public int getMBR() { return mbr; }
    public int getMPC() { return mpc; }
    public int getLV() { return lv; }
    public int getSP() { return sp; }
    public boolean isFlagN() { return flagN; }
    public boolean isFlagZ() { return flagZ; }
    public String getSinalControle() { return ctrlSign; }
    public String getMsgMPC() { return msgMPC; }
    public String getstatusCiclo() { return statusCiclo; }
    public int getOpcodeAtual() { return opcodeAtual; }
}