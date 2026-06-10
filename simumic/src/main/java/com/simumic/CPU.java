package com.simumic;

import java.util.HashMap;
import java.util.Map;

/**
CAMPOS DO MIR
    AMUX(1) | COND(2) | ULA(2) | SH(2) | MBR(1) | MAR(1) | RD(1) | WR(1)
    ENC(1)  | C(4)    | B(4)   | A(4)  | ADDR(8)
 */

public class CPU {
    private final Memoria ram;

    // Indexs regs
    private final int[] regs = new int[16];

    private static final int 
     PC = 0, AC = 1, SP = 2, IR = 3, TIR = 4, ZERO = 5, P1 /*+1*/ = 6, M1 /*+1*/ = 7, AMASK = 8, SMASK = 9,
     A = 10, B = 11, C = 12, D = 13, E = 14, F = 15;

    private int mar, mbr;
    private boolean flagN, flagZ;
    private int mpc; 
    private int busA, busB, busC;


    // Sinais de controle do MIR
    private int amux_ctrl;   // 0=LatchA, 1=MBR
    private int cond_ctrl;   // 0=nenhum, 1=ifN, 2=ifZ, 3=goto
    private int alu_ctrl;    // 0=A+B, 1=A&B, 2=A, 3=~A
    private int sh_ctrl;     // 0=nenhum, 1=lshift, 2=rshift
    private int mbr_ctrl, mar_ctrl, rd_ctrl, wr_ctrl;
    private int enc_ctrl;    // 1=habilita escrita no banco de registradores via C
    
    private int a_reg, b_reg, c_reg, addr_field;

    // Mostrar na interface
    private int subcicloAtual, opcodeAtual;
    private String statusCiclo, msgMPC, sinalControle;
    private final String[] mpcStrings;

    // Analise ULA
    private int latchA, latchB;
    private int aluResult;        // resultado bruto
    private int shifterResult;    // resultado após o shifter (vai para busC)
    private boolean flagN_latched, flagZ_latched; // travadas até o sub4

    private long totalSubciclos = 0;
    private long totalCiclos = 0;
    private long instrucoesExecutadas = 0;

    
    private boolean rdPending, wrPending; // booleano em vez de salvar a prev em uma variável

    private static final int[][] CONTROL_STORE = buildControlStore();


