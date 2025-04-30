package com.sisop.software.rotinasDeTratamento;

import com.sisop.hardware.HW;
import com.sisop.hardware.cpu.Interrupts;

public class InterruptHandling {
    private HW hw; // referencia ao hw se tiver que setar algo

    public InterruptHandling(HW _hw) {
        hw = _hw;
    }

    public void handle(Interrupts irpt) {
        // apenas avisa - todas interrupcoes neste momento finalizam o programa
        System.out.println(
                "                                               Interrupcao " + irpt + "   pc: " + hw.cpu.pc);
    }
}
