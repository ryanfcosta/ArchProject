package com.simumic;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import java.util.Map;

public class PrimaryController {

    @FXML private Label labelPC, labelAC, labelIR, labelMAR, labelMBR, labelLV, labelSP, labelTIR, labelB, labelC, labelD, labelE, labelF, labelAMASK, labelSMASK;
    @FXML private Rectangle rectPC, rectAC, rectIR, rectMAR, rectMBR, rectLV, rectSP, rectTIR, rectB, rectC, rectD, rectE, rectF, rectAMASK, rectSMASK;
    @FXML private Label labelZERO, labelP1, labelM1;
    @FXML private Rectangle rectZERO, rectP1, rectM1;
    @FXML private Label    labelFlagN, labelFlagZ;
    @FXML private Rectangle rectFlagN, rectFlagZ;
    @FXML private Label    labelControl;
    @FXML private Rectangle rectControl;
    @FXML private Label      labelMPC;
    @FXML private ScrollPane scrollMPC;
    @FXML private Label      labelStatus;
    @FXML private Label labelBusA, labelBusB, labelBusC;
    @FXML private Label labelLatchA, labelLatchB;
    @FXML private Label labelALU, labelShifter;

    @FXML private Button     btnSubciclo;

    private CPU    cpu;
    private Memoria ram;

    @FXML private TextArea consoleMacro;
    @FXML private Label labelAssemblerStatus;
    @FXML private Label labelEstatisticas, labelInstr;

    // Cores
    private static final Color COR_FUNDO     = Color.web("#000000");
    private static final Color COR_CINZA     = Color.web("#d2d5d5");
    private static final Color COR_VERDE     = Color.web("#077d29");
    private static final Color COR_VERMELHO  = Color.web("#c3150e");

    @FXML
    public void initialize() {
        ram = new Memoria();
        cpu = new CPU(ram);

        if (scrollMPC != null) {
            scrollMPC.heightProperty().addListener((obs, o, n) -> scrollMPC.setVvalue(1.0));
        }

        atualizarLabels();
    }

    @FXML
    private void resetCpu() {
        cpu.reset();
        btnSubciclo.setText("SUBCICLO 1");
        btnSubciclo.setStyle("-fx-background-color: #c23616; -fx-text-fill: white; " + "-fx-font-weight: bold; -fx-font-size: 14; -fx-padding: 10 20;");
        atualizarLabels();
    }

    @FXML
    private void execSubciclo() {
        cpu.executarSubciclo();
        atualizarLabels();
        int sb = atualizarBotaoSubciclo();
        if(sb == 1)
            imprimirConsoleMPC();
    }

    @FXML
    private void execCicloCompleto() {
        cpu.executarCicloCompleto();
        atualizarLabels();
        atualizarBotaoSubciclo();
        imprimirConsoleMPC();
    }

private void imprimirConsoleMPC() {
        if (labelMPC != null) {
            String mpcMsg = cpu.getMsgMPC();
            if (mpcMsg.startsWith("MPC 77") || mpcMsg.startsWith("MPC 27")) {
                return; 
            }

            if (mpcMsg.startsWith("MPC 00")) {
                mpcMsg = "\n" + mpcMsg;
            }

            String atual = labelMPC.getText();
            labelMPC.setText(atual.equals("---") ? mpcMsg : atual + "\n" + mpcMsg);
            
            if (scrollMPC != null) scrollMPC.setVvalue(1.0); // Desce a barra de scroll automaticamente
        }
    }
    private int atualizarBotaoSubciclo() {
        int proximo = cpu.getSubcicloAtual();// estadoClock já aponta para o PRÓXIMO subciclo a ser executado
        String[] nomes = {
            "SUBCICLO 1",
            "SUBCICLO 2",
            "SUBCICLO 3",
            "SUBCICLO 4"
        };
        String[] cores = { "#c23616", "#8e44ad", "#2980b9", "#27ae60" };
        btnSubciclo.setText(nomes[proximo - 1]);
        btnSubciclo.setStyle(
            "-fx-background-color: " + cores[proximo - 1] + "; " +
            "-fx-text-fill: white; -fx-font-weight: bold; " +
            "-fx-font-size: 12; -fx-padding: 10 20;"
        );

        return proximo;
    }

