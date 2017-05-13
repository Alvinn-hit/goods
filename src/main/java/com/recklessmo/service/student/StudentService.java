package com.recklessmo.service.student;

import com.recklessmo.dao.score.ScoreDAO;
import com.recklessmo.dao.student.StudentDAO;
import com.recklessmo.model.setting.Grade;
import com.recklessmo.model.setting.Group;
import com.recklessmo.model.student.StudentInfo;
import com.recklessmo.service.setting.GradeSettingService;
import com.recklessmo.web.webmodel.page.StudentPage;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by hpf on 7/23/16.
 */
@Service
public class StudentService {

    @Resource
    private StudentDAO studentDAO;

    @Resource
    private ScoreDAO scoreDAO;

    @Resource
    private GradeSettingService gradeSettingService;

    /**
     *
     * 用于搜索学生列表
     *
     * @param page
     * @return
     */
    public List<StudentInfo> getStudentInfo(StudentPage page){
        List<StudentInfo> result = studentDAO.getStudentInfoList(page);
        compose(result, page.getOrgId());
        return result;
    }

    /**
     *
     * 用于搜索指定条件下的搜索总数,用于分页
     *
     * @param page
     * @return
     */
    public int getStudentInfoTotalCount(StudentPage page){
        return studentDAO.getStudentInfoListTotalCount(page);
    }

    /**
     *
     * 用于插入学生信息
     *
     * @param studentInfo
     */
    public void insertStudentAddInfo(StudentInfo studentInfo){
        studentDAO.insertStudentInfo(studentInfo);
    }

    /**
     *
     * 用于更新学生信息
     *
     * @param studentAllInfo
     */
    public void updateStudentInfo(StudentInfo studentInfo){
        studentDAO.updateStudentInfo(studentInfo);
    }

    /**
     *
     * 用于批量插入学生信息
     * 每次插入500个
     *
     */
    public void insertStudentList(List<StudentInfo> studentList){
        int gap = 500;
        int start = 0;
        while(start < studentList.size()) {
            int end = start + gap;
            if(end > studentList.size()){
                end = studentList.size();
            }
            List<StudentInfo> sub = studentList.subList(start, end);
            studentDAO.insertStudentList(sub);
            start = end;
        }
    }

    /**
     *
     *  根据微信id获取学生信息
     *
     * @param wechatId
     * @return
     */
    public StudentInfo getStudentInfoByWechatId(String wechatId){
        return studentDAO.getStudentInfoByWechatId(wechatId);
    }

    /**
     *
     * 根据sid获取学生信息
     *
     * @param sid
     * @return
     */
    public StudentInfo getStudentInfoBySid(long orgId, String sid){
        StudentInfo studentInfo =  studentDAO.getStudentInfoBySid(orgId, sid);
        compose(studentInfo, orgId);
        return studentInfo;
    }

    /**
     *
     * 根据学号列表获取学生信息
     *
     * @param orgId
     * @param sidList
     * @return
     */
    public List<StudentInfo> getStudentInfoBySidList(long orgId, List<String> sidList){
        return studentDAO.getStudentInfoBySidList(orgId, sidList);
    }


    /**
     *
     * 根据gradeid和classid来获取对应的学生信息
     *
     * @param gradeId
     * @param classId
     * @return
     */
    public List<StudentInfo> getStudentListByGradeIdAndClassId(long orgId, long gradeId, long classId){
        List<StudentInfo> studentInfoList = studentDAO.getStudentListByGradeIdAndClassId(orgId, gradeId, classId);
        compose(studentInfoList, orgId);
        return studentInfoList;
    }


    /**
     *
     * 根据examid查询学生信息
     *
     * 用于进行个人综合分析页面的实现
     *
     * @param page
     * @return
     */
    public List<StudentInfo> searchStudentByExam(StudentPage page){
        List<String> sidList =  scoreDAO.searchSidByExamId(page.getOrgId(), page.getExamId());
        List<StudentInfo> studentInfoList = studentDAO.getStudentInfoBySidList(page.getOrgId(), sidList);
        compose(studentInfoList, page.getOrgId());
        return studentInfoList;
    }


    private void compose(StudentInfo studentInfo, long orgId){
        List<StudentInfo> studentInfoList = new LinkedList<>();
        studentInfoList.add(studentInfo);
        compose(studentInfoList, orgId);
    }

    private void compose(List<StudentInfo> studentInfoList, long orgId){
        List<Grade> gradeList = gradeSettingService.listAllGrade(orgId);
        Map<Long, Grade> gradeMap = new HashMap<>();
        Map<Long, Group> classMap = new HashMap<>();
        gradeList.stream().forEach(grade-> {
            gradeMap.put(grade.getGradeId(), grade);
            grade.getClassList().stream().forEach(group -> {
                classMap.put(group.getClassId(), group);
            });
        });

        studentInfoList.stream().forEach(studentGradeInfo -> {
            Grade grade = gradeMap.get(studentGradeInfo.getGradeId());
            if(grade != null) {
                studentGradeInfo.setGradeName(grade.getGradeName());
                studentGradeInfo.setGradeOtherName(grade.getOtherName());
            }
            Group group = classMap.get(studentGradeInfo.getClassId());
            if(group != null) {
                studentGradeInfo.setClassName(group.getClassName());
            }
        });
    }


}
