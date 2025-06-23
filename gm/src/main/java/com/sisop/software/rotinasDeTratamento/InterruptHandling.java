package main.java.com.sisop.software.rotinasDeTratamento;

import com.sisop.software.escalonador.Escalonador;
import main.java.com.sisop.Sistema;
import main.java.com.sisop.hardware.HW;
import main.java.com.sisop.hardware.cpu.Interrupts;

public class InterruptHandling {
    private HW hw;// referencia ao hw se tiver que setar algo
    private final Escalonador escalonador;

    private Sistema s;
    public InterruptHandling(HW _hw, Escalonador escalonador, Sistema s) {
        hw = _hw;
        this.escalonador = escalonador;
        this.s = s;
    }

    public void handle(Interrupts irpt, boolean trace) {
        // apenas avisa - todas interrupcoes neste momento finalizam o programa
        if (trace)
            System.out.println(
                "                                               Interrupcao " + irpt + "   pc: " + hw.cpu.pc);
    }
    public void handle(Interrupts irpt) throws InterruptedException {
        switch (irpt) {
            case intInstrucaoInvalida -> {
            }
            // já existente
            case intEnderecoInvalido -> {
            }
            // já existente
            case intQuantum -> {
                System.out.println(this.escalonador.proximoProcesso);
                        this.escalonador.salvaContexto();
                    s.sem.release();
            }
        }
    }
}
