package com.coronavirus.coronavirustracker.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.coronavirus.coronavirustracker.model.LocationStats;
import com.coronavirus.coronavirustracker.services.CoronaVirusDataService;

@Controller
public class HomeController {

	@Autowired
	CoronaVirusDataService coronaVirusDataService;
	
	@GetMapping("/")
	public String home(Model model) {
		List<LocationStats> allStats=coronaVirusDataService.getAllstats();
		int totalCases=allStats.stream().mapToInt(stat->stat.getLatestTotalCases()).sum();
		int totalNewCases = allStats.stream().mapToInt(stat -> stat.getDiffFromPreviousDay()).sum();
		model.addAttribute("localStats",coronaVirusDataService.getAllstats());
		model.addAttribute("totalReportedCases",totalCases);
		model.addAttribute("totalNewCases", totalNewCases);
		int totalIndiaCases=coronaVirusDataService.getNewStatsForIndia().get(0).getLatestTotalCases();
		int latestIndiaCases=coronaVirusDataService.getNewStatsForIndia().get(0).getDiffFromPreviousDay();
		model.addAttribute("totalIndiaCases", totalIndiaCases);
		model.addAttribute("latestIndiaCases", latestIndiaCases);
		
		return "home";
		
	}
}
