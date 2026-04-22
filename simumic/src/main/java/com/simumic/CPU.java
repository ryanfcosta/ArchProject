package com.simumic;

public class CPU {
    private Memoria ram;  
    
    // Registradores
    private int pc = 0, ac = 0, ir = 0, mar = 0, mbr = 0; 
    
    // Registradores da stack 
    private int lv = 0, sp = 0;

    // Status e flag
    private String ctrlSign = "IDLE";
    private boolean flagN = false, flagZ = true;  

    // Mascara endereço
    private final int AMASK = 0xFFF;
    private int opcodeAtual = 0;
    private int enderecoAtual = 0;

    // Strings
    private String msgMPC = "---";
    private String statusCiclo = "---";


    public CPU(Memoria memoria) {
        this.ram = memoria;
    }

    public void reset() {
        this.pc = 0; this.ac = 0; this.ir = 0; this.mar = 0; this.mbr = 0;
        this.lv = 0; this.sp = 0;
        this.flagN = false; this.flagZ = true; this.ctrlSign = "IDLE";
        this.statusCiclo = "---"; this.msgMPC = "---";
        this.opcodeAtual = 0; this.enderecoAtual = 0;
    }

    public void fetch(){
        this.statusCiclo = "BUSCA";
        
        this.msgMPC = "MPC 0: [mar := pc; rd;]\n  Datapath: PC -> Travas da ULA -> ULA (passagem) -> Barramento C -> MAR";
        this.mar = this.pc;
        
        this.msgMPC += "\nMPC 1: [pc := pc + 1; rd;]\n  Datapath: PC -> Travas da ULA -> ULA (soma +1) -> Barramento C -> PC"; 
        this.pc++; 
        this.ctrlSign = "READ";

        this.msgMPC += "\nMPC 2: [ir := mbr; if n then goto 28;]\n  Datapath Externo: RAM -> MBR\n  Datapath Interno: MBR -> IR"; 
        this.mbr = ram.read(this.mar);
        this.ir = this.mbr;
    }

    public void decode(){
        this.statusCiclo = "DECODE";
        this.msgMPC = "--- SUBCICLO 2: DECODE ---\nULA e o MMUX testam os bits do opcode sequencialmente.";

        this.opcodeAtual = (this.ir >> 12) & 0xF; 
        this.enderecoAtual = this.ir & AMASK; 

        int primeiroBit = (this.ir >> 15) & 1;
        if (primeiroBit == 1) {
            this.flagN = true; 
            this.msgMPC += "\n  -> O 1º bit (bit 15) é 1! Flag N ativada. Salto GOTO 28 (Família 1xxx)";
        } else {
            this.flagN = false; 
            this.msgMPC += "\n  -> O 1º bit (bit 15) é 0. Flag N não ativada. Transição para MPC 3 (Família 0xxx)";
        }
        
        this.ctrlSign = "IDLE"; 
    }


    private void directRead(int mpc1, int mpc2) {
        this.mar = this.enderecoAtual;             
        this.ctrlSign = "READ"; 
        this.mbr = ram.read(this.mar);  
        this.msgMPC += "\nMPC " + mpc1 + ": [mar := ir; rd;]\nMPC " + mpc2 + ": [rd;]\n  Sinal: RD ativado. Buscando valor na RAM no endereço (" + this.mar + ").\n  Datapath: IR -> MAR\n  Ocioso. Aguardando dado.";
    }

    private void localRead(int mpc1, int mpc2) {
        this.mar = this.enderecoAtual + this.lv;
        this.ctrlSign = "READ";
        this.mbr = ram.read(this.mar);
        this.msgMPC += "\nMPC " + mpc1 + ": [mar := ir + lv; rd;]\nMPC " + mpc2 + ": [rd;]\n  Datapath Interno: Cálculo do Ponteiro na ULA (IR + LV = " + this.mar + ")\n  Sinal: RD ativado.";
    }