    //  ATUALIZA TODAS AS LABELS DA UI
    private void atualizarLabels() {
        Map<String, Integer> regs = cpu.getRegs();

        preencherReg(labelPC,  rectPC,  regs.get("PC"));
        preencherReg(labelAC,  rectAC,  regs.get("AC"));
        preencherReg(labelIR,  rectIR,  regs.get("IR"));
        preencherReg(labelMAR, rectMAR, cpu.getMAR());
        preencherReg(labelMBR, rectMBR, cpu.getMBR());
        preencherReg(labelSP,  rectSP,  regs.get("SP"));
        preencherReg(labelLV,  rectLV,  regs.get("LV"));
        preencherReg(labelZERO,  rectZERO,  regs.get("ZERO"));
        preencherReg(labelP1,    rectP1,    regs.get("P1"));
        preencherReg(labelM1,    rectM1,    regs.get("M1"));
        preencherReg(labelAMASK, rectAMASK, regs.get("AMASK"));
        preencherReg(labelSMASK, rectSMASK, regs.get("SMASK"));
        preencherReg(labelB,     rectB,     regs.get("B"));
        preencherReg(labelC,     rectC,     regs.get("C"));
        preencherReg(labelD,     rectD,     regs.get("D"));
        preencherReg(labelE,     rectE,     regs.get("E"));
        preencherReg(labelF,     rectF,     regs.get("F"));
        if (labelTIR != null)
            preencherReg(labelTIR, rectTIR, regs.get("TIR"));

        boolean n = cpu.isFlagN();
        boolean z = cpu.isFlagZ();
        labelFlagN.setText(n ? "1" : "0");
        labelFlagZ.setText(z ? "1" : "0");
        rectFlagN.setFill(n ? COR_VERDE : COR_FUNDO);
        rectFlagZ.setFill(z ? COR_VERDE : COR_FUNDO);

        String sinal = cpu.getSinalControle();
        labelControl.setText(sinal);
        switch (sinal) {
            case "READ":    { rectControl.setFill(COR_VERDE); rectControl.setStroke(COR_VERDE);break; }
            case "WRITE":   { rectControl.setFill(COR_VERMELHO); rectControl.setStroke(COR_VERMELHO);break; }
            default:        { rectControl.setFill(COR_FUNDO); rectControl.setStroke(COR_CINZA);break; }
        }

        String status = cpu.getstatusCiclo();
        labelStatus.setText(status);
        Color corStatus;
        switch (status) {
            case "SUB1":  corStatus = COR_VERDE; break;
            case "SUB2":  corStatus = COR_VERDE; break;
            case "SUB3" : corStatus = COR_VERDE; break;
            case "SUB4" : corStatus = COR_VERDE; break; 
            default:      corStatus = COR_CINZA; break;
        };
        labelStatus.setTextFill(corStatus);

        if (labelBusA != null)    labelBusA.setText(String.format("%04X", cpu.getBusA()));
        if (labelBusB != null)    labelBusB.setText(String.format("%04X", cpu.getBusB()));
        if (labelBusC != null)    labelBusC.setText(String.format("%04X", cpu.getBusC()));
        if (labelLatchA != null)  labelLatchA.setText(String.format("%04X", cpu.getLatchA()));
        if (labelLatchB != null)  labelLatchB.setText(String.format("%04X", cpu.getLatchB()));
        if (labelALU != null)     labelALU.setText(String.format("%04X", cpu.getAluResult()));
        if (labelShifter != null) labelShifter.setText(String.format("%04X", cpu.getShifterResult()));
    
        if (labelEstatisticas != null) {
            labelEstatisticas.setText(cpu.getTotalCiclos() + " Ciclos | " + cpu.getTotalSubciclos() + " Sub");
            labelInstr.setText(cpu.getInstrucoesExecutadas() + " Instr. Executadas");
        }
    }

