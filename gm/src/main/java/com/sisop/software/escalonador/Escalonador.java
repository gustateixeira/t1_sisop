        package com.sisop.software.escalonador;

        import main.java.com.sisop.hardware.HW;
        import main.java.com.sisop.software.rotinasDeTratamento.InterruptHandling;
        import main.java.com.sisop.software.gp.GP;


        public class Escalonador extends Thread {
            public  int QUANTUM; //numero de operacoes do round-robin
            private boolean running = true;
            private GP gp;
            private HW hw;

            public volatile GP.PCB proximoProcesso;
            private main.java.com.sisop.hardware.cpu.CPU cpu;

            private InterruptHandling ih;
            public main.java.com.sisop.Sistema sistema;
            public Escalonador(int quantum, GP gp, HW hw, main.java.com.sisop.Sistema s){
                QUANTUM = quantum;
                this.gp = gp;
                this.hw = hw;
                this.cpu = hw.cpu;
                this.sistema = s;
                this.ih = new InterruptHandling(hw, this, s);
            }
            public void run(){
                while(true) {
                    try{
                    this.sistema.sem.acquire();
                    // Atualizar o PCB do processo em execução com o contexto da CPU
                    GP.PCB processoAtual = gp.rodando;
                    if (processoAtual != null && !processoAtual.isFinalizado()) {
                        processoAtual.setRegistradores(hw.cpu.reg.clone());
                        processoAtual.setPc(hw.cpu.pc);
                    }


                    // Selecionar o próximo processo da lista de prontos para execução
                    this.proximoProcesso = gp.prontos.take();
                    if (this.proximoProcesso != null) {
                        // Atualizar a CPU com o contexto do próximo processo
                        hw.cpu.pc = this.proximoProcesso.pc;
                        hw.cpu.reg = this.proximoProcesso.getRegistradores().clone();
                        gp.rodando = this.proximoProcesso;
                        hw.cpu.executando = this.proximoProcesso;
                        this.cpu.libera();
                    }
                    }catch (InterruptedException e){
                        break;
                    }
                }
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


