package main.java.com.sisop.software.utilidades;

import main.java.com.sisop.hardware.HW;
import  main.java.com.sisop.hardware.memoria.Word;
import java.util.Scanner;

public class Utilities {
    private HW hw;

    public Utilities(HW _hw) {
        hw = _hw;
    }

    private void loadProgram(Word[] p) {
        Word[] m = hw.mem.pos; // m[] é o array de posições memória do hw
        for (int i = 0; i < p.length; i++) {
            m[i+hw.cpu.pc].opc = p[i].opc;
            m[i+hw.cpu.pc].ra = p[i].ra;
            m[i+hw.cpu.pc].rb = p[i].rb;
            m[i+hw.cpu.pc].p = p[i].p;
        }
    }

    // dump da memória
    public void dump(Word w) { // funcoes de DUMP nao existem em hardware - colocadas aqui para facilidade
        System.out.print("[ ");
        System.out.print(w.opc);
        System.out.print(", ");
        System.out.print(w.ra);
        System.out.print(", ");
        System.out.print(w.rb);
        System.out.print(", ");
        System.out.print(w.p);
        System.out.println("  ] ");
    }

    public void dump(int ini, int fim) {
        Word[] m = hw.mem.pos; // m[] é o array de posições memória do hw
        for (int i = ini; i < fim; i++) {
            System.out.print(i);
            System.out.print(":  ");
            dump(m[i]);
        }
    }

    public void loadAndExec(Word[] p, boolean traceOn) {
        loadProgram(p); // carga do programa na memoria
        System.out.println("PC:"+hw.cpu.pc);
        System.out.println("---------------------------------- programa carregado na memoria");
        dump(hw.cpu.pc, hw.cpu.pc+p.length); // dump da memoria nestas posicoes
        System.out.println("---------------------------------- inicia execucao ");
        hw.cpu.run(traceOn,hw.cpu.pc); // cpu roda programa ate parar
        System.out.println("---------------------------------- memoria após execucao ");
        dump(hw.cpu.pc, p.length); // dump da memoria com resultado
    }
}