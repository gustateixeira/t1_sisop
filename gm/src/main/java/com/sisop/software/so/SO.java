package com.sisop.software.so;

import com.sisop.hardware.HW;
import com.sisop.software.gm.GM;
import com.sisop.software.gp.GP;
import com.sisop.software.rotinasDeTratamento.InterruptHandling;
import com.sisop.software.rotinasDeTratamento.SysCallHandling;
import com.sisop.software.utilidades.Utilities;

public class SO {
    public InterruptHandling ih;
    public SysCallHandling sc;
    public Utilities utils;
    public GM gm;
    public GP gp;

    public SO(HW hw) {
        gm = new GM(128, hw.mem.pos.length);       // Inicializa GM
        gp = new GP(gm);                           // Inicializa GP com GM
        ih = new InterruptHandling(hw.cpu, gp);    // Corrigido: passa CPU e GP
        sc = new SysCallHandling(hw);              // Chamadas de sistema
        hw.cpu.setAddressOfHandlers(ih, sc);       // Seta os tratadores
        utils = new Utilities(hw);                 // Utilidades
    }
}