    private void preencherReg(Label label, Rectangle rect, Integer valor) {
        if (label == null) return;
        int v = (valor == null) ? 0 : valor;
        label.setText(String.format("%04X", v & 0xFFFF));
        if (rect != null) rect.setStroke(COR_CINZA);
    }
    @FXML
    private void montarCodigo() {
        String texto = consoleMacro.getText();
        if (texto == null || texto.trim().isEmpty()) {
            labelAssemblerStatus.setTextFill(COR_VERMELHO);
            labelAssemblerStatus.setText("Console vazio!");
            return;
        }

        String[] linhas = texto.split("\\n");
        int endereco = 0; // O programa sempre começa a gravar no endereço 0 da RAM
        int sucesso = 0;

        for (String linha : linhas) {
            linha = linha.trim();
            // Ignora linhas vazias ou comentários (iniciados com ponto e vírgula)
            if (linha.isEmpty() || linha.startsWith(";")) continue; 

            try {
                int instrucaoMontada = parseLinha(linha);
                ram.write(endereco, instrucaoMontada);
                endereco++;
                sucesso++;
            } catch (Exception e) {
                labelAssemblerStatus.setTextFill(COR_VERMELHO);
                labelAssemblerStatus.setText("Erro de sintaxe na linha " + (sucesso + 1) + "!");
                return;
            }
        }

        labelAssemblerStatus.setTextFill(COR_VERDE);
        labelAssemblerStatus.setText(sucesso + " instruções na RAM!");
        System.out.println("Montagem concluída com sucesso.");
    }

    // Tradutor de Mnemônicos para Binário
    private int parseLinha(String linha) {
        String[] partes = linha.trim().split("\\s+");
        String mnem = partes[0].toUpperCase();
        int arg = 0;

        if (partes.length > 1) arg = Integer.parseInt(partes[1]); // Se a instrução tiver argumento pega o valor

        int opcode = 0;
        
        switch (mnem) {
            case "LODD": opcode = 0x0000; break;
            case "STOD": opcode = 0x1000; break;
            case "ADDD": opcode = 0x2000; break;
            case "SUBD": opcode = 0x3000; break;
            case "JPOS": opcode = 0x4000; break;
            case "JZER": opcode = 0x5000; break;
            case "JUMP": opcode = 0x6000; break;
            case "LOCO": opcode = 0x7000; break;
            case "LODL": opcode = 0x8000; break;
            case "STOL": opcode = 0x9000; break;
            case "ADDL": opcode = 0xA000; break;
            case "SUBL": opcode = 0xB000; break;
            case "JNEG": opcode = 0xC000; break;
            case "JNZE": opcode = 0xD000; break;
            case "CALL": opcode = 0xE000; break;
            // Família Estendida (Opcode 15 = 1111)
            case "PSHI": opcode = 0xF000; break; // 1111 000
            case "POPI": opcode = 0xF200; break; // 1111 001
            case "PUSH": opcode = 0xF400; break; // 1111 010
            case "POP":  opcode = 0xF600; break; // 1111 011
            case "RETN": opcode = 0xF800; break; // 1111 100
            case "SWAP": opcode = 0xFA00; break; // 1111 101
            case "INSP": opcode = 0xFC00; break; // 1111 110
            case "DESP": opcode = 0xFE00; break; // 1111 111
            default: 
                throw new IllegalArgumentException("Mnemônico desconhecido: " + mnem);
        }

        // Funde o Opcode (4 bits mais significativos) com o Argumento (12 bits) usando OR Lógico
        return opcode | (arg & 0x0FFF);
    }
}