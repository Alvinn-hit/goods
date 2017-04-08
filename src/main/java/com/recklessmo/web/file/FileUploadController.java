package com.recklessmo.web.file;

import com.recklessmo.model.exam.Exam;
import com.recklessmo.model.score.CourseScore;
import com.recklessmo.model.score.Score;
import com.recklessmo.model.security.DefaultUserDetails;
import com.recklessmo.model.setting.Grade;
import com.recklessmo.model.setting.Group;
import com.recklessmo.model.student.StudentAddInfo;
import com.recklessmo.model.student.StudentGradeInfo;
import com.recklessmo.response.JsonResponse;
import com.recklessmo.service.exam.ExamService;
import com.recklessmo.service.score.ScoreService;
import com.recklessmo.service.setting.CourseSettingService;
import com.recklessmo.service.setting.GradeSettingService;
import com.recklessmo.service.student.StudentService;
import com.recklessmo.util.ContextUtils;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 用于文件上传
 * Created by hpf on 9/9/16.
 */
@Controller
@RequestMapping("common/file")
public class FileUploadController {

    @Resource
    private ScoreService scoreService;

    @Resource
    private GradeSettingService gradeSettingService;

    @Resource
    private StudentService studentService;

    @Resource
    private CourseSettingService courseSettingService;

    @Resource
    private ExamService examService;

