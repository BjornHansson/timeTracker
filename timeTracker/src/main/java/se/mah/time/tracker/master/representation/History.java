package se.mah.time.tracker.master.representation;

public class History {
	private String date;
	private String time;

	public History(String date, String time) {
		this.date = date;
		this.time = time;
	}

	public String getDate() {
		return date;
	}

	public String getTime() {
		return time;
	}
}
