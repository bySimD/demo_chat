package com.chatserver.demo.netty.handler;

import com.chatserver.demo.repasitory.ChannelIdUserRepository;
import com.chatserver.demo.service.UserNameService;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@ChannelHandler.Sharable
public class WebSocketInBoundHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    private final ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    @Autowired
    private UserNameService userNameService;

    @Autowired
    private ChannelIdUserRepository channelIdUserRepository;

    private static ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

//		om.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        String newUser = userNameService.generateName();

        // 네임 전달
        Map<String, Object> nameNoti = new HashMap<>();
        nameNoti.put("command", "NOTI");
        nameNoti.put("userName", newUser);
        nameNoti.put("type", "NAME");

        String resultMessage = objectMapper.writeValueAsString(nameNoti);
        ctx.writeAndFlush(new TextWebSocketFrame(resultMessage));

        // 유저 접속 브로드 캐스트
        Map<String, Object> enterNoti = new HashMap<>();
        enterNoti.put("command", "NOTI");
        enterNoti.put("userName", newUser);
        enterNoti.put("type", "ENTER");

        resultMessage = objectMapper.writeValueAsString(enterNoti);

        channelGroup.writeAndFlush(new TextWebSocketFrame(resultMessage));

        // 채널 등록
        channelIdUserRepository.addUser(ctx.channel().id(), newUser);
        channelGroup.add(ctx.channel());

    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {

        if (!(frame instanceof TextWebSocketFrame))
            throw new UnsupportedOperationException("unsupported frame type : " + frame.getClass().getName());

        // 전송된 내용을 JSON 개체로 변환
        Map<String, Object> data;
        try {

            data = objectMapper.readValue(((TextWebSocketFrame) frame).text(), new TypeReference<Map<String, Object>>() {});
            data.put("userName", channelIdUserRepository.getUser(ctx.channel().id()));

            if (data.get("command").equals("CHAT")) {
                String str_data = objectMapper.writeValueAsString(data);
                channelGroup.writeAndFlush(new TextWebSocketFrame(str_data));
            }
            else if ((data.get("command").equals("NAME"))){
                data.put("conList", channelIdUserRepository.getUserList());
                String str_data = objectMapper.writeValueAsString(data);
                ctx.writeAndFlush(new TextWebSocketFrame(str_data));
            }

        } catch (JsonParseException | JsonMappingException e) {

            e.printStackTrace();
            return;
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        channelGroup.remove(ctx.channel());

        // 유저 접속 브로드 캐스트
        Map<String, Object> enterNoti = new HashMap<>();
        enterNoti.put("command", "NOTI");
        enterNoti.put("userName", channelIdUserRepository.getUser(ctx.channel().id()));
        enterNoti.put("type", "LEAVE");

        String resultMessage = objectMapper.writeValueAsString(enterNoti);

        channelGroup.writeAndFlush(new TextWebSocketFrame(resultMessage));

        channelIdUserRepository.deleteUser(ctx.channel().id());
    }
}
