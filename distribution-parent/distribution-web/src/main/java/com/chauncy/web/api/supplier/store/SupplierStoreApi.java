package com.chauncy.web.api.supplier.store;

import com.chauncy.common.enums.system.ResultCode;
import com.chauncy.data.dto.manage.good.add.GoodAttributeDto;
import com.chauncy.data.dto.manage.store.select.StoreSearchDto;
import com.chauncy.data.dto.supplier.store.update.StoreBusinessLicenseDto;
import com.chauncy.data.vo.JsonViewData;
import com.chauncy.data.vo.manage.store.SmStoreBaseVo;
import com.chauncy.data.vo.manage.store.StoreAccountInfoVo;
import com.chauncy.data.vo.manage.store.StoreBaseInfoVo;
import com.chauncy.data.vo.manage.store.StoreOperationalInfoVo;
import com.chauncy.store.service.ISmStoreService;
import com.chauncy.web.base.BaseApi;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @author yeJH
 * @since 2019/6/19 21:32
 */
@Api(tags = "商家_店铺管理接口")
@RestController
@RequestMapping("/supplier/store")
@Slf4j
public class SupplierStoreApi extends BaseApi {

    @Autowired
    private ISmStoreService smStoreService;

    /**
     * 查询店铺基本信息
     * @param id
     * @return
     */
    @ApiOperation(value = "查询店铺基本信息", notes = "根据店铺ID获取店铺基本信息")
    @ApiImplicitParam(name = "id", value = "店铺id", dataType = "Long", paramType = "path")
    @RequestMapping(value = {"/findBaseById/{id}", "/findBaseById"}, method = {RequestMethod.GET})
    public JsonViewData<StoreBaseInfoVo> findBaseById(@PathVariable(required = false) Long id) {

        return new JsonViewData(ResultCode.SUCCESS, "查找成功",
                smStoreService.findBaseById(id));

    }

    /**
     * 查询店铺账户信息
     * @param id
     * @return
     */
    @ApiOperation(value = "查询店铺账户信息", notes = "根据店铺ID获取店铺账户信息")
    @ApiImplicitParam(name = "id", value = "店铺id", dataType = "Long", paramType = "path")
    @RequestMapping(value = {"/findAccountById/{id}", "/findAccountById"}, method = {RequestMethod.GET})
    public JsonViewData<StoreAccountInfoVo> findAccountById(@PathVariable(value = "id", required = false)Long id) {

        return new JsonViewData(ResultCode.SUCCESS, "查找成功",
                smStoreService.findAccountById(id));

    }

    /**
     * 查询店铺运营信息
     * @param id
     * @return
     */
    @ApiOperation(value = "查询店铺运营信息", notes = "根据店铺ID获取店铺运营信息")
    @ApiImplicitParam(name = "id", value = "店铺id", required = true, dataType = "Long", paramType = "path")
    @RequestMapping(value = {"/findOperationalById/{id}", "/findOperationalById"}, method = {RequestMethod.GET})
    public JsonViewData<StoreOperationalInfoVo> findOperationalById(@PathVariable(value = "id", required = false)Long id) {

        return new JsonViewData(ResultCode.SUCCESS, "查找成功",
                smStoreService.findOperationalById(id));

    }

    /**
     * 商家上传经营资质
     * @param storeBusinessLicenseDto
     * @return
     */
    @ApiOperation(value = "商家上传经营资质", notes = "商家上传经营资质")
    @PostMapping("/uploadLicense")
    public JsonViewData uploadBusinessLicense(@Valid @RequestBody  @ApiParam(required = true, name = "storeBusinessLicenseDto", value = "店铺资质证书")
                                                      StoreBusinessLicenseDto storeBusinessLicenseDto) {

        smStoreService.uploadBusinessLicense(storeBusinessLicenseDto);
        return new JsonViewData(ResultCode.SUCCESS, "编辑成功");

    }




}