    public void memoria(){
        this.statusCiclo = "MEMÓRIA";
        this.msgMPC = "--- SUBCICLO 3: MEMÓRIA ---";

        switch (this.opcodeAtual) {
            case 0: directRead(10, 11); break; // LODD
            case 2: directRead(15, 16); break; // ADDD
            case 3: directRead(18, 19); break; // SUBD
            case 8: localRead(31, 32); break; // LODL
            case 10: localRead(41, 42); break; // ADDL
            case 11: localRead(44, 45); break; // SUBL
            case 1: // STOD 
                this.mar = this.enderecoAtual;             
                this.mbr = this.ac;             
                this.ctrlSign = "WRITE";
                ram.write(this.mar, this.mbr);  
                this.msgMPC += "\nMPC 9: [mar := ir;]\nMPC 10: [mbr := ac; wr;]\n  Sinal: WR (Write) ativado. RAM recebe ordem de gravar.";
                break;
            case 9: // STOL
                this.mar = this.enderecoAtual + this.lv;
                this.mbr = this.ac;
                this.ctrlSign = "WRITE";
                ram.write(this.mar, this.mbr);
                this.msgMPC += "\nMPC 38: [mar := ir + lv;]\nMPC 39: [mbr := ac; wr;]\n  Datapath Interno: Cálculo do Ponteiro (LV + " + this.enderecoAtual + ")\n  Sinal: WR ativado.";
                break;
            case 14: // CALL
                this.sp++; 
                this.mar = this.sp; 
                this.mbr = this.pc; 
                this.ctrlSign = "WRITE"; 
                ram.write(this.mar, this.mbr);
                this.msgMPC += "\nMPC 48: [sp := sp + 1;]\nMPC 49: [mar := sp;]\nMPC 50: [mbr := pc; wr;]\n  Stack Pointer incrementado. RAM grava endereço de retorno.";
                break;
            case 4: case 5: case 6: case 7: case 12: case 13: // Saltos e LOCO
                this.ctrlSign = "IDLE";
                this.msgMPC += "\n  [MEMÓRIA] Ocioso. Esta instrução não realiza acesso de leitura/escrita externa neste ciclo.";
                break;
            default:
                this.ctrlSign = "IDLE";
                break;
        }
    }

