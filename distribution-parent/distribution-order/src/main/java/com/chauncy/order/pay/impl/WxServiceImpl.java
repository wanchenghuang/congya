package com.chauncy.order.pay.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.chauncy.activity.gift.IAmGiftOrderService;
import com.chauncy.common.enums.app.order.OrderStatusEnum;
import com.chauncy.common.enums.app.order.PayOrderStatusEnum;
import com.chauncy.common.enums.app.order.afterSale.AfterSaleStatusEnum;
import com.chauncy.common.enums.app.order.afterSale.AfterSaleTypeEnum;
import com.chauncy.common.enums.goods.GoodsTypeEnum;
import com.chauncy.common.enums.log.PaymentWayEnum;
import com.chauncy.common.enums.order.OrderPayTypeEnum;
import com.chauncy.common.enums.system.ResultCode;
import com.chauncy.common.exception.sys.ServiceException;
import com.chauncy.common.util.BigDecimalUtil;
import com.chauncy.common.util.LoggerUtil;
import com.chauncy.common.util.wechat.WXConfigUtil;
import com.chauncy.common.util.wechat.WXPayPlus;
import com.chauncy.common.util.wechat.WxMD5Util;
import com.chauncy.data.bo.manage.order.OrderRefundInfoBo;
import com.chauncy.data.domain.po.activity.gift.AmGiftOrderPo;
import com.chauncy.data.domain.po.afterSale.OmAfterSaleOrderPo;
import com.chauncy.data.domain.po.order.OmOrderCustomDeclarePo;
import com.chauncy.data.domain.po.order.OmOrderPo;
import com.chauncy.data.domain.po.order.OmRealUserPo;
import com.chauncy.data.domain.po.pay.PayOrderPo;
import com.chauncy.data.domain.po.pay.PayParamPo;
import com.chauncy.data.domain.po.sys.SysUserPo;
import com.chauncy.data.dto.app.order.pay.PayParamDto;
import com.chauncy.data.mapper.activity.gift.AmGiftOrderMapper;
import com.chauncy.data.mapper.afterSale.OmAfterSaleOrderMapper;
import com.chauncy.data.mapper.order.OmOrderCustomDeclareMapper;
import com.chauncy.data.mapper.order.OmRealUserMapper;
import com.chauncy.data.mapper.pay.IPayOrderMapper;
import com.chauncy.data.mapper.pay.PayParamMapper;
import com.chauncy.data.vo.app.order.pay.UnifiedOrderVo;
import com.chauncy.order.afterSale.IOmAfterSaleOrderService;
import com.chauncy.order.pay.IWxService;
import com.chauncy.order.service.IOmOrderService;
import com.chauncy.security.util.SecurityUtil;
import com.github.wxpay.sdk.WXPay;
import com.github.wxpay.sdk.WXPayUtil;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * @author yeJH
 * @since 2019/7/5 16:23
 */

@Service
public class WxServiceImpl implements IWxService {

    private static final Logger logger = LoggerFactory.getLogger(WxServiceImpl.class);

    //支付结果通知地址
    @Value("${distribution.wxpay.PAY_NOTIFY_URL}")
    public String PAY_NOTIFY_URL ;
    //退款结果通知地址
    public static final String REFUND_NOTIFY_URL = "http://112.126.96.226/distribution/app/wxPay/notify";
    //交易类型
    public static final String TRADE_TYPE_APP = "APP";
    //币种  微信支付订单支付时使用的币种
    public static final String FEE_TYPE = "CNY";
    //清关用户实名信息  请传固定值IDCARD,暂只支持大陆身份证。
    public static final String CERT_TYPE = "IDCARD";


    @Autowired
    private IPayOrderMapper payOrderMapper;

    @Autowired
    private PayParamMapper payParamMapper;

    @Autowired
    private OmOrderCustomDeclareMapper omOrderCustomDeclareMapper;

    @Autowired
    private OmRealUserMapper omRealUserMapper;

    @Autowired
    private AmGiftOrderMapper amGiftOrderMapper;

    @Autowired
    private IAmGiftOrderService amGiftOrderService;

    @Autowired
    private OmAfterSaleOrderMapper omAfterSaleOrderMapper;

    @Autowired
    private IOmOrderService omOrderService;

    @Autowired
    private WXConfigUtil wxConfigUtil;

    @Autowired
    private WxMD5Util wxMD5Util;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private IOmAfterSaleOrderService omAfterSaleOrderService;

