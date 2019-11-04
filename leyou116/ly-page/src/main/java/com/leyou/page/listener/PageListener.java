package com.leyou.page.listener;

import com.leyou.common.constants.MQConstants;
import com.leyou.page.service.PageService;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.leyou.common.constants.MQConstants.Exchange.ITEM_EXCHANGE_NAME;
import static com.leyou.common.constants.MQConstants.Queue.PAGE_ITEM_DOWN;
import static com.leyou.common.constants.MQConstants.Queue.PAGE_ITEM_UP;

@Component
public class PageListener {

    @Autowired
    private PageService pageService;

    /**
     * 新增静态页
     * @param id  要和发送消息一方的参数一模一样
     */
    @RabbitListener(bindings=@QueueBinding(
            value = @Queue(value = PAGE_ITEM_UP, durable="true"),//指定监听的队列，并可以创建队列
            exchange = @Exchange(value = ITEM_EXCHANGE_NAME, type = ExchangeTypes.TOPIC),//指定创建交换机
            key = MQConstants.RoutingKey.ITEM_UP_KEY//指定交互机与当前队列之间的通信规则
    ))
    public void addPage(Long id){
        pageService.createStaticItemPage(id);
    }

    /**
     * 删除静态页
     * @param id  要和发送消息一方的参数一模一样
     */
    @RabbitListener(bindings=@QueueBinding(
            value = @Queue(value = PAGE_ITEM_DOWN, durable="true"),//指定监听的队列，并可以创建队列
            exchange = @Exchange(value = ITEM_EXCHANGE_NAME, type = ExchangeTypes.TOPIC),//指定创建交换机
            key = MQConstants.RoutingKey.ITEM_DOWN_KEY//指定交互机与当前队列之间的通信规则
    ))
    public void delPage(Long id){
        pageService.deleteStaticPage(id);
    }

}
