package com.shannon.server.controller;

import com.shannon.common.codec.JsonDecoder;
import com.shannon.common.codec.JsonEncoder;
import com.shannon.common.enums.MsgType;
import com.shannon.common.model.EcKeys;
import com.shannon.common.model.SocketMsg;
import com.shannon.common.model.User;
import com.shannon.server.util.NettySocketHolder;
import com.shannon.server.util.SpringBeanFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
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
    @ApiOperation(value = "用户登录")
    public void login(@RequestBody @NotNull User user){
        if ("root".equals(user.getName()) && "123456".equals(user.getPwd())){
            log.info("用户登录成功{}",user.toString());
        }else {
            throw new IllegalArgumentException("用户名或密码错误！");
        }
    }

    @GetMapping("refreshKey")
    @ApiModelProperty(value = "刷新秘钥")
    public void refreshKey(){

        //首先验证用户是否拥有网关
        Channel channel = NettySocketHolder.get("1");
        EcKeys ecKeys = SpringBeanFactory.getBean("EcKeys",EcKeys.class);
        SocketMsg msg = new SocketMsg()
                .setType(MsgType.REFRESH_KEY_VALUE).setContent(ecKeys.getPubKey());
        ChannelFuture future = channel.writeAndFlush(msg);
        future.addListener((ChannelFutureListener) future1 -> {
            if (future1.isSuccess()){
                log.info("发送刷新秘钥消息成功");
                //把AES加解密的编解码器替换为默认的json编解码器，去掉心跳，让网关重新执行认证
                channel.pipeline().remove("idle");
                channel.pipeline().replace("decoder","decoder",new JsonDecoder());
                channel.pipeline().replace("encoder","encoder",new JsonEncoder());
            }else {
                log.info("发送刷新秘钥消息失败");
            }
        });
    }

}