    /**
     * @Author yeJH
     * @Date 2019/10/9 20:40
     * @Description 海外直邮的商品下单之后 微信自助清关 将订单附加信息提交到海关
     * https://pay.weixin.qq.com/wiki/doc/api/external/declarecustom.php?chapter=18_1
     *
     * @Update yeJH
     *
     * @param  orderId
     * @return void
     **/
    @Override
    public void customDeclareOrder(Long orderId) throws Exception {
        OmOrderPo omOrderPo = omOrderService.getById(orderId);

        if(null == omOrderPo) {
            throw new ServiceException(ResultCode.NO_EXISTS);
        }

        if(!OrderStatusEnum.NEED_SEND_GOODS.equals(omOrderPo.getStatus())) {
            //订单不是已支付状态
            return ;
            //throw new ServiceException(ResultCode.FAIL, "订单状态不是已支付状态");
        }
        if(!(GoodsTypeEnum.BONDED.getName().equals(omOrderPo.getGoodsType()) ||
                GoodsTypeEnum.OVERSEA.getName().equals(omOrderPo.getGoodsType()))) {
            return ;
            //订单类型不是保税仓 或者 海外直邮
            //throw new ServiceException(ResultCode.FAIL, "订单类型不是保税仓或者海外直邮");
        }

        WxMD5Util md5Util = wxMD5Util;
        WXConfigUtil config = wxConfigUtil;
        WXPayPlus wxPayPlus = new WXPayPlus(config);

        //支付单
        PayOrderPo payOrderPo = payOrderMapper.selectById(omOrderPo.getPayOrderId());

        //申请退款接口微信签名参数
        Map<String, String> data = new HashMap<>();
        //商户订单号
        data.put("out_trade_no", String.valueOf(omOrderPo.getPayOrderId()));
        //微信支付订单号
        data.put("transaction_id", payOrderPo.getPayOrderNo());
        //应用id APPID
        data.put("appid", config.getAppID());
        //商户号
        data.put("mch_id", config.getMchID());
        //海关  GUANGZHOU_ZS 广州（总署版）
        data.put("customs", config.getCustoms());
        //商户海关备案号
        data.put("mch_customs_no", config.getMchCustomsNo());

        //以下为拆单传的信息
        //商户子订单号，如有拆单则必传
        data.put("sub_order_no", String.valueOf(omOrderPo.getId()));
        //微信支付订单支付时使用的币种，暂只支持人民币CNY,如有拆单则必传。
        data.put("fee_type", FEE_TYPE);
        //物流费用，以分为单位，如有拆单则必传。
        Integer transportFee = BigDecimalUtil.safeMultiply(omOrderPo.getShipMoney(), new BigDecimal(100)).intValue();
        data.put("transport_fee", String.valueOf(transportFee));
        //商品费用，以分为单位，如有拆单则必传。 订单总金额 - 运费(商品实际费用 + 税费)
        BigDecimal productFee = BigDecimalUtil.safeSubtract(omOrderPo.getTotalMoney(), omOrderPo.getShipMoney());
        Integer productFeeCent = BigDecimalUtil.safeMultiply(productFee, new BigDecimal(100)).intValue();
        data.put("product_fee", String.valueOf(productFeeCent));
        //子订单金额，以分为单位，不能超过原订单金额，order_fee=transport_fee+product_fee（应付金额=物流费+商品价格），如有拆单则必传。
        data.put("order_fee", String.valueOf(transportFee + productFeeCent));

        //用户实名信息
        OmRealUserPo omRealUserPo = omRealUserMapper.selectById(omOrderPo.getRealUserId());
        //证件类型	请传固定值IDCARD,暂只支持大陆身份证。
        data.put("cert_type", CERT_TYPE);
        //证件号码	用户大陆身份证号，尾号为字母X的身份证号，请大写字母X。
        data.put("cert_id", omRealUserPo.getIdCard().toUpperCase());
        //姓名	用户姓名。
        data.put("name", omRealUserPo.getTrueName());
        //参数签名
        try {
            data.put("sign", md5Util.getSign(data));
        } catch (Exception e) {
            throw new ServiceException(ResultCode.FAIL, "微信自助清关接口签名失败");
        }

        Map<String, String> response;
        //调用微信订单附加信息提交接口API
        try {
            response = wxPayPlus.customDeclareOrder(data);
        } catch (Exception e) {
            throw new ServiceException(ResultCode.FAIL, "调用微信自助清关接口失败");
        }

        OmOrderCustomDeclarePo omOrderCustomDeclarePo = new OmOrderCustomDeclarePo();
        omOrderCustomDeclarePo.setOrderId(omOrderPo.getId());
        omOrderCustomDeclarePo.setCreateBy("auto");

        //获取返回码
        String returnCode = response.get("return_code");
        omOrderCustomDeclarePo.setReturnCode(returnCode);
        //返回信息
        omOrderCustomDeclarePo.setReturnMsg(response.get("return_msg"));
        //若返回码return_code为SUCCESS，则会返回一个result_code,再对该result_code进行判断
        if (returnCode.equals("SUCCESS")) {
            String resultCode = response.get("result_code");
            if ("SUCCESS".equals(resultCode)) {
                //保存海关申报信息
                omOrderCustomDeclarePo = saveOrderCustomDeclare(omOrderCustomDeclarePo, response);
            } else {
                //调用微信申报接口返回失败
                //错误代码描述
                omOrderCustomDeclarePo.setErrCodeDes(response.get("err_code_des"));
                //错误代码
                omOrderCustomDeclarePo.setErrCode(response.get("err_code"));
            }
        }
        QueryWrapper<OmOrderCustomDeclarePo> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(OmOrderCustomDeclarePo::getOrderId, omOrderPo.getId());
        OmOrderCustomDeclarePo oldDeclarePo = omOrderCustomDeclareMapper.selectOne(queryWrapper);
        if(null == oldDeclarePo) {
            omOrderCustomDeclareMapper.insert(omOrderCustomDeclarePo);
        } else {
            omOrderCustomDeclarePo.setId(oldDeclarePo.getId());
            omOrderCustomDeclareMapper.updateById(omOrderCustomDeclarePo);
        }


    }

