package com.github.tessdev.holidayservice.controller;

import com.github.tessdev.holidayservice.model.Holiday;
import com.github.tessdev.holidayservice.service.HolidayService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/holidays")
public class HolidayController {

    private final HolidayService holidayService;

    public HolidayController(HolidayService holidayService) {
        this.holidayService = holidayService;
    }

    @GetMapping
    public ResponseEntity<List<Holiday>> list() {
        return ResponseEntity.ok(holidayService.getAll());
    }
}
