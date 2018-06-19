package com.hyp.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.hyp.dto.ArchiveDto;
import com.hyp.dto.cond.ContentCond;
import com.hyp.model.ContentDomain;

import java.util.List;

/**
 * 

* @Title: ContentDao  

* @Description: 文章持久层  

* @author HanYupeng  

* @date 2018-06-15 09:51
 */
@Mapper
public interface ContentDao {

    /**
     * 添加文章
     * @param contentDomain
     * @return
     */
    int addArticle(ContentDomain contentDomain);

    /**
     * 根据编号删除文章
     * @param cid
     * @return
     */
    int deleteArticleById(@Param("cid") Integer cid);

    /**
     * 更新文章
     * @param contentDomain
     * @return
     */
    int updateArticleById(ContentDomain contentDomain);

    /**
     * 更新文章的评论数
     * @param cid
     * @param commentsNum
     * @return
     */
    int updateArticleCommentCountById(@Param("cid") Integer cid, @Param("commentsNum") Integer commentsNum);



    /**
     * 根据编号获取文章
     * @param cid
     * @return
     */
    ContentDomain getArticleById(@Param("cid") Integer cid);

    /**
     * 根据条件获取文章列表
     * @param contentCond
     * @return
     */
    List<ContentDomain> getArticlesByCond(ContentCond contentCond);

    /**
     * 获取文章总数量
     * @return
     */
    Long getArticleCount();

    /**
     * 获取归档数据
     * @param contentCond 查询条件（只包含开始时间和结束时间）
     * @return
     */
    List<ArchiveDto> getArchive(ContentCond contentCond);

    /**
     * 获取最近的文章（只包含id和title）
     * @return
     */
    List<ContentDomain> getRecentlyArticle();

    /**
     * 搜索文章-根据标题 或 内容匹配
     * @param param
     * @return
     */
    List<ContentDomain> searchArticle(@Param("param") String param);

}
