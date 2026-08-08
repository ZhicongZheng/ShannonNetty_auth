

#### Netty Introduction
> **Netty is a NIO client-server framework that enables rapid and easy development of network applications such as protocol servers and clients. It greatly simplifies and streamlines network programming, such as TCP and UDP socket servers.
> "Rapid and easy" does not mean that the final application will suffer from maintainability or performance issues. Netty is carefully designed and offers rich protocols such as FTP, SMTP, HTTP, and various traditional binary and text-based protocols. Therefore, Netty successfully strikes a balance between ease of development, performance, stability, and flexibility without compromise. Netty version 3.x (stable, JDK 1.5+), 4.x (recommended, stable, JDK 1.6+), 5.x (not recommended). Newer versions are not very stable, so Netty 4.x is used here.**

#### Project Dependencies
```xml
 <dependency>
   <groupId>io.netty</groupId>
   <artifactId>netty-all</artifactId>
   <version>4.1.21.Final</version>
 </dependency>
```
#### IdleStateHandler
- Netty can use `IdleStateHandler` to manage connections. When a connection remains idle for too long (no messages are sent or received), an event is triggered, allowing us to implement a heartbeat mechanism within this event.

#### Server Bootstrap

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

#### Client Bootstrap
* When the client remains idle for N seconds without sending a message to the server, it will automatically send a heartbeat to maintain the connection.

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

* [x] [Reference Article](https://crossoverjie.top/2018/05/24/netty/Netty(1)TCP-Heartbeat/)

* [x] [Further Reading](https://netty.io/index.html)
