package com.chauncy.data.mapper.activity.coupon;

import com.chauncy.data.domain.po.activity.coupon.AmCouponRelCouponGoodsPo;
import com.chauncy.data.mapper.IBaseMapper;
import com.chauncy.data.vo.app.advice.coupon.FindCouponListVo;

import java.util.List;

/**
 * <p>
 * 优惠券和商品关联表 Mapper 接口
 * </p>
 *
 * @author huangwancheng
 * @since 2019-07-18
 */
public interface AmCouponRelCouponGoodsMapper extends IBaseMapper<AmCouponRelCouponGoodsPo> {

    /**
     * @Author chauncy
     * @Date 2019-09-27 21:36
     * @Description //查询商品关联的满减、固定折扣形式优惠券
     *
     * @Update chauncy
     *
     * @param  goodsId
     * @return java.util.List<com.chauncy.data.vo.app.advice.coupon.FindCouponListVo>
     **/
    List<FindCouponListVo> findCouponList(Long goodsId);

    /**
     * @Author chauncy
     * @Date 2019-09-27 21:38
     * @Description //优惠券列表--包邮-指定全部商品
     *
     * @Update chauncy
     *
     * @param  goodsId
     * @return java.util.List<com.chauncy.data.vo.app.advice.coupon.FindCouponListVo>
     **/
    List<FindCouponListVo> findCouponList1(Long goodsId);

    /**
     * @Author chauncy
     * @Date 2019-09-27 21:39
     * @Description //优惠券列表--包邮-指定分类
     *
     * @Update chauncy
     *
     * @param  goodsCategoryId
     * @return java.util.List<com.chauncy.data.vo.app.advice.coupon.FindCouponListVo>
     **/
    List<FindCouponListVo> findCouponList2(Long goodsCategoryId);
}
