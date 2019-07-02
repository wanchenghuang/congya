package com.chauncy.data.dto.manage.message.information.select;

import com.chauncy.data.dto.base.BaseSearchDto;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * @author yeJH
 * @since 2019/6/28 16:49
 */
@Data
@ApiModel("资讯查询条件")
public class InformationSearchDto  extends BaseSearchDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @ApiModelProperty(value = "创建时间")
    private LocalDate startTime;

    @JsonFormat( pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @ApiModelProperty(value = "创建时间")
    private LocalDate endTime;

}
