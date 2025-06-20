    package com.sisop.software.escalonador;

    import main.java.com.sisop.hardware.HW;
    import main.java.com.sisop.software.gp.GP;


    public class Escalonador {
        public final int QUANTUM; //numero de operacoes do round-robin
        private int quantumContador = 0; //contador
        private boolean running = true;
        private GP gp;
        public Escalonador(int quantum, GP gp){
            QUANTUM = quantum;
            this.gp = gp;
        }
        public void execAll(HW hw){

            // Atualizar o PCB do processo em execução com o contexto da CPU
            GP.PCB processoAtual = gp.rodando;
            if (processoAtual != null) {
                processoAtual.setRegistradores(hw.cpu.reg);
                processoAtual.setPc(hw.cpu.pc);
                gp.prontos.add(processoAtual);
            }
            if (gp.prontos.isEmpty()) {
                return;
            }

            // Selecionar o próximo processo da lista de prontos para execução
            GP.PCB proximoProcesso = gp.prontos.removeFirst();
            if (proximoProcesso != null) {
                // Atualizar a CPU com o contexto do próximo processo
                hw.cpu.pc = proximoProcesso.pc;
                hw.cpu.reg = proximoProcesso.getRegistradores().clone();

                gp.rodando = proximoProcesso;
            }
            //printa o processo em execução
            if (gp.rodando == null) return;

        }

    }


