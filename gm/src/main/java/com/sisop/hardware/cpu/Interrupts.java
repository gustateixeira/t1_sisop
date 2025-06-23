package main.java.com.sisop.hardware.cpu;

public enum Interrupts {           // possiveis interrupcoes que esta CPU gera
    noInterrupt, intEnderecoInvalido, intInstrucaoInvalida, intOverflow, intSTOP, intQuantum;
}
