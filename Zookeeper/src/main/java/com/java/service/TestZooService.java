package com.java.service;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient("testZoo")
public interface TestZooService {

	@RequestMapping(method = RequestMethod.GET, value = "/testZoo")
    String testZoo();
}
