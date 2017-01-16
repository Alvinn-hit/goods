package com.recklessmo.web.wechat;

import com.recklessmo.model.exam.Exam;
import com.recklessmo.model.security.DefaultUserDetails;
import com.recklessmo.model.wechat.WechatMessage;
import com.recklessmo.model.wechat.WechatUser;
import com.recklessmo.response.JsonResponse;
import com.recklessmo.service.exam.ExamService;
import com.recklessmo.service.wechat.WechatBizService;
import com.recklessmo.util.ContextUtils;
import com.recklessmo.web.webmodel.page.Page;
import com.recklessmo.web.webmodel.page.WechatMsgPage;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by hpf on 8/29/16.
 *
 * 用于处理crm端请求
 *
 */
@RequestMapping("/v1/wechat")
@Controller
public class WechatController {

    @Resource
    private WechatBizService wechatBizService;

    @ResponseBody
    @RequestMapping(value = "/user/recent/list", method = RequestMethod.POST)
    public JsonResponse listRecentUser(@RequestBody Page page){
        List<WechatUser> wechatUsers = wechatBizService.getRecentUserList(page);
        int totalCount = wechatBizService.getRecentUserCount(page);
        return new JsonResponse(200, wechatUsers, totalCount);
    }


    @ResponseBody
    @RequestMapping(value = "/user/all/list", method = RequestMethod.POST)
    public JsonResponse listAllUser(@RequestBody Page page){
        List<WechatUser> wechatUsers = wechatBizService.getRecentUserList(page);
        int totalCount = wechatBizService.getRecentUserCount(page);
        return new JsonResponse(200, wechatUsers, totalCount);
    }

    @ResponseBody
    @RequestMapping(value = "/message/list", method = RequestMethod.POST)
    public JsonResponse listMessage(@RequestBody WechatMsgPage page){
        List<WechatMessage> wechatMessages = wechatBizService.getMessageList(page);
        int totalCount = wechatBizService.getMessageListCount(page);
        return new JsonResponse(200, wechatMessages, totalCount);
    }

    @ResponseBody
    @RequestMapping(value = "/message/add", method = RequestMethod.POST)
    public JsonResponse addMessage(@RequestBody WechatMessage wechatMessage){
        DefaultUserDetails defaultUserDetails = ContextUtils.getLoginUserDetail();
        wechatMessage.setOrgId(0);
        wechatMessage.setCreated(new Date());
        wechatMessage.setMessageType(1);
        wechatMessage.setUserId(defaultUserDetails.getId());
        wechatMessage.setUserName("hpf");
        wechatMessage.setType(1);
        boolean result = wechatBizService.sendMessage(wechatMessage);
        return new JsonResponse(200, result, null);
    }



}