package com.healthcare.assistant.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@ComponentScan(basePackages = "com.healthcare.assistant.controller")
@Configuration
public class ComponentScanForControllers {
}