    /**
     * 上传成绩
     *
     * @param multipartFile
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/score/uploadExcel", method = {RequestMethod.POST})
    @ResponseBody
    public JsonResponse scoreFileUpload(@RequestParam("examId") long examId, @RequestParam("file") MultipartFile multipartFile) throws Exception {
        //处理excel文件
        InputStream inputStream = multipartFile.getInputStream();
        Workbook workbook = WorkbookFactory.create(inputStream);
        List<Score> data = new LinkedList<>();
        List<String> labelList = null;
        StringBuilder errMsg = new StringBuilder();
        int totalSheets = workbook.getNumberOfSheets();
        if (totalSheets != 0) {
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0) {
                    labelList = processHeadLabel(row);
                    //校验表头是否符合要求
                    if (checkHead(labelList, examId)) {
                        continue;
                    } else {
                        throw new Exception("表头不正确");
                    }
                }
                Score score = processRow(examId, labelList, row, errMsg);
                data.add(score);
            }
        } else {
            //抛出异常,无数据
            throw new Exception("表格无内容");
        }
        //处理score list
        if (errMsg.length() > 0) {
            //返回错误信息
            return new JsonResponse(300, "数据错误!", null);
        } else {
            preProcessData(data);
            scoreService.insertScoreList(data);
            return new JsonResponse(200, null, null);
        }
    }

    private void preProcessData(List<Score> scoreList){
        List<Grade> gradeList = gradeSettingService.listAllGrade();
        Map<Long, Grade> gradeMap = gradeList.stream().collect(Collectors.toMap(Grade::getGradeId, Function.identity()));
        List<String> sidList = scoreList.stream().map(o->o.getSid()).collect(Collectors.toList());
        List<StudentGradeInfo> studentGradeInfoList = studentService.getStudentGradeInfoBySidList(sidList);
        Map<String, StudentGradeInfo> studentGradeInfoMap = studentGradeInfoList.stream().collect(Collectors.toMap(StudentGradeInfo::getSid, Function.identity()));
        scoreList.stream().forEach(item -> {
            StudentGradeInfo studentGradeInfo = studentGradeInfoMap.get(item.getSid());
            Grade grade = gradeMap.get(studentGradeInfo.getGradeId());
            item.setGradeId(studentGradeInfo.getGradeId());
            item.setClassId(studentGradeInfo.getClassId());
            item.setGradeName(gradeMap.get(studentGradeInfo.getGradeId()).getGradeName());
            Optional<Group> groupOptional = grade.getClassList().stream().filter(o -> o.getClassId() == studentGradeInfo.getClassId()).findAny();
            if(groupOptional.isPresent()){
                item.setClassName(groupOptional.get().getClassName());
            }
        });
    }

    private List<String> processHeadLabel(Row row) throws Exception {
        List<String> labelList = new LinkedList<>();
        int colNums = row.getLastCellNum();
        //处理label
        for (int t = row.getFirstCellNum(); t < colNums; t++) {
            Cell cell = row.getCell(t, Row.RETURN_BLANK_AS_NULL);
            if (cell == null) {
                throw new Exception("表头不能为空");
            } else {
                labelList.add(cell.getStringCellValue().trim());
            }
        }
        return labelList;
    }

    private boolean checkHead(List<String> labelList, long examId) throws Exception {
        //check 是否表头有修改
        if (!labelList.contains("学号")) {
            return false;
        }
        return true;
    }

    private Score processRow(long examId, List<String> labelList, Row row, StringBuilder sb) throws Exception {
        Score score = new Score();
        score.setExamId(examId);
        DataFormatter dataFormatter = new DataFormatter();
        for (int j = row.getFirstCellNum(); j < row.getLastCellNum(); j++) {
            String label = labelList.get(j);
            Cell cell = row.getCell(j, Row.RETURN_BLANK_AS_NULL);
            if ("年级".equals(label) || "班级".equals(label) || "姓名".equals(label)) {
            } else if("年级ID".equals(label) || "班级ID".equals(label) || "学号".equals(label)){
                if (cell != null) {
                    String value = dataFormatter.formatCellValue(cell);
                    if("年级ID".equals(label)){
                        score.setGradeId(Long.parseLong(value));
                    }else if("班级ID".equals(label)){
                        score.setClassId(Long.parseLong(value));
                    }else {
                        score.setSid(value);
                    }
                } else {
                    int rowNumber = row.getRowNum();
                    sb.append("第");
                    sb.append(rowNumber);
                    sb.append("行, 第");
                    sb.append(j);
                    sb.append("列, 为空");
                }
            } else {
                CourseScore courseScore = new CourseScore();
                courseScore.setCourseName(label);
                if (cell != null) {
                    String value = dataFormatter.formatCellValue(cell);
                    //处理值;都转换成string之后进行具体解析
                    courseScore.setScore(Double.parseDouble(value));
                } else {
                    //增加缺考标记
                    courseScore.setFlag(1);
                    courseScore.setScore(0.0);
                }
                score.getCourseScoreList().add(courseScore);
            }
        }
        return score;
    }

    /**
     * 上传学生信息
     * <p>
     * 学号信息暂时做成非自动生成, 初高中学生班级变化多.
     *
     * @param multipartFile
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/student/uploadExcel", method = {RequestMethod.POST})
    @ResponseBody
    public JsonResponse studentFileUpload(@RequestParam("file") MultipartFile multipartFile) throws Exception {
        //处理excel文件
        DefaultUserDetails defaultUserDetails = ContextUtils.getLoginUserDetail();
        InputStream inputStream = multipartFile.getInputStream();
        DataFormatter dataFormatter = new DataFormatter();
        Workbook workbook = WorkbookFactory.create(inputStream);
        List<StudentAddInfo> data = new LinkedList<>();
        Map<String, Long> gradeMap = getGradeMap(0L);
        Map<String, Long> classMap = getClassMap(0L);
        int totalSheets = workbook.getNumberOfSheets();
        if (totalSheets != 0) {
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0) {
                    continue;
                }
                StudentAddInfo studentAddInfo = new StudentAddInfo();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                int colNums = row.getLastCellNum();
                for (int j = row.getFirstCellNum(); j < colNums; j++) {
                    Cell cell = row.getCell(j, Row.RETURN_BLANK_AS_NULL);
                    if (cell != null) {
                        //处理值;都转换成string之后进行具体解析
                        String value = dataFormatter.formatCellValue(cell);
                        value = value.trim();
                        switch (j) {
                            case 0:
                                studentAddInfo.setName(value);
                                break;
                            case 1:
                                studentAddInfo.setOtherName(value);
                                break;
                            case 2:
                                studentAddInfo.setPhone(value);
                                break;
                            case 3:
                                studentAddInfo.setScn(value);
                                break;
                            case 4:
                                studentAddInfo.setGender("男".equals(value) ? 0 : 1);
                                break;
                            case 5:
                                studentAddInfo.setBirth(sdf.parse(value));
                                break;
                            case 6:
                                studentAddInfo.setBirthTown(value);
                                break;
                            case 7:
                                studentAddInfo.setPeople(value);
                                break;
                            case 8:
                                studentAddInfo.setHomeTown(value);
                                break;
                            case 9:
                                studentAddInfo.setAddress(value);
                                break;
                            case 10:
                                studentAddInfo.setQq(value);
                                break;
                            case 11:
                                studentAddInfo.setWechat(value);
                                break;
                            case 12:
                                studentAddInfo.setSid("0");
                                break;
                            case 13:
                                studentAddInfo.setGradeName(value);
                                studentAddInfo.setGradeId(gradeMap.get(value));
                                break;
                            case 14:
                                studentAddInfo.setClassName(value);
                                studentAddInfo.setClassId(classMap.get(studentAddInfo.getGradeName() + "_" + value));
                                break;
                            case 15:
                                studentAddInfo.setJob(value);
                                break;
                            default:
                                break;
                        }
                    } else {
                        //抛出异常,不允许有为空的情况,缺失的都必须用0来代替
                        //全部为0代表缺考
                    }
                }
                data.add(studentAddInfo);
            }
        } else {
            //抛出异常,无数据
        }
        studentService.insertStudentList(data);
        return new JsonResponse(200, null, null);
    }

    private Map<String, Long> getGradeMap(long orgId) {
        List<Grade> gradeList = gradeSettingService.listAllGrade();
        Map<String, Long> res = new HashMap<>();
        for (Grade grade : gradeList) {
            res.put(grade.getGradeName(), grade.getGradeId());
        }
        return res;
    }

    private Map<String, Long> getClassMap(long orgId) {
        List<Grade> gradeList = gradeSettingService.listAllGrade();
        Map<String, Long> res = new HashMap<>();
        for (Grade grade : gradeList) {
            for (Group group : grade.getClassList()) {
                res.put(grade.getGradeName() + "_" + group.getClassName(), group.getClassId());
            }
        }
        return res;
    }

}
