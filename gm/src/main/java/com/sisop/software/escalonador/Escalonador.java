    package com.sisop.software.escalonador;

    import main.java.com.sisop.hardware.HW;
    import main.java.com.sisop.software.rotinasDeTratamento.InterruptHandling;
    import main.java.com.sisop.software.gp.GP;


    public class Escalonador {
        public  int QUANTUM; //numero de operacoes do round-robin
        private int quantumContador = 0; //contador
        private boolean running = true;
        private GP gp;
        private HW hw;

        private InterruptHandling ih = new InterruptHandling(hw, this);
        public Escalonador(int quantum, GP gp, HW hw){
            QUANTUM = quantum;
            this.gp = gp;
            this.hw = hw;
        }
        public void schedule(){

            // Atualizar o PCB do processo em execução com o contexto da CPU
            GP.PCB processoAtual = gp.rodando;
            if (processoAtual != null && !processoAtual.isFinalizado()) {
                processoAtual.setRegistradores(hw.cpu.reg.clone());
                processoAtual.setPc(hw.cpu.pc);
                gp.prontos.add(processoAtual);
            }
            if (gp.prontos.isEmpty()) {
                return;
            }

            // Selecionar o próximo processo da lista de prontos para execução
            GP.PCB proximoProcesso = gp.prontos.remove(0);
            if (proximoProcesso != null) {
                // Atualizar a CPU com o contexto do próximo processo
                hw.cpu.pc = proximoProcesso.pc;
                hw.cpu.reg = proximoProcesso.getRegistradores().clone();
                gp.rodando = proximoProcesso;
                hw.cpu.runFor(QUANTUM,proximoProcesso.base);
                if(!proximoProcesso.isFinalizado()){
                    salvaContexto();
                }
            }

            //printa o processo em execução
            if (gp.rodando == null) return;

        }
        public void salvaContexto(){
            if (gp.rodando != null) {
                gp.rodando.setPc(hw.cpu.pc);
                gp.rodando.setRegistradores(hw.cpu.reg.clone());
                gp.prontos.add(gp.rodando);
                gp.rodando = null;
            }
        }
        public void setFinalizado(){
            this.gp.rodando.setFinalizado(true);
            this.gp.prontos.remove(this.gp.rodando);
        }

    }


