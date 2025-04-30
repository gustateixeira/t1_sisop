package com.sisop;
import java.awt.im.InputContext;
import java.security.spec.RSAOtherPrimeInfo;
import java.util.Arrays;
import java.util.Scanner;

import com.sisop.hardware.HW;
import com.sisop.programas.Programs;
import com.sisop.software.gm.GM;
import com.sisop.software.gp.GP;
import com.sisop.software.so.SO;
import org.w3c.dom.ls.LSOutput;

// PUCRS - Escola Politécnica - Sistemas Operacionais
// Prof. Fernando Dotti
// Código fornecido como parte da solução do projeto de Sistemas Operacionais
//
// Estrutura deste código:
//    Todo código está dentro da classe *Sistema*
//    Dentro de Sistema, encontra-se acima a definição de HW:
//           Memory,  Word, 
//           CPU tem Opcodes (codigos de operacoes suportadas na cpu),
//               e Interrupcoes possíveis, define o que executa para cada instrucao
//           VM -  a máquina virtual é uma instanciação de CPU e Memória
//    Depois as definições de SW:
//           no momento são esqueletos (so estrutura) para
//					InterruptHandling    e
//					SysCallHandling 
//    A seguir temos utilitários para usar o sistema
//           carga, início de execução e dump de memória
//    Por último os programas existentes, que podem ser copiados em memória.
//           Isto representa programas armazenados.
//    Veja o main.  Ele instancia o Sistema com os elementos mencionados acima.
//           em seguida solicita a execução de algum programa com  loadAndExec

public class Sistema {

	public HW hw;
	public SO so;
	public Programs progs;

	public Sistema(int tamMem) {
		hw = new HW(tamMem);           // memoria do HW tem tamMem palavras
		so = new SO(hw);
		hw.cpu.setUtilities(so.utils); // permite cpu fazer dump de memoria ao avancar
		progs = new Programs();
	}


	public void run() {

        System.out.println("""
                COMANDOS:\
                new <NOME>
                rm <id>\s
                ps
                dump\s
                dumpM
                exec <id>\s
                traceOn\s
                traceOff\s
                exit""");

		Scanner sc = new Scanner(System.in);
        String input;
        do {
            input = sc.next();
            if (input.equals("new")) {
                String name = sc.next();
                System.out.println(name);
                this.so.gp.criaProcesso(Arrays.stream(progs.progs).filter(program -> program.name.equals(name)).findFirst().get());
            }
            if (input.equals("rm")) {
                this.so.gp.desalocaProcesso(sc.nextLong());
            }
            if (input.equals("ps")) {
                System.out.println(this.so.gp.prontos);
            }
            if (input.equals("dump")) {
                int i = sc.nextInt();
                System.out.println(this.so.gp.prontos.get(i - 1));
                so.utils.dump(progs.progs[sc.nextInt()].image[0]);
            }
            if (input.equals("dumpM")) {
                so.utils.dump(sc.nextInt(), sc.nextInt());
            }
            if (input.equals("exec")) {
				int index = sc.nextInt();
				GP.PCB pcb = this.so.gp.prontos.get(index);
				so.utils.loadAndExec(progs.retrieveProgram(pcb.getNome()));
			}

        } while (!input.equals("exit"));

		// so.utils.loadAndExec(progs.retrieveProgram("fatorial"));
		// fibonacci10,
		// fibonacci10v2,
		// progMinimo,
		// fatorialWRITE, // saida
		// fibonacciREAD, // entrada
		// PB
		// PC, // bubble sort
	}

	public static void main(String args[]) {
		Sistema s = new Sistema(1024);
		s.run();

	}

	

}