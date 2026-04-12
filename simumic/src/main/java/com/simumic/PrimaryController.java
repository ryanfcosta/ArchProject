    package com.simumic;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class PrimaryController {
    // Pega as labels do xml
    @FXML private Label labelPC;
    @FXML private Label labelAC;
    @FXML private Label labelIR;
    @FXML private Label labelMAR;
    @FXML private Label labelMBR;

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
    }
}