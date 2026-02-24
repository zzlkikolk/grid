package com.jerryz.grid;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GridApplicationTests {

    @Test
    void contextLoads() {
    }


    public static int F(int x){
        return 2*x+1;
    }

    public static void main(String[] args) {
        int y = F(9);
        System.out.println(y);
    }
}
