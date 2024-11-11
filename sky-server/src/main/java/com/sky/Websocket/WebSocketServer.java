package com.sky.Websocket;


import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

// TODO WebSocket日后也可以算是亮点
// WebSocket是一种基于TCP的新的网络协议，它实现了浏览器和服务器的全双工通信————浏览器和服务器只需要完成一次握手，二者就
// 可以创建持久性的连接，并可以进行双向的数据传输

// Http和WebSocket相比：
// 1.Http是短连接，WebSocket是长连接
// 2.Http是单向通信的，是基于请求响应模式完成的；WebSocket是双向通信的，不需要请求响应也可以完成通信
// 3.但Http和WebSocket二者底层都是TCP连接的

// WebSocket支持双向通信，功能强大，是否可以通过WebSocket开发所有业务功能，从而取代Http？
// 其实是不行的，WebSocket有很多优点的同时，也伴随着许多缺点：1.长连接是需要很多资源的，服务器维护长连接需要成本
// 2.浏览器的支持程度不同，所以说兼容性存在问题     3.WebSocket是长连接，所以说受网络限制较大，需要处理重连问题
// 所以说WebSocket不能完全取代Http，只适合在特定的场景下使用

// WebSocket的使用场景：不需要请求响应就可以直接更新的网页，浏览器既可以向服务器发送消息，服务器也可主动向浏览器推送消息
// 如：视频弹幕、网页聊天、体育实况更新、股票基金报价实时更新；主要是一些需要实时数据更新的场景

// 在maven项目中使用WebSocket
// 1.导入WebSocket的maven坐标
// 2.导入WebSocket服务端组件WebSocketServer，用于和客户端通信
// 3.导入配置类WebSocketConfiguration，注册WebSocket的服务端组件
// 4.编写代码逻辑，判断什么时候需要向客户端推送数据

/**
 * WebSocket服务
 *
 */
@Component
@ServerEndpoint("/ws/{sid}")
public class WebSocketServer {

    // 存放会话对象
    private static Map<String, Session> sessionMap = new HashMap<>();

    /**
     * 连接建立成功调用的方法
     *
     * @param session
     * @param sid
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("sid") String sid) {
        System.out.println("客户端" + sid + "建立连接");
        sessionMap.put(sid, session);
    }

    /**
     * 收到客户端消息之后调用的方法
     *
     * @param message
     * @param sid
     */
    @OnMessage
    public void onMessage(String message, @PathParam("sid") String sid) {
        System.out.println("收到来自客户端：" + sid + "的信息是：" + message);
    }

    /**
     * 关闭连接调用方法
     *
     * @param sid
     */
    @OnClose
    public void onClose(@PathParam("sid") String sid) {
        System.out.println("连接断开：" + sid);
        sessionMap.remove(sid);
    }

    /**
     * 群发
     *
     * @param message
     */
    // 因为可能有很多客户端都连接了这个服务端，所以说在更新数据的时候需要群发，为所有连接了的客户端都更新
    public void sendToAllClients(String message) {
        Collection<Session> sessions = sessionMap.values();
        for (Session session : sessions) {
            try {
                // 服务器向客户端发送消息
                session.getBasicRemote().sendText(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