    /**
     * @Author yeJH
     * @Date 2019/10/11 15:39
     * @Description 将海关申报结果保存
     *
     * @Update yeJH
     *
     * @param  response
     * @return com.chauncy.data.domain.po.order.OmOrderCustomDeclarePo
     **/
    private OmOrderCustomDeclarePo saveOrderCustomDeclare(OmOrderCustomDeclarePo omOrderCustomDeclarePo,
                                                          Map<String, String> response) {
        omOrderCustomDeclarePo.setCreateBy("auto");
        //商户子订单号   orderId
        omOrderCustomDeclarePo.setOrderId(Long.valueOf(response.get("sub_order_no")));
        //海关申报状态码
        omOrderCustomDeclarePo.setDeclareStatus(response.get("state"));
        //微信子订单号
        omOrderCustomDeclarePo.setSubOrderId(response.get("sub_order_id"));
        //最后更新时间
        omOrderCustomDeclarePo.setModifyTime(response.get("modify_time"));
        //订购人和支付人身份信息校验结果
        omOrderCustomDeclarePo.setCertCheckResult(response.get("cert_check_result"));
        //验核机构
        omOrderCustomDeclarePo.setVerifyDepartment(response.get("verify_department"));
        //交易流水号
        omOrderCustomDeclarePo.setVerifyDepartmentTradeId(response.get("verify_department_trade_id"));
        //业务结果
        omOrderCustomDeclarePo.setResultCode(response.get("result_code"));
        return omOrderCustomDeclarePo;
    }

    /**
     * 调用官方SDK统一下单 获取前端调起支付接口的参数
     * @return
     * @throws Exception
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UnifiedOrderVo unifiedOrder(PayParamDto payParamDto) throws Exception {
        UnifiedOrderVo unifiedOrderVo = new UnifiedOrderVo();
        OrderPayTypeEnum orderPayTypeEnum = OrderPayTypeEnum.getById(payParamDto.getOrderPayType());
        switch (orderPayTypeEnum) {
            case GOODS_PAYMENT:
                unifiedOrderVo = goodsPayment(payParamDto.getIpAddr(), payParamDto.getPayOrderId());
                break;
            case GIFT_RECHARGE:
                unifiedOrderVo = gifiRecharge(payParamDto.getIpAddr(), payParamDto.getPayOrderId());
                break;
        }
        return unifiedOrderVo;
    }

    /**
     * 商品支付
     * @param ipAddr
     * @param payOrderId
     * @return
     * @throws Exception
     */
    private UnifiedOrderVo goodsPayment(String ipAddr, Long payOrderId) throws Exception {
        //商品支付
        QueryWrapper<PayOrderPo> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(PayOrderPo::getId, payOrderId)
                .eq(PayOrderPo::getEnabled, true);
        PayOrderPo payOrderPo = payOrderMapper.selectOne(queryWrapper);
        if (null == payOrderPo) {
            throw new ServiceException(ResultCode.NO_EXISTS, "订单记录不存在");
        }
//        LocalDateTime endTime = payOrderMapper.getGroupEndTimeByPayId(payOrderId);
//        if (endTime!=null){
//            if (LocalDateTime.now().isAfter(endTime)){
//                throw new ServiceException(ResultCode.FAIL, "支付失败！当前拼团订单已超过活动时间！");
//            }
//        }
        WxMD5Util md5Util = wxMD5Util;
        WXConfigUtil config = wxConfigUtil;
        WXPay wxpay = new WXPay(config);
        //app调起支付接口所需参数
        UnifiedOrderVo unifiedOrderVo = new UnifiedOrderVo();
        //参加调起支付的签名字段有且只能是6个，分别为appid、partnerid、prepayid、package、noncestr和timestamp，而且都必须是小写
        unifiedOrderVo.setAppId(config.getAppID());
        unifiedOrderVo.setPartnerId(config.getMchID());
        unifiedOrderVo.setPackageStr("Sign=WXPay");
        unifiedOrderVo.setNonceStr(WXPayUtil.generateNonceStr());
        //时间戳 单位为秒
        unifiedOrderVo.setTimestamp(String.valueOf(System.currentTimeMillis() / 1000));

        if(Strings.isNotBlank(payOrderPo.getPrePayId()) && payOrderPo.getPayTypeCode().equals(PaymentWayEnum.WECHAT.getName())) {
            //预支付交易会话标识 说明预支付单已生成
            unifiedOrderVo.setPrepayId(payOrderPo.getPrePayId());
            //returnMap.put("sign", response.get("sign"));
            //returnMap.put("trade_type", response.get("trade_type"));
            //调起支付参数重新签名  不要使用请求预支付订单时返回的签名
            Map<String, String> returnMap = getWxPayParam(unifiedOrderVo);
            returnMap.remove("class");
            unifiedOrderVo.setSign(md5Util.getSign(returnMap));
            return unifiedOrderVo;
        }

        //总金额  单位分
        Integer totalFee = BigDecimalUtil.safeMultiply(payOrderPo.getTotalRealPayMoney(), new BigDecimal(100)).intValue();
        //统一下单接口微信签名参数
        Map<String, String> data = getSignMap(wxConfigUtil, wxMD5Util, ipAddr, payOrderId, totalFee, OrderPayTypeEnum.GOODS_PAYMENT);

        //支付单信息
        payOrderPo.setPayTypeCode(PaymentWayEnum.WECHAT.getName());
        payOrderPo.setStartTime(LocalDateTime.now());
        payOrderPo.setUserIp(ipAddr);
        payOrderPo.setNotifyUrl(PAY_NOTIFY_URL);
        //payOrderPo.setExtra("");
        payOrderPo.setSubject(config.getBody());
        //payOrderPo.setDetail("");
        payOrderPo.setMerchantId(config.getMchID());
        payOrderPo.setTradeType(TRADE_TYPE_APP);

        //使用微信统一下单API请求预付订单
        Map<String, String> response = wxpay.unifiedOrder(data);
        //获取返回码
        String returnCode = response.get("return_code");
        //若返回码return_code为SUCCESS，则会返回一个result_code,再对该result_code进行判断
        if (returnCode.equals("SUCCESS")) {
            String resultCode = response.get("result_code");
            if ("SUCCESS".equals(resultCode)) {
                //resultCode 为SUCCESS，才会返回prepay_id和trade_type
                unifiedOrderVo.setPrepayId(response.get("prepay_id"));
                //调起支付参数重新签名  不要使用请求预支付订单时返回的签名
                Map<String, String> returnMap = getWxPayParam(unifiedOrderVo);
                returnMap.remove("class");
                unifiedOrderVo.setSign(md5Util.getSign(returnMap));
                //更新支付订单
                payOrderPo.setPrePayId(response.get("prepay_id"));
                payOrderMapper.updateById(payOrderPo);

                //记录本次支付订单传给微信服务器的xml参数  海关申报需要用到
                savePayParamPo(payOrderId, data);

                return unifiedOrderVo;
            } else {
                //调用微信统一下单接口返回失败
                String errCodeDes = response.get("err_code_des");
                //更新支付订单
                payOrderPo.setErrorCode(response.get("err_code"));
                payOrderPo.setErrorMsg(errCodeDes);
                payOrderMapper.updateById(payOrderPo);
                throw new ServiceException(ResultCode.FAIL, errCodeDes);
            }
        } else {
            //调用微信统一下单接口返回失败
            String returnMsg = response.get("return_msg");
            //更新支付订单
            payOrderPo.setErrorCode(response.get("return_code"));
            payOrderPo.setErrorMsg(returnMsg);
            payOrderMapper.updateById(payOrderPo);
            throw new ServiceException(ResultCode.FAIL, returnMsg);
        }
    }

