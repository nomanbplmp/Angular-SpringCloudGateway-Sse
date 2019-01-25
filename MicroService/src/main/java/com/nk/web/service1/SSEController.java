package com.nk.web.service1;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.servlet.http.HttpServletResponse;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Controller
public class SSEController {

  private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();
  
  
  @GetMapping("/ticks")
  public SseEmitter handle(HttpServletResponse response) {
    response.setHeader("Cache-Control", "no-store");

    SseEmitter emitter = new SseEmitter();
    // SseEmitter emitter = new SseEmitter(180_000L);

    this.emitters.add(emitter);

    emitter.onCompletion(() -> this.emitters.remove(emitter));
    emitter.onTimeout(() -> this.emitters.remove(emitter));

    new Thread(()-> {
    	while(true) {
    		try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		//System.out.println("calling tick");
    		onTick(new TickInfo("tick" + UUID.randomUUID()));
    	}
    }).start();
    
    return emitter;
  }

  @EventListener
  public void onTick(TickInfo tickInfo) {
    List<SseEmitter> deadEmitters = new ArrayList<>();
    this.emitters.forEach(emitter -> {
      try {
        emitter.send(tickInfo);

        // close connnection, browser automatically reconnects
        // emitter.complete();

        // SseEventBuilder builder = SseEmitter.event().name("second").data("1");
        // SseEventBuilder builder =
        // SseEmitter.event().reconnectTime(10_000L).data(memoryInfo).id("1");
        // emitter.send(builder);
      }
      catch (Exception e) {
        deadEmitters.add(emitter);
      }
    });

    this.emitters.removeAll(deadEmitters);
  }







}
