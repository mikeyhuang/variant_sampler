package org.mulinlab.snpsnap.database;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class LDComputerTest {

    @Test
    public void compute() {
        try {
            LDComputer computer = new LDComputer();
            computer.compute("7", 88660988, "rs12111706");
            computer.compute("9", 5453460, "rs79855302");


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}