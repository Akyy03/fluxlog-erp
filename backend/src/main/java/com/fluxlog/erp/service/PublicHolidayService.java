package com.fluxlog.erp.service;

import com.fluxlog.erp.dto.external.PublicHolidayDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PublicHolidayService {

    private final RestTemplate restTemplate;

    public List<LocalDate> getRomanianHolidays(int year) {
        String url = "https://date.nager.at/api/v3/PublicHolidays/" + year + "/RO";

        try {
            PublicHolidayDTO[] response = restTemplate.getForObject(url, PublicHolidayDTO[].class);

            if (response != null) {
                return Arrays.stream(response)
                        .map(holiday -> LocalDate.parse(holiday.getDate()))
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            System.err.println("Eroare la obținerea sărbătorilor legale: " + e.getMessage());
        }

        return List.of();
    }
}