package com.simumic;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
//import javafx.scene.layout.HBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;

public class PrimaryController {
    // Labels XML do registrador
    @FXML private Label labelPC, labelAC, labelIR, labelMAR, labelMBR, labelMPCReg;
    @FXML private Rectangle rectPC, rectAC, rectIR, rectMAR, rectMBR, rectLV, rectSP, rectMPC;
    
    // Registradores da Pilha
    @FXML private Label labelLV; 
    @FXML private Label labelSP; 

    // Labels XML das flags
    @FXML private Rectangle rectFlagN, rectFlagZ;
    @FXML private Label labelFlagN, labelFlagZ;

    // Barramento de Controle
    @FXML private Rectangle rectControl;
    @FXML private Label labelControl;

    // Input instrução e Botão
    @FXML private TextField inputInstrucao;
    @FXML private Button btnSubciclo; 
    @FXML private TextField inputEndereco;
    @FXML private Label labelPlaceholder;

    // MPC e Assembly
    @FXML private Label labelStatus, labelMPC;
    @FXML private ScrollPane scrollMPC;

    // Instancia CPU e RAM
    private Memoria ram = new Memoria();
    private CPU cpu = new CPU(ram); 

    // Clock de 4 Fases (0 = Fetch, 1 = Decode, 2 = Memória, 3 = Execute)
    private int estadoClock = 0;

    @FXML private void resetCpu() {
        cpu.reset(); // Reseta a lógica interna da CPU
        estadoClock = 0; // Reseta clock para a fase 0 (Fetch)
        
        // Volta o botão
        btnSubciclo.setText("SUBCICLO 1: FETCH");
        btnSubciclo.setStyle("-fx-background-color: #c23616; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14; -fx-padding: 10 20;");
        
        attLabels(); 
    }

    @FXML private void ramWrite() {
        String endTrim = inputEndereco.getText().trim();
        String instTrim = inputInstrucao.getText().trim(); // Pega e tira os espaços do input

        // Escreve na RAM usando try catch para evitar diferente de 0/1
        try {
            int endDec = endTrim.isEmpty() ? 0 : Integer.parseInt(endTrim); // Grava no endereço (0 por padrão)
            int dataDec = Integer.parseInt(instTrim, 2); // Converte para decimal

            ram.write(endDec, dataDec); 
            System.out.println("Sucesso! Memoria[" + endDec + "] = " + dataDec);
            
            labelControl.setText("WR: RAM[" + endDec + "]");
            rectControl.setFill(Color.web("#e67e22"));
        } catch (NumberFormatException e) {
            System.out.println("Erro: Digite apenas 0 e 1.");
        }
    }

    @FXML
    private void execSubciclo() {
        if (estadoClock == 0) {
            cpu.fetch();
            btnSubciclo.setText("SUBCICLO 2: DECODE");
            btnSubciclo.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14; -fx-padding: 10 20;");
            estadoClock = 1;

        } else if (estadoClock == 1) {
            cpu.decode();
            btnSubciclo.setText("SUBCICLO 3: ACESSA MEMÓRIA");
            btnSubciclo.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14; -fx-padding: 10 20;");
            estadoClock = 2;

        } else if (estadoClock == 2) {
            cpu.memoria();
            btnSubciclo.setText("SUBCICLO 4: EXECUTE");
            btnSubciclo.setStyle("-fx-background-color: #8e44ad; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14; -fx-padding: 10 20;");
            estadoClock = 3;

        } else {
            cpu.executeULA();
            btnSubciclo.setText("SUBCICLO 1: FETCH");
            btnSubciclo.setStyle("-fx-background-color: #c23616; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14; -fx-padding: 10 20;");
            estadoClock = 0;
        }
        
        attLabels();
    }


    @FXML private void execCicloCompleto() {
        if (estadoClock == 0) {
            cpu.fetch(); cpu.decode(); cpu.memoria(); cpu.executeULA();
        } else if (estadoClock == 1) {
            cpu.decode(); cpu.memoria(); cpu.executeULA();
        } else if (estadoClock == 2) {
            cpu.memoria(); cpu.executeULA();
        } else if (estadoClock == 3) {
            cpu.executeULA();
        }
        
        btnSubciclo.setText("SUBCICLO 1: FETCH");
        btnSubciclo.setStyle("-fx-background-color: #c23616; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14; -fx-padding: 10 20;");
        estadoClock = 0;
        
        attLabels();
    }

