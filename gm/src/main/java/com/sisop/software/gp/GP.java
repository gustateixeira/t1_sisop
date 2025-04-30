package com.sisop.software.gp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sisop.programas.Programs.Program;
import com.sisop.software.gm.GM;

public class GP {

    public class PCB {
        private boolean estado; // Se esta executando o
        private long id;
        private static long idCounter = 0;
        private int pc;
        private int memSize;
        private int[] tabelaPaginas;
        private String nome;


        public PCB(){
            this.estado = false;
            this.id = idCounter;
            idCounter++;
        }

        public void setId(long id){
            this.id = id;
        }
        public void setPc(int pc){
            this.pc = pc;
        }
        public void setMemSize(int memSize){
            this.memSize = memSize;
        }
        public void setTabelaPaginas(int [] tabelaPg){
            this.tabelaPaginas = tabelaPg;
        }
        public void setEstado(boolean state){
            this.estado = state;
        }
        public void setNome(String nome){
            this.nome = nome;
        }
        public String toString(){
            return "Nome: " + this.nome +" Id: " + this.id + " Pc:" + this.pc + " memSize: " + this.memSize + " Tabela de paginas: " + Arrays.toString(this.tabelaPaginas);
        }
        public long getId(){
            return this.id;
        }
        public String getNome(){
            return this.nome;
        }
    

    }
    public List<PCB> prontos;
    public List<PCB> rodando; //lista de pcbs
    private GM gm; //gerente de memória

    public GP(GM gm){
        this.gm = gm;
        this.rodando = new ArrayList<>();
        this.prontos = new ArrayList<>();
    }
    

    public boolean criaProcesso(Program p){
    
        int [] tabelaPaginas = new int[p.image.length];
        if(!gm.aloca(p.image.length, tabelaPaginas)){
            return false;
        }

        PCB pcb = new PCB();
        pcb.setPc(0);
        pcb.setTabelaPaginas(tabelaPaginas);
        pcb.setMemSize(p.image.length);
        pcb.setEstado(false);
        pcb.setNome(p.name);
        this.prontos.add(pcb);
        return true;
    }
     
    public void desalocaProcesso(long id){
        PCB desaloc = this.prontos.stream().filter(pcb -> pcb.id == id).findFirst().orElse(null);
        gm.desaloca(desaloc.tabelaPaginas);
        this.prontos.remove(desaloc);
    }

        

}
