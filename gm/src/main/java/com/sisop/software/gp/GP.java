package com.sisop.software.gp;
import java.util.ArrayList;
import java.util.List;

import com.sisop.programas.Programs.Program;
import com.sisop.software.gm.GM;

public class GP {

    private class PCB {
        private boolean estado; // Se esta executando o
        private long id;
        private int pc;
        private int memSize;
        private int[] tabelaPaginas;
    


        public PCB(){
            this.estado = false;
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

        
    

    }
    private List<Long> ids;
    private  long id;
    private List<PCB> prontos;
    private List<PCB> rodando; //lista de pcbs
    private GM gm; //gerente de memória

    public GP(GM gm){
        this.gm = gm;
        this.rodando = new ArrayList<>();
        this.prontos = new ArrayList<>();
        this.ids = new ArrayList<>();
        this.id = 0;
    }
    

    public boolean criaProcesso(Program p){
    
        int [] tabelaPaginas = new int[p.image.length];
        if(!gm.aloca(p.image.length, tabelaPaginas)){
            return false;
        }

        PCB pcb = new PCB();
        pcb.setId(id);
        while(ids.contains(id)){
            id++;
        }
        ids.add(id);
        pcb.setPc(0);
        pcb.setTabelaPaginas(tabelaPaginas);
        pcb.setMemSize(p.image.length);
        pcb.setEstado(false);
        this.prontos.add(pcb);

        return true;
    }
     
    public void desalocaProcesso(long id){
        PCB desaloc = this.prontos.stream().filter(pcb -> pcb.id == id).findFirst().orElse(null);
        gm.desaloca(desaloc.tabelaPaginas);
        ids.remove(id);
        this.id = id;
    }

        

}
