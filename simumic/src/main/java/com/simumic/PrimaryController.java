package com.simumic;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import java.util.Map;

public class PrimaryController {

    @FXML private Label    labelPC, labelAC, labelIR, labelMAR, labelMBR;
    @FXML private Label    labelLV, labelSP, labelTIR;
    @FXML private Rectangle rectPC, rectAC, rectIR, rectMAR, rectMBR;
    @FXML private Rectangle rectLV, rectSP, rectTIR;
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

    // ── Entradas do usuário ───────────────────────────────────────────────────
    @FXML private TextField  inputEndereco, inputInstrucao;
    @FXML private Label      labelPlaceholder;
    @FXML private Button     btnSubciclo;

    // ── CPU e estado ──────────────────────────────────────────────────────────
    private CPU    cpu;
    private Memoria ram;

    // Cores
    private static final Color COR_FUNDO     = Color.web("#2c3e50");
    private static final Color COR_AMARELO   = Color.web("#fbc531");
    private static final Color COR_VERDE     = Color.web("#2ecc71");
    private static final Color COR_AZUL      = Color.web("#3498db");
    private static final Color COR_ROXO      = Color.web("#9b59b6");
    private static final Color COR_ROSA      = Color.web("#e84393");
    private static final Color COR_CIANO     = Color.web("#00cec9");
    private static final Color COR_CINZA     = Color.web("#7f8c8d");
    private static final Color COR_VERMELHO  = Color.web("#e74c3c");
    private static final Color COR_VERMELHO2 = Color.web("#c0392b");
    private static final Color COR_VERDE2    = Color.web("#27ae60");

    @FXML
    public void initialize() {
        ram = new Memoria();
        cpu = new CPU(ram);

        // Scroll do microprograma segue texto automaticamente
        scrollMPC.heightProperty().addListener((obs, o, n) ->
            scrollMPC.setVvalue(1.0));

        // Placeholder no campo de instrução
        inputInstrucao.textProperty().addListener((obs, o, n) -> {
            int len = n == null ? 0 : n.length();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 16 - len; i++) sb.append('0');
            labelPlaceholder.setText(sb.toString());
        });