    /*
    COND: 0=nenhum  1=ifN→addr  2=ifZ→addr  3=goto addr
    ULA:  0=A+B     1=A AND B   2=A         3=NOT A
    SH:   0=nenhum  1=lshift    2=rshift
    */
private static int[][] buildControlStore() {
        int[][] cs = new int[256][13];// amux cond alu sh mbr mar rd wr enc C   B   A   addr
        
        // Fetch / Decode
        cs[ 0] = mi(0, 0, 2, 0,  0, 1, 1, 0, 0,  0,  PC,  0,   1);  // mar:=pc; rd; (MAR no B bus)
        cs[ 1] = mi(0, 0, 0, 0,  0, 0, 1, 0, 1, PC,  P1, PC,   2);  // pc:=pc+1; rd;
        cs[ 2] = mi(1, 1, 2, 0,  0, 0, 0, 0, 1, IR,   0,  0,  28);  // ir:=mbr; if n goto 28
        cs[ 3] = mi(0, 1, 0, 0,  0, 0, 0, 0, 1,TIR,  IR, IR,  20);  // tir:=ir+ir; if n goto 20
        cs[ 4] = mi(0, 1, 0, 0,  0, 0, 0, 0, 1,TIR, TIR,TIR,  11);  // tir:=tir+tir; if n goto 11
        cs[ 5] = mi(0, 1, 2, 0,  0, 0, 0, 0, 0,  0, TIR,TIR,   9);  // alu:=tir; if n goto 9
        
        // LODD: mar:=ir; rd → ac:=mbr; goto 0
        cs[ 6] = mi(0, 0, 2, 0,  0, 1, 1, 0, 0,  0,  IR,  0,   7);  // mar:=ir; rd;
        cs[ 7] = mi(0, 0, 2, 0,  0, 0, 1, 0, 0,  0,   0,  0,   8);  // rd;
        cs[ 8] = mi(1, 3, 2, 0,  0, 0, 0, 0, 1, AC,   0,  0,   0);  // ac:=mbr; goto 0;
        
        // STOD: mar:=ir; mbr:=ac; wr → wr; goto 0
        cs[ 9] = mi(0, 0, 2, 0,  1, 1, 0, 1, 0,  0,  IR, AC,  10);  // mar:=ir (B bus); mbr:=ac (C bus via A); wr;
        cs[10] = mi(0, 3, 0, 0,  0, 0, 0, 1, 0,  0,   0,  0,   0);  // wr; goto 0;
        
        // Decode bit 13
        cs[11] = mi(0, 1, 0, 0,  0, 0, 0, 0, 1,TIR, TIR,TIR,  15);  // tir:=tir+tir; if n goto 15
        
        // ADDD: mar:=ir; rd → ac:=mbr+ac; goto 0
        cs[12] = mi(0, 0, 2, 0,  0, 1, 1, 0, 0,  0,  IR,  0,  13);  // mar:=ir; rd;
        cs[13] = mi(0, 0, 2, 0,  0, 0, 1, 0, 0,  0,   0,  0,  14);  // rd;
        cs[14] = mi(1, 3, 0, 0,  0, 0, 0, 0, 1, AC,  AC,  0,   0);  // ac:=mbr+ac; goto 0;
        
        // SUBD: mar:=ir; rd → a:=inv(mbr) → ac:=ac+a; goto 0
        cs[15] = mi(0, 1, 0, 0,  0, 0, 0, 0, 1,TIR, TIR,TIR,  25);  // tir:=tir+tir; if n goto 25
        cs[16] = mi(0, 0, 2, 0,  0, 1, 1, 0, 0,  0,  IR,  0,  17);  // mar:=ir; rd;
        cs[17] = mi(0, 0, 2, 0,  0, 0, 1, 0, 0,  0,   0,  0,  18);  // rd;
        cs[18] = mi(1, 0, 3, 0,  0, 0, 0, 0, 1,  A,   0,  0,  19);  // a:=inv(mbr);
        cs[19] = mi(0, 3, 0, 0,  0, 0, 0, 0, 1, AC,  AC,  A,   0);  // ac:=ac+a; goto 0;

        // Decode JUMPS
        cs[20] = mi(0, 1, 0, 0,  0, 0, 0, 0, 1,TIR, TIR,TIR,  26);  // tir:=tir+tir; if n then goto 26
        cs[21] = mi(0, 1, 0, 0,  0, 0, 0, 0, 1,TIR, TIR,TIR,  24);  // tir:=tir+tir; if n then goto 24
        cs[22] = mi(0, 1, 2, 0,  0, 0, 0, 0, 0,  0,  AC, AC,   0);  // alu:=ac; if n then goto 0
        cs[23] = mi(0, 3, 1, 0,  0, 0, 0, 0, 1, PC,AMASK,IR,   0);  // pc:=band(ir,amask); goto 0
        cs[24] = mi(0, 2, 2, 0,  0, 0, 0, 0, 0,  0,  AC, AC,  23);  // alu:=ac; if z then goto 23
        cs[25] = mi(0, 3, 0, 0,  0, 0, 0, 0, 0,  0,   0,  0,   0);  // goto 0
        cs[26] = mi(0, 1, 0, 0,  0, 0, 0, 0, 1,TIR, TIR,TIR,  77);  // tir:=tir+tir; if n then goto 77
        cs[27] = mi(0, 3, 1, 0,  0, 0, 0, 0, 1, PC,AMASK,IR,   0);  // pc:=band(ir,amask); goto 0
        
        // Decode bit 15
        cs[28] = mi(0, 1, 0, 0,  0, 0, 0, 0, 1,TIR,  IR, IR,  40);  // tir:=ir+ir; if n goto 40
        cs[29] = mi(0, 1, 0, 0,  0, 0, 0, 0, 1,TIR, TIR,TIR,  35);  // tir:=tir+tir; if n goto 35
        cs[30] = mi(0, 1, 2, 0,  0, 0, 0, 0, 0,  0, TIR,TIR,  33);  // alu:=tir; if n goto 33
        
        // LODL: a:=ir+sp; mar:=a; rd → ac:=mbr; goto 0
        cs[31] = mi(0, 0, 0, 0,  0, 0, 0, 0, 1,  A,  SP, IR,  32);  // a:=ir+sp;
        cs[32] = mi(0, 0, 2, 0,  0, 1, 1, 0, 0,  0,   A,  0,   7);  // mar:=a; rd; goto 7 
        
        // STOL: a:=ir+sp; mar:=a; mbr:=ac; wr
        cs[33] = mi(0, 1, 2, 0,  0, 0, 0, 0, 0,  0, TIR,TIR,  38);  // alu:=tir; if n goto 38
        cs[34] = mi(0, 0, 0, 0,  0, 0, 0, 0, 1,  A,  SP, IR,  35);  // a:=ir+sp;
        cs[35] = mi(0, 0, 2, 0,  1, 1, 0, 1, 0,  0,   A, AC,  10);  // mar:=a (B); mbr:=ac (C via A); wr; goto 10
        
        // ADDL / SUBL
        cs[36] = mi(0, 1, 0, 0,  0, 0, 0, 0, 1,TIR, TIR,TIR,  38);  // tir:=tir+tir; if n goto 38
        cs[37] = mi(0, 0, 0, 0,  0, 0, 0, 0, 1,  A,  SP, IR,  38);  // a:=ir+sp;
        cs[38] = mi(0, 0, 2, 0,  0, 1, 1, 0, 0,  0,   A,  0,  13);  // mar:=a; rd; goto 13 
        cs[39] = mi(0, 0, 2, 0,  0, 1, 1, 0, 0,  0,   A,  0,  16);  // mar:=a; rd; goto 16
        
        // Decode 2º nível
        cs[40] = mi(0, 1, 0, 0,  0, 0, 0, 0, 1,TIR,  IR, IR,  52);  // tir:=ir+ir; if n goto 52
        cs[41] = mi(0, 1, 0, 0,  0, 0, 0, 0, 1,TIR, TIR,TIR,  46);  // tir:=tir+tir; if n goto 46
        cs[42] = mi(0, 1, 2, 0,  0, 0, 0, 0, 0,  0, TIR,TIR,  44);  // alu:=tir; if n goto 44

        // JNEG
        cs[43] = mi(0, 1, 2, 0,  0, 0, 0, 0, 0,  0,  AC, AC,  22);  // alu:=ac; if n goto 22;
        cs[44] = mi(0, 2, 2, 0,  0, 0, 0, 0, 0,  0,  AC, AC,   0);  // alu:=ac; if z goto 0;
        cs[45] = mi(0, 3, 0, 0,  0, 0, 0, 0, 1,TIR, TIR,TIR,  50);  // tir:=tir+tir; goto 50
        cs[46] = mi(0, 0, 0, 0,  0, 0, 0, 0, 1,  A,  IR, IR,  47);  // a:=ir+sp;
        cs[47] = mi(0, 3, 2, 0,  0, 0, 0, 0, 1, PC,AMASK,IR,   0);  // pc:=band(ir,amask); goto 0;
        
        // CALL
        cs[48] = mi(0, 0, 0, 0,  0, 0, 0, 0, 1, SP,  SP, M1,  49);  // sp:=sp+(-1);
        cs[49] = mi(0, 0, 2, 0,  1, 1, 0, 1, 0,  0,  SP, PC,  50);  // mar:=sp (B); mbr:=pc (C via A); wr;
        cs[50] = mi(0, 3, 2, 0,  0, 0, 0, 1, 1, PC,AMASK,IR,   0);  // pc:=band(ir,amask); wr; goto 0;
        
        // Decode grupo 1111
        cs[51] = mi(0, 1, 0, 0,  0, 0, 0, 0, 1,TIR,  IR, IR,  65);  // tir:=ir+ir; if n goto 65
        cs[52] = mi(0, 1, 0, 0,  0, 0, 0, 0, 1,TIR, TIR,TIR,  59);  // tir:=tir+tir; if n goto 59
        cs[53] = mi(0, 1, 2, 0,  0, 0, 0, 0, 0,  0, TIR,TIR,  56);  // alu:=tir; if n goto 56
        
        // PSHI
        cs[54] = mi(0, 0, 2, 0,  0, 1, 1, 0, 0,  0,  AC,  0,  55);  // mar:=ac; rd;
        cs[55] = mi(0, 0, 0, 0,  0, 1, 0, 0, 1, SP,  SP, M1,  10);  // sp:=sp-1; goto 10
        
        // POPI
        cs[56] = mi(0, 1, 2, 0,  0, 0, 0, 0, 0,  0, TIR,TIR,  62);  // alu:=tir; if n goto 62
        cs[57] = mi(0, 0, 0, 0,  0, 1, 1, 0, 1, SP,  SP, P1,  58);  // mar:=sp; sp:=sp+1; rd;
        cs[58] = mi(0, 0, 2, 0,  0, 1, 0, 1, 0,  0,  AC,  0,  10);  // mar:=ac; wr; goto 10
        
        // PUSH
        cs[59] = mi(0, 1, 0, 0,  0, 0, 0, 0, 1,TIR, TIR,TIR,  62);  // tir:=tir+tir; if n goto 62
        cs[60] = mi(0, 0, 0, 0,  0, 0, 0, 0, 1, SP,  SP, M1,  61);  // sp:=sp+(-1);
        cs[61] = mi(0, 0, 2, 0,  1, 1, 0, 1, 0,  0,  SP, AC,  10);  // mar:=sp; mbr:=ac; wr; goto 10
        
        // POP
        cs[62] = mi(0, 1, 0, 0,  0, 0, 0, 0, 1,TIR, TIR,TIR,  73);  // tir:=tir+tir; if n goto 73
        cs[63] = mi(0, 0, 0, 0,  0, 1, 1, 0, 1, SP,  SP, P1,  64);  // mar:=sp; sp:=sp+1; rd;
        cs[64] = mi(0, 3, 2, 0,  0, 0, 0, 0, 1, AC,   0,  0,   0);  // ac:=mbr; goto 0;
        
        // RETN
        cs[65] = mi(0, 1, 0, 0,  0, 0, 0, 0, 1,TIR,  IR, IR,  73);  // tir:=ir+ir; if n goto 73
        cs[66] = mi(0, 0, 0, 0,  0, 1, 1, 0, 1, SP,  SP, P1,  67);  // mar:=sp; sp:=sp+1; rd;
        cs[67] = mi(1, 3, 2, 0,  0, 0, 0, 0, 1, PC,   0,  0,   0);  // pc:=mbr; goto 0;
        
        // SWAP
        cs[68] = mi(0, 0, 2, 0,  0, 0, 0, 0, 1,  A,  AC, AC,  69);  // a:=ac;
        cs[69] = mi(0, 0, 2, 0,  0, 0, 0, 0, 1, AC,  SP, SP,  70);  // ac:=sp;
        cs[70] = mi(0, 3, 2, 0,  0, 0, 0, 0, 1, SP,   A,  A,   0);  // sp:=a; goto 0;
        
        // INSP / DESP
        cs[71] = mi(0, 1, 2, 0,  0, 0, 0, 0, 0,  0, TIR,TIR,  76);  // alu:=tir; if n goto 76
        cs[72] = mi(0, 0, 2, 0,  0, 0, 0, 0, 1,  A,SMASK,IR,  73);  // a:=band(ir,smask);
        cs[73] = mi(0, 3, 0, 0,  0, 0, 0, 0, 1, SP,  SP,  A,   0);  // sp:=sp+a; goto 0;
        
        cs[74] = mi(0, 0, 2, 0,  0, 0, 0, 0, 1,  A,SMASK,IR,  75);  // a:=band(ir,smask);
        cs[75] = mi(0, 0, 3, 0,  0, 0, 0, 0, 1,  A,   0,  A,  76);  // a:=inv(a);
        cs[76] = mi(0, 3, 0, 0,  0, 0, 0, 0, 1, SP,  SP,  A,   0);  // a:=a+1; goto 75
        
        // LOCO EXECUTION
        cs[77] = mi(0, 3, 1, 0,  0, 0, 0, 0, 1, AC,AMASK,IR,   0);  // ac:=band(ir,amask); goto 0;
        
        return cs;
    }

