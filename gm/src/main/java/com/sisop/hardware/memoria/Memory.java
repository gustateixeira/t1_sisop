package main.java.com.sisop.hardware.memoria;

import main.java.com.sisop.hardware.cpu.Opcode;

public class Memory {
		public main.java.com.sisop.hardware.memoria.Word[] pos; // pos[i] é a posição i da memória. cada posição é uma palavra.

		public Memory(int size) {
			pos = new Word[size];
			for (int i = 0; i < pos.length; i++) {
				pos[i] = new Word(Opcode.___, -1, -1, -1);
			}
			 // cada posicao da memoria inicializada
		}
	
	}


