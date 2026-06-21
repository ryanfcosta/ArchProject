package com.simumic;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;

public class TertiaryController {

    @FXML private Label     labelTituloL1;
    @FXML private Label     labelStatsL1;
    @FXML private ListView<String> listaL1;

    @FXML private Label     labelTituloL2;
    @FXML private Label     labelStatsL2;
    @FXML private ListView<String> listaL2;

    private Cache cacheL1;
    private Cache cacheL2;

    public void setCaches(Cache cacheL1, Cache cacheL2) {
        this.cacheL1 = cacheL1;
        this.cacheL2 = cacheL2;
        if (labelTituloL1 != null) labelTituloL1.setText(cacheL1.getNome());
        if (labelTituloL2 != null) labelTituloL2.setText(cacheL2.getNome());
        configurarCelulas(listaL1);
        configurarCelulas(listaL2);
        atualizar();
    }

    @FXML
    public void atualizar() {
        if (cacheL1 != null) {
            preencher(listaL1, cacheL1);
            labelStatsL1.setText(formatarStats(cacheL1));
        }
        if (cacheL2 != null) {
            preencher(listaL2, cacheL2);
            labelStatsL2.setText(formatarStats(cacheL2));
        }
    }

    private void preencher(ListView<String> lista, Cache cache) {
        ObservableList<String> linhas = FXCollections.observableArrayList();
        int tamBloco = cache.getTamBloco();
        int linhaDestacada = cache.getUltimaLinha();

        for (int linha = 0; linha < cache.getTamCache(); linha++) {
            boolean valido = cache.isValida(linha);
            int tag = cache.getTag(linha);
            int inicio = tag * tamBloco;
            int fim = inicio + tamBloco - 1;

            StringBuilder conteudo = new StringBuilder();
            for (int i = 0; i < tamBloco; i++) {
                conteudo.append(String.format("%04X", cache.getDado(linha, i)));
                if (i < tamBloco - 1) conteudo.append(' ');
            }

            boolean destacada = (linha == linhaDestacada);

            // Formato: linha;V;TAG;rangeBloco;conteudo;destacada
            String item = String.format(
                    "%02d;%d;%s;%04d-%04d;%s;%s",
                    linha,
                    valido ? 1 : 0,
                    valido ? String.valueOf(tag) : "----",
                    inicio, fim,
                    conteudo.toString(),
                    destacada ? "1" : "0"
            );
            linhas.add(item);
        }
        lista.setItems(linhas);
    }

    private void configurarCelulas(ListView<String> lista) {
        lista.setCellFactory(lv -> new ListCell<>() {
            private final HBox hbox = new HBox(12.0);
            private final Label lblLinha = new Label();
            private final Label lblV     = new Label();
            private final Label lblTag   = new Label();
            private final Label lblBloco = new Label();
            private final Label lblDados = new Label();

            {
                lblLinha.setPrefWidth(55.0);
                lblLinha.setAlignment(Pos.CENTER);

                lblV.setPrefWidth(35.0);
                lblV.setAlignment(Pos.CENTER);

                lblTag.setPrefWidth(60.0);
                lblTag.setAlignment(Pos.CENTER);

                lblBloco.setPrefWidth(110.0);
                lblBloco.setAlignment(Pos.CENTER);

                lblDados.setPrefWidth(200.0);
                lblDados.setAlignment(Pos.CENTER);

                hbox.getChildren().addAll(lblLinha, lblV, lblTag, lblBloco, lblDados);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("-fx-background-color: #000; -fx-padding: 2 10 2 10;");
                    return;
                }

                String[] p = item.split(";");
                String linha   = p[0];
                boolean valido = "1".equals(p[1]);
                String tag     = p[2];
                String bloco   = p[3];
                String dados   = p[4];
                boolean destacada = "1".equals(p[5]);

                lblLinha.setText(linha);
                lblV.setText(valido ? "1" : "0");
                lblTag.setText(tag);
                lblBloco.setText(bloco);
                lblDados.setText(dados);

                String corTexto  = valido ? "#d2d5d5" : "#555555";
                String corV      = valido ? "#077d29" : "#555555";
                String cssBase   = "-fx-font-family: Monospaced; -fx-font-size: 12; -fx-text-fill: " + corTexto + ";";

                lblLinha.setStyle(cssBase);
                lblV.setStyle("-fx-font-family: Monospaced; -fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: " + corV + ";");
                lblTag.setStyle("-fx-font-family: Monospaced; -fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: " +
                        (valido ? "#e91e8c" : "#555555") + ";"); // tag em magenta, igual ao slide
                lblBloco.setStyle("-fx-font-family: Monospaced; -fx-font-size: 12; -fx-text-fill: " +
                        (valido ? "#3498db" : "#555555") + ";"); // bloco em azul, igual ao slide
                lblDados.setStyle(cssBase);

                setText(null);
                setGraphic(hbox);

                if (destacada) {
                    setStyle("-fx-background-color: #1a1a1a; -fx-padding: 2 10 2 10; " +
                             "-fx-border-color: #077d29; -fx-border-width: 1 0 1 0;");
                } else {
                    setStyle("-fx-background-color: #000; -fx-padding: 2 10 2 10;");
                }
            }
        });
    }

    private String formatarStats(Cache c) {
        double total = c.getHits() + c.getMisses();
        double percent = (total == 0) ? 0 : (c.getHits() / total) * 100;
        String ultimoHit = "";
        if (c.getUltimoEndereco() >= 0) {
            ultimoHit = String.format("  |  Último endereço: %04d → linha %02d, tag %d (%s)",
                    c.getUltimoEndereco(), c.getUltimaLinha(), c.getUltimoBloco(),
                    c.isUltimoAcessoHit() ? "HIT" : "MISS");
        }
        return String.format("HITS=%d  MISSES=%d  (%.1f%%)%s", c.getHits(), c.getMisses(), percent, ultimoHit);
    }
}
