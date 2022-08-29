package hiccup.hiccupstore.user.controller.mypage;


import hiccup.hiccupstore.user.dao.UserMapper;
import hiccup.hiccupstore.user.dto.join.JoinFormDto;
import hiccup.hiccupstore.user.dto.UserDto;
import hiccup.hiccupstore.commonutil.security.service.Oauth2UserContext;
import hiccup.hiccupstore.user.service.mypage.MyPageUserInformationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@Controller
@Slf4j
@RequiredArgsConstructor
public class MyPageUserInformationUpdateController {

    private final PasswordEncoder passwordEncoder;
    private final MyPageUserInformationService myPageUserInformationService;
    private final UserMapper userMapper;

    @GetMapping("/mypage/userinformationupadte")
    public String MyPageUserInformationUpdate(){

        return "mypage/userinformationupadte";

    }

    @PostMapping("/mypage/userinformationupadte")
    public String MyPageUserInformationUpdatePost(String password, Model model){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDto user;
        try {
            user = (UserDto) authentication.getPrincipal();
        } catch (Exception exce){
            user = ((Oauth2UserContext) authentication.getPrincipal()).getAccount();
        }

        if(passwordEncoder.matches(password,user.getPassword())){

            UserDto userInfo = userMapper.getUser(user.getUserName());
            model.addAttribute("userdto",userInfo);
            String[] addresssplit = userInfo.getAddress().split("/");
            model.addAttribute("addresssplit",addresssplit);

            return "mypage/userinformationupadteform";

        }

        return "redirect:/userinformationupadte";

    }

    @PostMapping("/mypage/updateinformation")
    public String updateinformation(JoinFormDto joinFormDto,RedirectAttributes redirectAttributes){

        myPageUserInformationService.MyPageUserInformationUpdate(joinFormDto);
        redirectAttributes.addFlashAttribute("msg","update_ok");

        return "redirect:/mypage";
    }

    @GetMapping("/mypage/userwithdrawal")
    public String userWithdrawal(){

        return "mypage/userwithdrawal";
    }

    @PostMapping("/mypage/userwithdrawal")
    public String userWithdrawalPost(HttpServletRequest request,
                                     HttpServletResponse response,
                                     RedirectAttributes redirectAttributes,
                                     String password){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDto user;
        try {
            user = (UserDto) authentication.getPrincipal();
        } catch (Exception exce){
            user = ((Oauth2UserContext) authentication.getPrincipal()).getAccount();
            System.out.println("classcastexception도 잡앗지롱");
        }

        if(passwordEncoder.matches(password,user.getPassword())){

            myPageUserInformationService.MyPageUserDeleted(user.getUserName());

            if(authentication != null){
                new SecurityContextLogoutHandler().logout(request,response,authentication);
            }
            redirectAttributes.addFlashAttribute("msg","DEL_OK");
            return "redirect:/";
        }

        return "mypage/userwithdrawal";

    }

}
