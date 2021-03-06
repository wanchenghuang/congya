package com.chauncy.data.dto.app.message.information.select;

import com.alibaba.fastjson.annotation.JSONField;
import com.chauncy.common.enums.message.InformationTypeEnum;
import com.chauncy.data.dto.base.BasePageDto;
import com.chauncy.data.dto.base.BaseSearchDto;
import com.chauncy.data.valid.annotation.EnumConstraint;
import com.chauncy.data.valid.annotation.NeedExistConstraint;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * @author yeJH
 * @since 2019/7/2 14:24
 */
@Data
@ApiModel(value = "SearchInfoByConditionDto", description = "条件查询资讯")
public class SearchInfoByConditionDto extends BasePageDto implements Serializable {

    private static final long serialVersionUID = 1L;


    /*@ApiModelProperty(value = "店铺id")
    @NeedExistConstraint(tableName = "sm_store")
    private Long storeId;*/

    @JsonIgnore
    @JSONField(serialize=false)
    @ApiModelProperty(value = "用户id 获取用户关注的店铺资讯")
    private Long userId;

    /*@ApiModelProperty(value = "资讯标签id")
    @NeedExistConstraint(tableName = "mm_information_label")
    private Long infoLabelId;*/

    @ApiModelProperty(value = "资讯分类id，informationType=4时传参")
    @NeedExistConstraint(tableName = "mm_information_category")
    private Long infoCategoryId;

    @ApiModelProperty(value = "资讯类型   \n1：全部资讯列表    \n2：关注资讯列表   \n" +
            "3：推荐资讯列表   \n4：分类资讯列表   \n5：搜索资讯列表   \n")
    @NotNull(message = "资讯列表类型不能为空")
    @EnumConstraint(target = InformationTypeEnum.class)
    private Integer informationType;

    @ApiModelProperty(value = "模糊搜索关键字，informationType=5时传参")
    private String keyword;

    @ApiModelProperty(value = "筛选条件   资讯标签id")
    private List<Long> labelIds;

    @ApiModelProperty(value = "筛选条件   内容分类id")
    private List<Long> categoryIds;


}
