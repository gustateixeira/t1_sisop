package main.java.com.sisop.software.so;

import com.sisop.software.escalonador.Escalonador;
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
    public Escalonador escalonador;
    public GM gm;
    private int quantum= 5;
	public GP gp;


    public SO(HW hw, main.java.com.sisop.Sistema s) {
         // rotinas de tratamento de int
        utils = new Utilities(hw);
        gm = new GM(16,hw.mem.pos.length, hw.mem);
        gp = new GP(hw, gm);
        this.escalonador = new Escalonador(this.quantum, this.gp,hw,s);
        ih = new InterruptHandling(hw,this.escalonador, s);
        sc = new SysCallHandling(hw, this.escalonador); // chamadas de sistema
        hw.cpu.setAddressOfHandlers(ih, sc);



    }
}