    private void attLabels() {
        // Atualiza as labels dos registradores básicos
        labelPC.setText(String.format("%04X", cpu.getPC()));
        labelAC.setText(String.format("%04X", cpu.getAC()));
        labelIR.setText(String.format("%04X", cpu.getIR()));
        labelMAR.setText(String.format("%04X", cpu.getMAR()));
        labelMBR.setText(String.format("%04X", cpu.getMBR()));
        
        // Atualiza as labels da Pilha 
        if (labelLV != null) labelLV.setText(String.format("%04X", cpu.getLV()));
        if (labelSP != null) labelSP.setText(String.format("%04X", cpu.getSP()));
        if (labelMPCReg != null) labelMPCReg.setText(String.format("%04X", cpu.getMPC()));
        
        // Atualiza Barramento de Controle (Sinal RD/WR)
        String sinal = cpu.getSinalControle();
        labelControl.setText(sinal);
        
        if (sinal.equals("READ")) {
            rectControl.setFill(Color.web("#2ecc71")); // Verde
            rectControl.setStroke(Color.web("#27ae60"));
        } else if (sinal.equals("WRITE")) {
            rectControl.setFill(Color.web("#e74c3c")); // Vermelho
            rectControl.setStroke(Color.web("#c0392b"));
        } else {
            rectControl.setFill(Color.web("#34495e")); // Cinza (IDLE)
            rectControl.setStroke(Color.web("#7f8c8d"));
        }

        // Container flag N
        if (cpu.isFlagN()) {
            labelFlagN.setText("1");
            rectFlagN.setFill(Color.web("#e74c3c")); 
            rectFlagN.setStroke(Color.web("#c0392b"));
        } else {
            labelFlagN.setText("0");
            rectFlagN.setFill(Color.web("#34495e")); 
            rectFlagN.setStroke(Color.web("#7f8c8d"));
        }

        // Container flag Z 
        if (rectFlagZ != null && labelFlagZ != null) {
            if (cpu.isFlagZ()) {
                labelFlagZ.setText("1");
                rectFlagZ.setFill(Color.web("#3498db"));
                rectFlagZ.setStroke(Color.web("#2980b9"));
            } else {
                labelFlagZ.setText("0");
                rectFlagZ.setFill(Color.web("#34495e")); 
                rectFlagZ.setStroke(Color.web("#7f8c8d"));
            }
        }

        // Atualiza status, mpc e preenche registrador
        labelStatus.setText(cpu.getstatusCiclo());
        // Label MPC vai ser um console que salva o MPC anterior
        labelMPC.setText(cpu.getMsgMPC());
        fillReg();
        System.out.println(cpu.getMPC());
    }
    
    private void fillReg() {
        Color corFundoApagado = Color.web("#2c3e50");
        Color corAmarelo = Color.web("#fbc531"); // PC, IR
        Color corVerde   = Color.web("#2ecc71"); // MAR
        Color corAzul    = Color.web("#3498db"); // MBR
        Color corRoxo    = Color.web("#9b59b6"); // AC
        Color corRosa    = Color.web("#e84393"); // LV, SP
        Color corCiano = Color.web("#00cec9"); //MPC

        // Apaga
        rectMAR.setFill(corFundoApagado); labelMAR.setTextFill(corVerde);
        rectPC.setFill(corFundoApagado);  labelPC.setTextFill(corAmarelo);
        rectMBR.setFill(corFundoApagado); labelMBR.setTextFill(corAzul);
        rectIR.setFill(corFundoApagado);  labelIR.setTextFill(corAmarelo);
        rectAC.setFill(corFundoApagado);  labelAC.setTextFill(corRoxo);
        rectMPC.setFill(corFundoApagado); labelMPCReg.setTextFill(corCiano);
        
        if (rectLV != null) rectLV.setFill(corFundoApagado);
        if (labelLV != null) labelLV.setTextFill(corRosa);
        if (rectSP != null) rectSP.setFill(corFundoApagado);
        if (labelSP != null) labelSP.setTextFill(corRosa);

        // Acende
        if (estadoClock == 1) { 
            // FETCH 
            rectPC.setFill(corAmarelo); labelPC.setTextFill(Color.WHITE);
            rectMAR.setFill(corVerde);  labelMAR.setTextFill(Color.WHITE);
            rectMBR.setFill(corAzul);   labelMBR.setTextFill(Color.WHITE);
            rectIR.setFill(corAmarelo); labelIR.setTextFill(Color.WHITE);
            if (rectMPC != null) { rectMPC.setFill(corCiano); labelMPCReg.setTextFill(Color.WHITE); }
        } 
        else if (estadoClock == 2) { 
            // DECODE
            rectIR.setFill(corAmarelo); labelIR.setTextFill(Color.WHITE);
        } 
        else if (estadoClock == 3) { 
            // MEMÓRIA
            rectMAR.setFill(corVerde); labelMAR.setTextFill(Color.WHITE);
            rectMBR.setFill(corAzul);  labelMBR.setTextFill(Color.WHITE);
            
            // Instrução de pilha, acende o LV ou SP
            int op = cpu.getOpcodeAtual();
            if (op == 8 || op == 9 || op == 10 || op == 11) { 
                if (rectLV != null) rectLV.setFill(corRosa);
                if (labelLV != null) labelLV.setTextFill(Color.WHITE);
            } else if (op == 14) { // CALL
                if (rectSP != null) rectSP.setFill(corRosa);
                if (labelSP != null) labelSP.setTextFill(Color.WHITE);
            }
        } 
        else if (estadoClock == 0) { 
            // EXECUTE
            rectAC.setFill(corRoxo); labelAC.setTextFill(Color.WHITE);
            
            // JUMP mexe PC
            int op = cpu.getOpcodeAtual();
            if (op >= 4 && op <= 6 || op >= 12 && op <= 14) {
                rectPC.setFill(corAmarelo); labelPC.setTextFill(Color.WHITE);
            }
        }
    }

