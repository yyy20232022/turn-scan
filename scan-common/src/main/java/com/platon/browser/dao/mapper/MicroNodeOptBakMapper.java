package com.platon.browser.dao.mapper;

import com.platon.browser.dao.entity.MicroNodeOptBak;
import com.platon.browser.dao.entity.MicroNodeOptBakExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface MicroNodeOptBakMapper {
    long countByExample(MicroNodeOptBakExample example);

    int deleteByExample(MicroNodeOptBakExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MicroNodeOptBak record);

    int insertSelective(MicroNodeOptBak record);

    List<MicroNodeOptBak> selectByExample(MicroNodeOptBakExample example);

    MicroNodeOptBak selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MicroNodeOptBak record, @Param("example") MicroNodeOptBakExample example);

    int updateByExample(@Param("record") MicroNodeOptBak record, @Param("example") MicroNodeOptBakExample example);

    int updateByPrimaryKeySelective(MicroNodeOptBak record);

    int updateByPrimaryKey(MicroNodeOptBak record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table micro_node_opt_bak
     *
     * @mbg.generated
     * @project https://github.com/itfsw/mybatis-generator-plugin
     */
    int batchInsert(@Param("list") List<MicroNodeOptBak> list);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table micro_node_opt_bak
     *
     * @mbg.generated
     * @project https://github.com/itfsw/mybatis-generator-plugin
     */
    int batchInsertSelective(@Param("list") List<MicroNodeOptBak> list, @Param("selective") MicroNodeOptBak.Column ... selective);
}