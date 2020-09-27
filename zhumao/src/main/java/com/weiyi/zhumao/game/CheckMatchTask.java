package com.weiyi.zhumao.game;

import com.weiyi.zhumao.app.Task;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CheckMatchTask implements Task {
    private CustomMatchmaker customMatchmaker;
    public CheckMatchTask(CustomMatchmaker customMatchmaker){
        this.customMatchmaker = customMatchmaker;
    }

    @Override
    public void run() {
        // log.info("检查是否有玩家等待超过时间");
        customMatchmaker.startGameWithRobots();
    }

    @Override
    public int getId() {
        return 0;
    }

    @Override
    public void setId(int id) {
    }

}