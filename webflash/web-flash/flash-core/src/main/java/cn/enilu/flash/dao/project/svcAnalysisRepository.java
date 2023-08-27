package cn.enilu.flash.dao.project;


import cn.enilu.flash.bean.entity.project.svcAnalysis;
import cn.enilu.flash.bean.entity.project.testTask;
import cn.enilu.flash.dao.BaseRepository;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


// public interface svcAnalysisRepository extends JpaRepository<svcAnalysis,Long> {
//     @Override
//     <S extends svcAnalysis> List<S> findAll(Example<S> example);
// }

public interface svcAnalysisRepository extends BaseRepository<svcAnalysis,Long> {

}


