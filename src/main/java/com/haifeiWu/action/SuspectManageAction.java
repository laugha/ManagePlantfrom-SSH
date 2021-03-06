package com.haifeiWu.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.haifeiWu.entity.PHCSMP_Staff;
import com.haifeiWu.entity.PHCSMP_Suspect;
import com.haifeiWu.service.ActivityRecordService;
import com.haifeiWu.service.BelongingInforService;
import com.haifeiWu.service.InformationCollectionService;
import com.haifeiWu.service.LeaveRecodService;
import com.haifeiWu.service.PersonalCheckService;
import com.haifeiWu.service.RoomService;
import com.haifeiWu.service.SuspectService;
import com.haifeiWu.service.TemporaryLeaveService;
import com.haifeiWu.service.UserService;
import com.haifeiWu.utils.PageBean;
import com.haifeiWu.utils.PropertiesReadUtils;
import com.haifeiWu.utils.Video;
import com.sun.org.apache.xerces.internal.impl.xpath.regex.ParseException;

/**
 * 嫌疑人信息管理action，待查嫌疑人信息，历史嫌疑人信息/demoone
 * 
 * @author wuhaifei
 * @d2016年10月17日
 */
@Controller
@RequestMapping("/suspectManage")
@Scope("prototype")
public class SuspectManageAction {
	private Logger log = Logger.getLogger(SuspectManageAction.class);
	@Autowired
	private SuspectService suspectService;// 嫌疑人信息管理
	@Autowired
	private LeaveRecodService leaveRecodService;
	@Autowired
	private RoomService roomService;
	@Autowired
	private BelongingInforService belongingInforService;
	// 嫌疑人的人身检查信息
	@Autowired
	private PersonalCheckService personalCheckService;
	// 询问讯问记录信息登记
	@Autowired
	private ActivityRecordService activityRecordService;
	// 信息采集信息登记
	@Autowired
	private InformationCollectionService informationCollectionService;
	@Autowired
	private TemporaryLeaveService temporaryLeaveService;

	@Autowired
	private UserService userService;

	/**
	 * 加载嫌疑人信息
	 * 
	 * @return
	 */

	@RequestMapping(value = "/load")
	public String SM_loadInfor(HttpServletRequest request) {
		/* log.info("历史记录，待办信息"); */
		// 获取待查嫌疑人信息
		List<PHCSMP_Suspect> suspectCheckInfor = suspectService
				.getOnPoliceSuspect();
		// 获取出区嫌疑人数据
		List<PHCSMP_Suspect> suspectCheckedInfor = suspectService
				.getLeavePoliceSuspect();
		// log.info("----------待查的" + suspectCheckInfor);
		// log.info("----------历史的----" + suspectCheckedInfor);
		List<String> roomnameList = new ArrayList<String>();
		// List<PHCSMP_Leave_Record>
		// if ((suspectCheckInfor == null) || (suspectCheckedInfor == null)) {
		// return "/jsp/error/null";
		// }
		// 将信息放入到request中
		if ((suspectCheckInfor != null))
			request.setAttribute("suspectCheckInfor", suspectCheckInfor);
		if (suspectCheckedInfor != null)
			request.setAttribute("suspectCheckedInfor", suspectCheckedInfor);

		// for (PHCSMP_Suspect phcsmp_Suspect : suspectCheckedInfor) {
		// System.err.println(phcsmp_Suspect.toString());
		// }
		for (PHCSMP_Suspect phcsmp_Suspect : suspectCheckInfor) {
			roomnameList.add(roomService.findByRoomID(
					phcsmp_Suspect.getRoom_Now()).getRoom_Name());
		}

		// 获取房间名
		request.setAttribute("roomNameList", roomnameList);
		// getDistanceTime(suspectCheckedInfor., suspectCheckedInfor.get(14));
		return "WEB-INF/jsp/suspectmanage/historyRecord";

	}

