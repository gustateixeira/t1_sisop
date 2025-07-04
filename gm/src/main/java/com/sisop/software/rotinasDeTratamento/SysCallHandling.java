package main.java.com.sisop.software.rotinasDeTratamento;

import com.sisop.software.escalonador.Escalonador;
import main.java.com.sisop.hardware.HW;

public class SysCallHandling {
		private final HW hw; // referencia ao hw se tiver que setar algo
		private final Escalonador escalonador;
		public SysCallHandling(HW _hw, Escalonador escalonador) {
			hw = _hw;
			this.escalonador = escalonador;
		}

		public void stop() { // chamada de sistema indicando final de programa// nesta versao cpu simplesmente pára
			this.escalonador.setFinalizado();
		}

		public void handle() { // chamada de sistema 
			                   // suporta somente IO, com parametros
							   // reg[8] = in ou out    e reg[9] endereco do inteiro
			System.out.println("SYSCALL pars:  " + hw.cpu.reg[8] + " / " + hw.cpu.reg[9]);

			if  (hw.cpu.reg[8]==1){
				  // leitura ...

			} else if (hw.cpu.reg[8]==2){
				  // escrita - escreve o conteuodo da memoria na posicao dada em reg[9]
				  System.out.println("OUT:   "+ hw.mem.pos[hw.cpu.reg[9]].p);
			} else {System.out.println("  PARAMETRO INVALIDO"); }

		}
	public void handle(boolean trace) { // chamada de sistema

	}
	}
