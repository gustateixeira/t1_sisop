package main.java.com.sisop.hardware.cpu;


import main.java.com.sisop.hardware.memoria.Memory;
import main.java.com.sisop.hardware.memoria.Word;
import main.java.com.sisop.software.rotinasDeTratamento.InterruptHandling;
import main.java.com.sisop.software.rotinasDeTratamento.SysCallHandling;
import main.java.com.sisop.software.utilidades.Utilities;

public class CPU {
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


    public CPU(Memory _mem, boolean _debug) { // ref a MEMORIA passada na criacao da CPU
        maxInt = 32767;            // capacidade de representacao modelada
        minInt = -32767;           // se exceder deve gerar interrupcao de overflow
        m = _mem.pos;              // usa o atributo 'm' para acessar a memoria, só para ficar mais pratico
        reg = new int[10];         // aloca o espaço dos registradores - regs 8 e 9 usados somente para IO

        debug = _debug;            // se true, print da instrucao em execucao

    }

    public void setAddressOfHandlers(InterruptHandling _ih, SysCallHandling _sysCall) {
        ih = _ih;                  // aponta para rotinas de tratamento de int
        sysCall = _sysCall;        // aponta para rotinas de tratamento de chamadas de sistema
    }

    public void setUtilities(Utilities _u) {
        u = _u;                     // aponta para rotinas utilitárias - fazer dump da memória na tela
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

    public void setContext(int _pc) {                 // usado para setar o contexto da cpu para rodar um processo
                                                      // [ nesta versao é somente colocar o PC na posicao 0 ]
        pc = _pc;                                     // pc cfe endereco logico
        irpt = Interrupts.noInterrupt;                // reset da interrupcao registrada
    }

    public void run(boolean traceOn) {                               // execucao da CPU supoe que o contexto da CPU, vide acima,
                                                          // esta devidamente setado
        terminouComStop = false;
        cpuStop = false;
        if(!traceOn){
            while (!cpuStop) {      // ciclo de instrucoes. acaba cfe resultado da exec da instrucao, veja cada caso.

                // --------------------------------------------------------------------------------------------------
                // FASE DE FETCH
                if (legal(pc)) { // pc valido
                    ir = m[pc];  // <<<<<<<<<<<< AQUI faz FETCH - busca posicao da memoria apontada por pc, guarda em ir
                    // resto é dump de debug
                    if (debug) {
                        u.dump(ir);
                    }

                    // --------------------------------------------------------------------------------------------------
                    // FASE DE EXECUCAO DA INSTRUCAO CARREGADA NO ir
                    switch (ir.opc) {       // conforme o opcode (código de operação) executa

                        // Instrucoes de Busca e Armazenamento em Memoria
                        case LDI: // Rd ← k        veja a tabela de instrucoes do HW simulado para entender a semantica da instrucao
                            reg[ir.ra] = ir.p;
                            pc++;
                            break;
                        case LDD: // Rd <- [A]
                            if (legal(ir.p)) {
                                reg[ir.ra] = m[ir.p].p;
                                pc++;
                            }
                            break;
                        case LDX: // RD <- [RS] // NOVA
                            if (legal(reg[ir.rb])) {
                                reg[ir.ra] = m[reg[ir.rb]].p;
                                pc++;
                            }
                            break;
                        case STD: // [A] ← Rs
                            if (legal(ir.p)) {
                                m[ir.p].opc = Opcode.DATA;
                                m[ir.p].p = reg[ir.ra];
                                pc++;
                            }
                            break;
                        case STX: // [Rd] ←Rs
                            if (legal(reg[ir.ra])) {
                                m[reg[ir.ra]].opc = Opcode.DATA;
                                m[reg[ir.ra]].p = reg[ir.rb];
                                pc++;
                            }
                            ;
                            break;
                        case MOVE: // RD <- RS
                            reg[ir.ra] = reg[ir.rb];
                            pc++;
                            break;
                        // Instrucoes Aritmeticas
                        case ADD: // Rd ← Rd + Rs
                            reg[ir.ra] = reg[ir.ra] + reg[ir.rb];
                            testOverflow(reg[ir.ra]);
                            pc++;
                            break;
                        case ADDI: // Rd ← Rd + k
                            reg[ir.ra] = reg[ir.ra] + ir.p;
                            testOverflow(reg[ir.ra]);
                            pc++;
                            break;
                        case SUB: // Rd ← Rd - Rs
                            reg[ir.ra] = reg[ir.ra] - reg[ir.rb];
                            testOverflow(reg[ir.ra]);
                            pc++;
                            break;
                        case SUBI: // RD <- RD - k // NOVA
                            reg[ir.ra] = reg[ir.ra] - ir.p;
                            testOverflow(reg[ir.ra]);
                            pc++;
                            break;
                        case MULT: // Rd <- Rd * Rs
                            reg[ir.ra] = reg[ir.ra] * reg[ir.rb];
                            testOverflow(reg[ir.ra]);
                            pc++;
                            break;

                        // Instrucoes JUMP
                        case JMP: // PC <- k
                            pc = ir.p;
                            break;
                        case JMPIM: // PC <- [A]
                            pc = m[ir.p].p;
                            break;
                        case JMPIG: // If Rc > 0 Then PC ← Rs Else PC ← PC +1
                            if (reg[ir.rb] > 0) {
                                pc = reg[ir.ra];
                            } else {
                                pc++;
                            }
                            break;
                        case JMPIGK: // If RC > 0 then PC <- k else PC++
                            if (reg[ir.rb] > 0) {
                                pc = ir.p;
                            } else {
                                pc++;
                            }
                            break;
                        case JMPILK: // If RC < 0 then PC <- k else PC++
                            if (reg[ir.rb] < 0) {
                                pc = ir.p;
                            } else {
                                pc++;
                            }
                            break;
                        case JMPIEK: // If RC = 0 then PC <- k else PC++
                            if (reg[ir.rb] == 0) {
                                pc = ir.p;
                            } else {
                                pc++;
                            }
                            break;
                        case JMPIL: // if Rc < 0 then PC <- Rs Else PC <- PC +1
                            if (reg[ir.rb] < 0) {
                                pc = reg[ir.ra];
                            } else {
                                pc++;
                            }
                            break;
                        case JMPIE: // If Rc = 0 Then PC <- Rs Else PC <- PC +1
                            if (reg[ir.rb] == 0) {
                                pc = reg[ir.ra];
                            } else {
                                pc++;
                            }
                            break;
                        case JMPIGM: // If RC > 0 then PC <- [A] else PC++
                            if (legal(ir.p)){
                                if (reg[ir.rb] > 0) {
                                    pc = m[ir.p].p;
                                } else {
                                    pc++;
                                }
                            }
                            break;
                        case JMPILM: // If RC < 0 then PC <- k else PC++
                            if (reg[ir.rb] < 0) {
                                pc = m[ir.p].p;
                            } else {
                                pc++;
                            }
                            break;
                        case JMPIEM: // If RC = 0 then PC <- k else PC++
                            if (reg[ir.rb] == 0) {
                                pc = m[ir.p].p;
                            } else {
                                pc++;
                            }
                            break;
                        case JMPIGT: // If RS>RC then PC <- k else PC++
                            if (reg[ir.ra] > reg[ir.rb]) {
                                pc = ir.p;
                            } else {
                                pc++;
                            }
                            break;

                        case DATA: // pc está sobre área supostamente de dados
                            irpt = Interrupts.intInstrucaoInvalida;
                            break;

                        // Chamadas de sistema
                        case SYSCALL:
                            sysCall.handle(); // <<<<< aqui desvia para rotina de chamada de sistema, no momento so
                            // temos IO
                            pc++;
                            break;

                        case STOP:
                            sysCall.stop();
                            terminouComStop = true;
                            cpuStop = true;
                            break;

                        // Inexistente
                        default:
                            irpt = Interrupts.intInstrucaoInvalida;
                            break;
                    }
                }
                // --------------------------------------------------------------------------------------------------
                // VERIFICA INTERRUPÇÃO !!! - TERCEIRA FASE DO CICLO DE INSTRUÇÕES
                if (irpt != Interrupts.noInterrupt) { // existe interrupção
                    ih.handle(irpt);                  // desvia para rotina de tratamento - esta rotina é do SO
                    cpuStop = true;                   // nesta versao, para a CPU
                }
            }
        }
        if(traceOn){
            while (!cpuStop) {      // ciclo de instrucoes. acaba cfe resultado da exec da instrucao, veja cada caso.

                // --------------------------------------------------------------------------------------------------
                // FASE DE FETCH
                if (legal(pc)) { // pc valido
                    ir = m[pc];  // <<<<<<<<<<<< AQUI faz FETCH - busca posicao da memoria apontada por pc, guarda em ir
                                 // resto é dump de debug
                    if (debug) {
                        System.out.print("                                              regs: ");
                        for (int i = 0; i < 10; i++) {
                            System.out.print(" r[" + i + "]:" + reg[i]);
                        }
                        ;
                        System.out.println();
                    }
                    if (debug) {
                        System.out.print("                      pc: " + pc + "       exec: ");
                        u.dump(ir);
                    }

                // --------------------------------------------------------------------------------------------------
                // FASE DE EXECUCAO DA INSTRUCAO CARREGADA NO ir
                    switch (ir.opc) {       // conforme o opcode (código de operação) executa

                        // Instrucoes de Busca e Armazenamento em Memoria
                        case LDI: // Rd ← k        veja a tabela de instrucoes do HW simulado para entender a semantica da instrucao
                            reg[ir.ra] = ir.p;
                            pc++;
                            break;
                        case LDD: // Rd <- [A]
                            if (legal(ir.p)) {
                                reg[ir.ra] = m[ir.p].p;
                                pc++;
                            }
                            break;
                        case LDX: // RD <- [RS] // NOVA
                            if (legal(reg[ir.rb])) {
                                reg[ir.ra] = m[reg[ir.rb]].p;
                                pc++;
                            }
                            break;
                        case STD: // [A] ← Rs
                            if (legal(ir.p)) {
                                m[ir.p].opc = Opcode.DATA;
                                m[ir.p].p = reg[ir.ra];
                                pc++;
                                if (debug)
                                    {   System.out.print("                                  ");
                                        u.dump(ir.p,ir.p+1);
                                    }
                                }
                            break;
                        case STX: // [Rd] ←Rs
                            if (legal(reg[ir.ra])) {
                                m[reg[ir.ra]].opc = Opcode.DATA;
                                m[reg[ir.ra]].p = reg[ir.rb];
                                pc++;
                            }
                            ;
                            break;
                        case MOVE: // RD <- RS
                            reg[ir.ra] = reg[ir.rb];
                            pc++;
                            break;
                        // Instrucoes Aritmeticas
                        case ADD: // Rd ← Rd + Rs
                            reg[ir.ra] = reg[ir.ra] + reg[ir.rb];
                            testOverflow(reg[ir.ra]);
                            pc++;
                            break;
                        case ADDI: // Rd ← Rd + k
                            reg[ir.ra] = reg[ir.ra] + ir.p;
                            testOverflow(reg[ir.ra]);
                            pc++;
                            break;
                        case SUB: // Rd ← Rd - Rs
                            reg[ir.ra] = reg[ir.ra] - reg[ir.rb];
                            testOverflow(reg[ir.ra]);
                            pc++;
                            break;
                        case SUBI: // RD <- RD - k // NOVA
                            reg[ir.ra] = reg[ir.ra] - ir.p;
                            testOverflow(reg[ir.ra]);
                            pc++;
                            break;
                        case MULT: // Rd <- Rd * Rs
                            reg[ir.ra] = reg[ir.ra] * reg[ir.rb];
                            testOverflow(reg[ir.ra]);
                            pc++;
                            break;

                        // Instrucoes JUMP
                        case JMP: // PC <- k
                            pc = ir.p;
                            break;
                        case JMPIM: // PC <- [A]
                                  pc = m[ir.p].p;
                            break;
                        case JMPIG: // If Rc > 0 Then PC ← Rs Else PC ← PC +1
                            if (reg[ir.rb] > 0) {
                                pc = reg[ir.ra];
                            } else {
                                pc++;
                            }
                            break;
                        case JMPIGK: // If RC > 0 then PC <- k else PC++
                            if (reg[ir.rb] > 0) {
                                pc = ir.p;
                            } else {
                                pc++;
                            }
                            break;
                        case JMPILK: // If RC < 0 then PC <- k else PC++
                            if (reg[ir.rb] < 0) {
                                pc = ir.p;
                            } else {
                                pc++;
                            }
                            break;
                        case JMPIEK: // If RC = 0 then PC <- k else PC++
                            if (reg[ir.rb] == 0) {
                                pc = ir.p;
                            } else {
                                pc++;
                            }
                            break;
                        case JMPIL: // if Rc < 0 then PC <- Rs Else PC <- PC +1
                            if (reg[ir.rb] < 0) {
                                pc = reg[ir.ra];
                            } else {
                                pc++;
                            }
                            break;
                        case JMPIE: // If Rc = 0 Then PC <- Rs Else PC <- PC +1
                            if (reg[ir.rb] == 0) {
                                pc = reg[ir.ra];
                            } else {
                                pc++;
                            }
                            break;
                        case JMPIGM: // If RC > 0 then PC <- [A] else PC++
                            if (legal(ir.p)){
                                if (reg[ir.rb] > 0) {
                                   pc = m[ir.p].p;
                                } else {
                                  pc++;
                               }
                            }
                            break;
                        case JMPILM: // If RC < 0 then PC <- k else PC++
                            if (reg[ir.rb] < 0) {
                                pc = m[ir.p].p;
                            } else {
                                pc++;
                            }
                            break;
                        case JMPIEM: // If RC = 0 then PC <- k else PC++
                            if (reg[ir.rb] == 0) {
                                pc = m[ir.p].p;
                            } else {
                                pc++;
                            }
                            break;
                        case JMPIGT: // If RS>RC then PC <- k else PC++
                            if (reg[ir.ra] > reg[ir.rb]) {
                                pc = ir.p;
                            } else {
                                pc++;
                            }
                            break;

                        case DATA: // pc está sobre área supostamente de dados
                            irpt = Interrupts.intInstrucaoInvalida;
                            break;

                        // Chamadas de sistema
                        case SYSCALL:
                            sysCall.handle(); // <<<<< aqui desvia para rotina de chamada de sistema, no momento so
                                                // temos IO
                            pc++;
                            break;

                        case STOP:
                            sysCall.stop();
                            terminouComStop = true;
                            cpuStop = true;
                            break;

                        // Inexistente
                        default:
                            irpt = Interrupts.intInstrucaoInvalida;
                            break;
                    }
                }
                // --------------------------------------------------------------------------------------------------
                // VERIFICA INTERRUPÇÃO !!! - TERCEIRA FASE DO CICLO DE INSTRUÇÕES
                if (irpt != Interrupts.noInterrupt) { // existe interrupção
                    ih.handle(irpt);                  // desvia para rotina de tratamento - esta rotina é do SO
                    cpuStop = true;                   // nesta versao, para a CPU
                }
        }
    } // FIM DO CICLO DE UMA INSTRUÇÃO
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

    public void runFor(int delta, boolean trace) {
        terminouComStop = false;
        cpuStop = false;
        int executed = 0;
        if (trace) {
            while (!cpuStop && executed < delta) {
                if (legal(pc)) {
                    ir = m[pc];

                    if (debug) {
                        System.out.print("                                              regs: ");
                        for (int i = 0; i < 10; i++) {
                            System.out.print(" r[" + i + "]:" + reg[i]);
                        }
                        System.out.println();
                        System.out.print("                      pc: " + pc + "       exec: ");
                        u.dump(ir);
                    }

                    switch (ir.opc) {
                        case LDI:
                            reg[ir.ra] = ir.p;
                            pc++;
                            break;
                        case LDD:
                            if (legal(ir.p)) {
                                reg[ir.ra] = m[ir.p].p;
                                pc++;
                            }
                            break;
                        case LDX:
                            if (legal(reg[ir.rb])) {
                                reg[ir.ra] = m[reg[ir.rb]].p;
                                pc++;
                            }
                            break;
                        case STD:
                            if (legal(ir.p)) {
                                m[ir.p].opc = Opcode.DATA;
                                m[ir.p].p = reg[ir.ra];
                                pc++;
                            }
                            break;
                        case STX:
                            if (legal(reg[ir.ra])) {
                                m[reg[ir.ra]].opc = Opcode.DATA;
                                m[reg[ir.ra]].p = reg[ir.rb];
                                pc++;
                            }
                            break;
                        case MOVE:
                            reg[ir.ra] = reg[ir.rb];
                            pc++;
                            break;
                        case ADD:
                            reg[ir.ra] += reg[ir.rb];
                            testOverflow(reg[ir.ra]);
                            pc++;
                            break;
                        case ADDI:
                            reg[ir.ra] += ir.p;
                            testOverflow(reg[ir.ra]);
                            pc++;
                            break;
                        case SUB:
                            reg[ir.ra] -= reg[ir.rb];
                            testOverflow(reg[ir.ra]);
                            pc++;
                            break;
                        case SUBI:
                            reg[ir.ra] -= ir.p;
                            testOverflow(reg[ir.ra]);
                            pc++;
                            break;
                        case MULT:
                            reg[ir.ra] *= reg[ir.rb];
                            testOverflow(reg[ir.ra]);
                            pc++;
                            break;

                        case JMP:
                            pc = ir.p;
                            break;
                        case JMPIM:
                            pc = m[ir.p].p;
                            break;
                        case JMPIG:
                            pc = (reg[ir.rb] > 0) ? reg[ir.ra] : pc + 1;
                            break;
                        case JMPIGK:
                            pc = (reg[ir.rb] > 0) ? ir.p : pc + 1;
                            break;
                        case JMPILK:
                            pc = (reg[ir.rb] < 0) ? ir.p : pc + 1;
                            break;
                        case JMPIEK:
                            pc = (reg[ir.rb] == 0) ? ir.p : pc + 1;
                            break;
                        case JMPIL:
                            pc = (reg[ir.rb] < 0) ? reg[ir.ra] : pc + 1;
                            break;
                        case JMPIE:
                            pc = (reg[ir.rb] == 0) ? reg[ir.ra] : pc + 1;
                            break;
                        case JMPIGM:
                            if (legal(ir.p)) pc = (reg[ir.rb] > 0) ? m[ir.p].p : pc + 1;
                            break;
                        case JMPILM:
                            if (legal(ir.p)) pc = (reg[ir.rb] < 0) ? m[ir.p].p : pc + 1;
                            break;
                        case JMPIEM:
                            if (legal(ir.p)) pc = (reg[ir.rb] == 0) ? m[ir.p].p : pc + 1;
                            break;
                        case JMPIGT:
                            pc = (reg[ir.ra] > reg[ir.rb]) ? ir.p : pc + 1;
                            break;

                        case DATA:
                            irpt = Interrupts.intInstrucaoInvalida;
                            break;

                        case SYSCALL:
                            sysCall.handle();
                            pc++;
                            break;

                        case STOP:
                            sysCall.stop();
                            terminouComStop = true;
                            cpuStop = true;
                            break;

                        default:
                            irpt = Interrupts.intInstrucaoInvalida;
                            break;
                    }
                }

                if (irpt != Interrupts.noInterrupt) {
                    ih.handle(irpt);
                    cpuStop = true;
                }

                executed++;
            }
        }else{
            while (!cpuStop && executed < delta) {
                if (legal(pc)) {
                    ir = m[pc];
                    if (debug) {
                        u.dump(ir);
                    }

                    switch (ir.opc) {
                        case LDI:
                            reg[ir.ra] = ir.p;
                            pc++;
                            break;
                        case LDD:
                            if (legal(ir.p)) {
                                reg[ir.ra] = m[ir.p].p;
                                pc++;
                            }
                            break;
                        case LDX:
                            if (legal(reg[ir.rb])) {
                                reg[ir.ra] = m[reg[ir.rb]].p;
                                pc++;
                            }
                            break;
                        case STD:
                            if (legal(ir.p)) {
                                m[ir.p].opc = Opcode.DATA;
                                m[ir.p].p = reg[ir.ra];
                                pc++;
                            }
                            break;
                        case STX:
                            if (legal(reg[ir.ra])) {
                                m[reg[ir.ra]].opc = Opcode.DATA;
                                m[reg[ir.ra]].p = reg[ir.rb];
                                pc++;
                            }
                            break;
                        case MOVE:
                            reg[ir.ra] = reg[ir.rb];
                            pc++;
                            break;
                        case ADD:
                            reg[ir.ra] += reg[ir.rb];
                            testOverflow(reg[ir.ra]);
                            pc++;
                            break;
                        case ADDI:
                            reg[ir.ra] += ir.p;
                            testOverflow(reg[ir.ra]);
                            pc++;
                            break;
                        case SUB:
                            reg[ir.ra] -= reg[ir.rb];
                            testOverflow(reg[ir.ra]);
                            pc++;
                            break;
                        case SUBI:
                            reg[ir.ra] -= ir.p;
                            testOverflow(reg[ir.ra]);
                            pc++;
                            break;
                        case MULT:
                            reg[ir.ra] *= reg[ir.rb];
                            testOverflow(reg[ir.ra]);
                            pc++;
                            break;

                        case JMP:
                            pc = ir.p;
                            break;
                        case JMPIM:
                            pc = m[ir.p].p;
                            break;
                        case JMPIG:
                            pc = (reg[ir.rb] > 0) ? reg[ir.ra] : pc + 1;
                            break;
                        case JMPIGK:
                            pc = (reg[ir.rb] > 0) ? ir.p : pc + 1;
                            break;
                        case JMPILK:
                            pc = (reg[ir.rb] < 0) ? ir.p : pc + 1;
                            break;
                        case JMPIEK:
                            pc = (reg[ir.rb] == 0) ? ir.p : pc + 1;
                            break;
                        case JMPIL:
                            pc = (reg[ir.rb] < 0) ? reg[ir.ra] : pc + 1;
                            break;
                        case JMPIE:
                            pc = (reg[ir.rb] == 0) ? reg[ir.ra] : pc + 1;
                            break;
                        case JMPIGM:
                            if (legal(ir.p)) pc = (reg[ir.rb] > 0) ? m[ir.p].p : pc + 1;
                            break;
                        case JMPILM:
                            if (legal(ir.p)) pc = (reg[ir.rb] < 0) ? m[ir.p].p : pc + 1;
                            break;
                        case JMPIEM:
                            if (legal(ir.p)) pc = (reg[ir.rb] == 0) ? m[ir.p].p : pc + 1;
                            break;
                        case JMPIGT:
                            pc = (reg[ir.ra] > reg[ir.rb]) ? ir.p : pc + 1;
                            break;

                        case DATA:
                            irpt = Interrupts.intInstrucaoInvalida;
                            break;

                        case SYSCALL:
                            sysCall.handle(trace);
                            pc++;
                            break;

                        case STOP:
                            sysCall.stop();
                            terminouComStop = true;
                            cpuStop = true;
                            break;

                        default:
                            irpt = Interrupts.intInstrucaoInvalida;
                            break;
                    }
                }

                if (irpt != Interrupts.noInterrupt) {
                    ih.handle(irpt, trace);
                    cpuStop = true;
                }

                executed++;
            }

        }
    }
}