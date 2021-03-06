package com.chauncy.web.api.app.component;

import com.chauncy.activity.gift.IAmGiftService;
import com.chauncy.common.enums.message.KeyWordTypeEnum;
import com.chauncy.common.enums.system.ResultCode;
import com.chauncy.common.exception.sys.ServiceException;
import com.chauncy.data.domain.po.user.UmUserPo;
import com.chauncy.data.dto.app.advice.category.select.GoodsCategoryVo;
import com.chauncy.data.dto.app.component.ScreenParamDto;
import com.chauncy.data.dto.app.component.ShareDto;
import com.chauncy.data.dto.app.product.FindGoodsCategoryDto;
import com.chauncy.data.vo.BaseVo;
import com.chauncy.data.vo.JsonViewData;
import com.chauncy.data.vo.app.component.ScreenParamVo;
import com.chauncy.data.vo.app.goods.ShareDetailVo;
import com.chauncy.data.vo.app.user.GetMembersCenterVo;
import com.chauncy.data.vo.app.version.FindVersionVo;
import com.chauncy.data.vo.manage.message.content.app.FindArticleContentVo;
import com.chauncy.message.advice.IMmAdviceService;
import com.chauncy.message.content.service.IMmArticleService;
import com.chauncy.message.content.service.IMmBootPageService;
import com.chauncy.message.content.service.IMmKeywordsSearchService;
import com.chauncy.message.information.service.IMmInformationService;
import com.chauncy.product.service.IPmGoodsCategoryService;
import com.chauncy.product.service.IPmGoodsService;
import com.chauncy.security.util.SecurityUtil;
import com.chauncy.store.service.ISmStoreService;
import com.chauncy.system.service.ISysVersionService;
import com.chauncy.user.service.IUmUserService;
import com.chauncy.web.base.BaseApi;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * @Author cheng
 * @create 2019-09-05 10:03
 *
 * app端处理单一业务的接口
 */
@RestController
@RequestMapping("/app/component")
@Api(tags = "app_组件")
public class ComponentApi extends BaseApi {

    @Autowired
    private IMmBootPageService bootPageService;

    @Autowired
    private IAmGiftService giftService;

    @Autowired
    private IMmKeywordsSearchService mmKeywordsSearchService;

    @Autowired
    private IPmGoodsService goodsService;

    @Autowired
    private ISmStoreService smStoreService;

    @Autowired
    private IMmInformationService mmInformationService;

    @Autowired
    private IUmUserService userService;

    @Autowired
    private IMmArticleService articleService;

    @Autowired
    private IPmGoodsCategoryService goodsCategoryService;

    @Autowired
    private IMmAdviceService adviceService;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private ISysVersionService versionService;

    /**
     * @Author yeJH
     * @Date 2019/10/17 10:29
     * @Description 联动查询所有分类
     *
     * @Update yeJH
     *
     * @param
     * @return com.chauncy.data.vo.JsonViewData
     **/
   /* @PostMapping("/findAllCategory")
    @ApiOperation(value = "联动查询所有分类")
    public JsonViewData<GoodsCategoryTreeVo> findGoodsCategoryTreeVo(){
        List<GoodsCategoryTreeVo> goodsCategoryTreeVo = goodsCategoryService.findGoodsCategoryTreeVo();
        return setJsonViewData(MyBaseTree.build(goodsCategoryTreeVo));
    }*/
  /**
     * @Author yeJH
     * @Date 2019/10/17 10:29
     * @Description 联动查询所有分类
     *
     * @Update yeJH
     *
     * @param
     * @return com.chauncy.data.vo.JsonViewData
     **/
    @PostMapping("/findAllCategory")
    @ApiOperation(value = "联动查询所有分类（修改）")
    public JsonViewData<GoodsCategoryVo> findAllCategory(
            @RequestBody @ApiParam(required = true,name = "findGoodsCategoryDto",value = "一级分类id")
            @Validated FindGoodsCategoryDto findGoodsCategoryDto){
        List<GoodsCategoryVo> goodsCategoryVoList = goodsCategoryService.findAllCategory(findGoodsCategoryDto);
        return setJsonViewData(goodsCategoryVoList);
    }

