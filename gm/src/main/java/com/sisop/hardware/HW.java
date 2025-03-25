package com.sisop.hardware;
import com.sisop.hardware.cpu.CPU;
import com.sisop.hardware.memoria.Memory;



public class HW {
    public Memory mem;
    public CPU cpu;

    public HW(int tamMem) {
        mem = new Memory(tamMem);
        cpu = new CPU(mem, true); // true liga debug
    }
}
