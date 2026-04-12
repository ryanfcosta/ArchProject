package com.simumic;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;

public class PrimaryController {
    // Labels XML do registrador
    @FXML private Label labelPC;
    @FXML private Label labelAC;
    @FXML private Label labelIR;
    @FXML private Label labelMAR;
    @FXML private Label labelMBR;

    // Labels XML das flags
    @FXML private Rectangle rectFlagN;
    @FXML private Label labelFlagN;

    // Barramento de Controle
    @FXML private Rectangle rectControl;
    @FXML private Label labelControl;

    // Input instrução 
    @FXML private TextField inputInstrucao;

    // Intancia CPU e RAM
    private Memoria ram = new Memoria();
    private CPU cpu = new CPU(ram); 
    
    // Controlador faz write  
    @FXML
    private void ramWrite() {
        String instrucaoTrim = inputInstrucao.getText().trim(); // Pega e tira os espaços do input

        // Escreve na RAM usando try catch para evitar diferente de 0/1
        try {
            // Converte para decimal
            int dataDec = Integer.parseInt(instrucaoTrim, 2);
            
            // Grava no edereço (0 por padrão da RAM)
            ram.write(0, dataDec); 
            System.out.println("Sucesso! Instrução gravada.");
        } catch (NumberFormatException e) {
            System.out.println("Erro: Digite apenas 0 e 1.");
        }
    }

    @FXML
    private void execCiclo() {
        // Da fetch
        cpu.fetch();

        // Atualiza as labels
        labelPC.setText(String.format("%04X", cpu.getPC()));
        labelAC.setText(String.format("%04X", cpu.getAC()));
        labelIR.setText(String.format("%04X", cpu.getIR()));
        labelMAR.setText(String.format("%04X", cpu.getMAR()));
        labelMBR.setText(String.format("%04X", cpu.getMBR()));
        
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
            // N bipou
            labelFlagN.setText("1");
            rectFlagN.setFill(Color.web("#e74c3c")); 
            rectFlagN.setStroke(Color.web("#c0392b"));
        } else {
            // N falso
            labelFlagN.setText("0");
            rectFlagN.setFill(Color.web("#34495e")); 
            rectFlagN.setStroke(Color.web("#7f8c8d"));
        }
    }
}