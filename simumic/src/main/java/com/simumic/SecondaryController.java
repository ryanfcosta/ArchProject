package com.simumic;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;


public class SecondaryController {

    @FXML private ListView<String>  listaMemoria;
    @FXML private CheckBox          chkSoNaoZero;
    @FXML private TextField         txtBusca;
    @FXML private TextField         txtRangeInicio;
    @FXML private TextField         txtRangeFim;
    @FXML private Label             labelTotalExibido;
    @FXML private Label             labelEnderecoAtual;

    private Memoria memoria; // ram compartilhada

    private int  filtroInicio = 0;
    private int  filtroFim    = 4095;
    private boolean soNaoZero = true;

    private static final String[] MNEMONICOS = {
        "LODD","STOD","ADDD","SUBD","JPOS","JZER","JUMP","LOCO",
        "LODL","STOL","ADDL","SUBL","JNEG","JNZE","CALL","(ext)"
    };

    public void setMemoria(Memoria memoria) {
        this.memoria = memoria;
    }

    @FXML
    public void atualizar() {
        if (memoria == null) return;
        soNaoZero = chkSoNaoZero.isSelected();
        carregarLista(filtroInicio, filtroFim, soNaoZero);
    }

    @FXML
    private void aplicarFiltro() {
        soNaoZero = chkSoNaoZero.isSelected();

        try {
            String si = txtRangeInicio.getText().trim();
            String sf = txtRangeFim.getText().trim();
            filtroInicio = si.isEmpty() ? 0    : Integer.parseInt(si);
            filtroFim    = sf.isEmpty() ? 4095 : Integer.parseInt(sf);
            // Clamp
            filtroInicio = Math.max(0,    Math.min(4095, filtroInicio));
            filtroFim    = Math.max(0,    Math.min(4095, filtroFim));
            if (filtroInicio > filtroFim) { int tmp = filtroInicio; filtroInicio = filtroFim; filtroFim = tmp; }
        } catch (NumberFormatException e) {
            filtroInicio = 0; filtroFim = 4095;
        }

        carregarLista(filtroInicio, filtroFim, soNaoZero);
    }


    @FXML
    private void limparFiltro() {
        txtRangeInicio.clear();
        txtRangeFim.clear();
        chkSoNaoZero.setSelected(false);
        filtroInicio = 0;
        filtroFim    = 4095;
        soNaoZero    = false;
        carregarLista(0, 4095, false);
    }


    @FXML
    private void irParaEndereco() {
        try {
            int end = Integer.parseInt(txtBusca.getText().trim());
            end = Math.max(0, Math.min(4095, end));
            // Destaca o endereço no rodapé
            int valor = memoria.read(end);
            labelEnderecoAtual.setText(
                String.format("► [%04d]  %04X  %s", end, valor, decodificarMnemonico(valor))
            );
            // Seleciona a linha correspondente na lista (se estiver visível)
            ObservableList<String> items = listaMemoria.getItems();
            String prefixo = String.format("[%04d]", end);
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i).startsWith(prefixo)) {
                    listaMemoria.scrollTo(i);
                    listaMemoria.getSelectionModel().select(i);
                    break;
                }
            }
        } catch (NumberFormatException e) {
            labelEnderecoAtual.setText("Endereço inválido.");
        }
    }


    private void carregarLista(int inicio, int fim, boolean apenasNaoZero) {
        ObservableList<String> linhas = FXCollections.observableArrayList();

        for (int i = inicio; i <= fim; i++) {
            int valor = memoria.read(i);
            if (apenasNaoZero && valor == 0) continue;

            // Formato: [endereço decimal] | HEX | BINÁRIO | DECIMAL | MNEMÔNICO
            String linha = String.format(
                "[%04d]  %04X  %s  %5d  %s",
                i,
                valor,
                toBin16(valor),
                valor,
                decodificarMnemonico(valor)
            );
            linhas.add(linha);
        }

        listaMemoria.setItems(linhas);

        // Estiliza células: endereços com valor não-zero ficam em verde
        listaMemoria.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("-fx-background-color: #000;");
                } else {
                    setText(item);
                    // Extrai hex da célula para saber se é zero
                    boolean ehZero = item.contains("  0000  ");
                    setStyle(
                        "-fx-background-color: #000; " +
                        "-fx-text-fill: " + (ehZero ? "#555555" : "#d2d5d5") + "; " +
                        "-fx-font-family: Monospaced; -fx-font-size: 12;"
                    );
                }
            }
        });

        labelTotalExibido.setText("Exibindo " + linhas.size() + " entrada(s)");
    }


    private String toBin16(int valor) {
        String b = String.format("%16s", Integer.toBinaryString(valor & 0xFFFF))
                         .replace(' ', '0');
        // Agrupa em 4 bits para facilitar leitura: "0000 0000 0000 0000"
        return b.substring(0,4) + " " + b.substring(4,8) + " "
             + b.substring(8,12) + " " + b.substring(12,16);
    }


    private String decodificarMnemonico(int valor) {
        int opcode4 = (valor >> 12) & 0xF;
        if (opcode4 != 0xF) {
            int arg = valor & 0x0FFF;
            return String.format("%-4s %d", MNEMONICOS[opcode4], arg);
        }
        // Família estendida — bits 11..9
        int sub = (valor >> 9) & 0x7;
        String[] ext = {"PSHI","POPI","PUSH","POP ","RETN","SWAP","INSP","DESP"};
        int arg8 = valor & 0xFF;
        if (sub >= 6) return ext[sub] + " " + arg8;  // INSP/DESP têm arg de 8 bits
        return ext[sub];
    }
}