    // Construir micro new int[]{ amux cond alu sh   mbr mar rd wr  enc  C  B  A  addr }
    private static int[] mi(int amux, int cond, int alu, int sh, int mbrF, int marF, int rd, int wr, int enc, int cF, int bF, int aF, int addr) {
        return new int[]{amux, cond, alu, sh, mbrF, marF, rd, wr, enc, cF, bF, aF, addr};
    }


    // Strings para exibição de cada MPC 
    private String[] buildMpcStrings() {
        String[] s = new String[256];
        for (int i = 0; i < 256; i++) s[i] = "---";
        s[ 0] = "mar:=pc; rd;";
        s[ 1] = "pc:=pc + 1; rd;";
        s[ 2] = "ir:=mbr; if n then goto 28;";
        s[ 3] = "tir:=lshift(ir + ir); if n then goto 19;";
        s[ 4] = "tir:=lshift(tir); if n then goto 11;";
        s[ 5] = "alu:=tir; if n then goto 9;";
        s[ 6] = "mar:=ir; rd;";
        s[ 7] = "rd;";
        s[ 8] = "ac:=mbr; goto 0;";
        s[ 9] = "mar:=ir; mbr:=ac; wr;";
        s[10] = "wr; goto 0;";
        s[11] = "tir:=lshift(tir); if n then goto 15;";
        s[12] = "mar:=ir; rd;";
        s[13] = "rd;";
        s[14] = "ac:=mbr + ac; goto 0;";
        s[15] = "tir:=lshift(tir); if n then goto 25;";
        s[16] = "mar:=ir; rd;";
        s[17] = "rd;";
        s[18] = "a:=inv(mbr);";
        s[19] = "ac:=ac + a; goto 0;";
        s[20] = "tir:=lshift(tir); if n then goto 25;";
        s[21] = "tir:=lshift(tir); if n then goto 23;";
        s[22] = "alu:=ac; if n then goto 0;";
        s[23] = "pc:=band(ir,amask); goto 0;";
        s[24] = "alu:=ac; if z then goto 22;";
        s[25] = "goto 0;";
        s[26] = "alu:=tir; if n then goto 27;";
        s[27] = "ac:=band(ir,amask); goto 0;";
        s[28] = "tir:=lshift(ir + ir); if n then goto 40;";
        s[29] = "tir:=lshift(tir); if n then goto 35;";
        s[30] = "alu:=tir; if n then goto 33;";
        s[31] = "a:=ir + sp;";
        s[32] = "mar:=a; rd; goto 7;";
        s[33] = "a:=ir + sp;";
        s[34] = "mar:=a; mbr:=ac; wr; goto 10;";
        s[35] = "alu:=tir; if n then goto 38;";
        s[36] = "a:=ir + sp;";
        s[37] = "mar:=a; rd; goto 13;";
        s[38] = "mar:=a; rd; goto 16;";
        s[39] = "tir:=lshift(tir); if n then goto 46;";
        s[40] = "tir:=lshift(ir + ir); if n then goto 52;";
        s[41] = "tir:=lshift(tir); if n then goto 46;";
        s[42] = "alu:=tir; if n then goto 44;";
        s[43] = "alu:=ac; if n then goto 22;";
        s[44] = "alu:=ac; if z then goto 0;";
        s[45] = "tir:=lshift(tir); if n then goto 50";
        s[46] = "a:=ir + sp;";
        s[47] = "pc:=band(ir,amask); goto 0;";
        s[48] = "sp:=sp + (-1);";
        s[49] = "mar:=sp; mbr:=pc; wr;";
        s[50] = "pc:=band(ir,amask); wr; goto 0;";
        s[51] = "tir:=lshift(ir+ir); if n then goto 65;";
        s[52] = "tir:=lshift(tir); if n then goto 59;";
        s[53] = "alu:=tir; if n then goto 56;";
        s[54] = "mar:=ac; rd;";
        s[55] = "sp:=sp + (-1); rd;";
        s[56] = "mar:=sp; wr; goto 10;";
        s[57] = "mar:=sp; sp:=sp + 1; rd;";
        s[58] = "mar:=ac; wr; goto 10;";
        s[59] = "alu:=tir; if n then goto 62;";
        s[60] = "mar:=sp; mbr:=ac; wr; goto 10;";
        s[61] = "sp:=sp + (-1);";
        s[62] = "tir:=lshift(tir); if n then goto 73;";
        s[63] = "alu:=tir; if n then goto 70;";
        s[64] = "mar:=sp; sp:=sp + 1; rd;";
        s[65] = "tir:=lshift(ir+ir); if n then goto 73;";
        s[66] = "mar:=sp; sp:=sp + 1; rd;";
        s[67] = "pc:=mbr; goto 0;";
        s[68] = "a:=ac;";
        s[69] = "ac:=sp;";
        s[70] = "sp:=a; goto 0;";
        s[71] = "alu:=tir; if n then goto 76;";
        s[72] = "a:=band(ir,smask);";
        s[73] = "sp:=sp + a; goto 0;";
        s[74] = "a:=band(ir, smask);";
        s[75] = "a:=inv(a);";
        s[76] = "a:=a + 1; goto 75;";
        return s;
    }