    /**
     * @Author yeJH
     * @Date 2019/10/11 21:43
     * @Description 记录本次支付订单传给微信服务器的xml参数  海关申报需要用到
     *
     * @Update yeJH
     *
     * @param  payOrderId
     * @param  data
     * @return void
     **/
    private void savePayParamPo(Long payOrderId, Map<String, String> data) throws Exception{

        //记录本次支付订单传给微信服务器的xml参数
        QueryWrapper<PayParamPo> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(PayParamPo::getPayOrderId, payOrderId);
        PayParamPo payParamPo = payParamMapper.selectOne(queryWrapper);
        if(null != payParamPo) {
            payParamPo.setId(payParamPo.getId());
            payParamPo.setPayOrderId(payOrderId);
            payParamPo.setInitalRequest(WXPayUtil.mapToXml(data));
            payParamMapper.updateById(payParamPo);
        } else {
            payParamPo = new PayParamPo();
            payParamPo.setPayOrderId(payOrderId);
            payParamPo.setInitalRequest(WXPayUtil.mapToXml(data));
            payParamMapper.insert(payParamPo);
        }

    }

    /**
     * @Author yeJH
     * @Date 2019/10/11 21:55
     * @Description 记录本次支付通知微信服务器传过来的参数  海关申报需要用到
     *
     * @Update yeJH
     *
     * @param  payOrderId
     * @param  data
     * @return void
     **/
    private void updatePayParamPo(Long payOrderId, String data) throws Exception{

        //记录本次支付订单传给微信服务器的xml参数
        QueryWrapper<PayParamPo> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(PayParamPo::getPayOrderId, payOrderId);
        PayParamPo payParamPo = payParamMapper.selectOne(queryWrapper);
        if(null != payParamPo) {
            payParamPo.setId(payParamPo.getId());
            payParamPo.setPayOrderId(payOrderId);
            payParamPo.setInitalResponse(data);
            payParamMapper.updateById(payParamPo);
        }

    }

