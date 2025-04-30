package test.java.com.sisop;


import main.java.com.sisop.software.gm.GM;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SistemaTest {
    private GM gm;

    @BeforeEach
    void setUp(){
        gm = new GM(16, 1024);
    }

    @Test
    public void alocaTest()
    {
        boolean actual= gm.aloca(32, new int[5]);

        assertTrue(actual);
    }
}
