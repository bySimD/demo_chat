package com.chatserver.demo.repasitory;

import io.netty.channel.ChannelId;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class ChannelIdUserRepository {
    private Map<ChannelId, String> channelIdUserMap = new ConcurrentHashMap<>();


    public String getUser(ChannelId channelId) {
        return channelIdUserMap.get(channelId);
    }

    public void addUser(ChannelId channelId, String userName) {
        channelIdUserMap.put(channelId, userName);
    }

    public void deleteUser(ChannelId channelId) {
        channelIdUserMap.remove(channelId);
    }

    public List<String> getUserList() {
        return new ArrayList<>(channelIdUserMap.values());
    }

}
