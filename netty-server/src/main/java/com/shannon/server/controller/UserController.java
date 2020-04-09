package com.shannon.server.controller;

import com.shannon.common.codec.JsonDecoder;
import com.shannon.common.codec.JsonEncoder;
import com.shannon.common.enums.MsgType;
import com.shannon.common.model.EcKeys;
import com.shannon.common.model.Gateway;
import com.shannon.common.model.SocketMsg;
import com.shannon.common.model.User;
import com.shannon.common.util.NettySocketHolder;
import com.shannon.server.util.SpringBeanFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户接口
 * @author zzc
 */
@Slf4j
@Api(value = "用户相关API")
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

    @GetMapping("/refreshKey")
    @ApiOperation(value = "刷新秘钥")
    public void refreshKey(@RequestParam String gwId){

        //首先验证用户是否拥有网关
        Gateway gw = NettySocketHolder.get(gwId);
        if (gw==null){
            throw new IllegalArgumentException("网关不在线");
        }
        Channel channel = gw.getChannel();
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

    @GetMapping("/getGwList")
    @ApiOperation("获取网关列表")
    public List<Map<String,String>> getGwList(){
        List<Map<String,String>> result = new ArrayList<>();

        NettySocketHolder.getMap().forEach((k,v)->{
            Map<String,String> map = new HashMap<>(4);
            map.put("gwid",v.getGwId());
            map.put("gwName",v.getGwName());
            map.put("status","在线");
            result.add(map);
        });
        return result;
    }

}
