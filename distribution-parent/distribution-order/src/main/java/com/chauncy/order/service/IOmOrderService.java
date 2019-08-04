package com.chauncy.order.service;

import com.chauncy.data.domain.po.order.OmOrderPo;
import com.chauncy.data.core.Service;
import com.chauncy.data.domain.po.pay.PayOrderPo;
import com.chauncy.data.domain.po.user.UmUserPo;
import com.chauncy.data.dto.manage.order.select.SearchOrderDto;
import com.chauncy.data.dto.supplier.order.SmSearchOrderDto;
import com.chauncy.data.dto.supplier.order.SmSendOrderDto;
import com.chauncy.data.vo.manage.order.list.OrderDetailVo;
import com.chauncy.data.vo.manage.order.list.SearchOrderVo;
import com.chauncy.data.vo.supplier.order.SmOrderDetailVo;
import com.chauncy.data.vo.supplier.order.SmSearchOrderVo;
import com.chauncy.data.vo.supplier.order.SmSendOrderVo;
import com.github.pagehelper.PageInfo;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * <p>
 * 订单表 服务类
 * </p>
 *
 * @author huangwancheng
 * @since 2019-06-28
 */
public interface IOmOrderService extends Service<OmOrderPo> {

    /**
     * 取消整个支付订单
     * @param payOrderId
     */
    boolean closeOrderByPayId(Long payOrderId);

    /**
     * 取消单个订单
     * @param orderId
     */
    boolean closeOrderByOrderId(Long orderId);

    /**
     * 支付成功通知
     * @param payOrderPo  支付订单
     * @param notifyMap  微信回调参数
     * @throws Exception
     */
    void wxPayNotify(PayOrderPo payOrderPo, Map<String, String> notifyMap) throws Exception;

    /**
     *总后台订单列表
     * @param searchOrderDto
     * @return
     */
    PageInfo<SearchOrderVo> search(SearchOrderDto searchOrderDto);

    /**
     *商家端订单列表
     * @param smSearchOrderDto
     * @return
     */
    PageInfo<SmSearchOrderVo> searchSmOrderList(SmSearchOrderDto smSearchOrderDto);

    /**
     *商家端发货订单列表
     * @param smSendOrderDto
     * @return
     */
    PageInfo<SmSendOrderVo> searchSmSendOrderList(SmSendOrderDto smSendOrderDto);

    /**
     * 查询平台订单详情
     * @param id
     * @return
     */
    OrderDetailVo getDetailById(@Param("id") Long id);

    /**
     * 查询店铺订单详情
     * @param id
     * @return
     */
    SmOrderDetailVo getSmDetailById(@Param("id") Long id);






}
