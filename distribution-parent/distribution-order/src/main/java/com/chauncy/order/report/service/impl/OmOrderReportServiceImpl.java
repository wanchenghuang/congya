package com.chauncy.order.report.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.chauncy.common.enums.order.ReportTypeEnum;
import com.chauncy.common.enums.system.ResultCode;
import com.chauncy.common.exception.sys.ServiceException;
import com.chauncy.common.util.BigDecimalUtil;
import com.chauncy.common.util.DateFormatUtil;
import com.chauncy.common.util.LoggerUtil;
import com.chauncy.data.domain.po.order.OmGoodsTempPo;
import com.chauncy.data.domain.po.order.OmOrderPo;
import com.chauncy.data.domain.po.order.report.OmOrderReportPo;
import com.chauncy.data.domain.po.order.report.OmReportRelGoodsTempPo;
import com.chauncy.data.domain.po.order.report.OmReportRelStorePo;
import com.chauncy.data.domain.po.product.stock.PmGoodsVirtualStockPo;
import com.chauncy.data.domain.po.product.stock.PmStoreRelGoodsStockPo;
import com.chauncy.data.domain.po.store.SmStorePo;
import com.chauncy.data.domain.po.sys.SysUserPo;
import com.chauncy.data.dto.base.BaseSearchPagingDto;
import com.chauncy.data.dto.manage.order.report.select.SearchReportDto;
import com.chauncy.data.mapper.order.OmGoodsTempMapper;
import com.chauncy.data.mapper.order.OmOrderMapper;
import com.chauncy.data.mapper.order.report.OmOrderReportMapper;
import com.chauncy.data.core.AbstractService;
import com.chauncy.data.mapper.order.report.OmReportRelGoodsTempMapper;
import com.chauncy.data.mapper.order.report.OmReportRelStoreMapper;
import com.chauncy.data.mapper.product.stock.PmGoodsVirtualStockMapper;
import com.chauncy.data.mapper.product.stock.PmStoreRelGoodsStockMapper;
import com.chauncy.data.mapper.store.SmStoreMapper;
import com.chauncy.data.vo.manage.order.report.ReportBaseInfoVo;
import com.chauncy.data.vo.manage.order.report.ReportRelGoodsTempVo;
import com.chauncy.order.report.service.IOmOrderReportService;
import com.chauncy.security.util.SecurityUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 商品销售报表 服务实现类
 * </p>
 *
 * @author huangwancheng
 * @since 2019-08-09
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class OmOrderReportServiceImpl extends AbstractService<OmOrderReportMapper, OmOrderReportPo> implements IOmOrderReportService {

    @Autowired
    private OmOrderReportMapper omOrderReportMapper;

    @Autowired
    private OmOrderMapper omOrderMapper;

    @Autowired
    private OmGoodsTempMapper omGoodsTempMapper;

    @Autowired
    private SmStoreMapper smStoreMapper;

    @Autowired
    private OmReportRelStoreMapper omReportRelStoreMapper;

    @Autowired
    private PmGoodsVirtualStockMapper pmGoodsVirtualStockMapper;

    @Autowired
    private PmStoreRelGoodsStockMapper pmStoreRelGoodsStockMapper;

    @Autowired
    private OmReportRelGoodsTempMapper omReportRelGoodsTempMapper;

    @Autowired
    private SecurityUtil securityUtil;


    /**
     * 查询商品销售报表
     *
     * @param searchReportDto
     * @return
     */
    @Override
    public PageInfo<ReportBaseInfoVo> searchReportPaging(SearchReportDto searchReportDto) {

        if(!ReportTypeEnum.PLATFORM_REPORT.getId().equals(searchReportDto.getReportType())) {
            //店铺用户查找对应的商品销售报表
            //获取当前店铺用户
            Long storeId = securityUtil.getCurrUser().getStoreId();
            if(null != storeId) {
                searchReportDto.setStoreId(storeId);
            } else {
                throw  new ServiceException(ResultCode.FAIL, "当前登录用户跟操作不匹配");
            }
        }
        Integer pageNo = searchReportDto.getPageNo() == null ? defaultPageNo : searchReportDto.getPageNo();
        Integer pageSize  = searchReportDto.getPageSize() == null ? defaultPageSize : searchReportDto.getPageSize();

        PageInfo<ReportBaseInfoVo> reportBaseInfoVoPageInfo = PageHelper.startPage(pageNo, pageSize)
                .doSelectPageInfo(() -> omOrderReportMapper.searchReportPaging(searchReportDto));

        return reportBaseInfoVoPageInfo;

    }

    /**
     * 根据ID查找商品销售报表信息
     *
     * @param id
     * @return
     */
    @Override
    public PageInfo<ReportRelGoodsTempVo> findReportById(BaseSearchPagingDto baseSearchPagingDto, Long id) {

        OmOrderReportPo omOrderReportPo = omOrderReportMapper.selectById(id);
        if(null == omOrderReportPo) {
            throw new ServiceException(ResultCode.NO_EXISTS, "记录不存在");
        }

        //查找关联信息
        Integer pageNo = baseSearchPagingDto.getPageNo() == null ? defaultPageNo : baseSearchPagingDto.getPageNo();
        Integer pageSize  = baseSearchPagingDto.getPageSize() == null ? defaultPageSize : baseSearchPagingDto.getPageSize();

        PageInfo<ReportRelGoodsTempVo> reportRelGoodsTempVoPageInfo = PageHelper.startPage(pageNo, pageSize)
                .doSelectPageInfo(() -> omOrderReportMapper.findReportById(id));

        return reportRelGoodsTempVoPageInfo;
    }

    /**
     * 批量创建商品销售报表
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchCreateSaleReport(LocalDate endDate) {

        //获取需要创建商品销售报表的店铺的数量
        int storeSum = omOrderReportMapper.getStoreSumNeedCreateReport(endDate, null);
        //一次性只处理1000条数据
        if(storeSum > 0) {
            for (int pageNo = 1; pageNo <= storeSum / 1000 + 1; pageNo++) {
                PageHelper.startPage(pageNo, 1000);
                List<Long> storeIdList = omOrderReportMapper.getStoreNeedCreateReport(endDate, null);
                storeIdList.forEach(storeId -> {
                    try {
                        createSaleReport(endDate, storeId);
                    } catch (Exception e) {
                        LoggerUtil.error(e);
                        log.error("店铺id为" + storeId + "的店铺生成商品销售报表报错");
                    }
                });
            }
        }
    }


    /**
     * 根据时间创建商品销售报表
     * endDate   需要创建账单的那一周   任何一天都可以
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createSaleReportByDate(LocalDate endDate) {
        //获取当前店铺用户
        SysUserPo sysUserPo = securityUtil.getCurrUser();
        if(null == sysUserPo.getStoreId()) {
            throw  new ServiceException(ResultCode.FAIL, "当前登录用户不是商家用户");
        }
        //时间所在周的结束日期
        Date date = DateFormatUtil.getLastDayOfWeek(DateFormatUtil.localDateToDate(endDate));
        endDate = DateFormatUtil.datetoLocalDate(date);

        createSaleReport(endDate, sysUserPo.getStoreId());
    }

    /**
     * 根据店铺id，日期创建商品销售报表  周期同货款账单
     * @param endDate
     * @param storeId
     */
    private void createSaleReport(LocalDate endDate, Long storeId) {
        SmStorePo smStorePo = smStoreMapper.selectById(storeId);
        if(null == smStorePo) {
            return;
        }
        //结算周期  周期同货款账单
        Integer settlementCycle = smStorePo.getPaymentBillSettlementCycle();
        //按结算周期往前推几周
        LocalDate startDate = endDate.plusDays(-7L * settlementCycle.longValue());
        //查询周期内下单扣减的店铺虚拟库存对应的记录
        List<OmOrderReportPo> omOrderReportPoList = omOrderReportMapper.searchOrderReport(startDate, endDate, storeId);
        for(OmOrderReportPo omOrderReportPo : omOrderReportPoList) {
            //新增报表记录
            omOrderReportMapper.insert(omOrderReportPo);
            //报表关联订单
            omReportRelGoodsTempMapper.updateRelReport(startDate, endDate, omOrderReportPo.getStoreId(),
                    omOrderReportPo.getBranchId(), omOrderReportPo.getIsParentStore(), omOrderReportPo.getId());
        }
    }

    /**
     * @Author yeJH
     * @Date 2019/12/8 12:38
     * @Description
     * 订单确认不能售后业务处理 扣减商品虚拟库存，插入报表订单关联
     * 1.插入关联 omReportRelGoodsTempPo 一个OmGoodsTempPo可能对应多个PmStoreRelGoodsStockPo
     *   下单的商品不同数量可能来自不同的批次
     * 2.店铺虚拟库存对应批次的商品规格剩余数量扣减
     * 3.店铺商品规格库存对应的库存减少
     * 4.订单中已售后的商品不扣减库存
     * PS:不一定有足够的虚拟库存扣减
     * 店铺A产生了商品销售报表  店铺的上级店铺们产生店铺A的分店销售报表
     *
     * @Update yeJH
     *
     * @param  orderId       订单id
     * @param  goodsTempId   订单快照id  （订单售后失败）
     * @return void
     **/
    @Override
    public void orderClosure(Long orderId, Long goodsTempId) {
        List<OmGoodsTempPo> omGoodsTempPoList;
        QueryWrapper<OmGoodsTempPo> omGoodsTempPoWrapper = new QueryWrapper<>();
        OmOrderPo omOrderPo = new OmOrderPo();
        if(null != orderId) {
            omOrderPo = omOrderMapper.selectById(orderId);
            //售后商品不需要扣减库存
            omGoodsTempPoWrapper.lambda()
                    .eq(OmGoodsTempPo::getOrderId, orderId)
                    .eq(OmGoodsTempPo::getIsAfterSale, false);
            omGoodsTempPoList = omGoodsTempMapper.selectList(omGoodsTempPoWrapper);
        } else {
            omGoodsTempPoWrapper.lambda()
                    .eq(OmGoodsTempPo::getId, goodsTempId);
            omGoodsTempPoList = omGoodsTempMapper.selectList(omGoodsTempPoWrapper);
            if(null != omGoodsTempPoList && omGoodsTempPoList.size() > 0) {
                omOrderPo = omOrderMapper.selectById(omGoodsTempPoList.get(0).getOrderId());
            }
        }
        for(OmGoodsTempPo omGoodsTempPo : omGoodsTempPoList) {
            //根据订单商品数量  判断店铺虚拟库存批次
            //获取商品所属店铺的商品规格虚拟库存 可能有多个批次，先创建的先扣减
            QueryWrapper<PmStoreRelGoodsStockPo> pmStoreRelGoodsStockPoWrapper = new QueryWrapper<>();
            pmStoreRelGoodsStockPoWrapper.lambda()
                    .eq(PmStoreRelGoodsStockPo::getStoreId, omOrderPo.getUserStoreId())
                    .eq(PmStoreRelGoodsStockPo::getGoodsSkuId, omGoodsTempPo.getSkuId())
                    .gt(PmStoreRelGoodsStockPo::getRemainingStockNum, 0)
                    .orderByAsc(PmStoreRelGoodsStockPo::getCreateTime);
            List<PmStoreRelGoodsStockPo> pmStoreRelGoodsStockPoList =
                    pmStoreRelGoodsStockMapper.selectList(pmStoreRelGoodsStockPoWrapper);
            //需要扣除的商品数量总数
            Integer needDeductionSum = omGoodsTempPo.getNumber();
            for(PmStoreRelGoodsStockPo pmStoreRelGoodsStockPo : pmStoreRelGoodsStockPoList) {
                //当前批次扣除的商品数量
                Integer currentDeductionNum ;
                //店铺商品虚拟库存关联
                OmReportRelGoodsTempPo omReportRelGoodsTempPo = new OmReportRelGoodsTempPo();
                omReportRelGoodsTempPo.setGoodsTempId(omGoodsTempPo.getId());
                omReportRelGoodsTempPo.setCreateBy(omOrderPo.getCreateBy());
                //每一条快照都单独生成  所以不需要branchId
                omReportRelGoodsTempPo.setStoreId(pmStoreRelGoodsStockPo.getStoreId());
                /*omReportRelGoodsTempPo.setStoreId(pmStoreRelGoodsStockPo.getParentStoreId());
                omReportRelGoodsTempPo.setBranchId(pmStoreRelGoodsStockPo.getStoreId());*/
                if(pmStoreRelGoodsStockPo.getRemainingStockNum() >= needDeductionSum) {
                    //该批次的虚拟库存足够扣减剩余的商品数量 只扣除剩余商品数量
                    omReportRelGoodsTempPo.setDistributePrice(pmStoreRelGoodsStockPo.getDistributePrice());
                    omReportRelGoodsTempPo.setGoodsNum(needDeductionSum);
                    //数量 * 扣减库存数量
                    currentDeductionNum = needDeductionSum;
                    BigDecimal totalAmount = BigDecimalUtil.safeMultiply(pmStoreRelGoodsStockPo.getDistributePrice(),
                            new BigDecimal(needDeductionSum.toString()));
                    omReportRelGoodsTempPo.setTotalAmount(totalAmount);
                } else {
                    //该批次的虚拟库存不足够扣减剩余的商品数量  全部扣除
                    omReportRelGoodsTempPo.setDistributePrice(pmStoreRelGoodsStockPo.getDistributePrice());
                    omReportRelGoodsTempPo.setGoodsNum(pmStoreRelGoodsStockPo.getRemainingStockNum());
                    //数量 * 扣减库存数量
                    currentDeductionNum = pmStoreRelGoodsStockPo.getRemainingStockNum();
                    BigDecimal totalAmount = BigDecimalUtil.safeMultiply(pmStoreRelGoodsStockPo.getDistributePrice(),
                            new BigDecimal(pmStoreRelGoodsStockPo.getRemainingStockNum().toString()));
                    omReportRelGoodsTempPo.setTotalAmount(totalAmount);
                }
                omReportRelGoodsTempPo.setIsParentStore(false);
                //插入关联
                omReportRelGoodsTempMapper.insert(omReportRelGoodsTempPo);

                //获取分配虚拟库存的关系链 上级店铺  产生分店销售报表
                List<Long> parentStoreIds = pmStoreRelGoodsStockMapper.getParentStoreIds(pmStoreRelGoodsStockPo.getId());
                parentStoreIds.forEach(parentStoreId ->{
                    if(!parentStoreId.equals(pmStoreRelGoodsStockPo.getStoreId())) {
                        omReportRelGoodsTempPo.setId(null);
                        omReportRelGoodsTempPo.setIsParentStore(true);
                        omReportRelGoodsTempPo.setStoreId(parentStoreId);
                        omReportRelGoodsTempMapper.insert(omReportRelGoodsTempPo);
                    }
                });

                //店铺关联商品虚拟库存对应批次的剩余库存扣减
                pmStoreRelGoodsStockPo.setRemainingStockNum(pmStoreRelGoodsStockPo.getRemainingStockNum() - currentDeductionNum);
                pmStoreRelGoodsStockMapper.updateById(pmStoreRelGoodsStockPo);

                //商品虚拟库存扣减
                pmGoodsVirtualStockMapper.deductionVirtualStock(
                        omOrderPo.getUserStoreId(), omGoodsTempPo.getSkuId(), currentDeductionNum);

                //剩余需要扣减的数量
                needDeductionSum = needDeductionSum - currentDeductionNum;
                if(needDeductionSum <= 0) {
                    break;
                }
            }
        }
    }

}