    /**
     * 礼包充值
      * @param ipAddr
     * @param payOrderId
     * @return
     * @throws Exception
     */
    private UnifiedOrderVo gifiRecharge(String ipAddr, Long payOrderId) throws Exception {

        //礼包充值
        AmGiftOrderPo amGiftOrderPo = amGiftOrderMapper.selectById(payOrderId);
        if(null == amGiftOrderPo) {
            throw new ServiceException(ResultCode.NO_EXISTS, "订单记录不存在");
        }

        WxMD5Util md5Util = wxMD5Util;
        WXConfigUtil config = wxConfigUtil;
        WXPay wxpay = new WXPay(config);
        //app调起支付接口所需参数
        UnifiedOrderVo unifiedOrderVo = new UnifiedOrderVo();
        //参加调起支付的签名字段有且只能是6个，分别为appid、partnerid、prepayid、package、noncestr和timestamp，而且都必须是小写
        unifiedOrderVo.setPartnerId(config.getMchID());
        unifiedOrderVo.setAppId(config.getAppID());
        unifiedOrderVo.setPackageStr("Sign=WXPay");
        unifiedOrderVo.setNonceStr(WXPayUtil.generateNonceStr());
        //时间戳 单位为秒
        unifiedOrderVo.setTimestamp(String.valueOf(System.currentTimeMillis() / 1000));

        //总金额  单位分
        Integer totalFee = BigDecimalUtil.safeMultiply(amGiftOrderPo.getPurchasePrice(), new BigDecimal(100)).intValue();
        //统一下单接口微信签名参数
        Map<String, String> data = getSignMap(wxConfigUtil, wxMD5Util, ipAddr, payOrderId, totalFee, OrderPayTypeEnum.GIFT_RECHARGE);

        //使用微信统一下单API请求预付订单
        Map<String, String> response = wxpay.unifiedOrder(data);
        //获取返回码
        String returnCode = response.get("return_code");
        //若返回码return_code为SUCCESS，则会返回一个result_code,再对该result_code进行判断
        if (returnCode.equals("SUCCESS")) {
            String resultCode = response.get("result_code");
            if ("SUCCESS".equals(resultCode)) {
                //resultCode 为SUCCESS，才会返回prepay_id和trade_type
                unifiedOrderVo.setPrepayId(response.get("prepay_id"));
                //调起支付参数重新签名  不要使用请求预支付订单时返回的签名
                Map<String, String> returnMap = getWxPayParam(unifiedOrderVo);
                unifiedOrderVo.setSign(md5Util.getSign(returnMap));
                return unifiedOrderVo;
            } else {
                //调用微信统一下单接口返回失败
                String errCodeDes = response.get("err_code_des");
                //更新支付订单
                amGiftOrderPo.setErrorCode(response.get("err_code"));
                amGiftOrderPo.setErrorMsg(errCodeDes);
                amGiftOrderMapper.updateById(amGiftOrderPo);
                throw new ServiceException(ResultCode.FAIL, errCodeDes);
            }
        } else {
            //调用微信统一下单接口返回失败
            String returnMsg = response.get("return_msg");
            //更新支付订单
            amGiftOrderPo.setErrorCode(response.get("return_code"));
            amGiftOrderPo.setErrorMsg(returnMsg);
            amGiftOrderMapper.updateById(amGiftOrderPo);
            throw new ServiceException(ResultCode.FAIL, returnMsg);
        }

    }

    /**
     * @Author yeJH
     * @Date 2019/10/12 9:43
     * @Description 将调用微信支付的参数转为map  完成签名操作
     *
     * @Update yeJH
     *
     * @param  unifiedOrderVo
     * @return java.util.Map<java.lang.String,java.lang.String>
     **/
    private Map<String, String> getWxPayParam(UnifiedOrderVo unifiedOrderVo) {
        Map<String, String> map = new HashMap<>();
        map.put("appid", unifiedOrderVo.getAppId());
        map.put("partnerid", unifiedOrderVo.getPartnerId());
        map.put("prepayid", unifiedOrderVo.getPrepayId());
        map.put("package", unifiedOrderVo.getPackageStr());
        map.put("noncestr", unifiedOrderVo.getNonceStr());
        map.put("timestamp", unifiedOrderVo.getTimestamp());
        return map;
    }

    /**
     * 统一下单接口微信签名参数
     * @param config
     * @param md5Util
     * @param ipAddr
     * @param payOrderId
     * @param totalFee
     * @param orderPayTypeEnum
     * @return
     * @throws Exception
     */
    private Map<String, String> getSignMap(WXConfigUtil config, WxMD5Util md5Util, String ipAddr,
                                           Long payOrderId, Integer totalFee, OrderPayTypeEnum orderPayTypeEnum) throws Exception {
        Map<String, String> data = new HashMap<>();
        //应用id APPID
        data.put("appid", config.getAppID());
        //商户号
        data.put("mch_id", config.getMchID());
        //随机字符串
        data.put("nonce_str", WXPayUtil.generateNonceStr());
        //商品描述  APP——需传入应用市场上的APP名字-实际商品名称
        data.put("body", config.getBody());
        //附加数据  订单支付类型（商品支付/礼包充值）
        data.put("attach", orderPayTypeEnum.name());
        //商户订单号
        data.put("out_trade_no", String.valueOf(payOrderId));
        data.put("total_fee", String.valueOf(totalFee));
        //调用微信支付API的机器IP
        data.put("spbill_create_ip", ipAddr);
        //异步通知地址
        data.put("notify_url", PAY_NOTIFY_URL);
        //交易类型
        data.put("trade_type", TRADE_TYPE_APP);
        //统一下单签名
        String orderSign = md5Util.getSign(data);
        data.put("sign", orderSign);
        return data;
    }

