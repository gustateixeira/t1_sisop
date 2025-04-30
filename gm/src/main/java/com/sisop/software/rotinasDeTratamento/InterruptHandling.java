package com.sisop.software.rotinasDeTratamento;

import com.sisop.hardware.cpu.CPU;
import com.sisop.hardware.cpu.Interrupts;
import com.sisop.software.gp.GP;
import com.sisop.software.gp.GP.PCB;

public class InterruptHandling {

    private CPU cpu;
    private GP gp;

    public InterruptHandling(CPU cpu, GP gp) {
        this.cpu = cpu;
        this.gp = gp;
    }

    public void handle(Interrupts irpt) {
        switch (irpt) {
            case INT_CLOCK:
                // salva contexto do processo rodando
                PCB current = gp.rodando.isEmpty() ? null : gp.rodando.remove(0);
                if (current != null) {
                    current.setRegContext(cpu.reg.clone()); // <-- AQUI
                    current.setPcContext(cpu.pc);
                    gp.prontos.add(current);
                }
                

                // escolhe novo processo
                if (!gp.prontos.isEmpty()) {
                    PCB next = gp.prontos.remove(0);
                    cpu.reg = next.getRegContext().clone(); // <-- AQUI
                    cpu.setContext(next.getPcContext());
                    gp.rodando.add(next);
                }                
                break;

            case intEnderecoInvalido:
                System.out.println("Erro: endereço inválido.");
                break;

            case intInstrucaoInvalida:
                System.out.println("Erro: instrução inválida.");
                break;

            case intOverflow:
                System.out.println("Erro: overflow.");
                break;

            default:
                System.out.println("Interrupção desconhecida.");
        }
    }
}
