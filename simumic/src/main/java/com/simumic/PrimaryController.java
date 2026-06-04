package com.simumic;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.Map;

public class PrimaryController {

@FXML private Label labelPC, labelAC, labelSP, labelIR, labelTIR, labelLV, 
                        labelB, labelC, labelD, labelE, labelF, 
                        labelAMASK, labelSMASK, labelMAR, labelMBR, 
                        labelZERO, labelP1, labelM1;

    @FXML private Rectangle rectPC, rectAC, rectSP, rectIR, rectTIR, rectLV, 
                            rectB, rectC, rectD, rectE, rectF, 
                            rectAMASK, rectSMASK, rectMAR, rectMBR, 
                            rectZERO, rectP1, rectM1;

    @FXML private Label labelBusA, labelBusB, labelBusC, labelLatchA, labelLatchB, 
                        labelALU, labelShifter, labelStatus, 
                        labelFlagN, labelFlagZ;
    
    @FXML private Rectangle rectLatchA, rectLatchB, rectAMUX, rectShifter,rectControl, 
                            rectFlagN, rectFlagZ;

    @FXML private Rectangle rectFlagRD, rectFlagWR;
    @FXML private Polygon polygonALU;
    
    @FXML private Rectangle rectDecA, rectDecB, rectDecC;

    // I/O
    @FXML private TextArea consoleMacro;
    @FXML private ScrollPane scrollMPC;
    @FXML private Label labelMPC, labelAssemblerStatus, labelEstatisticas, labelInstr;
    @FXML private Button btnSubciclo;
    // Botão e spinner para execução automática
    @FXML private Button btnAutoRun;
    @FXML private Spinner<Integer> spinnerDelay;

    private CPU cpu;
    private Memoria ram;

    // Timeline para o auto run — roda um ciclo completo a cada N ms
    private Timeline autoRunTimeline;
    // Referência à janela de memória, para não abrir duplicata
    private Stage janelaMemoria;

    // CORES
    private static final Color COR_FUNDO    = Color.web("#000000");
    private static final Color COR_CINZA    = Color.web("#d2d5d5");
    private static final Color COR_VERDE    = Color.web("#077d29");
    private static final Color COR_VERMELHO = Color.web("#c3150e");

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
        
        btnSubciclo.setStyle(
            "-fx-background-color: linear-gradient(to right, #077d29 25%, #000000 25%); " +
            "-fx-text-fill: white; -fx-font-weight: bold; " +
            "-fx-font-size: 12; -fx-padding: 10 20; -fx-background-radius: 0;"
        );
        
        atualizarLabels();
    }

   @FXML
    private void execSubciclo() {
        
        cpu.executarSubciclo();
        atualizarLabels();
        atualizarBotaoSubciclo();
        
        if ((cpu.getSubcicloAtual() == 1)) 
            imprimirConsoleMPC();
    }

    @FXML
    private void execCicloCompleto() {
        cpu.executarCicloCompleto();
        atualizarLabels();
        atualizarBotaoSubciclo();
        imprimirConsoleMPC();
    }


    @FXML
    private void toggleAutoRun() {
        if (autoRunTimeline != null && autoRunTimeline.getStatus() == Animation.Status.RUNNING) {
            // ── PAUSAR ────────────────────────────────────────────────────────
            autoRunTimeline.stop();
            autoRunTimeline = null;
            btnAutoRun.setText("▶  AUTO RUN");
            btnAutoRun.setStyle(
                "-fx-background-color: #1a5276; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-font-size: 12; -fx-padding: 10 20; -fx-background-radius: 0;"
            );
        } else {
            int delay = spinnerDelay.getValue(); // ms entre cada ciclo
            autoRunTimeline = new Timeline(
                new KeyFrame(Duration.millis(delay), e -> {
                    execSubciclo();
                    atualizarLabels();
                    atualizarBotaoSubciclo();
                    
                    // Atualiza a janela de memória se estiver aberta
                    if (janelaMemoria != null && janelaMemoria.isShowing()) {
                        Object ctrl = janelaMemoria.getScene().getUserData();
                        if (ctrl instanceof SecondaryController) {
                            ((SecondaryController) ctrl).atualizar();
                        }
                    }
                })
            );
            autoRunTimeline.setCycleCount(Timeline.INDEFINITE);
            autoRunTimeline.play();
            btnAutoRun.setText("⏹  PARAR");
            btnAutoRun.setStyle(
                "-fx-background-color: #922b21; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-font-size: 12; -fx-padding: 10 20; -fx-background-radius: 0;"
            );
        }
    }

    // ─── JANELA DE MEMÓRIA ────────────────────────────────────────────────────
    // Abre (ou traz ao foco) uma janela secundária com o dump da RAM.
    // Passa a referência da Memoria para o SecondaryController via setter,
    // evitando acoplamento direto entre controladores.
    @FXML
    private void abrirJanelaMemoria() {
        // Se já está aberta, só traz para frente
        if (janelaMemoria != null && janelaMemoria.isShowing()) {
            janelaMemoria.toFront();
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/simumic/secondary.fxml")
            );
            Parent root = loader.load();

            // Injeta a RAM no SecondaryController
            SecondaryController ctrl = loader.getController();
            ctrl.setMemoria(ram);
            ctrl.atualizar();

            // Guarda referência ao controller na cena para o auto run atualizá-la
            Scene cena = new Scene(root, 900, 600);
            cena.setUserData(ctrl);

            janelaMemoria = new Stage();
            janelaMemoria.setTitle("Inspetor de Memória RAM — SimuMIC");
            janelaMemoria.setScene(cena);
            janelaMemoria.show();

        } catch (IOException e) {
            e.printStackTrace();
            labelAssemblerStatus.setTextFill(COR_VERMELHO);
            labelAssemblerStatus.setText("Erro ao abrir janela de memória.");
        }
    }

