package scoket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class NIOServer {
    public static void main(String[] args) throws IOException {
        serve(9999);
    }
    public static void serve(int port) throws IOException {
        //创建一个socket通道，将端口9999绑定到该通道
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        //只有非阻塞模式的通道才能被注册到选择器
        serverChannel.configureBlocking(false);
        ServerSocket ssocket = serverChannel.socket();
        InetSocketAddress address = new InetSocketAddress(port);
        ssocket.bind(address);
        //打开一个选择器
        Selector selector = Selector.open();
        //注册选择器对通道的ACCPET时间感兴趣，通道自身具有四种事件，分别为Connect、Accept、Read、Write
        //serverSocketChannel只支持ACCEPT事件
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        final ByteBuffer msg = ByteBuffer.wrap("Hi!\r\n".getBytes());
        for (;;) {
            try {
                //这个方法是阻塞的，会阻塞到选择到能够进行IO操作的通道为止
                selector.select();
            } catch (IOException ex) {
                ex.printStackTrace();
                break;
            }
            //SelectionKey是通道在选择器中注册的句柄
            Set<SelectionKey> readyKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = readyKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();
                try {
                    //对通道读操作
                    if (key.isAcceptable()) {
                        System.out.println(key);
                        ServerSocketChannel server =
                                (ServerSocketChannel)key.channel();
                        SocketChannel client = server.accept();
                        client.configureBlocking(false);
                        client.register(selector, SelectionKey.OP_WRITE |
                                SelectionKey.OP_READ, msg.duplicate());
                        System.out.println(
                                "Accepted connection from " + client);
                        ByteBuffer byteBuffer=ByteBuffer.allocate(1024);
                        int bytesRead = client.read(byteBuffer); //read into buffer.
                        while (bytesRead != -1) {
                        while(byteBuffer.hasRemaining()){
                            System.out.print((char) byteBuffer.get()); // read 1 byte at a time
                        }
                            byteBuffer.clear(); //make buffer ready for writing
                            bytesRead = client.read(byteBuffer);
                        }
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //对通道写操作
                    if (key.isWritable()) {
                        System.out.println(key);
                        SocketChannel client =
                                (SocketChannel)key.channel();
                        ByteBuffer buffer =
                                (ByteBuffer)key.attachment();
                        while (buffer.hasRemaining()) {
                            if (client.write(buffer) == 0) {
                                break;
                            }
                        }
                        client.close();
                    }
                } catch (IOException ex) {
                    key.cancel();
                    try {
                        key.channel().close();
                    } catch (IOException cex) {
// ignore on close
                    }
                }
            }
        }
    }
}