    public void executeULA(){
        this.msgMPC = "--- SUBCICLO 4: EXECUTE ---";

        switch (this.opcodeAtual) {
            case 0: // LODD
                this.statusCiclo = "LODD " + this.enderecoAtual;
                this.ac = this.mbr; 
                this.msgMPC += "\nMPC 12: [ac := mbr; goto 0;]\n  Datapath: MBR -> ULA -> AC\n  Operação concluída. MPC zerado.";           
                break;
            case 1: case 9: // STOD STOL
                this.statusCiclo = (this.opcodeAtual == 1 ? "STOD " : "STOL ") + this.enderecoAtual;
                this.msgMPC += "\n  [ULA] Ociosa. Operação de escrita finalizada no ciclo anterior. goto 0;";
                break;
            case 2: // ADDD
                this.statusCiclo = "ADDD " + this.enderecoAtual;
                this.ac += this.mbr;
                this.msgMPC += "\nMPC 17: [ac := ac + mbr; goto 0;]\n  Datapath: AC + MBR -> ULA (SOMA) -> AC\n  Operação concluída.";
                break;
            case 3: // SUBD
                this.statusCiclo = "SUBD " + this.enderecoAtual;
                this.ac -= this.mbr; 
                this.msgMPC += "\nMPC 20: [ac := ac - mbr; goto 0;]\n  Datapath: AC - MBR -> ULA (SUBTRAÇÃO) -> AC\n  Operação concluída.";
                break;
            case 4: // JPOS (if AC >= 0)
                this.statusCiclo = "JPOS " + this.enderecoAtual;
                this.msgMPC += "\nMPC 21: [alu := ac; if n then goto 0;]\n  Datapath: AC avaliado pela ULA.";
                if (this.ac >= 0) {
                    this.pc = this.enderecoAtual;
                    this.msgMPC += "\nMPC 22: [pc := ir; goto 0;]\n  -> Salto executado com sucesso!";
                } else {
                    this.msgMPC += "\n  -> Se AC for negativo (N=1): Salto abortado. goto 0;";
                }
                break;
            case 5: // JZER (if AC == 0)
                this.statusCiclo = "JZER " + this.enderecoAtual;
                this.msgMPC += "\nMPC 25: [alu := ac; if z then goto 27;]\n  Datapath: ULA testa Flag Z.";
                if (this.ac == 0) {
                    this.pc = this.enderecoAtual;
                    this.msgMPC += "\nMPC 27: [pc := ir; goto 0;]\n  -> Salto executado com sucesso!";
                } else {
                    this.msgMPC += "\nMPC 26: [goto 0;]\n  -> O salto falhou (AC != 0). Aborta desvio.";
                }
                break;
            case 6: // JUMP
                this.statusCiclo = "JUMP " + this.enderecoAtual;
                this.pc = this.enderecoAtual;
                this.msgMPC += "\nMPC 24: [pc := ir; goto 0;]\n  Datapath Interno: IR -> PC\n  -> Salto direto executado!";
                break;
            case 7: // LOCO
                this.statusCiclo = "LOCO " + this.enderecoAtual;
                this.ac = this.enderecoAtual; // Equivale a (ir and amask)
                this.msgMPC += "\nMPC 30: [ac := ir and amask; goto 0;]\n  Datapath: IR (16b) AND AMASK (4095) -> AC\n  -> Constante extraída e salva no AC!";
                break;
            case 8: // LODL
                this.statusCiclo = "LODL " + this.enderecoAtual;
                this.ac = this.mbr;
                this.msgMPC += "\nMPC 33: [ac := mbr; goto 0;]\n  Variável local carregada no AC.";
                break;
            case 10: // ADDL
                this.statusCiclo = "ADDL " + this.enderecoAtual;
                this.ac += this.mbr;
                this.msgMPC += "\nMPC 43: [ac := ac + mbr; goto 0;]\n  Soma de Variável Local concluída.";
                break;
            case 11: // SUBL
                this.statusCiclo = "SUBL " + this.enderecoAtual;
                this.ac -= this.mbr;
                this.msgMPC += "\nMPC 46: [ac := ac - mbr; goto 0;]\n  Subtração de Variável Local concluída.";
                break;
            case 12: // JNEG (if AC < 0)
                this.statusCiclo = "JNEG " + this.enderecoAtual;
                this.msgMPC += "\nMPC 34: [alu := ac; if n then goto 27;]";
                if (this.ac < 0) {
                    this.pc = this.enderecoAtual;
                    this.msgMPC += "\nMPC 27: [pc := ir; goto 0;]\n  -> Salto executado!";
                } else {
                    this.msgMPC += "\n  -> O salto falhou. Aborta desvio.";
                }
                break;
            case 13: // JNZE (if AC != 0)
                this.statusCiclo = "JNZE " + this.enderecoAtual;
                this.msgMPC += "\nMPC 35: [alu := ac; if z then goto 0;]";
                if (this.ac != 0) {
                    this.pc = this.enderecoAtual;
                    this.msgMPC += "\nMPC 27: [pc := ir; goto 0;]\n  -> Salto executado!";
                } else {
                    this.msgMPC += "\n  -> O salto falhou (Z=1). Aborta desvio.";
                }
                break;
            case 14: // CALL
                this.statusCiclo = "CALL " + this.enderecoAtual;
                this.pc = this.enderecoAtual;
                this.msgMPC += "\nMPC 51: [pc := ir; goto 0;]\n  Datapath Interno: IR -> PC\n  -> O PC recebe o endereço da função. Salto executado!";
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
    public int getLV() { return lv; }
    public int getSP() { return sp; }
    public boolean isFlagN() { return flagN; }
    public boolean isFlagZ() { return flagZ; }
    public String getSinalControle() { return ctrlSign; }
    public String getMsgMPC() { return msgMPC; }
    public String getstatusCiclo() { return statusCiclo; }
    public int getOpcodeAtual() { return opcodeAtual; }
}