package com.simumic;

public class CPU {
    private Memoria ram;  // Acesso a memoria externa
    
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

    private String ctrlSign = "IDLE";

    //flags
    private boolean flagN = false; // flag decode

    // Constante (final) mascara que isola endereço (12b) ainda não implementada
    private final int AMASK = 0xFFF;
    public CPU(Memoria memoria) {
        this.ram = memoria;
        //initIntrucTree();
    }


    
    // Fetch
    public void fetch(){
        // MPC 0: [mar := pc; rd;] Datapath: PC -> Travas da ULA -> ULA (passagem) -> Barramento C -> MAR
        this.mar = this.pc;
        
        // MPC 1: [pc := pc + 1; rd;] Datapath: PC -> Travas da ULA -> ULA (soma +1) -> Barramento C -> PC
        this.pc++; // soma enquanto sinal ram procura dado
        this.ctrlSign = "READ";
        //MPC 2: [ir := mbr; if n then goto 28;] 
        //Datapath Externo: RAM -> Barramento de Dados Externo -> MBR
        this.mbr = ram.read(this.mar);
        //Datapath Interno: MBR -> Travas da ULA -> ULA (passgem) -> Barramento C -> IR
        this.ir = this.mbr;
    }

    // Decode
    public void decode_din(){
        int opcode = (this.ir >> 12) & 0xF; // Isola o opcode empurrando os outros bits para a direita (>>12) e usa máscara 4b = 1 filtra para tirar o lixo
        int endereco = this.ir & AMASK; // Isola o endereco usando a mascara 12b

        switch (opcode) {
            case 0: 
                // LODD (Load Direct): Carrega o valor da RAM para o Acumulador (AC)
                // MPC 10: [mar := ir; rd;]
                this.mar = endereco;             
                
                // MPC 11: [rd;] 
                this.ctrlSign = "READ"; 
                this.mbr = ram.read(this.mar);  
                
                // MPC 12: [ac := mbr; goto 0;] -> Volta para o fetch (0) após executar
                this.ac = this.mbr; 
                this.flagN = (this.ac < 0);            
                break;

            case 1: 
                // STOD (Store Direct): Guarda o valor do Acumulador (AC) na RAM
                // MPC 13: [mar := ir; mbr := ac; wr;]
                this.mar = endereco;             
                this.mbr = this.ac;             
                
                // MPC 14: [wr; goto 0;]
                this.ctrlSign = "WRITE";
                ram.write(this.mar, this.mbr);  
                break;

            case 2: 
                // ADDD (Add Direct): Soma o valor da RAM com o AC
                // MPC 15: [mar := ir; rd;]
                this.mar = endereco;
                
                // MPC 16: [rd;] 
                this.ctrlSign = "READ";
                this.mbr = ram.read(this.mar);
                
                // MPC 17: [ac := ac + mbr; goto 0;]
                this.ac += this.mbr;
                break;

            default:
                this.ctrlSign = "IDLE";
                System.out.println("[ERRO] Instrução não reconhecida: " + opcode);
                break;
        }
    }

    // Devolve variaveis / Getter
    public int getPC() { return pc; }
    public int getAC() { return ac; }
    public int getIR() { return ir; }
    public int getMAR() { return mar; }
    public int getMBR() { return mbr; }
    public boolean isFlagN() { return flagN; }
    public String getSinalControle() { return ctrlSign; }
}
