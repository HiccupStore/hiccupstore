package hiccup.hiccupstore.user.controller;

import hiccup.hiccupstore.user.dto.*;
import hiccup.hiccupstore.user.service.JoinService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@Slf4j
public class JoinController {

    private final JoinService joinservice;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/join")
    public String join(){
        return "register";
    }

    @GetMapping("/joinform")
    public String joinForm(@ModelAttribute JoinFormDto joinFormDto){
        return "registerinput";
    }


    /** 회원가입하는 회원들의 정보 저장하는 매서드 */
    @PostMapping("/joincomplete")
    public String joinComplete(@Validated @ModelAttribute JoinFormDto joinFormDto,BindingResult bindingResult){

        /** 글로벌 오류 비밀번호 == 비밀번호확인 */
        if(!joinFormDto.getPassword().equals(joinFormDto.getPasswordConfirm())){

            bindingResult.reject("NotMatchedPassWord",null,"비밀번호를 일치시켜주세요.");

        }

        /** 필드오류  유효성조건은 JoinFormDto에서 확인하세요. */
        if(bindingResult.hasErrors()){

            log.info("bindingResult = {} ",bindingResult);

            return "registerinput";

        }

        System.out.println(joinFormDto);
        joinFormDto.setPassword(passwordEncoder.encode(joinFormDto.getPassword()));
        Integer save = joinservice.userSave(joinFormDto);

        return "registercomplete";

    }

    /** 중복되는 아이디 검색하는 매서드 ajax로 받아온다.*/
    @PostMapping("/searchUserName")
    @ResponseBody
    public String searchUserName(@RequestBody duplicateusernamedto duplicateusernamedto){

        log.info("중복아이디검사");
        log.info("useName = {}" +duplicateusernamedto);

        /** 중복되는 아이디 Mapper로 찾아본다 */
        if(joinservice.getUser(duplicateusernamedto.getUsername()) == null){
            log.info("사용가능한아이디");
            return "true";
        }

        log.info("중복된 아이디");
        return "false";

    }

    /** 중복되는 이메일 검색하는 매서드 ajax로 받아온다.*/
    @PostMapping("/searchEmail")
    @ResponseBody
    public String searchEmail(@RequestBody duplicateusernamedto duplicateusernamedto){


        /** 중복되는 아이디 Mapper로 찾아본다 */
        if(joinservice.getEmail(duplicateusernamedto.getEmail()) == null){
            log.info("사용가능한 이메일");
            return "true";
        }

        log.info("중복된 이메일");
        return "false";

    }

    /** 중복되는 아이디 검색하는 매서드 ajax로 받아온다.*/
    @PostMapping("/searchMobile")
    @ResponseBody
    public String searchMobile(@RequestBody duplicateusernamedto duplicateusernamedto){

        log.info("중복아이디검사");
        log.info("useName = {}" +duplicateusernamedto);

        /** 중복되는 아이디 Mapper로 찾아본다 */
        if(joinservice.getMobile(duplicateusernamedto.getMobile()) == null){
            log.info("사용가능한 전화번호");
            return "true";
        }

        log.info("중복된 전화번호");
        return "false";

    }


    /** snsjoinform */
    @GetMapping("/snsjoin")
    public String snsjoin(){
        return "snsregister";
    }

    @GetMapping("/snsjoinform")
    public String snsjoinForm(@ModelAttribute SnsJoinDto snsJoinDto){
        return "snsregisterinput";
    }

    /** 회원가입하는 회원들의 정보 저장하는 매서드 */
    @PostMapping("/snsjoincomplete")
    public String snsjoinComplete(@Validated @ModelAttribute SnsJoinDto snsJoinDto,BindingResult bindingResult){


        /** 필드오류  유효성조건은 JoinFormDto에서 확인하세요. */
        if(bindingResult.hasErrors()){

            log.info("bindingResult = {} ",bindingResult);

            return "registerinput";

        }

        Integer success = joinservice.userUpdate(snsJoinDto);

        return "snsregistercomplete";

    }

}
