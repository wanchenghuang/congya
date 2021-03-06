package com.chauncy.data.vo.manage.product;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * @Author huangwancheng
 * @create 2019-06-05 15:47
 *
 * 商品基本信息Vo
 */
@Data
public class PmGoodsBaseVo {

    @ApiModelProperty(value = "名称")
    @NotBlank(message = "名称不能为空")
    private String name;

    @ApiModelProperty(value = "排序数字，此排序是展示在前端服务说明的排序")
    @Min(value = 0,message = "排序数字必须大于0")
    private BigDecimal sort;

    @ApiModelProperty(value = "服务说明内容")
    private String content;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "是否启用 1-是 0-否 默认为1")
    @NotNull(message = "启用状态不能为空!")
    private Boolean enabled;

    @ApiModelProperty(value = "类型 1->平台服务说明管理类型 2->商家服务说明管理类型 3->平台活动说明管理类型  4->商品参数管理类型 5->标签管理类型 6->购买须知管理类型 7->规格管理类型 8->品牌管理")
    private Integer type;

    @ApiModelProperty(value = "副标题")
    private String subtitle;

    @ApiModelProperty(value = "logo图片")
    private String logoImage;

    @ApiModelProperty(value = "logo缩略图")
    private String logoIcon;

    @ApiModelProperty(value = "商品属性值")
    private List<String> valueList;
}
