package main.java.com.sisop.software.gm;
import main.java.com.sisop.hardware.memoria.Memory;
import main.java.com.sisop.hardware.memoria.Word;
import main.java.com.sisop.hardware.cpu.Interrupts;

import java.util.Arrays;

public class GM{
    private final int TAMPG;
    private final int FRAMES;
    private final int TAMMEM;
    private final boolean[] OCUPADO; //se ocupado = true, se livre = false
    private int posOcupadas = 0;
    private Memory m;

    private main.java.com.sisop.hardware.cpu.CPU cpu;

    public GM(int tamPg, int tamMem, Memory m ){
        this.TAMPG = tamPg;
        this.TAMMEM = tamMem;
        this.FRAMES = Math.ceilDiv(TAMMEM,TAMPG);
        this.OCUPADO = new boolean[FRAMES];
        this.m = m;
    }

    public void setCpu(main.java.com.sisop.hardware.cpu.CPU cpu){
        this.cpu = cpu;
    }
    public boolean aloca(int nroPalavras, int[] tabelaPaginas){
        if(nroPalavras == 0){
            return false;
        }

        int pagsNecess = 1;

        if(nroPalavras > TAMPG) {
            if(nroPalavras % TAMPG == 0) {
                pagsNecess = nroPalavras / TAMPG;
            } else {
                pagsNecess = (int) (nroPalavras / TAMPG) + 1;
            }
        }
        int livres = OCUPADO.length - posOcupadas;

        if(pagsNecess > livres){
            return false;
        }
        

        int j = 0;

        for(int i = 0; i < OCUPADO.length-1; i++){
            if(pagsNecess > 0){
                if(!OCUPADO[i]){
                    OCUPADO[i] = true;
                    tabelaPaginas[j] = i;
                    j++;
                    pagsNecess--;
                    posOcupadas++;
                }
            }
        }
        System.out.println(Arrays.toString(tabelaPaginas));
        return true;
    }

    public void desaloca(int[] tabelaPaginas){
        if(tabelaPaginas == null){
            return;
        } else {
            for (int tabelaPagina : tabelaPaginas) {
                OCUPADO[tabelaPagina] = false;
            }
        }
    }

    public int getTamanhoPagina() {
        return TAMPG;
    }

    public int traduzEndereco(int enderecoLogico, int[] tabelaPaginas){
        int entrada = tabelaPaginas[0];

        return entrada * TAMPG;
    }

}
