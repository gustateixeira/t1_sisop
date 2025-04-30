package com.sisop.software.gm;
import com.sisop.hardware.cpu.Opcode;
import com.sisop.hardware.memoria.Memory;
import com.sisop.hardware.memoria.Word;

import java.util.Arrays;

public class GM{
    private final int TAMPG;
    private final int FRAMES;
    private final int TAMMEM;
    private final boolean[] OCUPADO; //se ocupado = true, se livre = false
    private int posOcupadas = 0;
    private Memory m;
    
    public GM(int tamPg, int tamMem, Memory m ){
        this.TAMPG = tamPg;
        this.TAMMEM = tamMem;
        this.FRAMES = Math.ceilDiv(TAMMEM,TAMPG);
        this.OCUPADO = new boolean[FRAMES];
        this.m = m;
    }

    public boolean aloca(int nroPalavras, int[] tabelaPaginas){
        if(nroPalavras == 0){
            return false;
        }

        int pagsNecess = 1;

        if(nroPalavras > TAMPG){
            pagsNecess = (int)(nroPalavras/TAMPG) + 1 ;
        }

        int livres = OCUPADO.length - posOcupadas;

        if(pagsNecess > livres){
            return false;
        }
        

        int j = 0;

        for(int i = 0; i < OCUPADO.length; i++){
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
        return true;
    }

    public void desaloca(int[] tabelaPaginas){
        System.out.println("Desalocando");
        if(tabelaPaginas == null){
            return;
        } else {
            System.out.println("Desalocando 0");
            Word[] memo = this.m.pos;
            for (int tabelaPagina : tabelaPaginas) {
                OCUPADO[tabelaPagina] = false;
            }
        }
    }

    public int getTamanhoPagina() {
        return TAMPG;
    }

}
