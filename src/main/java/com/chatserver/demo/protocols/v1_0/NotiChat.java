// automatically generated by the FlatBuffers compiler, do not modify

package com.chatserver.demo.protocols.v1_0;

import java.nio.*;
import java.lang.*;
import java.util.*;
import com.google.flatbuffers.*;

@SuppressWarnings("unused")
public final class NotiChat extends Table {
  public static NotiChat getRootAsNotiChat(ByteBuffer _bb) { return getRootAsNotiChat(_bb, new NotiChat()); }
  public static NotiChat getRootAsNotiChat(ByteBuffer _bb, NotiChat obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public void __init(int _i, ByteBuffer _bb) { bb_pos = _i; bb = _bb; }
  public NotiChat __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  public String data() { int o = __offset(4); return o != 0 ? __string(o + bb_pos) : null; }
  public ByteBuffer dataAsByteBuffer() { return __vector_as_bytebuffer(4, 1); }
  public ByteBuffer dataInByteBuffer(ByteBuffer _bb) { return __vector_in_bytebuffer(_bb, 4, 1); }

  public static int createNotiChat(FlatBufferBuilder builder,
      int dataOffset) {
    builder.startObject(1);
    NotiChat.addData(builder, dataOffset);
    return NotiChat.endNotiChat(builder);
  }

  public static void startNotiChat(FlatBufferBuilder builder) { builder.startObject(1); }
  public static void addData(FlatBufferBuilder builder, int dataOffset) { builder.addOffset(0, dataOffset, 0); }
  public static int endNotiChat(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
}