    // Classe construtora
    public CPU(Memoria ram) {
        this.ram = ram;
        this.mpcStrings = buildMpcStrings();
        reset();
    }

    public void reset() {
        for (int i = 0; i < 16; i++) regs[i] = 0;
        // Constantes
        regs[ZERO]  = 0;
        regs[P1]    = 1;
        regs[M1]    = -1;           // complemento de 2 → 0xFFFF como 16 bits
        regs[AMASK] = 0x0FFF;       // 12 bits: máscara de endereço
        regs[SMASK] = 0x00FF;       // 8 bits: máscara de constante (INSP/DESP)

        mar = 0; mbr = 0;
        busA = 0; busB = 0; busC = 0;
        mpc = 0;
        subcicloAtual = 1;
        flagN = false; flagZ = true;
        rdPending = false; wrPending = false;
        statusCiclo = "IDLE";
        msgMPC = "---";
        sinalControle = "IDLE";
        opcodeAtual = -1;
        latchA = 0; latchB = 0;
        aluResult = 0; shifterResult = 0;
        totalSubciclos = 0;
        totalCiclos = 0;
        instrucoesExecutadas = 0;

        // Carrega MIR inicial
        carregarMIR(mpc);
    }

    private void carregarMIR(int addr) {
        if (addr < 0 || addr >= CONTROL_STORE.length || CONTROL_STORE[addr] == null) {
            // Endereço fora da faixa ou NOP
            amux_ctrl = 0; cond_ctrl = 0; alu_ctrl = 0; sh_ctrl = 0;
            mbr_ctrl = 0; mar_ctrl = 0; rd_ctrl = 0; wr_ctrl = 0;
            enc_ctrl = 0; c_reg = 0; b_reg = 0; a_reg = 0; addr_field = 0;
            return;
        }
        int[] mir = CONTROL_STORE[addr]; // Carrega sinais
        amux_ctrl = mir[0]; cond_ctrl = mir[1]; alu_ctrl = mir[2]; sh_ctrl = mir[3]; mbr_ctrl = mir[4]; mar_ctrl = mir[5]; rd_ctrl = mir[6];
        wr_ctrl   = mir[7]; enc_ctrl  = mir[8]; c_reg    = mir[9]; b_reg    = mir[10]; a_reg    = mir[11]; addr_field = mir[12];
    }

