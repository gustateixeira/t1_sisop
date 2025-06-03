package main.java.com.sisop.hardware;

import main.java.com.sisop.hardware.cpu.CPU;
import main.java.com.sisop.hardware.memoria.Memory;


public class HW {
    public Memory mem;
    public CPU cpu;

    public HW(int tamMem) {
        mem = new Memory(tamMem);
        cpu = new CPU(mem, true); // true liga debug
    }
}
