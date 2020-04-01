package com.coronavirus.coronavirustracker.services;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.coronavirus.coronavirustracker.model.LocationStats;

@Service
public class CoronaVirusDataService {

	private static String Virus_Data_URL="https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_confirmed_global.csv";
	private List<LocationStats> allstats=new ArrayList<LocationStats>();
	
	public List<LocationStats> getAllstats() {
		return allstats;
	}
	public void setAllstats(List<LocationStats> allstats) {
		this.allstats = allstats;
	}
	private List<LocationStats> newStatsForIndia=new ArrayList<LocationStats>();
	public List<LocationStats> getNewStatsForIndia() {
		return newStatsForIndia;
	}
	public void setNewStatsForIndia(List<LocationStats> newStatsForIndia) {
		this.newStatsForIndia = newStatsForIndia;
	}
	@PostConstruct
	@Scheduled(cron = "0 10 6 * * ?")
	public void fetchVirusData() throws IOException, InterruptedException {
		List<LocationStats> newStats=new ArrayList<LocationStats>();
		
		//create a new httpclient
		HttpClient client=HttpClient.newHttpClient();
		//create the request using the builder pattern
		HttpRequest request=HttpRequest.newBuilder().uri(URI.create(Virus_Data_URL)).build();
		//take the body and return as string
		HttpResponse<String> httpResponse=client.send(request, HttpResponse.BodyHandlers.ofString());
		StringReader csvBodyReader=new StringReader(httpResponse.body());
		Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(csvBodyReader);
		for (CSVRecord record : records) {
			LocationStats locationStats=new LocationStats();
			if(record.get("Province/State").equals("")||record.get("Province/State")==null)
				locationStats.setState("ALL");
			else
				locationStats.setState(record.get("Province/State"));
			locationStats.setCountry(record.get("Country/Region"));
			int prevDayCases=Integer.parseInt(record.get(record.size()-2));
			int latestDayCases=Integer.parseInt(record.get(record.size()-1));
			locationStats.setLatestTotalCases(latestDayCases);
			locationStats.setDiffFromPreviousDay(latestDayCases-prevDayCases);
			
		    newStats.add(locationStats);
		    if(record.get("Country/Region").equalsIgnoreCase("India")) {
			newStatsForIndia.clear();
		    	LocationStats locationStatsForIndia=new LocationStats();
		    	locationStatsForIndia.setCountry(record.get("Country/Region"));
		    	locationStatsForIndia.setState(record.get("Province/State"));
		    	int latestDayCasesForIndia=Integer.parseInt(record.get(record.size()-1));
		    	int prevDayCasesForIndia=Integer.parseInt(record.get(record.size()-2));
		    	locationStatsForIndia.setLatestTotalCases(latestDayCasesForIndia);
		    	locationStatsForIndia.setDiffFromPreviousDay(latestDayCasesForIndia-prevDayCasesForIndia);
		    	newStatsForIndia.add(locationStatsForIndia);
		    }
		    
		}
		Collections.sort(newStats, new Comparator<LocationStats>() {

			@Override
			public int compare(LocationStats o1, LocationStats o2) {
				return	o1.getCountry().compareTo(o2.getCountry());
				 
			}
		});
		this.allstats=newStats;
		
	}
}
