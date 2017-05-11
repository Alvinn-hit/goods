package com.recklessmo.dao.student;

import com.recklessmo.model.student.StudentInfo;
import com.recklessmo.web.webmodel.page.StudentPage;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * Created by hpf on 7/23/16.
 *
 */
public interface StudentDAO {

    void insertStudentInfo(StudentInfo studentInfo);
    void insertStudentList(@Param("list") List<StudentInfo> studentInfoList);
    void updateStudentInfo(StudentInfo studentInfo);

    List<StudentInfo> getStudentInfoList(StudentPage page);
    int getStudentInfoListTotalCount(StudentPage page);

    StudentInfo getStudentInfoByWechatId(@Param("wechatId")String wechatId);
    StudentInfo getStudentInfoBySid(@Param("orgId")long orgId, @Param("sid")String sid);
    List<StudentInfo> getStudentInfoBySidList(@Param("orgId")long orgId, @Param("sidList")List<String> sidList);
    List<StudentInfo> getStudentListByGradeIdAndClassId(@Param("orgId")long orgId, @Param("gradeId")long gradeId, @Param("classId")long classId);

}

