package com.chauncy.data.dto.manage.message.information.select;

import com.chauncy.data.dto.base.BaseSearchDto;
import com.chauncy.data.valid.annotation.NeedExistConstraint;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author yeJH
 * @since 2019/7/1 14:31
 */
public class InformationCommentDto extends BaseSearchDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "资讯id")
    @NeedExistConstraint(tableName = "mm_information")
    @NotNull(message = "id不能为空")
    private Long id;

}
