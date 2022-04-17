package com.jpastart.store.controller;

import com.jpastart.store.domain.item.Item;
import com.jpastart.store.domain.member.Member;
import com.jpastart.store.domain.order.Order;
import com.jpastart.store.repository.OrderSearch;
import com.jpastart.store.service.ItemService;
import com.jpastart.store.service.MemberService;
import com.jpastart.store.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final MemberService memberService;
    private final ItemService itemService;

    @GetMapping("/order")
    public String createForm(Model model){

        List<Member> members = memberService.findMembers();
        List<Item> items = itemService.findItems();
        model.addAttribute("members", members);
        model.addAttribute("items", items);
        return "order/orderForm";

    }


    @PostMapping("/order")
    public String createOrder(@RequestParam Long memberId,
                              @RequestParam Long itemId,
                              @RequestParam int count){
        orderService.order(memberId, itemId, count);
        return "redirect:/";
    }

    @GetMapping("/orders")
    public String orderList(@ModelAttribute("orderSearch")OrderSearch orderSearch, Model model){
        List<Order> orders = orderService.findOlders(orderSearch);
        model.addAttribute("orders",orders);
        return "order/orderList";
    }

    @PostMapping("/orders/{orderId}/cancel")
    public String cancelOrder(@PathVariable("orderId") Long orderId){
        orderService.cancelOrder(orderId);
        return "redirect:/orders";
    }

}
