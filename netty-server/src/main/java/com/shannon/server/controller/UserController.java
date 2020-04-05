package com.shannon.server.controller;

import com.shannon.common.enums.MsgType;
import com.shannon.common.model.SocketMsg;
import com.shannon.common.model.User;
import com.shannon.server.util.NettySocketHolder;
import io.netty.channel.Channel;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;

/**
 * 用户接口
 * @author zzc
 */
@Slf4j
@Api(tags = {"用户相关API"})
@RestController
@RequestMapping("/user")
public class UserController {

    @PostMapping("/login")
    @ApiOperation("用户登录")
    public void login(@RequestBody @NotNull User user){
        if ("root".equals(user.getName()) && "123456".equals(user.getPwd())){
            log.info("用户登录成功{}",user.toString());
        }else {
            throw new IllegalArgumentException("用户名或密码错误！");
        }
    }

    @GetMapping("refreshKey")
    @ApiModelProperty("刷新秘钥")
    public void refreshKey(){
        Channel channel = NettySocketHolder.get("1");
        SocketMsg msg = new SocketMsg()
                .setType(MsgType.REFRESH_KEY_VALUE);
        channel.writeAndFlush(msg);
    }

}