    // Placeholder/Ghost text
    @FXML public void initialize() {
        labelMPC.heightProperty().addListener((observable, oldValue, newValue) -> {
            scrollMPC.setVvalue(1.0); // Listener que rola a barrinha
        });

        inputInstrucao.textProperty().addListener((observable, oldValue, newValue) -> {
            int tamanho = newValue.length();
            
            if (tamanho == 0) {
                labelPlaceholder.setText("0000000000000000");
                labelPlaceholder.setTextFill(Color.web("#bdc3c7")); 
            } else if (tamanho < 16) {
                // Append vazio
                StringBuilder espacos = new StringBuilder();
                for (int i = 0; i < tamanho; i++) espacos.append(" ");
                // Zeros que faltam
                StringBuilder zeros = new StringBuilder();
                for (int i = 0; i < 16 - tamanho; i++) zeros.append("0");
                
                labelPlaceholder.setText(espacos.toString() + zeros.toString());
                labelPlaceholder.setTextFill(Color.web("#f39c12")); 
            } else {
                labelPlaceholder.setText(""); 
            }
        });
    }

    // Botão preenche opcode
    @FXML private void btnLODD() { inputInstrucao.setText("0000"); inputInstrucao.requestFocus(); }
    @FXML private void btnSTOD() { inputInstrucao.setText("0001"); inputInstrucao.requestFocus(); }
    @FXML private void btnADDD() { inputInstrucao.setText("0010"); inputInstrucao.requestFocus(); }
    @FXML private void btnSUBD() { inputInstrucao.setText("0011"); inputInstrucao.requestFocus(); }
    @FXML private void btnJPOS() { inputInstrucao.setText("0100"); inputInstrucao.requestFocus(); }
    @FXML private void btnJZER() { inputInstrucao.setText("0101"); inputInstrucao.requestFocus(); }
    @FXML private void btnJUMP() { inputInstrucao.setText("0110"); inputInstrucao.requestFocus(); }
    @FXML private void btnLOCO() { inputInstrucao.setText("0111"); inputInstrucao.requestFocus(); }
    
    @FXML private void btnLODL() { inputInstrucao.setText("1000"); inputInstrucao.requestFocus(); }
    @FXML private void btnSTOL() { inputInstrucao.setText("1001"); inputInstrucao.requestFocus(); }
    @FXML private void btnADDL() { inputInstrucao.setText("1010"); inputInstrucao.requestFocus(); }
    @FXML private void btnSUBL() { inputInstrucao.setText("1011"); inputInstrucao.requestFocus(); }
    @FXML private void btnJNEG() { inputInstrucao.setText("1100"); inputInstrucao.requestFocus(); }
    @FXML private void btnJNZE() { inputInstrucao.setText("1101"); inputInstrucao.requestFocus(); }
    @FXML private void btnCALL() { inputInstrucao.setText("1110"); inputInstrucao.requestFocus(); }
    
    @FXML private void btnPSHI() { inputInstrucao.setText("1111000"); inputInstrucao.requestFocus(); }
    @FXML private void btnPOPI() { inputInstrucao.setText("1111001"); inputInstrucao.requestFocus(); }
    @FXML private void btnPUSH() { inputInstrucao.setText("1111010"); inputInstrucao.requestFocus(); }
    @FXML private void btnPOP()  { inputInstrucao.setText("1111011"); inputInstrucao.requestFocus(); }
    @FXML private void btnRETN() { inputInstrucao.setText("1111100"); inputInstrucao.requestFocus(); }
    @FXML private void btnSWAP() { inputInstrucao.setText("1111101"); inputInstrucao.requestFocus(); }
    @FXML private void btnINSP() { inputInstrucao.setText("1111110"); inputInstrucao.requestFocus(); }
}