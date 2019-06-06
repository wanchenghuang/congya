package com.chauncy.data.mapper.common;

import com.chauncy.common.base.BaseTest;
import com.chauncy.common.util.LoggerUtil;
import com.chauncy.data.mapper.Mapper;
import com.chauncy.data.mapper.sys.SysUserMapper;
import com.chauncy.web.StartApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

/**
 * @Author zhangrt
 * @Date 2019-06-05 18:13
 **/
@SpringBootTest(classes = StartApplication.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class CommonMapperTest extends BaseTest{

    @Autowired
    private SysUserMapper mapper;



    @Test
    public void getChildrenIds(){
        List<Long> sys_permission = mapper.getChildIds(null, "sys_permission");
        sys_permission.forEach(x->LoggerUtil.error(x.toString()));
    }

}