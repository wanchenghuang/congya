package com.chauncy.data.dto.supplier.good.select;

import com.chauncy.data.dto.base.BaseSearchDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.Min;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @Author cheng
 * @create 2019-06-21 14:52
 *
 * 条件查询商品信息
 */
@Data
@ApiModel(description = "条件查询商品信息")
@Accessors(chain = true)
public class SearchGoodInfosDto {

    @Min(1)
    @ApiModelProperty(value = "页码")
    private Integer pageNo;

    @Min(1)
    @ApiModelProperty(value = "分页大小")
    private Integer pageSize;

    @ApiModelProperty(value = "店铺id")
    private Long storeId;

    @ApiModelProperty("商品名称")
    private String goodName;

    @ApiModelProperty("商品分类")
    private String categoryName;

    @ApiModelProperty("商品品牌")
    private String brandName;

    @ApiModelProperty("商品审核状态")
    private Integer verifyStatus;

    @ApiModelProperty("商品销售状态")
    private Boolean publishStatus;

    @ApiModelProperty("店铺名称")
    private String storeName;
//
//    @ApiModelProperty("提交时间")
//    private LocalDate pushTime;
//
//    @ApiModelProperty("审核时间")
//    private LocalDate verifyTime;
//
    @ApiModelProperty("最低销售价格")
    private BigDecimal lowestSellPrice;

    @ApiModelProperty("最高销售价格")
    private BigDecimal highestSellPrice;
//
    @ApiModelProperty("最低供货价格")
    private BigDecimal lowestSupplierPrice;

    @ApiModelProperty("最高销售价格")
    private BigDecimal highestSupplierPrice;
//
//    @ApiModelProperty("商品标签")
//    private String labelName;



}
