    package main.java.com.sisop;

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

    import com.sisop.software.escalonador.Escalonador;
    import main.java.com.sisop.hardware.HW;
    import main.java.com.sisop.programas.Programs;
    import main.java.com.sisop.software.gp.GP;
    import main.java.com.sisop.software.so.SO;

    import java.util.*;
    import java.util.concurrent.Semaphore;

    public class Sistema {

        public HW hw;
        public SO so;
        public Programs progs;
        public Escalonador scheduler;
        public final Semaphore sem;


        public Sistema(int tamMem) {
            hw = new HW(tamMem);           // memoria do HW tem tamMem palavras
            so = new SO(hw, this);
            hw.cpu.setUtilities(so.utils);
            hw.cpu.setGm(so.gm);// permite cpu fazer dump de memoria ao avancar
            progs = new Programs();
            hw.cpu.start();
            this.sem = new Semaphore(1);
            scheduler = new Escalonador(5,so.gp, hw, this);

        }


        public void run() throws InterruptedException {

            System.out.println("""
                    COMANDOS:\
                    new <NOME>
                    rm <id>\s
                    ps
                    dump<id processo>\s
                    dumpM
                    exec <id>\s
                    traceOn\s
                    traceOff\s
                    execAll\s
                    exit""");


            Scanner sc = new Scanner(System.in);
            String input;
            boolean traceOn = false;
            do {
                input = sc.next();
                if (input.equals("new")) {
                    String name = sc.next();
                    System.out.println(name);
                    this.so.gp.criaProcesso(Arrays.stream(progs.progs).filter(program -> program.name.equals(name)).findFirst().get());
                }
                if (input.equals("rm")) {
                    int id = sc.nextInt();
                    this.so.gp.desalocaProcesso(id);
                }
                if (input.equals("ps")) {
                    System.out.println(this.so.gp.prontos);
                }
                if (input.equals("dump")) {
                    int i = sc.nextInt();
                    so.utils.dump(progs.progs[sc.nextInt()].image[0]);
                }
                if (input.equals("dumpM")) {
                    int lower = sc.nextInt();
                    int upper = sc.nextInt();
                    if(upper == -1){
                        upper = hw.mem.pos.length;
                    }
                    so.utils.dump(lower, upper);
                }
                if (input.equals("execAll")) {
                    if(!scheduler.isAlive()) {
                        scheduler.setName("scheduler");
                        scheduler.start();
                    }
                }
                if(input.equals("traceOn")){
                    traceOn = true;
                }
                if(input.equals("traceOff")){
                    traceOn = false;
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

        public static void main(String args[]) throws InterruptedException {
            Sistema s = new Sistema(1024);
            s.run();

        }

    }