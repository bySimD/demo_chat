package com.chatserver.demo.netty;

import com.chatserver.demo.netty.codec.ByteArrayDecoder;
import com.chatserver.demo.netty.codec.ByteArrayEncoder;
import com.chatserver.demo.netty.handler.WebSocketHandler;
import com.chatserver.demo.netty.handler.WebSocketInBoundHandler;
import com.chatserver.demo.netty.handler.WebSocketOutBoundHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class NettyServer {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private ServerBootstrap sb;

    @Value("${netty.port}")
    private int port;

    @Value("${netty.threads.worker}")
    private int workerThreads;

    @Value("${netty.threads.acceptor}")
    private int acceptorThreads;

    @Value("${netty.backlog}")
    private int backlog;

    @Autowired
    private WebSocketHandler webSocketHandler;
    @Autowired
    private WebSocketInBoundHandler webSocketInBoundHandler;
    @Autowired
    private WebSocketOutBoundHandler webSocketOutBoundHandler;


//    @Value("${netty.server.transfer.websocket.path}")
//    private String transferWebsocketPath;
//    @Value("${netty.server.transfer.websocket.subProtocol}")
//    private String transferWebsocketSubProtocol;
//    @Value("${netty.server.transfer.websocket.allowExtensions}")
//    private boolean transferWebsocketAllowExtensions;


    @PostConstruct
    public void init() {
        EventLoopGroup acceptGroups;
        EventLoopGroup workGroups;

        // check system supports epoll
        if (Epoll.isAvailable()) {
            logger.info("Epoll Supported and selected");
            acceptGroups = new EpollEventLoopGroup(acceptorThreads);
            workGroups = new EpollEventLoopGroup(workerThreads);

            sb = new ServerBootstrap();
            sb.group(acceptGroups, workGroups)
                    .channel(EpollServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, backlog)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel sc) {
                            addPipeline(sc);
                        }
                    });
        } else {
            logger.info("Epoll Not Supported, NIO selected");
            acceptGroups = new NioEventLoopGroup(acceptorThreads);
            workGroups = new NioEventLoopGroup(workerThreads);

            sb = new ServerBootstrap();
            sb.group(acceptGroups, workGroups)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, backlog)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel sc) {
                            addPipeline(sc);
                        }
                    });
        }

        try {
            sb.bind(port);
        } catch (Exception e) {
            e.printStackTrace();
            acceptGroups.shutdownGracefully();
            workGroups.shutdownGracefully();
        }
    }

    private void addPipeline(SocketChannel sc) {
        ChannelPipeline cp = sc.pipeline();
        cp
            .addLast(new HttpServerCodec())
            .addLast(new HttpObjectAggregator(65536))
            .addLast(new WebSocketServerCompressionHandler())
            .addLast(new WebSocketServerProtocolHandler("/", null,true))
//                .addLast(new LoggingHandler(LogLevel.valueOf(logLevelPipeline)))
            .addLast(webSocketHandler);


    }

    private void addPipeline2(SocketChannel sc) {
        ChannelPipeline cp = sc.pipeline();
        cp
            .addLast(new HttpServerCodec())
            .addLast(new HttpObjectAggregator(65536))
            .addLast(new WebSocketServerCompressionHandler())
            .addLast(new WebSocketServerProtocolHandler("/", null,true));
//                .addLast(new LoggingHandler(LogLevel.valueOf(logLevelPipeline)))

        // outbound ↑ , call sequence
        cp.addLast(new ByteArrayEncoder());       // (1)
        cp.addLast(webSocketOutBoundHandler);    // (3)

        //  inbound ↓ , (call sequence)
        cp.addLast(new ByteArrayDecoder());       // (1)
        cp.addLast(webSocketInBoundHandler);     // (3)
    }
}