    public void executarSubciclo() {
        totalSubciclos++;
        switch (subcicloAtual) {
            case 1: sub1(); break;
            case 2: sub2(); break;
            case 3: sub3(); break;
            case 4: sub4(); break;
        }
        subcicloAtual = (subcicloAtual % 4) + 1;
    }

    /*
    SUBCICLO 1
        - MIR ← Control Store[MPC] (nova microinstrução)
        - Sinais de controle distribuídos ao datapath
        - Captura de dado: se havia leitura pendente (rdPending) && a memória já completou, MBR ← RAM[MAR_anterior] (fim do acesso de leitura)
        - Sinais rd/wr atualizados (transições: old→new)
        - Registradores A e B selecionados pelos decoders A e B
    */
    private void sub1() {
        if (mpc == 0) instrucoesExecutadas++;
        statusCiclo = "SUB1";
        msgMPC = String.format("MPC %02d: [%s]", mpc, mpcStrings[mpc]);
        

        carregarMIR(mpc); // MIR ← Control Store[MPC]
                
        // Captura de dado de leitura anterior
        if (rdPending) {
            System.out.println("É PRA LERRRR");
            mbr = ram.read(mar) & 0xFFFF;   // MBR ← RAM[MAR]
            rdPending = false;
            sinalControle = "READ";
        } 
        if (mpc == 2) opcodeAtual = (mbr >> 12) & 0xF; // atualiza opcode se estamos no MPC 2 (ir:=mbr)

        // Atualizar rd/wr (transições)
        if (rd_ctrl == 1) rdPending = true;   // leitura será capturada no próximo sub1
        if (wr_ctrl == 1) wrPending = true;   // escrita será completada no sub4

        busA = regs[a_reg]; busB = regs[b_reg]; // Joga registradores selecionados nos barramentos

        sinalControle = (rd_ctrl == 1) ? "READ" : (wr_ctrl == 1) ? "WRITE" : "IDLE";
    }

