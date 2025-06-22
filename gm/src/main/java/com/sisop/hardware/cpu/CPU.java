    package main.java.com.sisop.hardware.cpu;


    import main.java.com.sisop.hardware.memoria.Memory;
    import main.java.com.sisop.hardware.memoria.Word;
    import main.java.com.sisop.software.gp.GP;
    import main.java.com.sisop.software.rotinasDeTratamento.InterruptHandling;
    import main.java.com.sisop.software.rotinasDeTratamento.SysCallHandling;
    import main.java.com.sisop.hardware.cpu.Interrupts;
    import main.java.com.sisop.software.utilidades.Utilities;
    import  main.java.com.sisop.hardware.cpu.Opcode;
    import main.java.com.sisop.software.gm.GM;


    import java.util.concurrent.Semaphore;

    public class CPU extends Thread{

        private final int QUANTUM = 5;
        private int maxInt; // valores maximo e minimo para inteiros nesta cpu
        private int minInt;
                            // CONTEXTO da CPU ...
        public int pc;     // ... composto de program counter,
        private Word ir;    // instruction register,
        public int[] reg;  // registradores da CPU
        private Interrupts irpt; // durante instrucao, interrupcao pode ser sinalizada
                            // FIM CONTEXTO DA CPU: tudo que precisa sobre o estado de um processo para
                            // executa-lo
                            // nas proximas versoes isto pode modificar

        private Word[] m;   // m é o array de memória "física", CPU tem uma ref a m para acessar

        private InterruptHandling ih;    // significa desvio para rotinas de tratamento de Int - se int ligada, desvia
        private SysCallHandling sysCall; // significa desvio para tratamento de chamadas de sistema

        private boolean cpuStop;    // flag para parar CPU - caso de interrupcao que acaba o processo, ou chamada stop -
                                    // nesta versao acaba o sistema no fim do prog

                                    // auxilio aa depuração
        private boolean debug;      // se true entao mostra cada instrucao em execucao
        private Utilities u;        // para debug (dump)
        private boolean terminouComStop = false;

        private final Semaphore sem= new Semaphore(0);


        private GM gm;

        public GP.PCB executando;


        public CPU(Memory _mem, boolean _debug) { // ref a MEMORIA passada na criacao da CPU
            maxInt = 32767;            // capacidade de representacao modelada
            minInt = -32767;           // se exceder deve gerar interrupcao de overflow
            m = _mem.pos;              // usa o atributo 'm' para acessar a memoria, só para ficar mais pratico
            reg = new int[10];         // aloca o espaço dos registradores - regs 8 e 9 usados somente para IO
            this.irpt = Interrupts.noInterrupt;
            debug = _debug;

        }
        public void setGm(GM address){
            this.gm = address;
        }

        public void setAddressOfHandlers(InterruptHandling _ih, SysCallHandling _sysCall) {
            ih = _ih;                  // aponta para rotinas de tratamento de int
            sysCall = _sysCall;        // aponta para rotinas de tratamento de chamadas de sistema
        }

        public void setUtilities(Utilities _u) {
            u = _u;                     // aponta para rotinas utilitárias - fazer dump da memória na tela
        }

        private int traduz(int endereco){
            return this.gm.traduzEndereco(endereco, executando.tabelaPaginas);
        }
                                       // verificação de enderecamento
        private boolean legal(int e) { // todo acesso a memoria tem que ser verificado se é válido -
                                       // aqui no caso se o endereco é um endereco valido em toda memoria
            if (e >= 0 && e < m.length) {
                return true;
            } else {
                irpt = Interrupts.intEnderecoInvalido;    // se nao for liga interrupcao no meio da exec da instrucao
                return false;
            }
        }

        private boolean testOverflow(int v) {             // toda operacao matematica deve avaliar se ocorre overflow
            if ((v < minInt) || (v > maxInt)) {
                irpt = Interrupts.intOverflow;            // se houver liga interrupcao no meio da exec da instrucao
                return false;
            }
            ;
            return true;
        }

        public int[] getRegs() {
            return reg.clone();
        }

        public void setRegs(int[] savedRegs) {
            this.reg = savedRegs.clone();
        }

        public int getPC() {
            return pc;
        }

        public void setPC(int newPC) {
            this.pc = newPC;
        }

        public boolean terminou() {
            return terminouComStop;
        }
        public void setIrpt(Interrupts irpt){
            this.irpt = irpt;
        }

        public void runFor() throws InterruptedException {
            terminouComStop = false;
            cpuStop = false;
            setIrpt(Interrupts.noInterrupt);
            int executed = 0;
            int offset = traduz(pc);
            System.out.println("offset: " + offset);
            while (!cpuStop && executed < QUANTUM) {
                   if (legal(pc)) {
                        ir = m[pc];

                        if (debug) {
                            u.dump(ir);
                        }

                        // FASE DE EXECUÇÃO
                        switch (ir.opc) {

                            case LDI:
                                reg[ir.ra] = ir.p;
                                pc++;
                                break;

                            case LDD:
                                if (legal(ir.p)) {
                                    reg[ir.ra] = m[offset + ir.p].p;
                                    pc++;
                                }
                                break;

                            case LDX:
                                if (legal(reg[ir.rb])) {
                                    reg[ir.ra] = m[offset + reg[ir.rb]].p;
                                    pc++;
                                }
                                break;

                            case STD:
                                if (legal(ir.p)) {
                                    m[offset + ir.p].opc = Opcode.DATA;
                                    m[offset + ir.p].p = reg[ir.ra];
                                    pc++;
                                }
                                break;

                            case STX:
                                if (legal(reg[ir.ra])) {
                                    m[offset + reg[ir.ra]].opc = Opcode.DATA;
                                    m[offset + reg[ir.ra]].p = reg[ir.rb];
                                    pc++;
                                }
                                break;

                            case MOVE:
                                reg[ir.ra] = reg[ir.rb];
                                pc++;
                                break;

                            case ADD:
                                reg[ir.ra] = reg[ir.ra] + reg[ir.rb];
                                testOverflow(reg[ir.ra]);
                                pc++;
                                break;

                            case ADDI:
                                reg[ir.ra] = reg[ir.ra] + ir.p;
                                testOverflow(reg[ir.ra]);
                                pc++;
                                break;

                            case SUB:
                                reg[ir.ra] = reg[ir.ra] - reg[ir.rb];
                                testOverflow(reg[ir.ra]);
                                pc++;
                                break;

                            case SUBI:
                                reg[ir.ra] = reg[ir.ra] - ir.p;
                                testOverflow(reg[ir.ra]);
                                pc++;
                                break;

                            case MULT:
                                reg[ir.ra] = reg[ir.ra] * reg[ir.rb];
                                testOverflow(reg[ir.ra]);
                                pc++;
                                break;

                            // Saltos
                            case JMP:
                                pc = offset + ir.p;
                                break;

                            case JMPIM:
                                pc = m[offset + ir.p].p;
                                break;

                            case JMPIG:
                                if (reg[ir.rb] > 0) {
                                    pc = reg[ir.ra];
                                } else {
                                    pc++;
                                }
                                break;

                            case JMPIGK:
                                if (reg[ir.rb] > 0) {
                                    pc = offset + ir.p;
                                } else {
                                    pc++;
                                }
                                break;

                            case JMPILK:
                                if (reg[ir.rb] < 0) {
                                    pc = offset + ir.p;
                                } else {
                                    pc++;
                                }
                                break;

                            case JMPIEK:
                                if (reg[ir.rb] == 0) {
                                    pc = offset + ir.p;
                                } else {
                                    pc++;
                                }
                                break;

                            case JMPIL:
                                if (reg[ir.rb] < 0) {
                                    pc = reg[ir.ra];
                                } else {
                                    pc++;
                                }
                                break;

                            case JMPIE:
                                if (reg[ir.rb] == 0) {
                                    pc = reg[ir.ra];
                                } else {
                                    pc++;
                                }
                                break;

                            case JMPIGM:
                                if (legal(ir.p)) {
                                    if (reg[ir.rb] > 0) {
                                        pc = m[offset + ir.p].p;
                                    } else {
                                        pc++;
                                    }
                                }
                                break;

                            case JMPILM:
                                if (reg[ir.rb] < 0) {
                                    pc = m[offset + ir.p].p;
                                } else {
                                    pc++;
                                }
                                break;

                            case JMPIEM:
                                if (reg[ir.rb] == 0) {
                                    pc = m[offset + ir.p].p;
                                } else {
                                    pc++;
                                }
                                break;

                            case JMPIGT:
                                if (reg[ir.ra] > reg[ir.rb]) {
                                    pc = offset + ir.p;
                                } else {
                                    pc++;
                                }
                                break;

                            case SYSCALL:
                            case STOP:
                                sysCall.stop();
                                terminouComStop = true;
                                cpuStop = true;
                                break;

                            default:
                                setIrpt(Interrupts.intInstrucaoInvalida);
                                break;
                        }
                    }
                    if (irpt != Interrupts.noInterrupt) {
                        System.out.println(irpt);
                        ih.handle(irpt);
                        cpuStop = true;
                        this.sysCall.stop();
                    }
                    System.out.println(pc);
                    executed++;

            }
            if (!terminouComStop && executed >= QUANTUM) {
                setIrpt(Interrupts.intQuantum);
                ih.handle(irpt);
            }

        }
        public void libera()	{
            sem.release();
        }

            public void setContext(int _pc) {                 // usado para setar o contexto da cpu para rodar um processo
            // [ nesta versao é somente colocar o PC na posicao 0 ]
            pc = _pc;                                     // pc cfe endereco logico
            irpt = Interrupts.noInterrupt;                // reset da interrupcao registrada
        }
        public void run(){
            while(true){
                try{
                    this.sem.acquire();
                }catch (InterruptedException e ){
                    continue;
                }finally {
                    try {
                        runFor();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
}
