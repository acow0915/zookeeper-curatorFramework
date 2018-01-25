package com.java.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ZookeeperTestController {
	
//	@Autowired
//    private ZooService zooService;
//	
//	@RequestMapping("/home")
//	public String zookeeperTest(){
//		return zooService.zooService();
//	}
	
	
	@RequestMapping("/sayHello")
	public String testZoo(){
		return "Hello world";
	}
}
