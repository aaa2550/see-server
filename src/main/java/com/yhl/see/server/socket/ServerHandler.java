package com.yhl.see.server.socket;

import io.netty.channel.ChannelHandler;

import static io.netty.channel.ChannelHandler.*;

@Sharable
public class ServerHandler {
/*
    private static final Logger logger = LoggerFactory.getLogger(ServerHandler.class);

    public static Map<String, Channel> innerNodeMap = new ConcurrentHashMap<>();

    public static Map<String, Channel> pushChannelMap = new ConcurrentHashMap<>();

    public static Map<String, String> pushChannelUserMap = new ConcurrentHashMap<>();

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("channel connected, remoteIp={}", ctx.channel().remoteAddress().toString());
    }

    *//**
     * 链接断开
     * @param ctx
     * @throws Exception
     *//*
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.info("channel closed, remoteIp={}", ctx.channel().remoteAddress().toString());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        RequestCommand command = (RequestCommand) msg;

        switch (command.getCommandType()) {
            case RequestCommandEnum.查询类树: //1：司机，2：乘客
                switch (requestInfo.getCmdType()) {
                    case 1: //登录
                        logger.info("司机登录, userType={}, userId={}, remoteIp={}", requestInfo.getUserType(),
                                requestInfo.getUserId(), ctx.channel().remoteAddress().toString());
                        dealWithLogin(requestInfo, ctx);

                        break;

                    case 2: //GPS
                        logger.info("司机心跳, userType={}, userId={}, gps={}", requestInfo.getUserType(),
                                requestInfo.getUserId(), (requestInfo.getLng() + "," + requestInfo.getLat()));
                        if (! isLocal(requestInfo, ctx)) {
                            return ;
                        }

                        CarDriver carDriver = driverMongoTemplate.findOne(generalDBQuery(requestInfo.getUserId()), CarDriver.class);

                        if(carDriver == null){
                            return ;
                        }

                        Update update = Update.update("heartDate", System.currentTimeMillis())
                                .set("bearing", requestInfo.getBearing())
                                .set("loc", new double[]{requestInfo.getLng(), requestInfo.getLat()});

                        BasicDBObject field = new BasicDBObject("driverId", true);
                        BasicQuery queryForUpdate = new BasicQuery(new BasicDBObject("driverId",requestInfo.getUserId()), field);
                        driverMongoTemplate.updateMulti(queryForUpdate, update, CarDriver.class);

                        //发送心跳如rocketmq
                        this.sendGps(carDriver, requestInfo);

                        break;

                    case 5: //司机端新单到达通知确认

                    case 6: //乘客取消给司机发送取消通知确认

                    case 8: //司机抢单失败通知确认
                        logger.info("司机确认收到消息, userType={}, userId={}, 消息内容={}", requestInfo.getUserType(),
                                requestInfo.getUserId(), requestInfo);
                        message2Confirm(requestInfo);
                        break;

                    case 3: //抢单通知确认
                        break;

                    case 100: //集群中转
                        transferInnerNode(requestInfo);

                        break;

                    default:
                        logger.error("dirver cmdType illegal");
                        ctx.close();//出现异常时关闭channel
                }

                break;

            case 2:
                switch (requestInfo.getCmdType()) {
                    case 1: //登录
                        logger.info("乘客登录, userType={}, userId={}, remoteIp={}", requestInfo.getUserType(),
                                requestInfo.getUserId(), ctx.channel().remoteAddress().toString());
                        dealWithLogin(requestInfo, ctx);

                        break;

                    case 2: //heartbear
                        logger.info("乘客心跳, userType={}, userId={}", requestInfo.getUserType(), requestInfo.getUserId());
                        if (! isLocal(requestInfo, ctx)) {
                            return ;
                        }

                        break;

                    case 4: //乘客端新单到达通知确（乘客下单 先给司机发抢单通知 司机抢后 最终如果绑单成功的话 会推送给乘客 告诉他是哪个司机接单了 这个就是新单达到）

                    case 7: //司机端取消给乘客发取消通知确认

                    case 9: //司机出发给乘客发送出发通知确认

                    case 10: //司机到达约定上车点给乘客发送已达到通知确认

                    case 11: //司机接到乘客给乘客发送服务中的通知确认

                    case 12: //生成账单待结算给乘客通知确认
                        logger.info("乘客确认收到消息, userType={}, userId={}, 消息内容={}", requestInfo.getUserType(),
                                requestInfo.getUserId(), requestInfo);
                        message2Confirm(requestInfo);
                        break;

                    case 100: //集群中转
                        transferInnerNode(requestInfo);

                        break;

                    default:
                        logger.error("passenger cmdType illegal");
                        ctx.close();//出现异常时关闭channel
                }

                break;

            case 3096:
                logger.info("inner node heartbeat, remoteIp={}", ctx.channel().remoteAddress().toString());
                break;

            default:
                logger.error("userType illegal");
                ctx.close();//出现异常时关闭channel

        }
    }

    //发送心跳如rocketmq
    private void sendGps(CarDriver carDriver, SocketRequestInfo requestInfo) {
        TrackMqInfo trackMqInfo = new TrackMqInfo();
        trackMqInfo.setCUpTime(requestInfo.getCUpTime());
        trackMqInfo.setDriverId(requestInfo.getUserId());
        trackMqInfo.setAccuracy(requestInfo.getAccuracy());
        trackMqInfo.setAltitude(requestInfo.getAltitude());
        trackMqInfo.setBearing(requestInfo.getBearing());
        trackMqInfo.setCoordType(requestInfo.getCoordType());
        trackMqInfo.setLat(requestInfo.getLat());
        trackMqInfo.setLng(requestInfo.getLng());
        trackMqInfo.setNetworkType(requestInfo.getNetworkType());
        trackMqInfo.setPlatform(requestInfo.getPlatform());
        trackMqInfo.setProvider(requestInfo.getProvider());
        trackMqInfo.setSUpTime(System.currentTimeMillis());
        if (StringUtils.isNotEmpty(carDriver.getOrderId())) {
            trackMqInfo.setOrderId(carDriver.getOrderId());
            trackMqInfo.setServiceStatus(DriverTrackStatus.IN_SERVICE.getStatus());
        } else {
            trackMqInfo.setServiceStatus(DriverTrackStatus.NORMAL.getStatus());
        }
        trackMqInfo.setLicensePlates(carDriver.getLicensePlates());
        trackMqInfo.setGroupName(carDriver.getGroupName());
        trackMqInfo.setModelName(carDriver.getModelName());
        trackMqInfo.setExchangeUserId(carDriver.getExchangeUserId());

        rocketMqProducer.sendMessage(Consts.GPS_TOPIC, "*",
                String.valueOf(requestInfo.getUserId()), trackMqInfo.toString());
    }

    private BasicQuery generalDBQuery(long driverId) {
        BasicDBObject fields = new BasicDBObject();
        fields.put("dutyStatus", true);
        fields.put("onlineStatus", true);
        fields.put("serviceStatus", true);
        fields.put("driverId", true);
        fields.put("status", true);
        fields.put("orderId", true);
        fields.put("groupId", true);
        fields.put("serviceCityId", true);
        fields.put("licensePlates", true);
        fields.put("exchangeUserId", true);
        return new BasicQuery(new BasicDBObject("driverId", driverId), fields);
    }

    private boolean isLocal(SocketRequestInfo socketRequestInfo, ChannelHandlerContext ctx) {
        SocketNode socketNode = ConsistentHash.getNodeInfo(String.valueOf(socketRequestInfo.getUserId()));
        if (! socketNode.isSelf()) {
            SocketResponseInfo responseInfo = new SocketResponseInfo();
            responseInfo.setCode(Consts.FAIL_CODE);
            responseInfo.put("redirectIp", socketNode.getNetIpAndPort());
            responseInfo.setCmdType(socketRequestInfo.getCmdType());
            PushUtil.pushMsg(responseInfo, ctx.channel());

            return false;
        }

        Channel pushChannel = ServerHandler.pushChannelMap.get(
                PushUtil.getChannelKey(socketRequestInfo.getUserId(), socketRequestInfo.getUserType()));
        if (pushChannel == null) {
            ctx.close();
        }

        return true;
    }

    private void message2Confirm(SocketRequestInfo socketRequestInfo) {
        CacheManager.hdel(String.format(Consts.KEY_FORMAT, socketRequestInfo.getUserId(),
                socketRequestInfo.getUserType()), socketRequestInfo.getMsgId());
    }

    private void transferInnerNode(SocketRequestInfo socketRequestInfo) {
        Channel channel = pushChannelMap.get(PushUtil.getChannelKey(socketRequestInfo.getUserId(), socketRequestInfo.getUserType()));
        if (channel != null) {
            SocketResponseInfo responseInfo = new SocketResponseInfo();
            responseInfo.setMsgId(socketRequestInfo.getMsgId());
            responseInfo.setCode(Consts.SUCCESS_CODE);
            responseInfo.setCmdType(socketRequestInfo.getOriginCmdType());
            responseInfo.put("payLoad", socketRequestInfo.getPayLoad());
            PushUtil.pushMsg(responseInfo, channel);
        } else {
            logger.error("集群中转推送的通道不存在，请排查, socketRequestInfo = {}", socketRequestInfo);
        }
    }


    private void dealWithLogin(SocketRequestInfo socketRequestInfo, ChannelHandlerContext ctx) {
        SocketNode socketNode = ConsistentHash.getNodeInfo(String.valueOf(socketRequestInfo.getUserId()));
        SocketResponseInfo responseInfo = new SocketResponseInfo();

        if (socketRequestInfo.getUserType() == UserEnum.DRIVER.getType()) {
            Driver driver = this.driverService.getDriverById(socketRequestInfo.getUserId());
            if (driver == null || !ReviewedStatusEnum.REVIEWED_SUCCESS.getCode().equals(driver.getReviewedStatus())) {
                logger.error("driver user illegal, userId={}", socketRequestInfo.getUserId());
                ctx.close();
                return;
            }
        } else if (socketRequestInfo.getUserType() == UserEnum.CUSTOMER.getType()) {
            Customer customer = this.customerService.getCustomerWithoutPhoneUrlById(socketRequestInfo.getUserId());
            if(customer == null) {
                logger.error("customer user illegal, userId={}", socketRequestInfo.getUserId());
                ctx.close();
                return;
            }
        }

        if (socketNode.isSelf()) {
            pushChannelMap.put(PushUtil.getChannelKey(socketRequestInfo.getUserId(),
                    socketRequestInfo.getUserType()), ctx.channel());
            pushChannelUserMap.put(ctx.channel().id().asShortText(), PushUtil.getChannelKey(socketRequestInfo.getUserId(),
                    socketRequestInfo.getUserType()));
            responseInfo.setCode(Consts.SUCCESS_CODE);
            responseInfo.put("userId", socketRequestInfo.getUserId()); //TODO delete
        } else {
            responseInfo.setCode(Consts.FAIL_CODE);
            responseInfo.put("redirectIp", socketNode.getNetIpAndPort());
            responseInfo.put("userId", socketRequestInfo.getUserId()); //TODO delete
        }

        responseInfo.setCmdType(socketRequestInfo.getCmdType());

        PushUtil.pushMsg(responseInfo, ctx.channel());

        if (socketNode.isSelf()) { //处理未收到的请求
            dealWithUnconfirmedMessage(socketRequestInfo, ctx.channel());
        }
    }

    private void dealWithUnconfirmedMessage(SocketRequestInfo socketRequestInfo, Channel pushChannel) {
        Map<String, SocketPushInfo> maps = CacheManager.hgetAll(String.format(Consts.KEY_FORMAT, socketRequestInfo.getUserId(), socketRequestInfo.getUserType()), SocketPushInfo.class);
        for (Map.Entry<String, SocketPushInfo> sp : maps.entrySet()) {
            if ((System.currentTimeMillis() - sp.getValue().getCreateTime()) < TimeUnit.MINUTES.toMillis(20)) {
                SocketResponseInfo socketResponseInfo = new SocketResponseInfo();
                socketResponseInfo.setMsgId(sp.getValue().getMsgId());
                socketResponseInfo.setCode(Consts.SUCCESS_CODE);
                socketResponseInfo.setCmdType(sp.getValue().getCmdType());
                socketResponseInfo.put("payLoad", sp.getValue().getPayLoad());
                PushUtil.pushMsg(socketResponseInfo, pushChannel);

                logger.info("重新推送AppInfo = {}", socketResponseInfo);
            } else {
                CacheManager.hdel(String.format(Consts.KEY_FORMAT, socketRequestInfo.getUserId(),
                        socketRequestInfo.getUserType()), sp.getValue().getMsgId());
                logger.info("重新推送AppInfo, 过期不推送 = {}", sp.getValue());
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("Socket Exception = {}", cause);
        ctx.close();//出现异常时关闭channel
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            if (((IdleStateEvent) evt).state() == IdleState.ALL_IDLE) {
                logger.info("channel idle closed, remoteIp = {}", ctx.channel().remoteAddress().toString());
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }*/
}
