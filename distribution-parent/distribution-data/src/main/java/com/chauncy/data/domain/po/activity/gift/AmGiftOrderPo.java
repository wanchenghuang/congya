package com.chauncy.data.domain.po.activity.gift;

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
 * 礼包订单表
 * </p>
 *
 * @author huangwancheng
 * @since 2019-07-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("am_gift_order")
@ApiModel(value = "AmGiftOrderPo对象", description = "礼包订单表")
public class AmGiftOrderPo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id")
    @TableId(value = "id", type = IdType.ID_WORKER)
    private Long id;

    @ApiModelProperty(value = "创建者")
    private String createBy;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "修改者")
    private String updateBy;

    @ApiModelProperty(value = "修改时间")
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "删除标志 1-删除 0未删除")
    @TableLogic
    private Boolean delFlag;

    @ApiModelProperty(value = "总支付单id")
    private Long payOrderId;

    @ApiModelProperty(value = "用户ID")
    private Long userId;

    @ApiModelProperty(value = "礼包ID")
    private Long giftId;

    @ApiModelProperty(value = "礼包名称")
    private String giftName;

    @ApiModelProperty(value = "购买金额")
    private BigDecimal purchasePrice;

    @ApiModelProperty(value = "经验值")
    private BigDecimal experience;

    @ApiModelProperty(value = "购物券")
    private BigDecimal vouchers;

    @ApiModelProperty(value = "积分")
    private BigDecimal integrals;

    @ApiModelProperty("礼包图片")
    private String picture;

    @ApiModelProperty("图文详情")
    private String detailHtml;

    @ApiModelProperty(value = "微信支付内部订单号(transaction_id)，  支付宝内部交易号(trade_no)，  一般使用 payOrderNo,")
    private String payOrderNo;

    @ApiModelProperty(value = "如果创建订单失败，则保存第三方返回的失败错误码")
    private String errorCode;

    private String errorMsg;

    @ApiModelProperty(value = "支付类型，微信 ，支付宝")
    private String payTypeCode;

    @ApiModelProperty(value = "支付状态 0-未支付。1-已支付")
    private Integer payStatus;
}
