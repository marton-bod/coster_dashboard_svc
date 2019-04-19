package io.coster.dashboard;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import io.coster.dashboard.domain.Expense;
import io.coster.dashboard.domain.LineChartEntry;
import io.coster.dashboard.domain.PieChartEntry;
import io.coster.usermanagementsvc.contract.AuthenticationResponse;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;
import java.util.DoubleSummaryStatistics;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-integrationtest.properties")
public class DashboardSvcApplicationTests {

	private static final ParameterizedTypeReference<List<LineChartEntry>> LINE_CHART_LIST_TYPE
			= new ParameterizedTypeReference<>() {};
	private static final ParameterizedTypeReference<List<PieChartEntry>> PIE_CHART_LIST_TYPE
			= new ParameterizedTypeReference<>() {};


	private static WireMockServer wireMockServer;
	private TestRestTemplate restTemplate = new TestRestTemplate();

	@LocalServerPort
	int port;

	@BeforeClass
	public static void startWireMock() throws JsonProcessingException {
		ObjectMapper objectMapper = new ObjectMapper();
		AuthenticationResponse response = AuthenticationResponse.builder()
				.valid(true)
				.build();
		configureFor("localhost", 10001);
		wireMockServer = new WireMockServer(10001);
		wireMockServer.start();
		stubFor(post(urlEqualTo("/auth/validate")).willReturn(aResponse()
				.withStatus(200)
				.withHeader("Content-type", "application/json")
				.withBody(objectMapper.writeValueAsString(response))));
	}

	@Test
	public void testDailyExpenses_WhenExistingEntries() {
		HttpHeaders cookieHeaders = getCookieHeaders("test@test.co.uk", "1234567");
		String month = "2019-01";

		ResponseEntity<List<LineChartEntry>> response = restTemplate.exchange(String.format("http://localhost:%d/dashboard/daily?month=%s", port, month),
				HttpMethod.GET, new HttpEntity<>(cookieHeaders), LINE_CHART_LIST_TYPE);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		List<LineChartEntry> entries = response.getBody();
		assertEquals(3, entries.size());
		assertEquals("12", entries.get(0).getX());
		assertEquals(2500, entries.get(0).getY(), 0.0001);
		assertEquals("20", entries.get(1).getX());
		assertEquals(1200 + 33500, entries.get(1).getY(), 0.0001);
		assertEquals("28", entries.get(2).getX());
		assertEquals(22500, entries.get(2).getY(), 0.0001);
	}

	@Test
	public void testDailyExpenses_WhenNoEntriesDontExist() {
		HttpHeaders cookieHeaders = getCookieHeaders("test@test.co.uk", "1234567");
		String month = "2019-04";

		ResponseEntity<List<LineChartEntry>> response = restTemplate.exchange(String.format("http://localhost:%d/dashboard/daily?month=%s", port, month),
				HttpMethod.GET, new HttpEntity<>(cookieHeaders), LINE_CHART_LIST_TYPE);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		List<LineChartEntry> entries = response.getBody();
		assertEquals(0, entries.size());
	}

	@Test
	public void testTotalStats_WhenExistingEntries() {
		HttpHeaders cookieHeaders = getCookieHeaders("test@test.co.uk", "1234567");
		String month = "2019-01";

		ResponseEntity<DoubleSummaryStatistics> response = restTemplate.exchange(String.format("http://localhost:%d/dashboard/total?month=%s", port, month),
				HttpMethod.GET, new HttpEntity<>(cookieHeaders), DoubleSummaryStatistics.class);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		DoubleSummaryStatistics stats = response.getBody();
		assertEquals(3, stats.getCount());
		assertEquals(33500 + 1200, stats.getMax(), 0.0001);
		assertEquals(2500, stats.getMin(), 0.0001);
		assertEquals(2500 + 1200 + 22500 + 33500, stats.getSum(), 0.0001);
	}

	@Test
	public void testCategoryStats_WhenExistingEntries() {
		HttpHeaders cookieHeaders = getCookieHeaders("test@test.co.uk", "1234567");
		String month = "2019-01";

		ResponseEntity<List<PieChartEntry>> response = restTemplate.exchange(String.format("http://localhost:%d/dashboard/category?month=%s", port, month),
				HttpMethod.GET, new HttpEntity<>(cookieHeaders), PIE_CHART_LIST_TYPE);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		List<PieChartEntry> entries = response.getBody();
		assertEquals(3, entries.size());
		assertEquals("CAFE", entries.get(0).getName());
		assertEquals(1200 + 22500, entries.get(0).getValue(), 0.0001);
		assertEquals("EATOUT", entries.get(1).getName());
		assertEquals(2500, entries.get(1).getValue(), 0.0001);
		assertEquals("ENTERTAINMENT", entries.get(2).getName());
		assertEquals(33500, entries.get(2).getValue(), 0.0001);
	}

	@Test
	public void testCategoryStats_WhenNoEntriesExist() {
		HttpHeaders cookieHeaders = getCookieHeaders("test@test.co.uk", "1234567");
		String month = "2019-04";

		ResponseEntity<List<PieChartEntry>> response = restTemplate.exchange(String.format("http://localhost:%d/dashboard/category?month=%s", port, month),
				HttpMethod.GET, new HttpEntity<>(cookieHeaders), PIE_CHART_LIST_TYPE);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		List<PieChartEntry> entries = response.getBody();
		assertEquals(0, entries.size());
	}

	@Test
	public void testMonthlyExpenses() {
		HttpHeaders cookieHeaders = getCookieHeaders("test@test.co.uk", "1234567");

		ResponseEntity<List<LineChartEntry>> response = restTemplate.exchange(String.format("http://localhost:%d/dashboard/monthly", port),
				HttpMethod.GET, new HttpEntity<>(cookieHeaders), LINE_CHART_LIST_TYPE);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		List<LineChartEntry> entries = response.getBody();
		assertEquals(12, entries.size());
		entries.stream().filter(e -> e.getX().equals("2019-01"))
				.findFirst()
				.ifPresent(e -> assertEquals(500 + 1200 + 22500 + 33500, e.getY(), 0.0001));
	}

	private HttpHeaders getCookieHeaders(String userId, String token) {
		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.put("auth_id", Collections.singletonList(userId));
		requestHeaders.put("auth_token", Collections.singletonList(token));
		return requestHeaders;
	}

}
