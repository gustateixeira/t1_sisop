package main.java.com.sisop.software.rotinasDeTratamento;

import com.sisop.software.escalonador.Escalonador;
import main.java.com.sisop.hardware.HW;
import main.java.com.sisop.hardware.cpu.Interrupts;

public class InterruptHandling {
    private HW hw;// referencia ao hw se tiver que setar algo
    private Escalonador escalonador;
    public InterruptHandling(HW _hw, Escalonador escalonador) {
        hw = _hw;
        this.escalonador = escalonador;
    }

    public void handle(Interrupts irpt, boolean trace) {
        // apenas avisa - todas interrupcoes neste momento finalizam o programa
        if (trace)
            System.out.println(
                "                                               Interrupcao " + irpt + "   pc: " + hw.cpu.pc);
    }
    public void handle(Interrupts irpt) {
        switch (irpt) {
            case intInstrucaoInvalida:
                // já existente
                break;

            case intEnderecoInvalido:
                // já existente
                break;

            case intQuantum:
                escalonador.salvaContexto();
                escalonador.schedule(); // troca para próximo processo
                break;
        }
    }
}
