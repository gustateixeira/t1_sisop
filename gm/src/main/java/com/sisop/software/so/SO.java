package com.sisop.software.so;

import com.sisop.hardware.HW;
import com.sisop.software.rotinasDeTratamento.InterruptHandling;
import com.sisop.software.rotinasDeTratamento.SysCallHandling;
import com.sisop.software.utilidades.Utilities;



public class SO {
    public InterruptHandling ih;
    public SysCallHandling sc;
    public Utilities utils;

    public SO(HW hw) {
        ih = new InterruptHandling(hw); // rotinas de tratamento de int
        sc = new SysCallHandling(hw); // chamadas de sistema
        hw.cpu.setAddressOfHandlers(ih, sc);
        utils = new Utilities(hw);
    }
}
