package com.chauncy.web.api.app.order.logistics;


import com.chauncy.common.enums.system.ResultCode;
import com.chauncy.data.bo.app.logistics.LastResultBo;
import com.chauncy.data.bo.app.logistics.TaskResponseBo;
import com.chauncy.data.dto.app.order.logistics.SynQueryLogisticsDto;
import com.chauncy.data.dto.app.order.logistics.TaskRequestDto;
import com.chauncy.data.vo.JsonViewData;
import com.chauncy.data.vo.app.order.logistics.FindLogicDetailVo;
import com.chauncy.data.vo.app.order.logistics.LogisticsCodeNumVo;
import com.chauncy.data.vo.app.order.logistics.NoticeResponseVo;
import com.chauncy.data.vo.app.order.logistics.SynQueryLogisticsVo;
import com.chauncy.order.logistics.IOmOrderLogisticsService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import io.swagger.annotations.Api;

import com.chauncy.web.base.BaseApi;

import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * 物流信息表 前端控制器
 * </p>
 *
 * @author huangwancheng
 * @since 2019-08-01
 */
@RestController
@RequestMapping("app/order/logistics")
@Api(tags = "app_订单_物流管理")
@Slf4j
public class OmOrderLogisticsApi extends BaseApi {

    @Autowired
    private IOmOrderLogisticsService service;

    /**
     * 订单订阅物流信息
     *
     * 快递100订阅请求接口
     *
     * @param taskRequestDto
     * @return
     */
    @ApiOperation("订单订阅物流信息请求接口")
    @PostMapping("/subscribe")
    public JsonViewData subscribleLogistics(@RequestBody @ApiParam(required = true, name = "taskRequestDto", value = "订单订阅物流信息")
                                                    @Validated TaskRequestDto taskRequestDto) {
        TaskResponseBo taskResponseBo = service.subscribleLogistics(taskRequestDto);
        if (taskResponseBo.getResult()){
            return setJsonViewData(ResultCode.SUCCESS,taskResponseBo.getMessage());
        }else{
            return setJsonViewData(ResultCode.FAIL,taskResponseBo.getMessage());
        }
    }

    /**
     * 快递100订阅推送数据
     * 快递结果回调接口
     *
     * @param request
     * @param orderId
     * @return
     */
    @PostMapping("/callback/{orderId}")
    @ApiOperation("快递结果回调接口")
    public JsonViewData expressCallback(HttpServletRequest request, @PathVariable String orderId) {
        String param = request.getParameter("param");
        log.info("订单物流回调开始，入参为：" + param);
        NoticeResponseVo noticeResponseVo = service.updateExpressInfo(param, orderId);
        if (noticeResponseVo.getResult()) {
            return setJsonViewData(ResultCode.SUCCESS, noticeResponseVo.getMessage());
        } else {
            return setJsonViewData(ResultCode.FAIL, noticeResponseVo.getMessage());
        }
    }

    /**
     * 根据订单号查询物流信息
     *
     * @param orderId
     * @return
     */
    @GetMapping("/findLogicDetail/{orderId}")
    @ApiOperation("根据订单号查询物流信息")
    public JsonViewData<FindLogicDetailVo> getLogistics(@PathVariable long orderId) {

        return setJsonViewData(service.getLogistics(orderId));
    }


    /**
     * 实时查询物流信息
     *
     * @param synQueryLogisticsDto
     * @return
     */
    @ApiOperation("实时查询物流信息")
    @PostMapping("/synQueryLogistics")
    public JsonViewData<SynQueryLogisticsVo> synQueryLogistics(@RequestBody @ApiParam(required = true, name = "taskRequestDto", value = "订单订阅物流信息")
                                                                         @Validated SynQueryLogisticsDto synQueryLogisticsDto) {
        return setJsonViewData(service.synQueryLogistics(synQueryLogisticsDto));
    }

    /**
     * 根据客户提交的快递单号，判断该单号可能所属的快递公司编码
     *
     * @param num
     * @return
     */
    @GetMapping("/autoFind/{num}")
    @ApiOperation("根据客户提交的快递单号，判断该单号可能所属的快递公司编码")
    public JsonViewData<LogisticsCodeNumVo> autoFind(@ApiParam(required = true,name="num",value="运单号")
                                                         @PathVariable String num){
        return setJsonViewData(service.autoFind(num));

    }
}