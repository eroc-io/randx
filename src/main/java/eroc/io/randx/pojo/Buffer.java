package eroc.io.randx.pojo;// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: buffer.proto

public final class Buffer {
  private Buffer() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }
  public interface transtionOrBuilder extends
      // @@protoc_insertion_point(interface_extends:transtion)
      com.google.protobuf.MessageLiteOrBuilder {

    /**
     * <code>optional bytes salt = 1;</code>
     */
    boolean hasSalt();
    /**
     * <code>optional bytes salt = 1;</code>
     */
    com.google.protobuf.ByteString getSalt();

    /**
     * <code>optional bytes dpk = 2;</code>
     */
    boolean hasDpk();
    /**
     * <code>optional bytes dpk = 2;</code>
     */
    com.google.protobuf.ByteString getDpk();

    /**
     * <code>optional bytes sign = 3;</code>
     */
    boolean hasSign();
    /**
     * <code>optional bytes sign = 3;</code>
     */
    com.google.protobuf.ByteString getSign();

    /**
     * <code>optional bytes pk = 4;</code>
     */
    boolean hasPk();
    /**
     * <code>optional bytes pk = 4;</code>
     */
    com.google.protobuf.ByteString getPk();

    /**
     * <code>repeated bytes cs = 5;</code>
     */
    java.util.List<com.google.protobuf.ByteString> getCsList();
    /**
     * <code>repeated bytes cs = 5;</code>
     */
    int getCsCount();
    /**
     * <code>repeated bytes cs = 5;</code>
     */
    com.google.protobuf.ByteString getCs(int index);

    /**
     * <code>repeated int32 liftcard = 6;</code>
     */
    java.util.List<Integer> getLiftcardList();
    /**
     * <code>repeated int32 liftcard = 6;</code>
     */
    int getLiftcardCount();
    /**
     * <code>repeated int32 liftcard = 6;</code>
     */
    int getLiftcard(int index);

    /**
     * <code>optional int32 pid = 7;</code>
     */
    boolean hasPid();
    /**
     * <code>optional int32 pid = 7;</code>
     */
    int getPid();

    /**
     * <code>optional string msg = 8;</code>
     */
    boolean hasMsg();
    /**
     * <code>optional string msg = 8;</code>
     */
    String getMsg();
    /**
     * <code>optional string msg = 8;</code>
     */
    com.google.protobuf.ByteString
        getMsgBytes();
  }
  /**
   * Protobuf type {@code transtion}
   */
  public  static final class transtion extends
      com.google.protobuf.GeneratedMessageLite<
          transtion, transtion.Builder> implements
      // @@protoc_insertion_point(message_implements:transtion)
      transtionOrBuilder {
    private transtion() {
      salt_ = com.google.protobuf.ByteString.EMPTY;
      dpk_ = com.google.protobuf.ByteString.EMPTY;
      sign_ = com.google.protobuf.ByteString.EMPTY;
      pk_ = com.google.protobuf.ByteString.EMPTY;
      cs_ = emptyProtobufList();
      liftcard_ = emptyIntList();
      msg_ = "";
    }
    private int bitField0_;
    public static final int SALT_FIELD_NUMBER = 1;
    private com.google.protobuf.ByteString salt_;
    /**
     * <code>optional bytes salt = 1;</code>
     */
    @Override
    public boolean hasSalt() {
      return ((bitField0_ & 0x00000001) == 0x00000001);
    }
    /**
     * <code>optional bytes salt = 1;</code>
     */
    @Override
    public com.google.protobuf.ByteString getSalt() {
      return salt_;
    }
    /**
     * <code>optional bytes salt = 1;</code>
     */
    private void setSalt(com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000001;
      salt_ = value;
    }
    /**
     * <code>optional bytes salt = 1;</code>
     */
    private void clearSalt() {
      bitField0_ = (bitField0_ & ~0x00000001);
      salt_ = getDefaultInstance().getSalt();
    }

    public static final int DPK_FIELD_NUMBER = 2;
    private com.google.protobuf.ByteString dpk_;
    /**
     * <code>optional bytes dpk = 2;</code>
     */
    @Override
    public boolean hasDpk() {
      return ((bitField0_ & 0x00000002) == 0x00000002);
    }
    /**
     * <code>optional bytes dpk = 2;</code>
     */
    @Override
    public com.google.protobuf.ByteString getDpk() {
      return dpk_;
    }
    /**
     * <code>optional bytes dpk = 2;</code>
     */
    private void setDpk(com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000002;
      dpk_ = value;
    }
    /**
     * <code>optional bytes dpk = 2;</code>
     */
    private void clearDpk() {
      bitField0_ = (bitField0_ & ~0x00000002);
      dpk_ = getDefaultInstance().getDpk();
    }

    public static final int SIGN_FIELD_NUMBER = 3;
    private com.google.protobuf.ByteString sign_;
    /**
     * <code>optional bytes sign = 3;</code>
     */
    @Override
    public boolean hasSign() {
      return ((bitField0_ & 0x00000004) == 0x00000004);
    }
    /**
     * <code>optional bytes sign = 3;</code>
     */
    @Override
    public com.google.protobuf.ByteString getSign() {
      return sign_;
    }
    /**
     * <code>optional bytes sign = 3;</code>
     */
    private void setSign(com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000004;
      sign_ = value;
    }
    /**
     * <code>optional bytes sign = 3;</code>
     */
    private void clearSign() {
      bitField0_ = (bitField0_ & ~0x00000004);
      sign_ = getDefaultInstance().getSign();
    }

    public static final int PK_FIELD_NUMBER = 4;
    private com.google.protobuf.ByteString pk_;
    /**
     * <code>optional bytes pk = 4;</code>
     */
    @Override
    public boolean hasPk() {
      return ((bitField0_ & 0x00000008) == 0x00000008);
    }
    /**
     * <code>optional bytes pk = 4;</code>
     */
    @Override
    public com.google.protobuf.ByteString getPk() {
      return pk_;
    }
    /**
     * <code>optional bytes pk = 4;</code>
     */
    private void setPk(com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000008;
      pk_ = value;
    }
    /**
     * <code>optional bytes pk = 4;</code>
     */
    private void clearPk() {
      bitField0_ = (bitField0_ & ~0x00000008);
      pk_ = getDefaultInstance().getPk();
    }

    public static final int CS_FIELD_NUMBER = 5;
    private com.google.protobuf.Internal.ProtobufList<com.google.protobuf.ByteString> cs_;
    /**
     * <code>repeated bytes cs = 5;</code>
     */
    @Override
    public java.util.List<com.google.protobuf.ByteString>
        getCsList() {
      return cs_;
    }
    /**
     * <code>repeated bytes cs = 5;</code>
     */
    @Override
    public int getCsCount() {
      return cs_.size();
    }
    /**
     * <code>repeated bytes cs = 5;</code>
     */
    @Override
    public com.google.protobuf.ByteString getCs(int index) {
      return cs_.get(index);
    }
    private void ensureCsIsMutable() {
      if (!cs_.isModifiable()) {
        cs_ =
            com.google.protobuf.GeneratedMessageLite.mutableCopy(cs_);
       }
    }
    /**
     * <code>repeated bytes cs = 5;</code>
     */
    private void setCs(
        int index, com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  ensureCsIsMutable();
      cs_.set(index, value);
    }
    /**
     * <code>repeated bytes cs = 5;</code>
     */
    private void addCs(com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  ensureCsIsMutable();
      cs_.add(value);
    }
    /**
     * <code>repeated bytes cs = 5;</code>
     */
    private void addAllCs(
        Iterable<? extends com.google.protobuf.ByteString> values) {
      ensureCsIsMutable();
      com.google.protobuf.AbstractMessageLite.addAll(
          values, cs_);
    }
    /**
     * <code>repeated bytes cs = 5;</code>
     */
    private void clearCs() {
      cs_ = emptyProtobufList();
    }

    public static final int LIFTCARD_FIELD_NUMBER = 6;
    private com.google.protobuf.Internal.IntList liftcard_;
    /**
     * <code>repeated int32 liftcard = 6;</code>
     */
    @Override
    public java.util.List<Integer>
        getLiftcardList() {
      return liftcard_;
    }
    /**
     * <code>repeated int32 liftcard = 6;</code>
     */
    @Override
    public int getLiftcardCount() {
      return liftcard_.size();
    }
    /**
     * <code>repeated int32 liftcard = 6;</code>
     */
    @Override
    public int getLiftcard(int index) {
      return liftcard_.getInt(index);
    }
    private void ensureLiftcardIsMutable() {
      if (!liftcard_.isModifiable()) {
        liftcard_ =
            com.google.protobuf.GeneratedMessageLite.mutableCopy(liftcard_);
       }
    }
    /**
     * <code>repeated int32 liftcard = 6;</code>
     */
    private void setLiftcard(
        int index, int value) {
      ensureLiftcardIsMutable();
      liftcard_.setInt(index, value);
    }
    /**
     * <code>repeated int32 liftcard = 6;</code>
     */
    private void addLiftcard(int value) {
      ensureLiftcardIsMutable();
      liftcard_.addInt(value);
    }
    /**
     * <code>repeated int32 liftcard = 6;</code>
     */
    private void addAllLiftcard(
        Iterable<? extends Integer> values) {
      ensureLiftcardIsMutable();
      com.google.protobuf.AbstractMessageLite.addAll(
          values, liftcard_);
    }
    /**
     * <code>repeated int32 liftcard = 6;</code>
     */
    private void clearLiftcard() {
      liftcard_ = emptyIntList();
    }

    public static final int PID_FIELD_NUMBER = 7;
    private int pid_;
    /**
     * <code>optional int32 pid = 7;</code>
     */
    @Override
    public boolean hasPid() {
      return ((bitField0_ & 0x00000010) == 0x00000010);
    }
    /**
     * <code>optional int32 pid = 7;</code>
     */
    @Override
    public int getPid() {
      return pid_;
    }
    /**
     * <code>optional int32 pid = 7;</code>
     */
    private void setPid(int value) {
      bitField0_ |= 0x00000010;
      pid_ = value;
    }
    /**
     * <code>optional int32 pid = 7;</code>
     */
    private void clearPid() {
      bitField0_ = (bitField0_ & ~0x00000010);
      pid_ = 0;
    }

    public static final int MSG_FIELD_NUMBER = 8;
    private String msg_;
    /**
     * <code>optional string msg = 8;</code>
     */
    @Override
    public boolean hasMsg() {
      return ((bitField0_ & 0x00000020) == 0x00000020);
    }
    /**
     * <code>optional string msg = 8;</code>
     */
    @Override
    public String getMsg() {
      return msg_;
    }
    /**
     * <code>optional string msg = 8;</code>
     */
    @Override
    public com.google.protobuf.ByteString
        getMsgBytes() {
      return com.google.protobuf.ByteString.copyFromUtf8(msg_);
    }
    /**
     * <code>optional string msg = 8;</code>
     */
    private void setMsg(
        String value) {
      if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000020;
      msg_ = value;
    }
    /**
     * <code>optional string msg = 8;</code>
     */
    private void clearMsg() {
      bitField0_ = (bitField0_ & ~0x00000020);
      msg_ = getDefaultInstance().getMsg();
    }
    /**
     * <code>optional string msg = 8;</code>
     */
    private void setMsgBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000020;
      msg_ = value.toStringUtf8();
    }

    @Override
    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        output.writeBytes(1, salt_);
      }
      if (((bitField0_ & 0x00000002) == 0x00000002)) {
        output.writeBytes(2, dpk_);
      }
      if (((bitField0_ & 0x00000004) == 0x00000004)) {
        output.writeBytes(3, sign_);
      }
      if (((bitField0_ & 0x00000008) == 0x00000008)) {
        output.writeBytes(4, pk_);
      }
      for (int i = 0; i < cs_.size(); i++) {
        output.writeBytes(5, cs_.get(i));
      }
      for (int i = 0; i < liftcard_.size(); i++) {
        output.writeInt32(6, liftcard_.getInt(i));
      }
      if (((bitField0_ & 0x00000010) == 0x00000010)) {
        output.writeInt32(7, pid_);
      }
      if (((bitField0_ & 0x00000020) == 0x00000020)) {
        output.writeString(8, getMsg());
      }
      unknownFields.writeTo(output);
    }

    @Override
    public int getSerializedSize() {
      int size = memoizedSerializedSize;
      if (size != -1) return size;

      size = 0;
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        size += com.google.protobuf.CodedOutputStream
          .computeBytesSize(1, salt_);
      }
      if (((bitField0_ & 0x00000002) == 0x00000002)) {
        size += com.google.protobuf.CodedOutputStream
          .computeBytesSize(2, dpk_);
      }
      if (((bitField0_ & 0x00000004) == 0x00000004)) {
        size += com.google.protobuf.CodedOutputStream
          .computeBytesSize(3, sign_);
      }
      if (((bitField0_ & 0x00000008) == 0x00000008)) {
        size += com.google.protobuf.CodedOutputStream
          .computeBytesSize(4, pk_);
      }
      {
        int dataSize = 0;
        for (int i = 0; i < cs_.size(); i++) {
          dataSize += com.google.protobuf.CodedOutputStream
            .computeBytesSizeNoTag(cs_.get(i));
        }
        size += dataSize;
        size += 1 * getCsList().size();
      }
      {
        int dataSize = 0;
        for (int i = 0; i < liftcard_.size(); i++) {
          dataSize += com.google.protobuf.CodedOutputStream
            .computeInt32SizeNoTag(liftcard_.getInt(i));
        }
        size += dataSize;
        size += 1 * getLiftcardList().size();
      }
      if (((bitField0_ & 0x00000010) == 0x00000010)) {
        size += com.google.protobuf.CodedOutputStream
          .computeInt32Size(7, pid_);
      }
      if (((bitField0_ & 0x00000020) == 0x00000020)) {
        size += com.google.protobuf.CodedOutputStream
          .computeStringSize(8, getMsg());
      }
      size += unknownFields.getSerializedSize();
      memoizedSerializedSize = size;
      return size;
    }

    public static transtion parseFrom(
        java.nio.ByteBuffer data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return com.google.protobuf.GeneratedMessageLite.parseFrom(
          DEFAULT_INSTANCE, data);
    }
    public static transtion parseFrom(
        java.nio.ByteBuffer data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return com.google.protobuf.GeneratedMessageLite.parseFrom(
          DEFAULT_INSTANCE, data, extensionRegistry);
    }
    public static transtion parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return com.google.protobuf.GeneratedMessageLite.parseFrom(
          DEFAULT_INSTANCE, data);
    }
    public static transtion parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return com.google.protobuf.GeneratedMessageLite.parseFrom(
          DEFAULT_INSTANCE, data, extensionRegistry);
    }
    public static transtion parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return com.google.protobuf.GeneratedMessageLite.parseFrom(
          DEFAULT_INSTANCE, data);
    }
    public static transtion parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return com.google.protobuf.GeneratedMessageLite.parseFrom(
          DEFAULT_INSTANCE, data, extensionRegistry);
    }
    public static transtion parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageLite.parseFrom(
          DEFAULT_INSTANCE, input);
    }
    public static transtion parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageLite.parseFrom(
          DEFAULT_INSTANCE, input, extensionRegistry);
    }
    public static transtion parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      return parseDelimitedFrom(DEFAULT_INSTANCE, input);
    }
    public static transtion parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
    }
    public static transtion parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageLite.parseFrom(
          DEFAULT_INSTANCE, input);
    }
    public static transtion parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageLite.parseFrom(
          DEFAULT_INSTANCE, input, extensionRegistry);
    }

    public static Builder newBuilder() {
      return (Builder) DEFAULT_INSTANCE.createBuilder();
    }
    public static Builder newBuilder(transtion prototype) {
      return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
    }

    /**
     * Protobuf type {@code transtion}
     */
    public static final class Builder extends
        com.google.protobuf.GeneratedMessageLite.Builder<
          transtion, Builder> implements
        // @@protoc_insertion_point(builder_implements:transtion)
        transtionOrBuilder {
      // Construct using Buffer.transtion.newBuilder()
      private Builder() {
        super(DEFAULT_INSTANCE);
      }


      /**
       * <code>optional bytes salt = 1;</code>
       */
      @Override
      public boolean hasSalt() {
        return instance.hasSalt();
      }
      /**
       * <code>optional bytes salt = 1;</code>
       */
      @Override
      public com.google.protobuf.ByteString getSalt() {
        return instance.getSalt();
      }
      /**
       * <code>optional bytes salt = 1;</code>
       */
      public Builder setSalt(com.google.protobuf.ByteString value) {
        copyOnWrite();
        instance.setSalt(value);
        return this;
      }
      /**
       * <code>optional bytes salt = 1;</code>
       */
      public Builder clearSalt() {
        copyOnWrite();
        instance.clearSalt();
        return this;
      }

      /**
       * <code>optional bytes dpk = 2;</code>
       */
      @Override
      public boolean hasDpk() {
        return instance.hasDpk();
      }
      /**
       * <code>optional bytes dpk = 2;</code>
       */
      @Override
      public com.google.protobuf.ByteString getDpk() {
        return instance.getDpk();
      }
      /**
       * <code>optional bytes dpk = 2;</code>
       */
      public Builder setDpk(com.google.protobuf.ByteString value) {
        copyOnWrite();
        instance.setDpk(value);
        return this;
      }
      /**
       * <code>optional bytes dpk = 2;</code>
       */
      public Builder clearDpk() {
        copyOnWrite();
        instance.clearDpk();
        return this;
      }

      /**
       * <code>optional bytes sign = 3;</code>
       */
      @Override
      public boolean hasSign() {
        return instance.hasSign();
      }
      /**
       * <code>optional bytes sign = 3;</code>
       */
      @Override
      public com.google.protobuf.ByteString getSign() {
        return instance.getSign();
      }
      /**
       * <code>optional bytes sign = 3;</code>
       */
      public Builder setSign(com.google.protobuf.ByteString value) {
        copyOnWrite();
        instance.setSign(value);
        return this;
      }
      /**
       * <code>optional bytes sign = 3;</code>
       */
      public Builder clearSign() {
        copyOnWrite();
        instance.clearSign();
        return this;
      }

      /**
       * <code>optional bytes pk = 4;</code>
       */
      @Override
      public boolean hasPk() {
        return instance.hasPk();
      }
      /**
       * <code>optional bytes pk = 4;</code>
       */
      @Override
      public com.google.protobuf.ByteString getPk() {
        return instance.getPk();
      }
      /**
       * <code>optional bytes pk = 4;</code>
       */
      public Builder setPk(com.google.protobuf.ByteString value) {
        copyOnWrite();
        instance.setPk(value);
        return this;
      }
      /**
       * <code>optional bytes pk = 4;</code>
       */
      public Builder clearPk() {
        copyOnWrite();
        instance.clearPk();
        return this;
      }

      /**
       * <code>repeated bytes cs = 5;</code>
       */
      @Override
      public java.util.List<com.google.protobuf.ByteString>
          getCsList() {
        return java.util.Collections.unmodifiableList(
            instance.getCsList());
      }
      /**
       * <code>repeated bytes cs = 5;</code>
       */
      @Override
      public int getCsCount() {
        return instance.getCsCount();
      }
      /**
       * <code>repeated bytes cs = 5;</code>
       */
      @Override
      public com.google.protobuf.ByteString getCs(int index) {
        return instance.getCs(index);
      }
      /**
       * <code>repeated bytes cs = 5;</code>
       */
      public Builder setCs(
          int index, com.google.protobuf.ByteString value) {
        copyOnWrite();
        instance.setCs(index, value);
        return this;
      }
      /**
       * <code>repeated bytes cs = 5;</code>
       */
      public Builder addCs(com.google.protobuf.ByteString value) {
        copyOnWrite();
        instance.addCs(value);
        return this;
      }
      /**
       * <code>repeated bytes cs = 5;</code>
       */
      public Builder addAllCs(
          Iterable<? extends com.google.protobuf.ByteString> values) {
        copyOnWrite();
        instance.addAllCs(values);
        return this;
      }
      /**
       * <code>repeated bytes cs = 5;</code>
       */
      public Builder clearCs() {
        copyOnWrite();
        instance.clearCs();
        return this;
      }

      /**
       * <code>repeated int32 liftcard = 6;</code>
       */
      @Override
      public java.util.List<Integer>
          getLiftcardList() {
        return java.util.Collections.unmodifiableList(
            instance.getLiftcardList());
      }
      /**
       * <code>repeated int32 liftcard = 6;</code>
       */
      @Override
      public int getLiftcardCount() {
        return instance.getLiftcardCount();
      }
      /**
       * <code>repeated int32 liftcard = 6;</code>
       */
      @Override
      public int getLiftcard(int index) {
        return instance.getLiftcard(index);
      }
      /**
       * <code>repeated int32 liftcard = 6;</code>
       */
      public Builder setLiftcard(
          int index, int value) {
        copyOnWrite();
        instance.setLiftcard(index, value);
        return this;
      }
      /**
       * <code>repeated int32 liftcard = 6;</code>
       */
      public Builder addLiftcard(int value) {
        copyOnWrite();
        instance.addLiftcard(value);
        return this;
      }
      /**
       * <code>repeated int32 liftcard = 6;</code>
       */
      public Builder addAllLiftcard(
          Iterable<? extends Integer> values) {
        copyOnWrite();
        instance.addAllLiftcard(values);
        return this;
      }
      /**
       * <code>repeated int32 liftcard = 6;</code>
       */
      public Builder clearLiftcard() {
        copyOnWrite();
        instance.clearLiftcard();
        return this;
      }

      /**
       * <code>optional int32 pid = 7;</code>
       */
      @Override
      public boolean hasPid() {
        return instance.hasPid();
      }
      /**
       * <code>optional int32 pid = 7;</code>
       */
      @Override
      public int getPid() {
        return instance.getPid();
      }
      /**
       * <code>optional int32 pid = 7;</code>
       */
      public Builder setPid(int value) {
        copyOnWrite();
        instance.setPid(value);
        return this;
      }
      /**
       * <code>optional int32 pid = 7;</code>
       */
      public Builder clearPid() {
        copyOnWrite();
        instance.clearPid();
        return this;
      }

      /**
       * <code>optional string msg = 8;</code>
       */
      @Override
      public boolean hasMsg() {
        return instance.hasMsg();
      }
      /**
       * <code>optional string msg = 8;</code>
       */
      @Override
      public String getMsg() {
        return instance.getMsg();
      }
      /**
       * <code>optional string msg = 8;</code>
       */
      @Override
      public com.google.protobuf.ByteString
          getMsgBytes() {
        return instance.getMsgBytes();
      }
      /**
       * <code>optional string msg = 8;</code>
       */
      public Builder setMsg(
          String value) {
        copyOnWrite();
        instance.setMsg(value);
        return this;
      }
      /**
       * <code>optional string msg = 8;</code>
       */
      public Builder clearMsg() {
        copyOnWrite();
        instance.clearMsg();
        return this;
      }
      /**
       * <code>optional string msg = 8;</code>
       */
      public Builder setMsgBytes(
          com.google.protobuf.ByteString value) {
        copyOnWrite();
        instance.setMsgBytes(value);
        return this;
      }

      // @@protoc_insertion_point(builder_scope:transtion)
    }
    @Override
    @SuppressWarnings({"unchecked", "fallthrough"})
    protected final Object dynamicMethod(
        MethodToInvoke method,
        Object arg0, Object arg1) {
      switch (method) {
        case NEW_MUTABLE_INSTANCE: {
          return new transtion();
        }
        case NEW_BUILDER: {
          return new Builder();
        }
        case IS_INITIALIZED: {
          return DEFAULT_INSTANCE;
        }
        case MAKE_IMMUTABLE: {
          cs_.makeImmutable();
          liftcard_.makeImmutable();
          return null;
        }
        case VISIT: {
          Visitor visitor = (Visitor) arg0;
          transtion other = (transtion) arg1;
          salt_ = visitor.visitByteString(
              hasSalt(), salt_,
              other.hasSalt(), other.salt_);
          dpk_ = visitor.visitByteString(
              hasDpk(), dpk_,
              other.hasDpk(), other.dpk_);
          sign_ = visitor.visitByteString(
              hasSign(), sign_,
              other.hasSign(), other.sign_);
          pk_ = visitor.visitByteString(
              hasPk(), pk_,
              other.hasPk(), other.pk_);
          cs_= visitor.visitList(cs_, other.cs_);
          liftcard_= visitor.visitIntList(liftcard_, other.liftcard_);
          pid_ = visitor.visitInt(
              hasPid(), pid_,
              other.hasPid(), other.pid_);
          msg_ = visitor.visitString(
              hasMsg(), msg_,
              other.hasMsg(), other.msg_);
          if (visitor == MergeFromVisitor
              .INSTANCE) {
            bitField0_ |= other.bitField0_;
          }
          return this;
        }
        case MERGE_FROM_STREAM: {
          com.google.protobuf.CodedInputStream input =
              (com.google.protobuf.CodedInputStream) arg0;
          com.google.protobuf.ExtensionRegistryLite extensionRegistry =
              (com.google.protobuf.ExtensionRegistryLite) arg1;
          if (extensionRegistry == null) {
            throw new NullPointerException();
          }
          try {
            boolean done = false;
            while (!done) {
              int tag = input.readTag();
              switch (tag) {
                case 0:
                  done = true;
                  break;
                case 10: {
                  bitField0_ |= 0x00000001;
                  salt_ = input.readBytes();
                  break;
                }
                case 18: {
                  bitField0_ |= 0x00000002;
                  dpk_ = input.readBytes();
                  break;
                }
                case 26: {
                  bitField0_ |= 0x00000004;
                  sign_ = input.readBytes();
                  break;
                }
                case 34: {
                  bitField0_ |= 0x00000008;
                  pk_ = input.readBytes();
                  break;
                }
                case 42: {
                  if (!cs_.isModifiable()) {
                    cs_ =
                        com.google.protobuf.GeneratedMessageLite.mutableCopy(cs_);
                  }
                  cs_.add(input.readBytes());
                  break;
                }
                case 48: {
                  if (!liftcard_.isModifiable()) {
                    liftcard_ =
                        com.google.protobuf.GeneratedMessageLite.mutableCopy(liftcard_);
                  }
                  liftcard_.addInt(input.readInt32());
                  break;
                }
                case 50: {
                  int length = input.readRawVarint32();
                  int limit = input.pushLimit(length);
                  if (!liftcard_.isModifiable() && input.getBytesUntilLimit() > 0) {
                    liftcard_ =
                        com.google.protobuf.GeneratedMessageLite.mutableCopy(liftcard_);
                  }
                  while (input.getBytesUntilLimit() > 0) {
                    liftcard_.addInt(input.readInt32());
                  }
                  input.popLimit(limit);
                  break;
                }
                case 56: {
                  bitField0_ |= 0x00000010;
                  pid_ = input.readInt32();
                  break;
                }
                case 66: {
                  String s = input.readString();
                  bitField0_ |= 0x00000020;
                  msg_ = s;
                  break;
                }
                default: {
                  if (!parseUnknownField(tag, input)) {
                    done = true;
                  }
                  break;
                }
              }
            }
          } catch (com.google.protobuf.InvalidProtocolBufferException e) {
            throw new RuntimeException(e.setUnfinishedMessage(this));
          } catch (java.io.IOException e) {
            throw new RuntimeException(
                new com.google.protobuf.InvalidProtocolBufferException(
                    e.getMessage()).setUnfinishedMessage(this));
          } finally {
          }
        }
        // fall through
        case GET_DEFAULT_INSTANCE: {
          return DEFAULT_INSTANCE;
        }
        case GET_PARSER: {
          com.google.protobuf.Parser<transtion> parser = PARSER;
          if (parser == null) {
            synchronized (transtion.class) {
              parser = PARSER;
              if (parser == null) {
                parser = new DefaultInstanceBasedParser(DEFAULT_INSTANCE);
                PARSER = parser;
              }
            }
          }
          return parser;
      }
      case GET_MEMOIZED_IS_INITIALIZED: {
        return (byte) 1;
      }
      case SET_MEMOIZED_IS_INITIALIZED: {
        return null;
      }
      }
      throw new UnsupportedOperationException();
    }


    // @@protoc_insertion_point(class_scope:transtion)
    private static final transtion DEFAULT_INSTANCE;
    static {
      // New instances are implicitly immutable so no need to make
      // immutable.
      DEFAULT_INSTANCE = new transtion();
    }

    public static transtion getDefaultInstance() {
      return DEFAULT_INSTANCE;
    }

    private static volatile com.google.protobuf.Parser<transtion> PARSER;

    public static com.google.protobuf.Parser<transtion> parser() {
      return DEFAULT_INSTANCE.getParserForType();
    }
  }


  static {
  }

  // @@protoc_insertion_point(outer_class_scope)
}