	/**
	 * 根据姓名或者档案编号查找嫌疑人信息
	 * 
	 * @return
	 * @throws IOException
	 * @throws ParseException
	 */
	@RequestMapping(value = ("/search"))
	public String searchsuspectInfor(HttpServletRequest request,
			HttpServletResponse response) throws ParseException, IOException {
		String searchInfor = request.getParameter("searchInfor");
		List<PHCSMP_Suspect> suspect = null;
		List<PHCSMP_Suspect> suspectNow = null;
		if (searchInfor == "" || searchInfor == null) {
			return SM_loadInfor(request);
		}
		/*
		 * 通过正则表达式来区分嫌疑人姓名
		 */
		Pattern p = Pattern.compile("^[\u4E00-\u9FA5]+$");
		Matcher m = p.matcher(searchInfor);
		boolean result = m.find();
		char fir = searchInfor.charAt(0);
		if (fir < 130) {
			if (fir < 60) {
				// 非数组类型的身份证号查找
				suspect = suspectService.findByCardId(searchInfor);
				suspectNow = suspectService.findByCardIdNow(searchInfor);
				log.info("身份证号：" + searchInfor);
				request.setAttribute("suspect", suspect);
				request.setAttribute("suspectNow", suspectNow);
				log.info(suspect);
			} else {
				// 根据档案编号查询嫌疑人信息
				suspect = suspectService.serachInforBySuspectId(searchInfor);
				suspectNow = suspectService
						.serachInforBySuspectIdNow(searchInfor);
				log.info("档案编号：" + searchInfor);
				request.setAttribute("suspect", suspect);
				request.setAttribute("suspectNow", suspectNow);
				log.info(suspect);
				log.info(suspectNow);
			}
		}
		if (result) {
			suspect = suspectService.findBySuspectName(searchInfor);
			suspectNow = suspectService.finBySuspectNameNow(searchInfor);
			request.setAttribute("suspect", suspect);
			request.setAttribute("suspectNow", suspectNow);
			log.info("嫌疑人姓名：" + searchInfor);
			log.info(suspect);
			log.info(suspectNow);
		}

		if (suspect.isEmpty()
				&& (!suspectNow.isEmpty() && suspectNow.size() == 1)) {
			return "redirect:/report/load?suspectID="
					+ suspectNow.get(0).getSuspect_ID();
		} else {
			return "WEB-INF/jsp/suspectmanage/suspectInforList";
		}
	}

