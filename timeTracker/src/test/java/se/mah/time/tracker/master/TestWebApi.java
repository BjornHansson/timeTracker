package se.mah.time.tracker.master;

import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_OK;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import org.junit.AfterClass;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import se.mah.time.tracker.master.WebApi;
import se.mah.time.tracker.master.representation.History;
import se.mah.time.tracker.master.representation.TrackTime;

public class TestWebApi {

	private static final String URL = "http://localhost:" + WebApi.SERVER_PORT;
	private static final Client CLIENT = ClientBuilder.newClient();
	private static final Gson GSON = new Gson();
	private static final WebApi API = new WebApi();

	@AfterClass
	public static void afterClass() {
		API.close();
	}

	@Test
	public void testGetIfTimeIsTracked() {
		Response response = CLIENT.target(URL + "/time").request(APPLICATION_JSON).get();
		String actualBody = response.readEntity(String.class);
		assertEquals(HTTP_OK, response.getStatus());
		TrackTime actual = GSON.fromJson(actualBody, TrackTime.class);
		assertFalse(actual.isTrackingTime());
	}

	@Test
	public void testPostStartAndStopTrackingTime() {
		// Start
		Entity<String> payloadStart = Entity.json("{'trackingTime': true}");
		Response responseStart = CLIENT.target(URL + "/time").request(APPLICATION_JSON).post(payloadStart);
		String actualBodyStart = responseStart.readEntity(String.class);
		assertEquals(HTTP_CREATED, responseStart.getStatus());
		TrackTime actualStart = GSON.fromJson(actualBodyStart, TrackTime.class);
		assertTrue(actualStart.isTrackingTime());
		// Stop
		Entity<String> payloadStop = Entity.json("{'trackingTime': false}");
		Response responseStop = CLIENT.target(URL + "/time").request(APPLICATION_JSON).post(payloadStop);
		String actualBodyStop = responseStop.readEntity(String.class);
		assertEquals(HTTP_CREATED, responseStop.getStatus());
		TrackTime actualStop = GSON.fromJson(actualBodyStop, TrackTime.class);
		assertFalse(actualStop.isTrackingTime());
	}

	@Test
	public void testGetHistoryOfTrackedTime() {
		// Start
		Entity<String> payloadStart = Entity.json("{'trackingTime': true}");
		CLIENT.target(URL + "/time").request(APPLICATION_JSON).post(payloadStart);
		// Stop
		Entity<String> payloadStop = Entity.json("{'trackingTime': false}");
		CLIENT.target(URL + "/time").request(APPLICATION_JSON).post(payloadStop);
		// Get history
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date();
		String formattedDate = dateFormat.format(date);
		Response response = CLIENT.target(URL + "/time/" + formattedDate + "/" + formattedDate)
				.request(APPLICATION_JSON).get();
		String actualBody = response.readEntity(String.class);
		assertEquals(HTTP_OK, response.getStatus());
		Type listType = new TypeToken<ArrayList<History>>() {
		}.getType();
		List<History> actual = GSON.fromJson(actualBody, listType);
		assertFalse(actual.isEmpty());
		assertFalse(actual.get(actual.size() - 1).getTime().isEmpty());
		assertEquals(formattedDate, actual.get(actual.size() - 1).getDate());
	}
}