    /**
     * 判断用户是否领取过新人礼包
     *
     * @return
     */
    @ApiOperation("判断用户是否领取过新人礼包")
    @GetMapping("/isReceive")
    public JsonViewData isReceive(){
        return setJsonViewData(giftService.isReceive());
    }

    /**
     * 获取搜索界面-热门搜索词语
     *
     * @return
     */
    @ApiOperation("获取搜索界面-热门搜索词语")
    @GetMapping("/getKeyWordByType/{type}")
    public JsonViewData<List<String>> isReceive(@ApiParam(required = true, value = "1：商品；2：店铺；3：资讯") @PathVariable Integer type){
        return setJsonViewData(mmKeywordsSearchService.getKeyWordByType(type));
    }

    /**
     * @Author yeJH
     * @Date 2019/9/19 18:43
     * @Description 获取筛选店铺/资讯/商品的参数
     *
     * @Update yeJH
     *
     * @Param [screenParamDto]
     * @return com.chauncy.data.vo.JsonViewData<com.chauncy.data.vo.app.component.ScreenParamVo>
     **/
    @ApiOperation("获取筛选店铺/资讯/商品的参数")
    @PostMapping("/findScreenParam")
    public JsonViewData<ScreenParamVo> findScreenParam(
            @RequestBody @ApiParam(required = true,name = "screenParamDto",value = "获取筛选店铺/资讯/商品的参数")
            @Valid ScreenParamDto screenParamDto){
        ScreenParamVo screenParamVo = new ScreenParamVo();
        if(screenParamDto.getKeyWordType().equals(KeyWordTypeEnum.GOODS.getId())) {
            if (null == screenParamDto.getFindStoreGoodsParamDto()) {
                throw new ServiceException(ResultCode.PARAM_ERROR, "筛选店铺商品的参数不能为空");
            }
            //商品
            screenParamVo = goodsService.findScreenGoodsParam(screenParamDto.getFindStoreGoodsParamDto());
        }
        if (screenParamDto.getKeyWordType().equals(KeyWordTypeEnum.MERCHANT.getId())) {
            if (null == screenParamDto.getFindStoreParamDto()) {
                throw new ServiceException(ResultCode.PARAM_ERROR, "筛选店铺的参数不能为空");
            }
            //店铺
            screenParamVo = smStoreService.findScreenStoreParam(screenParamDto.getFindStoreParamDto());
        }
        if (screenParamDto.getKeyWordType().equals(KeyWordTypeEnum.INFORMATION.getId()) ) {
            if (null == screenParamDto.getFindInfoParamDto()){
                throw new ServiceException(ResultCode.PARAM_ERROR, "筛选资讯的参数不能为空");
            }
            //资讯
            screenParamVo = mmInformationService.findScreenInfoParam(screenParamDto.getFindInfoParamDto());
        }
        if (screenParamDto.getKeyWordType().equals(KeyWordTypeEnum.BRAND.getId()) ) {
            if (null == screenParamDto.getFindRelGoodsParamDto()){
                throw new ServiceException(ResultCode.PARAM_ERROR, "筛选关联商品的参数不能为空");
            }
            //各种关联下的商品
            screenParamVo = adviceService.findScreenRelGoodsParam(screenParamDto.getFindRelGoodsParamDto());
        }
        return setJsonViewData(screenParamVo);
    }

    /**
     * 获取提供给用户领取的新人礼包
     *
     * @return
     */
    @ApiOperation("获取提供给用户领取的新人礼包")
    @GetMapping("/getGift")
    public JsonViewData<BaseVo> getGift(){

        return setJsonViewData(giftService.getGift());
    }