	/***
	 * 录像下载失败的嫌疑人信息
	 * 
	 * @return
	 */
	@RequestMapping(value = "/downVideoFail")
	public String videoDownFailList(HttpServletRequest request) {

		List<String> leaveTimeList = new ArrayList<String>();
		// try {
		List<PHCSMP_Suspect> suspectList = suspectService
				.findAllVideoDownloadFailSuspectInfor();
		for (int i = 0; i < suspectList.size(); i++) {

			String suspectId = suspectList.get(i).getSuspect_ID();
			// log.info("suspectId" + suspectId);
			if (leaveRecodService.findLeaveRecordInfor(suspectId) != null) {
				String leavaTime = leaveRecodService.findLeaveRecordInfor(
						suspectId).getLeave_Time();
				// log.info("leavetime=sbsbsb+" + leavaTime);
				leaveTimeList.add(leavaTime);
			} else {
				leaveTimeList.add(null);
			}
			// log.info("leavetime=" + leaveTimeList.get(i));
		}
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

		for (int i = 0; i < suspectList.size(); i++) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("suspect_Name", suspectList.get(i).getSuspect_Name());
			map.put("suspect_ID", suspectList.get(i).getSuspect_ID());
			map.put("enter_Time", suspectList.get(i).getEnter_Time());
			map.put("identifyCard_Number", suspectList.get(i)
					.getIdentifyCard_Number());
			map.put("leave_Time", leaveTimeList.get(i));
			list.add(map);
		}
		request.setAttribute("suspect", list);
		return "WEB-INF/jsp/suspectmanage/videoDownloadFailSuspectList";
	}

	/**
	 * 再次下载录像文件
	 * 
	 * @param band_ID
	 * @param identificationCard
	 * @return
	 */
	@RequestMapping(value = ("/download"))
	public String downLoadByHands(HttpServletRequest request) throws Exception {
		String suspect_ID = request.getParameter("suspect_ID");
		log.info("=============");
		log.info("=-----==" + suspect_ID);
		PHCSMP_Suspect phcsmp_Suspect = suspectService
				.findBySuspetcId(suspect_ID);

		// log.info("查询到的嫌疑人信息---------------"
		// + phcsmp_Suspect.toString());
		try {
			Video.setRBServerCfg();
			Video.setFtpServerCfg(phcsmp_Suspect.getBand_ID(),
					phcsmp_Suspect.getIdentifyCard_Number());
			Video.uploadRecFile(phcsmp_Suspect.getBand_ID(),
					phcsmp_Suspect.getIdentifyCard_Number());
		} catch (Exception e) {
			request.setAttribute("msg", "下载失败，请重新下载");
		}
		return videoDownFailList(request);
	}

	@RequestMapping(value = "/downVideoSucc")
	public String videoDownSuccessList(HttpServletRequest request) {
		List<String> leaveTimeList = new ArrayList<String>();
		// try {
		List<PHCSMP_Suspect> suspectList = suspectService
				.findAllByIsRecordVedio();
		for (int i = 0; i < suspectList.size(); i++) {

			String suspectId = suspectList.get(i).getSuspect_ID();
			// log.info("suspectId" + suspectId);
			String leavaTime = leaveRecodService
					.findLeaveRecordInfor(suspectId).getLeave_Time();
			// log.info("leavetime=" + leavaTime);
			leaveTimeList.add(leavaTime);
			// log.info("leavetime=" + leaveTimeList.get(i));
		}
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

		for (int i = 0; i < suspectList.size(); i++) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("suspect_Name", suspectList.get(i).getSuspect_Name());
			map.put("suspect_ID", suspectList.get(i).getSuspect_ID());
			map.put("enter_Time", suspectList.get(i).getEnter_Time());
			map.put("identifyCard_Number", suspectList.get(i)
					.getIdentifyCard_Number());
			map.put("leave_Time", leaveTimeList.get(i));
			map.put("vedio_number", suspectList.get(i).getVedio_Number());
			list.add(map);

		}

		if (!list.isEmpty()) {
			request.setAttribute("suspect", list);
		}

		// 将path放到前台
		request.setAttribute("vedioPath",
				PropertiesReadUtils.getRecordConfString("uploadDir"));// vedioFile
		return "WEB-INF/jsp/suspectmanage/videoDownloadSuccessSuspectList";

	}

	@RequestMapping("/downloadVeio")
	public String download(String vedioName, HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		response.setCharacterEncoding("utf-8");// 设置编码
		response.setContentType("multipart/form-data");// 设置类型
		response.setHeader("Content-Disposition", "attachment;fileName="
				+ vedioName); // 设置响应头
		log.info("vedioName   -----------" + vedioName);
		try {
			// 获取服务器根目录
			String classPath = Thread.currentThread().getContextClassLoader()
					.getResource("").getPath();
			String rootPath = "";
			if ("\\".equals(File.separator)) {
				String path = classPath.substring(1,
						classPath.indexOf("/WEB-INF/classes"));
				rootPath = path.substring(0, path.lastIndexOf("/"));
				rootPath = rootPath.replace("/", "\\");
			}
			log.info("rootPath   -----------" + rootPath);
			String uploadPath = PropertiesReadUtils
					.getRecordConfString("uploadDir");
			log.info("===============uploadPath:" + uploadPath);
			String filePath = rootPath + "\\" + "ftpVedio" + uploadPath + "\\"
					+ vedioName;
			log.info("filePath   -----------" + filePath);
			/* String filePath=rootPath+"\\"+"ftp"+"\\"+vedioName; */

			System.out
					.println(filePath
							+ "rrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrr");
			/*
			 * String filePath =
			 * request.getSession().getServletContext().getRealPath
			 * ("/"+vedioName);;
			 */
			InputStream inputStream = new FileInputStream(new File(filePath));
			OutputStream os = response.getOutputStream();
			byte[] b = new byte[2048];
			int length;
			while ((length = inputStream.read(b)) > 0) {
				os.write(b, 0, length);
			}// 边读模板文件边写入输出流
			os.close();
			inputStream.close();// 关流
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null; // 注意此时return null
	}

	/**
	 * 历史嫌疑人分页显示
	 */
	@RequestMapping(value = ("/execute"))
	public String SM_executee(HttpServletRequest request) throws Exception {
		int page = Integer.parseInt(request.getParameter("page"));
		// 获取历史嫌疑人数据，在页面显示数量
		log.info("sb--------------------------");
		List<PHCSMP_Suspect> suspectCheckedInfor = suspectService
				.getLeavePoliceSuspect();
		request.setAttribute("suspectCheckedInfor", suspectCheckedInfor);
		// 表示每页显示5条记录，page表示当前网页
		PageBean pageBean = suspectService.getPageBean(10, page);

		// request = ServletActionContext.getRequest();

		request.setAttribute("pageBean", pageBean);

		return "WEB-INF/jsp/suspectmanage/historySuspectInforList";
	}

	/**
	 * 对应警员嫌疑人信息加载
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/suspecttosStaffload")
	public String suspecttosStaffload(HttpServletRequest request) {
		/* System.out.println("历史记录，待办信息"); */
		// 获取当时查询人员及办案人员的id;
		int staff_ID = 0;
		if (request.getSession().getAttribute("user") != null) {
			PHCSMP_Staff user = (PHCSMP_Staff) request.getSession()
					.getAttribute("user");
			staff_ID = user.getStaff_ID();
			String staffName = user.getStaff_Name();
			request.setAttribute("staffName", staffName);
		} else {
			String loginError = "登录超时，请重新登录！";
			request.setAttribute("loginError", loginError);
			return "WEB-INF/jsp/suspectmanage/relogin";
		}

		// 获取待查嫌疑人信息
		List<PHCSMP_Suspect> suspectCheckInfor = suspectService
				.getOnPoliceSuspecttoStaff(staff_ID);
		// 获取出区嫌疑人数据
		List<PHCSMP_Suspect> suspectCheckedInfor = suspectService
				.getLeavePoliceSuspecttoStaff(staff_ID);
		// System.out.println("----------待查的" + suspectCheckInfor);
		// System.out.println("----------历史的----" + suspectCheckedInfor);
		List<String> roomnameList = new ArrayList<String>();
		// List<PHCSMP_Leave_Record>
		// if ((suspectCheckInfor == null) || (suspectCheckedInfor == null)) {
		// return "/jsp/error/null";
		// }
		// 将信息放入到request中
		if ((suspectCheckInfor != null))
			request.setAttribute("suspectCheckInfor", suspectCheckInfor);
		if (suspectCheckedInfor != null)
			request.setAttribute("suspectCheckedInfor", suspectCheckedInfor);

		// for (PHCSMP_Suspect phcsmp_Suspect : suspectCheckedInfor) {
		// System.err.println(phcsmp_Suspect.toString());
		// }
		for (PHCSMP_Suspect phcsmp_Suspect : suspectCheckInfor) {
			roomnameList.add(roomService.findByRoomID(
					phcsmp_Suspect.getRoom_Now()).getRoom_Name());
		}

		// 获取房间名
		request.setAttribute("roomNameList", roomnameList);
		// getDistanceTime(suspectCheckedInfor., suspectCheckedInfor.get(14));
		return "WEB-INF/jsp/suspectmanage/suspecttoStaff";

	}

	/**
	 * 登录超时重新登录
	 * 
	 * @param request
	 * @param fileName
	 * @return
	 */
	@RequestMapping(value = "/relogin", method = RequestMethod.POST)
	public String relogin(PHCSMP_Staff staff, HttpServletRequest request,
			HttpServletResponse response) {
		try {

			PHCSMP_Staff user = null;
			if (userService.findUserByStaffNameAndPwd(staff.getStaff_Name(),
					staff.getPassWord()) != null) {
				user = userService.findUserByStaffNameAndPwd(
						staff.getStaff_Name(), staff.getPassWord());
			}

			// 向客户端输出cookie
			Cookie cookie = new Cookie("ip", request.getRemoteAddr());
			cookie.setMaxAge(24 * 60 * 60 * 7);// 七天
			response.addCookie(cookie);
			request.getSession().setAttribute("user", user);
			return "redirect:/suspectManage/suspecttosStaffload";

		} catch (Exception e) {
			request.setAttribute("loginError", "用户名或密码不正确！");
			return "WEB-INF/jsp/login";
		}
	}

	@RequestMapping(value = "/downSucc")
	public String downSucc(HttpServletRequest request,
			@RequestParam("fileName") String fileName) {
		log.info("----------fileName-----" + fileName);
		request.setAttribute("fileName", fileName);
		return "WEB-INF/jsp/suspectmanage/downSucc";
	}

	@RequestMapping(value = "/vedioPlay")
	public String vedioPlay(@RequestParam("vedioName") String vedioName,
			HttpServletRequest request) {
		String uploadDir = PropertiesReadUtils.getRecordConfString("uploadDir");
		request.setAttribute("vedioPath", "/ftpVedio" + uploadDir + "/"
				+ vedioName);
		return "WEB-INF/jsp/suspectmanage/vedio";
	}
}
