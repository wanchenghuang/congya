package com.chauncy.web.api.app.activity.spell;

import com.chauncy.activity.spell.IAmSpellGroupService;
import com.chauncy.common.enums.system.ResultCode;
import com.chauncy.data.domain.po.user.UmUserPo;
import com.chauncy.data.dto.app.activity.SearchMySpellGroupDto;
import com.chauncy.data.dto.app.activity.SearchSpellGroupInfoDto;
import com.chauncy.data.dto.app.product.SearchSpellGroupGoodsDto;
import com.chauncy.data.vo.BaseVo;
import com.chauncy.data.vo.JsonViewData;
import com.chauncy.data.vo.app.activity.spell.MySpellGroupVo;
import com.chauncy.data.vo.app.activity.spell.SpellGroupDetailVo;
import com.chauncy.data.vo.app.activity.spell.SpellGroupInfoVo;
import com.chauncy.data.vo.app.goods.SpellGroupGoodsVo;
import com.chauncy.security.util.SecurityUtil;
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
 * @since 2019/9/30 16:59
 */
@Api(tags = "APP_活动_拼团")
@RestController
@RequestMapping("/app/activity/spell")
@Slf4j
public class AsSpellGroupApi  extends BaseApi {

    @Autowired
    private IAmSpellGroupService amSpellGroupService;

    @Autowired
    private SecurityUtil securityUtil;


    /**
     * @Author yeJH
     * @Date 2019/9/25 16:14
     * @Description 获取拼团活动商品列表
     *
     * @Update yeJH
     *
     * @param  searchSpellGroupGoodsDto  查询拼团活动商品列表参数
     * @return com.chauncy.data.vo.JsonViewData<com.github.pagehelper.PageInfo<com.chauncy.data.vo.app.goods.ActivityGoodsVo>>
     **/
    @PostMapping("/searchActivityGoodsList")
    @ApiOperation(value = "获取拼团动商品列表")
    public JsonViewData<PageInfo<SpellGroupGoodsVo>> searchActivityGoodsList(
            @ApiParam(required = true, name = "searchSpellGroupGoodsDto", value = "查询拼团活动商品列表参数")
            @Valid @RequestBody SearchSpellGroupGoodsDto searchSpellGroupGoodsDto){

        return new JsonViewData(ResultCode.SUCCESS,"查找成功",
                amSpellGroupService.searchActivityGoodsList(searchSpellGroupGoodsDto));
    }


    /**
     * @Author yeJH
     * @Date 2019/9/25 16:14
     * @Description 获取拼团活动商品一级分类
     *
     * @Update yeJH
     *
     * @return com.chauncy.data.vo.JsonViewData<java.util.List<com.chauncy.data.vo.BaseVo>>
     **/
    @PostMapping("/findGoodsCategory")
    @ApiOperation(value = "获取拼团活动商品一级分类")
    public JsonViewData<List<BaseVo>> findGoodsCategory() {

        return new JsonViewData(ResultCode.SUCCESS,"查找成功",
                amSpellGroupService.findGoodsCategory());
    }


    /**
     * @Author yeJH
     * @Date 2019/10/6 18:21
     * @Description 根据商品id获取拼团信息
     *
     * @Update yeJH
     *
     * @param  searchSpellGroupInfoDto
     * @return com.chauncy.data.vo.JsonViewData<com.github.pagehelper.PageInfo<com.chauncy.data.vo.app.activity.spell.SpellGroupInfoVo>>
     **/
    @PostMapping("/searchSpellGroupInfo")
    @ApiOperation(value = "根据商品id获取拼团信息")
    public JsonViewData<PageInfo<SpellGroupInfoVo>> searchSpellGroupInfo(
            @ApiParam(required = true, name = "searchSpellGroupInfoDto", value = "根据商品查询拼团信息")
            @Valid @RequestBody SearchSpellGroupInfoDto searchSpellGroupInfoDto) {

        UmUserPo currentUser = securityUtil.getAppCurrUser();

        return new JsonViewData(ResultCode.SUCCESS,"查找成功",
                amSpellGroupService.searchSpellGroupInfo(searchSpellGroupInfoDto, currentUser));
    }