    /**
     * 新人领取礼包
     * @param giftId
     * @return
     */
    @ApiOperation("新人领取礼包")
    @GetMapping("/receiveGift/{giftId}")
    public JsonViewData receiveGift(@ApiParam(required = true,name = "giftId",value = "礼包ID")
                                    @PathVariable Long giftId){
        giftService.receiveGift(giftId);
        return setJsonViewData(ResultCode.SUCCESS,"恭喜你,领取成功!");
    }

    /**
     * 获取引导页图片
     *
     * @return
     */
    @GetMapping("/findBootPage")
    @ApiOperation("获取引导页图片")
    public JsonViewData<List<String>> findBootPage(){

        return setJsonViewData(bootPageService.findBootPage());
    }

    /**
     * 分享商品
     *
     * @param
     * @return
     */
    @PostMapping("/share")
    @ApiOperation("分享商品/资讯成功访问该接口")
    public JsonViewData shareGoods(@RequestBody @ApiParam(required = true,name = "shareDto",value = "分享商品/资讯Dto")
                                   @Validated ShareDto shareDto){

        goodsService.share(shareDto);

        return setJsonViewData(ResultCode.SUCCESS,"转发成功");
    }

    /**
     * @Author yeJH
     * @Date 2019/11/17 21:29
     * @Description 获取分享商品/资讯/注册页面信息
     *
     * @Update yeJH
     *
     * @param  shareDto
     * @return com.chauncy.data.vo.JsonViewData<ShareDetailVo>
     **/
    @PostMapping("/share/getShareDetail")
    @ApiOperation("分享商品/资讯/注册页面信息")
    public JsonViewData<ShareDetailVo> getShareDetail(@RequestBody @ApiParam(required = true,name = "shareDto",value = "分享商品/资讯/注册页面Dto")
                                   @Validated ShareDto shareDto, HttpServletRequest request){

        UmUserPo umUserPo = securityUtil.getAppCurrUser();
        return setJsonViewData(goodsService.getShareDetail(shareDto, umUserPo));
    }

    /**
     * 会员中心
     *
     * @return
     */
    @PostMapping("/getMembersCenter")
    @ApiOperation("会员中心")
    public JsonViewData<GetMembersCenterVo> getMembersCenter(){

        UmUserPo userPo = securityUtil.getAppCurrUser();

        return setJsonViewData(userService.getMembersCenter(userPo));
    }

    /**
     * @Author chauncy
     * @Date 2019-09-18 16:11
     * @Description //查找所有的文章位置
     *
     * @Update chauncy
     *
     * @Param []
     * @return com.chauncy.data.vo.JsonViewData<java.util.List<com.chauncy.data.vo.BaseVo>>
     **/
    @GetMapping("/selectLocations")
    @ApiOperation("查找所有的文章位置")
    public JsonViewData<Map<Integer,String>> selectLocations(){

//        System.out.println(articleService.findArticleLocations());

        return setJsonViewData(articleService.findArticleLocations());
    }

    /**
     * @Author chauncy
     * @Date 2019-09-18 21:14
     * @Description //根据文章位置类型获取文章信息
     *
     * @Update chauncy
     *
     * @Param [type]
     * @return com.chauncy.data.vo.JsonViewData<java.util.List<com.chauncy.data.vo.manage.message.content.app.FindArticleContentVo>>
     **/
    @GetMapping("/findArticleContent/{type}")
    @ApiOperation("根据文章位置类型获取文章信息")
    public JsonViewData<List<FindArticleContentVo>> findArticleContent(@PathVariable Integer type){

        return setJsonViewData(articleService.findArticleContent(type));
    }

    @GetMapping("/find-version/{type}")
    @ApiOperation("查找版本信息")
    public JsonViewData<FindVersionVo> findVersion(@ApiParam(required = true,name = "type",value = "app类型 1-android 2-ios") @PathVariable Integer type){

        return setJsonViewData(versionService.findVersion(type));
    }

}
