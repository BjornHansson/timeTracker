package se.mah.time.tracker.master;

import static java.net.HttpURLConnection.HTTP_CREATED;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static spark.Spark.before;
import static spark.Spark.get;
import static spark.Spark.port;
import static spark.Spark.post;
import static spark.Spark.staticFileLocation;
import static spark.Spark.stop;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.hsqldb.lib.StopWatch;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mysql.cj.jdbc.MysqlDataSource;

import se.mah.time.tracker.master.representation.History;
import se.mah.time.tracker.master.representation.TrackTime;

/**
 * The web API for the client
 * 
 * @author Björn Hansson
 *
 */
public class WebApi {

	public static final int SERVER_PORT = 8585;
	private static final String WEB_PAGE_FOLDER = "/public";
	private static final Gson GSON = new GsonBuilder().create();
	private static final String USE_TRACK_TIME = "USE track_time";
	private MysqlDataSource myDataSource = new MysqlDataSource();
	private StopWatch myStopWatch = new StopWatch();
	private TrackTime myTrackTimeChecker = new TrackTime();

	/**
	 * To launch the web API from IDE and then sleep
	 * 
	 * @param args
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws InterruptedException {
		new WebApi();
		TimeUnit.DAYS.sleep(365);
	}

	/**
	 * Constructor to launch the web API from code
	 */
	public WebApi() {
		initDatabase();
		initWebApi();
	}

	/**
	 * Close/stop the web API
	 */
	public void close() {
		stop();
	}

	private void initDatabase() {
		InputStream input = WebApi.class.getClassLoader().getResourceAsStream("database.properties");
		Properties properties = new Properties();
		try {
			properties.load(input);
		} catch (IOException e) {
			e.printStackTrace();
		}
		myDataSource.setUser(properties.getProperty("user"));
		myDataSource.setPassword(properties.getProperty("password"));
		myDataSource.setServerName(properties.getProperty("server"));
	}

	private void initWebApi() {
		staticFileLocation(WEB_PAGE_FOLDER);
		port(SERVER_PORT);
		before((request, response) -> {
			response.type(APPLICATION_JSON);
		});

		// Start or stop tracking time
		post("/time", (request, response) -> {
			response.status(HTTP_CREATED);
			String body = request.body();
			myTrackTimeChecker = GSON.fromJson(body, TrackTime.class);
			handleTimeRecording(myTrackTimeChecker.isTrackingTime());
			return GSON.toJson(myTrackTimeChecker);
		});

		// Check if time is tracked now or not
		get("/time", (request, response) -> {
			return GSON.toJson(myTrackTimeChecker);
		});

		// Get history of tracked time
		get("/time/:start/:end", (request, response) -> {
			String startDate = request.params("start");
			String endDate = request.params("end");
			List<History> history = fetchHistory(startDate, endDate);
			return GSON.toJson(history);
		});
	}

	private void handleTimeRecording(boolean trackTime) {
		if (trackTime) {
			myStopWatch.zero();
		} else {
			myStopWatch.stop();

			long elapsedTime = myStopWatch.elapsedTime();
			String formattedTime = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(elapsedTime),
					TimeUnit.MILLISECONDS.toMinutes(elapsedTime) % TimeUnit.HOURS.toMinutes(1),
					TimeUnit.MILLISECONDS.toSeconds(elapsedTime) % TimeUnit.MINUTES.toSeconds(1));

			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			Date date = new Date();
			String formattedDate = dateFormat.format(date);

			StringBuilder query = new StringBuilder();
			query.append("INSERT INTO time_recorded (id, date, time) VALUES (NULL, ");
			query.append("'");
			query.append(formattedDate);
			query.append("', '");
			query.append(formattedTime);
			query.append("')");
			try {
				Connection connection = myDataSource.getConnection();
				Statement statement = connection.createStatement();
				statement.executeQuery(USE_TRACK_TIME);
				statement.executeUpdate(query.toString());
				statement.close();
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	private List<History> fetchHistory(String startDate, String endDate) {
		List<History> history = new ArrayList<History>();
		StringBuilder query = new StringBuilder();
		query.append("SELECT date, time FROM time_recorded WHERE date BETWEEN ");
		query.append("'");
		query.append(startDate);
		query.append("' AND '");
		query.append(endDate);
		query.append("'");
		try {
			Connection connection = myDataSource.getConnection();
			Statement statement = connection.createStatement();
			statement.executeQuery(USE_TRACK_TIME);
			ResultSet result = statement.executeQuery(query.toString());
			while (result.next()) {
				History element = new History(result.getString("date"), result.getString("time"));
				history.add(element);
			}
			statement.close();
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return history;
	}
}
