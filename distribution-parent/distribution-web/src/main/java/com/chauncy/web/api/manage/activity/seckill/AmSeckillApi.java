package com.chauncy.web.api.manage.activity.seckill;


import com.chauncy.activity.integrals.IAmIntegralsService;
import com.chauncy.activity.registration.IAmActivityRelActivityGoodsService;
import com.chauncy.common.enums.system.ResultCode;
import com.chauncy.data.dto.manage.activity.EditEnableDto;
import com.chauncy.data.dto.manage.activity.SearchActivityListDto;
import com.chauncy.data.dto.manage.activity.SearchCategoryByActivityIdDto;
import com.chauncy.data.dto.manage.activity.reduced.add.SaveReducedDto;
import com.chauncy.data.dto.manage.activity.seckill.SaveSeckillDto;
import com.chauncy.data.vo.JsonViewData;
import com.chauncy.data.vo.manage.activity.SearchActivityListVo;
import com.chauncy.data.vo.manage.activity.SearchCategoryByActivityIdVo;
import com.chauncy.data.vo.manage.activity.group.FindActivityGroupsVo;
import com.chauncy.data.vo.supplier.MemberLevelInfos;
import com.chauncy.web.base.BaseApi;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import io.swagger.annotations.Api;
import com.chauncy.activity.seckill.IAmSeckillService;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 秒杀活动管理 前端控制器
 * </p>
 *
 * @author huangwancheng
 * @since 2019-07-23
 */
@RestController
@RequestMapping("/manage/activity/seckill")
@Api(tags = "平台—活动管理-秒杀活动管理")
public class AmSeckillApi extends BaseApi {

    @Autowired
    private IAmSeckillService service;

    @Autowired
    private IAmIntegralsService integralsService;

    @Autowired
    private IAmActivityRelActivityGoodsService relActivityGoodsService;

    /**
     * 获取分类信息
     *
     * @param searchCategoryByActivityIdDto
     * @return
     */
//    @PostMapping("/searchCategory")
    @ApiOperation("获取分类信息")
    public JsonViewData<SearchCategoryByActivityIdVo> searchCategory(@RequestBody @ApiParam(required = true, name = "searchCategoryByActivityIdDto", value = "分类列表查询条件")
                                                                     @Validated SearchCategoryByActivityIdDto searchCategoryByActivityIdDto) {
        Map<String, Object> map = Maps.newHashMap();
        try {
            map = integralsService.searchCategory(searchCategoryByActivityIdDto);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return setJsonViewData(map);

    }

    /**
     * 获取全部会员ID和名称
     * @return
     */
//    @ApiOperation("获取全部会员ID和名称")
    @GetMapping("/findAllMemberLevel")
    public JsonViewData<List<MemberLevelInfos>> findAllMemberLevel(){

        return new JsonViewData(service.findAllMemberLevel());
    }

    /**
     * 获取全部可用的分组
     * @return
     */
    /*@ApiOperation("获取全部可用的分组")
    @GetMapping("/findAllActivityGroup")
    public JsonViewData<List<FindActivityGroupsVo>> findAllActivityGroup(){

        return new JsonViewData(service.FindAllActivityGroup());
    }*/

    /**
     * 保存秒杀活动信息
     * @param saveSeckillDto
     * @return
     */
    @PostMapping("/saveSeckill")
    @ApiOperation("保存秒杀活动信息")
    public JsonViewData saveSeckill(@RequestBody @ApiParam(required = true,name="saveSeckillDto",value="保存秒杀活动信息")
                                    @Validated SaveSeckillDto saveSeckillDto){

        service.saveReduced(saveSeckillDto);
        return setJsonViewData(ResultCode.SUCCESS,"保存成功");
    }

    /**
     * 条件查询秒杀活动信息
     *
     * @param searchActivityListDto
     * @return
     */
    @ApiOperation("条件查询秒杀活动信息")
    @PostMapping("/searchSeckillList")
    public JsonViewData<PageInfo<SearchActivityListVo>> searchSeckillList(@RequestBody @ApiParam(required = true,name="searchActivityListDto",value = "条件查询秒杀活动信息")
                                                                         @Validated SearchActivityListDto searchActivityListDto){

        return setJsonViewData(service.searchSeckillList(searchActivityListDto));
    }

    /**
     * 批量删除活动
     *
     * @param ids
     * @return
     */
    @ApiOperation("批量删除活动")
    @GetMapping("/delByIds/{ids}")
    public JsonViewData delByIds(@ApiParam(required = true,name="ids",value = "活动ID")
                                 @PathVariable List<Long> ids){
        service.delByIds(ids);
        return setJsonViewData(ResultCode.SUCCESS,"删除成功");
    }

    /**
     * 批量结束
     * @param enableDto
     * @return
     */
    @ApiOperation("批量禁用启用")
    @PostMapping("/editEnable")
    public JsonViewData editEnable(@RequestBody @ApiParam(required = true,name="enableDto",value="批量禁用启用")
                                   @Validated EditEnableDto enableDto){
        service.editEnable(enableDto);
        //判断该满减活动ID是否在am_activity_rel_activity_goods表中存在,true则更新verify_status状态为6--活动已被禁用
        relActivityGoodsService.updateStatusByEnable(enableDto);
        return setJsonViewData(ResultCode.SUCCESS,"修改成功");
    }

}
