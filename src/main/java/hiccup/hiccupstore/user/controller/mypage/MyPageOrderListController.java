package hiccup.hiccupstore.user.controller.mypage;

import hiccup.hiccupstore.user.dto.OrderDto;
import hiccup.hiccupstore.user.dto.OrderFormDto;
import hiccup.hiccupstore.user.dto.OrderLatelyProductDto;
import hiccup.hiccupstore.user.dto.UserDto;
import hiccup.hiccupstore.user.service.mypage.MyPageOrderListService;
import hiccup.hiccupstore.user.service.mypage.MyPageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Controller
@Slf4j
@RequiredArgsConstructor
public class MyPageOrderListController {

    private final MyPageOrderListService myPageOrderListService;

    @GetMapping("/mypageorderlist")
    public String mypageOrderList(){

        return "mypageorderlist";
    }

    @GetMapping("/mypageorderlistsearch")
    public String mypageOrderListPost(HttpSession session,String startdate, String lastdate, Model model,Integer page){

        //UserDto user = (UserDto) session.getAttribute(SessionConst.LOGIN_MEMBER);

        Integer pagesize = 5;
        if(page == null)
            page=1;

        log.info("startdate = {} , lastdate ={}",startdate,lastdate);

        UserDto user = UserDto.builder().userId(2).userName("ssoboro").nickName("오인석").address("햄스터동네").birth("93/06/18").
                email("ssoboro1@gmail.com").phone("01085264714").password("4863527wyc").build();

        List<OrderLatelyProductDto> orderLatelyProductList = myPageOrderListService.MyPage(startdate,lastdate,user,page,pagesize);

        ArrayList<OrderFormDto> orderFormList = null;
        log.info("size는 얼마인가요? = {} ",orderLatelyProductList.size());

        if (orderLatelyProductList.size() != 0) {
            log.info("여기를 지나가는 orderFormList = {} ",orderLatelyProductList);
            List<OrderDto> orderDtos = myPageOrderListService.MyPage2(user,page,pagesize,model);

            orderFormList = makeOrderList(orderLatelyProductList,orderDtos);
            model.addAttribute("orderFormList",orderFormList);

        }

        log.info("여기서 잘확인해보자 = {} ",orderFormList);

        model.addAttribute("startdate",startdate);
        model.addAttribute("lastdate",lastdate);

        return "mypageorderlist";
    }

    /** productLatelyDto랑 orderDto를 합쳐서 ProductDtoList를 만든다. */
    ArrayList<OrderFormDto> makeOrderList(List<OrderLatelyProductDto> orderLatelyProductList,List<OrderDto> orderList){

        ArrayList<OrderFormDto> OrderFormDtoList = new ArrayList<>();
        HashMap<Integer, OrderFormDto> objectObjectHashMap = new HashMap<>();
        HashMap<Integer, List<OrderLatelyProductDto>> objectObjectHashMap1 = new HashMap<>();

        for(int i = 0; i < orderList.size();i++){
            OrderFormDto orderFormDto = OrderFormDto.builder().orderId(orderList.get(i).getOrderid()).
                    orderdate(orderList.get(i).getOrderdate()).
                    status(orderList.get(i).getStatus()).
                    build();

            objectObjectHashMap.put(orderList.get(i).getOrderid(),orderFormDto);
            List<OrderLatelyProductDto> OrderLatelyProductDto = new ArrayList<>();
            objectObjectHashMap1.put(orderList.get(i).getOrderid(),OrderLatelyProductDto);
            OrderFormDtoList.add(orderFormDto);

        }

        for (OrderLatelyProductDto orderLatelyProductDto : orderLatelyProductList) {

            OrderFormDto orderFormDto = objectObjectHashMap.get(orderLatelyProductDto.getOrderid());
            List<OrderLatelyProductDto> orderLatelyProductDtoList =
                    objectObjectHashMap1.get(orderLatelyProductDto.getOrderid());
            orderLatelyProductDtoList.add(orderLatelyProductDto);

            if(orderFormDto.getOrderLatelyProductDto() == null){
                orderFormDto.setOrderLatelyProductDto(orderLatelyProductDtoList);
            }
        }

        return OrderFormDtoList;
    }

}