    /*
    SUBCICLO 2
        - Abre latch A e B
        - AMUX seleciona entrada da ULA
        - ULA processa (flags calculadas mas não gravadas)
        - Shifter processa saída da ULA
    */
    private void sub2() {
        statusCiclo = "SUB2";
        
        latchA = busA; latchB = busB; // habilita saída dos barramentos

        int entradaULA = (amux_ctrl == 1) ? mbr : latchA; // AMUX

        // Processa ULA
        switch (alu_ctrl) {
            case 0: aluResult = entradaULA + latchB; break;  // A + B
            case 1: aluResult = entradaULA & latchB; break;  // A AND B
            case 2: aluResult = entradaULA; break;           // A (passa)
            case 3: aluResult = ~entradaULA; break;          // NOT A
        }
        aluResult &= 0xFFFF;   // trunca a 16 bits

        // Flags calculadas pela ULA e travadas
        flagN_latched = (aluResult & 0x8000) != 0;
        flagZ_latched = (aluResult == 0);

        // Shifter
        switch (sh_ctrl) {
            case 0: shifterResult = aluResult; break;
            case 1: shifterResult = ((aluResult << 1) & 0xFFFF); break;  // lshift
            case 2: shifterResult = ((aluResult >> 1) & 0x7FFF); break;  // rshift 
        }

        busC = shifterResult; // Entrada do Barramento C
    }