    /**
     * @Author yeJH
     * @Date 2019/10/6 23:46
     * @Description 查询我的拼团
     *
     * @Update yeJH
     *
     * @param  searchMySpellGroupDto
     * @return com.chauncy.data.vo.JsonViewData<com.github.pagehelper.PageInfo<com.chauncy.data.vo.app.activity.spell.MySpellGroupVo>>
     **/
    @PostMapping("/searchMySpellGroup")
    @ApiOperation(value = "查询我的拼团")
    public JsonViewData<PageInfo<MySpellGroupVo>> searchMySpellGroup(
            @ApiParam(required = true, name = "searchMySpellGroupDto", value = "查询我的拼团")
            @Valid @RequestBody SearchMySpellGroupDto searchMySpellGroupDto) {

        return new JsonViewData(ResultCode.SUCCESS,"查找成功",
                amSpellGroupService.searchMySpellGroup(searchMySpellGroupDto));
    }

    /**
     * @Author yeJH
     * @Date 2019/10/7 21:28
     * @Description 获取拼团详情
     *
     * @Update yeJH
     *
     * @param relId 用户拼团id
     * @return com.chauncy.data.vo.JsonViewData<com.chauncy.data.vo.app.activity.spell.SpellGroupDetailVo>
     **/
    @PostMapping("/getSpellGroupDetail/{relId}")
    @ApiOperation(value = "获取拼团详情")
    public JsonViewData<SpellGroupDetailVo> getSpellGroupDetail(@ApiParam(required = true,name = "relId",value = "用户拼团id")
                                                                    @PathVariable Long relId) {

        return new JsonViewData(ResultCode.SUCCESS,"查找成功",
                amSpellGroupService.getSpellGroupDetail(relId));
    }

    /**
     * @Author chauncy
     * @Date 2019-10-10 13:39
     * @Description //获取拼团主页面中的今日必拼的三个商品
     *
     * @Update chauncy
     *
     * @param
     * @return com.chauncy.data.vo.JsonViewData<com.chauncy.data.vo.app.goods.SpellGroupGoodsVo>
     **/
    @ApiOperation("获取拼团主页面中的今日必拼的三个商品")
    @GetMapping("/findTodaySpell")
    public JsonViewData<List<SpellGroupGoodsVo>> findTodaySpell(){

        return setJsonViewData(amSpellGroupService.findTodaySpell());
    }

    /**
     * @Author chauncy
     * @Date 2019-10-10 15:49
     * @Description //获取今日必拼商品相关的类目
     *
     * @Update chauncy
     *
     * @param
     * @return com.chauncy.data.vo.JsonViewData<java.util.List<com.chauncy.data.vo.BaseVo>>
     **/
    @GetMapping("/findTodaySpellCategory")
    @ApiOperation("获取今日必拼商品一级类目")
    public JsonViewData<List<BaseVo>> findTodaySpellCategory(){

        return setJsonViewData(amSpellGroupService.findTodaySpellCategory());
    }

    /**
     * @Author chauncy
     * @Date 2019-10-10 16:00
     * @Description //查询今日必拼商品列表参数
     *
     * @Update chauncy
     *
     * @param  searchTodaySpellGoods
     * @return com.chauncy.data.vo.JsonViewData<com.github.pagehelper.PageInfo<com.chauncy.data.vo.app.goods.SpellGroupGoodsVo>>
     **/
    @ApiOperation("查询今日必拼商品列表参数")
    @PostMapping("/searchTodaySpellGoods")
    public JsonViewData<PageInfo<SpellGroupGoodsVo>> searchTodaySpellGoods(
            @ApiParam(required = true, name = "searchTodaySpellGoods", value = "查询今日必拼商品列表参数")
            @Valid @RequestBody SearchSpellGroupGoodsDto searchTodaySpellGoods){

        return new JsonViewData(ResultCode.SUCCESS,"查找成功",
                amSpellGroupService.searchTodaySpellGoods(searchTodaySpellGoods));
    }
}
