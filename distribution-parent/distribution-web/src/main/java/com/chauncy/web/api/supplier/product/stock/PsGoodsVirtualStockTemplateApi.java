package com.chauncy.web.api.supplier.product.stock;


import com.chauncy.common.enums.system.ResultCode;
import com.chauncy.data.bo.base.BaseBo;
import com.chauncy.data.dto.base.BaseSearchByTimeDto;
import com.chauncy.data.dto.base.BaseSearchDto;
import com.chauncy.data.dto.supplier.good.add.StockTemplateBaseDto;
import com.chauncy.data.dto.supplier.store.update.SelectStockTemplateGoodsDto;
import com.chauncy.data.valid.group.IUpdateGroup;
import com.chauncy.data.vo.JsonViewData;
import com.chauncy.data.vo.supplier.PmGoodsVo;
import com.chauncy.data.vo.supplier.good.stock.GoodsStockTemplateVo;
import com.chauncy.data.vo.supplier.good.stock.StockTemplateGoodsInfoVo;
import com.chauncy.product.service.IPmGoodsService;
import com.chauncy.product.stock.IPmGoodsVirtualStockTemplateService;
import com.chauncy.web.base.BaseApi;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * <p>
 * 商品虚拟库存模板信息表 前端控制器
 * </p>
 *
 * @author huangwancheng
 * @since 2019-07-08
 */
@RestController
@RequestMapping("/supplier/product/stock/template")
@Api(tags = "商家_库存管理_库存模板管理接口")
public class PsGoodsVirtualStockTemplateApi extends BaseApi {

    @Autowired
    private IPmGoodsVirtualStockTemplateService pmGoodsVirtualStockTemplateService;
    @Autowired
    private IPmGoodsService pmGoodsService;

    /**
     * 保存库存模板信息
     * @param stockTemplateBaseDto
     * @return
     */
    @PostMapping("/save")
    @ApiOperation(value = "保存库存模板信息")
    @Transactional(rollbackFor = Exception.class)
    public JsonViewData save(@Valid @RequestBody @ApiParam(required = true, name = "stockTemplateBaseDto", value = "库存模板信息")
                                     StockTemplateBaseDto stockTemplateBaseDto) {


        return new JsonViewData(ResultCode.SUCCESS, "添加成功",
                pmGoodsVirtualStockTemplateService.saveStockTemplate(stockTemplateBaseDto));
    }

    /**
     * 编辑库存模板信息
     * @param stockTemplateBaseDto
     * @return
     */
    /*@PostMapping("/edit")
    @ApiOperation(value = "编辑库存模板信息")
    @Transactional(rollbackFor = Exception.class)
    public JsonViewData edit(@Validated(IUpdateGroup.class)  @RequestBody @ApiParam(required = true, name = "stockTemplateBaseDto", value = "库存模板信息")
                                     StockTemplateBaseDto stockTemplateBaseDto) {


        return new JsonViewData(ResultCode.SUCCESS, "编辑成功",
                pmGoodsVirtualStockTemplateService.editStockTemplate(stockTemplateBaseDto));
    }*/

    /**
     * 库存模板根据商品类型查询店铺商品信息
     *
     * @param selectStockTemplateGoodsDto
     * @return
     */
    @PostMapping("/selectGoodsByType")
    @ApiOperation(value = "库存模板根据商品类型查询店铺商品信息 ")
    public JsonViewData<PageInfo<StockTemplateGoodsInfoVo >> selectGoodsByType(@RequestBody @ApiParam(name = "selectStockTemplateGoodsDto", value = "根据商品类型查询店铺商品信息")
                                                    @Validated SelectStockTemplateGoodsDto selectStockTemplateGoodsDto){

        return new JsonViewData(ResultCode.SUCCESS,"操作成功",
                pmGoodsService.selectGoodsByType(selectStockTemplateGoodsDto));
    }

    /**
     * 库存模板根据模板id查询店铺商品详细信息
     *
     * @param baseSearchDto
     * @return
     */
    @PostMapping("/searchGoodsInfoByTemplateId")
    @ApiOperation(value = "库存模板根据模板id查询店铺商品详细信息")
    public JsonViewData<GoodsStockTemplateVo> searchGoodsInfoByTemplateId(@RequestBody @ApiParam(name = "baseSearchDto",value = "模板id查询店铺商品详细信息")
                                                                         @Validated BaseSearchDto baseSearchDto){

        return new JsonViewData(ResultCode.SUCCESS,"操作成功",
                pmGoodsService.searchGoodsInfoByTemplateId(baseSearchDto));
    }

    /**
     * 根据Id删除库存模板
     *
     * @param ids
     */
    @ApiOperation(value = "根据Id删除库存模板", notes = "根据Id删除")
    @GetMapping("/delTemplateById/{ids}")
    public JsonViewData delTemplateById(@ApiParam(required = true, name = "ids", value = "ids")
                                 @PathVariable Long[] ids) {

        pmGoodsVirtualStockTemplateService.delTemplateById(ids);
        return new JsonViewData(ResultCode.SUCCESS, "删除成功");
    }


    /**
     * 根据relId删除商品与库存模板的关联
     *
     * @param ids
     */
    @ApiOperation(value = "删除商品与库存模板的关联", notes = "根据relId删除")
    @GetMapping("/delRelById/{ids}")
    public JsonViewData delRelById(@ApiParam(required = true, name = "ids", value = "ids")
                                 @PathVariable Long[] ids) {

        pmGoodsVirtualStockTemplateService.delRelById(ids);
        return new JsonViewData(ResultCode.SUCCESS, "删除成功");
    }

    /**
     * 分页条件查询
     * @param baseSearchByTimeDto
     * @return
     */
    @ApiOperation(value = "分页条件查询", notes = "根据模板名称以及创建时间查询")
    @PostMapping("/searchPaging")
    public JsonViewData<PageInfo<GoodsStockTemplateVo>> searchPaging(@RequestBody BaseSearchByTimeDto baseSearchByTimeDto) {

        PageInfo<GoodsStockTemplateVo> goodsStockTemplateVoPageInfo = pmGoodsVirtualStockTemplateService.searchPaging(baseSearchByTimeDto);
        return new JsonViewData(ResultCode.SUCCESS, "查询成功",
                goodsStockTemplateVoPageInfo);
    }



}