    /*
    SUBCICLO 3 
        - Pode escrever no MAR -> Acesso à memória pode iniciar
        - Pode escrever no MBR
    */
    private void sub3() {
        statusCiclo = "SUB3";

        if (mar_ctrl == 1) mar = busB & 0x0FFF;   
        if (mbr_ctrl == 1) mbr = busC & 0xFFFF;
    }

    /* SUBCICLO 4
        - Registrador destino grava barramento C (se enc_ctrl=1)
        - Flags N e Z da ULA são armazenadas
        - MPC atualizado pela lógica de sequenciamento (Mmux + incremento)
        - Buffer de escrita completa: RAM[MAR] ← MBR (se wrPending)
    */
    private void sub4() {
        totalCiclos++;
        statusCiclo = "SUB4";
        
        if (enc_ctrl == 1 && (c_reg < ZERO || c_reg > SMASK)) regs[c_reg] = busC; //constantes 0, P1, M1, AMASK e SMASK não escrevem

        flagN = flagN_latched; flagZ = flagZ_latched; // armazena flags travadas


        // Completa escrita na memória (RAM[MAR] ← MBR)
        if (wrPending) {
        System.out.println("É PRA ESCREVER");
            ram.write(mar, mbr & 0xFFFF);
            wrPending = false;
            sinalControle = "WRITE";
        }

        int proximoMPC;
        switch (cond_ctrl) {
            case 0:  proximoMPC = mpc + 1; break;
            case 1:  proximoMPC = flagN ? addr_field : (mpc + 1); break; // if n
            case 2:  proximoMPC = flagZ ? addr_field : (mpc + 1); break; // if z
            case 3:  proximoMPC = addr_field; break; // goto
            default: proximoMPC = mpc + 1; break;
        }
        mpc = proximoMPC & 0xFF;   // 8 bits (256 posições na Control Store)
    }


