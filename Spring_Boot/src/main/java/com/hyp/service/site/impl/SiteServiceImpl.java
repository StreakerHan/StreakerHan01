package com.hyp.service.site.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hyp.constant.ErrorConstant;
import com.hyp.constant.Types;
import com.hyp.constant.WebConst;
import com.hyp.dao.AttAchDao;
import com.hyp.dao.CommentDao;
import com.hyp.dao.ContentDao;
import com.hyp.dao.MetaDao;
import com.hyp.dto.ArchiveDto;
import com.hyp.dto.MetaDto;
import com.hyp.dto.StatisticsDto;
import com.hyp.dto.cond.CommentCond;
import com.hyp.dto.cond.ContentCond;
import com.hyp.exception.BusinessException;
import com.hyp.model.CommentDomain;
import com.hyp.model.ContentDomain;
import com.hyp.service.site.SiteService;
import com.hyp.utils.DateKit;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 

* @Title: SiteServiceImpl  

* @Description: 站点服务实现层  

* @author HanYupeng  

* @date 2018-06-15 10:33
 */
@Service
public class SiteServiceImpl implements SiteService{

    private static final Logger LOGGER = LoggerFactory.getLogger(SiteServiceImpl.class);

    @Autowired
    private CommentDao commentDao;

    @Autowired
    private ContentDao contentDao;

    @Autowired
    private MetaDao metaDao;

    @Autowired
    private AttAchDao attAchDao;

    @Override
    public List<CommentDomain> getComments(int limit) {
        LOGGER.debug("Enter recentComments method:limit={}", limit);
        if (limit < 0 || limit > 10){
            limit = 10;
        }
        PageHelper.startPage(1, limit);
        List<CommentDomain> rs = commentDao.getCommentsByCond(new CommentCond());
        LOGGER.debug("Exit recentComments method");
        return rs;
    }

    @Override
    public List<ContentDomain> getNewArticles(int limit) {
        LOGGER.debug("Enter recentArticles method:limit={}", limit);
        if (limit < 0 || limit > 10)
            limit = 10;
        PageHelper.startPage(1, limit);
        List<ContentDomain> rs = contentDao.getArticlesByCond(new ContentCond());
        LOGGER.debug("Exit recentArticles method");
        return rs;
    }

    @Override
    public CommentDomain getComment(Integer coid) {
        LOGGER.debug("Enter recentComment method");
        if (null == coid)
            throw BusinessException.withErrorCode(ErrorConstant.Common.PARAM_IS_EMPTY);
        CommentDomain comment = commentDao.getCommentById(coid);
        LOGGER.debug("Exit recentComment method");
        return comment;
    }

    @Override
    public StatisticsDto getStatistics() {
        LOGGER.debug("Enter recentStatistics method");
        //文章总数
        Long artices = contentDao.getArticleCount();

        Long comments = commentDao.getCommentsCount();

        Long links = metaDao.getMetasCountByType(Types.LINK.getType());

        Long atts = attAchDao.getAttsCount();

        StatisticsDto rs = new StatisticsDto();
        rs.setArticles(artices);
        rs.setAttachs(atts);
        rs.setComments(comments);
        rs.setLinks(links);

        LOGGER.debug("Exit recentStatistics method");
        return rs;
    }

    @Override
    public List<ArchiveDto> getArchivesSimple(ContentCond contentCond) {
        LOGGER.debug("Enter getArchives method");
        List<ArchiveDto> archives = contentDao.getArchive(contentCond);
        LOGGER.debug("Exit getArchives method");
        return archives;
    }

    @Override
    public List<ArchiveDto> getArchives(ContentCond contentCond) {
        LOGGER.debug("Enter getArchives method");
        List<ArchiveDto> archives = contentDao.getArchive(contentCond);
        parseArchives(archives, contentCond);
        LOGGER.debug("Exit getArchives method");
        return archives;
    }



    private void parseArchives(List<ArchiveDto> archives, ContentCond contentCond) {
        if (null != archives){
            archives.forEach(archive -> {
                String date = archive.getDate();
                Date sd = DateKit.dateFormat(date, "yyyy年MM月");
                int start = DateKit.getUnixTimeByDate(sd);
                int end = DateKit.getUnixTimeByDate(DateKit.dateAdd(DateKit.INTERVAL_MONTH, sd, 1)) - 1;
                ContentCond cond = new ContentCond();
                cond.setStartTime(start);
                cond.setEndTime(end);
                cond.setType(contentCond.getType());
                List<ContentDomain> contentss = contentDao.getArticlesByCond(cond);
                archive.setArticles(contentss);
            });
        }
    }

    @Override
    public List<MetaDto> getMetas(String type, String orderBy, int limit) {
        LOGGER.debug("Enter metas method:type={},order={},limit={}", type, orderBy, limit);
        List<MetaDto> retList=null;
        if (StringUtils.isNotBlank(type)) {
            if(StringUtils.isBlank(orderBy)){
                orderBy = "count desc, a.mid desc";
            }
            if(limit < 1 || limit > WebConst.MAX_POSTS){
                limit = 10;
            }
            Map<String, Object> paraMap = new HashMap<>();
            paraMap.put("type", type);
            paraMap.put("order", orderBy);
            paraMap.put("limit", limit);
            retList= metaDao.selectFromSql(paraMap);
        }
        LOGGER.debug("Exit metas method");
        return retList;
    }
}
