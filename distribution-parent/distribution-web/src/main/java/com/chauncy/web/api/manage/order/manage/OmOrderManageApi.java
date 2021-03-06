package com.chauncy.web.api.manage.order.manage;


import com.chauncy.data.domain.po.user.UmUserPo;
import com.chauncy.data.dto.manage.order.select.SearchOrderDto;
import com.chauncy.data.vo.JsonViewData;
import com.chauncy.data.vo.manage.order.list.OrderDetailVo;
import com.chauncy.data.vo.manage.order.list.SearchOrderVo;
import com.chauncy.order.service.IOmOrderService;
import com.chauncy.web.base.BaseApi;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 订单管理
 * </p>
 *
 * @author huangwancheng
 * @since 2019-07-20
 */
@RestController
@RequestMapping("/manage/order")
@Api(tags = "平台_订单管理")
public class OmOrderManageApi extends BaseApi {

    @Autowired
    private IOmOrderService orderService;


    @ApiOperation(value = "查询订单列表")
    @PostMapping("/list")
    public JsonViewData<PageInfo<SearchOrderVo>> search(@RequestBody SearchOrderDto searchOrderDto) {
        PageInfo<SearchOrderVo> search = orderService.search(searchOrderDto);
        return setJsonViewData(search);
    }

    @ApiOperation(value = "查询订单详情")
    @PostMapping("/{id}")
    public JsonViewData<OrderDetailVo> search(@PathVariable Long id) {

        return setJsonViewData(orderService.getDetailById(id));
    }


}
