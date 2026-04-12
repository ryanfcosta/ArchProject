package com.simumic;

public class CPU {
    // Acesso a memoria externa
    private Memoria ram;
    public CPU(Memoria memoria) {
        this.ram = memoria;
    }

    // Registradores
    private int pc = 0; 
    private int ac = 0;
    private int ir = 0;
    
    // Registradores para contato com ram
    private int mar = 0;
    private int mbr = 0;
    
    // Registradores da stack ainda não implementados
    //private int lv = 0;
    //private int sp = 0; //stack pointer

    // Constante (final) mascara que isola endereço (12b) ainda não implementada
    //private final int AMASK = 0xFFF;
    
    // Fetch
    public void fetch(){
        // MPC 0: [mar := pc; rd;] Datapath: PC -> Travas da ULA -> ULA (passagem) -> Barramento C -> MAR
        this.mar = this.pc;
        
        // MPC 1: [pc := pc + 1; rd;] Datapath: PC -> Travas da ULA -> ULA (soma +1) -> Barramento C -> PC
        this.pc++; // soma enquanto sinal ram procura dado
        
        //MPC 2: [ir := mbr; if n then goto 28;] 
        //Datapath Externo: RAM -> Barramento de Dados Externo -> MBR
        this.mbr = ram.read(this.mar);
        //Datapath Interno: MBR -> Travas da ULA -> ULA (passgem) -> Barramento C -> IR
        this.ir = this.mbr;
    }

    // Devolve variaveis 
    public int getPC() { return pc; }
    public int getAC() { return ac; }
    public int getIR() { return ir; }
    public int getMAR() { return mar; }
    public int getMBR() { return mbr; }
}
