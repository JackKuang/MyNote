package com.learndemo.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping(value = "/return")
public class LearnReturnType {

    /**
     * Model����������ҳ����ת��url��ַ��������������ת��ַ����ô���ǿ���ͨ�������������ķ���ֵ��������תurl
     * ��ַ��������������ת��ַ
     * 
     * @param model
     *            һ���ӿڣ� ��ʵ����ΪExtendedModelMap���̳���ModelMap��
     * @return ��תurl��ַ��������������ת��ַ
     */
    @RequestMapping(value = "/index1")
    public String index1(Model model) {
        model.addAttribute("result", "��̨����index1");
        return "result";
    }

    /**
     * ModelMap������Ҫ���ڴ��ݿ��Ʒ����������ݵ����ҳ��,������request�����setAttribute���������á� �÷���ͬ��Model
     * 
     * @param model
     * @return ��תurl��ַ��������������ת��ַ
     */

    @RequestMapping(value = "/index2")
    public String index2(ModelMap model) {
        model.addAttribute("result", "��̨����index2");
        return "result";
    }

    /**
     * ModelAndView��Ҫ���������� ����ת���ַ�ʹ��ݿ��Ʒ������������ݵ����ҳ��
     * @return ����һ��ģ����ͼ����
     */
    @RequestMapping(value = "/index3")
    public ModelAndView index3() {
        ModelAndView mv = new ModelAndView("result");
        // ModelAndView mv = new ModelAndView();
        // mv.setViewName("result");
        mv.addObject("result", "��̨����index3");
        return mv;
    }

    /**
     * map�����洢�ݿ��Ʒ������������ݣ�ͨ��ModelAndView�������ء�
     * ��ȻModel,ModelMapҲ����ͨ�����ַ�������
     * @return ����һ��ģ����ͼ����
     */
    @RequestMapping(value = "/index4")
    public ModelAndView index4() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("result", "��̨����index4");
        return new ModelAndView("result", map);
    }
}