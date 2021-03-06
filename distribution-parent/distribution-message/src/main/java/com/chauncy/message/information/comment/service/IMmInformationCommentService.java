package com.chauncy.message.information.comment.service;

import com.chauncy.data.domain.po.message.information.comment.MmInformationCommentPo;
import com.chauncy.data.core.Service;
import com.chauncy.data.dto.base.BaseUpdateStatusDto;
import com.chauncy.data.dto.manage.message.information.add.AddInformationCommentDto;
import com.chauncy.data.dto.manage.message.information.select.InformationCommentDto;
import com.chauncy.data.dto.manage.message.information.select.InformationViceCommentDto;
import com.chauncy.data.dto.manage.message.information.select.SearchInfoCommentDto;
import com.chauncy.data.vo.manage.message.information.comment.InformationCommentVo;
import com.chauncy.data.vo.manage.message.information.comment.InformationMainCommentVo;
import com.chauncy.data.vo.manage.message.information.comment.InformationViceCommentVo;
import com.github.pagehelper.PageInfo;

import java.util.List;

/**
 * <p>
 * 资讯评论信息表 服务类
 * </p>
 *
 * @author huangwancheng
 * @since 2019-06-30
 */
public interface IMmInformationCommentService extends Service<MmInformationCommentPo> {


    /**
     * @Author yeJH
     * @Date 2019/10/21 20:33
     * @Description 查询资讯所有评论
     *
     * @Update yeJH
     *
     * @param  searchInfoCommentDto
     * @return com.github.pagehelper.PageInfo<com.chauncy.data.vo.manage.message.information.comment.InformationCommentVo>
     **/
    PageInfo<InformationCommentVo> searchInfoComment(SearchInfoCommentDto searchInfoCommentDto);
    /**
     * 后台分页查询评论
     * @param informationCommentDto
     * @return
     */
    PageInfo<InformationViceCommentVo> searchPaging(InformationCommentDto informationCommentDto);
    /**
     * 根据主评论id查询副评论
     *
     * @param mainId
     * @return
     */
    List<InformationViceCommentVo> searchViceCommentByMainId(Long mainId, Long userId);
    /**
     * app分页查询评论
     * @param informationCommentDto
     * @return
     */
    PageInfo<InformationMainCommentVo> searchInfoCommentById(InformationCommentDto informationCommentDto);

    /**
     * 隐藏显示评论
     *
     * @param baseUpdateStatusDto
     */
    void editStatusBatch(BaseUpdateStatusDto baseUpdateStatusDto);

    /**
     * 删除评论
     * @param id
     */
    void delInfoCommentById(Long id);

    /**
     * 保存评论
     */
    InformationMainCommentVo saveInfoComment(AddInformationCommentDto addInformationCommentDto, Long userId);

    /**
     * 用户点赞评论
     * @param commentId
     * @param userId
     */
    void likeComment(Long commentId, Long userId);
}
