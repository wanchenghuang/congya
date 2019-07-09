package com.chauncy.message.information.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.chauncy.common.enums.common.VerifyStatusEnum;
import com.chauncy.common.enums.system.ResultCode;
import com.chauncy.common.exception.sys.ServiceException;
import com.chauncy.data.domain.po.message.information.MmInformationPo;
import com.chauncy.data.domain.po.message.information.rel.MmRelInformationGoodsPo;
import com.chauncy.data.domain.po.sys.SysUserPo;
import com.chauncy.data.domain.po.user.UmUserPo;
import com.chauncy.data.dto.app.message.information.select.SearchInfoByConditionDto;
import com.chauncy.data.dto.base.BaseUpdateStatusDto;
import com.chauncy.data.dto.manage.message.information.add.InformationDto;
import com.chauncy.data.dto.manage.message.information.select.InformationSearchDto;
import com.chauncy.data.mapper.message.information.MmInformationMapper;
import com.chauncy.data.mapper.message.information.rel.MmRelInformationGoodsMapper;
import com.chauncy.data.mapper.product.PmGoodsCategoryMapper;
import com.chauncy.data.mapper.product.PmGoodsMapper;
import com.chauncy.data.mapper.user.UmUserMapper;
import com.chauncy.data.vo.app.message.information.InformationBaseVo;
import com.chauncy.data.vo.app.message.information.InformationPagingVo;
import com.chauncy.data.vo.manage.message.information.InformationPageInfoVo;
import com.chauncy.data.vo.manage.message.information.InformationVo;
import com.chauncy.data.vo.supplier.InformationRelGoodsVo;
import com.chauncy.message.information.rel.service.IMmRelInformationGoodsService;
import com.chauncy.message.information.service.IMmInformationService;
import com.chauncy.data.core.AbstractService;
import com.chauncy.security.util.SecurityUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author huangwancheng
 * @since 2019-06-25
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class MmInformationServiceImpl extends AbstractService<MmInformationMapper, MmInformationPo> implements IMmInformationService {

    @Autowired
    private MmInformationMapper mmInformationMapper;
    @Autowired
    private MmRelInformationGoodsMapper mmRelInformationGoodsMapper;
    @Autowired
    private PmGoodsCategoryMapper pmGoodsCategoryMapper;
    @Autowired
    private PmGoodsMapper pmGoodsMapper;
    @Autowired
    private UmUserMapper umUserMapper;
    @Autowired
    private IMmRelInformationGoodsService mmRelInformationGoodsService;

    @Autowired
    private SecurityUtil securityUtil;

    /**
     * 保存资讯
     *
     * @param informationDto
     */
    @Override
    public void saveInformation(InformationDto informationDto) {

        MmInformationPo mmInformationPo = new MmInformationPo();
        BeanUtils.copyProperties(informationDto, mmInformationPo);
        //获取当前用户
        SysUserPo sysUserPo = securityUtil.getCurrUser();
        mmInformationPo.setCreateBy(sysUserPo.getUsername());
        mmInformationPo.setId(null);
        //新增资讯默认为待审核状态
        mmInformationPo.setVerifyStatus(VerifyStatusEnum.WAIT_CONFIRM.getId());
        mmInformationPo.setStoreId(sysUserPo.getStoreId());
        mmInformationPo.setUpdateTime(LocalDateTime.now());
        mmInformationMapper.insert(mmInformationPo);

        //批量插入商品资讯关联记录
        informationDto.setId(mmInformationPo.getId());
        saveBatchRelInformationGoods(informationDto, sysUserPo.getUsername());
    }

    /**
     * 批量插入店铺品牌关联记录
     * @param informationDto
     * @param userName
     */
    private void saveBatchRelInformationGoods(InformationDto informationDto, String userName) {
        List<MmRelInformationGoodsPo> mmRelInformationGoodsPoList = new ArrayList<>();
        for(Long goodsId : informationDto.getGoodsIds()) {
            MmRelInformationGoodsPo mmRelInformationGoodsPo = new MmRelInformationGoodsPo();
            mmRelInformationGoodsPo.setInformationId(informationDto.getId());
            mmRelInformationGoodsPo.setCreateBy(userName);
            mmRelInformationGoodsPo.setGoodsId(goodsId);
            mmRelInformationGoodsPoList.add(mmRelInformationGoodsPo);
        }
        mmRelInformationGoodsService.saveBatch(mmRelInformationGoodsPoList);
    }

    /**
     * 编辑资讯
     *
     * @param informationDto
     */
    @Override
    public void editInformation(InformationDto informationDto) {

        MmInformationPo mmInformationPo = mmInformationMapper.selectById(informationDto.getId());
        SysUserPo sysUserPo = securityUtil.getCurrUser();
        if(null == sysUserPo.getStoreId()) {
            //平台用户
            if (mmInformationPo.getVerifyStatus().equals(VerifyStatusEnum.WAIT_CONFIRM.getId()) ||
                    mmInformationPo.getVerifyStatus().equals(VerifyStatusEnum.CHECKED.getId())) {
                //只有资讯的状态为待审核或者审核通过  平台用户才能编辑资讯  并且不更改资讯审核状态
                BeanUtils.copyProperties(informationDto, mmInformationPo);
                mmInformationPo.setUpdateBy(sysUserPo.getUsername());
                mmInformationMapper.updateById(mmInformationPo);
            } else {
                throw new ServiceException(ResultCode.NO_AUTH,"当前不可编辑已被审核驳回的资讯");
            }
        } else {
            //商家用户
            if (mmInformationPo.getVerifyStatus().equals(VerifyStatusEnum.NOT_APPROVED.getId())) {
                //只有资讯的状态为待审核或者审核通过  平台用户才能编辑资讯
                BeanUtils.copyProperties(informationDto, mmInformationPo);
                mmInformationPo.setUpdateBy(sysUserPo.getUsername());
                //编辑之后的资讯变回待审核状态
                mmInformationPo.setVerifyStatus(VerifyStatusEnum.WAIT_CONFIRM.getId());
                mmInformationMapper.updateById(mmInformationPo);
            } else {
                throw new ServiceException(ResultCode.NO_AUTH,"当前不可编辑待审核或已审核通过的资讯");
            }
        }

        //将资讯与商品关联表的记录删除
        Map<String, Object> map = new HashMap<>();
        map.put("information_id", informationDto.getId());
        mmRelInformationGoodsMapper.deleteByMap(map);

        //批量插入商品资讯关联记录
        saveBatchRelInformationGoods(informationDto, sysUserPo.getUsername());
    }

    /**
     * 批量删除资讯
     * @param ids
     */
    @Override
    public void delInformationByIds(Long[] ids) {

        //批量删除资讯
        mmInformationMapper.deleteBatchIds(Arrays.asList(ids));

        //删除资讯商品关联表的记录
        QueryWrapper<MmRelInformationGoodsPo> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("information_id", Arrays.asList(ids));
        mmRelInformationGoodsMapper.delete(queryWrapper);
    }

    /**
     * 后台根据ID查找资讯
     *
     * @param id 资讯id
     * @return
     */
    @Override
    public InformationVo findById(Long id) {
        //资讯基本信息
        InformationVo informationVo = mmInformationMapper.findById(id);
        //跟资讯绑定关系的商品信息
        List<InformationRelGoodsVo> goodsList = pmGoodsMapper.searchRelGoodsByInfoId(id);

        for(InformationRelGoodsVo informationRelGoodsVo : goodsList) {
            List<String> categorys = pmGoodsCategoryMapper.loadParentName(informationRelGoodsVo.getGoodsCategoryId());
            StringBuilder stringBuilder = new StringBuilder();
            for(String category : categorys) {
                stringBuilder.append(category).append("/");
            }
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            informationRelGoodsVo.setCategoryName(stringBuilder.toString());
        }
        informationVo.setGoodsList(goodsList);

        return informationVo;
    }

    /**
     * app根据ID查找资讯
     *
     * @param id 资讯id
     * @return
     */
    @Override
    public InformationBaseVo findBaseById(Long id) {
        MmInformationPo mmInformationPo = mmInformationMapper.selectById(id);
        UpdateWrapper updateWrapper = new UpdateWrapper();
        updateWrapper.eq("id", id);
        updateWrapper.set("browsing_num", mmInformationPo.getBrowsingNum() + 1);
        this.update(updateWrapper);
        //资讯基本信息
        return  mmInformationMapper.findBaseById(id);
    }

    /**
     * 根据关联ID删除资讯跟店铺的绑定关系
     *
     * @param id 资讯商品关联id
     * @return
     */
    @Override
    public void delRelById(Long id) {
        MmRelInformationGoodsPo mmRelInformationGoodsPo = mmRelInformationGoodsMapper.selectById(id);
        if(null != mmRelInformationGoodsPo) {
            //直接删除
            mmRelInformationGoodsMapper.deleteById(id);
        } else {
            throw new ServiceException(ResultCode.NO_EXISTS,"删除的信息不存在");
        }
    }


    /**
     * 后台分页条件查询
     *
     * @param informationSearchDto
     * @return
     */
    @Override
    public PageInfo<InformationPageInfoVo> searchPaging(InformationSearchDto informationSearchDto) {

        Integer pageNo = informationSearchDto.getPageNo()==null ? defaultPageNo : informationSearchDto.getPageNo();
        Integer pageSize = informationSearchDto.getPageSize()==null ? defaultPageSize : informationSearchDto.getPageSize();

        PageInfo<InformationPageInfoVo> informationPageInfoVoPageInfo = PageHelper.startPage(pageNo, pageSize)
                .doSelectPageInfo(() -> mmInformationMapper.searchInfoPaging(informationSearchDto));
        return informationPageInfoVoPageInfo;
    }


    /**
     * app分页条件查询
     *
     * @param searchInfoByConditionDto
     * @return
     */
    @Override
    public PageInfo<InformationPagingVo> searchPaging(SearchInfoByConditionDto searchInfoByConditionDto) {

        Integer pageNo = searchInfoByConditionDto.getPageNo()==null ? defaultPageNo : searchInfoByConditionDto.getPageNo();
        Integer pageSize = searchInfoByConditionDto.getPageSize()==null ? defaultPageSize : searchInfoByConditionDto.getPageSize();

        PageInfo<InformationPagingVo> informationBaseVoPageInfo = PageHelper.startPage(pageNo, pageSize)
                .doSelectPageInfo(() -> mmInformationMapper.searchInfoBasePaging(searchInfoByConditionDto));
        return informationBaseVoPageInfo;
    }


    /**
     * 用户点赞资讯
     *
     * @param infoId 资讯id
     * @param userId 用户id
     * @return
     */
    @Override
    public void likeInfo(Long infoId, Long userId) {
        UmUserPo umUserPo = umUserMapper.selectById(userId);
        if(null == umUserPo) {
            throw new ServiceException(ResultCode.NO_EXISTS,"用户不存在");
        }
        MmInformationPo mmInformationPo = mmInformationMapper.selectById(infoId);
        if(null != mmInformationPo) {
            UpdateWrapper updateWrapper = new UpdateWrapper();
            updateWrapper.eq("id", infoId);
            updateWrapper.set("liked_num", mmInformationPo.getLikedNum() + 1);
            this.update(updateWrapper);
        } else {
            throw new ServiceException(ResultCode.NO_EXISTS,"资讯不存在");
        }
    }

    /**
     * 审核资讯
     *
     * @param baseUpdateStatusDto
     */
    @Override
    public void verifyInfo(BaseUpdateStatusDto baseUpdateStatusDto) {
        MmInformationPo mmInformationPo = mmInformationMapper.selectById(baseUpdateStatusDto.getId()[0]);
        if(null == mmInformationPo) {
            throw new ServiceException(ResultCode.NO_EXISTS,"资讯不存在");
        } else {
            if(!mmInformationPo.getVerifyStatus().equals(VerifyStatusEnum.WAIT_CONFIRM.getId()))  {
                throw new ServiceException(ResultCode.PARAM_ERROR,"资讯不是待审核状态");
            } else {
                UpdateWrapper updateWrapper = new UpdateWrapper();
                updateWrapper.eq("id", mmInformationPo.getId());
                updateWrapper.set("verify_status", baseUpdateStatusDto.getEnabled());
                updateWrapper.set("verify_time", LocalDateTime.now());
                updateWrapper.set("verify_by", securityUtil.getCurrUser().getUsername());
                this.update(updateWrapper);
            }
        }
    }

}