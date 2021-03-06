package com.chauncy.data.domain.po.order;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 'App用户提现信息表
 * </p>
 *
 * @author huangwancheng
 * @since 2019-07-26
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("om_user_withdrawal")
@ApiModel(value = "OmUserWithdrawalPo对象", description = "App用户提现信息表")
public class OmUserWithdrawalPo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id")
    @TableId(value = "id", type = IdType.ID_WORKER)
    private Long id;

    @ApiModelProperty(value = "用户id")
    private Long umUserId;

    @ApiModelProperty(value = "提现对应红包数额")
    private BigDecimal equalAmount;

    @ApiModelProperty(value = "提现金额")
    private BigDecimal withdrawalAmount;

    @ApiModelProperty(value = "实际应发金额")
    private BigDecimal actualAmount;

    @ApiModelProperty(value = "平台备注")
    private String remark;

    @ApiModelProperty(value = "状态 1.待审核 2.处理中 3.提现成功 4.驳回")
    private Integer withdrawalStatus;

    @ApiModelProperty(value = "驳回原因")
    private String rejectReason;

    @ApiModelProperty(value = "真实姓名")
    private String realName;

    @ApiModelProperty(value = "微信账号")
    private String wechat;

    @ApiModelProperty(value = "支付宝账号")
    private String alipay;

    @ApiModelProperty(value = "提现方式  1.微信  2.支付宝")
    private Integer withdrawalWay;

    @ApiModelProperty(value = "创建者")
    private String createBy;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "处理中时间")
    private LocalDateTime processingTime;

    @ApiModelProperty(value = "结算时间")
    private LocalDateTime settlementTime;

    @ApiModelProperty(value = "修改者")
    private String updateBy;

    @ApiModelProperty(value = "修改时间")
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "删除标志 1-删除 0未删除")
    @TableLogic
    private Boolean delFlag;


}