    /**
     * 微信支付结果通知
     * @param notifyData 异步通知后的XML数据
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String payBack(String notifyData) throws Exception{
        String failXmlBack = "<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[报文为空]]></return_msg></xml>";
        String successXmlBack = "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>";
        WXConfigUtil config = wxConfigUtil;
        WXPay wxpay = new WXPay(config);
        //异步通知返回微信的参数 xml格式
        String xmlBack = "";
        Map<String, String> notifyMap = null;
        try {
            //异步通知微信返回的参数 调用官方SDK转换成map类型数据
            notifyMap = WXPayUtil.xmlToMap(notifyData);
            String outTradeNo = notifyMap.get("out_trade_no");
            String attach = notifyMap.get("attach");
            OrderPayTypeEnum orderPayTypeEnum = OrderPayTypeEnum.valueOf(attach);
            //验证签名是否有效，有效则进一步处理
            if (wxpay.isPayResultNotifySignatureValid(notifyMap)) {
                //状态
                String returnCode = notifyMap.get("return_code");
                if (returnCode.equals("SUCCESS")) {
                    if(orderPayTypeEnum.equals(OrderPayTypeEnum.GOODS_PAYMENT)) {
                        //商品支付订单
                        PayOrderPo payOrderPo =  payOrderMapper.selectById(outTradeNo);
                        if (null != payOrderPo) {
                            if(payOrderPo.getStatus().equals(PayOrderStatusEnum.NEED_PAY.getId())) {
                                //回调支付金额
                                Integer cashFee = Integer.parseInt(notifyMap.get("cash_fee"));
                                //支付订单计算应支付总金额
                                Integer totalMoney = BigDecimalUtil.safeMultiply(payOrderPo.getTotalRealPayMoney(), new BigDecimal(100)).intValue();
                                if(cashFee.equals(totalMoney)) {
                                    //业务数据持久化
                                    omOrderService.wxPayNotify(payOrderPo, notifyMap);

                                    //记录本次支付通知微信服务器传过来的参数  海关申报需要用到
                                    updatePayParamPo(payOrderPo.getId(), notifyData);

                                } else {
                                    logger.info("微信手机支付回调成功，订单号:{}，但是金额不对应，回调支付金额{}，支付订单计算应支付总金额", outTradeNo, cashFee, totalMoney);
                                }
                            } else if(payOrderPo.getStatus().equals(PayOrderStatusEnum.ALREADY_PAY.getId()) ||
                                payOrderPo.getStatus().equals(PayOrderStatusEnum.ALREADY_CANCEL.getId())) {
                            //订单状态为已支付或者已取消
                            // 注意特殊情况：微信服务端同样的通知可能会多次发送给商户系统，所以数据持久化之前需要检查是否已经处理过了，处理了直接返回成功标志
                            // 注意特殊情况：订单已经退款，但收到了支付结果成功的通知，不应把商户的订单状态从退款改成支付成功
                            }
                            logger.info("微信手机支付回调成功订单号:{}", outTradeNo);
                            xmlBack = successXmlBack;
                        } else {
                            logger.info("微信手机支付回调失败订单号:{}", outTradeNo);
                            xmlBack = failXmlBack;
                        }
                    } else if(orderPayTypeEnum.equals(OrderPayTypeEnum.GIFT_RECHARGE)) {
                        //礼包充值订单
                        AmGiftOrderPo amGiftOrderPo = amGiftOrderMapper.selectById(outTradeNo);
                        if (null != amGiftOrderPo) {
                            if(amGiftOrderPo.getPayStatus().equals(PayOrderStatusEnum.NEED_PAY.getId())) {
                                //回调支付金额
                                Integer cashFee = Integer.parseInt(notifyMap.get("cash_fee"));
                                //支付订单计算应支付总金额
                                Integer totalMoney = BigDecimalUtil.safeMultiply(amGiftOrderPo.getPurchasePrice(), new BigDecimal(100)).intValue();
                                if(cashFee.equals(totalMoney)) {
                                    //业务数据持久化
                                    amGiftOrderService.wxPayNotify(amGiftOrderPo, notifyMap);

                                } else {
                                    logger.info("微信手机支付回调成功，订单号:{}，但是金额不对应，回调支付金额{}，支付订单计算应支付总金额", outTradeNo, cashFee, totalMoney);
                                }
                            } else if(amGiftOrderPo.getPayStatus().equals(PayOrderStatusEnum.ALREADY_PAY.getId()) ||
                                    amGiftOrderPo.getPayStatus().equals(PayOrderStatusEnum.ALREADY_CANCEL.getId())) {
                                //订单状态为已支付或者已取消
                                // 注意特殊情况：微信服务端同样的通知可能会多次发送给商户系统，所以数据持久化之前需要检查是否已经处理过了，处理了直接返回成功标志
                                // 注意特殊情况：订单已经退款，但收到了支付结果成功的通知，不应把商户的订单状态从退款改成支付成功
                            }
                            logger.info("微信手机支付回调成功订单号:{}", outTradeNo);
                            xmlBack = successXmlBack;
                        } else {
                            logger.info("微信手机支付回调失败订单号:{}", outTradeNo);
                            xmlBack = failXmlBack;
                        }
                    }
                }
                return xmlBack;
            } else {
                //签名错误，如果数据里没有sign字段，也认为是签名错误
                //失败的数据要不要存储？
                logger.error("手机支付回调通知签名错误,返回参数：" + notifyMap);
                xmlBack = "<xml>" + "<return_code><![CDATA[FAIL]]></return_code>" + "<return_msg><![CDATA[报文为空]]></return_msg>" + "</xml>";
                return xmlBack;
            }
        } catch (Exception e) {
            logger.error("手机支付回调通知失败", e);
            xmlBack = "<xml>" + "<return_code><![CDATA[FAIL]]></return_code>" + "<return_msg><![CDATA[报文为空]]></return_msg>" + "</xml>";
        }
        return xmlBack;
    }



    /**
     *   微信查询订单接口  订单未操作的做业务更新
     *   官方文档 ：https://pay.weixin.qq.com/wiki/doc/api/app/app.php?chapter=9_2&index=4
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void orderQuery(Long payOrderId) throws Exception {
        PayOrderPo payOrderPo = payOrderMapper.selectById(payOrderId);
        if(null == payOrderPo) {
            throw new ServiceException(ResultCode.NO_EXISTS, "订单记录不存在");
        }

        WxMD5Util md5Util = new WxMD5Util();
        WXConfigUtil config = wxConfigUtil;
        WXPay wxpay = new WXPay(config);
        Map<String, String> returnMap = new HashMap<>();

        //查询订单接口微信签名参数
        Map<String, String> data = new HashMap<>();
        //应用id APPID
        data.put("appid", config.getAppID());
        //商户号
        data.put("mch_id", config.getMchID());
        //随机字符串
        data.put("nonce_str", WXPayUtil.generateNonceStr());
        //微信订单号(优先选择)  商户订单号
        if(Strings.isNotBlank(payOrderPo.getPayOrderNo())) {
            data.put("transaction_id", payOrderPo.getPayOrderNo());
        } else {
            data.put("out_trade_no", String.valueOf(payOrderPo.getId()));
        }
        //签名
        String sign = md5Util.getSign(data);
        data.put("sign", sign);

        //使用微信查询订单API请求预付订单
        Map<String, String> response = wxpay.orderQuery(data);
        //获取返回码
        String returnCode = response.get("return_code");
        if (returnCode.equals("SUCCESS")) {
            String resultCode = response.get("result_code");
            if ("SUCCESS".equals(resultCode)) {
                //resultCode 为SUCCESS，才会返回prepay_id和trade_type
                if("SUCCESS".equals(response.get("trade_state")) && payOrderPo.getStatus().equals(PayOrderStatusEnum.NEED_PAY.getId())) {
                    //回调支付金额
                    Integer cashFee = Integer.parseInt(response.get("cash_fee"));
                    //支付订单计算应支付总金额
                    Integer totalMoney = BigDecimalUtil.safeMultiply(payOrderPo.getTotalRealPayMoney(), new BigDecimal(100)).intValue();
                    if(cashFee.equals(totalMoney)) {
                        //业务数据持久化
                        omOrderService.wxPayNotify(payOrderPo, response);
                    } else {
                        logger.info("微信查询订单成功，订单号:{}，但是金额不对应，回调支付金额{}，支付订单计算应支付总金额", payOrderPo, cashFee, totalMoney);
                    }
                }
            } else {
                //调用微信统一下单接口返回失败
                String errCodeDes = response.get("err_code_des");
                throw new ServiceException(ResultCode.FAIL, errCodeDes);
            }
        } else {
            //调用微信查询订单接口返回失败
            String returnMsg = response.get("return_msg");
            throw new ServiceException(ResultCode.FAIL, returnMsg);
        }

    }



    /**
     * @Author yeJH
     * @Date 2019/10/29 10:24
     * @Description 调用官方SDK申请退款
     *
     * @Update yeJH
     *
     * @param  afterSaleOrderId  isAfterSaleOrder=true  售后订单id  isAfterSaleOrder=false  订单id
     * @param  isAfterSaleOrder  是否是售后订单  true 是  false 否
     * @param  isAuto  是否自动退款
     *                 1.售后时间截止  自动退款  true
     *                 2.后台财务同意退款        false
     * @return boolean
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String refund(Long afterSaleOrderId, boolean isAfterSaleOrder, boolean isAuto)  {

        if(isAfterSaleOrder) {
            //售后订单
            OmAfterSaleOrderPo omAfterSaleOrderPo = omAfterSaleOrderMapper.selectById(afterSaleOrderId);
            if (null == omAfterSaleOrderPo) {
                throw new ServiceException(ResultCode.NO_EXISTS, "退款订单记录不存在");
            }
            //如果不是延时任务，需要判断登录用户是否有权限
            if (!isAuto) {
                //获取当前店铺用户
                SysUserPo sysUserPo = securityUtil.getCurrUser();
                if (null == sysUserPo.getStoreId() || !sysUserPo.getStoreId().equals(omAfterSaleOrderPo.getStoreId())) {
                    //当前登录用户跟操作不匹配
                    throw new ServiceException(ResultCode.FAIL, "当前登录用户跟操作不匹配");
                }
            }
            //售后订单仅退款且状态为待商家处理  或者  售后订单退货退款且状态为待商家退款 才能退款
            if (!((omAfterSaleOrderPo.getAfterSaleType() == AfterSaleTypeEnum.ONLY_REFUND &&
                    omAfterSaleOrderPo.getStatus() == AfterSaleStatusEnum.NEED_STORE_DO) ||
                    (omAfterSaleOrderPo.getAfterSaleType() == AfterSaleTypeEnum.RETURN_GOODS &&
                            omAfterSaleOrderPo.getStatus() == AfterSaleStatusEnum.NEED_STORE_REFUND))) {
                throw new ServiceException(ResultCode.FAIL, "退款操作跟当前售后订单状态不符");
            }
        } else {
            //订单
            OmOrderPo omOrderPo = omOrderService.getById(afterSaleOrderId);
            if (null == omOrderPo) {
                throw new ServiceException(ResultCode.NO_EXISTS, "订单记录不存在");
            }
        }
        WxMD5Util md5Util = wxMD5Util;
        WXConfigUtil config = wxConfigUtil;
        WXPay wxpay = new WXPay(config);

        //申请退款接口微信签名参数
        Map<String, String> data = new HashMap<>();
        //应用id APPID
        data.put("appid", config.getAppID());
        //商户号
        data.put("mch_id", config.getMchID());
        //随机字符串
        data.put("nonce_str", WXPayUtil.generateNonceStr());
        //获取退款信息  订单号  支付流水号  退款金额
        OrderRefundInfoBo orderRefundInfoBo = getRefundInfo(afterSaleOrderId, isAfterSaleOrder);
        if(null != orderRefundInfoBo.getPayOrderNo()) {
            //微信订单号
            data.put("transaction_id", orderRefundInfoBo.getPayOrderNo());
        } else {
            //商户订单号
            data.put("out_trade_no", String.valueOf(orderRefundInfoBo.getPayOrderId()));
        }
        //商户退款单号
        data.put("out_refund_no", String.valueOf(afterSaleOrderId));
        //退款金额
        String refundFee = Integer.toString(
                BigDecimalUtil.safeMultiply(orderRefundInfoBo.getRefundMoney(), new BigDecimal(100)).intValue());
        data.put("refund_fee", refundFee);
        //订单金额
        String totalFee = Integer.toString(
                BigDecimalUtil.safeMultiply(orderRefundInfoBo.getTotalRealPayMoney(), new BigDecimal(100)).intValue());
        data.put("total_fee", totalFee);
        //退款结果通知url
        //data.put("notify_url", REFUND_NOTIFY_URL);
        //申请退款签名
        String orderSign = null;
        try {
            orderSign = md5Util.getSign(data);
        } catch (Exception e) {
            LoggerUtil.error(e);
            throw new ServiceException(ResultCode.FAIL,"微信退款出错！");
        }
        data.put("sign", orderSign);

        //使用微信申请退款API请求预付订单
        Map<String, String> response = null;
        try {
            response = wxpay.refund(data);
        } catch (Exception e) {
            LoggerUtil.error(e);
            throw new ServiceException(ResultCode.FAIL,"微信退款出错！");
        }
        //获取返回码
        String returnCode = response.get("return_code");
        //若返回码return_code为SUCCESS，则会返回一个result_code,再对该result_code进行判断
        if (returnCode.equals("SUCCESS")) {
            String resultCode = response.get("result_code");
            if ("SUCCESS".equals(resultCode)) {
                return response.get("refund_id");
                //resultCode 为SUCCESS
                //更新售后订单订单
                /*UpdateWrapper<OmAfterSaleOrderPo> updateWrapper = new UpdateWrapper<>();
                updateWrapper.lambda().eq(OmAfterSaleOrderPo::getId, omAfterSaleOrderPo.getId())
                        .set(OmAfterSaleOrderPo::getRefundId, response.get("refund_id"));
                omAfterSaleOrderService.update(updateWrapper);
                omAfterSaleOrderService.permitRefund(afterSaleOrderId,isAuto);*/
            } else {
                //调用微信申请退款接口返回失败
                String errCodeDes = response.get("err_code_des");
                throw new ServiceException(ResultCode.FAIL, errCodeDes);
            }
        } else {
            //调用微信微信申请接口返回失败
            String returnMsg = response.get("return_msg");
            throw new ServiceException(ResultCode.FAIL, returnMsg);
        }
    }

    /**
     * @Author yeJH
     * @Date 2019/10/29 11:05
     * @Description 获取退款信息  订单号  支付流水号  退款金额
     *
     * @Update yeJH
     *
     * @param  afterSaleOrderId
     * @param  isAfterSaleOrder
     * @return com.chauncy.data.bo.manage.order.OrderRefundInfoBo
     **/
    private OrderRefundInfoBo getRefundInfo(Long afterSaleOrderId, boolean isAfterSaleOrder) {
        if(isAfterSaleOrder) {
            //售后订单
            return payOrderMapper.findOrderRefundInfo(afterSaleOrderId);
        } else {
            //非售后订单
            OrderRefundInfoBo orderRefundInfoBo = new OrderRefundInfoBo();
            OmOrderPo omOrderPo = omOrderService.getById(afterSaleOrderId);
            if(null != omOrderPo && null != omOrderPo.getPayOrderId()) {
                PayOrderPo payOrderPo = payOrderMapper.selectById(omOrderPo.getPayOrderId());
                orderRefundInfoBo.setPayOrderId(payOrderPo.getId());
                orderRefundInfoBo.setPayOrderNo(payOrderPo.getPayOrderNo());
                orderRefundInfoBo.setTotalRealPayMoney(payOrderPo.getTotalRealPayMoney());
                orderRefundInfoBo.setRefundMoney(payOrderPo.getTotalRealPayMoney());
                return orderRefundInfoBo;
            } else {
                throw new ServiceException(ResultCode.PARAM_ERROR, "不存在该订单或者支付单");
            }
        }
    }


}

