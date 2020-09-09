package com.weiyi.zhumao.util;

import java.util.List;

import com.weiyi.zhumao.game.Command;

public class CompressCommands {

    public static void compress(List<Command> commands){
        removeExplodeCmd(commands);
    }

    static void removeExplodeCmd(List<Command> commands){
        boolean haveExplodeCmd = false;
        for(int i=0;i<commands.size();i++){
            Command cmd = commands.get(i);
            if(haveExplodeCmd){
                if(cmd.getCmdType()==2){
                    commands.remove(i);
                    i--;
                }
            }
            else{
                if(cmd.getCmdType()==2){
                    haveExplodeCmd= true;
                }
            }
        }
    }

}