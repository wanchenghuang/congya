package com.chauncy.web.api.manage.user.member;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.chauncy.common.enums.system.ResultCode;
import com.chauncy.common.util.StringUtils;
import com.chauncy.data.domain.po.user.PmMemberLevelPo;
import com.chauncy.data.dto.base.BaseSearchDto;
import com.chauncy.data.dto.manage.good.add.AddMemberLevelDto;
import com.chauncy.data.dto.manage.user.select.SearchLevelDto;
import com.chauncy.data.valid.group.IUpdateGroup;
import com.chauncy.data.vo.JsonViewData;
import com.chauncy.user.service.IPmMemberLevelService;
import com.chauncy.web.base.BaseApi;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @Author zhangrt
 * @Date 2019/6/25 15:21
 **/
@Api(tags = "会员等级")
@RestController
@RequestMapping("/manage/user/member")
public class memberApi extends BaseApi {

    @Autowired
    private IPmMemberLevelService memberLevelService;

    @PostMapping("/add")
    @ApiOperation("添加会员等级")
    public JsonViewData add(@Validated @RequestBody AddMemberLevelDto memberLevelDto){
        PmMemberLevelPo maxLevel = memberLevelService.findByMaxLevel();
        if (memberLevelDto.getLevelExperience().compareTo(maxLevel.getLevelExperience())<0){
            return setJsonViewData(ResultCode.PARAM_ERROR,"新增会员等级经验值不得少于上一等级经验值！");
        }
        PmMemberLevelPo saveMemberLevel=new PmMemberLevelPo();
        BeanUtils.copyProperties(memberLevelDto,saveMemberLevel);
        saveMemberLevel.setId(null).setLevel(maxLevel.getLevel()+1).
        setCreateBy(getUser().getUsername());
        memberLevelService.save(saveMemberLevel);
        return setJsonViewData(saveMemberLevel.getId());

    }


    @PostMapping("/update")
    @ApiOperation("修改会员等级")
    public JsonViewData<PmMemberLevelPo> update(@Validated(IUpdateGroup.class) @RequestBody AddMemberLevelDto memberLevelDto){

       PmMemberLevelPo queryMemberLevel=memberLevelService.getById(memberLevelDto.getId());
       PmMemberLevelPo condition=new PmMemberLevelPo();
       //获取上一等级的
        condition.setLevel(queryMemberLevel.getLevel()-1);
        PmMemberLevelPo lastLevel = memberLevelService.getOne(new QueryWrapper<>(condition));
        if (lastLevel!=null && memberLevelDto.getLevelExperience().compareTo(lastLevel.getLevelExperience())<0){
            return setJsonViewData(ResultCode.PARAM_ERROR,"修改后的经验值不得小于或等于上一等级的经验值！");
        }
        //获取下一等级的
        condition.setLevel(queryMemberLevel.getLevel()+1);
        PmMemberLevelPo nextLevel = memberLevelService.getOne(new QueryWrapper<>(condition));
        if (nextLevel!=null && memberLevelDto.getLevelExperience().compareTo(nextLevel.getLevelExperience())>0){
            return setJsonViewData(ResultCode.PARAM_ERROR,"修改后的经验值不得大于或等于下一等级的经验值！");
        }
        PmMemberLevelPo updateMemberLevel=new PmMemberLevelPo();
        BeanUtils.copyProperties(memberLevelDto,updateMemberLevel);
        updateMemberLevel.setUpdateBy(getUser().getUsername());
        memberLevelService.updateById(updateMemberLevel);
        return setJsonViewData(updateMemberLevel);

    }


    @PostMapping("/search")
    @ApiOperation("查找会员等级分页")
    public JsonViewData<PageInfo<PmMemberLevelPo>> search( @Validated @RequestBody SearchLevelDto searchLevelDto){
        Integer pageNo=searchLevelDto.getPageNo()==null?defaultPageNo:searchLevelDto.getPageNo();
        Integer pageSize=searchLevelDto.getPageSize()==null?defaultPageSize:searchLevelDto.getPageSize();
        QueryWrapper queryWrapper=new QueryWrapper();
        if (StringUtils.isNotBlank(searchLevelDto.getActor())){
            queryWrapper.like("actor",searchLevelDto.getActor());
        }
        if (StringUtils.isNotBlank(searchLevelDto.getLevelName())){
            queryWrapper.like("level_name",searchLevelDto.getLevelName());
        }
        PageInfo<PmMemberLevelPo> memberLevelPos = PageHelper.startPage(pageNo, pageSize).doSelectPageInfo(() -> memberLevelService.list(queryWrapper));
        return setJsonViewData(memberLevelPos);
    }

    @PostMapping("/delete/{id}")
    @ApiOperation("删除会员等级")
    public JsonViewData search( @PathVariable Long id){
        PmMemberLevelPo memberLevelPo = memberLevelService.getById(id);
        if (memberLevelPo.getLevel()==1){
            return setJsonViewData(ResultCode.FAIL,"一级等级为默认等级，不能删除！");
        }
        boolean b = memberLevelService.removeById(id);
        return b?setJsonViewData(ResultCode.SUCCESS):setJsonViewData(ResultCode.FAIL);
    }

}