    public void executarCicloCompleto() {
        sub1(); sub2(); sub3(); sub4();
        subcicloAtual = 1;
    }

    public int getMAR()             { return mar; }
    public int getMBR()             { return mbr; }

    public int getAReg()            { return a_reg; }
    public int getBReg()            { return b_reg; }
    public int getCReg()            { return c_reg; }
    public int getAmuxCtrl()        { return amux_ctrl; }
    public int getMarCtrl()         { return mar_ctrl; }
    public int getMbrCtrl()         { return mbr_ctrl; }
    public int getEncCtrl()         { return enc_ctrl; }

    public int getMPC()             { return mpc; }
    public int getSubcicloAtual()   { return subcicloAtual; }
    public boolean isFlagN()        { return flagN; }
    public boolean isFlagZ()        { return flagZ; }


    public String getSinalControle(){ return sinalControle; }
    public String getMsgMPC()       { return msgMPC; }
    public String getstatusCiclo()  { return statusCiclo; }
    public int getOpcodeAtual()     { return opcodeAtual; }
    public long getTotalSubciclos() { return totalSubciclos; }
    public long getTotalCiclos() { return totalCiclos; }
    public long getInstrucoesExecutadas() { return instrucoesExecutadas; }
    
    public int getBusA()            { return busA; }
    public int getBusB()            { return busB; }
    public int getBusC()            { return busC; }
    public int getLatchA()          { return latchA; }
    public int getLatchB()          { return latchB; }
    public int getAluResult()       { return aluResult; }
    public int getShifterResult()   { return shifterResult; }

    public int getReg(int idx) {
        return (idx >= 0 && idx < 16) ? regs[idx] : 0;
    }

// HM pras labels da interface
    public Map<String, Integer> getRegs() {
        Map<String, Integer> m = new HashMap<>();
        m.put("PC",    regs[PC]);
        m.put("AC",    regs[AC]);
        m.put("SP",    regs[SP]);
        m.put("IR",    regs[IR]);
        m.put("TIR",   regs[TIR]);
        m.put("ZERO",  regs[ZERO]);
        m.put("P1",    regs[P1]);
        m.put("M1",    regs[M1]);
        m.put("AMASK", regs[AMASK]);
        m.put("SMASK", regs[SMASK]);
        m.put("LV",    regs[A]); // LV mapeado no A Tanembaum
        m.put("B",     regs[B]);
        m.put("C",     regs[C]);
        m.put("D",     regs[D]);
        m.put("E",     regs[E]);
        m.put("F",     regs[F]);
        m.put("MAR",   mar);
        m.put("MBR",   mbr);
        return m;
    }
}