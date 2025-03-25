package com.sisop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import  com.sisop.software.gm.GM;



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
     
        assertEquals (true,actual );
    }
}