private void imprimirConsoleMPC() {
        if (labelMPC != null) {
            String mpcMsg = cpu.getMsgMPC();
            if (mpcMsg.startsWith("MPC 77") || mpcMsg.startsWith("MPC 27")) {
                return; 
            }

            if (mpcMsg.startsWith("MPC 00") && cpu.getInstrucoesExecutadas() != 1) {
                mpcMsg = "\n" + mpcMsg;
            }

            String atual = labelMPC.getText();
            labelMPC.setText(atual.equals("---") ? mpcMsg : atual + "\n" + mpcMsg);
            
            if (scrollMPC != null) scrollMPC.setVvalue(1.0); // Desce a barra de scroll automaticamente
        }
    }
private int atualizarBotaoSubciclo() {
        int proximo = cpu.getSubcicloAtual();
        String[] nomes = {
            "SUBCICLO 1",
            "SUBCICLO 2",
            "SUBCICLO 3",
            "SUBCICLO 4"
        };
        int porcentagem = proximo * 25;
        
        String gradient = String.format(
            "linear-gradient(to right, #077d29 %d%%, #333333 %d%%)", 
            porcentagem, porcentagem
        );

        btnSubciclo.setText(nomes[proximo - 1]);
        btnSubciclo.setStyle(
            "-fx-background-color: " + gradient + "; " +
            "-fx-text-fill: white; -fx-font-weight: bold; " +
            "-fx-font-size: 12; -fx-padding: 10 20; -fx-background-radius: 0;"
        );

        return proximo;
    }

    private Rectangle getRectByIndex(int index) {
        switch(index) {
            case 0: return rectPC;
            case 1: return rectAC;
            case 2: return rectSP;
            case 3: return rectIR;
            case 4: return rectTIR;
            case 5: return rectZERO;
            case 6: return rectP1;
            case 7: return rectM1;
            case 8: return rectAMASK;
            case 9: return rectSMASK;
            case 10: return rectLV; 
            case 11: return rectB;
            case 12: return rectC;
            case 13: return rectD;
            case 14: return rectE;
            case 15: return rectF;
            default: return null;
        }
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
        preencherReg(labelTIR, rectTIR, regs.get("TIR"));
        
        polygonALU.setStroke(COR_CINZA);
        rectShifter.setStroke(COR_CINZA);


        boolean n = cpu.isFlagN();
        boolean z = cpu.isFlagZ();
        labelFlagN.setText(n ? "1" : "0");
        labelFlagZ.setText(z ? "1" : "0");
        rectFlagN.setFill(n ? COR_VERDE : COR_FUNDO); rectFlagN.setStroke(n ? COR_VERDE : COR_CINZA);
        rectFlagZ.setFill(z ? COR_VERDE : COR_FUNDO); rectFlagZ.setStroke(z ? COR_VERDE : COR_CINZA);
        

        rectFlagRD.setFill(COR_FUNDO); rectFlagRD.setStroke(COR_CINZA);
        rectFlagWR.setFill(COR_FUNDO); rectFlagWR.setStroke(COR_CINZA);
        String sinal = cpu.getSinalControle();
        
        if (sinal == "READ") {rectFlagRD.setFill(COR_VERDE);rectFlagRD.setStroke(COR_VERDE);}
        else if (sinal == "WRITE") {rectFlagWR.setFill(COR_VERDE); rectFlagWR.setStroke(COR_VERDE);}


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
        if (rectLatchA != null) rectLatchA.setStroke(COR_CINZA);
        if (rectLatchB != null) rectLatchB.setStroke(COR_CINZA);
        if (rectAMUX != null) rectAMUX.setStroke(COR_CINZA);
        rectDecA.setStroke(COR_CINZA);
        rectDecB.setStroke(COR_CINZA);
        rectDecC.setStroke(COR_CINZA);

        if ("SUB1".equals(status)) {
            // No SUB1, ocorre a leitura dos registradores para os barramentos A e B
            Rectangle rA = getRectByIndex(cpu.getAReg());
            if (rA != null) rA.setStroke(COR_VERDE);
            
            Rectangle rB = getRectByIndex(cpu.getBReg());
            if (rB != null) rB.setStroke(COR_VERDE);
            
            // Se acabou de ler um dado da memória RAM (O MBR é o recetor)
            if ("READ".equals(sinal)) rectMBR.setStroke(COR_VERDE);
            
        } else if ("SUB2".equals(status)) {
            // No SUB2, os Latches seguram os dados, e o AMUX encaminha para a ULA
            if (rectDecA != null) rectDecA.setStroke(COR_VERDE);
            if (rectDecB != null) rectDecB.setStroke(COR_VERDE);
            if (rectLatchA != null) rectLatchA.setStroke(COR_VERDE);
            if (rectLatchB != null) rectLatchB.setStroke(COR_VERDE);
            if (rectAMUX != null) rectAMUX.setStroke(COR_VERDE);
            if (polygonALU != null) polygonALU.setStroke(COR_VERDE);
            if (rectShifter != null) rectShifter.setStroke(COR_VERDE);
            
            // O AMUX decide se a entrada A da ULA vem do Latch A (0) ou do MBR (1)
            if (cpu.getAmuxCtrl() == 1) {
                if (rectMBR != null) rectMBR.setStroke(COR_VERDE);
            }
            
        } else if ("SUB3".equals(status)) {
            if (polygonALU != null) polygonALU.setStroke(COR_CINZA);    
            // No SUB3, os resultados viajam do Barramento C para o MAR e MBR
            if (cpu.getMarCtrl() == 1) rectMAR.setStroke(COR_VERDE);
            if (cpu.getMbrCtrl() == 1) rectMBR.setStroke(COR_VERDE);
            
        } else if ("SUB4".equals(status)) {
            // No SUB4, ocorre a Escrita de volta no Banco de Registradores (Bus C)
            if (rectDecC != null && cpu.getEncCtrl() == 1) rectDecC.setStroke(COR_VERDE);
            if (cpu.getEncCtrl() == 1) {
                Rectangle rC = getRectByIndex(cpu.getCReg());
                if (rC != null) rC.setStroke(COR_VERDE);
            }
            // Se está a escrever os dados finais na RAM
            if ("WRITE".equals(sinal)) {
                rectMAR.setStroke(COR_VERDE);
                rectMBR.setStroke(COR_VERDE);
            }
        }
    
        if (labelEstatisticas != null) {
            labelEstatisticas.setText(cpu.getTotalCiclos() + " Ciclos | " + cpu.getTotalSubciclos() + " Sub");
            labelInstr.setText(cpu.getInstrucoesExecutadas() + " Instr. Executadas");
        }
    }

    private void preencherReg(Label label, Rectangle rect, Integer valor) {
        if (label == null) return;
        int v = (valor == null) ? 0 : valor;
        label.setText(String.format("%04X", v & 0xFFFF));
        rect.setStroke(COR_CINZA);
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