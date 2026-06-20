package com.simumic;

public class Cache implements AcessoMemoria {
    private final int[] tags;
    private final int[][] dados;
    private final boolean[] validos;

    private final int tamCache;
    private final int tamBloco;
    private final AcessoMemoria proximoNivel;

    public Cache(AcessoMemoria proximoNivel, int tamCache, int tamBloco) {
        if (tamCache <= 0 || tamBloco <= 0) {
            throw new IllegalArgumentException("Tamanhos da cache devem ser positivos.");
        }

        this.proximoNivel = proximoNivel;
        this.tamCache = tamCache;
        this.tamBloco = tamBloco;
        this.dados = new int[tamCache][tamBloco];
        this.tags = new int[tamCache];
        this.validos = new boolean[tamCache];
    }

    @Override
    public int read(int endereco) {
        if (endereco < 0) {
            return 0;
        }

        int bloco = endereco / tamBloco;
        int linha = linhaDoBloco(bloco);
        int offset = endereco % tamBloco;

        if (!validos[linha] || tags[linha] != bloco) {
            carregarBloco(bloco, linha);
        }

        return dados[linha][offset] & 0xFFFF;
    }

    @Override
    public void write(int endereco, int dado) {
        if (endereco < 0) {
            return;
        }

        int valor = dado & 0xFFFF;
        int bloco = endereco / tamBloco;
        int linha = linhaDoBloco(bloco);
        int offset = endereco % tamBloco;

        if (!validos[linha] || tags[linha] != bloco) {
            carregarBloco(bloco, linha);
        }

        dados[linha][offset] = valor;
        proximoNivel.write(endereco, valor);
    }

    private int linhaDoBloco(int bloco) {
        return bloco % tamCache;
    }

    private void carregarBloco(int bloco, int linha) {
        int inicioBloco = bloco * tamBloco;
        for (int i = 0; i < tamBloco; i++) {
            dados[linha][i] = proximoNivel.read(inicioBloco + i) & 0xFFFF;
        }
        tags[linha] = bloco;
        validos[linha] = true;
    }
}