package main.java.com.sisop.software.so;

import main.java.com.sisop.hardware.HW;
import main.java.com.sisop.software.gm.GM;
import main.java.com.sisop.software.gp.GP;
import main.java.com.sisop.software.rotinasDeTratamento.InterruptHandling;
import main.java.com.sisop.software.rotinasDeTratamento.SysCallHandling;
import main.java.com.sisop.software.utilidades.Utilities;

public class SO {
    public InterruptHandling ih;
    public SysCallHandling sc;
    public Utilities utils;
    public GM gm;
	public GP gp;


    public SO(HW hw) {
        ih = new InterruptHandling(hw); // rotinas de tratamento de int
        sc = new SysCallHandling(hw); // chamadas de sistema
        hw.cpu.setAddressOfHandlers(ih, sc);
        utils = new Utilities(hw);
        gm = new GM(16,hw.mem.pos.length, hw.mem);
        gp = new GP(hw, gm);

    }
}
