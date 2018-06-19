package com.hyp.service.comment.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hyp.constant.ErrorConstant;
import com.hyp.dao.CommentDao;
import com.hyp.dto.cond.CommentCond;
import com.hyp.exception.BusinessException;
import com.hyp.model.CommentDomain;
import com.hyp.model.ContentDomain;
import com.hyp.service.comment.CommentService;
import com.hyp.service.content.ContentService;
import com.hyp.utils.DateKit;
import com.hyp.utils.TaleUtils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 

* @Title: CommentServiceImpl  

* @Description:   评论服务实现类

* @author HanYupeng  

* @date 2018-06-15 10:30
 */
@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentDao commentDao;

    @Autowired
    private ContentService contentService;



    private static final Map<String,String> STATUS_MAP = new ConcurrentHashMap<>();

    /**
     * 评论状态：正常
     */
    private static final String STATUS_NORMAL = "approved";
    /**
     * 评论状态：不显示
     */
    private static final String STATUS_BLANK = "not_audit";

    static {
        STATUS_MAP.put("approved",STATUS_NORMAL);
        STATUS_MAP.put("not_audit",STATUS_BLANK);
    }

    @Override
    @Transactional
    public void addComment(CommentDomain comments) {
        String msg = null;
        if (null == comments) {
            msg = "评论对象为空";
        }
        if (StringUtils.isBlank(comments.getAuthor())) {
            comments.setAuthor("热心网友");
        }
        if (StringUtils.isNotBlank(comments.getMail()) && !TaleUtils.isEmail(comments.getMail())) {
            msg =  "请输入正确的邮箱格式";
        }
        if (StringUtils.isBlank(comments.getContent())) {
            msg = "评论内容不能为空";
        }
        if (comments.getContent().length() < 5 || comments.getContent().length() > 2000) {
            msg = "评论字数在5-2000个字符";
        }
        if (null == comments.getCid()) {
            msg = "评论文章不能为空";
        }
        if (msg != null)
            throw BusinessException.withErrorCode(msg);
        ContentDomain atricle = contentService.getAtricleById(comments.getCid());
        if (null == atricle)
            throw BusinessException.withErrorCode("该文章不存在");
        comments.setOwnerId(atricle.getAuthorId());
        comments.setStatus(STATUS_MAP.get(STATUS_BLANK));
        comments.setCreated(DateKit.getCurrentUnixTime());
        commentDao.addComment(comments);

        ContentDomain temp = new ContentDomain();
        temp.setCid(atricle.getCid());
        Integer count = atricle.getCommentsNum();
        if (null == count){
            count = 0;
        }
        temp.setCommentsNum(count + 1);
        contentService.updateContentByCid(temp);

    }

    @Transactional
    @Override
    public void deleteComment(Integer coid) {
        if (null == coid)
            throw BusinessException.withErrorCode(ErrorConstant.Common.PARAM_IS_EMPTY);
        // 如果删除的评论存在子评论，一并删除
        //查找当前评论是否有子评论
        CommentCond commentCond = new CommentCond();
        commentCond.setParent(coid);
        CommentDomain comment = commentDao.getCommentById(coid);
        List<CommentDomain> childComments = commentDao.getCommentsByCond(commentCond);
        Integer count = 0;
        //删除子评论
        if (null != childComments && childComments.size() > 0){
            for (int i = 0; i < childComments.size(); i++) {
                commentDao.deleteComment(childComments.get(i).getCoid());
                count++;
            }
        }
        //删除当前评论
        commentDao.deleteComment(coid);
        count++;

        //更新当前文章的评论数
        ContentDomain contentDomain = contentService.getAtricleById(comment.getCid());
        if (null != contentDomain
                && null != contentDomain.getCommentsNum()
                && contentDomain.getCommentsNum() != 0){
            contentDomain.setCommentsNum(contentDomain.getCommentsNum() - count);
            contentService.updateContentByCid(contentDomain);
        }
    }

    @Override
    public void updateCommentStatus(Integer coid, String status) {
        if (null == coid)
            throw BusinessException.withErrorCode(ErrorConstant.Common.PARAM_IS_EMPTY);
        commentDao.updateCommentStatus(coid, status);
    }

    @Override
    public CommentDomain getCommentById(Integer coid) {
        if (null == coid)
            throw BusinessException.withErrorCode(ErrorConstant.Common.PARAM_IS_EMPTY);

        return commentDao.getCommentById(coid);
    }

    @Override
    public List<CommentDomain> getCommentsByCId(Integer cid) {
        if (null == cid)
            throw BusinessException.withErrorCode(ErrorConstant.Common.PARAM_IS_EMPTY);
        return commentDao.getCommentsByCId(cid);
    }

    @Override
    public PageInfo<CommentDomain> getCommentsByCond(CommentCond commentCond, int pageNum, int pageSize) {
        if (null == commentCond)
            throw BusinessException.withErrorCode(ErrorConstant.Common.PARAM_IS_EMPTY);
        PageHelper.startPage(pageNum, pageSize);
        List<CommentDomain> comments = commentDao.getCommentsByCond(commentCond);
        PageInfo<CommentDomain> pageInfo = new PageInfo<>(comments);
        return pageInfo;
    }
}
