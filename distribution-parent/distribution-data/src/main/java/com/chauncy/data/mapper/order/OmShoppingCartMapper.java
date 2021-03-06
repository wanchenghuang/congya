package com.chauncy.data.mapper.order;

import com.chauncy.data.domain.po.order.OmShoppingCartPo;
import com.chauncy.data.domain.po.user.UmUserPo;
import com.chauncy.data.dto.app.car.ShopTicketSoWithCarGoodDto;
import com.chauncy.data.mapper.IBaseMapper;
import com.chauncy.data.vo.app.car.CarGoodsVo;
import com.chauncy.data.vo.app.car.ShopTicketSoWithCarGoodVo;
import com.chauncy.data.vo.app.order.cart.CartVo;
import com.chauncy.data.vo.app.order.cart.StoreGoodsVo;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 购物车列表 Mapper 接口
 * </p>
 *
 * @author huangwancheng
 * @since 2019-07-04
 */
public interface OmShoppingCartMapper extends IBaseMapper<OmShoppingCartPo> {

    /**
     * 查看购物车
     *
     * @param userId
     * @return
     */
    List<CartVo> searchCart(@Param("userId") Long userId);

    /**
     * 根据店铺和商品类型拆单
     * @param ids
     * @return
     */
    List<ShopTicketSoWithCarGoodVo> searchByIds(@Param("ids") List<Long> ids);

    /**
     * 查出需要算出预返购物券的相关属性
     * @param id
     * @return
     */
    ShopTicketSoWithCarGoodDto getTempById(Long id);

    /**
     * 减去红包和购物券
     * @param
     * @return
     */
    int updateDiscount(@Param("redEnvelops") BigDecimal redEnvelops,@Param("shopTicket") BigDecimal shopTicket,
    @Param("integral") BigDecimal integral,
    @Param("id") Long id
    );

    /**
     * @Author chauncy
     * @Date 2019-09-25 20:24
     * @Description //获取失效商品
     *
     * @Update chauncy
     *
     * @Param [id]
     * @return java.util.List<com.chauncy.data.vo.app.order.cart.StoreGoodsVo>
     **/
    List<StoreGoodsVo> searchDisableList(Long id);
}
