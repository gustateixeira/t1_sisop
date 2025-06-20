package main.java.com.sisop.software.gp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import main.java.com.sisop.hardware.HW;
import main.java.com.sisop.programas.Programs;
import main.java.com.sisop.software.gm.GM;

public class GP {
    private HW hw;

    public class PCB {
        private boolean estado;
        private long id;
        private static long idCounter = 0;
        public int pc;
        public int base;
        private int memSize;
        public int[] tabelaPaginas;
        private String nome;
        private int[] registradores;
        private boolean finalizado = false;

        public PCB() {
            this.estado = false;
            this.id = idCounter++;
        }
        public void atualizaBase(int newBase){this.base = base;}
        public void setPc(int pc) { this.pc = pc; }
        public void setMemSize(int memSize) { this.memSize = memSize; }
        public void setTabelaPaginas(int[] tabelaPg) { this.tabelaPaginas = tabelaPg; }
        public void setEstado(boolean state) { this.estado = state; }
        public void setNome(String nome) { this.nome = nome; }
        public long getId() { return this.id; }
        public String getNome() { return this.nome; }
        public void setRegistradores(int[] r) { this.registradores = r.clone(); }
        public int[] getRegistradores() { return this.registradores; }
        public void setFinalizado(boolean f) { this.finalizado = f; }
        public boolean isFinalizado() { return finalizado; }

        public String toString() {
            return "Nome: " + nome + " Id: " + id + " Pc: " + pc + " memSize: " + memSize +
                    " Tabela de paginas: " + Arrays.toString(tabelaPaginas);
        }
    }

    public List<PCB> prontos;
    public List<PCB> rodando;
    private GM gm;

    public GP(HW hw, GM gm) {
        this.hw = hw;
        this.gm = gm;
        this.rodando = new ArrayList<>();
        this.prontos = new ArrayList<>();
    }

    public boolean criaProcesso(Programs.Program p) {
        int tamPag = gm.getTamanhoPagina();
        int pagsNecessarias = 1;
        System.out.println("tamanho do processo: " + p.image.length);
        System.out.println("tamanho da pagina: " + tamPag);
        if(p.image.length > tamPag){
            pagsNecessarias = (int) Math.ceil((double) p.image.length / tamPag);
            System.out.println("Paginas necessarias: " + pagsNecessarias);
        }
        int[] tabelaPaginas = new int[pagsNecessarias];

        if (!gm.aloca(p.image.length, tabelaPaginas)) {
            return false;
        }

        // Carregar programa nas páginas físicas alocadas
        int indexMemoria = 0;
        for (int i = 0; i < pagsNecessarias; i++) {
            int frameBase = tabelaPaginas[i] * tamPag;
            System.out.println("Carregando página " + i + " na moldura " + tabelaPaginas[i] + " (endereço base: " + frameBase + ")");
            for (int j = 0; j < tamPag && indexMemoria < p.image.length; j++) {
                hw.mem.pos[frameBase + j].opc = p.image[indexMemoria].opc;
                hw.mem.pos[frameBase + j].ra = p.image[indexMemoria].ra;
                hw.mem.pos[frameBase + j].rb = p.image[indexMemoria].rb;
                hw.mem.pos[frameBase + j].p = p.image[indexMemoria].p;
                System.out.println("  Mem[" + (frameBase + j) + "] = " + p.image[indexMemoria].opc + "," +
                        p.image[indexMemoria].ra + "," + p.image[indexMemoria].rb + "," +
                        p.image[indexMemoria].p);
                indexMemoria++;
            }
        }

        PCB pcb = new PCB();
        pcb.setTabelaPaginas(tabelaPaginas);
        pcb.setPc(tamPag*pcb.tabelaPaginas[0]);
        pcb.setMemSize(p.image.length);
        pcb.setEstado(false);
        pcb.setNome(p.name);
        prontos.add(pcb);
        return true;
    }

    public void desalocaProcesso(long id) {

        PCB desaloc = prontos.stream().filter(pcb -> pcb.id == id).findFirst().orElse(null);
        if (desaloc != null) {
            gm.desaloca(desaloc.tabelaPaginas);
            prontos.remove(desaloc);
        }
    }
}
