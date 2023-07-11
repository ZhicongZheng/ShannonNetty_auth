#### Netty介绍
> **Netty是一个NIO客户端服务器框架，可以快速轻松地开发协议服务器和客户端等网络应用程序。它极大地简化并简化了TCP和UDP套接字服务器等网络编程。
“快速简便”并不意味着最终的应用程序会受到可维护性或性能问题的影响。Netty经过精心设计，具有丰富的协议，如FTP，SMTP，HTTP以及各种二进制和基于文本的传统协议。因此，Netty成功地找到了一种在不妥协的情况下实现易于开发，性能，稳定性和灵活性的方法。Netty 版本3x(稳定,jdk1.5+),4x(推荐,稳定,jdk1.6+),5x(不推荐),新版本不是很稳定,所以这里使用的是 Netty4x 版本**

#### 项目依赖
```xml
 <dependency>
   <groupId>io.netty</groupId>
   <artifactId>netty-all</artifactId>
   <version>4.1.21.Final</version>
 </dependency>
```
#### IdleStateHandler
- Netty 可以使用 IdleStateHandler 来实现连接管理，当连接空闲时间太长（没有发送、接收消息）时则会触发一个事件，我们便可在该事件中实现心跳机制。

#### 服务端引导程序

```java
/**
 * Socket服务器事件处理器
 */
@Slf4j
@Component
public class ShannonHeartServerHandler extends SimpleChannelInboundHandler<SocketMsg> {

    private static final ByteBuf HEART_BEAT = Unpooled.unreleasableBuffer(
            Unpooled.copiedBuffer(new SocketMsg().setId(1).setType(MsgType.HEART_BEAT).setContent("pong").toString(), CharsetUtil.UTF_8));

    /**
     * 取消绑定
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.info("{} 通道退出",ctx.name());
        NettySocketHolder.remove((NioSocketChannel) ctx.channel());
    }

    /**
     * 用户事件处理器
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
            if (idleStateEvent.state() == IdleState.READER_IDLE) {
                log.info("服务端已经5秒没有收到信息,向客户端发送心跳");
                //向客户端发送消息
                ctx.writeAndFlush(HEART_BEAT).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        }

        super.userEventTriggered(ctx, evt);
    }

    /**
     * 从通道中读取消息
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, SocketMsg socketMsg) {
        log.info("收到customProtocol={}", socketMsg);
        switch (socketMsg.getType()){
            case HEART_BEAT:
                log.info("收到客户端的心跳");
            case DH_SENDPUBKEY:
                log.info("收到客户端秘钥协商消息");
                KeyPair keyPair = ECCUtil.initKey();
                ByteBuf PublicKeyStr = Unpooled.unreleasableBuffer(
                        Unpooled.copiedBuffer(new SocketMsg().setId(1).setType(MsgType.HEART_BEAT)
                                .setContent(ECCUtil.getPublicKeyStr(keyPair)).toString(), CharsetUtil.UTF_8));
                ctx.writeAndFlush(PublicKeyStr);
        }

        NettySocketHolder.put(socketMsg.getId(), (NioSocketChannel) ctx.channel());
    }
}
```
#### 解码器
```java
public class HeartbeatDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        long id = byteBuf.readLong();
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);
        String content = new String(bytes);
        CustomProtocol socketMsg = new CustomProtocol();
        socketMsg.setId(id);
        socketMsg.setContent(content);
        list.add(socketMsg);
    }
}
```

#### 客户端引导程序
* 当客户端空闲了 N 秒没有给服务端发送消息时会自动发送一个心跳来维持连接。

```java

/**
 * Netty客户端
 */
@Slf4j
@Component
public class ShannonNettyClient {
    private EventLoopGroup group = new NioEventLoopGroup();
    @Value("${netty.server.port}")
    private int nettyPort;
    @Value("${netty.server.host}")
    private String host;

    private SocketChannel socketChannel;

    @PostConstruct
    public void start() throws InterruptedException {
        Bootstrap bootstrap = new Bootstrap();
        //NioSocketChannel用于创建客户端通道，而不是NioServerSocketChannel。
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ShannonChannelInitializer())
                .remoteAddress(host,nettyPort)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true);

        ChannelFuture future = bootstrap.connect().sync();
        if (future.isSuccess()) {
            log.info("启动 Netty客户端 成功");
        }
        //客户端断线重连逻辑,20秒重连一次
        future.addListener((ChannelFutureListener) future1 -> {
            if (future1.isSuccess()) {
                log.info("连接Netty服务端成功");
            } else {
                log.info("连接失败，进行断线重连");
                future1.channel().eventLoop().schedule(() -> {
                    try {
                        start();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        log.info("连接Netty服务端异常："+e.getMessage());
                    }
                }, 20, TimeUnit.SECONDS);
            }
        });

        socketChannel = (SocketChannel) future.channel();
    }

}

```

* [x] [参考文章](https://crossoverjie.top/2018/05/24/netty/Netty(1)TCP-Heartbeat/)

* [x] [需要了解更多参考](https://netty.io/index.html)
