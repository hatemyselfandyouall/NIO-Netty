package scoket;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MyClient {
    public static void main(String[] args) throws Exception{
        sendMsg("127.0.0.1",9999,new MyClientInitializer("word"));
    }

    public static void sendMsg(String targetHost, Integer targetPort,ChannelInitializer<SocketChannel> channelChannelInitializer){
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();

        try{
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class)
                    .handler(channelChannelInitializer);
            ChannelFuture channelFuture = bootstrap.connect(targetHost,targetPort).sync();
            channelFuture.channel().closeFuture().sync();
        }catch (Exception e){
        }  finally{
            eventLoopGroup.shutdownGracefully();
        }
    }

    static class MyClientInitializer extends ChannelInitializer<SocketChannel> {

        private String msg;

        public  MyClientInitializer(String inmsg){
            msg=inmsg;
        }
        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();

//        pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,0,4,0,4));
//        pipeline.addLast(new LengthFieldPrepender(4));
//        pipeline.addLast(new StringDecoder(CharsetUtil.UTF_8));
//        pipeline.addLast(new StringEncoder(CharsetUtil.UTF_8));
//        pipeline.addLast(new MyClientHandler());
            pipeline.addLast(new StringDecoder(CharsetUtil.UTF_8));
//        //字符串编码
            pipeline.addLast(new StringEncoder(CharsetUtil.UTF_8));
            pipeline.addLast(new LineBasedFrameDecoder(100));
            pipeline.addLast(new MyClientHandler(msg));

            //自己定义的处理器
//        pipeline.addLast(new MyServerHandler());
        }
    }

    static class MyClientHandler extends SimpleChannelInboundHandler<String> {

        private String msg;

        public MyClientHandler(String inMsg) {
            msg=inMsg;
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
            //服务端的远程地址
            System.out.println(ctx.channel().remoteAddress());
            System.out.println("client output: "+msg);
//        ctx.writeAndFlush("from client: "+ LocalDateTime.now());
            ctx.close();
        }

        /**
         * 当服务器端与客户端进行建立连接的时候会触发，如果没有触发读写操作，则客户端和客户端之间不会进行数据通信，也就是channelRead0不会执行，
         * 当通道连接的时候，触发channelActive方法向服务端发送数据触发服务器端的handler的channelRead0回调，然后
         * 服务端向客户端发送数据触发客户端的channelRead0，依次触发。
         */
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            ctx.writeAndFlush(msg);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
            ctx.close();
        }
    }
}