package com.chauncy.web.api.app.activity.integral;

import com.chauncy.common.enums.system.ResultCode;
import com.chauncy.data.dto.app.product.SearchActivityGoodsListDto;
import com.chauncy.data.vo.JsonViewData;
import com.chauncy.data.vo.app.advice.activity.ActivityGroupDetailVo;
import com.chauncy.data.vo.app.advice.activity.ActivityGroupListVo;
import com.chauncy.data.vo.app.goods.ActivityGoodsVo;
import com.chauncy.message.advice.IMmAdviceService;
import com.chauncy.web.base.BaseApi;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * @author yeJH
 * @since 2019/9/24 16:12
 */
@Api(tags = "APP_活动_积分/满减")
@RestController
@RequestMapping("/app/activity/integral")
@Slf4j
public class AmIntegralMainApi extends BaseApi {


    @Autowired
    private IMmAdviceService adviceService;

    /**
     * @Author yeJH
     * @Date 2019/9/24 16:27
     * @Description 点击积分专区，满减专区获取活动分组信息
     *
     * @Update yeJH
     *
     * @param  groupType  活动分组类型 1：满减  2：积分
     * @return com.chauncy.data.vo.JsonViewData<java.util.List<com.chauncy.data.vo.app.advice.activity.ActivityGroupListVo>>
     **/
    @GetMapping("/findActivityGroup/{groupType}")
    @ApiOperation(value = "点击积分专区，满减专区获取活动分组信息")
    public JsonViewData<List<ActivityGroupListVo>> findActivityGroup(@ApiParam(required = true,
            value = "活动分组类型 1：满减  2：积分", name = "groupType") @PathVariable Integer groupType){

        List<ActivityGroupListVo> activityGroupListVoList = adviceService.findActivityGroup(groupType);
        return new JsonViewData(ResultCode.SUCCESS,"查找成功", activityGroupListVoList);
    }

    /**
     * @Author yeJH
     * @Date 2019/9/24 18:01
     * @Description 根据活动分组关联表id获取活动分组详情
     *              获取选项卡信息（满减：热销精选；积分：精选商品）
     *              获取轮播图信息
     *
     * @Update yeJH
     *
     * @param  relId  广告与活动分组关联表id
     * @return com.chauncy.data.vo.JsonViewData<java.util.List<com.chauncy.data.vo.app.advice.activity.ActivityGroupDetailVo>>
     **/
    @GetMapping("/findActivityGroupDetail/{relId}")
    @ApiOperation(value = "根据活动分组获取活动分组详情")
    public JsonViewData<ActivityGroupDetailVo> findActivityGroupDetail(@ApiParam(required = true,
            value = "广告与活动分组关联表id", name = "relId") @PathVariable Long relId){

        ActivityGroupDetailVo activityGroupDetailVo = adviceService.findActivityGroupDetail(relId);
        return new JsonViewData(ResultCode.SUCCESS,"查找成功", activityGroupDetailVo);
    }

    /**
     * @Author yeJH
     * @Date 2019/9/25 16:14
     * @Description 获取活动商品列表
     *
     * @Update yeJH
     *
     * @param  searchActivityGoodsListDto  查询积分/满减活动商品列表参数
     * @return com.chauncy.data.vo.JsonViewData<com.github.pagehelper.PageInfo<com.chauncy.data.vo.app.goods.ActivityGoodsVo>>
     **/
    @GetMapping("/findTabGoodsList")
    @ApiOperation(value = "根据选项卡id 获取活动商品列表")
    public JsonViewData<PageInfo<ActivityGoodsVo>> findTabGoodsList(@Valid @RequestBody @ApiParam(required = true,
            name = "searchActivityGoodsListDto", value = "查询积分/满减活动商品列表参数")
            SearchActivityGoodsListDto searchActivityGoodsListDto){

        return new JsonViewData(ResultCode.SUCCESS,"查找成功",
                adviceService.findTabGoodsList(searchActivityGoodsListDto));
    }



}