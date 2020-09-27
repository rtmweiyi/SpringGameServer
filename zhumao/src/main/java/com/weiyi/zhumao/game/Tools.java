package com.weiyi.zhumao.game;
import java.util.concurrent.ThreadLocalRandom;

public class Tools {
    public static long getRobotId(){
        long randomNum = ThreadLocalRandom.current().nextInt(1000000, 9999999);
        return randomNum;
    }
}