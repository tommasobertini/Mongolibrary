package it.unipi.dii.inginf.lsmdb.mongolibrary.controller;


import it.unipi.dii.inginf.lsmdb.mongolibrary.MongolibraryApplication;
import it.unipi.dii.inginf.lsmdb.mongolibrary.exceptions.LoginException;
import it.unipi.dii.inginf.lsmdb.mongolibrary.service.LoginManager;
import it.unipi.dii.inginf.lsmdb.mongolibrary.util.Constants;
import it.unipi.dii.inginf.lsmdb.mongolibrary.util.CustomBean;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


@Controller
public class LoginController {

	private LoginManager loginManager = new LoginManager();

	static CustomBean customBean = MongolibraryApplication.getCustomBean();


	@RequestMapping(value = {"/","/login"}, method = RequestMethod.GET)
	public String login(){ return "login"; }

	@PostMapping("/login")
	public String doLogin(@RequestParam("username") String username,
						  @RequestParam("password") String password,
						  @RequestParam("user-class") String userClass,
						  Model model){

		//System.out.println(userClass);
		try{
			loginManager.doLogin(userClass, username, password);
			model.addAttribute("sessionUsername", username);

			customBean.putBean(Constants.SESSION_USERNAME, username);

			if (userClass.equals("admins")){
				customBean.putBean(Constants.SESSION_USER_CLASS, "admin");
				return "admin";
			} else {
				customBean.putBean(Constants.SESSION_USER_CLASS, "customer");
				return "userHome";
			}

		} catch (LoginException le){
			le.printStackTrace();
			model.addAttribute("info", "error");
			model.addAttribute("infoMessage", "Invalid email or password.");
			return "/login";
		}

	}

	@RequestMapping(value = {"/logout"}, method = RequestMethod.GET)
	public String logout(){

		customBean.clear();
		return "redirect:/login";
	}

}
