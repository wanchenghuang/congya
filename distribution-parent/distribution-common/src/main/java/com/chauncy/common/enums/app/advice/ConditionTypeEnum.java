package com.chauncy.common.enums.app.advice;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.chauncy.common.enums.BaseEnum;
import lombok.Getter;

import java.util.Objects;

/**
 * @Author cheng
 * @create 2019-09-04 21:28
 */
@Getter
public enum ConditionTypeEnum implements BaseEnum {


    TAB(1,"选项卡"),

    BRAND(2,"品牌"),

    THIRD_CATEGORY(3,"具体商品三级分类"),

    BAIHUO_ASSOCIATED(4,"百货关联的所有商品列表"),

    COUPON(5,"优惠券关联的所有商品列表"),

    HOME(6,"首页下面的商品列表"),

    SECOND_CATEGORY(7,"具体商品二级分类");

    @EnumValue  //这个注解放在数据库存储的字段上
    private Integer id;

    private String name;

    ConditionTypeEnum(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    //重写toString方法，返回值为显示的值
    @Override
    public String toString() {
        return this.name()+ "_"+this.name;
    }

    //通过ID获取结果
    public static ConditionTypeEnum fromId(Integer id) {
        for (ConditionTypeEnum type : ConditionTypeEnum.values()) {
            if (type.getId().equals(id))
                return type;
        }
        return null;
    }

    //通过名称来获取结果
    public static ConditionTypeEnum fromName(String name) {
        for (ConditionTypeEnum type : ConditionTypeEnum.values()) {
            if (type.getName().equals(name)) {
                return type;
            }
        }
        return null;
    }

    //通过enumName获取结果
    public static ConditionTypeEnum fromEnumName(String name) {
        for (ConditionTypeEnum type : ConditionTypeEnum.values()) {
            if (type.name().equals(name))
                return type;
        }
        return null;
    }

    @Override
    public boolean isExist(Object field) {
        //通过ID判断
        return Objects.nonNull(fromId(Integer.parseInt(field.toString())));

        //通过enumName判断
//        return Objects.nonNull(fromEnumName(field.toString()));
    }
}