        atualizarLabels();
    }

    // Botões
    @FXML private void btnLODD() { inputInstrucao.setText("0000"); }
    @FXML private void btnSTOD() { inputInstrucao.setText("0001"); }
    @FXML private void btnADDD() { inputInstrucao.setText("0010"); }
    @FXML private void btnSUBD() { inputInstrucao.setText("0011"); }
    @FXML private void btnJPOS() { inputInstrucao.setText("0100"); }
    @FXML private void btnJZER() { inputInstrucao.setText("0101"); }
    @FXML private void btnJUMP() { inputInstrucao.setText("0110"); }
    @FXML private void btnLOCO() { inputInstrucao.setText("0111"); }
    @FXML private void btnLODL() { inputInstrucao.setText("1000"); }
    @FXML private void btnSTOL() { inputInstrucao.setText("1001"); }
    @FXML private void btnADDL() { inputInstrucao.setText("1010"); }
    @FXML private void btnSUBL() { inputInstrucao.setText("1011"); }
    @FXML private void btnJNEG() { inputInstrucao.setText("1100"); }
    @FXML private void btnJNZE() { inputInstrucao.setText("1101"); }
    @FXML private void btnCALL() { inputInstrucao.setText("1110"); }
    @FXML private void btnPSHI() { inputInstrucao.setText("1111000"); }
    @FXML private void btnPOPI() { inputInstrucao.setText("1111001"); }
    @FXML private void btnPUSH() { inputInstrucao.setText("1111010"); }
    @FXML private void btnPOP()  { inputInstrucao.setText("1111011"); }
    @FXML private void btnRETN() { inputInstrucao.setText("1111100"); }
    @FXML private void btnSWAP() { inputInstrucao.setText("1111101"); }
    @FXML private void btnINSP() { inputInstrucao.setText("1111110"); }

    @FXML
    private void ramWrite() {
        String endTrim  = inputEndereco.getText().trim();
        String instTrim = inputInstrucao.getText().trim();
        if (endTrim.isEmpty() || instTrim.isEmpty()) return;

        try {
            int endDec  = Integer.parseInt(endTrim);
            // Completa com zeros à direita até 16 bits
            String bits = String.format("%-16s", instTrim).replace(' ', '0');
            if (!bits.matches("[01]{16}")) {
                System.out.println("Erro: Digite apenas 0 e 1.");
                return;
            }
            int dataDec = Integer.parseInt(bits, 2);
            ram.write(endDec, dataDec);
            System.out.printf("WR: RAM[%d] = %04X (%s)%n", endDec, dataDec, bits);
        } catch (NumberFormatException e) {
            System.out.println("Erro: endereço inválido.");
        }
    }

    @FXML
    private void resetCpu() {
        cpu.reset();
        btnSubciclo.setText("SUBCICLO 1");
        btnSubciclo.setStyle("-fx-background-color: #c23616; -fx-text-fill: white; " +
                             "-fx-font-weight: bold; -fx-font-size: 14; -fx-padding: 10 20;");
        atualizarLabels();
    }

    @FXML
    private void execSubciclo() {
        cpu.executarSubciclo();
        atualizarLabels();
        atualizarBotaoSubciclo();
    }

    @FXML
    private void execCicloCompleto() {
        cpu.executarCicloCompleto();
        atualizarLabels();
        atualizarBotaoSubciclo();
    }

    private void atualizarBotaoSubciclo() {
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
    }

    //  ATUALIZA TODAS AS LABELS DA UI
    private void atualizarLabels() {
        Map<String, Integer> regs = cpu.getRegs();

        preencherReg(labelPC,  rectPC,  regs.get("PC"),  COR_AMARELO);
        preencherReg(labelAC,  rectAC,  regs.get("AC"),  COR_ROXO);
        preencherReg(labelIR,  rectIR,  regs.get("IR"),  COR_AMARELO);
        preencherReg(labelMAR, rectMAR, cpu.getMAR(),    COR_VERDE);
        preencherReg(labelMBR, rectMBR, cpu.getMBR(),    COR_AZUL);
        preencherReg(labelSP,  rectSP,  regs.get("SP"),  COR_ROSA);
        preencherReg(labelLV,  rectLV,  regs.get("LV"),  COR_ROSA);
        if (labelTIR != null)
            preencherReg(labelTIR, rectTIR, regs.get("TIR"), COR_CIANO);

        boolean n = cpu.isFlagN();
        boolean z = cpu.isFlagZ();
        labelFlagN.setText(n ? "1" : "0");
        labelFlagZ.setText(z ? "1" : "0");
        rectFlagN.setFill(n ? COR_VERMELHO : COR_FUNDO);
        rectFlagZ.setFill(z ? COR_VERDE   : COR_FUNDO);

        String sinal = cpu.getSinalControle();
        labelControl.setText(sinal);
        switch (sinal) {
            case "READ":    { rectControl.setFill(COR_VERDE2); rectControl.setStroke(COR_VERDE);break; }
            case "WRITE":   { rectControl.setFill(COR_VERMELHO2); rectControl.setStroke(COR_VERMELHO);break; }
            default:        { rectControl.setFill(COR_FUNDO); rectControl.setStroke(COR_CINZA);break; }
        }

        String status = cpu.getstatusCiclo();
        labelStatus.setText(status);
        Color corStatus;
        switch (status) {
            case "SUB1":  corStatus = COR_VERMELHO; break;
            case "SUB2":  corStatus = COR_ROXO;     break;
            case "SUB3" : corStatus = COR_AZUL;     break;
            case "SUB4" : corStatus = COR_VERDE;    break; 
            default:      corStatus = COR_ROSA;     break;
        };
        labelStatus.setTextFill(corStatus);

        // Microprograma
        String mpcMsg = cpu.getMsgMPC();
        if (labelMPC != null && status == "SUB1") {
            String atual = labelMPC.getText();
            labelMPC.setText(atual.equals("---") ? mpcMsg : atual + "\n" + mpcMsg);
            scrollMPC.setVvalue(1.0);
        }

        // Barramentos
        if (labelBusA != null)    labelBusA.setText(String.format("%04X", cpu.getBusA()));
        if (labelBusB != null)    labelBusB.setText(String.format("%04X", cpu.getBusB()));
        if (labelBusC != null)    labelBusC.setText(String.format("%04X", cpu.getBusC()));
        if (labelLatchA != null)  labelLatchA.setText(String.format("%04X", cpu.getLatchA()));
        if (labelLatchB != null)  labelLatchB.setText(String.format("%04X", cpu.getLatchB()));
        if (labelALU != null)     labelALU.setText(String.format("%04X", cpu.getAluResult()));
        if (labelShifter != null) labelShifter.setText(String.format("%04X", cpu.getShifterResult()));
    }

    private void preencherReg(Label label, Rectangle rect, Integer valor, Color corBorda) {
        if (label == null) return;
        int v = (valor == null) ? 0 : valor;
        label.setText(String.format("%04X", v & 0xFFFF));
        if (rect != null) rect.setStroke(corBorda);
    }
}